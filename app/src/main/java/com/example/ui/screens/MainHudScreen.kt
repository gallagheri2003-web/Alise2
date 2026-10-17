package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiChatModel
import com.example.data.api.GeminiChatRole
import com.example.ui.components.JarvisGlobeCore
import com.example.ui.components.HolographicVoiceWaveformHud
import com.example.ui.components.HolographicTelemetryModule
import com.example.ui.components.HolographicWaveformStyle
import com.example.ui.components.HolographicSpeechBubble
import com.example.ui.components.GlowingNeonText
import com.example.ui.components.TacticalCodeTerminalOverlay
import com.example.ui.components.OneUiWidgetCustomizationBar
import com.example.ui.components.OneUiWidgetCard
import com.example.ui.components.OneUiLayoutMode
import com.example.ui.components.alienSymbiontSurface
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.components.holographicBreathingGlow
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.AudioTranscriptionState
import com.example.voice.VoiceState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHudScreen(
    viewModel: JarvisViewModel,
    onNavigateToVision: () -> Unit,
    onNavigateToProtocols: () -> Unit,
    onNavigateToTelemetry: () -> Unit,
    onNavigateToVoiceStudio: () -> Unit,
    onNavigateToAutoCoder: () -> Unit = {},
    onNavigateToLiveConsole: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val interactions by viewModel.interactions.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val activeVoiceModel by viewModel.voiceEngine.activeVoiceModel.collectAsState()
    val personalityMode by viewModel.voiceEngine.aiPersonalityMode.collectAsState()

    // Gemini Chatbot & Search Grounding States
    val selectedModel by viewModel.selectedGeminiModel.collectAsState()
    val selectedRole by viewModel.selectedGeminiRole.collectAsState()
    val customRoleInstruction by viewModel.customRoleSystemInstruction.collectAsState()
    val isSearchGroundingEnabled by viewModel.isSearchGroundingEnabled.collectAsState()
    val isChatGenerating by viewModel.isChatGenerating.collectAsState()

    // Audio Transcription States (gemini-3.5-transcribe)
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val liveTranscribeAmplitude by viewModel.liveTranscribeAmplitude.collectAsState()
    val audioAmplitude by viewModel.voiceEngine.audioAmplitude.collectAsState()
    val partialTranscript by viewModel.voiceEngine.partialTranscript.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showRoleConfigDialog by remember { mutableStateOf(false) }
    var tempCustomPrompt by remember { mutableStateOf(customRoleInstruction) }
    var showWaveformHud by remember { mutableStateOf(true) }
    var isSphereExpanded by remember { mutableStateOf(false) }
    var oneUiMode by remember { mutableStateOf(OneUiLayoutMode.ONE_UI_DEX) }
    var activeAccentColor by remember { mutableStateOf(AlienEmpathyCyan) }
    val chatListState = rememberLazyListState()

    LaunchedEffect(interactions.size) {
        if (interactions.isNotEmpty()) {
            chatListState.animateScrollToItem(interactions.size - 1)
        }
    }

    // Role Configuration Dialog
    if (showRoleConfigDialog) {
        AlertDialog(
            onDismissRequest = { showRoleConfigDialog = false },
            containerColor = Color(0xFF070F1E),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "Role", tint = SkyBright, modifier = Modifier.size(20.dp))
                    Text(
                        text = "GEMINI CHATBOT ROLE DIRECTIVE",
                        style = Typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select a specialized persona role or define custom system instructions for Gemini multi-turn reasoning:",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 9.sp
                    )

                    // Role Buttons
                    GeminiChatRole.values().forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x3338BDF8) else Color(0x151E293B))
                                .border(1.dp, if (isSelected) SkyBright else Color(0x2238BDF8), RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.setGeminiRole(role)
                                    if (role != GeminiChatRole.CUSTOM_DIRECTIVE) {
                                        tempCustomPrompt = role.defaultSystemPrompt
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = role.displayName.uppercase(),
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SkyBright else Color.White,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = role.category,
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = EmeraldNominal,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    // System Instruction Editor
                    Text(
                        text = "ACTIVE SYSTEM INSTRUCTION:",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                    OutlinedTextField(
                        value = tempCustomPrompt.ifBlank { selectedRole.defaultSystemPrompt },
                        onValueChange = { tempCustomPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBright,
                            unfocusedBorderColor = Color(0x4438BDF8),
                            focusedContainerColor = Color(0xFF030712),
                            unfocusedContainerColor = Color(0xFF030712)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomRoleSystemInstruction(tempCustomPrompt)
                        showRoleConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyAccent)
                ) {
                    Text("APPLY ROLE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleConfigDialog = false }) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = JarvisTextMuted, fontSize = 10.sp)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // 1. TOP SITUATIONAL TELEMETRY & VOICE PROFILE STRIP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 8.dp,
                    borderColor = Color(0x3338BDF8),
                    backgroundColor = Color(0xD9050B16)
                )
                .clickable { onNavigateToTelemetry() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Location & MGRS
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS",
                            tint = EmeraldNominal,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "MGRS: ${locationState.mgrsCoordinate.ifBlank { "31U DQ 4821 9840" }}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = "Head: ${locationState.cardinalHeading} (${locationState.bearingDegrees.toInt()}°) | Alt: ${locationState.altitudeFeet}ft | Battery: ${telemetry.batteryLevel}%",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright.copy(alpha = 0.8f),
                        fontSize = 7.5.sp
                    )
                }

                // Right: Clear Chat button & Role badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x2238BDF8))
                            .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(4.dp))
                            .clickable { showRoleConfigDialog = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = SkyBright, modifier = Modifier.size(10.dp))
                            Text(
                                text = selectedRole.displayName.take(10).uppercase(),
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldNominal,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = JarvisTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // PRIMARY A.L.I.S.E. 3D HOLLOW BLUE-GLASS SPHERE CENTERPIECE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 10.dp,
                    borderColor = Color(0x3300E5FF),
                    backgroundColor = Color(0xF2020712)
                )
                .holographicBreathingGlow(color = Color(0xFF00E5FF), cornerRadius = 10.dp)
                .padding(vertical = 6.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sphere Header & Mode Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when (voiceState) {
                                        is VoiceState.Speaking -> Color(0xFF00F5FF)
                                        is VoiceState.ListeningCommand, is VoiceState.ListeningWakeWord -> Color(0xFF38BDF8)
                                        is VoiceState.Processing, is VoiceState.Executing -> Color(0xFF818CF8)
                                        is VoiceState.Error -> CrimsonDanger
                                        else -> EmeraldNominal
                                    }
                                )
                        )
                        Text(
                            text = "A.L.I.S.E. HOLLOW GLASS SPHERE",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBAE6FD),
                            fontSize = 8.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x2238BDF8))
                                .clickable { isSphereExpanded = !isSphereExpanded }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isSphereExpanded) "COMPACT" else "EXPAND",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.5.sp,
                                color = SkyBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // The Precision 3D Hollow Symmetrical Blue-Glass Sphere
                JarvisGlobeCore(
                    size = if (isSphereExpanded) 180.dp else 105.dp,
                    amplitude = if (transcriptionState is AudioTranscriptionState.Recording) liveTranscribeAmplitude else audioAmplitude,
                    voiceState = voiceState,
                    showTelemetry = true,
                    onClick = {
                        if (voiceState is VoiceState.Speaking) {
                            viewModel.voiceEngine.bargeIn()
                        } else if (voiceState is VoiceState.ListeningCommand) {
                            viewModel.voiceEngine.stopListening()
                        } else {
                            viewModel.voiceEngine.startListeningCommandDirectly()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Live Voice & Neural State Readout
                Text(
                    text = when (voiceState) {
                        is VoiceState.Speaking -> "SPEAKING • TAP SPHERE TO INTERRUPT"
                        is VoiceState.ListeningCommand -> "LISTENING TO IAN... • TAP TO STOP"
                        is VoiceState.ListeningWakeWord -> "STANDBY: 'HEY ALISE' • TAP TO ACTIVATE"
                        is VoiceState.Processing -> "PROCESSING TACTICAL QUERY..."
                        is VoiceState.Executing -> "EXECUTING ANDROID SYSTEM ACTION..."
                        is VoiceState.Success -> "ACTION COMPLETED WITH SUCCESS"
                        is VoiceState.Interrupted -> "BARGE-IN ACKNOWLEDGED"
                        is VoiceState.Error -> "SYSTEM STATUS: ${(voiceState as VoiceState.Error).message}"
                        else -> "ALISE CORE STANDBY • TAP SPHERE TO TALK"
                    },
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (voiceState) {
                        is VoiceState.Speaking -> Color(0xFF00F5FF)
                        is VoiceState.ListeningCommand -> SkyBright
                        is VoiceState.Processing -> Color(0xFF818CF8)
                        is VoiceState.Error -> CrimsonDanger
                        else -> Color(0xFF94A3B8)
                    },
                    fontSize = 8.sp
                )

                if (partialTranscript.isNotBlank() && voiceState !is VoiceState.Idle) {
                    Text(
                        text = "\"$partialTranscript\"",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ONE UI 8.5 / 9 SAMSUNG CUSTOMIZATION BAR
        OneUiWidgetCustomizationBar(
            selectedMode = oneUiMode,
            onModeSelect = { oneUiMode = it },
            activeAccentColor = activeAccentColor,
            onAccentColorSelect = { activeAccentColor = it }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // OVERLAY TACTICAL CODE TERMINAL (SCROLLING SYSTEM CLI LOGS)
        TacticalCodeTerminalOverlay(
            isExpandedDefault = false,
            onExecuteCommand = { cmd ->
                viewModel.voiceEngine.speak("Executing terminal command: $cmd", playChimeBefore = false)
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. GEMINI MODEL SELECTOR & SEARCH GROUNDING CONTROLS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 6.dp,
                    borderColor = Color(0x2238BDF8),
                    backgroundColor = Color(0xF0030712)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Models Selector: PRO / FLASH / LITE
                GeminiChatModel.values().forEach { model ->
                    val isSelected = selectedModel == model
                    val badgeColor = when (model) {
                        GeminiChatModel.COMPLEX_PRO -> Color(0xFFA855F7) // Purple Pro
                        GeminiChatModel.GENERAL_FLASH -> SkyBright // Sky Flash
                        GeminiChatModel.FAST_LITE -> EmeraldNominal // Emerald Lite
                        GeminiChatModel.MULTIMODAL_LIVE -> AmberWarning // Amber Live
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) badgeColor.copy(alpha = 0.25f) else Color(0x151E293B))
                            .border(1.dp, if (isSelected) badgeColor else Color(0x2238BDF8), RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.setGeminiModel(model)
                                if (model == GeminiChatModel.MULTIMODAL_LIVE) {
                                    onNavigateToLiveConsole()
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (model) {
                                    GeminiChatModel.COMPLEX_PRO -> Icons.Default.AutoAwesome
                                    GeminiChatModel.GENERAL_FLASH -> Icons.Default.Bolt
                                    GeminiChatModel.FAST_LITE -> Icons.Default.SmartToy
                                    GeminiChatModel.MULTIMODAL_LIVE -> Icons.Default.GraphicEq
                                },
                                contentDescription = null,
                                tint = if (isSelected) badgeColor else JarvisTextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = model.badge,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else JarvisTextMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                }

                // Google Search Grounding Toggle Chip
                val groundingActive = isSearchGroundingEnabled
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (groundingActive) Color(0x3322C55E) else Color(0x151E293B))
                        .border(1.dp, if (groundingActive) EmeraldNominal else Color(0x2238BDF8), RoundedCornerShape(4.dp))
                        .clickable { viewModel.toggleSearchGrounding() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Search Grounding",
                            tint = if (groundingActive) EmeraldNominal else JarvisTextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (groundingActive) "SEARCH: ON (3.5 FLASH)" else "SEARCH: OFF",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (groundingActive) EmeraldNominal else JarvisTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }

                // Auto Coder Navigation Quick Action
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x1F1E293B))
                        .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                        .clickable { onNavigateToAutoCoder() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚡ CODE EDITOR",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Holographic Voice Waveform HUD Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (showWaveformHud) Color(0x3338BDF8) else Color(0x151E293B))
                        .border(1.dp, if (showWaveformHud) SkyBright else Color(0x2238BDF8), RoundedCornerShape(4.dp))
                        .clickable { showWaveformHud = !showWaveformHud }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                        .testTag("toggle_voice_waveform_hud")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Waveform HUD",
                            tint = if (showWaveformHud) SkyBright else JarvisTextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (showWaveformHud) "VOX HUD: ON" else "VOX HUD: OFF",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (showWaveformHud) SkyBright else JarvisTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        // HOLOGRAPHIC ANIMATED VOICE WAVEFORM HUD OVERLAY
        AnimatedVisibility(
            visible = showWaveformHud || voiceState is VoiceState.Speaking || voiceState is VoiceState.ListeningCommand || transcriptionState is AudioTranscriptionState.Recording,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                HolographicTelemetryModule(
                    telemetry = telemetry,
                    coreColor = if (voiceState is VoiceState.Speaking) SkyBright else EmeraldNominal
                )
                Spacer(modifier = Modifier.height(4.dp))
                HolographicVoiceWaveformHud(
                    amplitude = if (transcriptionState is AudioTranscriptionState.Recording) liveTranscribeAmplitude else audioAmplitude,
                    voiceState = voiceState,
                    activeVoiceName = activeVoiceModel.name,
                    heightDp = 78.dp,
                    showControls = true,
                    showTelemetryReadout = true,
                    onTestVoiceClick = {
                        viewModel.voiceEngine.speak("ALISE holographic neural audio channel verified. Awaiting tactical commands.", playChimeBefore = true)
                    },
                    onBargeInClick = {
                        viewModel.voiceEngine.bargeIn()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. MULTI-TURN CONVERSATION THREAD (SCROLLABLE)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 8.dp,
                    borderColor = Color(0x3338BDF8),
                    backgroundColor = Color(0xF2030814)
                )
                .holographicBreathingGlow(color = SkyBright, cornerRadius = 8.dp)
                .padding(6.dp)
        ) {
            LazyColumn(
                state = chatListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(interactions, key = { it.id }) { msg ->
                    val isUser = msg.sender == "IAN" || msg.sender == "USER"

                    HolographicSpeechBubble(
                        sender = msg.sender,
                        messageText = msg.text,
                        timestamp = msg.timestamp,
                        isUser = isUser,
                        modelUsed = msg.modelUsed,
                        isSearchGrounded = msg.isSearchGrounded,
                        onSpeakClick = if (!isUser) {
                            { viewModel.voiceEngine.speak(msg.text, playChimeBefore = false) }
                        } else null
                    )
                }

                if (isChatGenerating) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x221E293B))
                                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = SkyBright
                                )
                                Text(
                                    text = "Gemini (${selectedModel.displayName}) synthesizing response with Search Grounding...",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBright,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. GEMINI AUDIO TRANSCRIBER BANNER (gemini-3.5-transcribe)
        when (val state = transcriptionState) {
            is AudioTranscriptionState.Recording -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, CrimsonDanger, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = "Recording", tint = CrimsonDanger, modifier = Modifier.size(12.dp))
                            val seconds = state.durationMs / 1000f
                            Text(
                                text = "RECORDING SPEECH: ${String.format(Locale.US, "%.1fs", seconds)} | Amp: ${String.format(Locale.US, "%.2f", liveTranscribeAmplitude)}",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.cancelAudioRecording() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                            IconButton(
                                onClick = {
                                    viewModel.stopAndTranscribeAudio(autoSubmitToChat = true)
                                },
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldNominal)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop & Transcribe", tint = Color.Black, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            is AudioTranscriptionState.Transcribing -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3338BDF8))
                        .border(1.dp, SkyBright, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = SkyBright)
                        Text(
                            text = "Transcribing audio via gemini-3.5-transcribe...",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright,
                            fontSize = 8.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            is AudioTranscriptionState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33EF4444))
                        .border(1.dp, CrimsonDanger, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transcription error: ${state.message}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                        IconButton(onClick = { viewModel.cancelAudioRecording() }, modifier = Modifier.size(16.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            else -> {}
        }

        // 5. INPUT & AUDIO RECORDING CONTROLS DOCK
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .obsidianGlassPanel(
                    cornerRadius = 8.dp,
                    borderColor = Color(0x4438BDF8),
                    backgroundColor = Color(0xF5040A18)
                )
                .holographicBreathingGlow(color = SkyBright, cornerRadius = 8.dp)
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Gemini Speech-to-Text Transcription Button (gemini-3.5-transcribe)
                val isRecordingAudio = transcriptionState is AudioTranscriptionState.Recording
                IconButton(
                    onClick = {
                        if (isRecordingAudio) {
                            viewModel.stopAndTranscribeAudio(autoSubmitToChat = true)
                        } else {
                            viewModel.startAudioTranscription()
                        }
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingAudio) CrimsonDanger else Color(0x330284C7))
                        .border(1.dp, if (isRecordingAudio) CrimsonDanger else SkyBright, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Transcribe Audio",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Text Input Field for Multi-Turn Chat
                BasicTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(SkyBright),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendGeminiChatMessage(textInput)
                                textInput = ""
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (textInput.isEmpty()) {
                                Text(
                                    text = "Ask ALISE (${selectedModel.badge} + ${if (isSearchGroundingEnabled) "Grounded" else "Direct"})...",
                                    style = Typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisTextMuted,
                                    fontSize = 8.5.sp
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendGeminiChatMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank() && !isChatGenerating,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank() && !isChatGenerating) Color(0xFF0284C7) else Color(0x221E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) Color.White else JarvisTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
