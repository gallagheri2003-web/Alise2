package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TacticalTerminalLog(
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO,
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogLevel(val color: Color) {
    INFO(AlienEmpathyCyan),
    WARN(AlienHarmonyGold),
    ERROR(Color(0xFFFF5252)),
    SUCCESS(AlienCelestialEmerald),
    NEURAL(AlienBioluminescentMagenta)
}

/**
 * Overlay Tactical Code Terminal component.
 * Mimics a command-line interface with scrolling system-level status logs while using the HUD.
 */
@Composable
fun TacticalCodeTerminalOverlay(
    modifier: Modifier = Modifier,
    isExpandedDefault: Boolean = false,
    onExecuteCommand: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(isExpandedDefault) }
    var commandInput by remember { mutableStateOf("") }
    
    val logs = remember {
        mutableStateListOf(
            TacticalTerminalLog("BOOT", "One UI 8.5 / 9 Spatial Kernel initialized.", LogLevel.SUCCESS),
            TacticalTerminalLog("SYMBIONT", "Alien-Humanoid neural bridge active on port 8080.", LogLevel.NEURAL),
            TacticalTerminalLog("SENSORS", "Telemetry delta nominal: Temp 33.4°C, NPU 12%", LogLevel.INFO),
            TacticalTerminalLog("QUANTUM", "Vault encryption lattice verified (AES-256-GCM).", LogLevel.SUCCESS),
            TacticalTerminalLog("A.L.I.S.E.", "Multimodal Live voice synth stream standing by.", LogLevel.NEURAL)
        )
    }

    val listState = rememberLazyListState()

    // Background streaming status log simulation
    LaunchedEffect(Unit) {
        val sampleLogPool = listOf(
            TacticalTerminalLog("KERNEL", "Thread #04 allocated 128MB RAM in spatial sandbox.", LogLevel.INFO),
            TacticalTerminalLog("SYMBIONT", "Neural link sync ping: 1.1ms latency.", LogLevel.NEURAL),
            TacticalTerminalLog("ONE_UI_9", "Compositing fluid glass widgets at 120 FPS.", LogLevel.SUCCESS),
            TacticalTerminalLog("SENSORS", "Thermals stable: Snapdragon 8 Gen 3 core 34.0°C.", LogLevel.INFO),
            TacticalTerminalLog("SECURITY", "Network defense perimeter scanning active ports...", LogLevel.WARN),
            TacticalTerminalLog("HUD", "Reticle target tracking locked on MGRS coordinates.", LogLevel.INFO),
            TacticalTerminalLog("VAULT", "Memory vault checksum pass (0x8F9A321B).", LogLevel.SUCCESS)
        )

        var logIndex = 0
        while (true) {
            delay(3200)
            val logItem = sampleLogPool[logIndex % sampleLogPool.size].copy(timestamp = System.currentTimeMillis())
            logs.add(logItem)
            if (logs.size > 80) logs.removeAt(0)
            logIndex++
            if (isExpanded) {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .alienSymbiontSurface(
                cornerRadius = 16.dp,
                glassColor = Color(0xF5040916),
                borderColor = AlienEmpathyCyan.copy(alpha = 0.5f),
                borderWidth = 1.dp
            )
    ) {
        Column {
            // Terminal Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "CLI Terminal",
                        tint = AlienEmpathyCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "TACTICAL CODE TERMINAL • CLI LOGS",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )

                    // Live pulse tag
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AlienCelestialEmerald)
                            .size(6.dp)
                    )
                    Text(
                        text = "${logs.size} LOGS",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AlienCelestialEmerald,
                        fontSize = 8.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle CLI",
                            tint = AlienEmpathyCyan
                        )
                    }
                }
            }

            // Expanded CLI View
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    // Terminal Log Output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF020610))
                            .border(1.dp, Color(0x3300F5FF), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(logs, key = { it.id }) { log ->
                                val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "[$timeStr]",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = JarvisTextMuted,
                                        fontSize = 8.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "[${log.tag}]",
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = log.level.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = log.message,
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 8.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Command Input Line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF07111F))
                            .border(1.dp, Color(0x4400F5FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "alise@oneui9:~$ ",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AlienEmpathyCyan,
                            fontSize = 10.sp
                        )

                        BasicTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.White
                            ),
                            cursorBrush = SolidColor(AlienEmpathyCyan),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commandInput.isNotBlank()) {
                                        val cmd = commandInput.trim()
                                        logs.add(TacticalTerminalLog("CMD", "> $cmd", LogLevel.NEURAL))
                                        
                                        when (cmd.lowercase()) {
                                            "status" -> logs.add(TacticalTerminalLog("SYS", "All 18 modules nominal. Thermal 33°C. NPU active.", LogLevel.SUCCESS))
                                            "clear" -> logs.clear()
                                            "oneui" -> logs.add(TacticalTerminalLog("ONEUI", "One UI 8.5 / 9 Spatial Glass Grid active at 120Hz.", LogLevel.SUCCESS))
                                            "ping" -> logs.add(TacticalTerminalLog("NET", "Neural ping: 0.8ms to Gemini 3.5 Flash.", LogLevel.INFO))
                                            "help" -> logs.add(TacticalTerminalLog("HELP", "Commands: status, clear, oneui, ping, logs, help", LogLevel.INFO))
                                            else -> {
                                                logs.add(TacticalTerminalLog("EXEC", "Executed: $cmd", LogLevel.INFO))
                                                onExecuteCommand?.invoke(cmd)
                                            }
                                        }
                                        commandInput = ""
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tactical_cli_input")
                        )

                        IconButton(
                            onClick = {
                                if (commandInput.isNotBlank()) {
                                    val cmd = commandInput.trim()
                                    logs.add(TacticalTerminalLog("CMD", "> $cmd", LogLevel.NEURAL))
                                    logs.add(TacticalTerminalLog("EXEC", "Executed: $cmd", LogLevel.INFO))
                                    onExecuteCommand?.invoke(cmd)
                                    commandInput = ""
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Run Command",
                                tint = AlienEmpathyCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
