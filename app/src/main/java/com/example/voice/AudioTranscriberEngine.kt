package com.example.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiInlineData
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.math.abs
import kotlin.math.max

sealed class AudioTranscriptionState {
    object Idle : AudioTranscriptionState()
    data class Recording(val durationMs: Long, val amplitude: Float) : AudioTranscriptionState()
    data class Transcribing(val modelUsed: String = "gemini-3.5-transcribe") : AudioTranscriptionState()
    data class Success(val transcript: String, val latencyMs: Long, val modelUsed: String) : AudioTranscriptionState()
    data class Error(val message: String) : AudioTranscriptionState()
}

class AudioTranscriberEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "AudioTranscriberEngine"
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val pcmOutputStream = ByteArrayOutputStream()

    private val _transcriptionState = MutableStateFlow<AudioTranscriptionState>(AudioTranscriptionState.Idle)
    val transcriptionState: StateFlow<AudioTranscriptionState> = _transcriptionState.asStateFlow()

    private val _liveAmplitude = MutableStateFlow(0f)
    val liveAmplitude: StateFlow<Float> = _liveAmplitude.asStateFlow()

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        if (_transcriptionState.value is AudioTranscriptionState.Recording) {
            return false
        }

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = max(minBufferSize, sampleRate * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _transcriptionState.value = AudioTranscriptionState.Error("AudioRecord initialization failed")
                return false
            }

            pcmOutputStream.reset()
            audioRecord?.startRecording()

            val startTime = System.currentTimeMillis()
            _transcriptionState.value = AudioTranscriptionState.Recording(0L, 0f)

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ShortArray(1024)
                while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        var maxAmp = 0
                        for (i in 0 until readCount) {
                            val sample = buffer[i]
                            pcmOutputStream.write(sample.toInt() and 0xFF)
                            pcmOutputStream.write((sample.toInt() shr 8) and 0xFF)
                            val absVal = abs(sample.toInt())
                            if (absVal > maxAmp) maxAmp = absVal
                        }
                        val normalizedAmp = (maxAmp / 32768.0f).coerceIn(0f, 1f)
                        _liveAmplitude.value = normalizedAmp
                        val currentDuration = System.currentTimeMillis() - startTime
                        _transcriptionState.value = AudioTranscriptionState.Recording(currentDuration, normalizedAmp)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start audio recording: ${e.message}", e)
            _transcriptionState.value = AudioTranscriptionState.Error("Microphone recording error: ${e.message}")
            return false
        }
    }

    fun stopAndTranscribe(
        onResult: ((transcript: String, latencyMs: Long) -> Unit)? = null
    ) {
        if (_transcriptionState.value !is AudioTranscriptionState.Recording) return

        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _liveAmplitude.value = 0f

        val pcmData = pcmOutputStream.toByteArray()
        if (pcmData.isEmpty()) {
            _transcriptionState.value = AudioTranscriptionState.Error("No audio data captured")
            return
        }

        _transcriptionState.value = AudioTranscriptionState.Transcribing("gemini-3.5-transcribe")

        scope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val wavBytes = createWavFile(pcmData, sampleRate, 1, 16)
                val base64Audio = Base64.encodeToString(wavBytes, Base64.NO_WRAP)

                val transcript = if (GeminiClient.hasValidApiKey()) {
                    transcribeWithGemini(base64Audio)
                } else {
                    "Simulated transcription: Gemini API key required for full live cloud transcription."
                }

                val latency = System.currentTimeMillis() - startTime
                _lastTranscript.value = transcript
                _transcriptionState.value = AudioTranscriptionState.Success(
                    transcript = transcript,
                    latencyMs = latency,
                    modelUsed = "gemini-3.5-transcribe"
                )

                withContext(Dispatchers.Main) {
                    onResult?.invoke(transcript, latency)
                }
            } catch (e: Exception) {
                Log.e(tag, "Audio transcription failed: ${e.message}", e)
                _transcriptionState.value = AudioTranscriptionState.Error("Transcription failed: ${e.message}")
            }
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _liveAmplitude.value = 0f
        pcmOutputStream.reset()
        _transcriptionState.value = AudioTranscriptionState.Idle
    }

    private suspend fun transcribeWithGemini(base64AudioWav: String): String {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(
                            text = "Please transcribe the following audio recording accurately into text. Output ONLY the verbatim transcribed speech with proper punctuation and capitalization, nothing else."
                        ),
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "audio/wav",
                                data = base64AudioWav
                            )
                        )
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.1f,
                maxOutputTokens = 2000
            )
        )

        // 1. Primary: Call gemini-3.5-transcribe
        return try {
            val response = GeminiClient.apiService.transcribeAudio(BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!text.isNullOrBlank()) {
                text
            } else {
                throw IllegalStateException("Empty transcription from gemini-3.5-transcribe")
            }
        } catch (e1: Exception) {
            Log.w(tag, "gemini-3.5-transcribe failed (${e1.message}), trying fallback...")
            // 2. Fallback: Call gemini-3.5-flash with audio part
            try {
                val fallbackResponse = GeminiClient.apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
                fallbackResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    ?: "Transcription completed with no detected spoken words."
            } catch (e2: Exception) {
                Log.e(tag, "Gemini audio transcription fallback failed: ${e2.message}")
                "Audio recorded (${pcmOutputStream.size() / 1024} KB). Transcription error: ${e2.message}"
            }
        }
    }

    private fun createWavFile(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val dos = DataOutputStream(out)

        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8

        // RIFF header
        dos.writeBytes("RIFF")
        dos.writeInt(Integer.reverseBytes(totalDataLen))
        dos.writeBytes("WAVE")

        // fmt chunk
        dos.writeBytes("fmt ")
        dos.writeInt(Integer.reverseBytes(16)) // Subchunk1Size for PCM
        dos.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat 1 = PCM
        dos.writeShort(java.lang.Short.reverseBytes(channels.toShort()).toInt())
        dos.writeInt(Integer.reverseBytes(sampleRate))
        dos.writeInt(Integer.reverseBytes(byteRate))
        dos.writeShort(java.lang.Short.reverseBytes((channels * bitsPerSample / 8).toShort()).toInt()) // BlockAlign
        dos.writeShort(java.lang.Short.reverseBytes(bitsPerSample.toShort()).toInt())

        // data chunk
        dos.writeBytes("data")
        dos.writeInt(Integer.reverseBytes(pcmData.size))
        dos.write(pcmData)
        dos.flush()

        return out.toByteArray()
    }
}
