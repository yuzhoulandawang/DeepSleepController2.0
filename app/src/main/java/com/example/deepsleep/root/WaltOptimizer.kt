package com.example.deepsleep.root

import android.util.Log
import com.example.deepsleep.data.LogRepository

object WaltOptimizer {
    private const val TAG = "WaltOptimizer"

    // 新增：全局优化方法（DeepSleepService 调用）
    suspend fun applyGlobalOptimizations(): Boolean {
        return try {
            Log.d(TAG, "应用全局优化")
            // 这里可以添加原有的全局优化命令，例如：
            // RootCommander.exec("echo 0 > /proc/sys/kernel/panic")
            // 等。如果没有具体实现，可以先调用 applyDefault()
            applyDefault()
        } catch (e: Exception) {
            Log.e(TAG, "全局优化失败: ${e.message}")
            false
        }
    }

    // 日常模式参数
    suspend fun applyDaily(params: Map<String, Int>? = null): Boolean {
        return try {
            val actualParams = params ?: mapOf(
                "up_rate_limit_us" to 1000,
                "down_rate_limit_us" to 500,
                "hispeed_load" to 85,
                "target_loads" to 80
            )
            applyMode("daily", actualParams)
        } catch (e: Exception) {
            Log.e(TAG, "applyDaily 失败", e)
            false
        }
    }

    // 待机模式参数
    suspend fun applyStandby(params: Map<String, Int>? = null): Boolean {
        return try {
            val actualParams = params ?: mapOf(
                "up_rate_limit_us" to 5000,
                "down_rate_limit_us" to 0,
                "hispeed_load" to 95,
                "target_loads" to 90
            )
            applyMode("standby", actualParams)
        } catch (e: Exception) {
            Log.e(TAG, "applyStandby 失败", e)
            false
        }
    }

    // 默认模式参数
    suspend fun applyDefault(params: Map<String, Int>? = null): Boolean {
        return try {
            val actualParams = params ?: mapOf(
                "up_rate_limit_us" to 0,
                "down_rate_limit_us" to 0,
                "hispeed_load" to 90,
                "target_loads" to 90
            )
            applyMode("default", actualParams)
        } catch (e: Exception) {
            Log.e(TAG, "applyDefault 失败", e)
            false
        }
    }

    // 性能模式参数
    suspend fun applyPerformance(params: Map<String, Int>? = null): Boolean {
        return try {
            val actualParams = params ?: mapOf(
                "up_rate_limit_us" to 0,
                "down_rate_limit_us" to 0,
                "hispeed_load" to 75,
                "target_loads" to 70
            )
            applyMode("performance", actualParams)
        } catch (e: Exception) {
            Log.e(TAG, "applyPerformance 失败", e)
            false
        }
    }

    // 应用自定义参数
    suspend fun applyCustom(params: Map<String, Int>): Boolean {
        return try {
            applyMode("custom", params)
        } catch (e: Exception) {
            Log.e(TAG, "applyCustom 失败", e)
            false
        }
    }

    // 通用应用方法
    private suspend fun applyMode(mode: String, params: Map<String, Int>): Boolean {
        return try {
            Log.d(TAG, "应用 CPU 模式: $mode")

            val commands = mutableListOf<String>()

            // 获取所有 CPU 策略目录
            val policiesResult = RootCommander.exec("ls -d /sys/devices/system/cpu/cpufreq/policy* 2>/dev/null")
            if (!policiesResult.isSuccess) {
                Log.w(TAG, "未找到 CPU 策略目录")
                return false
            }

            val policies = policiesResult.out.trim().split("\n").filter { it.isNotEmpty() }

            for (policy in policies) {
                val waltDir = "$policy/walt"

                // 应用参数
                params.forEach { (key, value) ->
                    commands.add("printf '%s' \"$value\" > $waltDir/$key 2>/dev/null || true")
                }
            }

            if (commands.isNotEmpty()) {
                RootCommander.execBatch(commands)
                Log.i(TAG, "CPU 模式已应用: $mode")
                true
            } else {
                Log.w(TAG, "没有可应用的参数")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "应用 CPU 模式失败: ${e.message}", e)
            false
        }
    }

    // 恢复默认设置
    suspend fun restoreDefault(): Boolean {
        return try {
            Log.d(TAG, "恢复 CPU 默认设置")
            RootCommander.exec("resetprop walt.enabled false")
            true
        } catch (e: Exception) {
            Log.e(TAG, "恢复默认设置失败: ${e.message}", e)
            false
        }
    }
}