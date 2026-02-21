package com.example.deepsleep.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.data.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(repository)
    )
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== 深度 Doze 配置 ==========
            SettingsSection(title = "深度 Doze 配置") {
                SwitchSetting(
                    title = "启用深度 Doze",
                    subtitle = "息屏后自动进入 Device Idle 模式",
                    checked = settings.deepDozeEnabled,
                    onCheckedChange = { viewModel.setDeepDozeEnabled(it) }
                )
                
                if (settings.deepDozeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderSetting(
                        title = "延迟进入时间",
                        value = settings.deepDozeDelaySeconds.toFloat(),
                        valueRange = 0f..300f,
                        onValueChange = { viewModel.setDeepDozeDelaySeconds(it.toInt()) },
                        label = "${settings.deepDozeDelaySeconds} 秒"
                    )
                    
                    SwitchSetting(
                        title = "强制 Doze 模式",
                        subtitle = "禁用 motion 检测，强制进入 Doze",
                        checked = settings.deepDozeForceMode,
                        onCheckedChange = { viewModel.setDeepDozeForceMode(it) }
                    )
                }
            }
            
            // ========== 深度睡眠配置（Hook 版本） ==========
            SettingsSection(title = "深度睡眠（Hook 版本）") {
                SwitchSetting(
                    title = "启用深度睡眠 Hook",
                    subtitle = "息屏后强制进入深度休眠，屏蔽自动退出",
                    checked = settings.deepSleepHookEnabled,
                    onCheckedChange = { viewModel.setDeepSleepHookEnabled(it) }
                )
                
                if (settings.deepSleepHookEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderSetting(
                        title = "延迟进入时间",
                        value = settings.deepSleepDelaySeconds.toFloat(),
                        valueRange = 0f..300f,
                        onValueChange = { viewModel.setDeepSleepDelaySeconds(it.toInt()) },
                        label = "${settings.deepSleepDelaySeconds} 秒"
                    )
                    
                    SwitchSetting(
                        title = "阻止自动退出",
                        subtitle = "屏蔽移动、广播等自动退出条件",
                        checked = settings.deepSleepBlockExit,
                        onCheckedChange = { viewModel.setDeepSleepBlockExit(it) }
                    )
                    
                    SliderSetting(
                        title = "状态检查间隔",
                        value = settings.deepSleepCheckInterval.toFloat(),
                        valueRange = 5f..60f,
                        onValueChange = { viewModel.setDeepSleepCheckInterval(it.toInt()) },
                        label = "${settings.deepSleepCheckInterval} 秒"
                    )
                }
            }
            
            // ========== 系统省电模式联动 ==========
            SettingsSection(title = "系统省电模式") {
                SwitchSetting(
                    title = "睡眠时开启省电模式",
                    subtitle = "进入深度睡眠时自动开启系统省电",
                    checked = settings.enablePowerSaverOnSleep,
                    onCheckedChange = { viewModel.setEnablePowerSaverOnSleep(it) }
                )
                
                SwitchSetting(
                    title = "唤醒时关闭省电模式",
                    subtitle = "退出深度睡眠时自动关闭系统省电",
                    checked = settings.disablePowerSaverOnWake,
                    onCheckedChange = { viewModel.setDisablePowerSaverOnWake(it) }
                )
            }
            
            // ========== CPU 调度优化配置 ==========
            SettingsSection(title = "CPU 调度优化") {
                SwitchSetting(
                    title = "启用 CPU 调度优化",
                    subtitle = "优化 WALT 调度器参数",
                    checked = settings.cpuOptimizationEnabled,
                    onCheckedChange = { viewModel.setCpuOptimizationEnabled(it) }
                )
                
                if (settings.cpuOptimizationEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SwitchSetting(
                        title = "自动切换 CPU 模式",
                        subtitle = "亮屏/息屏时自动切换模式",
                        checked = settings.autoSwitchCpuMode,
                        onCheckedChange = { viewModel.setAutoSwitchCpuMode(it) }
                    )
                    
                    if (settings.autoSwitchCpuMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "亮屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CpuModeChip(
                                mode = "daily",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("daily") }
                            )
                            CpuModeChip(
                                mode = "standby",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("standby") }
                            )
                            CpuModeChip(
                                mode = "default",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("default") }
                            )
                            CpuModeChip(
                                mode = "performance",
                                currentMode = settings.cpuModeOnScreen,
                                onClick = { viewModel.setCpuModeOnScreen("performance") }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "息屏模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CpuModeChip(
                                mode = "daily",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("daily") }
                            )
                            CpuModeChip(
                                mode = "standby",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("standby") }
                            )
                            CpuModeChip(
                                mode = "default",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("default") }
                            )
                            CpuModeChip(
                                mode = "performance",
                                currentMode = settings.cpuModeOnScreenOff,
                                onClick = { viewModel.setCpuModeOnScreenOff("performance") }
                            )
                        }
                    } else {
                        SwitchSetting(
                            title = "允许手动切换模式",
                            subtitle = "亮屏时可在主界面手动切换 CPU 模式",
                            checked = settings.allowManualCpuMode,
                            onCheckedChange = { viewModel.setAllowManualCpuMode(it) }
                        )
                    }
                }
            }
            
            // ========== 进程压制配置 ==========
            SettingsSection(title = "进程压制") {
                SwitchSetting(
                    title = "启用进程压制",
                    checked = settings.suppressEnabled,
                    onCheckedChange = { viewModel.setSuppressEnabled(it) }
                )
                
                if (settings.suppressEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "压制模式",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.suppressMode == "conservative",
                            onClick = { viewModel.setSuppressMode("conservative") },
                            label = { Text("保守") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.suppressMode == "aggressive",
                            onClick = { viewModel.setSuppressMode("aggressive") },
                            label = { Text("激进") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderSetting(
                        title = "OOM 压制值",
                        value = settings.suppressOomValue.toFloat(),
                        valueRange = 100f..1500f,
                        onValueChange = { viewModel.setSuppressOomValue(it.toInt()) },
                        label = "${settings.suppressOomValue}"
                    )
                    
                    SliderSetting(
                        title = "压制间隔",
                        value = settings.suppressInterval.toFloat(),
                        valueRange = 30f..600f,
                        onValueChange = { viewModel.setSuppressInterval(it.toInt()) },
                        label = "${settings.suppressInterval} 秒"
                    )
                }
            }
            
            // ========== 后台优化配置 ==========
            SettingsSection(title = "后台优化") {
                SwitchSetting(
                    title = "启用后台优化",
                    subtitle = "限制 RUN_ANY_IN_BACKGROUND 和 WAKE_LOCK",
                    checked = settings.backgroundOptimizationEnabled,
                    onCheckedChange = { viewModel.setBackgroundOptimizationEnabled(it) }
                )
            }
            
            // ========== 其他配置 ==========
            SettingsSection(title = "其他") {
                SwitchSetting(
                    title = "开机自动启动服务",
                    checked = settings.bootStartEnabled,
                    onCheckedChange = { viewModel.setBootStartEnabled(it) }
                )
                
                SwitchSetting(
                    title = "显示通知",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }
            
            // ========== 高级设置入口 ==========
            SettingsSection(title = "高级设置") {
                OutlinedButton(
                    onClick = onNavigateToAdvancedSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CPU 参数详细配置")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    label: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun CpuModeChip(
    mode: String,
    currentMode: String,
    onClick: () -> Unit
) {
    val label = when (mode) {
        "daily" -> "日常"
        "standby" -> "待机"
        "default" -> "默认"
        "performance" -> "性能"
        else -> mode
    }
    
    FilterChip(
        selected = currentMode == mode,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) }
    )
}
