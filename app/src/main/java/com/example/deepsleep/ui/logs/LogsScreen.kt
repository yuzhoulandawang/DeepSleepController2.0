package com.example.deepsleep.ui.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.LogEntry
import com.example.deepsleep.model.LogLevel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LogsViewModel = viewModel()
    val logs by viewModel.logs.collectAsState()
    val filteredLogs by viewModel.filteredLogs.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportLogs(context) }) {
                        Icon(Icons.Default.Download, contentDescription = "导出")
                    }
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 日志级别筛选
            LevelFilter(
                selectedLevel = selectedLevel,
                onLevelSelected = { viewModel.setLevelFilter(it) }
            )
            
            // 日志列表
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLogs) { log ->
                        LogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun LevelFilter(
    selectedLevel: LogLevel?,
    onLevelSelected: (LogLevel?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    {
        // 全部
        FilterChip(
            selected = selectedLevel == null,
            onClick = { onLevelSelected(null) },
            label = { Text("全部") }
        )
        
        // DEBUG
        FilterChip(
            selected = selectedLevel == LogLevel.DEBUG,
            onClick = { onLevelSelected(LogLevel.DEBUG) },
            label = { Text("DEBUG") }
        )
        
        // INFO
        FilterChip(
            selected = selectedLevel == LogLevel.INFO,
            onClick = { onLevelSelected(LogLevel.INFO) },
            label = { Text("INFO") }
        )
        
        // SUCCESS
        FilterChip(
            selected = selectedLevel == LogLevel.SUCCESS,
            onClick = { onLevelSelected(LogLevel.SUCCESS) },
            label = { Text("SUCCESS") }
        )
        
        // WARNING
        FilterChip(
            selected = selectedLevel == LogLevel.WARNING,
            onClick = { onLevelSelected(LogLevel.WARNING) },
            label = { Text("WARNING") }
        )
        
        // ERROR
        FilterChip(
            selected = selectedLevel == LogLevel.ERROR,
            onClick = { onLevelSelected(LogLevel.ERROR) },
            label = { Text("ERROR") }
        )
        
        // FATAL
        FilterChip(
            selected = selectedLevel == LogLevel.FATAL,
            onClick = { onLevelSelected(LogLevel.FATAL) },
            label = { Text("FATAL") }
        )
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getLogLevelColor(log.level).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 时间和标签
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LogLevelBadge(log.level)
                }
                
                // 模块
                if (log.tag.isNotEmpty()) {
                    Text(
                        text = log.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            // 消息
            if (log.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // 异常信息
            if (log.throwable != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.throwable ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun LogLevelBadge(level: LogLevel) {
    val (text, color) = when (level) {
        LogLevel.DEBUG -> "DEBUG" to Color.Gray
        LogLevel.INFO -> "INFO" to Color.Blue
        LogLevel.SUCCESS -> "SUCCESS" to Color(0xFF4CAF50)
        LogLevel.WARNING -> "WARNING" to Color(0xFFFF9800)
        LogLevel.ERROR -> "ERROR" to Color.Red
        LogLevel.FATAL -> "FATAL" to Color(0xFFB71C1C)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

fun getLogLevelColor(level: LogLevel): Color {
    return when (level) {
        LogLevel.DEBUG -> Color.Gray
        LogLevel.INFO -> Color.Blue
        LogLevel.SUCCESS -> Color(0xFF4CAF50)
        LogLevel.WARNING -> Color(0xFFFF9800)
        LogLevel.ERROR -> Color.Red
        LogLevel.FATAL -> Color(0xFFB71C1C)
    }
}
