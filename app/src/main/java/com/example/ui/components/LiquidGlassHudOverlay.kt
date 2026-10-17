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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisBorderActive
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGlassBackground
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisHudScanline
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.Typography

/**
 * Custom Compose Holographic Liquid Glass HUD Overlay UI.
 * Renders semi-transparent cyan-blue gradients, animated scanline raster effects,
 * holographic grid lines, tactical corner reticles, and interactive liquid glass controls.
 */
@Composable
fun LiquidGlassHudOverlay(
    modifier: Modifier = Modifier,
    enableScanlines: Boolean = false,
    enableHoloGrid: Boolean = false,
    enableLiquidGlassSheen: Boolean = true,
    scanlineAlpha: Float = 0.15f,
    content: @Composable () -> Unit
) {
    // Dynamic HUD Customization States
    var showScanlines by remember { mutableStateOf(enableScanlines) }
    var showHoloGrid by remember { mutableStateOf(enableHoloGrid) }
    var showLiquidSheen by remember { mutableStateOf(enableLiquidGlassSheen) }
    var currentScanlineAlpha by remember { mutableFloatStateOf(scanlineAlpha) }
    var liquidAlpha by remember { mutableFloatStateOf(0.32f) }
    var isControlPanelExpanded by remember { mutableStateOf(false) }

    // Animations for Scanlines and Holographic Glare
    val infiniteTransition = rememberInfiniteTransition(label = "HudOverlayAnimations")

    // Moving Scanbeam Sweep (Top to Bottom)
    val scanbeamY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanbeam_y"
    )

    // Holographic Pulse Flicker
    val holoPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holo_pulse"
    )

    // Refractive 'Liquid Glass' Breathing Animation via animateFloatAsState
    var hudActiveBreathingToggle by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2600L)
            hudActiveBreathingToggle = !hudActiveBreathingToggle
        }
    }

    val hudRadialBreathingAlpha by animateFloatAsState(
        targetValue = if (hudActiveBreathingToggle) 0.40f else 0.18f,
        animationSpec = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
        label = "hud_active_radial_gradient_breathing"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Underlay Base Content (Main HUD Screen)
        content()

        // 2. Holographic Semi-Transparent Cyan-Blue Ambient Vignette & Gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Semi-transparent cyan-blue perimeter radial ambient light
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        JarvisCyanDark.copy(alpha = 0.25f * liquidAlpha * hudRadialBreathingAlpha),
                        JarvisBlueCore.copy(alpha = 0.38f * liquidAlpha * hudRadialBreathingAlpha),
                        JarvisVoid.copy(alpha = 0.45f)
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = kotlin.math.max(w, h) * 0.75f
                )
            )

            // Top-Left and Bottom-Right Cyan Light Flares
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyanBright.copy(alpha = 0.35f * holoPulse * hudRadialBreathingAlpha),
                        JarvisCyan.copy(alpha = 0.15f * hudRadialBreathingAlpha),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = w * 0.45f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisBlueCore.copy(alpha = 0.30f * holoPulse * hudRadialBreathingAlpha),
                        JarvisCyanDark.copy(alpha = 0.12f * hudRadialBreathingAlpha),
                        Color.Transparent
                    ),
                    center = Offset(w, h),
                    radius = w * 0.5f
                )
            )

            // 3. Holographic Coordinate Mesh / Grid Lines
            if (showHoloGrid && w > 0 && h > 0) {
                val gridSpacing = 48.dp.toPx()
                val gridColor = JarvisCyan.copy(alpha = 0.07f * holoPulse)

                var x = 0f
                while (x < w) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                    )
                    x += gridSpacing
                }

                var y = 0f
                while (y < h) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                    )
                    y += gridSpacing
                }
            }

            // 4. Scanline Raster & Downward Moving Holographic Scanbeam
            if (showScanlines && w > 0 && h > 0) {
                val lineStep = 5.dp.toPx()
                val scanlineColor = JarvisCyan.copy(alpha = 0.09f * currentScanlineAlpha)

                var lineY = 0f
                while (lineY < h) {
                    drawLine(
                        color = scanlineColor,
                        start = Offset(0f, lineY),
                        end = Offset(w, lineY),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    lineY += lineStep
                }

                // Downward Glowing Holographic Scanbeam Pass
                val beamY = h * scanbeamY
                val beamHeight = 60.dp.toPx()

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            JarvisCyan.copy(alpha = 0.06f * currentScanlineAlpha),
                            JarvisCyanBright.copy(alpha = 0.24f * currentScanlineAlpha),
                            JarvisBlueCore.copy(alpha = 0.08f * currentScanlineAlpha),
                            Color.Transparent
                        ),
                        startY = beamY - beamHeight,
                        endY = beamY + beamHeight
                    )
                )

                drawLine(
                    color = JarvisCyanBright.copy(alpha = 0.6f * currentScanlineAlpha),
                    start = Offset(0f, beamY),
                    end = Offset(w, beamY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // 5. Tactical Corner Holographic Reticle Brackets with Liquid Cyan Refraction
            val bracketSize = 28.dp.toPx()
            val bracketThickness = 2.dp.toPx()
            val bracketColor = JarvisCyanBright.copy(alpha = 0.85f * holoPulse)

            // Top-Left
            drawLine(bracketColor, Offset(12.dp.toPx(), 12.dp.toPx()), Offset(12.dp.toPx() + bracketSize, 12.dp.toPx()), bracketThickness)
            drawLine(bracketColor, Offset(12.dp.toPx(), 12.dp.toPx()), Offset(12.dp.toPx(), 12.dp.toPx() + bracketSize), bracketThickness)

            // Top-Right
            drawLine(bracketColor, Offset(w - 12.dp.toPx(), 12.dp.toPx()), Offset(w - 12.dp.toPx() - bracketSize, 12.dp.toPx()), bracketThickness)
            drawLine(bracketColor, Offset(w - 12.dp.toPx(), 12.dp.toPx()), Offset(w - 12.dp.toPx(), 12.dp.toPx() + bracketSize), bracketThickness)

            // Bottom-Left
            drawLine(bracketColor, Offset(12.dp.toPx(), h - 12.dp.toPx()), Offset(12.dp.toPx() + bracketSize, h - 12.dp.toPx()), bracketThickness)
            drawLine(bracketColor, Offset(12.dp.toPx(), h - 12.dp.toPx()), Offset(12.dp.toPx(), h - 12.dp.toPx() - bracketSize), bracketThickness)

            // Bottom-Right
            drawLine(bracketColor, Offset(w - 12.dp.toPx(), h - 12.dp.toPx()), Offset(w - 12.dp.toPx() - bracketSize, h - 12.dp.toPx()), bracketThickness)
            drawLine(bracketColor, Offset(w - 12.dp.toPx(), h - 12.dp.toPx()), Offset(w - 12.dp.toPx(), h - 12.dp.toPx() - bracketSize), bracketThickness)
        }

        // 6. Top Holographic Telemetry Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            JarvisGlassPanel.copy(alpha = 0.8f),
                            JarvisCyanDark.copy(alpha = 0.3f),
                            JarvisGlassPanel.copy(alpha = 0.8f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            JarvisCyanBright,
                            JarvisBlueCore.copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JarvisCyanBright)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIQUID HUD • HOLOGRAM v2.4",
                        style = Typography.labelSmall,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyanBright,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCANLINES: ${if (showScanlines) "ON" else "OFF"}",
                        style = Typography.labelSmall,
                        fontSize = 8.5.sp,
                        color = JarvisTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Floating Liquid Glass HUD Settings Toggle Button
                    Box(
                        modifier = Modifier
                            .testTag("hud_overlay_control_toggle")
                            .clip(CircleShape)
                            .background(JarvisCyan.copy(alpha = 0.25f))
                            .border(1.dp, JarvisCyanBright, CircleShape)
                            .clickable { isControlPanelExpanded = !isControlPanelExpanded }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isControlPanelExpanded) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = "HUD Overlay Controls",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 7. Interactive Floating Liquid Glass HUD Control Panel (Expandable Drawer/Sheet)
        AnimatedVisibility(
            visible = isControlPanelExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            LiquidGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = "HOLOGRAPHIC LIQUID GLASS HUD CONTROLS",
                statusTag = "REAL-TIME MATRIX",
                accentColor = JarvisCyanBright,
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Scanner,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scanline Raster",
                                style = Typography.bodyMedium,
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                        }
                        LiquidGlassToggle(
                            checked = showScanlines,
                            onCheckedChange = { showScanlines = it },
                            testTag = "toggle_hud_scanlines"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Grid4x4,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Holographic Grid",
                                style = Typography.bodyMedium,
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                        }
                        LiquidGlassToggle(
                            checked = showHoloGrid,
                            onCheckedChange = { showHoloGrid = it },
                            testTag = "toggle_hud_grid"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Liquid Specular Sheen",
                                style = Typography.bodyMedium,
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                        }
                        LiquidGlassToggle(
                            checked = showLiquidSheen,
                            onCheckedChange = { showLiquidSheen = it },
                            testTag = "toggle_liquid_sheen"
                        )
                    }

                    // Row 2: Sliders
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Scanline Intensity",
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "${(currentScanlineAlpha * 100).toInt()}%",
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = currentScanlineAlpha,
                            onValueChange = { currentScanlineAlpha = it },
                            valueRange = 0.05f..0.80f,
                            testTag = "slider_scanline_intensity"
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Liquid Cyan Refraction Density",
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "${(liquidAlpha * 100).toInt()}%",
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = liquidAlpha,
                            onValueChange = { liquidAlpha = it },
                            valueRange = 0.10f..0.85f,
                            testTag = "slider_liquid_density"
                        )
                    }

                    // Row 3: Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassButton(
                            onClick = {
                                showScanlines = true
                                showHoloGrid = true
                                showLiquidSheen = true
                                currentScanlineAlpha = 0.30f
                                liquidAlpha = 0.35f
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_reset_hud_overlay"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = JarvisCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RECALIBRATE HUD",
                                    style = Typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = JarvisCyanBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LiquidGlassButton(
                            onClick = { isControlPanelExpanded = false },
                            modifier = Modifier.weight(1f),
                            accentColor = JarvisBlueCore,
                            testTag = "btn_close_hud_overlay_controls"
                        ) {
                            Text(
                                text = "DISMISS",
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
