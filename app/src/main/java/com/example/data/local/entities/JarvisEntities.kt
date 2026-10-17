package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "directives")
data class DirectiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val priority: String = "NORMAL", // "HIGH", "CRITICAL", "NORMAL"
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "protocols")
data class ProtocolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codeName: String,
    val displayName: String,
    val description: String,
    val actionsJson: String, // list of action tags (e.g. ["FLASHLIGHT_ON", "SPOTIFY_LAUNCH", "MAX_VOLUME"])
    val category: String = "TACTICAL", // "TACTICAL", "SECURITY", "PRODUCTIVITY", "ENTERTAINMENT"
    val isPreset: Boolean = true,
    val executionCount: Int = 0,
    val lastExecuted: Long = 0L
)

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tag: String,
    val message: String,
    val level: String = "INFO", // "INFO", "WARN", "TACTICAL", "VOICE", "ERROR"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val transcript: String,
    val aiSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "threat_incidents")
data class ThreatIncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "WIFI", "SYSTEM", "FILE", "MALWARE", "RANSOMWARE", "NETWORK", "DNS"
    val severity: String = "HIGH", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val title: String,
    val description: String,
    val sourceIdentifier: String = "Unknown",
    val remediationAction: String = "Automated Isolation",
    val patternSignature: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

@Entity(tableName = "auto_code_snippets")
data class AutoCodeSnippetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val language: String = "Python", // "Python", "Kotlin", "JavaScript", "Java", etc.
    val originalCode: String,
    val correctedCode: String,
    val fixesSummary: String = "",
    val issuesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "code_templates")
data class CodeTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Python General", // "Web & API", "Bots & Agents", "Data Science", "System & CLI", "Database", "Security"
    val description: String = "",
    val language: String = "Python",
    val templateCode: String,
    val isCustom: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_history")
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "ALISE"
    val text: String,
    val modelUsed: String = "Gemini 3.5 Flash",
    val isSearchGrounded: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


