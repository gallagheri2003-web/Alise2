package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.DeviceTelemetry
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography

@Composable
fun HolographicTelemetryModule(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier,
    coreColor: Color = SkyBright
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .obsidianGlassPanel(
                cornerRadius = 10.dp,
                borderColor = coreColor.copy(alpha = 0.35f),
                backgroundColor = Color(0xF2020612)
            )
            .holographicBreathingGlow(color = coreColor, cornerRadius = 10.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SYSTEM TELEMETRY LINK",
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = coreColor,
                fontSize = 9.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery
                TelemetryReadout(
                    icon = Icons.Default.BatteryFull,
                    label = "PWR",
                    value = "${telemetry.batteryLevel}%",
                    statusColor = when {
                        telemetry.batteryLevel > 40 -> EmeraldNominal
                        telemetry.batteryLevel > 20 -> AmberWarning
                        else -> JarvisAlertRed
                    }
                )

                // CPU
                TelemetryReadout(
                    icon = Icons.Default.Memory,
                    label = "CPU",
                    value = "${telemetry.cpuLoadPercent}%",
                    statusColor = if (telemetry.cpuLoadPercent > 80) JarvisAlertRed else if (telemetry.cpuLoadPercent > 60) AmberWarning else JarvisCyan
                )

                // Network
                TelemetryReadout(
                    icon = Icons.Default.NetworkCheck,
                    label = "NET",
                    value = if (telemetry.networkLatencyMs > 0 && telemetry.networkLatencyMs < 999) "${telemetry.networkLatencyMs}ms" else "OFFLINE",
                    statusColor = if (telemetry.networkLatencyMs in 1..99) EmeraldNominal else if (telemetry.networkLatencyMs in 100..299) AmberWarning else JarvisAlertRed
                )
            }
        }
    }
}

@Composable
private fun TelemetryReadout(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    statusColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = statusColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = JarvisTextMuted,
                fontSize = 7.sp
            )
            Text(
                text = value,
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                fontSize = 10.sp
            )
        }
    }
}
