package com.example.deepsleep.ui.logs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.model.LogEntry
import com.example.deepsleep.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class LogsViewModel : ViewModel() {

    private val repository = LogRepository()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _selectedLevel = MutableStateFlow<LogLevel?>(null)
    val selectedLevel: StateFlow<LogLevel?> = _selectedLevel.asStateFlow()

    val filteredLogs: StateFlow<List<LogEntry>> = combine(
        _logs,
        _selectedLevel
    ) { logs, level ->
        if (level == null) logs else logs.filter { it.level == level }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        refreshLogs()
    }

    fun refreshLogs() {
        viewModelScope.launch {
            _logs.value = repository.readLogs()
        }
    }

    fun setLevelFilter(level: LogLevel?) {
        _selectedLevel.value = level
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            refreshLogs()
        }
    }

    fun exportLogs(context: Context) {
        viewModelScope.launch {
            repository.createShareableFile(context)
        }
    }

    suspend fun getLogSize(): String = repository.getLogSize()
}