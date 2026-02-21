package com.example.deepsleep.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.deepsleep.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        // ========== 深度 Doze 配置 ==========
        val DEEP_DOZE_ENABLED = booleanPreferencesKey("deep_doze_enabled")
        val DEEP_DOZE_DELAY_SECONDS = intPreferencesKey("deep_doze_delay_seconds")
        val DEEP_DOZE_FORCE_MODE = booleanPreferencesKey("deep_doze_force_mode")

        // ========== 深度睡眠 Hook 配置 ==========
        val DEEP_SLEEP_HOOK_ENABLED = booleanPreferencesKey("deep_sleep_hook_enabled")
        val DEEP_SLEEP_DELAY_SECONDS = intPreferencesKey("deep_sleep_delay_seconds")
        val DEEP_SLEEP_BLOCK_EXIT = booleanPreferencesKey("deep_sleep_block_exit")
        val DEEP_SLEEP_CHECK_INTERVAL = intPreferencesKey("deep_sleep_check_interval")

        // ========== 系统省电模式联动 ==========
        val ENABLE_POWER_SAVER_ON_SLEEP = booleanPreferencesKey("enable_power_saver_on_sleep")
        val DISABLE_POWER_SAVER_ON_WAKE = booleanPreferencesKey("disable_power_saver_on_wake")

        // ========== CPU 调度优化配置 ==========
        val CPU_OPT_ENABLED = booleanPreferencesKey("cpu_opt_enabled")
        val CPU_MODE_ON_SCREEN = stringPreferencesKey("cpu_mode_on_screen")
        val CPU_MODE_ON_SCREEN_OFF = stringPreferencesKey("cpu_mode_on_screen_off")
        val AUTO_SWITCH_CPU_MODE = booleanPreferencesKey("auto_switch_cpu_mode")
        val ALLOW_MANUAL_CPU_MODE = booleanPreferencesKey("allow_manual_cpu_mode")

        // CPU 参数 - 日常模式
        val DAILY_UP_RATE_LIMIT = intPreferencesKey("daily_up_rate_limit")
        val DAILY_DOWN_RATE_LIMIT = intPreferencesKey("daily_down_rate_limit")
        val DAILY_HI_SPEED_LOAD = intPreferencesKey("daily_hi_speed_load")
        val DAILY_TARGET_LOADS = intPreferencesKey("daily_target_loads")

        // CPU 参数 - 待机模式
        val STANDBY_UP_RATE_LIMIT = intPreferencesKey("standby_up_rate_limit")
        val STANDBY_DOWN_RATE_LIMIT = intPreferencesKey("standby_down_rate_limit")
        val STANDBY_HI_SPEED_LOAD = intPreferencesKey("standby_hi_speed_load")
        val STANDBY_TARGET_LOADS = intPreferencesKey("standby_target_loads")

        // CPU 参数 - 默认模式
        val DEFAULT_UP_RATE_LIMIT = intPreferencesKey("default_up_rate_limit")
        val DEFAULT_DOWN_RATE_LIMIT = intPreferencesKey("default_down_rate_limit")
        val DEFAULT_HI_SPEED_LOAD = intPreferencesKey("default_hi_speed_load")
        val DEFAULT_TARGET_LOADS = intPreferencesKey("default_target_loads")

        // CPU 参数 - 性能模式
        val PERF_UP_RATE_LIMIT = intPreferencesKey("perf_up_rate_limit")
        val PERF_DOWN_RATE_LIMIT = intPreferencesKey("perf_down_rate_limit")
        val PERF_HI_SPEED_LOAD = intPreferencesKey("perf_hi_speed_load")
        val PERF_TARGET_LOADS = intPreferencesKey("perf_target_loads")

        // ========== 进程压制配置 ==========
        val SUPPRESS_ENABLED = booleanPreferencesKey("suppress_enabled")
        val SUPPRESS_MODE = stringPreferencesKey("suppress_mode")
        val SUPPRESS_OOM_VALUE = intPreferencesKey("suppress_oom_value")
        val SUPPRESS_INTERVAL = intPreferencesKey("suppress_interval")
        val DEBOUNCE_INTERVAL = intPreferencesKey("debounce_interval")

        // ========== 后台优化配置 ==========
        val BG_OPT_ENABLED = booleanPreferencesKey("bg_opt_enabled")

        // ========== 其他配置 ==========
        val BOOT_START_ENABLED = booleanPreferencesKey("boot_start_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        // ========== 兼容旧版本 ==========
        val DEEP_SLEEP_ENABLED = booleanPreferencesKey("deep_sleep_enabled")
        val CPU_MODE = stringPreferencesKey("cpu_mode")
        val AUTO_CPU_MODE = booleanPreferencesKey("auto_cpu_mode")

        // ========== 新增用于保存 motion 备份 ==========
        val MOTION_BACKUP = stringPreferencesKey("motion_backup")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            // 深度 Doze
            deepDozeEnabled = preferences[PreferencesKeys.DEEP_DOZE_ENABLED] ?: true,
            deepDozeDelaySeconds = preferences[PreferencesKeys.DEEP_DOZE_DELAY_SECONDS] ?: 5,
            deepDozeForceMode = preferences[PreferencesKeys.DEEP_DOZE_FORCE_MODE] ?: false,

            // 深度睡眠 Hook
            deepSleepHookEnabled = preferences[PreferencesKeys.DEEP_SLEEP_HOOK_ENABLED] ?:
                preferences[PreferencesKeys.DEEP_SLEEP_ENABLED] ?: true,
            deepSleepDelaySeconds = preferences[PreferencesKeys.DEEP_SLEEP_DELAY_SECONDS] ?: 1,
            deepSleepBlockExit = preferences[PreferencesKeys.DEEP_SLEEP_BLOCK_EXIT] ?: true,
            deepSleepCheckInterval = preferences[PreferencesKeys.DEEP_SLEEP_CHECK_INTERVAL] ?: 10,

            // 系统省电模式联动
            enablePowerSaverOnSleep = preferences[PreferencesKeys.ENABLE_POWER_SAVER_ON_SLEEP] ?: false,
            disablePowerSaverOnWake = preferences[PreferencesKeys.DISABLE_POWER_SAVER_ON_WAKE] ?: true,

            // CPU 调度优化
            cpuOptimizationEnabled = preferences[PreferencesKeys.CPU_OPT_ENABLED] ?: false,
            cpuModeOnScreen = preferences[PreferencesKeys.CPU_MODE_ON_SCREEN] ?: "daily",
            cpuModeOnScreenOff = preferences[PreferencesKeys.CPU_MODE_ON_SCREEN_OFF] ?: "standby",
            autoSwitchCpuMode = preferences[PreferencesKeys.AUTO_SWITCH_CPU_MODE] ?:
                preferences[PreferencesKeys.AUTO_CPU_MODE] ?: true,
            allowManualCpuMode = preferences[PreferencesKeys.ALLOW_MANUAL_CPU_MODE] ?: true,

            // CPU 参数 - 日常模式
            dailyUpRateLimit = preferences[PreferencesKeys.DAILY_UP_RATE_LIMIT] ?: 1000,
            dailyDownRateLimit = preferences[PreferencesKeys.DAILY_DOWN_RATE_LIMIT] ?: 500,
            dailyHiSpeedLoad = preferences[PreferencesKeys.DAILY_HI_SPEED_LOAD] ?: 85,
            dailyTargetLoads = preferences[PreferencesKeys.DAILY_TARGET_LOADS] ?: 80,

            // CPU 参数 - 待机模式
            standbyUpRateLimit = preferences[PreferencesKeys.STANDBY_UP_RATE_LIMIT] ?: 5000,
            standbyDownRateLimit = preferences[PreferencesKeys.STANDBY_DOWN_RATE_LIMIT] ?: 0,
            standbyHiSpeedLoad = preferences[PreferencesKeys.STANDBY_HI_SPEED_LOAD] ?: 95,
            standbyTargetLoads = preferences[PreferencesKeys.STANDBY_TARGET_LOADS] ?: 90,

            // CPU 参数 - 默认模式
            defaultUpRateLimit = preferences[PreferencesKeys.DEFAULT_UP_RATE_LIMIT] ?: 0,
            defaultDownRateLimit = preferences[PreferencesKeys.DEFAULT_DOWN_RATE_LIMIT] ?: 0,
            defaultHiSpeedLoad = preferences[PreferencesKeys.DEFAULT_HI_SPEED_LOAD] ?: 90,
            defaultTargetLoads = preferences[PreferencesKeys.DEFAULT_TARGET_LOADS] ?: 90,

            // CPU 参数 - 性能模式
            perfUpRateLimit = preferences[PreferencesKeys.PERF_UP_RATE_LIMIT] ?: 0,
            perfDownRateLimit = preferences[PreferencesKeys.PERF_DOWN_RATE_LIMIT] ?: 0,
            perfHiSpeedLoad = preferences[PreferencesKeys.PERF_HI_SPEED_LOAD] ?: 75,
            perfTargetLoads = preferences[PreferencesKeys.PERF_TARGET_LOADS] ?: 70,

            // 进程压制
            suppressEnabled = preferences[PreferencesKeys.SUPPRESS_ENABLED] ?: true,
            suppressMode = preferences[PreferencesKeys.SUPPRESS_MODE] ?: "conservative",
            suppressOomValue = preferences[PreferencesKeys.SUPPRESS_OOM_VALUE] ?: 800,
            suppressInterval = preferences[PreferencesKeys.SUPPRESS_INTERVAL] ?: 60,
            debounceInterval = preferences[PreferencesKeys.DEBOUNCE_INTERVAL] ?: 3,

            // 后台优化
            backgroundOptimizationEnabled = preferences[PreferencesKeys.BG_OPT_ENABLED] ?: true,

            // 其他
            bootStartEnabled = preferences[PreferencesKeys.BOOT_START_ENABLED] ?: false,
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,

            // 兼容旧版本
            deepSleepEnabled = preferences[PreferencesKeys.DEEP_SLEEP_ENABLED] ?: true,
            cpuMode = preferences[PreferencesKeys.CPU_MODE] ?: "daily",
            autoCpuMode = preferences[PreferencesKeys.AUTO_CPU_MODE] ?: true
        )
    }

    suspend fun getSettings(): AppSettings = settings.first()

    // ========== 深度 Doze ==========
    suspend fun setDeepDozeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_DOZE_ENABLED] = enabled }
    }

    suspend fun setDeepDozeDelaySeconds(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_DOZE_DELAY_SECONDS] = seconds }
    }

    suspend fun setDeepDozeForceMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_DOZE_FORCE_MODE] = enabled }
    }

    // ========== 深度睡眠 Hook ==========
    suspend fun setDeepSleepHookEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.DEEP_SLEEP_HOOK_ENABLED] = enabled
            it[PreferencesKeys.DEEP_SLEEP_ENABLED] = enabled  // 兼容旧版本
        }
    }

    suspend fun setDeepSleepDelaySeconds(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_SLEEP_DELAY_SECONDS] = seconds }
    }

    suspend fun setDeepSleepBlockExit(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_SLEEP_BLOCK_EXIT] = enabled }
    }

    suspend fun setDeepSleepCheckInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEEP_SLEEP_CHECK_INTERVAL] = interval }
    }

    // ========== 系统省电模式联动 ==========
    suspend fun setEnablePowerSaverOnSleep(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ENABLE_POWER_SAVER_ON_SLEEP] = enabled }
    }

    suspend fun setDisablePowerSaverOnWake(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DISABLE_POWER_SAVER_ON_WAKE] = enabled }
    }

    // ========== CPU 调度优化 ==========
    suspend fun setCpuOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CPU_OPT_ENABLED] = enabled }
    }

    suspend fun setCpuModeOnScreen(mode: String) {
        context.dataStore.edit {
            it[PreferencesKeys.CPU_MODE_ON_SCREEN] = mode
            it[PreferencesKeys.CPU_MODE] = mode  // 兼容旧版本
        }
    }

    suspend fun setCpuModeOnScreenOff(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.CPU_MODE_ON_SCREEN_OFF] = mode }
    }

    // 新增：手动选择 CPU 模式
    suspend fun setCpuMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.CPU_MODE] = mode }
    }

    suspend fun setAutoSwitchCpuMode(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AUTO_SWITCH_CPU_MODE] = enabled
            it[PreferencesKeys.AUTO_CPU_MODE] = enabled  // 兼容旧版本
        }
    }

    suspend fun setAllowManualCpuMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ALLOW_MANUAL_CPU_MODE] = enabled }
    }

    // CPU 参数 - 日常模式
    suspend fun setDailyUpRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_UP_RATE_LIMIT] = value }
    }

    suspend fun setDailyDownRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setDailyHiSpeedLoad(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_HI_SPEED_LOAD] = value }
    }

    suspend fun setDailyTargetLoads(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_TARGET_LOADS] = value }
    }

    // CPU 参数 - 待机模式
    suspend fun setStandbyUpRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.STANDBY_UP_RATE_LIMIT] = value }
    }

    suspend fun setStandbyDownRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.STANDBY_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setStandbyHiSpeedLoad(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.STANDBY_HI_SPEED_LOAD] = value }
    }

    suspend fun setStandbyTargetLoads(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.STANDBY_TARGET_LOADS] = value }
    }

    // CPU 参数 - 默认模式
    suspend fun setDefaultUpRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_UP_RATE_LIMIT] = value }
    }

    suspend fun setDefaultDownRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setDefaultHiSpeedLoad(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_HI_SPEED_LOAD] = value }
    }

    suspend fun setDefaultTargetLoads(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_TARGET_LOADS] = value }
    }

    // CPU 参数 - 性能模式
    suspend fun setPerfUpRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.PERF_UP_RATE_LIMIT] = value }
    }

    suspend fun setPerfDownRateLimit(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.PERF_DOWN_RATE_LIMIT] = value }
    }

    suspend fun setPerfHiSpeedLoad(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.PERF_HI_SPEED_LOAD] = value }
    }

    suspend fun setPerfTargetLoads(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.PERF_TARGET_LOADS] = value }
    }

    // ========== 进程压制 ==========
    suspend fun setSuppressEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_ENABLED] = enabled }
    }

    suspend fun setSuppressMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_MODE] = mode }
    }

    suspend fun setSuppressOomValue(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_OOM_VALUE] = value }
    }

    suspend fun setSuppressInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.SUPPRESS_INTERVAL] = interval }
    }

    suspend fun setDebounceInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEBOUNCE_INTERVAL] = interval }
    }

    // ========== 后台优化 ==========
    suspend fun setBackgroundOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BG_OPT_ENABLED] = enabled }
    }

    // ========== 其他 ==========
    suspend fun setBootStartEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BOOT_START_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    // ========== 新增方法供 DeepSleepService 使用 ==========
    suspend fun saveMotionBackup(state: String) {
        context.dataStore.edit { it[PreferencesKeys.MOTION_BACKUP] = state }
    }

    suspend fun getBackgroundWhitelist(): List<String> {
        // 暂时返回空列表，如需实现可扩展
        return emptyList()
    }

    suspend fun getSuppressWhitelist(): List<String> {
        // 暂时返回空列表，如需实现可扩展
        return emptyList()
    }
}