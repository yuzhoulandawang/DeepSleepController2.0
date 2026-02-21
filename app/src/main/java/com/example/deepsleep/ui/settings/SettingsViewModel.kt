package com.example.deepsleep.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    // ========== 深度 Doze 配置 ==========
    fun setDeepDozeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDeepDozeEnabled(enabled)
        }
    }
    
    fun setDeepDozeDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            repository.setDeepDozeDelaySeconds(seconds)
        }
    }
    
    fun setDeepDozeForceMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDeepDozeForceMode(enabled)
        }
    }
    
    // ========== 深度睡眠配置（Hook 版本） ==========
    fun setDeepSleepHookEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDeepSleepHookEnabled(enabled)
        }
    }
    
    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            repository.setDeepSleepDelaySeconds(seconds)
        }
    }
    
    fun setDeepSleepBlockExit(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDeepSleepBlockExit(enabled)
        }
    }
    
    fun setDeepSleepCheckInterval(seconds: Int) {
        viewModelScope.launch {
            repository.setDeepSleepCheckInterval(seconds)
        }
    }
    
    // ========== 系统省电模式联动 ==========
    fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnablePowerSaverOnSleep(enabled)
        }
    }
    
    fun setDisablePowerSaverOnWake(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDisablePowerSaverOnWake(enabled)
        }
    }
    
    // ========== CPU 调度优化配置 ==========
    fun setCpuOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setCpuOptimizationEnabled(enabled)
        }
    }
    
    fun setCpuModeOnScreen(mode: String) {
        viewModelScope.launch {
            repository.setCpuModeOnScreen(mode)
        }
    }
    
    fun setCpuModeOnScreenOff(mode: String) {
        viewModelScope.launch {
            repository.setCpuModeOnScreenOff(mode)
        }
    }
    
    fun setAutoSwitchCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoSwitchCpuMode(enabled)
        }
    }
    
    fun setAllowManualCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAllowManualCpuMode(enabled)
        }
    }
    
    // ========== CPU 参数 - 日常模式 ==========
    fun setDailyUpRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setDailyUpRateLimit(value)
        }
    }
    
    fun setDailyDownRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setDailyDownRateLimit(value)
        }
    }
    
    fun setDailyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            repository.setDailyHiSpeedLoad(value)
        }
    }
    
    fun setDailyTargetLoads(value: Int) {
        viewModelScope.launch {
            repository.setDailyTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 待机模式 ==========
    fun setStandbyUpRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setStandbyUpRateLimit(value)
        }
    }
    
    fun setStandbyDownRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setStandbyDownRateLimit(value)
        }
    }
    
    fun setStandbyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            repository.setStandbyHiSpeedLoad(value)
        }
    }
    
    fun setStandbyTargetLoads(value: Int) {
        viewModelScope.launch {
            repository.setStandbyTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 默认模式 ==========
    fun setDefaultUpRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setDefaultUpRateLimit(value)
        }
    }
    
    fun setDefaultDownRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setDefaultDownRateLimit(value)
        }
    }
    
    fun setDefaultHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            repository.setDefaultHiSpeedLoad(value)
        }
    }
    
    fun setDefaultTargetLoads(value: Int) {
        viewModelScope.launch {
            repository.setDefaultTargetLoads(value)
        }
    }
    
    // ========== CPU 参数 - 性能模式 ==========
    fun setPerfUpRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setPerfUpRateLimit(value)
        }
    }
    
    fun setPerfDownRateLimit(value: Int) {
        viewModelScope.launch {
            repository.setPerfDownRateLimit(value)
        }
    }
    
    fun setPerfHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            repository.setPerfHiSpeedLoad(value)
        }
    }
    
    fun setPerfTargetLoads(value: Int) {
        viewModelScope.launch {
            repository.setPerfTargetLoads(value)
        }
    }
    
    // ========== 进程压制配置 ==========
    fun setSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSuppressEnabled(enabled)
        }
    }
    
    fun setSuppressMode(mode: String) {
        viewModelScope.launch {
            repository.setSuppressMode(mode)
        }
    }
    
    fun setSuppressOomValue(value: Int) {
        viewModelScope.launch {
            repository.setSuppressOomValue(value)
        }
    }
    
    fun setSuppressInterval(seconds: Int) {
        viewModelScope.launch {
            repository.setSuppressInterval(seconds)
        }
    }
    
    fun setDebounceInterval(seconds: Int) {
        viewModelScope.launch {
            repository.setDebounceInterval(seconds)
        }
    }
    
    // ========== 后台优化配置 ==========
    fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBackgroundOptimizationEnabled(enabled)
        }
    }
    
    // ========== 其他配置 ==========
    fun setBootStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBootStartEnabled(enabled)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }
    
    companion object {
        fun provideFactory(repository: SettingsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SettingsViewModel(repository) }
            }
    }
}
