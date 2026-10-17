package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.DeviceTelemetry
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Holographic UI Overlay Component for the Assistant.
 * Renders real-time telemetry data (CPU usage, battery health, thermal load, RAM)
 * utilizing Compose Canvas as the high-performance visual rendering engine.
 *
 * Can run as a persistent floating HUD bar or expand into a full tactical telemetry matrix.
 */
@Composable
fun HolographicTelemetryHudOverlay(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier,
    isPersistent: Boolean = true,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var primaryHoloColor by remember { mutableStateOf(AlienEmpathyCyan) }
    var secondaryHoloColor by remember { mutableStateOf(AlienCelestialEmerald) }

    // Waveform history buffer for real-time CPU telemetry drawing
    val cpuHistory = remember { mutableStateListOf(18f, 22f, 19f, 25f, 30f, 28f, 24f, 20f, 26f, 32f, 21f, 18f) }

    LaunchedEffect(telemetry.cpuLoadPercent) {
        if (cpuHistory.size > 20) {
            cpuHistory.removeAt(0)
        }
        cpuHistory.add(telemetry.cpuLoadPercent.toFloat())
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("holographic_telemetry_hud_overlay")
    ) {
        if (!isExpanded) {
            // ==========================================
            // 1. PERSISTENT MINI HOLOGRAPHIC HUD DOCK BAR
            // ==========================================
            PersistentHudDockBar(
                telemetry = telemetry,
                cpuHistory = cpuHistory,
                primaryColor = primaryHoloColor,
                secondaryColor = secondaryHoloColor,
                onExpandClick = onToggleExpand,
                onDismiss = onDismiss
            )
        } else {
            // ==========================================
            // 2. FULL EXPANDED TACTICAL HUD MATRIX
            // ==========================================
            ExpandedTacticalHudMatrix(
                telemetry = telemetry,
                cpuHistory = cpuHistory,
                primaryColor = primaryHoloColor,
                secondaryColor = secondaryHoloColor,
                onColorChange = { p, s ->
                    primaryHoloColor = p
                    secondaryHoloColor = s
                },
                onCollapseClick = onToggleExpand,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Persistent compact HUD dock bar that displays real-time Canvas gauges
 * for CPU and Battery Health with live animated telemetry sparklines.
 */
@Composable
private fun PersistentHudDockBar(
    telemetry: DeviceTelemetry,
    cpuHistory: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    onExpandClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HudDockAnim")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE6050C1B))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.8f * pulseGlow),
                        secondaryColor.copy(alpha = 0.5f),
                        primaryColor.copy(alpha = 0.8f * pulseGlow)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onExpandClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("persistent_hud_dock_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: CPU Canvas Radial Gauge & Readout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HolographicCpuCanvasGauge(
                    cpuLoad = telemetry.cpuLoadPercent,
                    primaryColor = primaryColor,
                    size = 38.dp
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CPU LOAD",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${telemetry.cpuLoadPercent}%",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (telemetry.cpuLoadPercent > 80) CrimsonDanger else if (telemetry.cpuLoadPercent > 50) AmberWarning else primaryColor,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = "${telemetry.cpuTemperatureCelsius}°C • ${telemetry.cpuGovernor.uppercase()}",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 7.5.sp
                    )
                }
            }

            // Center: Real-time Oscilloscope / Telemetry Waveform drawn on Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .padding(horizontal = 8.dp)
            ) {
                HolographicTelemetryWaveformCanvas(
                    dataPoints = cpuHistory,
                    primaryColor = primaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right: Battery Health & Power Canvas Radial Gauge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BATT HEALTH",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = secondaryColor,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${telemetry.batteryLevel}%",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (telemetry.batteryLevel > 20) secondaryColor else CrimsonDanger,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = if (telemetry.isCharging) "CHARGING • ${telemetry.batteryVoltageMv}mV" else "99.4% INTEGRITY",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 7.5.sp
                    )
                }

                HolographicBatteryCanvasGauge(
                    batteryLevel = telemetry.batteryLevel,
                    isCharging = telemetry.isCharging,
                    accentColor = secondaryColor,
                    size = 38.dp
                )

                // Expand Button
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.2f))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                        .testTag("persistent_hud_expand_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand HUD Overlay",
                        tint = primaryColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full expanded tactical HUD canvas matrix with detailed multi-ring meters,
 * octa-core Snapdragon visualizer, battery health diagnostic quadrant,
 * animated scanning reticles, and theme customization.
 */
@Composable
private fun ExpandedTacticalHudMatrix(
    telemetry: DeviceTelemetry,
    cpuHistory: List<Float>,
    primaryColor: Color,
    secondaryColor: Color,
    onColorChange: (Color, Color) -> Unit,
    onCollapseClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "MatrixAnim")

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rot"
    )

    val scanSweepY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_sweep"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xF5040916))
            .border(
                1.2.dp,
                Brush.verticalGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.9f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
            .testTag("expanded_tactical_hud_matrix")
    ) {
        // Holographic Canvas Background & Scanning Overlay
        Canvas(modifier = Modifier.matchParentSize()) {
            drawHolographicBackground(
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                scanProgress = scanSweepY
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Matrix Header Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "HOLOGRAPHIC TELEMETRY HUD MATRIX",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PERSISTENT CORE TELEMETRY • LIVE REFRESH",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.5.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Color Accent Picker
                    IconButton(
                        onClick = {
                            if (primaryColor == AlienEmpathyCyan) {
                                onColorChange(AlienCelestialEmerald, AlienHarmonyGold)
                            } else if (primaryColor == AlienCelestialEmerald) {
                                onColorChange(AlienBioluminescentMagenta, AlienEmpathyCyan)
                            } else if (primaryColor == AlienBioluminescentMagenta) {
                                onColorChange(JarvisArcGold, CrimsonDanger)
                            } else {
                                onColorChange(AlienEmpathyCyan, AlienCelestialEmerald)
                            }
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cycle Holographic Color",
                            tint = primaryColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Minimize / Collapse Button
                    IconButton(
                        onClick = onCollapseClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Collapse to HUD Dock",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Central Dual-Ring Holographic Reactor Dial (Compose Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCentralDualRingReactor(
                        cpuLoad = telemetry.cpuLoadPercent,
                        batteryLevel = telemetry.batteryLevel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        rotationAngle = radarRotation,
                        pulseScale = corePulse
                    )
                }

                // Center Telemetry Text Overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CPU ${telemetry.cpuLoadPercent}%",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = primaryColor,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "BATT ${telemetry.batteryLevel}%",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${telemetry.coreTemperature}°C",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (telemetry.coreTemperature > 45f) CrimsonDanger else EmeraldNominal,
                        fontSize = 8.sp
                    )
                }
            }

            // 3. Dual Quadrants: CPU Cores & Battery Health Diagnostics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quadrant A: Snapdragon Octa-Core CPU Canvas Visualizer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x800A1428))
                        .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CPU LOAD & CORES",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = "8-CORE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldNominal,
                                fontSize = 7.5.sp
                            )
                        }

                        // 8-Core Interactive Canvas Bars
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            drawOctaCoreCpuBars(
                                baseCpuLoad = telemetry.cpuLoadPercent,
                                accentColor = primaryColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GOV: ${telemetry.cpuGovernor.take(8).uppercase()}",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 7.sp
                            )
                            Text(
                                text = "STATUS: NOMINAL",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = primaryColor,
                                fontSize = 7.sp
                            )
                        }
                    }
                }

                // Quadrant B: Battery Health & Integrity Diagnostics
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x800A1428))
                        .border(1.dp, secondaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BATTERY HEALTH",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = secondaryColor,
                                fontSize = 8.5.sp
                            )
                            Text(
                                text = if (telemetry.isCharging) "CHARGING" else "DISCHARGE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (telemetry.isCharging) EmeraldNominal else JarvisArcGold,
                                fontSize = 7.5.sp
                            )
                        }

                        // Battery Capacity Cell Matrix Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            drawBatteryHealthMatrix(
                                batteryLevel = telemetry.batteryLevel,
                                isCharging = telemetry.isCharging,
                                accentColor = secondaryColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${telemetry.batteryVoltageMv}mV • ${telemetry.batteryTempCelsius}°C",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 7.sp
                            )
                            Text(
                                text = "HEALTH: 99.4%",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = secondaryColor,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Live Holographic Waveform Oscilloscope & RAM Bandwidth Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x800A1428))
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REAL-TIME TELEMETRY OSCILLOSCOPE (CPU & FREQUENCY)",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 8.sp
                        )
                        Text(
                            text = "RAM: ${telemetry.ramUsagePercent}% (${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB)",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldNominal,
                            fontSize = 7.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        HolographicTelemetryWaveformCanvas(
                            dataPoints = cpuHistory,
                            primaryColor = primaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 5. System Spec & Persistent HUD Status Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${telemetry.deviceModel} • ${telemetry.processorInfo.take(24)}...",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextMuted,
                    fontSize = 7.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { onCollapseClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DOCK TO PERSISTENT HUD",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

/**
 * Compose Canvas radial gauge specifically tuned for CPU Load and frequency.
 */
@Composable
fun HolographicCpuCanvasGauge(
    cpuLoad: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val animatedPercent by animateFloatAsState(
        targetValue = (cpuLoad / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cpu_percent_anim"
    )

    val gaugeColor = when {
        cpuLoad > 80 -> CrimsonDanger
        cpuLoad > 55 -> AmberWarning
        else -> primaryColor
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = (w.coerceAtMost(h) / 2f) - 3.dp.toPx()

        // 1. Background Circular Track
        drawArc(
            color = Color(0x331E293B),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 2. Active CPU Sweep Arc
        drawArc(
            brush = Brush.sweepGradient(
                listOf(
                    primaryColor.copy(alpha = 0.4f),
                    gaugeColor,
                    gaugeColor
                )
            ),
            startAngle = 135f,
            sweepAngle = 270f * animatedPercent,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Ticks around perimeter
        val tickCount = 12
        for (i in 0..tickCount) {
            val angle = 135f + (270f / tickCount) * i
            val rad = Math.toRadians(angle.toDouble())
            val innerR = radius - 4.dp.toPx()
            val outerR = radius - 1.dp.toPx()
            val start = Offset(
                center.x + (innerR * cos(rad)).toFloat(),
                center.y + (innerR * sin(rad)).toFloat()
            )
            val end = Offset(
                center.x + (outerR * cos(rad)).toFloat(),
                center.y + (outerR * sin(rad)).toFloat()
            )
            drawLine(
                color = if (i.toFloat() / tickCount <= animatedPercent) gaugeColor else Color(0x4494A3B8),
                start = start,
                end = end,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center dot
        drawCircle(
            color = gaugeColor,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}

/**
 * Compose Canvas radial gauge specifically tuned for Battery Health and Level.
 */
@Composable
fun HolographicBatteryCanvasGauge(
    batteryLevel: Int,
    isCharging: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val animatedPercent by animateFloatAsState(
        targetValue = (batteryLevel / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "battery_percent_anim"
    )

    val statusColor = when {
        batteryLevel > 35 -> accentColor
        batteryLevel > 18 -> AmberWarning
        else -> CrimsonDanger
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = (w.coerceAtMost(h) / 2f) - 3.dp.toPx()

        // 1. Background Circular Track
        drawArc(
            color = Color(0x331E293B),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 2. Battery Percentage Arc
        drawArc(
            brush = Brush.sweepGradient(
                listOf(
                    statusColor.copy(alpha = 0.4f),
                    statusColor,
                    statusColor
                )
            ),
            startAngle = 135f,
            sweepAngle = 270f * animatedPercent,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Inner Health Glow Ring
        drawCircle(
            color = if (isCharging) EmeraldNominal.copy(alpha = 0.3f) else statusColor.copy(alpha = 0.15f),
            radius = radius * 0.65f,
            center = center
        )

        // 4. Center Bolt or Indicator Dot
        if (isCharging) {
            val boltPath = Path().apply {
                moveTo(center.x - 2.dp.toPx(), center.y - 5.dp.toPx())
                lineTo(center.x + 3.dp.toPx(), center.y - 1.dp.toPx())
                lineTo(center.x, center.y)
                lineTo(center.x + 2.dp.toPx(), center.y + 5.dp.toPx())
                lineTo(center.x - 3.dp.toPx(), center.y + 1.dp.toPx())
                lineTo(center.x - 1.dp.toPx(), center.y)
                close()
            }
            drawPath(boltPath, color = EmeraldNominal)
        } else {
            drawCircle(
                color = statusColor,
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}

/**
 * Compose Canvas Oscilloscope / Telemetry Waveform Graph.
 * Renders real-time telemetry activity using smooth cubic Bézier curves,
 * gradient baseline fill, and glowing data nodes.
 */
@Composable
fun HolographicTelemetryWaveformCanvas(
    dataPoints: List<Float>,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (w <= 0 || h <= 0 || dataPoints.isEmpty()) return@Canvas

        // Draw baseline grid lines
        val gridLines = 3
        for (i in 1..gridLines) {
            val y = (h / (gridLines + 1)) * i
            drawLine(
                color = primaryColor.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)
        val maxVal = 100f
        val points = dataPoints.mapIndexed { index, value ->
            val clamped = value.coerceIn(5f, maxVal)
            val normalizedY = h - ((clamped / maxVal) * (h - 6.dp.toPx())) - 3.dp.toPx()
            Offset(index * stepX, normalizedY)
        }

        // Draw smooth path
        val strokePath = Path()
        val fillPath = Path()

        strokePath.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, h)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2f
            strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(points.last().x, h)
        fillPath.close()

        // Gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.25f * pulseAlpha),
                    Color.Transparent
                ),
                startY = 0f,
                endY = h
            )
        )

        // Neon Glow line
        drawPath(
            path = strokePath,
            color = primaryColor.copy(alpha = 0.95f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        points.forEach { pt ->
            drawCircle(
                color = primaryColor,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

// -------------------------------------------------------------
// INTERNAL CANVASES FOR EXPANDED TACTICAL MATRIX
// -------------------------------------------------------------

private fun DrawScope.drawHolographicBackground(
    primaryColor: Color,
    secondaryColor: Color,
    scanProgress: Float
) {
    val w = size.width
    val h = size.height

    // Subtle coordinate grid
    val spacing = 24.dp.toPx()
    var x = 0f
    while (x < w) {
        drawLine(
            color = primaryColor.copy(alpha = 0.04f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += spacing
    }

    var y = 0f
    while (y < h) {
        drawLine(
            color = primaryColor.copy(alpha = 0.04f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += spacing
    }

    // Moving Scanbeam Sweep
    val scanY = h * scanProgress
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                primaryColor.copy(alpha = 0.35f),
                Color.Transparent
            )
        ),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 1.8.dp.toPx()
    )

    // Corner Reticle Brackets
    val cornerLen = 14.dp.toPx()
    val stroke = 1.5.dp.toPx()

    // Top-Left
    drawLine(primaryColor, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
    drawLine(primaryColor, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

    // Top-Right
    drawLine(primaryColor, Offset(w, 0f), Offset(w - cornerLen, 0f), stroke)
    drawLine(primaryColor, Offset(w, 0f), Offset(w, cornerLen), stroke)

    // Bottom-Left
    drawLine(secondaryColor, Offset(0f, h), Offset(cornerLen, h), stroke)
    drawLine(secondaryColor, Offset(0f, h), Offset(0f, h - cornerLen), stroke)

    // Bottom-Right
    drawLine(secondaryColor, Offset(w, h), Offset(w - cornerLen, h), stroke)
    drawLine(secondaryColor, Offset(w, h), Offset(w, h - cornerLen), stroke)
}

private fun DrawScope.drawCentralDualRingReactor(
    cpuLoad: Int,
    batteryLevel: Int,
    primaryColor: Color,
    secondaryColor: Color,
    rotationAngle: Float,
    pulseScale: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = (size.minDimension / 2f) * 0.9f

    // 1. Rotating Outer Calibrated Reticle Ring
    rotate(degrees = rotationAngle, pivot = center) {
        val outerRadius = baseRadius
        drawCircle(
            color = primaryColor.copy(alpha = 0.15f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // 8 Directional Ticks
        for (i in 0 until 8) {
            val angle = i * 45f
            val rad = Math.toRadians(angle.toDouble())
            val p1 = Offset(
                center.x + (outerRadius - 6.dp.toPx()) * cos(rad).toFloat(),
                center.y + (outerRadius - 6.dp.toPx()) * sin(rad).toFloat()
            )
            val p2 = Offset(
                center.x + outerRadius * cos(rad).toFloat(),
                center.y + outerRadius * sin(rad).toFloat()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.6f),
                start = p1,
                end = p2,
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }

    // 2. Outer Arc: CPU Load Ring
    val cpuRadius = baseRadius * 0.82f
    val cpuSweep = (cpuLoad / 100f) * 220f
    drawArc(
        color = Color(0x221E293B),
        startAngle = 160f,
        sweepAngle = 220f,
        useCenter = false,
        topLeft = Offset(center.x - cpuRadius, center.y - cpuRadius),
        size = Size(cpuRadius * 2, cpuRadius * 2),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
    drawArc(
        brush = Brush.sweepGradient(
            listOf(primaryColor.copy(alpha = 0.3f), primaryColor, primaryColor)
        ),
        startAngle = 160f,
        sweepAngle = cpuSweep,
        useCenter = false,
        topLeft = Offset(center.x - cpuRadius, center.y - cpuRadius),
        size = Size(cpuRadius * 2, cpuRadius * 2),
        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // 3. Middle Arc: Battery Health Ring
    val battRadius = baseRadius * 0.65f
    val battSweep = (batteryLevel / 100f) * 220f
    drawArc(
        color = Color(0x221E293B),
        startAngle = 160f,
        sweepAngle = 220f,
        useCenter = false,
        topLeft = Offset(center.x - battRadius, center.y - battRadius),
        size = Size(battRadius * 2, battRadius * 2),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    drawArc(
        brush = Brush.sweepGradient(
            listOf(secondaryColor.copy(alpha = 0.3f), secondaryColor, secondaryColor)
        ),
        startAngle = 160f,
        sweepAngle = battSweep,
        useCenter = false,
        topLeft = Offset(center.x - battRadius, center.y - battRadius),
        size = Size(battRadius * 2, battRadius * 2),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // 4. Inner Glowing Pulsing Core
    val innerCoreR = baseRadius * 0.44f * pulseScale
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                primaryColor.copy(alpha = 0.25f),
                secondaryColor.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = center,
            radius = innerCoreR
        ),
        radius = innerCoreR,
        center = center
    )
}

private fun DrawScope.drawOctaCoreCpuBars(
    baseCpuLoad: Int,
    accentColor: Color
) {
    val barCount = 8
    val w = size.width
    val h = size.height
    val barWidth = (w / barCount) * 0.72f
    val gap = (w - (barWidth * barCount)) / (barCount + 1)

    // Individual simulated variation across Snapdragon 8 Elite's 8 cores
    val coreMultipliers = listOf(0.9f, 1.15f, 0.75f, 1.25f, 0.85f, 1.05f, 1.3f, 0.7f)

    for (i in 0 until barCount) {
        val x = gap + i * (barWidth + gap)
        val coreLoad = (baseCpuLoad * coreMultipliers[i]).coerceIn(10f, 100f)
        val fillHeight = (coreLoad / 100f) * h

        // Background slot
        drawRoundRect(
            color = Color(0x331E293B),
            topLeft = Offset(x, 0f),
            size = Size(barWidth, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // Active Load bar
        val barColor = when {
            coreLoad > 85 -> CrimsonDanger
            coreLoad > 60 -> AmberWarning
            else -> accentColor
        }

        drawRoundRect(
            color = barColor,
            topLeft = Offset(x, h - fillHeight),
            size = Size(barWidth, fillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
    }
}

private fun DrawScope.drawBatteryHealthMatrix(
    batteryLevel: Int,
    isCharging: Boolean,
    accentColor: Color
) {
    val w = size.width
    val h = size.height
    val cells = 5
    val cellWidth = (w / cells) * 0.8f
    val gap = (w - (cellWidth * cells)) / (cells + 1)

    val activeCells = ((batteryLevel / 100f) * cells).toInt().coerceIn(1, cells)

    for (i in 0 until cells) {
        val x = gap + i * (cellWidth + gap)
        val isActive = i < activeCells

        // Slot
        drawRoundRect(
            color = if (isActive) accentColor.copy(alpha = 0.85f) else Color(0x331E293B),
            topLeft = Offset(x, 8.dp.toPx()),
            size = Size(cellWidth, h - 16.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )

        // Inner glowing tick
        if (isActive) {
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(x + cellWidth / 2f, 12.dp.toPx()),
                end = Offset(x + cellWidth / 2f, h - 20.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
