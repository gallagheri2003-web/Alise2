package com.example.hardware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HardwareController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _mediaVolume = MutableStateFlow(getMediaVolumePercent())
    val mediaVolume: StateFlow<Int> = _mediaVolume.asStateFlow()

    init {
        cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                _isFlashlightOn.value = enabled
            }
        }, null)
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager?.cameraIdList?.firstOrNull()

            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId, enabled)
                _isFlashlightOn.value = enabled
                vibrateTactilePulse()
                true
            } else {
                false
            }
        } catch (e: CameraAccessException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!_isFlashlightOn.value)
    }

    fun strobeFlashlight(burstCount: Int = 4, intervalMs: Long = 120) {
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0 until burstCount) {
                setFlashlight(true)
                delay(intervalMs)
                setFlashlight(false)
                delay(intervalMs)
            }
        }
    }

    fun getMediaVolumePercent(): Int {
        val am = audioManager ?: return 50
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return (current * 100 / max).coerceIn(0, 100)
    }

    fun setMediaVolumePercent(percent: Int) {
        val am = audioManager ?: return
        val clamped = percent.coerceIn(0, 100)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (clamped * max / 100).coerceIn(0, max)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        _mediaVolume.value = clamped
        vibrateTactilePulse()
    }

    fun adjustVolumeUp() {
        val am = audioManager ?: return
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        _mediaVolume.value = getMediaVolumePercent()
        vibrateTactilePulse()
    }

    fun adjustVolumeDown() {
        val am = audioManager ?: return
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        _mediaVolume.value = getMediaVolumePercent()
        vibrateTactilePulse()
    }

    fun triggerMediaAction(action: String) {
        val keyEvent = when (action.uppercase()) {
            "PLAY", "RESUME" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "PAUSE", "STOP" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            "NEXT", "SKIP" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "PREV", "PREVIOUS" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyEvent))
        }
        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyEvent))
        }
        context.sendOrderedBroadcast(downIntent, null)
        context.sendOrderedBroadcast(upIntent, null)
        vibrateTactilePulse()
    }

    fun launchSpotify(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // Open Spotify web or search in browser/store
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val general = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(general)
        }
    }

    fun vibrateTactilePulse(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun vibrateTacticalAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 60, 60, 60, 120)
                val amplitudes = intArrayOf(0, 200, 0, 255, 0, 180)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }
}
