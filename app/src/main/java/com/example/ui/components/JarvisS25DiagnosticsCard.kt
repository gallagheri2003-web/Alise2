package com.example.ui.components

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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.hardware.DeviceTelemetry
import com.example.ui.theme.JarvisAlertRed
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

@Composable
fun JarvisS25DiagnosticsCard(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier,
    testTag: String = "jarvis_s25_diagnostics_card"
) {
    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "SAMSUNG GALAXY S25 LIVE DIAGNOSTICS",
        statusTag = "SNAPDRAGON 8 ELITE • 120 FPS",
        accentColor = JarvisCyanBright,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: CPU Load & Core Frequencies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticStatBox(
                    modifier = Modifier.weight(1f),
                    title = "CPU CORE LOAD",
                    value = "23% (3.8 GHz)",
                    subText = "8 Cores Nominal",
                    icon = Icons.Default.Speed,
                    accentColor = JarvisCyanBright
                )

                DiagnosticStatBox(
                    modifier = Modifier.weight(1f),
                    title = "RAM ALLOCATION",
                    value = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB",
                    subText = "${telemetry.ramUsagePercent}% In Use",
                    icon = Icons.Default.Memory,
                    accentColor = JarvisCyan
                )
            }

            // Row 2: Battery Health & Core Thermal Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticStatBox(
                    modifier = Modifier.weight(1f),
                    title = "BATTERY HEALTH",
                    value = "${telemetry.batteryLevel}% (${(telemetry.batteryVoltageMv / 1000f)}V)",
                    subText = if (telemetry.isCharging) "Charging • Good Health" else "Discharging • Nominal",
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = if (telemetry.batteryLevel > 20) JarvisSuccessGreen else JarvisAlertRed
                )

                DiagnosticStatBox(
                    modifier = Modifier.weight(1f),
                    title = "CORE THERMALS",
                    value = "${telemetry.coreTemperature}°C",
                    subText = "Throttling: Optimal",
                    icon = Icons.Default.Thermostat,
                    accentColor = if (telemetry.coreTemperature < 42f) JarvisCyanBright else JarvisArcGold
                )
            }

            // Row 3: Storage Split Breakdown Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisSurfaceDark.copy(alpha = 0.5f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = JarvisArcGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STORAGE DISTRIBUTION",
                            style = Typography.labelSmall,
                            fontSize = 9.5.sp,
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${telemetry.storageFreeGb.toInt()} GB Free of ${telemetry.storageTotalGb.toInt()} GB",
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisArcGold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar for Storage Usage
                val usedRatio = (telemetry.storageUsagePercent / 100f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = JarvisArcGold,
                    trackColor = JarvisSurfaceDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "OS System: 24 GB",
                        fontSize = 8.5.sp,
                        color = JarvisTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Apps & Data: ${(telemetry.storageTotalGb - telemetry.storageFreeGb - 24).coerceAtLeast(0.0).toInt()} GB",
                        fontSize = 8.5.sp,
                        color = JarvisTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Latency: ${telemetry.networkLatencyMs}ms",
                        fontSize = 8.5.sp,
                        color = JarvisCyanBright,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceDark.copy(alpha = 0.5f))
            .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = Typography.labelSmall,
                    fontSize = 8.5.sp,
                    color = JarvisTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.titleMedium,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                style = Typography.bodySmall,
                fontSize = 8.5.sp,
                color = JarvisTextMuted
            )
        }
    }
}
