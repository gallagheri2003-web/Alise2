package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.DeviceTelemetry
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.Typography

@Composable
fun HudTelemetryBanner(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    HudFrameContainer(
        modifier = modifier,
        title = "GALAXY S25 TELEMETRY MATRIX",
        statusTag = "ONLINE"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery Gauge
            TelemetryRadialGauge(
                label = "POWER",
                value = "${telemetry.batteryLevel}%",
                percent = telemetry.batteryLevel / 100f,
                icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                color = when {
                    telemetry.batteryLevel > 40 -> JarvisCyan
                    telemetry.batteryLevel > 20 -> JarvisArcGold
                    else -> JarvisAlertRed
                }
            )

            // RAM Gauge
            TelemetryRadialGauge(
                label = "RAM LOAD",
                value = "${telemetry.ramUsagePercent}%",
                percent = telemetry.ramUsagePercent / 100f,
                icon = Icons.Default.Memory,
                color = if (telemetry.ramUsagePercent > 80) JarvisAlertRed else JarvisCyan
            )

            // Storage Gauge
            TelemetryRadialGauge(
                label = "STORAGE",
                value = "${telemetry.storageUsagePercent}%",
                percent = telemetry.storageUsagePercent / 100f,
                icon = Icons.Default.Storage,
                color = JarvisArcGold
            )

            // Thermal / Network
            TelemetryRadialGauge(
                label = "CORE TEMP",
                value = "${String.format("%.0f", telemetry.coreTemperature)}°C",
                percent = (telemetry.coreTemperature / 60f).coerceIn(0f, 1f),
                icon = Icons.Default.PhoneAndroid,
                color = if (telemetry.coreTemperature > 45) JarvisAlertRed else JarvisSuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Device & Network Meta Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(JarvisSurfaceDark.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${telemetry.deviceModel} • ONE UI 8.5",
                    style = Typography.labelSmall,
                    color = JarvisTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (telemetry.isNetworkConnected) JarvisSuccessGreen else JarvisAlertRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${telemetry.networkLatencyMs}ms",
                    style = Typography.labelSmall,
                    color = if (telemetry.isNetworkConnected) JarvisSuccessGreen else JarvisAlertRed
                )
            }
        }
    }
}

@Composable
fun TelemetryRadialGauge(
    label: String,
    value: String,
    percent: Float,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 1f),
        label = "gauge_percent"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(54.dp)) {
                val strokeW = 4.dp.toPx()
                val radius = (size.width - strokeW) / 2f
                val topLeft = Offset(strokeW / 2f, strokeW / 2f)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = JarvisBorderGlow.copy(alpha = 0.4f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Active gauge arc
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * animatedPercent,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = Typography.labelLarge,
            fontSize = 11.sp,
            color = JarvisTextPrimary
        )
        Text(
            text = label,
            style = Typography.labelSmall,
            fontSize = 9.sp,
            color = JarvisTextMuted
        )
    }
}
