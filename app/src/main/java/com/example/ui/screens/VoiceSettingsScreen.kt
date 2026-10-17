package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HolographicVoiceWaveformHud
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.AiPersonalityMode
import com.example.voice.AudioTranscriptionState
import com.example.voice.VoiceModelType
import java.util.Locale

@Composable
fun VoiceSettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceEngine = viewModel.voiceEngine

    val activeModel by voiceEngine.activeVoiceModel.collectAsState()
    val allModels by voiceEngine.voiceModels.collectAsState()
    val personalityMode by voiceEngine.aiPersonalityMode.collectAsState()
    val amplitude by voiceEngine.audioAmplitude.collectAsState()
    val voiceState by voiceEngine.voiceState.collectAsState()

    // Global customization properties
    val isPitchRateLocked by voiceEngine.isGlobalPitchRateLocked.collectAsState()
    val globalPitch by voiceEngine.globalPitch.collectAsState()
    val globalRate by voiceEngine.globalRate.collectAsState()
    val isChimeEnabled by voiceEngine.isChimeEnabled.collectAsState()
    val ttsVolume by voiceEngine.ttsVolume.collectAsState()

    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val liveTranscribeAmplitude by viewModel.liveTranscribeAmplitude.collectAsState()

    val voiceListenerState by viewModel.voiceListenerState.collectAsState()
    val btcOracle by viewModel.btcOracleTelemetry.collectAsState()

    var testPhrase by remember {
        mutableStateOf(
            "ALISE speech synthesis engine online. Ready for your query."
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newModelName by remember { mutableStateOf("") }
    var newModelPath by remember {
        mutableStateOf("/sdcard/models/custom_voice.onnx")
    }
    var newModelDescription by remember {
        mutableStateOf("User custom neural voice model")
    }
    var newModelType by remember {
        mutableStateOf(VoiceModelType.CUSTOM_IMPORTED_FILE)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // ============================================================
        // 1. UNIFIED AI ASSISTANT STATUS
        // ============================================================

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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldNominal)
                                )

                                Text(
                                    text = "UNIFIED PERSONAL AI ASSISTANT",
                                    style = Typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.5.sp
                                )
                            }

                            Text(
                                text = "ONE AI • ONE IDENTITY • ONE CONVERSATION",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                        }

                        Button(
                            onClick = {
                                voiceEngine.testVoiceModel(testPhrase)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7)
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Test",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "PREVIEW",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2210B981))
                            .border(
                                1.dp,
                                Color(0x4434D399),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "ACTIVE ASSISTANT",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNominal,
                                fontSize = 8.sp
                            )

                            Text(
                                text = "Single conversational AI using the selected LLM, tools, memory and voice interface.",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )

                            Text(
                                text = "Voice models change speech output only. They do not create additional AI personalities.",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 7.5.sp
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // 2. LIQUID / HOLOGRAPHIC VOICE INTERFACE
        // ============================================================

        item {
            HolographicVoiceWaveformHud(
                amplitude = amplitude,
                voiceState = voiceState,
                activeVoiceName = activeModel.name,
                heightDp = 100.dp,
                showControls = true,
                showTelemetryReadout = true,

                onTestVoiceClick = {
                    voiceEngine.speak(
                        testPhrase,
                        playChimeBefore = true
                    )
                },

                onBargeInClick = {
                    voiceEngine.bargeIn()
                }
            )
        }

        // ============================================================
        // 2B. MICROPHONE AUDIO INPUT & TRANSCRIPTION (gemini-3.5-transcribe)
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x4438BDF8),
                        backgroundColor = Color(0xF2050B18)
                    )
                    .padding(10.dp)
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
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speech Transcription",
                                tint = SkyBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "AUDIO INPUT & TRANSCRIPTION",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "MODEL: gemini-3.5-transcribe",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldNominal,
                                    fontSize = 7.5.sp
                                )
                            }
                        }

                        val isRecording = transcriptionState is AudioTranscriptionState.Recording
                        Button(
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopAndTranscribeAudio(autoSubmitToChat = false)
                                } else {
                                    viewModel.startAudioTranscription()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) CrimsonDanger else SkyBright
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isRecording) Color.White else Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRecording) "STOP & TRANSCRIBE" else "RECORD MIC",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) Color.White else Color.Black,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    when (val state = transcriptionState) {
                        is AudioTranscriptionState.Recording -> {
                            val seconds = state.durationMs / 1000f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, CrimsonDanger, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
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
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonChecked,
                                            contentDescription = "Recording",
                                            tint = CrimsonDanger,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "LISTENING... ${String.format(Locale.US, "%.1fs", seconds)} | Amp: ${String.format(Locale.US, "%.2f", liveTranscribeAmplitude)}",
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.cancelAudioRecording() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is AudioTranscriptionState.Transcribing -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x330284C7))
                                    .border(1.dp, SkyBright, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = SkyBright
                                    )
                                    Text(
                                        text = "Transcribing audio via gemini-3.5-transcribe...",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBright,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        is AudioTranscriptionState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x2210B981))
                                    .border(1.dp, EmeraldNominal, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TRANSCRIPT (${state.latencyMs}ms • ${state.modelUsed}):",
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldNominal,
                                            fontSize = 8.sp
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    voiceEngine.speak(state.transcript, playChimeBefore = false)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Speak Transcript",
                                                    tint = SkyBright,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.saveVoiceNote("Audio Transcript", state.transcript)
                                                    Toast.makeText(context, "Saved to Vault", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = "Save Note",
                                                    tint = EmeraldNominal,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "\"${state.transcript}\"",
                                        style = Typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        is AudioTranscriptionState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, CrimsonDanger, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Error: ${state.message}",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    fontSize = 8.5.sp
                                )
                            }
                        }

                        else -> {
                            Text(
                                text = "Press RECORD MIC to speak. Audio will be sent to gemini-3.5-transcribe and converted to text.",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // 2C. ALICE VOICE LISTENER SERVICE (Android SpeechRecognizer API)
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x4422D3EE),
                        backgroundColor = Color(0xF2060E1E)
                    )
                    .padding(10.dp)
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
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Listener Service",
                                tint = SkyBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "ALICE VOICE LISTENER SERVICE",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "ENGINE: Android SpeechRecognizer API",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (voiceListenerState.isRunning) EmeraldNominal else CrimsonDanger,
                                    fontSize = 7.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.toggleVoiceListenerService()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (voiceListenerState.isRunning) CrimsonDanger else EmeraldNominal
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (voiceListenerState.isRunning) "STOP LISTENER" else "START SERVICE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    // Status and Live Telemetry readout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x220284C7))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "STATUS: ${if (voiceListenerState.isRunning) (if (voiceListenerState.isListening) "LIVE LISTENING" else "STANDBY") else "SERVICE STOPPED"}",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (voiceListenerState.isRunning) EmeraldNominal else AmberWarning,
                                    fontSize = 8.5.sp
                                )
                                Text(
                                    text = "TRIGGERS: ${voiceListenerState.triggerCount}",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBright,
                                    fontSize = 8.5.sp
                                )
                            }

                            if (voiceListenerState.lastRecognizedText.isNotBlank()) {
                                Text(
                                    text = "LAST PHRASE: \"${voiceListenerState.lastRecognizedText}\"",
                                    style = Typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    fontSize = 8.5.sp
                                )
                            }

                            if (voiceListenerState.lastTriggeredAction.isNotBlank()) {
                                Text(
                                    text = "EXECUTED: ${voiceListenerState.lastTriggeredAction}",
                                    style = Typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldNominal,
                                    fontSize = 8.5.sp
                                )
                            }

                            if (voiceListenerState.errorMessage != null) {
                                Text(
                                    text = "NOTICE: ${voiceListenerState.errorMessage}",
                                    style = Typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = CrimsonDanger,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    // Keyword Triggers Guide
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "ARMED KEYWORD DIRECTIVES:",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright,
                            fontSize = 8.sp
                        )
                        Text(
                            text = "• PROTOCOLS: \"Bitcoin Oracle\", \"Protocol Sentry Mode\", \"Protocol Wi-Fi Defense\", \"Protocol DNS Sanctuary\", \"Protocol Clean Slate\", \"Protocol Lockdown\", \"Deep Focus\"",
                            style = Typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.5.sp
                        )
                        Text(
                            text = "• SYSTEM COMMANDS: \"Flashlight on / off / strobe\", \"Volume up / down / mute\", \"Open Spotify / Camera\", \"Take a note ...\"",
                            style = Typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.5.sp
                        )
                    }
                }
            }
        }

        // ============================================================
        // 2D. BTC ORACLE & DECENTRALIZED TELEMETRY MATRIX
        // ============================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x44F59E0B),
                        backgroundColor = Color(0xF2120E05)
                    )
                    .padding(10.dp)
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
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bitcoin Oracle",
                                tint = AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "BTC ORACLE & DEFI TELEMETRY",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "DATA CLASS: BtcOracleTelemetry",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmberWarning,
                                    fontSize = 7.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val speech = "Bitcoin Oracle verified. Price is $${btcOracle.btcUsd.toInt()} dollars. Volatility index is ${btcOracle.vix1moPercent} percent, Ethereum gas is ${btcOracle.ethGasGweiMedian} Gwei, pool depth is ${btcOracle.poolDepthBtc.toInt()} Bitcoin, and effective yield APY is ${String.format(java.util.Locale.US, "%.2f", btcOracle.effectiveYieldApy)} percent."
                                voiceEngine.speak(speech, playChimeBefore = true)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberWarning
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VOICE READOUT",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 8.sp
                            )
                        }
                    }

                    // Telemetry Grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x3310B981))
                                    .border(1.dp, EmeraldNominal, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("BTC / USD ORACLE", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("$${String.format(java.util.Locale.US, "%,.2f", btcOracle.btcUsd)}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = EmeraldNominal)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33F59E0B))
                                    .border(1.dp, AmberWarning, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("BTC 1M VIX", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("${btcOracle.vix1moPercent}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = AmberWarning)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33A855F7))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("ETH GAS MEDIAN", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("${btcOracle.ethGasGweiMedian} Gwei", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x2238BDF8))
                                    .border(1.dp, SkyBright, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("POOL DEPTH", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("${String.format(java.util.Locale.US, "%,.0f", btcOracle.poolDepthBtc)} BTC", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = SkyBright)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x2210B981))
                                    .border(1.dp, EmeraldNominal, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("EFFECTIVE APY", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("${String.format(java.util.Locale.US, "%.2f", btcOracle.effectiveYieldApy)}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = EmeraldNominal)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x22E2E8F0))
                                    .border(1.dp, Color(0x44E2E8F0), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text("TAX RATE", fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = JarvisTextMuted)
                                    Text("${btcOracle.taxRatePercentBtc}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Metric mutation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.tickBtcOracleMetrics(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3310B981)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                        ) {
                            Text("+ TICK ORACLE", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = EmeraldNominal)
                        }

                        Button(
                            onClick = { viewModel.tickBtcOracleMetrics(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                        ) {
                            Text("- TICK ORACLE", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CrimsonDanger)
                        }

                        Button(
                            onClick = {
                                viewModel.submitTextCommand("execute protocol BTC_ORACLE_TELEMETRY")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x330284C7)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(26.dp)
                        ) {
                            Text("RUN PROTOCOL", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = SkyBright)
                        }
                    }
                }
            }
        }

        // ============================================================
        // 3. ASSISTANT PERSONALITY
        // ============================================================

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
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "PERSONAL ASSISTANT BEHAVIOR",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )

                    Text(
                        text = "These options customize ONE assistant. They do not create separate AI agents.",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 7.5.sp
                    )

                    AiPersonalityMode.values().forEach { mode ->

                        val isSelected = personalityMode == mode

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) {
                                        Color(0x330284C7)
                                    } else {
                                        Color(0x221E293B)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) {
                                        SkyBright
                                    } else {
                                        Color(0x2E38BDF8)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                            .clickable {
                                voiceEngine.selectPersonalityMode(mode)
                            }
                            .padding(8.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {

                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) {
                                                        EmeraldNominal
                                                    } else {
                                                        JarvisTextMuted
                                                    }
                                                )
                                        )

                                        Text(
                                            text = mode.title,
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) {
                                                Color.White
                                            } else {
                                                JarvisTextMuted
                                            },
                                            fontSize = 9.5.sp
                                        )
                                    }

                                    Text(
                                        text = mode.subtitle,
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) {
                                            SkyBright
                                        } else {
                                            JarvisTextMuted.copy(alpha = 0.7f)
                                        },
                                        fontSize = 8.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = EmeraldNominal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // 4. GLOBAL VOICE TUNING PARAMETERS (CUSTOMIZABLE)
        // ============================================================
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "VOICE ENGINE TACTICAL CUSTOMIZATION",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )

                    // Lock Pitch / Rate Globally
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPitchRateLocked) Color(0x2238BDF8) else Color(0x111E293B))
                            .clickable { voiceEngine.togglePitchRateLock() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GLOBAL PITCH/RATE OVERRIDE LOCK",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isPitchRateLocked) SkyBright else Color.White,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = "Locks Pitch and Rate globally across all voice templates.",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 7.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isPitchRateLocked) EmeraldNominal else Color(0x22FFFFFF))
                                .border(1.dp, if (isPitchRateLocked) EmeraldNominal else Color(0x44FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPitchRateLocked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Locked",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    // Pitch Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PITCH / TIMBRE:",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.2fx", if (isPitchRateLocked) globalPitch else voiceEngine.pitch),
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )
                        }
                        Slider(
                            value = if (isPitchRateLocked) globalPitch else voiceEngine.pitch,
                            onValueChange = {
                                if (isPitchRateLocked) {
                                    voiceEngine.updateGlobalPitch(it)
                                } else {
                                    voiceEngine.pitch = it
                                }
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBright,
                                activeTrackColor = SkyBright,
                                inactiveTrackColor = Color(0x3338BDF8)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Speech Rate Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CADENCE / SPEECH SPEED:",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.2fx", if (isPitchRateLocked) globalRate else voiceEngine.speechRate),
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )
                        }
                        Slider(
                            value = if (isPitchRateLocked) globalRate else voiceEngine.speechRate,
                            onValueChange = {
                                if (isPitchRateLocked) {
                                    voiceEngine.updateGlobalRate(it)
                                } else {
                                    voiceEngine.speechRate = it
                                }
                            },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldNominal,
                                activeTrackColor = EmeraldNominal,
                                inactiveTrackColor = Color(0x3322C55E)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Chime Alert Sound Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChimeEnabled) Color(0x2210B981) else Color(0x111E293B))
                            .clickable { voiceEngine.toggleChimeEnabled() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TACTICAL CHIME SOUND",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isChimeEnabled) EmeraldNominal else Color.White,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = "Play holographic confirmation tone before speaking.",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 7.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isChimeEnabled) EmeraldNominal else Color(0x22FFFFFF))
                                .border(1.dp, if (isChimeEnabled) EmeraldNominal else Color(0x44FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChimeEnabled) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Chime Active",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    // TTS Volume Control
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SPEECH OUTPUT VOLUME:",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.0f%%", ttsVolume * 100f),
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.5.sp
                            )
                        }
                        Slider(
                            value = ttsVolume,
                            onValueChange = { voiceEngine.updateTtsVolume(it) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = SkyBright,
                                activeTrackColor = SkyBright,
                                inactiveTrackColor = Color(0x3338BDF8)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Test Phrase Input
                    Text(
                        text = "TEST TRANSMISSION SENTENCE:",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                    BasicTextField(
                        value = testPhrase,
                        onValueChange = { testPhrase = it },
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
                            .border(
                                1.dp,
                                Color(0x3338BDF8),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(6.dp)
                    )
                }
            }
        }

        // ============================================================
        // 5. VOICE MODEL MANAGER
        // ============================================================

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "VOICE OUTPUT MODELS (${allModels.size})",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SkyBright,
                        fontSize = 9.5.sp
                    )

                    Text(
                        text = "All models are voice outputs for the same assistant.",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 7.5.sp
                    )
                }

                Button(
                    onClick = {
                        showAddDialog = !showAddDialog
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x3338BDF8)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    ),
                    modifier = Modifier.height(24.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = SkyBright,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(3.dp)
                    )

                    Text(
                        text = if (showAddDialog) {
                            "CANCEL"
                        } else {
                            "ADD MODEL FILE"
                        },
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.sp
                    )
                }
            }
        }

        // ============================================================
        // 6. CUSTOM MODEL IMPORT
        // ============================================================

        if (showAddDialog) {

            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = EmeraldNominal,
                            backgroundColor = Color(0xF2040E16)
                        )
                        .padding(10.dp)
                ) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = "IMPORT CUSTOM VOICE MODEL",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldNominal,
                            fontSize = 9.5.sp
                        )

                        Text(
                            text = "SUPPORTED: .ONNX / .TFLITE / .BIN / .WAV",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.5.sp
                        )

                        Text(
                            text = "MODEL NAME:",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyBright,
                            fontSize = 8.sp
                        )

                        BasicTextField(
                            value = newModelName,
                            onValueChange = {
                                newModelName = it
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
                                .border(
                                1.dp,
                                Color(0x3338BDF8),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(6.dp)
                        )

                        Text(
                            text = "STORAGE PATH / FILE LOCATION:",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyBright,
                            fontSize = 8.sp
                        )

                        BasicTextField(
                            value = newModelPath,
                            onValueChange = {
                                newModelPath = it
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color(0xFF86EFAC)
                            ),
                            cursorBrush = SolidColor(Color(0xFF86EFAC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF03070E))
                                .border(
                                1.dp,
                                Color(0x3338BDF8),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(6.dp)
                        )

                        Text(
                            text = "DESCRIPTION & TIMBRE NOTES:",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyBright,
                            fontSize = 8.sp
                        )

                        BasicTextField(
                            value = newModelDescription,
                            onValueChange = {
                                newModelDescription = it
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color.White
                            ),
                            cursorBrush = SolidColor(SkyBright),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF03070E))
                                .border(
                                1.dp,
                                Color(0x3338BDF8),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(6.dp)
                        )

                        Button(
                            onClick = {

                                if (newModelName.isNotBlank()) {

                                    voiceEngine.addCustomVoiceModel(
                                        name = newModelName,
                                        type = newModelType,
                                        description = newModelDescription,
                                        filePath = newModelPath
                                    )

                                    showAddDialog = false
                                    newModelName = ""

                                    Toast.makeText(
                                        context,
                                        "Custom Voice Model Loaded Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Please enter a model name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldNominal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = "INSTALL VOICE OUTPUT MODEL",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // 7. AVAILABLE VOICE OUTPUT MODELS
        // ============================================================

        items(allModels) { model ->

            val isSelected = activeModel.id == model.id

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = if (isSelected) {
                            SkyBright
                        } else {
                            Color(0x2E38BDF8)
                        },
                        backgroundColor = if (isSelected) {
                            Color(0xF2061226)
                        } else {
                            Color(0xF2050B16)
                        }
                    )
                    .clickable {
                        voiceEngine.selectVoiceModel(model)
                    }
                    .padding(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            SkyBright
                                        } else {
                                            JarvisTextMuted
                                        }
                                    )
                            )

                            Text(
                                text = model.name,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )

                            if (model.isCustomUserUploaded) {

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            EmeraldNominal.copy(alpha = 0.2f)
                                        )
                                        .padding(
                                            horizontal = 4.dp,
                                            vertical = 1.dp
                                        )
                                ) {

                                    Text(
                                        text = "CUSTOM",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = EmeraldNominal,
                                        fontSize = 7.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = model.description,
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 8.5.sp
                        )

                        Text(
                            text = "Locale: ${model.localeTag}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyAccent,
                            fontSize = 7.5.sp
                        )

                        if (model.modelFilePath != null) {

                            Text(
                                text = "Path: ${model.modelFilePath}",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 7.5.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        if (model.isCustomUserUploaded) {

                            IconButton(
                                onClick = {
                                    voiceEngine.deleteVoiceModel(model.id)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CrimsonDanger,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                voiceEngine.selectVoiceModel(model)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) {
                                    SkyBright
                                } else {
                                    Color(0x221E293B)
                                }
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            ),
                            modifier = Modifier.height(24.dp)
                        ) {

                            Text(
                                text = if (isSelected) {
                                    "ACTIVE"
                                } else {
                                    "SELECT"
                                },
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) {
                                    Color.Black
                                } else {
                                    JarvisTextMuted
                                },
                                fontSize = 7.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
