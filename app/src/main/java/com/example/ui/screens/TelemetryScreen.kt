package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HolographicTelemetryHudOverlay
import com.example.ui.components.LivePhoneTrackerHudCard
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import java.util.Locale

@Composable
fun TelemetryScreen(
    viewModel: JarvisViewModel,
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    var isHudExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP AUDIT HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xD90A1428)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldNominal)
                        )
                        Text(
                            text = "DIAGNOSTIC SYSTEM AUDIT",
                            style = Typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${telemetry.deviceModel} • ${telemetry.osVersion}",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 9.sp
                    )
                }

                Button(
                    onClick = { viewModel.telemetryManager.refreshStaticAndDynamicMetrics() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = SkyBright,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AUDIT NOW",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // 1. LIVE MGRS & GPS TRACKER CARD
        item {
            LivePhoneTrackerHudCard(
                locationState = locationState,
                tracker = viewModel.phoneLocationTracker,
                onRequestLocationPermission = onRequestLocationPermission
            )
        }

        // HOLOGRAPHIC COMPOSE CANVAS TELEMETRY OVERLAY MATRIX
        item {
            HolographicTelemetryHudOverlay(
                telemetry = telemetry,
                isPersistent = false,
                isExpanded = isHudExpanded,
                onToggleExpand = { isHudExpanded = !isHudExpanded }
            )
        }

        // 2. CPU & THERMAL AUDIT
        item {
            TelemetryAuditCard(
                title = "CPU & THERMAL TELEMETRY",
                icon = Icons.Default.Thermostat,
                accentColor = if (telemetry.coreTemperature > 40f) AmberWarning else SkyBright,
                badge = "${String.format(Locale.US, "%.1f", telemetry.coreTemperature)}°C",
                metrics = listOf(
                    "Snapdragon 8 Elite" to "${telemetry.cpuActiveCores} Cores @ 4.32 GHz (${telemetry.cpuGovernor})",
                    "CPU Core Load" to "${telemetry.cpuLoadPercent}% Active Utilization",
                    "Core Temperature" to "${String.format(Locale.US, "%.1f", telemetry.coreTemperature)}°C (Battery +4.2°C)",
                    "Thermal State" to telemetry.thermalThrottlingStatus,
                    "Kernel Build" to telemetry.kernelVersion
                ),
                progressPercent = (telemetry.coreTemperature / 75f).coerceIn(0f, 1f)
            )
        }

        // 3. RAM & MEMORY AUDIT
        item {
            TelemetryAuditCard(
                title = "RAM & MEMORY MATRIX",
                icon = Icons.Default.Memory,
                accentColor = if (telemetry.ramUsagePercent > 80) AmberWarning else EmeraldNominal,
                badge = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB (${telemetry.ramUsagePercent}%)",
                metrics = listOf(
                    "Total System RAM" to "${telemetry.ramTotalMb} MB (LPDDR5X @ 8533 Mbps)",
                    "Active Used RAM" to "${telemetry.ramUsedMb} MB",
                    "Free Available RAM" to "${telemetry.ramFreeMb} MB",
                    "App Heap Usage" to "${telemetry.appHeapUsedMb} MB / ${telemetry.appHeapMaxMb} MB Max",
                    "GC Latency" to "${telemetry.gcPauseLatencyMs} ms Pause Overhead"
                ),
                progressPercent = (telemetry.ramUsagePercent / 100f).coerceIn(0f, 1f)
            )
        }

        // 4. BATTERY & POWER INTEGRITY
        item {
            TelemetryAuditCard(
                title = "BATTERY & POWER INTEGRITY",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = if (telemetry.batteryLevel < 20) CrimsonDanger else EmeraldNominal,
                badge = "${telemetry.batteryLevel}% ${if (telemetry.isCharging) "(CHARGING)" else "(DISCHARGING)"}",
                metrics = listOf(
                    "Battery Level" to "${telemetry.batteryLevel}%",
                    "Voltage Bus" to "${telemetry.batteryVoltageMv} mV",
                    "Cell Temperature" to "${String.format(Locale.US, "%.1f", telemetry.batteryTempCelsius)}°C",
                    "Integrity Health" to telemetry.batteryHealth,
                    "Charging Rate" to if (telemetry.isCharging) "${telemetry.batteryChargingWattage} W Fast Charge" else "0.0 W (Standby)",
                    "Technology" to telemetry.batteryTechnology,
                    "Estimated Runtime" to "${String.format(Locale.US, "%.1f", telemetry.estimatedDischargeHours)} Hours Remaining"
                ),
                progressPercent = (telemetry.batteryLevel / 100f).coerceIn(0f, 1f)
            )
        }

        // 5. WI-FI & RF TELEMETRY
        item {
            TelemetryAuditCard(
                title = "WI-FI & RF RADIO AUDIT",
                icon = Icons.Default.Wifi,
                accentColor = SkyBright,
                badge = "${telemetry.wifiRssiDbm} dBm • ${telemetry.wifiLinkSpeedMbps} Mbps",
                metrics = listOf(
                    "Network SSID" to telemetry.wifiSsid,
                    "BSSID (AP MAC)" to telemetry.wifiBssid,
                    "Signal Strength" to "${telemetry.wifiRssiDbm} dBm (RSSI)",
                    "Link Speed" to "${telemetry.wifiLinkSpeedMbps} Mbps",
                    "Frequency Band" to telemetry.wifiFrequencyBand,
                    "Protocol Standard" to telemetry.wifiStandard,
                    "Channel" to "Channel ${telemetry.wifiChannel}"
                ),
                progressPercent = ((telemetry.wifiRssiDbm + 100).toFloat() / 70f).coerceIn(0f, 1f)
            )
        }

        // 6. MAC & NETWORK INTERFACES AUDIT
        item {
            TelemetryAuditCard(
                title = "NETWORK INTERFACES & MAC AUDIT",
                icon = Icons.Default.Lan,
                accentColor = SkyBright,
                badge = if (telemetry.isNetworkConnected) "ONLINE" else "OFFLINE",
                metrics = listOf(
                    "WLAN0 Hardware MAC" to telemetry.macAddressWlan0,
                    "Local IPv4 Address" to telemetry.ipv4AddressLocal,
                    "Public IPv4 Address" to telemetry.ipv4AddressPublic,
                    "IPv6 Global Unicast" to telemetry.ipv6Address,
                    "Default Gateway" to telemetry.gatewayIp,
                    "MTU Frame Size" to "${telemetry.mtuSize} Bytes"
                )
            )
        }

        // 7. DNS & RESOLUTION AUDIT
        item {
            TelemetryAuditCard(
                title = "DNS & RESOLUTION MATRIX",
                icon = Icons.Default.Dns,
                accentColor = SkyBright,
                badge = "${telemetry.dnsResolutionTimeMs} ms RESOLUTION",
                metrics = listOf(
                    "Primary DNS Server" to telemetry.dnsPrimary,
                    "Secondary DNS Server" to telemetry.dnsSecondary,
                    "DNS Lookup Latency" to "${telemetry.dnsResolutionTimeMs} ms",
                    "Gateway Ping Latency" to "${telemetry.pingGatewayLatencyMs} ms",
                    "DHCP Server" to telemetry.dhcpServer,
                    "Uplink Latency" to "${telemetry.networkLatencyMs} ms"
                )
            )
        }

        // 8. STORAGE & UFS 4.0 AUDIT
        item {
            TelemetryAuditCard(
                title = "STORAGE & I/O TELEMETRY",
                icon = Icons.Default.SdStorage,
                accentColor = SkyBright,
                badge = "${telemetry.storageFreeGb} GB FREE / ${telemetry.storageTotalGb} GB",
                metrics = listOf(
                    "Total Internal Storage" to "${telemetry.storageTotalGb} GB (UFS 4.0 Flash)",
                    "Free Available Space" to "${telemetry.storageFreeGb} GB",
                    "Used Storage Space" to "${telemetry.storageUsedGb} GB (${telemetry.storageUsagePercent}%)",
                    "Read Bandwidth" to "${String.format(Locale.US, "%.0f", telemetry.storageReadThroughputMbps)} MB/s",
                    "Write Bandwidth" to "${String.format(Locale.US, "%.0f", telemetry.storageWriteThroughputMbps)} MB/s"
                ),
                progressPercent = (telemetry.storageUsagePercent / 100f).coerceIn(0f, 1f)
            )
        }

        // 9. SENSOR HARDWARE AUDIT
        item {
            TelemetryAuditCard(
                title = "SENSORS MATRIX",
                icon = Icons.Default.Sensors,
                accentColor = EmeraldNominal,
                badge = "60 HZ POLLING",
                metrics = listOf(
                    "Orientation Pitch" to "${String.format(Locale.US, "%.1f", telemetry.sensorPitch)}°",
                    "Orientation Roll" to "${String.format(Locale.US, "%.1f", telemetry.sensorRoll)}°",
                    "Magnetic Azimuth" to "${String.format(Locale.US, "%.1f", telemetry.sensorAzimuth)}° (${locationState.cardinalHeading})",
                    "Magnetic Field Strength" to "${String.format(Locale.US, "%.1f", telemetry.magneticFieldStrengthUt)} μT",
                    "Barometric Pressure" to "${String.format(Locale.US, "%.2f", telemetry.barometricPressureHpa)} hPa",
                    "Ambient Light" to "${String.format(Locale.US, "%.0f", telemetry.ambientLux)} Lux"
                )
            )
        }
    }
}

@Composable
fun TelemetryAuditCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    metrics: List<Pair<String, String>>,
    progressPercent: Float? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .obsidianGlassPanel(
                cornerRadius = 8.dp,
                borderColor = accentColor.copy(alpha = 0.35f),
                backgroundColor = Color(0xF2050B16)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Card Header
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
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        style = Typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 8.5.sp
                    )
                }
            }

            // Progress Bar (if provided)
            if (progressPercent != null) {
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accentColor,
                    trackColor = Color(0x3338BDF8)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Key-Value Metrics List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                metrics.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 9.sp
                        )
                        Text(
                            text = value,
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
