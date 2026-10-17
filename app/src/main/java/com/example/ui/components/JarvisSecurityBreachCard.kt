package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
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
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography

data class IdentifiedBreach(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val typeCode: String,
    val detailedMessage: String,
    val sourceIpOrBssid: String,
    val severity: String = "CRITICAL",
    val timestamp: String
)

@Composable
fun JarvisSecurityBreachCard(
    activeBreaches: List<IdentifiedBreach>,
    onNeutralizeBreach: (IdentifiedBreach) -> Unit,
    onRunSecurityScan: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "jarvis_security_breach_card"
) {
    val hasBreaches = activeBreaches.isNotEmpty()

    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "NETWORK & SYSTEM BREACH IDENTIFIER",
        statusTag = if (hasBreaches) "${activeBreaches.size} BREACH(ES) DETECTED" else "ALL PIPELINES SECURE",
        accentColor = if (hasBreaches) JarvisAlertRed else JarvisSuccessGreen,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hasBreaches) {
                // List of Active Identified Breaches
                activeBreaches.forEach { breach ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JarvisAlertRed.copy(alpha = 0.15f))
                            .border(1.2.dp, JarvisAlertRed, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GppBad,
                                        contentDescription = "Breach Detected",
                                        tint = JarvisAlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = breach.title.uppercase(),
                                        style = Typography.labelLarge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisAlertRed
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(JarvisAlertRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = breach.severity,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // EXPLICIT BREACH IDENTIFICATION MESSAGE
                            Text(
                                text = "BREACH IDENTIFIED: ${breach.detailedMessage}",
                                style = Typography.bodySmall,
                                fontSize = 10.sp,
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SOURCE: ${breach.sourceIpOrBssid} • TIME: ${breach.timestamp}",
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisArcGold
                                )

                                LiquidGlassButton(
                                    onClick = { onNeutralizeBreach(breach) },
                                    accentColor = JarvisAlertRed,
                                    cornerRadius = 8.dp,
                                    testTag = "btn_neutralize_breach_${breach.id}"
                                ) {
                                    Text(
                                        text = "NEUTRALIZE & BLOCK",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // All Secure Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSuccessGreen.copy(alpha = 0.10f))
                        .border(1.dp, JarvisSuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GppGood,
                            contentDescription = "Secure",
                            tint = JarvisSuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NO ACTIVE BREACHES IDENTIFIED",
                                style = Typography.labelMedium,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisSuccessGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All S25 network sockets, ARP tables & DNS resolution paths verified secure.",
                                style = Typography.bodySmall,
                                fontSize = 9.5.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }
            }

            // Trigger Real-Time Security Scan Button
            LiquidGlassButton(
                onClick = onRunSecurityScan,
                modifier = Modifier.fillMaxWidth(),
                accentColor = JarvisCyanBright,
                testTag = "btn_run_security_scan"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Run Security Scan",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SCAN S25 FOR BREACHES & ANOMALIES",
                        style = Typography.labelLarge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
