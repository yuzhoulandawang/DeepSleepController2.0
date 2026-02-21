package com.example.deepsleep.ui.main

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.AppSettings
import com.example.deepsleep.model.LogLevel
import com.example.deepsleep.model.Statistics
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val hasRoot: Boolean = false,
    val isRefreshing: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val settingsRepository = SettingsRepository(context)
    private val logRepository = LogRepository()
    private val statsRepository = StatsRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())

    // 统计信息：使用 StateFlow 定期刷新
    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    init {
        setupNotificationChannel()
        viewModelScope.launch {
            refreshRootStatus()
        }
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                refreshRootStatus()
            }
        }
        // 定期刷新统计
        viewModelScope.launch {
            while (true) {
                _statistics.value = statsRepository.loadStats()
                delay(30_000)
            }
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "deep_sleep_channel",
                "深度睡眠控制",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "深度睡眠优化服务通知"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun refreshRootStatus() {
        _uiState.update { it.copy(isRefreshing = true) }
        val hasRoot = RootCommander.checkRoot()
        _uiState.update { it.copy(hasRoot = hasRoot, isRefreshing = false) }
    }

    // ========== 设置方法 ==========
    fun setDeepSleepEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度睡眠已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDeepDozeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度 Doze 已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDeepDozeDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeDelaySeconds(seconds)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度 Doze 延迟时间已设置为: $seconds 秒")
        }
    }

    fun setDeepDozeForceMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepDozeForceMode(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度 Doze 强制模式已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDeepSleepHookEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepHookEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度睡眠 Hook 已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDeepSleepDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepDelaySeconds(seconds)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度睡眠延迟时间已设置为: $seconds 秒")
        }
    }

    fun setDeepSleepBlockExit(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepBlockExit(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度睡眠阻止自动退出已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDeepSleepCheckInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setDeepSleepCheckInterval(interval)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "深度睡眠状态检查间隔已设置为: $interval 秒")
        }
    }

    fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnablePowerSaverOnSleep(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "进入深度睡眠时开启省电模式已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDisablePowerSaverOnWake(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDisablePowerSaverOnWake(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "退出深度睡眠时关闭省电模式已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setCpuOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCpuOptimizationEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "CPU 调度优化已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setCpuModeOnScreen(mode: String) {
        viewModelScope.launch {
            settingsRepository.setCpuModeOnScreen(mode)
            val modeName = getCpuModeName(mode)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "亮屏 CPU 模式已设置为: $modeName")
        }
    }

    fun setCpuModeOnScreenOff(mode: String) {
        viewModelScope.launch {
            settingsRepository.setCpuModeOnScreenOff(mode)
            val modeName = getCpuModeName(mode)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "息屏 CPU 模式已设置为: $modeName")
        }
    }

    fun setAutoSwitchCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSwitchCpuMode(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "自动切换 CPU 模式已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setAllowManualCpuMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowManualCpuMode(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "手动切换 CPU 模式已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setDailyUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyUpRateLimit(value)
        }
    }

    fun setDailyDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyDownRateLimit(value)
        }
    }

    fun setDailyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyHiSpeedLoad(value)
        }
    }

    fun setDailyTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyTargetLoads(value)
        }
    }

    fun setStandbyUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyUpRateLimit(value)
        }
    }

    fun setStandbyDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyDownRateLimit(value)
        }
    }

    fun setStandbyHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyHiSpeedLoad(value)
        }
    }

    fun setStandbyTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setStandbyTargetLoads(value)
        }
    }

    fun setDefaultUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultUpRateLimit(value)
        }
    }

    fun setDefaultDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultDownRateLimit(value)
        }
    }

    fun setDefaultHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultHiSpeedLoad(value)
        }
    }

    fun setDefaultTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultTargetLoads(value)
        }
    }

    fun setPerfUpRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfUpRateLimit(value)
        }
    }

    fun setPerfDownRateLimit(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfDownRateLimit(value)
        }
    }

    fun setPerfHiSpeedLoad(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfHiSpeedLoad(value)
        }
    }

    fun setPerfTargetLoads(value: Int) {
        viewModelScope.launch {
            settingsRepository.setPerfTargetLoads(value)
        }
    }

    fun setSuppressEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSuppressEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "进程压制已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setSuppressMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setSuppressMode(mode)
            val modeName = when (mode) {
                "conservative" -> "保守"
                "aggressive" -> "激进"
                else -> mode
            }
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "压制模式已设置为: $modeName")
        }
    }

    fun setDebounceInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setDebounceInterval(interval)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "防抖间隔已设置为: $interval 秒")
        }
    }

    fun setSuppressInterval(interval: Int) {
        viewModelScope.launch {
            settingsRepository.setSuppressInterval(interval)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "压制间隔已设置为: $interval 秒")
        }
    }

    fun setSuppressOomValue(value: Int) {
        viewModelScope.launch {
            settingsRepository.setSuppressOomValue(value)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "OOM 值已设置为: $value")
        }
    }

    fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundOptimizationEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "后台优化已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setBootStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBootStartEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "开机自启动已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "通知已${if (enabled) "启用" else "禁用"}")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            logRepository.clearLogs()
            logRepository.appendLog(LogLevel.INFO, "MainViewModel", "日志已清除")
        }
    }

    private fun getCpuModeName(mode: String): String {
        return when (mode) {
            "daily" -> "日常模式"
            "standby" -> "待机模式"
            "default" -> "默认模式"
            "performance" -> "性能模式"
            else -> mode
        }
    }
}