package com.example.deepsleep.root

import com.example.deepsleep.data.LogRepository

object WaltOptimizer {
    private const val TAG = "WaltOptimizer"
    
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
            false
        }
    }
    
    // 应用自定义参数
    suspend fun applyCustom(params: Map<String, Int>): Boolean {
        return try {
            applyMode("custom", params)
        } catch (e: Exception) {
            false
        }
    }
    
    // 通用应用方法
    private suspend fun applyMode(mode: String, params: Map<String, Int>): Boolean {
        return try {
            LogRepository.info(TAG, "应用 CPU 模式: $mode")
            
            val commands = mutableListOf<String>()
            
            // 获取所有 CPU 策略目录
            val policiesResult = RootCommander.exec("ls -d /sys/devices/system/cpu/cpufreq/policy* 2>/dev/null")
            if (!policiesResult.isSuccess) {
                LogRepository.warning(TAG, "未找到 CPU 策略目录")
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
                LogRepository.success(TAG, "CPU 模式已应用: $mode")
                true
            } else {
                LogRepository.warning(TAG, "没有可应用的参数")
                false
            }
        } catch (e: Exception) {
            LogRepository.error(TAG, "应用 CPU 模式失败: ${e.message}")
            false
        }
    }
    
    // 恢复默认设置
    suspend fun restoreDefault(): Boolean {
        return try {
            LogRepository.info(TAG, "恢复 CPU 默认设置")
            RootCommander.exec("resetprop walt.enabled false")
            true
        } catch (e: Exception) {
            LogRepository.error(TAG, "恢复默认设置失败: ${e.message}")
            false
        }
    }
}
