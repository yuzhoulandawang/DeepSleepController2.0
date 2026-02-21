package com.example.deepsleep.root

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object DeepSleepController {
    private const val TAG = "DeepSleepController"
    private var isInDeepSleep = false
    private var checkJob: Job? = null
    
    suspend fun enterDeepSleep(
        blockExit: Boolean,
        checkIntervalSeconds: Int
    ): Boolean {
        return try {
            Log.d(TAG, "准备进入深度睡眠...")
            Log.d(TAG, "参数: blockExit=$blockExit, checkInterval=$checkIntervalSeconds")
            
            val dozeSuccess = DozeController.forceIdle()
            if (!dozeSuccess) {
                Log.e(TAG, "Doze 进入失败")
                return false
            }
            Log.i(TAG, "Doze 模式已进入")
            
            if (blockExit) {
                Log.d(TAG, "屏蔽移动检测...")
                RootCommander.exec("settings put global motion_detection_enabled 0")
                Log.i(TAG, "移动检测已屏蔽")
                
                Log.d(TAG, "限制唤醒源...")
                RootCommander.exec("echo 0 > /sys/power/autosleep")
                Log.i(TAG, "唤醒源已限制")
            }
            
            RootCommander.exec("echo 'active' > /data/local/tmp/deep_sleep_status")
            Log.i(TAG, "深度睡眠已进入")
            
            isInDeepSleep = true
            
            if (blockExit) {
                startStatusCheck(checkIntervalSeconds)
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "进入深度睡眠失败: ${e.message}", e)
            false
        }
    }
    
    suspend fun exitDeepSleep(): Boolean {
        return try {
            Log.d(TAG, "准备退出深度睡眠...")
            
            checkJob?.cancel()
            checkJob = null
            
            Log.d(TAG, "恢复移动检测...")
            RootCommander.exec("settings put global motion_detection_enabled 1")
            
            Log.d(TAG, "恢复唤醒源...")
            RootCommander.exec("echo 1 > /sys/power/autosleep")
            
            RootCommander.exec("rm -f /data/local/tmp/deep_sleep_status")
            
            val dozeSuccess = DozeController.step()
            if (dozeSuccess) {
                Log.i(TAG, "已退出深度睡眠")
            } else {
                Log.w(TAG, "退出 Doze 模式失败")
            }
            
            isInDeepSleep = false
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "退出深度睡眠失败: ${e.message}", e)
            false
        }
    }
    
    fun isInDeepSleep(): Boolean = isInDeepSleep
    
    private fun startStatusCheck(intervalSeconds: Int) {
        checkJob?.cancel()
        checkJob = CoroutineScope(Dispatchers.IO).launch {
            var checkCount = 0
            while (isActive) {
                delay(intervalSeconds * 1000L)
                checkCount++
                
                val isIdle = checkDozeStatus()
                
                if (isIdle) {
                    if (checkCount % 10 == 0) {
                        Log.d(TAG, "状态检查 #$checkCount: 正常")
                    }
                } else {
                    Log.w(TAG, "状态检查 #$checkCount: 检测到意外退出，正在重新进入...")
                    enterDeepSleep(blockExit = true, checkIntervalSeconds = intervalSeconds)
                    Log.i(TAG, "已重新进入深度睡眠")
                    checkCount = 0
                }
            }
        }
    }
    
    private suspend fun checkDozeStatus(): Boolean {
        return try {
            val result = RootCommander.exec("dumpsys deviceidle")
            result.out.contains("IDLE")
        } catch (e: Exception) {
            false
        }
    }
    
    fun stopAll() {
        checkJob?.cancel()
        checkJob = null
        isInDeepSleep = false
    }
}