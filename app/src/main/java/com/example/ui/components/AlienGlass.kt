package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienGlassBorder
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Living Cosmic Background Canvas with drifting bioluminescent light pods and subtle alien nebulae.
 * Fits post-iOS 27 & Android 17 spatial design language.
 */
@Composable
fun AlienNebulaBackgroundCanvas(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NebulaDriftTransition")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Deep Abyssal Void Base
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        Color(0xFF070A18),
                        Color(0xFF05030E)
                    )
                )
            )

            if (w > 0 && h > 0) {
                // 2. Drifting Bioluminescent Magenta Nebula Pod (Top Left)
                val pod1X = w * (0.3f + 0.2f * sin(phase1.toDouble()).toFloat())
                val pod1Y = h * (0.25f + 0.15f * cos(phase1.toDouble() * 0.8).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AlienBioluminescentMagenta.copy(alpha = 0.22f),
                            Color(0x228B5CF6),
                            Color.Transparent
                        ),
                        center = Offset(pod1X, pod1Y),
                        radius = w * 0.85f
                    )
                )

                // 3. Drifting Bioluminescent Cyan/Emerald Empathy Pod (Bottom Right)
                val pod2X = w * (0.7f + 0.25f * cos(phase2.toDouble()).toFloat())
                val pod2Y = h * (0.75f + 0.15f * sin(phase2.toDouble() * 0.9).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AlienEmpathyCyan.copy(alpha = 0.20f),
                            AlienCelestialEmerald.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(pod2X, pod2Y),
                        radius = w * 0.90f
                    )
                )

                // 4. Celestial Starlight Particles Grid (Subtle cosmic dust)
                val dustCount = 18
                for (i in 0 until dustCount) {
                    val angle = (phase1 + i * (6.28f / dustCount))
                    val px = (w * 0.1f + (i * 57) % w + 20f * sin(angle.toDouble())).toFloat()
                    val py = (h * 0.05f + (i * 113) % h + 20f * cos(angle.toDouble())).toFloat()
                    val pAlpha = 0.2f + 0.35f * sin((phase2 + i).toDouble()).toFloat().coerceIn(0f, 1f)
                    
                    drawCircle(
                        color = Color.White.copy(alpha = pAlpha),
                        radius = (1.5f + (i % 3) * 0.8f).dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }

        content()
    }
}

/**
 * Translucent Alien Symbiont Surface with bioluminescent prismatic borders and specular glass refraction.
 */
fun Modifier.alienSymbiontSurface(
    cornerRadius: Dp = 20.dp,
    glassColor: Color = Color(0x2E0B1226),
    borderColor: Color = AlienGlassBorder,
    borderWidth: Dp = 1.2.dp,
    accentGlow: Color = AlienEmpathyCyan
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.linearGradient(
            colors = listOf(
                glassColor,
                Color(0x1F1A0B2E),
                Color(0x28071828)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                accentGlow.copy(alpha = 0.7f),
                AlienBioluminescentMagenta.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.3f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .drawWithContent {
        drawContent()

        val width = size.width
        val height = size.height
        if (width > 0 && height > 0) {
            // Specular Rim Refraction
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(1f, 1f, width - 1f, height - 1f),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                )
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.40f),
                        accentGlow.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height * 0.3f
                ),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }

/**
 * Alien Bioluminescent Neural Orb. Represents the benevolent alien-human AI core.
 */
@Composable
fun AlienBioluminescentOrb(
    modifier: Modifier = Modifier,
    amplitude: Float = 0.2f,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    isConnected: Boolean = true,
    statusText: String = "HUMANITY-ALISE SYMBIONT ACTIVE",
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AlienOrbPulse")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    val coreScale by animateFloatAsState(
        targetValue = 1.0f + amplitude * 0.35f + (if (isSpeaking) 0.12f else 0f),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "core_scale"
    )

    val mainColor = when {
        !isConnected -> Color(0xFFFF5252)
        isSpeaking -> AlienBioluminescentMagenta
        isListening -> AlienHarmonyGold
        else -> AlienEmpathyCyan
    }

    val secondaryColor = when {
        !isConnected -> Color(0xFFD32F2F)
        isSpeaking -> AlienEmpathyCyan
        isListening -> AlienCelestialEmerald
        else -> AlienBioluminescentMagenta
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.30f * coreScale

            // 1. Outer Bioluminescent Halo Waves
            val haloCount = 3
            for (i in 0 until haloCount) {
                val waveAlpha = (0.25f - i * 0.07f) * (0.8f + 0.2f * sin(pulsePhase.toDouble() + i * 1.2).toFloat())
                val haloRadius = baseRadius * (1.2f + i * 0.32f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            mainColor.copy(alpha = waveAlpha.coerceIn(0f, 1f)),
                            secondaryColor.copy(alpha = (waveAlpha * 0.5f).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = center,
                        radius = haloRadius
                    )
                )
            }

            // 2. Rotating Symbiont Particle Rings
            val ringCount = 16
            val ringRadius = baseRadius * 1.35f
            for (i in 0 until ringCount) {
                val angle = pulsePhase + i * (6.28318f / ringCount)
                val rx = center.x + ringRadius * cos(angle.toDouble()).toFloat()
                val ry = center.y + ringRadius * sin(angle.toDouble()).toFloat() * 0.45f
                val dotAlpha = 0.4f + 0.6f * sin((pulsePhase * 2f + i).toDouble()).toFloat().coerceIn(0f, 1f)

                drawCircle(
                    color = secondaryColor.copy(alpha = dotAlpha),
                    radius = 2.5.dp.toPx(),
                    center = Offset(rx, ry)
                )
            }

            // 3. Dense Inner Glowing Core Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        mainColor,
                        secondaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                center = center,
                radius = baseRadius
            )

            // 4. Prismatic Specular Ring Highlight
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        mainColor.copy(alpha = 0.6f),
                        secondaryColor.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.9f)
                    ),
                    center = center
                ),
                center = center,
                radius = baseRadius,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Alien Symbiont Status Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (isSpeaking) "SPEAKING" else if (isListening) "LISTENING" else if (isConnected) "ONLINE" else "DISCONNECTED",
                style = com.example.ui.theme.Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
            Text(
                text = statusText,
                style = com.example.ui.theme.Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = mainColor,
                fontSize = 8.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Spatial Floating Navigation Dock Bar for Post-iOS 27 & Android 17 layout architecture.
 */
@Composable
fun SpatialAlienFloatingDock(
    destinations: Array<com.example.JarvisDestination>,
    currentDestination: com.example.JarvisDestination,
    onDestinationSelected: (com.example.JarvisDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .alienSymbiontSurface(
                cornerRadius = 28.dp,
                glassColor = Color(0xF2070D1E),
                borderColor = Color(0x6600F5FF),
                borderWidth = 1.2.dp,
                accentGlow = AlienEmpathyCyan
            )
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                val selected = currentDestination == dest

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        AlienEmpathyCyan.copy(alpha = 0.35f),
                                        AlienBioluminescentMagenta.copy(alpha = 0.40f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x22101F38),
                                        Color(0x11081028)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.White.copy(alpha = 0.8f) else Color(0x2200F5FF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onDestinationSelected(dest) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag(dest.tag)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) Color.White else JarvisTextMuted
                        )
                        Text(
                            text = dest.title,
                            style = com.example.ui.theme.Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else JarvisTextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extraterrestrial Empathic Badge Component.
 */
@Composable
fun AlienHumanBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AlienEmpathyCyan
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.60f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text.uppercase(),
                style = com.example.ui.theme.Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
