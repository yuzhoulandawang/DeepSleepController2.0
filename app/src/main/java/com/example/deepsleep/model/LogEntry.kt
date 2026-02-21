package com.example.deepsleep.model

enum class LogLevel {
    DEBUG,
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    FATAL
}

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String = "",
    val message: String,
    val throwable: String? = null
)
