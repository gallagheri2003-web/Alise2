package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CompanionAura
import com.example.ui.components.IosCompanionCard
import com.example.ui.components.iosGlassSurface
import com.example.ui.theme.IosAccentAmber
import com.example.ui.theme.IosAccentIndigo
import com.example.ui.theme.IosAccentPeach
import com.example.ui.theme.IosAccentRose
import com.example.ui.theme.IosAccentSage
import com.example.ui.theme.IosAccentSky
import com.example.ui.theme.IosAccentViolet
import com.example.ui.theme.IosBorderGlass
import com.example.ui.theme.IosBorderSubtle
import com.example.ui.theme.IosCanvasDark
import com.example.ui.theme.IosCanvasSoft
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSurfaceCardElevated
import com.example.ui.theme.IosSurfaceGlass
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextQuaternary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.VoiceState
import java.util.Calendar

@Composable
fun CompanionHomeScreen(
    viewModel: JarvisViewModel,
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToVoice: () -> Unit = {},
    onNavigateToSanctuary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioAmplitude by viewModel.voiceEngine.audioAmplitude.collectAsState()
    val interactions by viewModel.interactions.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val mediaVolume by viewModel.mediaVolume.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()
    val voiceListenerState by viewModel.voiceListenerState.collectAsState()
    val btcOracle by viewModel.btcOracleTelemetry.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showQuickControls by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Smooth scroll on new message
    LaunchedEffect(interactions.size) {
        if (interactions.isNotEmpty()) {
            listState.animateScrollToItem(interactions.size)
        }
    }

    // Warm greeting based on local time
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Good night"
        }
    }

    val quickSuggestions = listOf(
        "How is my day looking?",
        "Quiet the room & focus",
        "How is my phone's health?",
        "Check Bitcoin oracle glance",
        "Take a note for me",
        "Turn on gentle light"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ============================================================
            // 1. WARM COMPANION HEADER & GREETING
            // ============================================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALICE",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IosAccentViolet,
                            letterSpacing = 1.2.sp,
                            fontSize = 14.sp
                        )

                        // Ambient Companion Status Capsule
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(IosSurfaceCard)
                                .border(1.dp, IosBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (voiceListenerState.isRunning) IosAccentSage else IosAccentAmber)
                            )
                            Text(
                                text = if (voiceListenerState.isRunning) "Always Listening" else "Companion Ready",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = IosTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "$greetingText, Ian",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary,
                        fontSize = 28.sp
                    )

                    Text(
                        text = "I'm right here with you. How can I care for your day?",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = IosTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // ============================================================
            // 2. THE LIVING COMPANION AURA (Central Presence)
            // ============================================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompanionAura(
                            voiceState = voiceState,
                            amplitude = audioAmplitude,
                            sizeDp = 180.dp,
                            onClick = {
                                if (voiceState is VoiceState.ListeningWakeWord || voiceState is VoiceState.ListeningCommand) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            }
                        )

                        // Ethereal Status Label
                        val statusLabel = when (voiceState) {
                            is VoiceState.ListeningCommand, is VoiceState.ListeningWakeWord -> "Listening to you with care..."
                            is VoiceState.Speaking -> "ALICE is speaking..."
                            is VoiceState.Processing -> "Reflecting on your thought..."
                            is VoiceState.Executing -> "Performing gentle action..."
                            else -> "Tap the aura or say 'Alice' anytime"
                        }

                        Text(
                            text = statusLabel,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = if (voiceState !is VoiceState.Idle) IosAccentViolet else IosTextTertiary,
                            fontWeight = if (voiceState !is VoiceState.Idle) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ============================================================
            // 3. THOUGHTFUL QUICK COMPANION PROMPTS
            // ============================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickSuggestions.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(IosSurfaceCard)
                                .border(1.dp, IosBorderSubtle, RoundedCornerShape(18.dp))
                                .clickable {
                                    viewModel.submitVoiceCommand(prompt)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = prompt,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = IosTextPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            // ============================================================
            // 4. COMPANION VITALS WIDGET (Humane, not mechanical)
            // ============================================================
            item {
                IosCompanionCard(
                    title = "Your Day & Device Vitality",
                    subtitle = "Quietly looking after your environment",
                    icon = Icons.Default.AutoAwesome,
                    badgeText = "Nominal",
                    badgeColor = IosAccentSage
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Battery Health Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(IosSurfaceCardElevated)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "BATTERY HEALTH",
                                    fontSize = 10.sp,
                                    color = IosTextTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${telemetry.batteryLevel}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = "Good for all day",
                                    fontSize = 11.sp,
                                    color = IosAccentSage
                                )
                            }
                        }

                        // Sanctuary & Peace Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(IosSurfaceCardElevated)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "ATMOSPHERE",
                                    fontSize = 10.sp,
                                    color = IosTextTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${telemetry.coreTemperature.toInt()}°C",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IosTextPrimary
                                )
                                Text(
                                    text = "Cool & serene",
                                    fontSize = 11.sp,
                                    color = IosAccentSky
                                )
                            }
                        }

                        // Crypto / Oracle Glance Card
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(IosSurfaceCardElevated)
                                .clickable {
                                    viewModel.submitVoiceCommand("bitcoin oracle")
                                }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "BTC ORACLE",
                                    fontSize = 10.sp,
                                    color = IosTextTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${btcOracle.btcUsd.toInt() / 1000}k",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IosAccentAmber
                                )
                                Text(
                                    text = "Yield: ${String.format(java.util.Locale.US, "%.1f", btcOracle.effectiveYieldApy)}%",
                                    fontSize = 11.sp,
                                    color = IosTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick hardware controls toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.toggleFlashlight() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFlashlightOn) IosAccentAmber else IosSurfaceCardElevated
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = null,
                                tint = if (isFlashlightOn) Color.Black else IosTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFlashlightOn) "Gentle Light On" else "Night Light",
                                fontSize = 11.sp,
                                color = if (isFlashlightOn) Color.Black else IosTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.executeProtocol(
                                    com.example.data.local.entities.ProtocolEntity(
                                        codeName = "S25_DEEP_FOCUS",
                                        displayName = "Calm Sanctuary",
                                        description = "Silences noise, dims distractions",
                                        actionsJson = "[]",
                                        category = "CALM",
                                        isPreset = true
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IosSurfaceCardElevated),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IosAccentViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calm Sanctuary",
                                fontSize = 11.sp,
                                color = IosTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ============================================================
            // 5. CONVERSATION STREAM (iOS Card Bubbles)
            // ============================================================
            item {
                Text(
                    text = "CONVERSATION WITH ALICE",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = IosTextTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            items(interactions) { msg ->
                val isAlice = msg.sender == "ALISE" || msg.sender == "ALICE" || msg.sender == "ASSISTANT"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAlice) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isAlice) 4.dp else 20.dp,
                                    bottomEnd = if (isAlice) 20.dp else 4.dp
                                )
                            )
                            .background(
                                if (isAlice) {
                                    Brush.linearGradient(
                                        listOf(
                                            IosSurfaceCardElevated.copy(alpha = 0.95f),
                                            IosSurfaceGlass.copy(alpha = 0.85f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            IosAccentViolet.copy(alpha = 0.90f),
                                            IosAccentIndigo.copy(alpha = 0.85f)
                                        )
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isAlice) IosBorderGlass else Color.Transparent,
                                shape = RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isAlice) 4.dp else 20.dp,
                                    bottomEnd = if (isAlice) 20.dp else 4.dp
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isAlice) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(IosAccentViolet),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isAlice) "ALICE" else "Ian",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAlice) IosAccentViolet else Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Text(
                                text = msg.text,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = if (isAlice) IosTextPrimary else Color.White,
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp
                            )

                            if (isAlice && msg.isSearchGrounded) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(IosAccentSky.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Grounded with live verified intelligence",
                                            fontSize = 9.sp,
                                            color = IosAccentSky,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(IosSurfaceCard)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = IosAccentViolet
                                )
                                Text(
                                    text = "ALICE is writing...",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = IosTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // 6. FLOATING iOS INPUT PILL (Always accessible)
        // ============================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(IosSurfaceCardElevated.copy(alpha = 0.98f))
                    .border(1.dp, IosBorderGlass.copy(alpha = 0.40f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice / Mic Action
                IconButton(
                    onClick = {
                        if (voiceState is VoiceState.ListeningWakeWord || voiceState is VoiceState.ListeningCommand) {
                            viewModel.stopVoiceListening()
                        } else {
                            viewModel.startVoiceListening()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (voiceState is VoiceState.ListeningCommand) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (voiceState is VoiceState.ListeningCommand) IosAccentRose else IosAccentViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    if (textInput.isBlank()) {
                        Text(
                            text = "Message ALICE or speak...",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = IosTextTertiary,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        textStyle = TextStyle(
                            color = IosTextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(IosAccentViolet),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.submitVoiceCommand(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) IosAccentViolet else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) Color.White else IosTextQuaternary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
