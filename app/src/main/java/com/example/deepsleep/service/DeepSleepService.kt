package com.example.deepsleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.deepsleep.MainActivity
import com.example.deepsleep.R
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.model.DozeState
import com.example.deepsleep.model.LogLevel
import com.example.deepsleep.model.ScreenState
import com.example.deepsleep.root.BackgroundOptimizer
import com.example.deepsleep.root.DozeController
import com.example.deepsleep.root.ProcessSuppressor
import com.example.deepsleep.root.WaltOptimizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class DeepSleepService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private var suppressJob: Job? = null

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var statsRepo: StatsRepository
    private lateinit var logRepo: LogRepository

    private val _dozeState = MutableStateFlow(DozeState.UNKNOWN)

    private var lastScreenOffTime = 0L
    private var lastScreenOnTime = 0L
    private var lastSuppressTime = 0L
    private var forceModeActive = false
    private var serviceStartTime = 0L

    companion object {
        const val CHANNEL_ID = "deep_sleep_service"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START_SERVICE"
        const val ACTION_STOP = "STOP_SERVICE"

        var isRunning = false
            private set
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> handleScreenOff()
                Intent.ACTION_SCREEN_ON -> handleScreenOn()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)
        statsRepo = StatsRepository()
        logRepo = LogRepository()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopServiceInternal()
        }
        return START_STICKY
    }

    private fun startService() {
        isRunning = true
        serviceStartTime = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification("服务启动中..."))

        serviceScope.launch {
            log(LogLevel.INFO, "Service", "=== 深度睡眠服务启动 ===")

            val motionBackup = DozeController.backupMotionState()
            settingsRepo.saveMotionBackup(motionBackup)
            log(LogLevel.INFO, "Service", "已备份 motion 状态: $motionBackup")

            WaltOptimizer.applyGlobalOptimizations()
            log(LogLevel.INFO, "Service", "全局优化已应用")

            val settings = settingsRepo.getSettings()
            if (settings.cpuOptimizationEnabled) {
                applyCpuMode(settings.cpuMode)
                log(LogLevel.INFO, "Service", "初始 CPU 模式: ${getCpuModeName(settings.cpuMode)}")
            }

            if (settings.backgroundOptimizationEnabled) {
                log(LogLevel.INFO, "Service", "开始后台优化...")
                val whitelist = settingsRepo.getBackgroundWhitelist()
                BackgroundOptimizer.optimizeAll(this@DeepSleepService, whitelist)
                log(LogLevel.INFO, "Service", "后台优化完成")
            }

            val initialScreen = checkScreenState()
            if (initialScreen == ScreenState.OFF) {
                log(LogLevel.INFO, "Service", "启动时屏幕已关闭，进入强制模式")
                enterForceMode()
                DozeController.enterDeepSleep()

                if (settings.cpuOptimizationEnabled && settings.autoCpuMode) {
                    WaltOptimizer.applyStandby()
                    log(LogLevel.INFO, "Service", "息屏自动切换到待机模式")
                }
            } else {
                if (settings.cpuOptimizationEnabled) {
                    applyCpuMode(settings.cpuMode)
                }
            }

            startMainLoop()

            if (settings.suppressEnabled) {
                startSuppressLoop()
            }

            val stats = statsRepo.loadStats()
            statsRepo.saveStats(stats.copy(serviceStartTime = serviceStartTime))
        }
    }

    private fun startMainLoop() {
        monitorJob = serviceScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val screen = checkScreenState()
                val doze = DozeController.getState()

                _dozeState.value = doze

                if (forceModeActive && screen == ScreenState.OFF &&
                    doze != DozeState.IDLE && doze != DozeState.IDLE_MAINTENANCE) {

                    log(LogLevel.WARNING, "Service", "⚠️ 检测到自动退出，尝试重新进入")
                    statsRepo.recordAutoExit()

                    if (DozeController.enterDeepSleep()) {
                        statsRepo.recordAutoExitRecovered()
                        log(LogLevel.SUCCESS, "Service", "✅ 已重新进入深度睡眠")
                    }
                }

                updateNotificationStatus(screen, doze)

                if (doze == DozeState.IDLE_MAINTENANCE) {
                    statsRepo.recordMaintenance()
                }

                val delay = if (screen == ScreenState.ON) 15000L else 2000L
                delay(delay)
            }
        }
    }

    private fun startSuppressLoop() {
        suppressJob = serviceScope.launch {
            while (isActive) {
                val settings = settingsRepo.getSettings()
                val currentTime = System.currentTimeMillis()
                val interval = settings.suppressInterval * 1000L
                val minInterval = 10000L

                if (currentTime - lastSuppressTime >= minInterval) {
                    val screen = checkScreenState()
                    val shouldSuppress = when (settings.suppressMode) {
                        "aggressive" -> true
                        else -> screen == ScreenState.OFF
                    }

                    if (shouldSuppress) {
                        val whitelist = settingsRepo.getSuppressWhitelist()
                        ProcessSuppressor.suppress(settings.suppressOomValue, whitelist)
                        log(LogLevel.INFO, "Service", "进程压制已执行，OOM值: ${settings.suppressOomValue}")
                    }

                    lastSuppressTime = currentTime
                }

                delay(interval)
            }
        }
    }

    private fun handleScreenOff() {
        serviceScope.launch {
            val currentTime = System.currentTimeMillis()
            val debounce = settingsRepo.getSettings().debounceInterval * 1000

            if (currentTime - lastScreenOffTime < debounce) {
                log(LogLevel.INFO, "Service", "⏳ 息屏防抖，忽略")
                return@launch
            }

            lastScreenOffTime = currentTime
            log(LogLevel.INFO, "Service", "🌙 屏幕关闭")
            statsRepo.recordStateChange()

            enterForceMode()

            statsRepo.recordEnterAttempt()
            val success = DozeController.enterDeepSleep()
            if (success) {
                statsRepo.recordEnterSuccess()
                log(LogLevel.SUCCESS, "Service", "✅ 已进入深度睡眠")
            } else {
                log(LogLevel.ERROR, "Service", "❌ 进入深度睡眠失败")
            }

            val settings = settingsRepo.getSettings()
            if (settings.cpuOptimizationEnabled && settings.autoCpuMode) {
                WaltOptimizer.applyStandby()
                log(LogLevel.INFO, "Service", "息屏自动切换到待机模式")
            } else if (settings.cpuOptimizationEnabled) {
                log(LogLevel.INFO, "Service", "自动模式已禁用，保持当前 CPU 模式")
            }

            if (settings.suppressEnabled) {
                val settings = settingsRepo.getSettings()
                val whitelist = settingsRepo.getSuppressWhitelist()
                ProcessSuppressor.suppress(settings.suppressOomValue, whitelist)
                lastSuppressTime = currentTime
            }
        }
    }

    private fun handleScreenOn() {
        serviceScope.launch {
            val currentTime = System.currentTimeMillis()
            val debounce = settingsRepo.getSettings().debounceInterval * 1000

            if (currentTime - lastScreenOnTime < debounce) {
                log(LogLevel.INFO, "Service", "⏳ 亮屏防抖，忽略")
                return@launch
            }

            lastScreenOnTime = currentTime
            log(LogLevel.INFO, "Service", "☀️ 屏幕开启")
            statsRepo.recordStateChange()

            exitForceMode()

            statsRepo.recordExitAttempt()
            val success = DozeController.exitDeepSleep()
            if (success) {
                statsRepo.recordExitSuccess()
                log(LogLevel.SUCCESS, "Service", "✅ 已退出深度睡眠")
            } else {
                log(LogLevel.ERROR, "Service", "❌ 退出深度睡眠失败")
            }

            val settings = settingsRepo.getSettings()
            if (settings.cpuOptimizationEnabled && settings.autoCpuMode) {
                WaltOptimizer.applyDaily()
                log(LogLevel.INFO, "Service", "亮屏自动切换到日常模式")
            } else if (settings.cpuOptimizationEnabled) {
                applyCpuMode(settings.cpuMode)
                log(LogLevel.INFO, "Service", "应用手动选择的 CPU 模式: ${getCpuModeName(settings.cpuMode)}")
            }
        }
    }

    private suspend fun applyCpuMode(mode: String) {
        when (mode) {
            "daily" -> WaltOptimizer.applyDaily()
            "standby" -> WaltOptimizer.applyStandby()
            "default" -> WaltOptimizer.restoreDefault()
            "performance" -> WaltOptimizer.applyPerformance()
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

    private fun enterForceMode() {
        if (forceModeActive) return
        forceModeActive = true
        serviceScope.launch {
            DozeController.disableMotion()
            log(LogLevel.INFO, "Service", "🔧 强制模式已启用（motion 已禁用）")
        }
    }

    private fun exitForceMode() {
        if (!forceModeActive) return
        forceModeActive = false
        serviceScope.launch {
            DozeController.enableMotion()
            log(LogLevel.INFO, "Service", "🔓 强制模式已退出（motion 已启用）")
        }
    }

    private fun checkScreenState(): ScreenState {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (powerManager.isInteractive) ScreenState.ON else ScreenState.OFF
    }

    private fun stopServiceInternal() {
        isRunning = false
        monitorJob?.cancel()
        suppressJob?.cancel()

        serviceScope.launch {
            log(LogLevel.INFO, "Service", "=== 服务停止 ===")
            exitForceMode()
            DozeController.exitDeepSleep()

            BackgroundOptimizer.restoreAll()
            log(LogLevel.INFO, "Service", "后台优化已恢复")

            WaltOptimizer.restoreDefault()
            log(LogLevel.INFO, "Service", "WALT 参数已恢复")

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "深度睡眠服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持深度睡眠控制服务运行"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("深度睡眠控制器")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private suspend fun updateNotificationStatus(screen: ScreenState, doze: DozeState) {
        val screenText = if (screen == ScreenState.ON) "亮屏" else "息屏"
        val dozeText = when (doze) {
            DozeState.IDLE -> "深度睡眠"
            DozeState.IDLE_MAINTENANCE -> "维护窗口"
            DozeState.ACTIVE -> "活跃"
            else -> "其他"
        }

        val settings = settingsRepo.getSettings()
        val cpuModeText = if (settings.cpuOptimizationEnabled) {
            " | ${getCpuModeName(settings.cpuMode)}"
        } else {
            ""
        }

        val status = "$screenText | $dozeText$cpuModeText${if (forceModeActive) " [强制]" else ""}"
        val notification = createNotification(status)

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private suspend fun log(level: LogLevel, tag: String, message: String) {
        LogRepository().appendLog(level, tag, message)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        serviceScope.cancel()
    }
}