package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.api.AiLlmEngine
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER", "ALISE", "SYSTEM"
    val fullText: String,
    var displayedText: String = "",
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date()),
    val metadata: String? = null,
    val isComplete: Boolean = true
)

@Composable
fun TerminalScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val telemetry by viewModel.telemetry.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val terminalMessages = remember {
        mutableStateListOf(
            TerminalLine(
                sender = "SYSTEM",
                fullText = "A.L.I.S.E. TACTICAL TERMINAL SHELL [v4.9.0-S25]\nKernel: Linux 6.6.21-android15 | Target: Galaxy S25 Snapdragon 8 Elite\nType any natural query or tactical CLI command. Real AI neural core active.",
                displayedText = "A.L.I.S.E. TACTICAL TERMINAL SHELL [v4.9.0-S25]\nKernel: Linux 6.6.21-android15 | Target: Galaxy S25 Snapdragon 8 Elite\nType any natural query or tactical CLI command. Real AI neural core active.",
                isComplete = true
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    fun sendTerminalQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || isProcessing) return

        inputCommand = ""
        isProcessing = true
        viewModel.soundSynthesizer.playKeyClickTone()

        // 1. Add User Command Line
        val userLine = TerminalLine(
            sender = "USER",
            fullText = trimmed,
            displayedText = trimmed,
            isComplete = true
        )
        terminalMessages.add(userLine)

        // 2. Query Real AI / Command Processor & Stream Typewriter
        coroutineScope.launch {
            listState.animateScrollToItem(terminalMessages.size - 1)

            val aiResult = AiLlmEngine.queryAi(
                userInput = trimmed,
                telemetry = telemetry,
                locationState = locationState,
                conversationHistory = terminalMessages.takeLast(6).map { Pair(it.sender, it.fullText) }
            )

            // Add placeholder for typewriter streaming
            val aliseLine = TerminalLine(
                sender = "ALISE",
                fullText = aiResult.responseText,
                displayedText = "",
                metadata = "Δt: ${aiResult.latencyMs}ms | Engine: ${aiResult.providerUsed}",
                isComplete = false
            )
            terminalMessages.add(aliseLine)
            val lineIndex = terminalMessages.size - 1

            // Character-by-character typewriter animation
            val full = aiResult.responseText
            val sb = StringBuilder()
            val stepSize = if (full.length > 200) 3 else 1
            var i = 0
            while (i < full.length) {
                val nextChunk = full.substring(i, (i + stepSize).coerceAtMost(full.length))
                sb.append(nextChunk)
                terminalMessages[lineIndex] = terminalMessages[lineIndex].copy(
                    displayedText = sb.toString()
                )
                i += stepSize
                delay(12)
                listState.animateScrollToItem(lineIndex)
            }

            terminalMessages[lineIndex] = terminalMessages[lineIndex].copy(isComplete = true)
            isProcessing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060B14))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // TOP TERMINAL STATUS STRIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3322C55E),
                        backgroundColor = Color(0xD9050F18)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Text(
                        text = "ALISE@S25-TACTICAL: TTY_01",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E),
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "MGRS: ${locationState.mgrsCoordinate.take(10)} | CPU: ${telemetry.coreTemperature}°C",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = SkyBright,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MAIN TERMINAL OUTPUT STREAM
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x2E38BDF8),
                        backgroundColor = Color(0xF2040812)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(terminalMessages, key = { it.id }) { msg ->
                    when (msg.sender) {
                        "USER" -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "[${msg.timestamp}] ",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = JarvisTextMuted,
                                        fontSize = 8.5.sp
                                    )
                                    Text(
                                        text = "COMMANDER@ALISE:~$ ",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBright,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = msg.fullText,
                                        style = Typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        "ALISE" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x260E2A47))
                                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ALISE://RESPONSE ",
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E),
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = "[${msg.timestamp}]",
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = JarvisTextMuted,
                                            fontSize = 8.sp
                                        )
                                    }
                                    if (msg.metadata != null) {
                                        Text(
                                            text = msg.metadata,
                                            style = Typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = AmberWarning,
                                            fontSize = 8.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row {
                                    Text(
                                        text = msg.displayedText,
                                        style = Typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF86EFAC), // Phosphor green tint
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (!msg.isComplete && cursorAlpha > 0.5f) {
                                        Text(
                                            text = " █",
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF22C55E),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                        "SYSTEM" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x221E293B))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg.displayedText,
                                    style = Typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBright,
                                    fontSize = 9.5.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }

                if (isProcessing) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ALISE@S25-TACTICAL:~$ SYNTHESIZING NEURAL OUTPUT...",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AmberWarning,
                                fontSize = 9.5.sp
                            )
                            if (cursorAlpha > 0.5f) {
                                Text(text = "█", color = AmberWarning, fontSize = 9.5.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // QUICK COMMAND CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickChips = listOf(
                    "sys.audit" to "Run complete system & hardware audit",
                    "mgrs.fix" to "Display MGRS precision coordinates",
                    "wifi.recon" to "Scan Wi-Fi and network DNS",
                    "optics.engage" to "Activate tactical optics HUD",
                    "code.patch" to "Self-audit and apply code hot-patch",
                    "tether.test" to "Test setting control and auto-return"
                )

                quickChips.forEach { (cmd, hint) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC050E1A))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                            .clickable {
                                sendTerminalQuery(cmd)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cmd,
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TERMINAL COMMAND INPUT FIELD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x4D22C55E),
                        backgroundColor = Color(0xF2040914)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALISE@S25:~$ ",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E),
                    fontSize = 11.sp
                )

                BasicTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(Color(0xFF22C55E)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendTerminalQuery(inputCommand) }),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field")
                )

                if (inputCommand.isNotBlank()) {
                    IconButton(
                        onClick = { sendTerminalQuery(inputCommand) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
