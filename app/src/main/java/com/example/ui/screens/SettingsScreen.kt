package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiLlmEngine
import com.example.hardware.SystemSettingCategory
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDirectory: () -> Unit = {},
    onNavigateToLiquidGlassSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val autonomousState by viewModel.settingController.executionState.collectAsState()
    val isTorchOn by viewModel.isFlashlightOn.collectAsState()
    val mediaVol by viewModel.mediaVolume.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricCoreLockEnabled.collectAsState()
    val isAutoLockBg by viewModel.isBiometricAutoLockBackground.collectAsState()
    val capability by viewModel.biometricCapability.collectAsState()

    var customApiKey by remember { mutableStateOf(AiLlmEngine.customLlamaApiKey) }
    var customEndpoint by remember { mutableStateOf(AiLlmEngine.customLlamaBaseUrl) }
    var selectedTetherDuration by remember { mutableFloatStateOf(4f) }

    fun getCategoryIcon(cat: SystemSettingCategory): ImageVector {
        return when (cat) {
            SystemSettingCategory.WIFI -> Icons.Default.Wifi
            SystemSettingCategory.BLUETOOTH -> Icons.Default.Bluetooth
            SystemSettingCategory.LOCATION_GPS -> Icons.Default.GpsFixed
            SystemSettingCategory.DISPLAY_BRIGHTNESS -> Icons.Default.BrightnessMedium
            SystemSettingCategory.SOUND_VOLUME -> Icons.Default.VolumeUp
            SystemSettingCategory.FLASHLIGHT -> Icons.Default.FlashOn
            SystemSettingCategory.AIRPLANE_MODE -> Icons.Default.AirplanemodeActive
            SystemSettingCategory.HOTSPOT -> Icons.Default.WifiTethering
            SystemSettingCategory.DO_NOT_DISTURB -> Icons.Default.DoNotDisturb
            SystemSettingCategory.NFC -> Icons.Default.Nfc
            SystemSettingCategory.BATTERY_SAVER -> Icons.Default.BatterySaver
            SystemSettingCategory.SCREEN_TIMEOUT -> Icons.Default.Timer
            SystemSettingCategory.ACCESSIBILITY -> Icons.Default.Accessibility
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP CONTROLLER HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xD9050B16)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldNominal)
                        )
                        Text(
                            text = "AUTONOMOUS SYSTEM SETTING CONTROLLER",
                            style = Typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "Every hardware switch has two execution modes: (1) Watch ALISE automate & auto-return, or (2) Launch setting with an auto-return tether.",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // BIOMETRIC HARDWARE SECURITY & CORE LOCK
        item {
            val activity = context as? FragmentActivity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = if (isBiometricEnabled) SkyAccent else Color(0x3338BDF8),
                        backgroundColor = Color(0xD9071020)
                    )
                    .padding(12.dp)
                    .testTag("biometric_settings_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isBiometricEnabled) EmeraldNominal else CrimsonDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CORE BIOMETRIC SECURITY",
                                style = Typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (capability.isAvailable) Color(0x224ADE80) else Color(0x22EF4444))
                                .border(1.dp, if (capability.isAvailable) EmeraldNominal else CrimsonDanger, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (capability.isAvailable) "SENSOR ACTIVE" else "STANDBY",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (capability.isAvailable) EmeraldNominal else CrimsonDanger
                            )
                        }
                    }

                    Text(
                        text = "Hardware biometric gate using Android's BiometricPrompt API with AES-256 session protection.",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 8.5.sp
                    )

                    // Switch 1: Biometric Core Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Require Biometric Verification",
                                style = Typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextPrimary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Secures core interface behind fingerprint/prompt",
                                style = Typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 8.sp
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricCoreLockEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldNominal,
                                checkedTrackColor = Color(0x444ADE80),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = Color(0x221E293B)
                            ),
                            modifier = Modifier.testTag("toggle_biometric_lock")
                        )
                    }

                    // Switch 2: Auto-Lock on Background
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-Lock on App Exit / Background",
                                style = Typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextPrimary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Locks immediately when switching apps",
                                style = Typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 8.sp
                            )
                        }
                        Switch(
                            checked = isAutoLockBg,
                            onCheckedChange = { viewModel.setBiometricAutoLockBackground(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SkyAccent,
                                checkedTrackColor = Color(0x4438BDF8),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = Color(0x221E293B)
                            ),
                            modifier = Modifier.testTag("toggle_autolock_background")
                        )
                    }

                    // Action Buttons: Test Prompt & Lock Now
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    viewModel.authenticateCoreInterface(activity) { success ->
                                        if (success) {
                                            Toast.makeText(context, "BiometricPrompt Verified!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "FragmentActivity unavailable", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("test_biometric_prompt_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = SkyBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TEST PROMPT",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.lockCoreInterface("SETTINGS_TRIGGER")
                                Toast.makeText(context, "JARVIS Core Locked", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("lock_core_now_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOCK CORE NOW",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        // ACTIVE AUTONOMOUS EXECUTION OVERLAY CARD
        if (autonomousState.isExecuting) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = AmberWarning,
                            backgroundColor = Color(0xF2180E04)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WATCHING ALISE EXECUTE: ${autonomousState.activeSetting?.title}",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "RETURN IN ${autonomousState.autoReturnCountdownSeconds}S",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger,
                                fontSize = 9.5.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { autonomousState.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AmberWarning,
                            trackColor = Color(0x33F59E0B)
                        )

                        Text(
                            text = autonomousState.executionStep,
                            style = Typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }

        // AUTO-RETURN TETHER DURATION SELECTOR
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xF2050B16)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AUTO-RETURN TETHER TIMER",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "Pulls you back to ALISE HUD after adjusting setting in OS",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 8.5.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(3f, 4f, 6f, 10f).forEach { sec ->
                            val isSel = selectedTetherDuration == sec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) Color(0x3338BDF8) else Color(0x221E293B))
                                    .border(1.dp, if (isSel) SkyBright else Color(0x3338BDF8), RoundedCornerShape(4.dp))
                                    .clickable { selectedTetherDuration = sec }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${sec.toInt()}s",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else JarvisTextMuted,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // REAL AI / LLM CONFIGURATION (LLAMA & GEMINI)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3322C55E),
                        backgroundColor = Color(0xF2050E18)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REAL AI NEURAL ENGINE CONFIGURATION",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "NO PRE-MADE STRINGS",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldNominal,
                            fontSize = 8.5.sp
                        )
                    }

                    Text(
                        text = "CUSTOM LLAMA / OPENROUTER API KEY:",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                    BasicTextField(
                        value = customApiKey,
                        onValueChange = {
                            customApiKey = it
                            AiLlmEngine.customLlamaApiKey = it
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(SkyBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF03070E))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )
                }
            }
        }

        // ALL 13 SYSTEM SETTING CONTROLLER CARDS
        items(SystemSettingCategory.values()) { setting ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x2E38BDF8),
                        backgroundColor = Color(0xF2050B16)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(setting),
                                contentDescription = setting.title,
                                tint = SkyBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = setting.title,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }

                        // Specific direct toggles
                        if (setting == SystemSettingCategory.FLASHLIGHT) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isTorchOn) AmberWarning else Color(0x221E293B))
                                    .clickable { viewModel.toggleFlashlight() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isTorchOn) "ON" else "OFF",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTorchOn) Color.Black else JarvisTextMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = setting.subtitle,
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 8.5.sp
                    )

                    // TWO CONTROL BUTTONS: (1) WATCH ALISE EXECUTE & (2) TETHER PULL-BACK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.settingController.watchJarvisExecute(
                                    setting = setting,
                                    onCompleteSpeech = { speech ->
                                        viewModel.voiceEngine.speak(speech)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SkyBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WATCH ALISE DO IT",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SkyBright,
                                fontSize = 8.sp
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.settingController.launchWithAutoReturnTether(
                                    setting = setting,
                                    tetherSeconds = selectedTetherDuration.toInt(),
                                    onCompleteSpeech = { speech ->
                                        viewModel.voiceEngine.speak(speech)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x221E293B)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = EmeraldNominal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TETHER PULL-BACK (${selectedTetherDuration.toInt()}S)",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNominal,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
