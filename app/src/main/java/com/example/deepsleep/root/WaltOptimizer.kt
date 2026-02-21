package com.example.deepsleep.root

import android.util.Log

object WaltOptimizer {
    private const val TAG = "WaltOptimizer"

    suspend fun applyGlobalOptimizations(): Boolean {
        return try {
            Log.d(TAG, "应用全局优化")
            applyDefault()
        } catch (e: Exception) {
            Log.e(TAG, "全局优化失败: ${e.message}")
            false
        }
    }

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

    suspend fun applyCustom(params: Map<String, Int>): Boolean {
        return try {
            applyMode("custom", params)
        } catch (e: Exception) {
            Log.e(TAG, "applyCustom 失败", e)
            false
        }
    }

    private suspend fun applyMode(mode: String, params: Map<String, Int>): Boolean {
        return try {
            Log.d(TAG, "应用 CPU 模式: $mode")

            val commands = mutableListOf<String>()

            val policiesResult = RootCommander.exec("ls -d /sys/devices/system/cpu/cpufreq/policy* 2>/dev/null")
            if (!policiesResult.isSuccess) {
                Log.w(TAG, "未找到 CPU 策略目录")
                return false
            }

            val policies = policiesResult.out.joinToString("\n").trim().split("\n").filter { it.isNotEmpty() }

            for (policy in policies) {
                val waltDir = "$policy/walt"
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