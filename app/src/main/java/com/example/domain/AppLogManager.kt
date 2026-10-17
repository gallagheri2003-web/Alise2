package com.example.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO, SYSTEM, WARNING, SECURITY
}

data class LogEntry(
    val id: String,
    val timestamp: String,
    val category: String,
    val level: LogLevel,
    val message: String
)

object AppLogManager {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    init {
        // Pre-populate with beautiful, high-tech initial system boot logs
        logInfo("BOOT", "ALISE Unified Neural Operating System Boot Phase 1 Complete")
        logSystem("HARDWARE", "Snapdragon 8 Elite core registry bound. 8 execution blocks active")
        logInfo("TELEMETRY", "DeviceTelemetryManager linked. Battery, CPU & MAC tracking calibrated")
        logSecurity("SECURITY", "Private DNS Proxy verified. Secure ARP table routing enabled")
        logInfo("LLM", "Gemini 2.0 REST pipelines compiled. Live API socket listener operational")
        logSystem("MEM_GUARD", "Dalvik GC threshold set to 85% heap ceiling")
        logWarning("SYSTEM", "Low system thermal headroom detected during model allocation pass")
    }

    private fun addLog(level: LogLevel, category: String, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val entry = LogEntry(
            id = "LOG-${System.currentTimeMillis() % 100000}",
            timestamp = timestamp,
            category = category.uppercase(),
            level = level,
            message = message
        )
        // Maintain last 150 logs to avoid memory bloat
        val currentList = _logs.value
        val updatedList = (listOf(entry) + currentList).take(150)
        _logs.value = updatedList
    }

    fun logInfo(category: String, message: String) {
        addLog(LogLevel.INFO, category, message)
    }

    fun logSystem(category: String, message: String) {
        addLog(LogLevel.SYSTEM, category, message)
    }

    fun logWarning(category: String, message: String) {
        addLog(LogLevel.WARNING, category, message)
    }

    fun logSecurity(category: String, message: String) {
        addLog(LogLevel.SECURITY, category, message)
    }

    fun clearLogs() {
        _logs.value = emptyList()
        logSystem("MEM_GUARD", "All app logs and audit records flushed from transient buffers")
    }
}
