package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.SystemLogEntity
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisGlassBackground
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Scrollable, semi-transparent terminal interface for system logging, telemetry checks,
 * command execution status, and interactive CLI input.
 */
@Composable
fun JarvisTerminal(
    logs: List<SystemLogEntity>,
    onClearLogs: () -> Unit,
    onRunHealthCheck: () -> Unit,
    onSendCommand: (String) -> Unit,
    modifier: Modifier = Modifier,
    scanlineIntensity: Float = 0.28f,
    testTag: String = "jarvis_terminal_console"
) {
    var commandInput by remember { mutableStateOf("") }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    // Auto scroll to latest log
    LaunchedEffect(logs.size, autoScrollEnabled) {
        if (autoScrollEnabled && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "JARVIS TERMINAL CONSOLE v3.2",
        statusTag = "TELEMETRY LINKED",
        accentColor = JarvisCyanBright,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Action Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisGlassPanel.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(JarvisCyanBright)
                    )
                    Text(
                        text = "LOGS: ${logs.size}",
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyanBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Health check diagnostic button
                    Box(
                        modifier = Modifier
                            .testTag("terminal_btn_health_check")
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisCyan.copy(alpha = 0.2f))
                            .border(1.dp, JarvisCyanBright, RoundedCornerShape(6.dp))
                            .clickable { onRunHealthCheck() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = JarvisCyanBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HEALTH CHECK",
                                style = Typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Auto scroll toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (autoScrollEnabled) JarvisCyan.copy(alpha = 0.3f) else JarvisSurfaceDark)
                            .border(1.dp, if (autoScrollEnabled) JarvisCyan else JarvisTextMuted, RoundedCornerShape(6.dp))
                            .clickable { autoScrollEnabled = !autoScrollEnabled }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = "Auto Scroll",
                            tint = if (autoScrollEnabled) JarvisCyanBright else JarvisTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Clear button
                    Box(
                        modifier = Modifier
                            .testTag("terminal_btn_clear_logs")
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceDark)
                            .border(1.dp, JarvisTextMuted.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onClearLogs() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Console",
                            tint = JarvisAlertRed,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Scrollable Console Log Terminal Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisSurfaceDark.copy(alpha = 0.85f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[SYSTEM TERMINAL READY • AWAITING TELEMETRY]",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = JarvisTextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(logs) { log ->
                            val timestampStr = try {
                                dateFormat.format(Date(log.timestamp))
                            } catch (e: Exception) {
                                log.timestamp.toString()
                            }

                            val levelColor = when (log.level.uppercase()) {
                                "TACTICAL", "SUCCESS" -> JarvisCyanBright
                                "ERROR", "ALERT" -> JarvisAlertRed
                                "WARN" -> JarvisArcGold
                                else -> JarvisTextSecondary
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[$timestampStr]",
                                    style = Typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisTextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[${log.tag}]",
                                    style = Typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.message,
                                    style = Typography.bodySmall,
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Interactive CLI Input Prompt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisGlassPanel)
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JARVIS>",
                    style = Typography.labelSmall,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyanBright
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_command_input"),
                    textStyle = Typography.bodySmall.copy(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(JarvisCyanBright),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commandInput.isNotBlank()) {
                                onSendCommand(commandInput.trim())
                                commandInput = ""
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (commandInput.isEmpty()) {
                            Text(
                                text = "Execute CLI directive (e.g. status, diag, ping)...",
                                style = Typography.bodySmall,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted
                            )
                        }
                        innerTextField()
                    }
                )

                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onSendCommand(commandInput.trim())
                            commandInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("terminal_btn_send_command")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Submit Command",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
