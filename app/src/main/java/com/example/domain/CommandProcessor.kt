package com.example.domain

import com.example.data.api.AiLlmEngine
import com.example.data.local.JarvisRepository
import com.example.data.local.entities.VoiceNoteEntity
import com.example.hardware.AndroidActionController
import com.example.hardware.CommandExecutionResult
import com.example.hardware.CommandIntent
import com.example.hardware.CommandStatus
import com.example.hardware.DeviceTelemetryManager
import com.example.hardware.HardwareController
import com.example.hardware.NetworkDefenseController
import com.example.service.JarvisAccessibilityService
import com.example.voice.JarvisVoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PendingConversation(
    val intent: CommandIntent,
    val contactQuery: String? = null,
    val payload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class CommandProcessor(
    private val repository: JarvisRepository,
    private val hardwareController: HardwareController,
    private val androidActionController: AndroidActionController,
    private val telemetryManager: DeviceTelemetryManager,
    private val protocolEngine: ProtocolEngine,
    private val voiceEngine: JarvisVoiceEngine,
    private val networkDefenseController: NetworkDefenseController
) {

    private val _lastExecutionResult = MutableStateFlow<CommandExecutionResult?>(null)
    val lastExecutionResult: StateFlow<CommandExecutionResult?> = _lastExecutionResult.asStateFlow()

    private var pendingConversation: PendingConversation? = null

    suspend fun processCommand(
        rawInput: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        speakOutput: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val input = rawInput.trim()
        val lower = input.lowercase()

        repository.logSystemEvent("INPUT", input, "CHAT")

        // 0. Check Active Pending Multi-Turn Conversation (e.g. Awaiting Message Content for "Text Mom")
        val currentPending = pendingConversation
        if (currentPending != null && System.currentTimeMillis() - currentPending.timestamp < 60_000) {
            when (currentPending.intent) {
                CommandIntent.SEND_MESSAGE -> {
                    pendingConversation = null
                    val contact = currentPending.contactQuery ?: "Contact"
                    val result = androidActionController.prepareSmsMessage(contact, input, "Text $contact $input")
                    _lastExecutionResult.value = result
                    repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")
                    if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                    return@withContext result.speechResponse
                }
                else -> {
                    pendingConversation = null
                }
            }
        } else {
            pendingConversation = null
        }

        // 1. Text Messaging / SMS Intent ("Text Mom", "Send message to Mom: I'll be home soon", "Message Dad")
        if (lower.startsWith("text ") || lower.startsWith("send message to ") || lower.startsWith("message ") || lower.startsWith("sms ")) {
            val (recipient, messageBody) = parseMessageCommand(input)
            if (recipient.isNotBlank()) {
                val result = androidActionController.prepareSmsMessage(recipient, messageBody, input)
                _lastExecutionResult.value = result
                repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")

                if (result.status == CommandStatus.REQUIRES_USER_CONFIRMATION) {
                    pendingConversation = PendingConversation(
                        intent = CommandIntent.SEND_MESSAGE,
                        contactQuery = recipient
                    )
                }

                if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                return@withContext result.speechResponse
            }
        }

        // 2. Phone Call Intent ("Call Mom", "Call 555-1234", "Dial Dad")
        if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.startsWith("phone ")) {
            val contactQuery = input
                .replaceFirst("call", "", ignoreCase = true)
                .replaceFirst("dial", "", ignoreCase = true)
                .replaceFirst("phone", "", ignoreCase = true)
                .trim()

            if (contactQuery.isNotBlank() && !lower.contains("what is") && !lower.contains("how to")) {
                val result = androidActionController.makePhoneCall(contactQuery, input)
                _lastExecutionResult.value = result
                repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")
                if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                return@withContext result.speechResponse
            }
        }

        // 3. Application Launcher ("Open Spotify", "Launch YouTube", "Open Camera", "Open Settings", "Open Maps")
        if ((lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start app ")) && !lower.contains("open source") && !lower.contains("how to open") && !lower.contains("open ai")) {
            val appQuery = input
                .replaceFirst("open", "", ignoreCase = true)
                .replaceFirst("launch", "", ignoreCase = true)
                .replaceFirst("start app", "", ignoreCase = true)
                .replaceFirst("the", "", ignoreCase = true)
                .replaceFirst("app", "", ignoreCase = true)
                .trim()

            if (appQuery.isNotBlank() && appQuery.split(" ").size <= 3) {
                val result = androidActionController.openApplication(appQuery, input)
                if (result.status == CommandStatus.SUCCESS) {
                    _lastExecutionResult.value = result
                    repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")
                    if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                    return@withContext result.speechResponse
                }
            }
        }

        // 4. Alarm and Timer Controls ("Set alarm for 7:30 AM", "Set timer for 10 minutes", "Set a 5 minute timer")
        if ((lower.startsWith("set alarm") || lower.startsWith("set an alarm") || lower.startsWith("set timer") || lower.startsWith("set a timer") || lower.startsWith("start timer")) && !lower.contains("explain") && !lower.contains("what is")) {
            if (lower.contains("alarm")) {
                val (hour, minute) = parseAlarmTime(lower)
                val label = "ALISE Alarm"
                val result = androidActionController.setAlarm(hour, minute, label, input)
                _lastExecutionResult.value = result
                repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")
                if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                return@withContext result.speechResponse
            } else if (lower.contains("timer")) {
                val seconds = parseTimerSeconds(lower)
                val result = androidActionController.setTimer(seconds, "ALISE Timer", input)
                _lastExecutionResult.value = result
                repository.logSystemEvent("COMMAND_EXEC", "${result.intent}: ${result.status} - ${result.resultDescription}", "SYSTEM")
                if (speakOutput) voiceEngine.speak(result.speechResponse, playChimeBefore = true)
                return@withContext result.speechResponse
            }
        }

        // 5. Flashlight Controls ("turn on flashlight", "turn off flashlight", "toggle flashlight", "flashlight on", "flashlight off")
        if ((lower.contains("flashlight") || lower.contains("torch")) && (lower.contains("turn on") || lower.contains("turn off") || lower.contains("toggle") || lower.startsWith("flashlight on") || lower.startsWith("flashlight off") || lower.contains("strobe"))) {
            val execution: CommandExecutionResult
            if (lower.contains("on") || lower.contains("enable") || lower.contains("activate")) {
                val ok = hardwareController.setFlashlight(true)
                execution = CommandExecutionResult(
                    rawCommand = input,
                    intent = CommandIntent.CONTROL_HARDWARE,
                    status = if (ok) CommandStatus.SUCCESS else CommandStatus.FAILED,
                    executionMethod = "CameraManager.setTorchMode(cameraId, true)",
                    resultDescription = if (ok) "Flashlight turned on." else "CameraManager torch failed.",
                    speechResponse = if (ok) "Flashlight turned on." else "Unable to activate flashlight."
                )
            } else if (lower.contains("off") || lower.contains("disable") || lower.contains("deactivate")) {
                val ok = hardwareController.setFlashlight(false)
                execution = CommandExecutionResult(
                    rawCommand = input,
                    intent = CommandIntent.CONTROL_HARDWARE,
                    status = if (ok) CommandStatus.SUCCESS else CommandStatus.FAILED,
                    executionMethod = "CameraManager.setTorchMode(cameraId, false)",
                    resultDescription = "Flashlight deactivated.",
                    speechResponse = "Flashlight deactivated."
                )
            } else if (lower.contains("strobe") || lower.contains("burst")) {
                hardwareController.strobeFlashlight(5, 100)
                execution = CommandExecutionResult(
                    rawCommand = input,
                    intent = CommandIntent.CONTROL_HARDWARE,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "CameraManager.setTorchMode (Strobe Sequence)",
                    resultDescription = "Strobe burst activated.",
                    speechResponse = "Strobe burst activated."
                )
            } else {
                val state = hardwareController.toggleFlashlight()
                execution = CommandExecutionResult(
                    rawCommand = input,
                    intent = CommandIntent.CONTROL_HARDWARE,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "CameraManager.setTorchMode toggle",
                    resultDescription = if (state) "Flashlight turned ON." else "Flashlight turned OFF.",
                    speechResponse = if (state) "Flashlight turned on." else "Flashlight turned off."
                )
            }
            _lastExecutionResult.value = execution
            repository.logSystemEvent("COMMAND_EXEC", "${execution.intent}: ${execution.status} - ${execution.resultDescription}", "SYSTEM")
            if (speakOutput) voiceEngine.speak(execution.speechResponse, playChimeBefore = true)
            return@withContext execution.speechResponse
        }

        // 6. Media Playback Controls ("pause music", "resume playback", "skip song", "next track")
        if (lower == "pause music" || lower == "stop music" || lower == "play music" || lower == "resume music" || lower == "next track" || lower == "next song" || lower == "previous song") {
            if (lower.contains("pause") || lower.contains("stop")) {
                hardwareController.triggerMediaAction("PAUSE")
                val reply = "Media playback paused."
                val exec = CommandExecutionResult(input, CommandIntent.MEDIA_CONTROL, CommandStatus.SUCCESS, "AudioManager Media Key: KEYCODE_MEDIA_PAUSE", "Dispatched media pause.", reply)
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
                return@withContext reply
            } else if (lower.contains("play") || lower.contains("resume")) {
                hardwareController.triggerMediaAction("PLAY")
                val reply = "Resuming media playback."
                val exec = CommandExecutionResult(input, CommandIntent.MEDIA_CONTROL, CommandStatus.SUCCESS, "AudioManager Media Key: KEYCODE_MEDIA_PLAY", "Dispatched media play.", reply)
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
                return@withContext reply
            } else if (lower.contains("next")) {
                hardwareController.triggerMediaAction("NEXT")
                val reply = "Skipping to the next track."
                val exec = CommandExecutionResult(input, CommandIntent.MEDIA_CONTROL, CommandStatus.SUCCESS, "AudioManager Media Key: KEYCODE_MEDIA_NEXT", "Dispatched media next.", reply)
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
                return@withContext reply
            } else if (lower.contains("previous")) {
                hardwareController.triggerMediaAction("PREV")
                val reply = "Returning to the previous track."
                val exec = CommandExecutionResult(input, CommandIntent.MEDIA_CONTROL, CommandStatus.SUCCESS, "AudioManager Media Key: KEYCODE_MEDIA_PREVIOUS", "Dispatched media previous.", reply)
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
                return@withContext reply
            }
        }

        // 7. Volume Adjustment ("volume up", "volume down", "mute volume", "turn volume up")
        if ((lower.startsWith("volume ") || lower.startsWith("turn volume ") || lower.startsWith("set volume ")) && (lower.contains("up") || lower.contains("down") || lower.contains("mute") || lower.contains("max") || lower.contains("%"))) {
            val reply: String
            val exec: CommandExecutionResult
            if (lower.contains("up") || lower.contains("increase") || lower.contains("louder") || lower.contains("raise")) {
                hardwareController.adjustVolumeUp()
                reply = "Volume increased to ${hardwareController.getMediaVolumePercent()}%."
                exec = CommandExecutionResult(input, CommandIntent.CONTROL_HARDWARE, CommandStatus.SUCCESS, "AudioManager.adjustStreamVolume(ADJUST_RAISE)", "Volume adjusted up.", reply)
            } else if (lower.contains("down") || lower.contains("decrease") || lower.contains("lower") || lower.contains("quieter")) {
                hardwareController.adjustVolumeDown()
                reply = "Volume lowered to ${hardwareController.getMediaVolumePercent()}%."
                exec = CommandExecutionResult(input, CommandIntent.CONTROL_HARDWARE, CommandStatus.SUCCESS, "AudioManager.adjustStreamVolume(ADJUST_LOWER)", "Volume adjusted down.", reply)
            } else if (lower.contains("max") || lower.contains("100")) {
                hardwareController.setMediaVolumePercent(100)
                reply = "Volume set to maximum."
                exec = CommandExecutionResult(input, CommandIntent.CONTROL_HARDWARE, CommandStatus.SUCCESS, "AudioManager.setStreamVolume(100%)", "Volume set to 100%.", reply)
            } else if (lower.contains("mute") || lower.contains("zero")) {
                hardwareController.setMediaVolumePercent(0)
                reply = "Audio muted."
                exec = CommandExecutionResult(input, CommandIntent.CONTROL_HARDWARE, CommandStatus.SUCCESS, "AudioManager.setStreamVolume(0)", "Audio muted.", reply)
            } else {
                reply = "Current media volume is ${hardwareController.getMediaVolumePercent()}%."
                exec = CommandExecutionResult(input, CommandIntent.CONTROL_HARDWARE, CommandStatus.SUCCESS, "AudioManager.getStreamVolume()", "Volume queried.", reply)
            }
            _lastExecutionResult.value = exec
            if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
            return@withContext reply
        }

        // 8. Note Taking ("take a note: ...", "remember that ...", "save note: ...")
        if (lower.startsWith("take a note") || lower.startsWith("note this") || lower.startsWith("remember that") || lower.startsWith("save note")) {
            val noteText = input.replaceFirst("take a note", "", ignoreCase = true)
                .replaceFirst("note this", "", ignoreCase = true)
                .replaceFirst("remember that", "", ignoreCase = true)
                .replaceFirst("save note", "", ignoreCase = true)
                .trim(':', ' ')
                .trim()
            if (noteText.isNotBlank()) {
                repository.saveVoiceNote(
                    VoiceNoteEntity(
                        title = "Note ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())}",
                        transcript = noteText,
                        aiSummary = "Saved to local notes vault"
                    )
                )
                val reply = "Saved to your notes: \"$noteText\""
                val exec = CommandExecutionResult(input, CommandIntent.NOTE_TAKING, CommandStatus.SUCCESS, "Room Database: VoiceNoteDao.insert()", "Saved voice note to persistent vault.", reply)
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(reply, playChimeBefore = true)
                return@withContext reply
            }
        }

        // 8.5. Auto Coder Intent ("auto code: ...", "correct this python file: ...", "fix code: ...", "check this python script: ...")
        if (lower.startsWith("auto code") || lower.startsWith("correct code") || lower.startsWith("fix code") || lower.startsWith("auto coder") || lower.startsWith("correct python") || (lower.contains("def ") && lower.contains("(") && lower.contains(")")) || (lower.contains("class ") && lower.contains("def "))) {
            val rawCode = if (lower.startsWith("auto code") || lower.startsWith("auto coder") || lower.startsWith("correct code") || lower.startsWith("fix code") || lower.startsWith("correct python")) {
                input.substringAfter(":").ifBlank { input.substringAfter("code").trim() }
            } else {
                input
            }

            if (rawCode.isNotBlank()) {
                val codeResult = AutoCoderEngine.processCode(rawCode)
                val reply = """
=== ALISE AUTO CODER [${codeResult.language}] ===
${codeResult.summary}
Fixes Applied (${codeResult.fixesApplied.size}):
${codeResult.fixesApplied.take(5).joinToString("\n") { "- Line ${it.lineNumber}: ${it.explanation}" }}

CORRECTED CODE:
${codeResult.correctedCode}
""".trimIndent()

                val speech = "Auto Coder complete. ${codeResult.summary}"
                val exec = CommandExecutionResult(
                    input,
                    CommandIntent.GENERAL_AI_QUERY,
                    CommandStatus.SUCCESS,
                    "${codeResult.providerUsed} (${codeResult.latencyMs}ms)",
                    "Auto Coder processed and repaired ${codeResult.fixesApplied.size} issues.",
                    speech
                )
                _lastExecutionResult.value = exec
                if (speakOutput) voiceEngine.speak(speech, playChimeBefore = true)
                return@withContext reply
            }
        }

        // 8.6. Custom Protocols & System Directive Triggers ("execute protocol ...", "run protocol ...", "protocol sentry mode", "bitcoin oracle")
        val isExplicitProtocol = lower.startsWith("protocol ") || lower.startsWith("execute protocol ") || lower.startsWith("run protocol ") || lower.startsWith("engage protocol ") || lower.startsWith("trigger protocol ") || lower.startsWith("start protocol ")
        val isDirectProtocolKeyword = lower.contains("bitcoin oracle") || lower.contains("btc oracle") || lower.contains("crypto oracle") || lower.contains("hubcoin") || lower == "sentry mode" || lower == "wifi defense" || lower == "clean slate" || lower == "battery saver" || lower == "deep focus" || lower == "diagnostic scan" || lower == "stealth mode" || lower == "sonar ping" || lower == "gps lock" || lower == "threat purge" || lower == "lockdown"

        if (isExplicitProtocol || isDirectProtocolKeyword) {
            val rawProtoQuery = when {
                lower.startsWith("execute protocol ") -> lower.removePrefix("execute protocol ")
                lower.startsWith("run protocol ") -> lower.removePrefix("run protocol ")
                lower.startsWith("engage protocol ") -> lower.removePrefix("engage protocol ")
                lower.startsWith("trigger protocol ") -> lower.removePrefix("trigger protocol ")
                lower.startsWith("start protocol ") -> lower.removePrefix("start protocol ")
                lower.startsWith("protocol ") -> lower.removePrefix("protocol ")
                else -> lower
            }.trim()

            val matchedCode = when {
                rawProtoQuery.contains("bitcoin") || rawProtoQuery.contains("oracle") || rawProtoQuery.contains("btc") || rawProtoQuery.contains("hubcoin") -> "BTC_ORACLE_TELEMETRY"
                rawProtoQuery.contains("wifi") || rawProtoQuery.contains("wi-fi") -> "S25_WIFI_DEFENSE"
                rawProtoQuery.contains("dns") -> "S25_DNS_SANCTUARY"
                rawProtoQuery.contains("lockdown") -> "TACTICAL_DEFENSE_LOCKDOWN"
                rawProtoQuery.contains("clean") -> "S25_CLEAN_SLATE"
                rawProtoQuery.contains("battery") -> "S25_BATTERY_SAVER"
                rawProtoQuery.contains("sentry") -> "S25_SENTRY_MODE"
                rawProtoQuery.contains("focus") -> "S25_DEEP_FOCUS"
                rawProtoQuery.contains("diagnostic") || rawProtoQuery.contains("benchmark") || rawProtoQuery.contains("health") -> "S25_DIAGNOSTIC_SCAN"
                rawProtoQuery.contains("stealth") -> "S25_STEALTH_MODE"
                rawProtoQuery.contains("sonar") -> "ACOUSTIC_SONAR_PING"
                rawProtoQuery.contains("gps") || rawProtoQuery.contains("perimeter") -> "GPS_PERIMETER_LOCK"
                rawProtoQuery.contains("threat") || rawProtoQuery.contains("purge") -> "NETWORK_THREAT_PURGE"
                else -> rawProtoQuery.uppercase().replace(" ", "_")
            }

            var protocol = repository.getProtocolByCode(matchedCode)
            if (protocol == null) {
                // Synthesize protocol entry if not yet in database
                protocol = com.example.data.local.entities.ProtocolEntity(
                    codeName = matchedCode,
                    displayName = if (matchedCode == "BTC_ORACLE_TELEMETRY") "BTC Oracle & Liquidity Matrix" else rawProtoQuery.replaceFirstChar { it.uppercase() },
                    description = "Voice triggered directive: $rawProtoQuery",
                    actionsJson = "[\"VOICE_TRIGGER\", \"EXECUTE\"]",
                    category = "TACTICAL",
                    isPreset = true
                )
            }

            protocolEngine.executeProtocol(protocol, if (speakOutput) voiceEngine else null)
            val reply = "Engaged Protocol: ${protocol.displayName}."
            val exec = CommandExecutionResult(
                rawCommand = input,
                intent = CommandIntent.SYSTEM_PROTOCOL,
                status = CommandStatus.SUCCESS,
                executionMethod = "ProtocolEngine.executeProtocol(${protocol.codeName})",
                resultDescription = "Triggered ${protocol.displayName} via voice command.",
                speechResponse = reply
            )
            _lastExecutionResult.value = exec
            repository.logSystemEvent("COMMAND_EXEC", "${exec.intent}: ${exec.status} - ${exec.resultDescription}", "SYSTEM")
            return@withContext reply
        }

        // 9. All other queries -> Send directly to the AI Conversation Engine (Gemini / Llama / Natural Reasoner)
        try {
            val telemetry = telemetryManager.telemetry.value
            val location = com.example.hardware.LivePhoneLocationState()
            val personality = voiceEngine.aiPersonalityMode.value

            val aiResult = AiLlmEngine.queryAi(
                userInput = input,
                telemetry = telemetry,
                locationState = location,
                personalityMode = personality,
                conversationHistory = conversationHistory
            )

            val replyText = aiResult.responseText
            val exec = CommandExecutionResult(
                rawCommand = input,
                intent = CommandIntent.GENERAL_AI_QUERY,
                status = CommandStatus.SUCCESS,
                executionMethod = "${aiResult.providerUsed} (${aiResult.latencyMs}ms)",
                resultDescription = "Generated intelligent natural response.",
                speechResponse = replyText
            )
            _lastExecutionResult.value = exec
            if (speakOutput) voiceEngine.speak(replyText, playChimeBefore = false)
            return@withContext replyText
        } catch (e: Exception) {
            val naturalFallback = AiLlmEngine.generateNaturalAssistantResponse(
                input = input,
                conversationHistory = conversationHistory,
                telemetry = telemetryManager.telemetry.value,
                locationState = com.example.hardware.LivePhoneLocationState()
            )
            val exec = CommandExecutionResult(
                rawCommand = input,
                intent = CommandIntent.GENERAL_AI_QUERY,
                status = CommandStatus.SUCCESS,
                executionMethod = "Local Natural Engine",
                resultDescription = "Natural response: ${e.message}",
                speechResponse = naturalFallback
            )
            _lastExecutionResult.value = exec
            if (speakOutput) voiceEngine.speak(naturalFallback, playChimeBefore = false)
            return@withContext naturalFallback
        }
    }

    private fun parseMessageCommand(input: String): Pair<String, String?> {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        val prefixes = listOf("text ", "send message to ", "message ", "sms ")
        var matchedPrefix = ""
        for (p in prefixes) {
            if (lower.startsWith(p)) {
                matchedPrefix = p
                break
            }
        }

        val rest = trimmed.substring(matchedPrefix.length).trim()

        val separators = listOf(" saying ", " that ", " and say ", ": ", ", ")
        for (sep in separators) {
            val idx = rest.indexOf(sep, ignoreCase = true)
            if (idx > 0) {
                val recipient = rest.substring(0, idx).trim()
                val body = rest.substring(idx + sep.length).trim()
                return Pair(recipient, body)
            }
        }

        val words = rest.split(" ")
        if (words.size <= 2) {
            return Pair(rest, null)
        }

        val recipient = words[0]
        val body = words.drop(1).joinToString(" ")
        return Pair(recipient, body)
    }

    private fun parseAlarmTime(lower: String): Pair<Int, Int> {
        val regex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val match = regex.find(lower)
        if (match != null) {
            var hour = match.groupValues[1].toIntOrNull() ?: 7
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3].lowercase()

            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            return Pair(hour, minute)
        }
        return Pair(7, 0)
    }

    private fun parseTimerSeconds(lower: String): Int {
        val regexMin = Regex("(\\d+)\\s*(?:min|minute)")
        val matchMin = regexMin.find(lower)
        if (matchMin != null) {
            val min = matchMin.groupValues[1].toIntOrNull() ?: 5
            return min * 60
        }

        val regexSec = Regex("(\\d+)\\s*(?:sec|second)")
        val matchSec = regexSec.find(lower)
        if (matchSec != null) {
            return matchSec.groupValues[1].toIntOrNull() ?: 60
        }

        val regexNum = Regex("(\\d+)")
        val matchNum = regexNum.find(lower)
        if (matchNum != null) {
            val num = matchNum.groupValues[1].toIntOrNull() ?: 5
            return num * 60
        }

        return 300
    }
}

