package com.example.deepsleep.root

import android.util.Log

object PowerSaverController {
    private const val TAG = "PowerSaverController"
    
    suspend fun enablePowerSaver(): Boolean {
        return try {
            Log.d(TAG, "开启系统省电模式...")
            RootCommander.exec("settings put global low_power 1")
            Log.i(TAG, "系统省电模式已开启")
            true
        } catch (e: Exception) {
            Log.e(TAG, "开启系统省电模式失败: ${e.message}")
            false
        }
    }
    
    suspend fun disablePowerSaver(): Boolean {
        return try {
            Log.d(TAG, "关闭系统省电模式...")
            RootCommander.exec("settings put global low_power 0")
            Log.i(TAG, "系统省电模式已关闭")
            true
        } catch (e: Exception) {
            Log.e(TAG, "关闭系统省电模式失败: ${e.message}")
            false
        }
    }
    
    suspend fun isEnabled(): Boolean {
        return try {
            val result = RootCommander.exec("settings get global low_power")
            result.out.firstOrNull()?.trim() == "1"
        } catch (e: Exception) {
            false
        }
    }
}