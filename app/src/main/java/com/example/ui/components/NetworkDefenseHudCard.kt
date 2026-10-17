package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun NetworkDefenseHudCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val wifiState by viewModel.wifiTelemetry.collectAsState()
    val dnsDiag by viewModel.dnsDiagnostic.collectAsState()
    val isDnsProbeRunning by viewModel.isDnsProbeRunning.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisSurfaceDark.copy(alpha = 0.85f),
                        Color(0xFF07121C).copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        JarvisCyan.copy(alpha = 0.5f),
                        if (dnsDiag.detectedProxyInterception) JarvisAlertRed else JarvisCyanBright.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (wifiState.isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Wi-Fi Status",
                        tint = if (wifiState.isWifiConnected) JarvisCyanBright else JarvisTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NETWORK & DEFENSE SHIELD",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (wifiState.isWifiConnected) "SSID: ${wifiState.ssid} (${wifiState.signalPercent}%)" else "Wi-Fi Interface Standby",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Security Badge
                    val badgeColor = if (dnsDiag.detectedProxyInterception || wifiState.isSecurityOpenRisk) JarvisAlertRed else JarvisCyanBright
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (dnsDiag.detectedProxyInterception) "PROXY DETECTED" else "SECURED",
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Expand",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Action 1: Fix Wi-Fi
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.executeWifiFix() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Fix Wi-Fi",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RESET WI-FI",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Action 2: Flush DNS Proxy
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dnsDiag.detectedProxyInterception) JarvisAlertRed.copy(alpha = 0.25f) else JarvisCyan.copy(alpha = 0.15f))
                        .border(1.dp, if (dnsDiag.detectedProxyInterception) JarvisAlertRed else JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.executeDnsProxyFix() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Flush DNS Proxy",
                            tint = if (dnsDiag.detectedProxyInterception) JarvisAlertRed else JarvisCyanBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FLUSH DNS",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Action 3: Lockdown
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisAlertRed.copy(alpha = 0.2f))
                        .border(1.dp, JarvisAlertRed.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.executeDefenseLockdown() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lockdown",
                            tint = JarvisAlertRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOCKDOWN",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Expanded Telemetry & Diagnostic Probe Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JarvisCyan.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wi-Fi Telemetry breakdown
                    Text(
                        text = "[ WI-FI & NETWORK IDENTITY TELEMETRY ]",
                        color = JarvisCyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "IP Address: ${wifiState.ipAddress} | Gateway: ${wifiState.gatewayIp}\nInterface: ${wifiState.networkInterfaceName} | MAC Mode: ${wifiState.macAddressMode}\nSpeed: ${wifiState.linkSpeedMbps} Mbps | Signal: ${wifiState.rssiDbm} dBm | Freq: ${wifiState.frequencyMhz} MHz\nDNS Servers: ${wifiState.dnsServers.joinToString(", ")}",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // IP & MAC Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // IP Renewal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisCyan.copy(alpha = 0.2f))
                                .border(1.dp, JarvisCyanBright, RoundedCornerShape(8.dp))
                                .clickable { viewModel.executeRenewIpLease() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡ RENEW IP LEASE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // MAC Privacy Settings
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisCyan.copy(alpha = 0.2f))
                                .border(1.dp, JarvisCyanBright, RoundedCornerShape(8.dp))
                                .clickable { viewModel.executeChangeMacPrivacy() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛡️ MAC PRIVACY",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // DNS Diagnostic Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ DNS PROXY DIAGNOSTIC ]",
                            color = JarvisCyanBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisCyan.copy(alpha = 0.2f))
                                .border(1.dp, JarvisCyan, RoundedCornerShape(6.dp))
                                .clickable(enabled = !isDnsProbeRunning) { viewModel.runDnsDiagnosticProbe() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDnsProbeRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = JarvisCyanBright,
                                        strokeWidth = 1.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Probe DNS",
                                        tint = JarvisCyanBright,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDnsProbeRunning) "PROBING..." else "RUN PROBE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (dnsDiag.cloudflareLatencyMs > 0 || dnsDiag.diagnosticSummary.isNotBlank()) {
                        Text(
                            text = "Cloudflare (1.1.1.1): ${dnsDiag.cloudflareLatencyMs} ms | Google (8.8.8.8): ${dnsDiag.googleLatencyMs} ms\nQuad9 (9.9.9.9): ${dnsDiag.quad9LatencyMs} ms | Host Resolved: ${dnsDiag.resolvedHostIp}\nActive DNS: ${dnsDiag.activeDnsServers}",
                            color = JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dnsDiag.diagnosticSummary,
                            color = if (dnsDiag.isDnsHealthy) JarvisCyan else JarvisAmber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (dnsDiag.remediationAdvice.isNotBlank()) {
                            Text(
                                text = "Fix Advice: ${dnsDiag.remediationAdvice}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = "Tap 'RUN PROBE' to execute real concurrent socket resolution tests across Cloudflare, Google, and Quad9 DoT endpoints.",
                            color = JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
