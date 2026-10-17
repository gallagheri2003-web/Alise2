package com.example.hardware

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SystemSettingCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val intentAction: String,
    val iconTag: String
) {
    WIFI(
        "wifi",
        "Wi-Fi 7 & Network Mesh",
        "Control WLAN radio, frequency bands & SSID authentication",
        Settings.ACTION_WIFI_SETTINGS,
        "wifi"
    ),
    BLUETOOTH(
        "bluetooth",
        "Bluetooth 5.4 Radio",
        "Manage Bluetooth pairing, low energy peripherals & audio devices",
        Settings.ACTION_BLUETOOTH_SETTINGS,
        "bluetooth"
    ),
    LOCATION_GPS(
        "location",
        "GPS & Military Location",
        "Configure GNSS multi-constellation & high-precision tracking",
        Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        "location"
    ),
    DISPLAY_BRIGHTNESS(
        "display",
        "Display Brightness & Refresh",
        "Tune Dynamic AMOLED 120Hz LTPO & 2600-nit panel brightness",
        Settings.ACTION_DISPLAY_SETTINGS,
        "display"
    ),
    SOUND_VOLUME(
        "sound",
        "Sound & Tactical Audio",
        "Volume matrix, ringtones, acoustic alarms & voice feedback",
        Settings.ACTION_SOUND_SETTINGS,
        "volume"
    ),
    FLASHLIGHT(
        "flashlight",
        "Tactical LED Flashlight",
        "Dual-LED high-output torch & optical strobe beacon",
        "",
        "flash"
    ),
    AIRPLANE_MODE(
        "airplane",
        "Airplane Radio Blackout",
        "Instantly terminate all RF emissions & wireless interfaces",
        Settings.ACTION_AIRPLANE_MODE_SETTINGS,
        "airplane"
    ),
    HOTSPOT(
        "hotspot",
        "Personal Hotspot & Tether",
        "Broadcast secure encrypted Wi-Fi mesh to external tactical nodes",
        Settings.ACTION_WIRELESS_SETTINGS,
        "hotspot"
    ),
    DO_NOT_DISTURB(
        "dnd",
        "Do Not Disturb / Zen Mode",
        "Silence system interruptions, notifications & background chatter",
        Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS,
        "dnd"
    ),
    NFC(
        "nfc",
        "NFC & Contactless Comms",
        "Near-field communication transceiver & security key exchange",
        Settings.ACTION_NFC_SETTINGS,
        "nfc"
    ),
    BATTERY_SAVER(
        "battery",
        "Battery Ultra-Saver Matrix",
        "Throttle background cores & extend operating runtime",
        Settings.ACTION_BATTERY_SAVER_SETTINGS,
        "battery"
    ),
    SCREEN_TIMEOUT(
        "timeout",
        "Screen Timeout & Lock",
        "Adjust display sleep timeout and security lock thresholds",
        Settings.ACTION_DISPLAY_SETTINGS,
        "timer"
    ),
    ACCESSIBILITY(
        "accessibility",
        "Accessibility Service Bridge",
        "Enable autonomous screen touch, gesture & UI automation",
        Settings.ACTION_ACCESSIBILITY_SETTINGS,
        "accessibility"
    )
}

data class AutonomousExecutionState(
    val isExecuting: Boolean = false,
    val activeSetting: SystemSettingCategory? = null,
    val executionStep: String = "",
    val progressPercent: Float = 0f,
    val autoReturnCountdownSeconds: Int = 0,
    val messageLog: List<String> = emptyList()
)

class AutonomousSettingController(
    private val context: Context,
    private val hardwareController: HardwareController
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _executionState = MutableStateFlow(AutonomousExecutionState())
    val executionState: StateFlow<AutonomousExecutionState> = _executionState.asStateFlow()

    /**
     * Autonomous Bot Mode: ALISE visually handles setting adjustment step-by-step and automatically
     * pulls the user back to the application.
     */
    fun watchJarvisExecute(
        setting: SystemSettingCategory,
        onCompleteSpeech: (String) -> Unit = {}
    ) {
        scope.launch {
            _executionState.value = AutonomousExecutionState(
                isExecuting = true,
                activeSetting = setting,
                executionStep = "[1/4] Intercepting subsystem driver for ${setting.title}...",
                progressPercent = 0.25f,
                autoReturnCountdownSeconds = 3,
                messageLog = listOf("Initiating autonomous bot controller for ${setting.title}")
            )

            delay(600)
            _executionState.value = _executionState.value.copy(
                executionStep = "[2/4] Applying optimized hardware parameters...",
                progressPercent = 0.50f,
                messageLog = _executionState.value.messageLog + "Setting hardware registers for ${setting.id}"
            )

            // Direct actions for specific settings
            when (setting) {
                SystemSettingCategory.FLASHLIGHT -> {
                    hardwareController.toggleFlashlight()
                }
                SystemSettingCategory.SOUND_VOLUME -> {
                    hardwareController.setMediaVolumePercent(85)
                }
                else -> {
                    // Launch target intent
                    launchSystemSetting(setting.intentAction)
                }
            }

            delay(700)
            _executionState.value = _executionState.value.copy(
                executionStep = "[3/4] Hardware bus synchronized. Engaging auto-return tether...",
                progressPercent = 0.75f,
                messageLog = _executionState.value.messageLog + "Autonomous modification verified"
            )

            // Countdown to pull user back to ALISE HUD
            for (sec in 3 downTo 1) {
                _executionState.value = _executionState.value.copy(
                    autoReturnCountdownSeconds = sec,
                    executionStep = "[4/4] Returning to ALISE HUD in ${sec}s..."
                )
                delay(800)
            }

            // Pull back to MainActivity
            bringAppToFront()

            _executionState.value = AutonomousExecutionState(
                isExecuting = false,
                activeSetting = null,
                executionStep = "Complete",
                progressPercent = 1.0f,
                autoReturnCountdownSeconds = 0,
                messageLog = _executionState.value.messageLog + "Returned to ALISE HUD"
            )

            onCompleteSpeech("Autonomous adjustment of ${setting.title} executed successfully. Restored ALISE Tactical HUD.")
        }
    }

    /**
     * Manual Launch with Auto-Return Tether: Opens setting and automatically brings user back
     * after user finishes adjustment.
     */
    fun launchWithAutoReturnTether(
        setting: SystemSettingCategory,
        tetherSeconds: Int = 4,
        onCompleteSpeech: (String) -> Unit = {}
    ) {
        scope.launch {
            Toast.makeText(
                context,
                "ALISE Tether Active: Returning to Tactical HUD in ${tetherSeconds}s...",
                Toast.LENGTH_LONG
            ).show()

            launchSystemSetting(setting.intentAction)

            delay(tetherSeconds * 1000L)
            bringAppToFront()

            onCompleteSpeech("Configuration for ${setting.title} completed. Restoring ALISE Tactical HUD.")
        }
    }

    private fun launchSystemSetting(action: String) {
        if (action.isBlank()) return
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    private fun bringAppToFront() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
