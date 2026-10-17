package com.example.voice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class JarvisSoundSynthesizer {

    private val sampleRate = 44100

    fun playActivationChime() {
        CoroutineScope(Dispatchers.Default).launch {
            // Dual frequency holographic power-up sweep: 520Hz -> 1040Hz
            val durationSec = 0.28
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 520.0 + (520.0 * progress)
                val envelope = sin(Math.PI * progress) // smooth attack/decay
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val angleHarmonic = 2.0 * Math.PI * i * (freq * 1.5) / sampleRate
                val value = (sin(angle) * 0.7 + sin(angleHarmonic) * 0.3) * envelope * Short.MAX_VALUE * 0.4
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playAcknowledgeTone() {
        CoroutineScope(Dispatchers.Default).launch {
            // Crisp tactical double-chirp: 880Hz then 1760Hz
            val durationSec = 0.16
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = if (progress < 0.45) 880.0 else 1760.0
                val envelope = (1.0 - progress) * sin(Math.PI * progress * 2.0).coerceAtLeast(0.0)
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val value = sin(angle) * envelope * Short.MAX_VALUE * 0.35
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playEngageSequence() {
        playActivationChime()
    }

    fun playProcessingDrone() {
        CoroutineScope(Dispatchers.Default).launch {
            // Subtle cybernetic processing ripple
            val durationSec = 0.35
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 440.0 + 40.0 * sin(2.0 * Math.PI * progress * 8.0)
                val envelope = sin(Math.PI * progress) * 0.25
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val value = sin(angle) * envelope * Short.MAX_VALUE
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playAlertPulse() {
        CoroutineScope(Dispatchers.Default).launch {
            // Tactical warning strobe tone
            val durationSec = 0.3
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 680.0
                val lfo = if ((i / (sampleRate / 20)) % 2 == 0) 1.0 else 0.2
                val envelope = (1.0 - progress)
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val value = sin(angle) * envelope * lfo * Short.MAX_VALUE * 0.4
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playAlertTone() {
        playAlertPulse()
    }

    fun playKeyClickTone() {
        CoroutineScope(Dispatchers.Default).launch {
            // Monospaced terminal mechanical keystroke pulse: 2400Hz sharp click
            val durationSec = 0.04
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val envelope = Math.exp(-18.0 * progress)
                val angle = 2.0 * Math.PI * i * 2400.0 / sampleRate
                val value = sin(angle) * envelope * Short.MAX_VALUE * 0.25
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playRepulsorTone() {
        CoroutineScope(Dispatchers.Default).launch {
            // Repulsor blast acoustic effect: pitch sweep down 1200Hz -> 180Hz
            val durationSec = 0.35
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 1200.0 * Math.exp(-3.0 * progress) + 180.0
                val envelope = Math.sin(Math.PI * Math.pow(progress, 0.5)) * (1.0 - progress)
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val value = sin(angle) * envelope * Short.MAX_VALUE * 0.45
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    fun playRadarSweepTone() {
        CoroutineScope(Dispatchers.Default).launch {
            // High ping radar chirp: 2400Hz
            val durationSec = 0.12
            val numSamples = (durationSec * sampleRate).toInt()
            val sample = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq = 2400.0 - (600.0 * progress)
                val envelope = Math.exp(-5.0 * progress)
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                val value = sin(angle) * envelope * Short.MAX_VALUE * 0.3
                sample[i] = value.toInt().toShort()
            }
            playPcmBuffer(sample)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep((buffer.size * 1000L) / sampleRate + 50)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
