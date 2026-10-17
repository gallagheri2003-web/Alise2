package com.example.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object ListeningWakeWord : VoiceState()
    object ListeningCommand : VoiceState()
    data class Processing(val query: String) : VoiceState()
    data class Speaking(val response: String) : VoiceState()
    data class Executing(val actionName: String) : VoiceState()
    object Success : VoiceState()
    object Interrupted : VoiceState()
    data class Error(val message: String) : VoiceState()
}

class JarvisVoiceEngine(
    private val context: Context,
    private val onCommandReceived: (String) -> Unit
) : RecognitionListener {

    private val handler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("ALISE Voice Synthesis Engine operational.")
    val lastSpokenResponse: StateFlow<String> = _lastSpokenResponse.asStateFlow()

    val soundSynthesizer = JarvisSoundSynthesizer()

    private val voiceScope = CoroutineScope(Dispatchers.Default)
    private var speakingAmplitudeJob: kotlinx.coroutines.Job? = null

    // Authoritative single-session guard and error backoff counter
    private var isSpeechRecognizerActive = false
    private var isSpeakingSelf = false
    private var consecutiveErrorCount = 0
    private var shouldResumeWakeWordAfterSpeech = false

    private fun requestAssistantAudioFocus(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            bargeIn()
                        }
                    }
                    .build()
                audioFocusRequest = request
                audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            bargeIn()
                        }
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun abandonAssistantAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (_: Exception) {}
    }

    private fun startSpeakingAmplitudeSync() {
        speakingAmplitudeJob?.cancel()
        speakingAmplitudeJob = voiceScope.launch {
            var step = 0
            val random = java.util.Random(System.currentTimeMillis())
            while (_voiceState.value is VoiceState.Speaking) {
                step++
                val t = step * 0.035
                // Multi-harmonic vocal envelope with phoneme rhythm (syllabic bursts and short word pauses)
                val carrier = (kotlin.math.sin(t * 8.0) * 0.35 + kotlin.math.sin(t * 14.5) * 0.25 + kotlin.math.sin(t * 2.3) * 0.2).toFloat()
                val modulation = ((kotlin.math.sin(t * 3.5) + 1.0) * 0.5).toFloat()
                val flutter = (random.nextFloat() * 0.15f)
                val rawAmp = ((carrier.coerceAtLeast(0f) * modulation) + 0.28f + flutter).coerceIn(0.15f, 0.98f)
                _audioAmplitude.value = rawAmp
                kotlinx.coroutines.delay(30)
            }
            _audioAmplitude.value = 0f
        }
    }

    private fun stopSpeakingAmplitudeSync() {
        speakingAmplitudeJob?.cancel()
        speakingAmplitudeJob = null
        _audioAmplitude.value = 0f
    }

    // Default built-in voice models
    private val defaultModels = listOf(
        CustomVoiceModel(
            id = "alise_natural_slate",
            name = "ALISE Slate Natural",
            type = VoiceModelType.ON_DEVICE_NEURAL_TTS,
            description = "Natural US English voice calibrated for balanced everyday interaction",
            localeTag = "en_US",
            defaultPitch = 1.0f,
            defaultRate = 1.05f
        ),
        CustomVoiceModel(
            id = "chatgpt_conversational",
            name = "ChatGPT Neural Neutral",
            type = VoiceModelType.ON_DEVICE_NEURAL_TTS,
            description = "Articulate, highly conversational, unbiased and fluid assistant delivery",
            localeTag = "en_US",
            defaultPitch = 1.02f,
            defaultRate = 1.08f
        ),
        CustomVoiceModel(
            id = "field_operator_direct",
            name = "Tactical Field Operator",
            type = VoiceModelType.ON_DEVICE_NEURAL_TTS,
            description = "Deeper pitch, concise cadence, rapid telemetry & threat transmission",
            localeTag = "en_US",
            defaultPitch = 0.88f,
            defaultRate = 1.15f
        ),
        CustomVoiceModel(
            id = "technical_analyst",
            name = "Technical & Code Analyst",
            type = VoiceModelType.ON_DEVICE_NEURAL_TTS,
            description = "Precise pronunciation for programming syntax, hardware metrics & IP addresses",
            localeTag = "en_US",
            defaultPitch = 1.0f,
            defaultRate = 1.0f
        ),
        CustomVoiceModel(
            id = "british_tactician",
            name = "British Tactical Director",
            type = VoiceModelType.ON_DEVICE_NEURAL_TTS,
            description = "Crisp, authoritative UK English tactical cadence",
            localeTag = "en_GB",
            defaultPitch = 0.94f,
            defaultRate = 1.02f
        )
    )

    private val _voiceModels = MutableStateFlow<List<CustomVoiceModel>>(defaultModels)
    val voiceModels: StateFlow<List<CustomVoiceModel>> = _voiceModels.asStateFlow()

    private val _activeVoiceModel = MutableStateFlow<CustomVoiceModel>(defaultModels[0])
    val activeVoiceModel: StateFlow<CustomVoiceModel> = _activeVoiceModel.asStateFlow()

    private val _aiPersonalityMode = MutableStateFlow<AiPersonalityMode>(AiPersonalityMode.CHATGPT_UNFILTERED)
    val aiPersonalityMode: StateFlow<AiPersonalityMode> = _aiPersonalityMode.asStateFlow()

    // Global customization properties independent of Gemini/Models/API
    private val _isGlobalPitchRateLocked = MutableStateFlow(false)
    val isGlobalPitchRateLocked: StateFlow<Boolean> = _isGlobalPitchRateLocked.asStateFlow()

    private val _globalPitch = MutableStateFlow(1.0f)
    val globalPitch: StateFlow<Float> = _globalPitch.asStateFlow()

    private val _globalRate = MutableStateFlow(1.05f)
    val globalRate: StateFlow<Float> = _globalRate.asStateFlow()

    private val _isChimeEnabled = MutableStateFlow(true)
    val isChimeEnabled: StateFlow<Boolean> = _isChimeEnabled.asStateFlow()

    private val _ttsVolume = MutableStateFlow(1.0f)
    val ttsVolume: StateFlow<Float> = _ttsVolume.asStateFlow()

    fun togglePitchRateLock() {
        _isGlobalPitchRateLocked.value = !_isGlobalPitchRateLocked.value
        applyVoiceModel(_activeVoiceModel.value)
    }

    fun updateGlobalPitch(value: Float) {
        _globalPitch.value = value.coerceIn(0.5f, 2.0f)
        if (_isGlobalPitchRateLocked.value) {
            pitch = _globalPitch.value
        }
    }

    fun updateGlobalRate(value: Float) {
        _globalRate.value = value.coerceIn(0.5f, 2.5f)
        if (_isGlobalPitchRateLocked.value) {
            speechRate = _globalRate.value
        }
    }

    fun toggleChimeEnabled() {
        _isChimeEnabled.value = !_isChimeEnabled.value
    }

    fun updateTtsVolume(value: Float) {
        _ttsVolume.value = value.coerceIn(0.1f, 1.0f)
    }

    var pitch: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setPitch(field)
        }

    var speechRate: Float = 1.05f
        set(value) {
            field = value.coerceIn(0.5f, 2.5f)
            tts?.setSpeechRate(field)
        }

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                } catch (_: Exception) {}

                applyVoiceModel(_activeVoiceModel.value)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeakingSelf = true
                        // Temporarily pause speech recognizer during synthesis to eliminate self-triggering
                        stopListeningInternal(keepResumeIntent = true)
                        requestAssistantAudioFocus()
                        _voiceState.value = VoiceState.Speaking(_lastSpokenResponse.value)
                        startSpeakingAmplitudeSync()
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeakingSelf = false
                        stopSpeakingAmplitudeSync()
                        abandonAssistantAudioFocus()
                        _voiceState.value = VoiceState.Idle
                        // Wait for acoustic decay before restoring microphone listening
                        if (shouldResumeWakeWordAfterSpeech) {
                            handler.postDelayed({
                                if (!isSpeakingSelf && _voiceState.value is VoiceState.Idle) {
                                    startListeningForWakeWord()
                                }
                            }, 250)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeakingSelf = false
                        stopSpeakingAmplitudeSync()
                        abandonAssistantAudioFocus()
                        _voiceState.value = VoiceState.Idle
                    }
                })
            }
        }
    }

    fun applyVoiceModel(model: CustomVoiceModel) {
        _activeVoiceModel.value = model
        if (_isGlobalPitchRateLocked.value) {
            pitch = _globalPitch.value
            speechRate = _globalRate.value
        } else {
            pitch = model.defaultPitch
            speechRate = model.defaultRate
        }

        if (isTtsReady) {
            try {
                val loc = when (model.localeTag) {
                    "en_GB" -> Locale.UK
                    "en_US" -> Locale.US
                    else -> Locale.getDefault()
                }
                tts?.language = loc
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speechRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectVoiceModel(model: CustomVoiceModel) {
        applyVoiceModel(model)
    }

    fun selectPersonalityMode(mode: AiPersonalityMode) {
        _aiPersonalityMode.value = mode
    }

    fun addCustomVoiceModel(
        name: String,
        type: VoiceModelType,
        description: String,
        filePath: String? = null,
        apiVoiceId: String? = null,
        pitch: Float = 1.0f,
        rate: Float = 1.0f,
        fileSizeMb: Float = 14.5f
    ): CustomVoiceModel {
        val newModel = CustomVoiceModel(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            type = type,
            description = description,
            modelFilePath = filePath,
            apiVoiceId = apiVoiceId,
            defaultPitch = pitch,
            defaultRate = rate,
            isCustomUserUploaded = true,
            fileSizeMb = fileSizeMb
        )
        _voiceModels.value = _voiceModels.value + newModel
        applyVoiceModel(newModel)
        return newModel
    }

    fun deleteVoiceModel(id: String) {
        val current = _voiceModels.value.filterNot { it.id == id }
        _voiceModels.value = current
        if (_activeVoiceModel.value.id == id) {
            applyVoiceModel(defaultModels[0])
        }
    }

    fun testVoiceModel(testText: String = "Hello Ian. This is your personalized ALISE voice model running on Samsung Galaxy S25.") {
        speak(testText, playChimeBefore = true)
    }

    fun startListeningForWakeWord() {
        if (isSpeakingSelf) {
            // Guard: Never start mic while ALISE is currently vocalizing
            shouldResumeWakeWordAfterSpeech = true
            return
        }

        shouldResumeWakeWordAfterSpeech = true
        handler.post {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                _voiceState.value = VoiceState.Error("Microphone permission required")
                return@post
            }

            stopListeningInternal(keepResumeIntent = true)
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _voiceState.value = VoiceState.Error("Speech recognition unavailable on device")
                return@post
            }

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@JarvisVoiceEngine)
                }
                isSpeechRecognizerActive = true

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _voiceState.value = VoiceState.ListeningWakeWord
                _partialTranscript.value = "Listening for 'Hey ALISE'..."
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isSpeechRecognizerActive = false
                _voiceState.value = VoiceState.Error("Failed to start mic: ${e.message}")
            }
        }
    }

    fun startListeningCommandDirectly() {
        bargeIn()
        soundSynthesizer.playAcknowledgeTone()
        handler.post {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                _voiceState.value = VoiceState.Error("Microphone permission required")
                return@post
            }

            stopListeningInternal(keepResumeIntent = false)
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _voiceState.value = VoiceState.Error("Speech recognition unavailable")
                return@post
            }

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@JarvisVoiceEngine)
                }
                isSpeechRecognizerActive = true

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                _voiceState.value = VoiceState.ListeningCommand
                _partialTranscript.value = "Listening for tactical command..."
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isSpeechRecognizerActive = false
                _voiceState.value = VoiceState.Error("Microphone error: ${e.message}")
            }
        }
    }

    private fun stopListeningInternal(keepResumeIntent: Boolean = false) {
        handler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
            isSpeechRecognizerActive = false
            if (!keepResumeIntent) {
                shouldResumeWakeWordAfterSpeech = false
            }
            _audioAmplitude.value = 0f
        }
    }

    fun stopListening() {
        stopListeningInternal(keepResumeIntent = false)
        if (_voiceState.value is VoiceState.ListeningWakeWord || _voiceState.value is VoiceState.ListeningCommand) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun bargeIn() {
        stopSpeakingAmplitudeSync()
        abandonAssistantAudioFocus()
        isSpeakingSelf = false
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        if (_voiceState.value is VoiceState.Speaking) {
            _voiceState.value = VoiceState.Interrupted
            handler.postDelayed({
                if (_voiceState.value is VoiceState.Interrupted) {
                    _voiceState.value = VoiceState.Idle
                }
            }, 300)
        }
    }

    fun speak(text: String, playChimeBefore: Boolean = false) {
        _lastSpokenResponse.value = text
        _voiceState.value = VoiceState.Speaking(text)
        startSpeakingAmplitudeSync()
        if (playChimeBefore && _isChimeEnabled.value) {
            soundSynthesizer.playAcknowledgeTone()
        }
        if (isTtsReady) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ALISE_TTS_${System.currentTimeMillis()}")
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, _ttsVolume.value)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "ALISE_TTS_${System.currentTimeMillis()}")
        }
    }

    fun shutdown() {
        stopListeningInternal(keepResumeIntent = false)
        stopSpeakingAmplitudeSync()
        abandonAssistantAudioFocus()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // --- RecognitionListener Callbacks ---

    override fun onReadyForSpeech(params: Bundle?) {
        consecutiveErrorCount = 0
    }

    override fun onBeginningOfSpeech() {
        if (_voiceState.value is VoiceState.Speaking) {
            bargeIn()
        }
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (!isSpeakingSelf) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioAmplitude.value = normalized
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _audioAmplitude.value = 0f
    }

    override fun onError(error: Int) {
        _audioAmplitude.value = 0f
        isSpeechRecognizerActive = false

        if (isSpeakingSelf) {
            // Echo cancellation suppression: ignored since mic was stopped during TTS
            return
        }

        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            _voiceState.value = VoiceState.Error("Microphone permission required")
            shouldResumeWakeWordAfterSpeech = false
            return
        }

        consecutiveErrorCount++
        val currentState = _voiceState.value

        if (currentState is VoiceState.ListeningWakeWord && shouldResumeWakeWordAfterSpeech) {
            if (consecutiveErrorCount <= 4) {
                // Exponential backoff to avoid activation loops, clicking, and rapid beeps
                val delayMs = (consecutiveErrorCount * 900L).coerceIn(900L, 4500L)
                handler.postDelayed({
                    if (!isSpeakingSelf && shouldResumeWakeWordAfterSpeech) {
                        startListeningForWakeWord()
                    }
                }, delayMs)
            } else {
                _voiceState.value = VoiceState.Idle
                consecutiveErrorCount = 0
            }
        } else {
            _voiceState.value = VoiceState.Idle
        }
    }

    override fun onResults(results: Bundle?) {
        _audioAmplitude.value = 0f
        isSpeechRecognizerActive = false
        consecutiveErrorCount = 0

        if (isSpeakingSelf) return

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""

        if (text.isBlank()) {
            if (_voiceState.value is VoiceState.ListeningWakeWord && shouldResumeWakeWordAfterSpeech) {
                startListeningForWakeWord()
            } else {
                _voiceState.value = VoiceState.Idle
            }
            return
        }

        _partialTranscript.value = text

        val upper = text.uppercase().trim()
        val wakeWordVariants = listOf(
            "HEY ALISE", "ALISE", "OK ALISE", "HI ALISE",
            "HEY ALICE", "ALICE", "OK ALICE",
            "HEY JARVIS", "JARVIS"
        )
        val triggeredWakeWord = wakeWordVariants.any { upper.startsWith(it) || upper.contains(it) }

        if (_voiceState.value is VoiceState.ListeningWakeWord) {
            if (triggeredWakeWord) {
                var command = upper
                for (w in wakeWordVariants) {
                    if (command.contains(w)) {
                        command = command.substringAfter(w).trim()
                        break
                    }
                }
                if (command.isNotBlank()) {
                    _voiceState.value = VoiceState.Processing(command)
                    onCommandReceived(command)
                } else {
                    speak("I'm listening, Ian.", playChimeBefore = true)
                    handler.postDelayed({
                        startListeningCommandDirectly()
                    }, 1100)
                }
            } else {
                startListeningForWakeWord()
            }
        } else {
            _voiceState.value = VoiceState.Processing(text)
            onCommandReceived(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        if (isSpeakingSelf) return
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            _partialTranscript.value = partial
            val upper = partial.uppercase()
            if (_voiceState.value is VoiceState.ListeningWakeWord) {
                if (upper.contains("HEY ALISE") || upper.contains("ALISE") || upper.contains("HEY JARVIS")) {
                    bargeIn()
                    soundSynthesizer.playAcknowledgeTone()
                }
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
