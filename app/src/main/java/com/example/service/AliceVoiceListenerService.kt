package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceListenerState(
    val isRunning: Boolean = false,
    val isListening: Boolean = false,
    val lastRecognizedText: String = "",
    val lastTriggeredAction: String = "",
    val audioAmplitude: Float = 0f,
    val triggerCount: Int = 0,
    val errorMessage: String? = null
)

class AliceVoiceListenerService : Service(), RecognitionListener {

    companion object {
        private const val TAG = "AliceVoiceListener"
        const val CHANNEL_ID = "alice_voice_listener_channel"
        const val NOTIFICATION_ID = 2002

        const val ACTION_START_LISTENING = "com.example.service.action.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.example.service.action.STOP_LISTENING"
        const val ACTION_TOGGLE_LISTENING = "com.example.service.action.TOGGLE_LISTENING"

        private val _listenerState = MutableStateFlow(VoiceListenerState())
        val listenerState: StateFlow<VoiceListenerState> = _listenerState.asStateFlow()

        // Global command dispatcher wired to active ViewModel or background handler
        private var commandDispatcher: ((String) -> Unit)? = null
        private var protocolDispatcher: ((String) -> Unit)? = null

        fun setCommandDispatcher(dispatcher: ((String) -> Unit)?) {
            commandDispatcher = dispatcher
        }

        fun setProtocolDispatcher(dispatcher: ((String) -> Unit)?) {
            protocolDispatcher = dispatcher
        }

        fun start(context: Context) {
            val intent = Intent(context, AliceVoiceListenerService::class.java).apply {
                action = ACTION_START_LISTENING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AliceVoiceListenerService::class.java).apply {
                action = ACTION_STOP_LISTENING
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            if (_listenerState.value.isRunning) {
                stop(context)
            } else {
                start(context)
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerActive = false
    private var consecutiveErrors = 0
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "AliceVoiceListenerService created.")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_LISTENING -> {
                shutdownService()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_LISTENING -> {
                isPaused = !isPaused
                if (isPaused) {
                    stopRecognition()
                    updateNotification("Voice Listener Paused", "Tap to resume keyword listening")
                    _listenerState.value = _listenerState.value.copy(isListening = false)
                } else {
                    startRecognitionLoop()
                    updateNotification("Listening for Keywords", "ALICE tactical protocols & commands armed")
                }
                return START_STICKY
            }
            else -> {
                startInForeground()
                startRecognitionLoop()
                return START_STICKY
            }
        }
    }

    private fun startInForeground() {
        val notification = buildNotification(
            title = "ALICE Voice Listener Active",
            content = "Monitoring for protocols & system commands"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        _listenerState.value = _listenerState.value.copy(
            isRunning = true,
            errorMessage = null
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ALICE Voice Protocol Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous background speech recognizer for ALICE tactical keywords and system protocols."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AliceVoiceListenerService::class.java).apply {
            action = ACTION_TOGGLE_LISTENING
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AliceVoiceListenerService::class.java).apply {
            action = ACTION_STOP_LISTENING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                togglePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Service",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    private fun startRecognitionLoop() {
        if (isPaused) return
        mainHandler.post {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                _listenerState.value = _listenerState.value.copy(
                    isListening = false,
                    errorMessage = "RECORD_AUDIO permission required."
                )
                return@post
            }

            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                _listenerState.value = _listenerState.value.copy(
                    isListening = false,
                    errorMessage = "SpeechRecognizer not available on device."
                )
                return@post
            }

            stopRecognition()

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(this@AliceVoiceListenerService)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                isRecognizerActive = true
                _listenerState.value = _listenerState.value.copy(
                    isListening = true,
                    errorMessage = null
                )
                speechRecognizer?.startListening(intent)
                Log.d(TAG, "SpeechRecognizer started listening.")
            } catch (e: Exception) {
                isRecognizerActive = false
                Log.e(TAG, "Failed to initialize SpeechRecognizer: ${e.message}", e)
                _listenerState.value = _listenerState.value.copy(
                    isListening = false,
                    errorMessage = "Failed to start: ${e.message}"
                )
                scheduleRestart(2500)
            }
        }
    }

    private fun stopRecognition() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning up speech recognizer: ${e.message}")
            } finally {
                speechRecognizer = null
                isRecognizerActive = false
            }
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        if (isPaused || !_listenerState.value.isRunning) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            startRecognitionLoop()
        }, delayMs)
    }

    // ============================================================
    // RecognitionListener Implementation
    // ============================================================

    override fun onReadyForSpeech(params: Bundle?) {
        _listenerState.value = _listenerState.value.copy(isListening = true)
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _listenerState.value = _listenerState.value.copy(audioAmplitude = normalized)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _listenerState.value = _listenerState.value.copy(
            isListening = false,
            audioAmplitude = 0f
        )
    }

    override fun onError(error: Int) {
        isRecognizerActive = false
        consecutiveErrors++
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            else -> "Code $error"
        }
        Log.d(TAG, "SpeechRecognizer error: $errorMsg (consecutive=$consecutiveErrors)")

        _listenerState.value = _listenerState.value.copy(
            isListening = false,
            audioAmplitude = 0f
        )

        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            _listenerState.value = _listenerState.value.copy(errorMessage = "Microphone permission required")
            return
        }

        // Automatic restart backoff for continuous listener loop
        val backoff = when {
            consecutiveErrors > 6 -> 4000L
            consecutiveErrors > 3 -> 2000L
            else -> 600L
        }
        scheduleRestart(backoff)
    }

    override fun onResults(results: Bundle?) {
        isRecognizerActive = false
        consecutiveErrors = 0

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: ""

        if (text.isNotBlank()) {
            Log.i(TAG, "Recognized text: '$text'")
            processRecognizedSpeech(text)
        }

        // Keep loop continuous
        scheduleRestart(400)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()?.trim() ?: ""
        if (partial.isNotBlank()) {
            _listenerState.value = _listenerState.value.copy(lastRecognizedText = partial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    // ============================================================
    // Tactical Keyword & Protocol Evaluator
    // ============================================================

    private fun processRecognizedSpeech(phrase: String) {
        val lower = phrase.lowercase(Locale.US).trim()
        var triggeredAction = ""

        // 1. Direct Protocol Keyword Matcher
        val matchedProtocolCode = when {
            lower.contains("bitcoin oracle") || lower.contains("btc oracle") || lower.contains("crypto oracle") || lower.contains("hubcoin") || lower.contains("btc telemetry") -> "BTC_ORACLE_TELEMETRY"
            lower.contains("wifi defense") || lower.contains("wi-fi defense") || lower.contains("wifi shield") || lower.contains("wifi sanctuary") -> "S25_WIFI_DEFENSE"
            lower.contains("dns sanctuary") || lower.contains("dns flush") || lower.contains("dns probe") || lower.contains("sanitize dns") -> "S25_DNS_SANCTUARY"
            lower.contains("defense lockdown") || lower.contains("tactical lockdown") || lower.contains("alert lockdown") -> "TACTICAL_DEFENSE_LOCKDOWN"
            lower.contains("clean slate") || lower.contains("purge cache") || lower.contains("ram recalibrate") -> "S25_CLEAN_SLATE"
            lower.contains("battery saver") || lower.contains("ultra battery") -> "S25_BATTERY_SAVER"
            lower.contains("sentry mode") || lower.contains("optical sentry") || lower.contains("perimeter strobe") -> "S25_SENTRY_MODE"
            lower.contains("deep focus") || lower.contains("focus mode") -> "S25_DEEP_FOCUS"
            lower.contains("diagnostic scan") || lower.contains("hardware benchmark") || lower.contains("system health check") -> "S25_DIAGNOSTIC_SCAN"
            lower.contains("stealth mode") || lower.contains("silent protocol") -> "S25_STEALTH_MODE"
            lower.contains("sonar ping") || lower.contains("ping sonar") || lower.contains("lost phone sonar") -> "ACOUSTIC_SONAR_PING"
            lower.contains("gps perimeter lock") || lower.contains("gps lock") || lower.contains("perimeter anchor") -> "GPS_PERIMETER_LOCK"
            lower.contains("threat purge") || lower.contains("network threat") -> "NETWORK_THREAT_PURGE"
            lower.startsWith("protocol ") -> {
                val protocolName = lower.removePrefix("protocol ").trim()
                mapGenericProtocolNameToCode(protocolName)
            }
            else -> null
        }

        if (matchedProtocolCode != null) {
            triggeredAction = "Protocol: $matchedProtocolCode"
            protocolDispatcher?.invoke(matchedProtocolCode)
            // Also invoke commandDispatcher as fallback
            commandDispatcher?.invoke("execute protocol $matchedProtocolCode")
        } else {
            // 2. Wake-word or direct system command
            val wakeWords = listOf("hey alice", "alice", "hey alise", "alise", "hey jarvis", "jarvis", "computer")
            val isWakeWordPrefixed = wakeWords.any { lower.startsWith(it) }
            val cleanCommand = if (isWakeWordPrefixed) {
                var stripped = lower
                for (w in wakeWords) {
                    if (stripped.startsWith(w)) {
                        stripped = stripped.removePrefix(w).trim()
                        break
                    }
                }
                stripped
            } else {
                lower
            }

            if (cleanCommand.isNotBlank()) {
                triggeredAction = "Command: $cleanCommand"
                commandDispatcher?.invoke(cleanCommand)
            }
        }

        _listenerState.value = _listenerState.value.copy(
            lastRecognizedText = phrase,
            lastTriggeredAction = triggeredAction.ifBlank { "Acknowledged: $phrase" },
            triggerCount = _listenerState.value.triggerCount + 1
        )

        updateNotification(
            title = "ALICE: $triggeredAction",
            content = "Recognized: \"$phrase\""
        )
    }

    private fun mapGenericProtocolNameToCode(name: String): String {
        val n = name.lowercase()
        return when {
            n.contains("bitcoin") || n.contains("oracle") || n.contains("btc") -> "BTC_ORACLE_TELEMETRY"
            n.contains("wifi") || n.contains("wi-fi") -> "S25_WIFI_DEFENSE"
            n.contains("dns") -> "S25_DNS_SANCTUARY"
            n.contains("lockdown") -> "TACTICAL_DEFENSE_LOCKDOWN"
            n.contains("clean") -> "S25_CLEAN_SLATE"
            n.contains("battery") -> "S25_BATTERY_SAVER"
            n.contains("sentry") -> "S25_SENTRY_MODE"
            n.contains("focus") -> "S25_DEEP_FOCUS"
            n.contains("diagnostic") || n.contains("health") -> "S25_DIAGNOSTIC_SCAN"
            n.contains("stealth") -> "S25_STEALTH_MODE"
            n.contains("sonar") -> "ACOUSTIC_SONAR_PING"
            n.contains("gps") || n.contains("perimeter") -> "GPS_PERIMETER_LOCK"
            n.contains("threat") || n.contains("purge") -> "NETWORK_THREAT_PURGE"
            else -> name.uppercase().replace(" ", "_")
        }
    }

    private fun shutdownService() {
        stopRecognition()
        mainHandler.removeCallbacksAndMessages(null)
        _listenerState.value = VoiceListenerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        shutdownService()
        Log.i(TAG, "AliceVoiceListenerService destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
