package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography

data class AutonomousActionLog(
    val timestamp: String,
    val actionText: String,
    val Category: String = "AUTONOMOUS"
)

@Composable
fun JarvisAutonomousControlCard(
    isAutonomousEnabled: Boolean,
    onToggleAutonomousControl: (Boolean) -> Unit,
    actionLogs: List<AutonomousActionLog>,
    modifier: Modifier = Modifier,
    testTag: String = "jarvis_autonomous_control_card"
) {
    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "J.A.R.V.I.S. AUTONOMOUS DEVICE CONTROL",
        statusTag = if (isAutonomousEnabled) "AUTONOMOUS ACTIVE" else "MANUAL MODE",
        accentColor = if (isAutonomousEnabled) JarvisCyanBright else JarvisArcGold,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Control Switch Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurfaceDark.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (isAutonomousEnabled) JarvisCyanBright else JarvisCyan.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Autonomous AI Control",
                        tint = if (isAutonomousEnabled) JarvisCyanBright else JarvisArcGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GIVE JARVIS CONTROL OF DEVICE",
                            style = Typography.labelLarge,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAutonomousEnabled)
                                "Self-learning engine auto-cleans cache, neutralizes breaches & manages hardware."
                            else
                                "Tap toggle to grant Jarvis autonomous background permissions.",
                            style = Typography.bodySmall,
                            fontSize = 9.5.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                LiquidGlassToggle(
                    checked = isAutonomousEnabled,
                    onCheckedChange = onToggleAutonomousControl,
                    testTag = "toggle_autonomous_control"
                )
            }

            // Active Autonomous Feeds
            AnimatedVisibility(
                visible = isAutonomousEnabled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Autonomous Capabilities Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AutonomousBadge(
                            text = "Auto-Breach Neutralizer",
                            accentColor = JarvisCyanBright,
                            modifier = Modifier.weight(1f)
                        )
                        AutonomousBadge(
                            text = "Auto Data Cleaner",
                            accentColor = JarvisArcGold,
                            modifier = Modifier.weight(1f)
                        )
                        AutonomousBadge(
                            text = "RAM/Thermal Manager",
                            accentColor = JarvisSuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Autonomous Stream Activity Log Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisSurfaceDark.copy(alpha = 0.5f))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = JarvisCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AUTONOMOUS ACTIVITY STREAM",
                                    style = Typography.labelSmall,
                                    fontSize = 9.5.sp,
                                    color = JarvisCyanBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${actionLogs.size} EVENTS",
                                style = Typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (actionLogs.isEmpty()) {
                            Text(
                                text = "Jarvis is monitoring system telemetry in background standby...",
                                style = Typography.bodySmall,
                                fontSize = 9.5.sp,
                                color = JarvisTextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                actionLogs.take(4).forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = JarvisSuccessGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "[${log.timestamp}] ",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = JarvisCyan
                                        )
                                        Text(
                                            text = log.actionText,
                                            fontSize = 9.5.sp,
                                            color = JarvisTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutonomousBadge(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Typography.labelSmall,
            fontSize = 8.5.sp,
            color = accentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
