package com.example.deepsleep.model

data class AppSettings(
    // ========== 深度 Doze 配置 ==========
    val deepDozeEnabled: Boolean = true,              // 是否启用深度 Doze（进入 Device Idle）
    val deepDozeDelaySeconds: Int = 5,               // 息屏后延迟进入深度 Doze 的时间（秒），范围：0-300秒
    val deepDozeForceMode: Boolean = false,          // 是否强制 Doze 模式（禁用 motion 检测）
    
    // ========== 深度睡眠配置（Hook 版本） ==========
    val deepSleepHookEnabled: Boolean = true,         // 是否启用深度睡眠 Hook
    val deepSleepDelaySeconds: Int = 1,              // 息屏后延迟进入深度睡眠的时间（秒），范围：0-300秒
    val deepSleepBlockExit: Boolean = true,          // 是否阻止自动退出深度睡眠（屏蔽移动、广播等触发）
    val deepSleepCheckInterval: Int = 10,            // 状态检查间隔（秒），确保保持深度睡眠状态
    
    // ========== 系统省电模式联动 ==========
    val enablePowerSaverOnSleep: Boolean = false,    // 进入深度睡眠时是否自动开启系统省电模式
    val disablePowerSaverOnWake: Boolean = true,     // 退出深度睡眠时是否自动关闭系统省电模式
    
    // ========== CPU 调度优化配置 ==========
    val cpuOptimizationEnabled: Boolean = false,     // 是否启用 CPU 调度优化
    
    // CPU 模式选择
    val cpuModeOnScreen: String = "daily",           // 亮屏时的 CPU 模式：daily/standby/default/performance
    val cpuModeOnScreenOff: String = "standby",      // 息屏时的 CPU 模式：daily/standby/default/performance
    val autoSwitchCpuMode: Boolean = true,           // 是否自动切换 CPU 模式（亮屏/息屏）
    val allowManualCpuMode: Boolean = true,          // 是否允许手动切换 CPU 模式（亮屏时）
    
    // CPU 参数 - 日常模式（可自定义）
    val dailyUpRateLimit: Int = 1000,                // up_rate_limit_us（微秒）
    val dailyDownRateLimit: Int = 500,               // down_rate_limit_us（微秒）
    val dailyHiSpeedLoad: Int = 85,                  // hispeed_load（百分比）
    val dailyTargetLoads: Int = 80,                  // target_loads（百分比）
    
    // CPU 参数 - 待机模式（可自定义）
    val standbyUpRateLimit: Int = 5000,
    val standbyDownRateLimit: Int = 0,
    val standbyHiSpeedLoad: Int = 95,
    val standbyTargetLoads: Int = 90,
    
    // CPU 参数 - 默认模式（可自定义）
    val defaultUpRateLimit: Int = 0,
    val defaultDownRateLimit: Int = 0,
    val defaultHiSpeedLoad: Int = 90,
    val defaultTargetLoads: Int = 90,
    
    // CPU 参数 - 性能模式（可自定义）
    val perfUpRateLimit: Int = 0,
    val perfDownRateLimit: Int = 0,
    val perfHiSpeedLoad: Int = 75,
    val perfTargetLoads: Int = 70,
    
    // ========== 进程压制配置 ==========
    val suppressEnabled: Boolean = true,
    val suppressMode: String = "conservative",       // conservative/aggressive
    val suppressOomValue: Int = 800,                 // OOM 值（可调整，建议范围：100-1500）
    val suppressInterval: Int = 60,                  // 压制间隔（秒）
    val debounceInterval: Int = 3,                   // 防抖间隔（秒）
    
    // ========== 后台优化配置 ==========
    val backgroundOptimizationEnabled: Boolean = true,
    
    // ========== 其他配置 ==========
    val bootStartEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    
    // ========== 兼容旧版本 ==========
    val deepSleepEnabled: Boolean = true,            // 兼容旧版本，等同于 deepSleepHookEnabled
    val cpuMode: String = "daily",                   // 兼容旧版本，等同于 cpuModeOnScreen
    val autoCpuMode: Boolean = true                  // 兼容旧版本，等同于 autoSwitchCpuMode
)
