package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository

object PowerSaverController {
    private const val TAG = "PowerSaverController"
    
    // 开启系统省电模式
    suspend fun enablePowerSaver(): Boolean {
        return try {
            LogRepository.info(TAG, "开启系统省电模式...")
            RootCommander.exec("settings put global low_power 1")
            LogRepository.success(TAG, "系统省电模式已开启")
            true
        } catch (e: Exception) {
            LogRepository.error(TAG, "开启系统省电模式失败: ${e.message}")
            false
        }
    }
    
    // 关闭系统省电模式
    suspend fun disablePowerSaver(): Boolean {
        return try {
            LogRepository.info(TAG, "关闭系统省电模式...")
            RootCommander.exec("settings put global low_power 0")
            LogRepository.success(TAG, "系统省电模式已关闭")
            true
        } catch (e: Exception) {
            LogRepository.error(TAG, "关闭系统省电模式失败: ${e.message}")
            false
        }
    }
    
    // 检查系统省电模式状态
    suspend fun isEnabled(): Boolean {
        return try {
            val result = RootCommander.exec("settings get global low_power")
            result.out.trim() == "1"
        } catch (e: Exception) {
            false
        }
    }
}
