package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.api.AiLlmEngine
import com.example.ui.components.AlienBioluminescentOrb
import com.example.ui.components.AlienHumanBadge
import com.example.ui.components.TacticalCodeTerminalOverlay
import com.example.ui.components.HolographicSpeechBubble
import com.example.ui.components.GlowingNeonText
import com.example.ui.components.alienSymbiontSurface
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.components.HolographicVoiceWaveformHud
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.VoiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class AliseSessionState {
    IDLE, CONNECTING, ACTIVE, LISTENING, SPEAKING, ERROR
}

enum class AliseLiveEngineMode(val title: String) {
    DIRECT_AI("ALISE DIRECT NEURAL CORE"),
    WEBSOCKET("GEMINI BIDI (WEBSOCKET)"),
    MARK_LIV_BACKEND("MARK-LIV LIVE BACKEND")
}

data class AliseDeviceInfo(
    val platform: String = "android",
    val deviceType: String = "phone",
    val isMobile: Boolean = true,
    val isTouchDevice: Boolean = true,
    val isSamsung: Boolean = false,
    val isGalaxyS25Family: Boolean = false,
    val isGalaxyS26Family: Boolean = false,
    val modelHint: String? = null,
    val browser: String = "Android Native Engine",
    val orientation: String = "portrait",
    val screenWidth: Int = 1080,
    val screenHeight: Int = 2400,
    val viewportWidth: Int = 412,
    val viewportHeight: Int = 892,
    val devicePixelRatio: Float = 3.0f,
    val fullscreenSupported: Boolean = true,
    val fullscreenActive: Boolean = false,
    val standalone: Boolean = true,
    val userAgent: String = ""
)

fun extractSamsungModel(model: String): String? {
    val regex = Regex("""\bSM-[A-Z0-9]+\b""", RegexOption.IGNORE_CASE)
    return regex.find(model)?.value?.uppercase()
}

fun isGalaxyS25Model(model: String?): Boolean {
    if (model.isNullOrBlank()) return false
    return Regex("""^SM-S93[1-8][A-Z0-9]*$""", RegexOption.IGNORE_CASE).matches(model)
}

fun isGalaxyS26Model(model: String?): Boolean {
    if (model.isNullOrBlank()) return false
    return Regex("""^SM-S94[0-9][A-Z0-9]*$""", RegexOption.IGNORE_CASE).matches(model)
}

fun getNativeDeviceInfo(context: Context, isFullscreen: Boolean): AliseDeviceInfo {
    val manufacturer = Build.MANUFACTURER ?: ""
    val model = Build.MODEL ?: ""
    val brand = Build.BRAND ?: ""
    val modelHint = extractSamsungModel(model) ?: extractSamsungModel(brand) ?: model.ifBlank { null }
    val isSamsung = manufacturer.contains("samsung", ignoreCase = true) || brand.contains("samsung", ignoreCase = true) || model.startsWith("SM-", ignoreCase = true)
    val isS25 = isGalaxyS25Model(modelHint)
    val isS26 = isGalaxyS26Model(modelHint)

    val metrics = context.resources.displayMetrics
    val config = context.resources.configuration
    val orientation = if (config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"
    val sw = config.smallestScreenWidthDp

    val deviceType = when {
        sw >= 700 -> "tablet"
        sw >= 1000 -> "desktop"
        else -> "phone"
    }

    val userAgent = "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${model.ifBlank { "Android" }} Build/${Build.ID}) ALISE/2.5 LiveTalk"

    return AliseDeviceInfo(
        platform = "android",
        deviceType = deviceType,
        isMobile = true,
        isTouchDevice = true,
        isSamsung = isSamsung,
        isGalaxyS25Family = isS25,
        isGalaxyS26Family = isS26,
        modelHint = modelHint,
        browser = "Android Native (OkHttp/WebSocket)",
        orientation = orientation,
        screenWidth = metrics.widthPixels,
        screenHeight = metrics.heightPixels,
        viewportWidth = (metrics.widthPixels / metrics.density).toInt(),
        viewportHeight = (metrics.heightPixels / metrics.density).toInt(),
        devicePixelRatio = metrics.density,
        fullscreenSupported = true,
        fullscreenActive = isFullscreen,
        standalone = true,
        userAgent = userAgent
    )
}

fun serializeDeviceInfo(device: AliseDeviceInfo): String {
    return when {
        device.isGalaxyS25Family -> "Samsung Galaxy S25 family (${device.modelHint ?: "model hidden"})"
        device.isGalaxyS26Family -> "Samsung Galaxy S26 family (${device.modelHint ?: "model hidden"})"
        device.isSamsung && device.deviceType == "phone" -> "Samsung Galaxy/Android phone (${device.modelHint ?: "model hidden"})"
        device.deviceType == "phone" -> "${device.platform.uppercase()} phone"
        device.deviceType == "tablet" -> "${device.platform.uppercase()} tablet"
        else -> "${device.platform.uppercase()} desktop"
    }
}

/**
 * Resolves the WebSocket URL with the Gemini API key query param if targeting Google's Generative Service.
 */
fun resolveWebSocketUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.contains("generativelanguage.googleapis.com") && !trimmed.contains("key=")) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val delimiter = if (trimmed.contains("?")) "&" else "?"
        return "$trimmed${delimiter}key=$apiKey"
    }
    return trimmed
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AliseLiveConsoleScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Voice & Telemetry States
    val voiceState by viewModel.voiceState.collectAsState()
    val audioAmp by viewModel.audioAmplitude.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val activeVoiceModel by viewModel.voiceEngine.activeVoiceModel.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    // Mode Selector: DIRECT_AI (always reliable & immediate audio) vs WEBSOCKET (live bidi socket)
    var selectedEngineMode by remember { mutableStateOf(AliseLiveEngineMode.DIRECT_AI) }
    var wsUrl by remember { mutableStateOf("wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent") }
    var sessionState by remember { mutableStateOf(AliseSessionState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var uiMessage by remember { mutableStateOf<String?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    var lastReceivedText by remember {
        mutableStateOf("ALISE Live Tactical System ready. Tap 'START ALISE' to activate real-time audio conversation.")
    }
    var isMicrophoneActive by remember { mutableStateOf(false) }

    val deviceInfo = remember(configuration.orientation, isFullscreen) {
        getNativeDeviceInfo(context, isFullscreen)
    }

    val deviceLabel = remember(deviceInfo) {
        serializeDeviceInfo(deviceInfo)
    }

    // Audio & Socket References
    var webSocketRef by remember { mutableStateOf<WebSocket?>(null) }
    var streamingAudioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    // Sync input text with live speech recognition when mic is listening in Direct AI mode
    LaunchedEffect(partialTranscript) {
        if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI && isMicrophoneActive && partialTranscript.isNotBlank()) {
            if (!partialTranscript.startsWith("Listening for") && !partialTranscript.startsWith("Listening...")) {
                inputText = partialTranscript
            }
        }
    }

    // Sync session state with voice state in Direct AI mode
    LaunchedEffect(voiceState) {
        if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI && sessionState != AliseSessionState.IDLE) {
            sessionState = when (voiceState) {
                is VoiceState.Speaking -> AliseSessionState.SPEAKING
                is VoiceState.ListeningCommand, is VoiceState.ListeningWakeWord -> AliseSessionState.LISTENING
                is VoiceState.Processing -> AliseSessionState.CONNECTING
                is VoiceState.Executing -> AliseSessionState.CONNECTING
                is VoiceState.Success, is VoiceState.Interrupted -> AliseSessionState.ACTIVE
                is VoiceState.Error -> AliseSessionState.ERROR
                VoiceState.Idle -> if (isMicrophoneActive) AliseSessionState.LISTENING else AliseSessionState.ACTIVE
            }
        }
    }

    // Microphone Permission Launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uiMessage = "Microphone permission granted."
        } else {
            errorMessage = "Microphone access is required for live voice conversation."
        }
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAudioPermissionIfNeeded(): Boolean {
        if (!hasRecordAudioPermission()) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        return true
    }

    // Stop all audio output (barge-in)
    fun stopAudioPlayback() {
        try {
            streamingAudioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                flush()
                release()
            }
        } catch (_: Exception) {}
        streamingAudioTrack = null

        // Stop on-device TTS engine
        viewModel.voiceEngine.bargeIn()

        // Inform remote websocket if connected
        webSocketRef?.let { ws ->
            try {
                if (wsUrl.contains("generativelanguage.googleapis.com")) {
                    val bargePayload = JSONObject().apply {
                        put("clientContent", JSONObject().apply {
                            put("turnComplete", true)
                        })
                    }
                    ws.send(bargePayload.toString())
                } else {
                    val bargeInJson = JSONObject().apply {
                        put("type", "barge_in")
                    }
                    ws.send(bargeInJson.toString())
                }
            } catch (_: Exception) {}
        }

        if (sessionState == AliseSessionState.SPEAKING) {
            sessionState = AliseSessionState.ACTIVE
        }
        uiMessage = "Audio playback interrupted (Barge-In)."
    }

    // Stream incoming base64 PCM bytes via AudioTrack
    fun playIncomingBase64Pcm(base64Data: String, sampleRate: Int = 24000) {
        try {
            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
            if (audioBytes.isEmpty()) return

            sessionState = AliseSessionState.SPEAKING

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            var track = streamingAudioTrack
            if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
                track = AudioTrack.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, audioBytes.size * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                streamingAudioTrack = track
            }

            track.write(audioBytes, 0, audioBytes.size)

            coroutineScope.launch {
                val durationMs = (audioBytes.size / (sampleRate * 2f)) * 1000f
                delay(durationMs.toLong() + 150)
                if (sessionState == AliseSessionState.SPEAKING) {
                    sessionState = AliseSessionState.ACTIVE
                }
            }
        } catch (e: Exception) {
            errorMessage = "PCM audio stream playback failed: ${e.message}"
        }
    }

    // Direct Neural Engine: Query Gemini with voice feedback
    fun executeDirectAiQuery(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        stopAudioPlayback()
        sessionState = AliseSessionState.SPEAKING
        lastReceivedText = "ALISE is computing response..."
        errorMessage = null

        coroutineScope.launch {
            try {
                viewModel.voiceEngine.soundSynthesizer.playAcknowledgeTone()
                val history = viewModel.interactions.value.takeLast(6).map { it.sender to it.text }

                val aiResult = AiLlmEngine.queryAi(
                    userInput = trimmed,
                    telemetry = telemetry,
                    locationState = locationState,
                    personalityMode = viewModel.voiceEngine.aiPersonalityMode.value,
                    conversationHistory = history,
                    geminiModel = viewModel.selectedGeminiModel.value,
                    chatRole = viewModel.selectedGeminiRole.value,
                    customSystemInstruction = "You are ALISE (Adaptive Learning & Intelligent System Entity), a highly capable tactical voice AI. Keep spoken responses concise, assertive, natural, and helpful.",
                    enableSearchGrounding = false
                )

                lastReceivedText = aiResult.responseText
                uiMessage = "Response generated via ${aiResult.providerUsed} (${aiResult.latencyMs}ms)"

                // Speak aloud using the chosen neural voice model
                viewModel.voiceEngine.speak(aiResult.responseText, playChimeBefore = false)
            } catch (e: Exception) {
                lastReceivedText = "ALISE encountered a processing error: ${e.message}"
                errorMessage = "Direct query error: ${e.message}"
                sessionState = AliseSessionState.ACTIVE
            }
        }
    }

    // WebSocket: Stream microphone PCM to socket
    fun startMicrophoneRecordingForSocket(ws: WebSocket) {
        if (!requestAudioPermissionIfNeeded()) return

        recordingJob?.cancel()
        recordingJob = coroutineScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            if (bufferSize <= 0) return@launch

            try {
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2
                )

                if (recorder.state != AudioRecord.STATE_INITIALIZED) return@launch

                recorder.startRecording()
                val buffer = ByteArray(2048)
                val isGeminiLive = wsUrl.contains("generativelanguage.googleapis.com")

                withContext(Dispatchers.Main) {
                    isMicrophoneActive = true
                    sessionState = AliseSessionState.LISTENING
                }

                while (isActive && sessionState != AliseSessionState.IDLE) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val chunk = ByteArray(read)
                        System.arraycopy(buffer, 0, chunk, 0, read)
                        val base64 = Base64.encodeToString(chunk, Base64.NO_WRAP)

                        if (isGeminiLive) {
                            // Gemini Multimodal Live API format
                            val realtimeJson = JSONObject().apply {
                                put("realtimeInput", JSONObject().apply {
                                    put("mediaChunks", JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("mimeType", "audio/pcm;rate=16000")
                                            put("data", base64)
                                        })
                                    })
                                })
                            }
                            ws.send(realtimeJson.toString())
                        } else {
                            // Generic ALISE Server format
                            val audioPayload = JSONObject().apply {
                                put("type", "audio")
                                put("format", "pcm16")
                                put("sampleRate", 16000)
                                put("channels", 1)
                                put("data", base64)
                            }
                            ws.send(audioPayload.toString())
                        }
                    }
                    delay(40)
                }

                recorder.stop()
                recorder.release()
            } catch (se: SecurityException) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Microphone permission required for audio stream."
                }
            } catch (e: Exception) {
                // Recording interrupted
            } finally {
                withContext(Dispatchers.Main) {
                    isMicrophoneActive = false
                }
            }
        }
    }

    // End Active Session
    fun endSession() {
        recordingJob?.cancel()
        recordingJob = null
        isMicrophoneActive = false

        viewModel.voiceEngine.stopListening()
        stopAudioPlayback()

        webSocketRef?.close(1000, "ALISE session ended by user")
        webSocketRef = null

        errorMessage = null
        sessionState = AliseSessionState.IDLE
        uiMessage = "ALISE session ended."
    }

    // Connect to WebSocket Server
    fun startWebSocketSession() {
        if (sessionState != AliseSessionState.IDLE && sessionState != AliseSessionState.ERROR) {
            return
        }

        val resolvedUrl = resolveWebSocketUrl(wsUrl)
        if (resolvedUrl.isBlank()) {
            errorMessage = "WebSocket URL is required."
            sessionState = AliseSessionState.ERROR
            return
        }

        errorMessage = null
        uiMessage = "Connecting to ALISE Live stream..."
        sessionState = AliseSessionState.CONNECTING

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url(resolvedUrl)
            .build()

        val isGeminiLive = resolvedUrl.contains("generativelanguage.googleapis.com")

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocketRef = webSocket
                coroutineScope.launch(Dispatchers.Main) {
                    sessionState = AliseSessionState.ACTIVE
                    uiMessage = "ALISE Live WebSocket connected."
                    viewModel.voiceEngine.soundSynthesizer.playActivationChime()

                    if (isGeminiLive) {
                        // Official Gemini Multimodal Live Setup Handshake
                        val setupJson = JSONObject().apply {
                            put("setup", JSONObject().apply {
                                put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
                                put("generationConfig", JSONObject().apply {
                                    put("responseModalities", JSONArray().apply {
                                        put("AUDIO")
                                        put("TEXT")
                                    })
                                    put("speechConfig", JSONObject().apply {
                                        put("voiceConfig", JSONObject().apply {
                                            put("prebuiltVoiceConfig", JSONObject().apply {
                                                put("voiceName", "Aoede")
                                            })
                                        })
                                    })
                                })
                                put("systemInstruction", JSONObject().apply {
                                    put("parts", JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("text", "You are ALISE (Adaptive Learning & Intelligent System Entity), a tactical AI assistant. Speak concisely, clearly, and authoritatively to Ian.")
                                        })
                                    })
                                })
                            })
                        }
                        webSocket.send(setupJson.toString())
                    } else {
                        // Standard Client Info Handshake
                        val clientInfoJson = JSONObject().apply {
                            put("type", "client_info")
                            put("client", JSONObject().apply {
                                put("app", "ALISE")
                                put("device", JSONObject().apply {
                                    put("platform", deviceInfo.platform)
                                    put("deviceType", deviceInfo.deviceType)
                                    put("isMobile", deviceInfo.isMobile)
                                    put("isTouchDevice", deviceInfo.isTouchDevice)
                                    put("isSamsung", deviceInfo.isSamsung)
                                    put("isGalaxyS25Family", deviceInfo.isGalaxyS25Family)
                                    put("isGalaxyS26Family", deviceInfo.isGalaxyS26Family)
                                    put("modelHint", deviceInfo.modelHint ?: "")
                                    put("browser", deviceInfo.browser)
                                    put("orientation", deviceInfo.orientation)
                                    put("screenWidth", deviceInfo.screenWidth)
                                    put("screenHeight", deviceInfo.screenHeight)
                                    put("viewportWidth", deviceInfo.viewportWidth)
                                    put("viewportHeight", deviceInfo.viewportHeight)
                                })
                                put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
                            })
                        }
                        webSocket.send(clientInfoJson.toString())
                    }

                    // Start microphone streaming if permission is available
                    if (hasRecordAudioPermission()) {
                        startMicrophoneRecordingForSocket(webSocket)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                coroutineScope.launch(Dispatchers.Main) {
                    try {
                        val json = JSONObject(text)

                        // 1. Check Gemini Multimodal Live Server Content
                        val serverContent = json.optJSONObject("serverContent")
                        if (serverContent != null) {
                            val modelTurn = serverContent.optJSONObject("modelTurn")
                            if (modelTurn != null) {
                                val parts = modelTurn.optJSONArray("parts")
                                if (parts != null) {
                                    var extractedText = ""
                                    for (i in 0 until parts.length()) {
                                        val part = parts.getJSONObject(i)
                                        // Inline PCM Audio
                                        val inlineData = part.optJSONObject("inlineData")
                                        if (inlineData != null) {
                                            val audioData = inlineData.optString("data")
                                            val mime = inlineData.optString("mimeType")
                                            val rate = if (mime.contains("24000")) 24000 else 16000
                                            if (audioData.isNotBlank()) {
                                                playIncomingBase64Pcm(audioData, rate)
                                            }
                                        }
                                        // Text snippet
                                        val partText = part.optString("text")
                                        if (partText.isNotBlank()) {
                                            extractedText += partText
                                        }
                                    }
                                    if (extractedText.isNotBlank()) {
                                        lastReceivedText = extractedText
                                    }
                                }
                            }
                            return@launch
                        }

                        // 2. Generic WebSocket payload fallback
                        val audioData = json.optString("audio").ifBlank { json.optString("audioData") }
                        if (audioData.isNotBlank()) {
                            playIncomingBase64Pcm(audioData, 24000)
                        }

                        val msgText = json.optString("text").ifBlank { json.optString("message") }
                        if (msgText.isNotBlank()) {
                            lastReceivedText = msgText
                            // If no audio stream was provided, speak via local neural TTS
                            if (audioData.isBlank()) {
                                viewModel.voiceEngine.speak(msgText, playChimeBefore = false)
                            }
                        }
                    } catch (_: Exception) {
                        lastReceivedText = text
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                coroutineScope.launch(Dispatchers.Main) {
                    val code = response?.code
                    val msg = t.message ?: "Unknown socket error"
                    errorMessage = "WebSocket Error (Code $code): $msg. Switching to Direct ALISE Engine is recommended."
                    sessionState = AliseSessionState.ERROR
                    endSession()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                coroutineScope.launch(Dispatchers.Main) {
                    if (sessionState != AliseSessionState.IDLE) {
                        sessionState = AliseSessionState.IDLE
                    }
                }
            }
        }

        try {
            client.newWebSocket(request, listener)
        } catch (e: Exception) {
            errorMessage = "Unable to connect: ${e.message}"
            sessionState = AliseSessionState.ERROR
        }
    }

    // Start Session based on active Engine Mode
    fun startSession() {
        if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI) {
            errorMessage = null
            sessionState = AliseSessionState.ACTIVE
            uiMessage = "ALISE Direct Neural Core active."
            viewModel.voiceEngine.soundSynthesizer.playActivationChime()
            viewModel.voiceEngine.speak(
                "ALISE Live Neural Core active. Tactical voice and telemetry systems online. Ready for queries.",
                playChimeBefore = false
            )
            lastReceivedText = "ALISE Neural Voice Core initialized. Tap 'MIC LIVE' or type a command to speak."
        } else {
            startWebSocketSession()
        }
    }

    // Toggle Microphone
    fun toggleMicrophone() {
        if (!requestAudioPermissionIfNeeded()) return

        if (isMicrophoneActive) {
            isMicrophoneActive = false
            if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI) {
                viewModel.voiceEngine.stopListening()
                if (inputText.isNotBlank()) {
                    executeDirectAiQuery(inputText)
                    inputText = ""
                }
            } else {
                recordingJob?.cancel()
                recordingJob = null
            }
            uiMessage = "Microphone stopped."
        } else {
            isMicrophoneActive = true
            sessionState = AliseSessionState.LISTENING

            if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI) {
                viewModel.voiceEngine.startListeningCommandDirectly()
                uiMessage = "Listening... Speak to ALISE."
            } else {
                webSocketRef?.let { ws ->
                    startMicrophoneRecordingForSocket(ws)
                    uiMessage = "Streaming audio to socket..."
                } ?: run {
                    uiMessage = "Connect WebSocket before streaming mic."
                    isMicrophoneActive = false
                }
            }
        }
    }

    // Send Text Message
    fun sendTextMessage() {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) return

        if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI) {
            executeDirectAiQuery(trimmed)
            inputText = ""
            uiMessage = null
            return
        }

        // WebSocket Mode
        val ws = webSocketRef
        if (ws == null || (sessionState != AliseSessionState.ACTIVE && sessionState != AliseSessionState.SPEAKING && sessionState != AliseSessionState.LISTENING)) {
            // If websocket isn't connected, fallback seamlessly to direct engine so the user isn't blocked!
            uiMessage = "WebSocket offline: Processing through Direct Neural Core..."
            executeDirectAiQuery(trimmed)
            inputText = ""
            return
        }

        val isGeminiLive = wsUrl.contains("generativelanguage.googleapis.com")
        val payload = if (isGeminiLive) {
            JSONObject().apply {
                put("clientContent", JSONObject().apply {
                    put("turns", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", trimmed)
                                })
                            })
                        })
                    })
                    put("turnComplete", true)
                })
            }
        } else {
            JSONObject().apply {
                put("type", "text")
                put("text", trimmed)
            }
        }

        val sent = ws.send(payload.toString())
        if (sent) {
            lastReceivedText = "Query dispatched: $trimmed"
            inputText = ""
            uiMessage = "Message dispatched to ALISE."
        } else {
            uiMessage = "Failed to transmit message over socket. Switching to Direct AI..."
            executeDirectAiQuery(trimmed)
            inputText = ""
        }
    }

    // Test Audio Pipeline
    fun runAudioPipelineTest() {
        stopAudioPlayback()
        viewModel.voiceEngine.soundSynthesizer.playAcknowledgeTone()
        val testPhrase = "ALISE Live Audio System operational. Neural voice synthesis and hardware speaker output verified on ${deviceInfo.modelHint ?: "Samsung Galaxy"}."
        lastReceivedText = testPhrase
        viewModel.voiceEngine.speak(testPhrase, playChimeBefore = true)
        uiMessage = "Audio test signal triggered."
    }

    fun copyToClipboard() {
        val textToCopy = inputText.ifBlank { lastReceivedText }
        if (textToCopy.isEmpty()) {
            uiMessage = "No text available to copy."
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ALISE Console Text", textToCopy)
        clipboard.setPrimaryClip(clip)
        uiMessage = "Copied to clipboard."
    }

    fun pasteFromClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            val pastedText = item?.text?.toString() ?: ""
            if (pastedText.isNotBlank()) {
                inputText = if (inputText.isNotBlank()) "$inputText $pastedText" else pastedText
                uiMessage = "Clipboard text pasted."
            } else {
                uiMessage = "Clipboard is empty."
            }
        } catch (e: Exception) {
            uiMessage = "Paste failed: ${e.message}"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            endSession()
        }
    }

    val isConnected = sessionState == AliseSessionState.ACTIVE ||
            sessionState == AliseSessionState.SPEAKING ||
            sessionState == AliseSessionState.LISTENING

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020812))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TOP HEADER BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 14.dp,
                    borderColor = Color(0xFF17445E),
                    backgroundColor = Color(0xF207131F)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigateBack() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF76D4FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ALISE LIVE CONSOLE",
                                style = Typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(7.dp)
                                    .background(
                                        when (sessionState) {
                                            AliseSessionState.SPEAKING -> Color(0xFF76D4FF)
                                            AliseSessionState.ACTIVE -> Color(0xFF59E889)
                                            AliseSessionState.LISTENING -> Color(0xFFF59E0B)
                                            AliseSessionState.CONNECTING -> Color(0xFFF59E0B)
                                            AliseSessionState.ERROR -> Color(0xFFFF5264)
                                            AliseSessionState.IDLE -> Color(0xFF718399)
                                        }
                                    )
                            )
                        }
                        Text(
                            text = "$deviceLabel • ${activeVoiceModel.name}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF76D4FF),
                            fontSize = 8.5.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Test Audio Button
                    Button(
                        onClick = { runAudioPipelineTest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x332A5B80)),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF387AA6)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Test Audio",
                            tint = Color(0xFF76D4FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUDIO TEST",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF76D4FF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Fullscreen Button
                    Button(
                        onClick = { isFullscreen = !isFullscreen },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071522)),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27516B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color(0xFFAEE7FF),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // 2. ENGINE SELECTOR TABS (DIRECT NEURAL vs WEBSOCKET)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 10.dp,
                    borderColor = Color(0xFF17445E),
                    backgroundColor = Color(0xF207131F)
                )
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AliseLiveEngineMode.values().forEach { mode ->
                    val isSelected = selectedEngineMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF113247) else Color(0xFF06101B))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF4FA8D8) else Color(0xFF153347),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (selectedEngineMode != mode) {
                                    endSession()
                                    selectedEngineMode = mode
                                    uiMessage = "Switched to ${mode.title}."
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (mode) {
                                    AliseLiveEngineMode.DIRECT_AI -> Icons.Default.Psychology
                                    AliseLiveEngineMode.WEBSOCKET -> Icons.Default.Bolt
                                    AliseLiveEngineMode.MARK_LIV_BACKEND -> Icons.Default.SmartToy
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF76D4FF) else Color(0xFF718399),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.title,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF718399),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // WEBSOCKET & MARK-LIV BACKEND CONFIGURATION
            if (selectedEngineMode == AliseLiveEngineMode.WEBSOCKET || selectedEngineMode == AliseLiveEngineMode.MARK_LIV_BACKEND) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .obsidianGlassPanel(
                                cornerRadius = 12.dp,
                                borderColor = Color(0xFF17445E),
                                backgroundColor = Color(0xF207131F)
                            )
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WEBSOCKET ALISE SERVER ENDPOINT",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF718399),
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "AUTO-KEY ATTACHED",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF59E889),
                                    fontSize = 7.5.sp
                                )
                            }

                            BasicTextField(
                                value = wsUrl,
                                onValueChange = { wsUrl = it },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF76D4FF)
                                ),
                                cursorBrush = SolidColor(Color(0xFF76D4FF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF06101B))
                                    .border(1.dp, Color(0xFF14344A), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            // STATUS GRID (6 CARDS)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 12.dp,
                            borderColor = Color(0xFF17445E),
                            backgroundColor = Color(0xF207131F)
                        )
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusCard(
                                label = "Session",
                                value = sessionState.name,
                                color = when (sessionState) {
                                    AliseSessionState.ACTIVE -> Color(0xFF59E889)
                                    AliseSessionState.SPEAKING -> Color(0xFF76D4FF)
                                    AliseSessionState.LISTENING -> Color(0xFFF59E0B)
                                    AliseSessionState.CONNECTING -> Color(0xFFF59E0B)
                                    AliseSessionState.ERROR -> Color(0xFFFF5264)
                                    AliseSessionState.IDLE -> Color(0xFF718399)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            StatusCard(
                                label = "Engine",
                                value = if (selectedEngineMode == AliseLiveEngineMode.DIRECT_AI) "DIRECT NEURAL" else "WEBSOCKET",
                                color = Color(0xFF76D4FF),
                                modifier = Modifier.weight(1f)
                            )

                            StatusCard(
                                label = "Voice Model",
                                value = activeVoiceModel.name.take(13),
                                color = Color(0xFF59E889),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusCard(
                                label = "Microphone",
                                value = if (isMicrophoneActive) "LISTENING (MIC ON)" else "MIC OFF",
                                color = if (isMicrophoneActive) Color(0xFFF59E0B) else Color(0xFF718399),
                                modifier = Modifier.weight(1f)
                            )
                            StatusCard(
                                label = "Orientation",
                                value = deviceInfo.orientation.uppercase(),
                                color = Color(0xFF59E889),
                                modifier = Modifier.weight(1f)
                            )
                            StatusCard(
                                label = "Screen",
                                value = "${deviceInfo.screenWidth} × ${deviceInfo.screenHeight}",
                                color = Color(0xFF59E889),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // PRIMARY ACTION CONTROLS STRIP
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 12.dp,
                            borderColor = Color(0xFF17445E),
                            backgroundColor = Color(0xF207131F)
                        )
                        .padding(10.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // START / END SESSION
                        Button(
                            onClick = {
                                if (isConnected || sessionState == AliseSessionState.CONNECTING) {
                                    endSession()
                                } else {
                                    startSession()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) Color(0x33FF5264) else Color(0x3359E889)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isConnected) Color(0xFFFF5264) else Color(0xFF3BA966)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Stop else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isConnected) Color(0xFFFF5264) else Color(0xFF59E889),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isConnected || sessionState == AliseSessionState.CONNECTING) "END SESSION" else "START ALISE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFFFF5264) else Color(0xFF59E889),
                                fontSize = 10.sp
                            )
                        }

                        // MIC LIVE (TOGGLE VOICE INPUT)
                        Button(
                            onClick = { toggleMicrophone() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMicrophoneActive) Color(0x33F59E0B) else Color(0xFF071522)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isMicrophoneActive) Color(0xFFF59E0B) else Color(0xFF27516B)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isMicrophoneActive) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic",
                                tint = if (isMicrophoneActive) Color(0xFFF59E0B) else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMicrophoneActive) "MIC LIVE" else "MIC OFF",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isMicrophoneActive) Color(0xFFF59E0B) else Color.White,
                                fontSize = 10.sp
                            )
                        }

                        // BARGE IN (HALT AUDIO)
                        Button(
                            onClick = { stopAudioPlayback() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FF5264)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD84655)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "BARGE IN",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5264),
                                fontSize = 10.sp
                            )
                        }

                        // COPY
                        Button(
                            onClick = { copyToClipboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071522)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27516B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "COPY",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.5.sp
                            )
                        }

                        // PASTE
                        Button(
                            onClick = { pasteFromClipboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071522)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27516B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PASTE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            // BIOLUMINESCENT ALIEN NEURAL CORE VISUALIZER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alienSymbiontSurface(
                            cornerRadius = 24.dp,
                            glassColor = Color(0xE6081226),
                            borderColor = Color(0x6600F5FF),
                            borderWidth = 1.2.dp,
                            accentGlow = if (sessionState == AliseSessionState.SPEAKING) AlienBioluminescentMagenta else AlienEmpathyCyan
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CELESTIAL HARMONY NEURAL CORE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AlienEmpathyCyan,
                                fontSize = 8.5.sp,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            AlienHumanBadge(
                                text = if (sessionState == AliseSessionState.SPEAKING) "ALISE SPEAKING"
                                else if (sessionState == AliseSessionState.LISTENING) "LISTENING TO HUMAN"
                                else if (isConnected) "SYMBIONT SYNCED" else "AWAITING LINK",
                                color = if (sessionState == AliseSessionState.SPEAKING) AlienBioluminescentMagenta
                                else if (sessionState == AliseSessionState.LISTENING) AlienHarmonyGold
                                else if (isConnected) AlienCelestialEmerald else Color.Gray
                            )
                        }

                        // Living Central Bioluminescent Orb
                        AlienBioluminescentOrb(
                            amplitude = audioAmp,
                            isSpeaking = sessionState == AliseSessionState.SPEAKING,
                            isListening = sessionState == AliseSessionState.LISTENING,
                            isConnected = isConnected,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = "DESIGNED BY BENEVOLENT STELLAR VISITORS • HUMANITY ALLIANCE",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 7.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // CHAT OUTPUT & WAVEFORM VISUALIZER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 14.dp,
                            borderColor = Color(0xFF17445E),
                            backgroundColor = Color(0xF207131F)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALISE OUTPUT & AUDIO TELEMETRY",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF718399),
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )

                            if (sessionState == AliseSessionState.SPEAKING) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFF76D4FF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SPEAKING ALOUD",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF76D4FF),
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Holographic Voice Waveform HUD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF040B14))
                                .border(1.dp, Color(0xFF163A51), RoundedCornerShape(8.dp))
                        ) {
                            HolographicVoiceWaveformHud(
                                amplitude = audioAmp,
                                voiceState = voiceState,
                                activeVoiceName = activeVoiceModel.name,
                                heightDp = 72.dp,
                                showControls = false,
                                showTelemetryReadout = false,
                                onTestVoiceClick = { runAudioPipelineTest() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Terminal Text Output Area with Glowing Neon Text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF06101B))
                                .border(1.2.dp, AlienEmpathyCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            GlowingNeonText(
                                text = lastReceivedText.ifBlank { "ALISE Neural Console active. Awaiting voice input or prompt..." },
                                baseColor = AlienEmpathyCyan,
                                glowSecondaryColor = AlienBioluminescentMagenta,
                                fontSize = 11f,
                                fontWeight = FontWeight.SemiBold,
                                isFlowing = true
                            )
                        }

                        // Overlay Tactical Code Terminal Component
                        TacticalCodeTerminalOverlay(
                            isExpandedDefault = false,
                            onExecuteCommand = { cmd ->
                                inputText = cmd
                                sendTextMessage()
                            }
                        )

                        // Quick Test Chips
                        Text(
                            text = "TACTICAL QUICK PROMPTS",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF718399),
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "System Status",
                                "Run Diagnostics",
                                "Battery & Thermals",
                                "Identify Yourself"
                            ).forEach { prompt ->
                                SuggestionChip(
                                    onClick = {
                                        inputText = prompt
                                        sendTextMessage()
                                    },
                                    label = {
                                        Text(
                                            text = prompt,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.5.sp,
                                            color = Color(0xFF76D4FF)
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Color(0xFF071828)
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = Color(0xFF1E4C6D)
                                    )
                                )
                            }
                        }

                        Text(
                            text = "MESSAGE / CLIPBOARD INPUT",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF718399),
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )

                        // INPUT TEXT FIELD
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.White
                            ),
                            cursorBrush = SolidColor(Color(0xFF76D4FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF06101B))
                                .border(1.dp, Color(0xFF17445E), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "Ask ALISE anything, speak with 'MIC LIVE', or paste text...",
                                            style = Typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF718399).copy(alpha = 0.6f),
                                            fontSize = 10.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        // SEND BUTTON (Always accessible, never blocks the user)
                        Button(
                            onClick = { sendTextMessage() },
                            enabled = inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5CBBEA),
                                disabledContainerColor = Color(0xFF071522)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) Color.Black else Color(0xFF526577),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEND TO ALISE (LIVE AUDIO & TEXT)",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (inputText.isNotBlank()) Color.Black else Color(0xFF526577),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }

            // SYSTEM STATUS TOAST MESSAGES
            if (uiMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A1A29))
                            .border(1.dp, Color(0xFF17445E), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF59E889),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiMessage!!,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFBBDCF0),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF270B11))
                            .border(1.dp, Color(0xFF812C36), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5264),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = errorMessage!!,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFF9EA9),
                                    fontSize = 8.5.sp
                                )
                                if (selectedEngineMode == AliseLiveEngineMode.WEBSOCKET) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap 'ALISE DIRECT NEURAL CORE' above to switch to on-device neural voice.",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF76D4FF),
                                        fontSize = 8.sp,
                                        modifier = Modifier.clickable {
                                            selectedEngineMode = AliseLiveEngineMode.DIRECT_AI
                                            startSession()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM CLIENT ENVIRONMENT SENT SUMMARY
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 10.dp,
                            borderColor = Color(0xFF17445E),
                            backgroundColor = Color(0xF207131F)
                        )
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "CLIENT ENVIRONMENT SENT TO ALISE",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF718399),
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "$deviceLabel • ${deviceInfo.orientation} • viewport ${deviceInfo.viewportWidth}×${deviceInfo.viewportHeight} • touch ${if (deviceInfo.isTouchDevice) "yes" else "no"} • fullscreen ${if (deviceInfo.fullscreenActive) "active" else "inactive"}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF76D4FF),
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xB3020812))
            .border(1.dp, Color(0xFF14344A), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF718399),
                fontSize = 7.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 8.5.sp
            )
        }
    }
}
