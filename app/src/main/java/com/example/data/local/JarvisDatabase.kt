package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.daos.AutoCodeSnippetDao
import com.example.data.local.daos.CodeTemplateDao
import com.example.data.local.daos.ConversationDao
import com.example.data.local.daos.DirectiveDao
import com.example.data.local.daos.ProtocolDao
import com.example.data.local.daos.SystemLogDao
import com.example.data.local.daos.ThreatIncidentDao
import com.example.data.local.daos.VoiceNoteDao
import com.example.data.local.entities.AutoCodeSnippetEntity
import com.example.data.local.entities.CodeTemplateEntity
import com.example.data.local.entities.ConversationMessageEntity
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.ProtocolEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.ThreatIncidentEntity
import com.example.data.local.entities.VoiceNoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DirectiveEntity::class,
        ProtocolEntity::class,
        SystemLogEntity::class,
        VoiceNoteEntity::class,
        ThreatIncidentEntity::class,
        AutoCodeSnippetEntity::class,
        CodeTemplateEntity::class,
        ConversationMessageEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun directiveDao(): DirectiveDao
    abstract fun protocolDao(): ProtocolDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun threatIncidentDao(): ThreatIncidentDao
    abstract fun autoCodeSnippetDao(): AutoCodeSnippetDao
    abstract fun codeTemplateDao(): CodeTemplateDao
    abstract fun conversationDao(): ConversationDao


    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            populateDefaultProtocolsAndDirectives(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaultProtocolsAndDirectives(db: JarvisDatabase) {
            // Real Samsung Galaxy S25 Default protocols
            val defaultProtocols = listOf(
                ProtocolEntity(
                    codeName = "S25_CLEAN_SLATE",
                    displayName = "S25 Clean Slate Cache Purge",
                    description = "Purges active background caches, recalibrates Samsung Galaxy S25 RAM telemetry, and resets dynamic AMOLED matrix.",
                    actionsJson = "[\"CLEAR_LOGS\", \"RESET_TELEMETRY\", \"HAPTIC_CONFIRM\", \"CHIME_EXECUTE\"]",
                    category = "PRODUCTIVITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_BATTERY_SAVER",
                    displayName = "S25 Ultra Battery Saver",
                    description = "Caps AMOLED display refresh at 60Hz, dims liquid glass HUD, and throttles background sensor polling for extended life.",
                    actionsJson = "[\"CAP_REFRESH_RATE\", \"HUD_DIM\", \"BATTERY_SAVER_ON\"]",
                    category = "PRODUCTIVITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_SENTRY_MODE",
                    displayName = "S25 Optical Sentry & GPS",
                    description = "Deploys photon array strobe, locks real-time GPS radar tracking, and arms perimeter breach warning.",
                    actionsJson = "[\"VISION_ARM\", \"FLASHLIGHT_BURST\", \"SCANNER_RETICLE_LOCK\", \"GPS_ARM\"]",
                    category = "SECURITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_DEEP_FOCUS",
                    displayName = "S25 Deep Focus Mode",
                    description = "Mutes non-essential audio streams, sets media volume to 15%, dims ambient lighting, and engages focus timer.",
                    actionsJson = "[\"DND_ENGAGE\", \"HUD_MINIMAL\", \"FOCUS_TIMER_60\", \"VOICE_CONFIRM\"]",
                    category = "PRODUCTIVITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_DIAGNOSTIC_SCAN",
                    displayName = "S25 Hardware Diagnostic",
                    description = "Executes full hardware benchmark of Samsung Galaxy S25: Snapdragon 8 Elite SoC, core thermals, 12GB RAM, and battery cell state.",
                    actionsJson = "[\"DIAGNOSTIC_HARDWARE\", \"BENCHMARK_SENSORS\", \"VOICE_READOUT\"]",
                    category = "TACTICAL",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_STEALTH_MODE",
                    displayName = "S25 Stealth Protocol",
                    description = "Silences all acoustic synthesizers, reduces display illumination to minimal tactical level, and enables silent haptic alerts.",
                    actionsJson = "[\"AUDIO_MUTE\", \"HUD_DIM\", \"SILENT_HAPTIC\"]",
                    category = "SECURITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "ACOUSTIC_SONAR_PING",
                    displayName = "Acoustic Lost-Phone Sonar",
                    description = "Rings lost-phone high-frequency acoustic locator tone via Samsung Galaxy S25 stereo speakers.",
                    actionsJson = "[\"SONAR_ALARM_ON\", \"FLASHLIGHT_STROBE\"]",
                    category = "SECURITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "GPS_PERIMETER_LOCK",
                    displayName = "GPS Safe Perimeter Lock",
                    description = "Recalibrates current GPS coordinates as safe zone center anchor and arms boundary breach detection.",
                    actionsJson = "[\"GPS_REANCHOR\", \"SAFE_ZONE_LOCK\"]",
                    category = "TACTICAL",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_WIFI_DEFENSE",
                    displayName = "Wi-Fi Shield & Socket Reset",
                    description = "Inspects Wi-Fi link security, RSSI signal, flushes stale socket connection pools, and resets active Wi-Fi interface.",
                    actionsJson = "[\"WIFI_INSPECT\", \"FLUSH_SOCKETS\", \"INTERFACE_RESET\"]",
                    category = "SECURITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "S25_DNS_SANCTUARY",
                    displayName = "DNS Proxy Flush & Sanitize",
                    description = "Executes real DNS resolution probes against Cloudflare/Google DoT, detects proxy interception, and launches Private DNS configuration.",
                    actionsJson = "[\"DNS_PROBE\", \"FLUSH_DNS_CACHE\", \"PRIVATE_DNS_LAUNCH\"]",
                    category = "SECURITY",
                    isPreset = true
                ),
                ProtocolEntity(
                    codeName = "TACTICAL_DEFENSE_LOCKDOWN",
                    displayName = "Defense Alert Lockdown",
                    description = "Purges clipboard buffers, silences acoustic speaker streams, engages warning strobe array, and opens system security center.",
                    actionsJson = "[\"CLIPBOARD_PURGE\", \"AUDIO_MUTE\", \"WARNING_STROBE\", \"SECURITY_LAUNCH\"]",
                    category = "SECURITY",
                    isPreset = true
                )
            )
            db.protocolDao().insertProtocols(defaultProtocols)

            // Default Directives for Ian
            db.directiveDao().insertDirective(
                DirectiveEntity(
                    title = "Primary User Identification",
                    content = "The primary user is Ian. Address him as Ian.",
                    priority = "CRITICAL"
                )
            )
            db.directiveDao().insertDirective(
                DirectiveEntity(
                    title = "Galaxy S25 Optimization Mandate",
                    content = "Maintain peak performance for Samsung Galaxy S25 telemetry, dynamic AMOLED refresh, and low-latency response times.",
                    priority = "HIGH"
                )
            )
            db.directiveDao().insertDirective(
                DirectiveEntity(
                    title = "Voice Response Standard",
                    content = "Deliver concise, intelligent, respectful, and natural responses with zero unnecessary delay.",
                    priority = "NORMAL"
                )
            )

            // Initial System Log
            db.systemLogDao().insertLog(
                SystemLogEntity(
                    tag = "ALISE_CORE",
                    message = "ALISE Engine online. Hardware platform: Samsung Galaxy S25. Primary User: Ian.",
                    level = "TACTICAL"
                )
            )

            // Seed Built-in Python Boilerplate Templates
            try {
                if (db.codeTemplateDao().getTemplateCount() == 0) {
                    db.codeTemplateDao().insertTemplates(BuiltinPythonTemplates.defaultTemplates)
                }
            } catch (e: Exception) {
                // Ignore if already populated
            }

            // Seed Initial Conversation History
            try {
                if (db.conversationDao().getMessageCount() == 0) {
                    db.conversationDao().insertMessage(
                        ConversationMessageEntity(
                            sender = "ALISE",
                            text = "Hello! I'm ALISE, powered by Gemini Neural Intelligence with Google Search Grounding and Speech Transcription. How can I assist you today?",
                            modelUsed = "Gemini 3.5 Flash",
                            isSearchGrounded = true
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore if already populated
            }
        }
    }
}

