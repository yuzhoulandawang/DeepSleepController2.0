package com.example.deepsleep.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.model.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLogs: () -> Unit,
    onNavigateToWhitelist: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "深度睡眠控制器",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 统计数据卡片
            StatisticsCard(statistics)
            
            // 深度睡眠配置
            DeepSleepSection(settings, viewModel)
            
            // 进程压制配置
            ProcessSuppressSection(settings, viewModel)
            
            // 后台优化配置
            BackgroundOptimizationSection(settings, viewModel)
            
            // CPU 调度优化配置
            CpuOptimizationSection(settings, viewModel)
            
            // 其他配置
            OtherSettingsSection(settings, viewModel)
            
            // 操作按钮
            ActionButtons(onNavigateToLogs, onNavigateToWhitelist, viewModel)
        }
    }
}

@Composable
fun StatisticsCard(statistics: Triple<Int, Int, Int>) {
    val (enterCount, successRate, fixRate) = statistics
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 统计数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("进入次数", enterCount.toString())
                StatItem("成功率", "$successRate%")
                StatItem("修复率", "$fixRate%")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DeepSleepSection(settings: AppSettings, viewModel: MainViewModel) {
    SectionCard(
        title = "🌙 深度睡眠",
        icon = Icons.Default.NightsStay
    ) {
        SwitchItem(
            title = "启用深度睡眠",
            subtitle = "屏幕熄灭时自动进入深度睡眠模式",
            checked = settings.deepSleepEnabled,
            onCheckedChange = { viewModel.setDeepSleepEnabled(it) }
        )
    }
}

@Composable
fun ProcessSuppressSection(settings: AppSettings, viewModel: MainViewModel) {
    var showDebounceDialog by remember { mutableStateOf(false) }
    var showSuppressIntervalDialog by remember { mutableStateOf(false) }
    var showOomDialog by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "⚡ 进程压制",
        icon = Icons.Default.Memory
    ) {
        SwitchItem(
            title = "启用进程压制",
            subtitle = "降低后台进程优先级以释放内存",
            checked = settings.suppressEnabled,
            onCheckedChange = { viewModel.setSuppressEnabled(it) }
        )
        
        if (settings.suppressEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 防抖间隔
            ClickableItem(
                title = "防抖间隔",
                subtitle = "${settings.debounceInterval} 秒",
                icon = Icons.Default.Timer
            ) {
                showDebounceDialog = true
            }
            
            // 压制间隔
            ClickableItem(
                title = "压制间隔",
                subtitle = "${settings.suppressInterval} 秒",
                icon = Icons.Default.Schedule
            ) {
                showSuppressIntervalDialog = true
            }
            
            // 压制模式
            SelectableItem(
                title = "压制模式",
                options = listOf("保守" to "conservative", "激进" to "aggressive"),
                selected = settings.suppressMode,
                onSelect = { viewModel.setSuppressMode(it) }
            )
            
            // OOM 值
            ClickableItem(
                title = "OOM 值",
                subtitle = settings.suppressOomValue.toString(),
                icon = Icons.Default.ShowChart
            ) {
                showOomDialog = true
            }
        }
        
        // 数值输入对话框
        if (showDebounceDialog) {
            NumberInputDialog(
                title = "设置防抖间隔",
                value = settings.debounceInterval,
                range = 1..30,
                unit = "秒",
                onConfirm = { viewModel.setDebounceInterval(it) },
                onDismiss = { showDebounceDialog = false }
            )
        }
        
        if (showSuppressIntervalDialog) {
            NumberInputDialog(
                title = "设置压制间隔",
                value = settings.suppressInterval,
                range = 10..600,
                unit = "秒",
                onConfirm = { viewModel.setSuppressInterval(it) },
                onDismiss = { showSuppressIntervalDialog = false }
            )
        }
        
        if (showOomDialog) {
            NumberInputDialog(
                title = "设置 OOM 值",
                value = settings.suppressOomValue,
                range = -1000..1000,
                unit = "",
                onConfirm = { viewModel.setSuppressOomValue(it) },
                onDismiss = { showOomDialog = false }
            )
        }
    }
}

@Composable
fun BackgroundOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
    SectionCard(
        title = "🔋 后台优化",
        icon = Icons.Default.BatterySaver
    ) {
        SwitchItem(
            title = "启用后台优化",
            subtitle = "禁用第三方应用后台运行",
            checked = settings.backgroundOptimizationEnabled,
            onCheckedChange = { viewModel.setBackgroundOptimizationEnabled(it) }
        )
    }
}

@Composable
fun CpuOptimizationSection(settings: AppSettings, viewModel: MainViewModel) {
    SectionCard(
        title = "🚀 CPU 调度优化",
        icon = Icons.Default.Speed
    ) {
        SwitchItem(
            title = "启用 CPU 调度优化",
            subtitle = "优化 CPU 频率和调度策略",
            checked = settings.cpuOptimizationEnabled,
            onCheckedChange = { viewModel.setCpuOptimizationEnabled(it) }
        )
        
        if (settings.cpuOptimizationEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 自动切换模式开关
            SwitchItem(
                title = "自动切换模式",
                subtitle = "息屏时切换到待机模式，亮屏时切换到日常模式",
                checked = settings.autoCpuMode,
                onCheckedChange = { viewModel.setAutoCpuMode(it) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // CPU 模式选择
            SelectableItem(
                title = "CPU 模式（手动选择）",
                options = listOf(
                    "日常模式" to "daily",
                    "待机模式" to "standby",
                    "默认模式" to "default",
                    "性能模式" to "performance"
                ),
                selected = settings.cpuMode,
                onSelect = { viewModel.setCpuMode(it) },
                largeChips = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 模式说明
            ModeDescriptionCard(settings.cpuMode)
        }
    }
}

@Composable
fun ModeDescriptionCard(currentMode: String) {
    val (title, description, color) = when (currentMode) {
        "daily" -> Triple(
            "📱 日常模式",
            "平衡性能与功耗，适合日常使用。CPU 频率适中，响应迅速。",
            MaterialTheme.colorScheme.primaryContainer
        )
        "standby" -> Triple(
            "💤 待机模式",
            "极致省电，降低 CPU 频率，延长待机时间。适合息屏或不需要高性能的场景。",
            MaterialTheme.colorScheme.tertiaryContainer
        )
        "default" -> Triple(
            "⚙️ 默认模式",
            "恢复系统默认设置，不进行任何优化干预。",
            MaterialTheme.colorScheme.surfaceVariant
        )
        "performance" -> Triple(
            "🎮 性能模式",
            "最高性能，CPU 频率拉满，快速响应。适合游戏、视频编辑等高性能需求场景。",
            MaterialTheme.colorScheme.errorContainer
        )
        else -> Triple(
            "❓ 未知模式",
            "当前模式配置异常，建议重新选择。",
            MaterialTheme.colorScheme.surfaceVariant
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun OtherSettingsSection(settings: AppSettings, viewModel: MainViewModel) {
    SectionCard(
        title = "⚙️ 其他设置",
        icon = Icons.Default.Settings
    ) {
        SwitchItem(
            title = "开机自启动",
            subtitle = "系统启动时自动运行服务",
            checked = settings.bootStartEnabled,
            onCheckedChange = { viewModel.setBootStartEnabled(it) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SwitchItem(
            title = "启用通知",
            subtitle = "显示优化状态通知",
            checked = settings.notificationsEnabled,
            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
        )
    }
}

@Composable
fun ActionButtons(
    onNavigateToLogs: () -> Unit,
    onNavigateToWhitelist: () -> Unit,
    viewModel: MainViewModel
) {
    SectionCard(
        title = "📝 操作",
        icon = Icons.Default.List
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLogs
            ) {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("查看日志", fontSize = 13.sp)
            }
            
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.clearLogs() }
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("清除日志", fontSize = 13.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToWhitelist
        ) {
            Icon(Icons.Default.ViewList, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看白名单", fontSize = 14.sp)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun ClickableItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SelectableItem(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    largeChips: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (largeChips) {
            // 大芯片模式（4个选项）
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (label, value) ->
                    LargeOptionChip(
                        label = label,
                        selected = selected == value,
                        onClick = { onSelect(value) }
                    )
                }
            }
        } else {
            // 小芯片模式（2个选项）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (label, value) ->
                    FilterChip(
                        selected = selected == value,
                        onClick = { onSelect(value) },
                        label = { Text(label, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun LargeOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (selected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NumberInputDialog(
    title: String,
    value: Int,
    range: IntRange,
    unit: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(value.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { 
                        inputValue = it
                        error = when {
                            it.isEmpty() -> null
                            it.toIntOrNull() == null -> "请输入有效的数字"
                            it.toIntOrNull() !in range -> "请输入 ${range.first} 到 ${range.last} 之间的数字"
                            else -> null
                        }
                    },
                    label = { Text("数值") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { 
                        Text(
                            text = error ?: "范围: ${range.first} - ${range.last}$unit",
                            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    inputValue.toIntOrNull()?.let { 
                        if (it in range) {
                            onConfirm(it)
                            onDismiss()
                        }
                    }
                },
                enabled = inputValue.isNotEmpty() && error == null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
