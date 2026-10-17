package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voice.VoiceState
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual State specification for the A.L.I.S.E. Blue-Glass Hollow 3D Sphere.
 * Directly corresponds to the Master Spec engineering passes.
 */
enum class SphereVisualState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    SUCCESS,
    INTERRUPTED,
    ERROR
}

fun VoiceState.toSphereVisualState(): SphereVisualState = when (this) {
    is VoiceState.Idle -> SphereVisualState.IDLE
    is VoiceState.ListeningWakeWord, is VoiceState.ListeningCommand -> SphereVisualState.LISTENING
    is VoiceState.Processing -> SphereVisualState.THINKING
    is VoiceState.Speaking -> SphereVisualState.SPEAKING
    is VoiceState.Executing -> SphereVisualState.EXECUTING
    is VoiceState.Success -> SphereVisualState.SUCCESS
    is VoiceState.Interrupted -> SphereVisualState.INTERRUPTED
    is VoiceState.Error -> SphereVisualState.ERROR
}

/**
 * Precision Symmetrical Blue-Glass / Hollow 3D Sphere.
 *
 * Implements:
 * - Controlled glass transparency & optical Fresnel rim lighting
 * - Balanced 3D wireframe spherical geometry with depth alpha sorting
 * - Continuous, seamless 360° rotation loop
 * - Harmonic voice-reactive surface wave integration that preserves spherical symmetry
 * - Coherent visual states: IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, SUCCESS, INTERRUPTED, ERROR
 * - Real hardware frame-rate telemetry (120 Hz Samsung Galaxy S-series display optimization)
 */
@Composable
fun JarvisGlobeCore(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    amplitude: Float = 0f,
    voiceState: VoiceState = VoiceState.Idle,
    isActive: Boolean = true,
    isAlert: Boolean = false,
    showTelemetry: Boolean = true,
    onClick: () -> Unit = {}
) {
    val visualState = if (isAlert) SphereVisualState.ERROR else voiceState.toSphereVisualState()

    // Real Hardware Frame Rate Tracker (measures actual withFrameNanos deltas)
    var realMeasuredFps by remember { mutableFloatStateOf(60f) }
    var realFrameTimeMs by remember { mutableFloatStateOf(16.6f) }
    var lastFrameNanos by remember { mutableLongStateOf(0L) }
    var frameCount by remember { mutableLongStateOf(0L) }
    var accumulatedNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                if (lastFrameNanos != 0L) {
                    val delta = now - lastFrameNanos
                    accumulatedNanos += delta
                    frameCount++

                    // Update FPS readout every ~250ms for stable readability
                    if (accumulatedNanos >= 250_000_000L) {
                        val avgDeltaMs = (accumulatedNanos.toDouble() / frameCount) / 1_000_000.0
                        realFrameTimeMs = avgDeltaMs.toFloat()
                        realMeasuredFps = (1000.0 / avgDeltaMs.coerceAtLeast(1.0)).coerceIn(10.0, 144.0).toFloat()
                        accumulatedNanos = 0L
                        frameCount = 0L
                    }
                }
                lastFrameNanos = now
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AliseSphereLoop")

    // Continuous 360° Y-axis rotation (speed modulates slightly during thinking)
    val rotationDuration = when (visualState) {
        SphereVisualState.THINKING -> 5000
        SphereVisualState.EXECUTING -> 6500
        SphereVisualState.SPEAKING -> 9000
        else -> 12000
    }

    val rotationYAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "globeRotationY"
    )

    // Subtle natural tilt oscillation (-10° to +10°)
    val tiltXAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globeTiltX"
    )

    // Breathing pulse scale
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globeBreathing"
    )

    // Surface wave phase for voice reactivity
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sphereWavePhase"
    )

    // Dynamic State Colors
    val targetPrimaryColor = when (visualState) {
        SphereVisualState.IDLE -> Color(0xFF00E5FF)       // Liquid Cyan Glass
        SphereVisualState.LISTENING -> Color(0xFF38BDF8)  // Deep Azure Focus
        SphereVisualState.THINKING -> Color(0xFF818CF8)   // Indigo Cognitive Resonance
        SphereVisualState.SPEAKING -> Color(0xFF00F5FF)   // Electric Bioluminescent Cyan
        SphereVisualState.EXECUTING -> Color(0xFF3B82F6)  // Sapphire Tactical
        SphereVisualState.SUCCESS -> Color(0xFF10B981)    // Emerald Confirmation
        SphereVisualState.INTERRUPTED -> Color(0xFFF59E0B)// Amber Barge-in
        SphereVisualState.ERROR -> Color(0xFFEF4444)      // Crimson Alert
    }

    val primaryColor by animateColorAsState(targetValue = targetPrimaryColor, animationSpec = tween(350), label = "primaryColor")
    val rimColor by animateColorAsState(
        targetValue = when (visualState) {
            SphereVisualState.ERROR -> Color(0xFFFCA5A5)
            SphereVisualState.SUCCESS -> Color(0xFF6EE7B7)
            SphereVisualState.THINKING -> Color(0xFFC7D2FE)
            else -> Color(0xFFBAE6FD)
        },
        animationSpec = tween(350),
        label = "rimColor"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("alise_hollow_glass_sphere")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = (this.size.width / 2f) * 0.84f
            val baseRadius = outerRadius * breathingScale
            val clampedAmp = amplitude.coerceIn(0f, 1f)

            // 1. Ambient Glass Core Glow (Fresnel scattering inside hollow shell)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f + (clampedAmp * 0.35f)),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                center = center,
                radius = baseRadius * 1.35f
            )

            // 2. Liquid Glass Outer Boundary & Fresnel Optical Rim
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.Transparent,
                    0.72f to primaryColor.copy(alpha = 0.05f),
                    0.92f to primaryColor.copy(alpha = 0.35f + clampedAmp * 0.25f),
                    1.0f to rimColor.copy(alpha = 0.75f + clampedAmp * 0.25f),
                    center = center,
                    radius = baseRadius
                ),
                center = center,
                radius = baseRadius
            )

            // Precision Outer Glass Silhouette Ring (Controlled crisp boundary)
            drawCircle(
                color = rimColor.copy(alpha = 0.6f + (clampedAmp * 0.3f)),
                center = center,
                radius = baseRadius,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 3. Symmetrical 3D Hollow Wireframe Sphere
            val latBands = 14
            val lonBands = 22

            val radX = Math.toRadians(tiltXAngle.toDouble())
            val radY = Math.toRadians(rotationYAngle.toDouble())

            val cosX = cos(radX).toFloat()
            val sinX = sin(radX).toFloat()
            val cosY = cos(radY).toFloat()
            val sinY = sin(radY).toFloat()

            // Draw latitude contours and luminous nodes
            for (i in 0..latBands) {
                val latNorm = (i.toDouble() / latBands) - 0.5 // -0.5 to +0.5
                val lat = Math.PI * latNorm
                val sinLat = sin(lat).toFloat()
                val cosLat = cos(lat).toFloat()

                var lastPoint: Offset? = null

                for (j in 0..lonBands) {
                    val lon = 2.0 * Math.PI * (j.toDouble() / lonBands)
                    val sinLon = sin(lon).toFloat()
                    val cosLon = cos(lon).toFloat()

                    // Voice-reactive surface wave: harmonic rippling preserving spherical silhouette
                    val surfaceRipple = if (clampedAmp > 0.05f) {
                        (sin(lat * 5.0 + wavePhase) * cos(lon * 3.0 + wavePhase) * clampedAmp * 0.06).toFloat()
                    } else 0f

                    val r = baseRadius * (1f + surfaceRipple)

                    // 3D coordinates on unit sphere
                    val x0 = cosLat * cosLon
                    val y0 = sinLat
                    val z0 = cosLat * sinLon

                    // Rotate around Y axis (continuous 360°)
                    val x1 = x0 * cosY + z0 * sinY
                    val y1 = y0
                    val z1 = -x0 * sinY + z0 * cosY

                    // Rotate around X axis (subtle tilt)
                    val x2 = x1
                    val y2 = y1 * cosX - z1 * sinX
                    val z2 = y1 * sinX + z1 * cosX // Depth Z: -1 (back) to +1 (front)

                    // 2D Orthographic perspective projection
                    val projectedX = center.x + x2 * r
                    val projectedY = center.y + y2 * r
                    val currentPoint = Offset(projectedX, projectedY)

                    // Controlled Depth-Sorted Transparency (Back shell is faint, front shell is luminous)
                    val depthAlpha = ((z2 + 1f) / 2f).coerceIn(0.10f, 1f)
                    val dotRadius = (1.4.dp.toPx() + (z2 + 1f) * 1.1.dp.toPx()) * (1f + clampedAmp * 0.25f)

                    // Draw subtle connecting latitude line
                    if (lastPoint != null && z2 > -0.4f) {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.18f * depthAlpha),
                            start = lastPoint,
                            end = currentPoint,
                            strokeWidth = 0.9.dp.toPx()
                        )
                    }
                    lastPoint = currentPoint

                    // Draw luminous node on glass surface
                    val nodeAlpha = if (z2 > 0.35f) {
                        depthAlpha * (0.75f + clampedAmp * 0.25f)
                    } else {
                        depthAlpha * 0.30f
                    }

                    drawCircle(
                        color = if (z2 > 0.5f) rimColor.copy(alpha = nodeAlpha) else primaryColor.copy(alpha = nodeAlpha),
                        center = currentPoint,
                        radius = dotRadius
                    )
                }
            }

            // 4. Inner Luminous Nucleus (Simulates refraction at hollow center)
            val nucleusRadius = baseRadius * 0.16f * (1f + clampedAmp * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor.copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = nucleusRadius * 1.8f
                ),
                center = center,
                radius = nucleusRadius * 1.8f
            )
            drawCircle(
                color = Color.White,
                center = center,
                radius = nucleusRadius * 0.45f
            )
        }

        // 5. Hardware Refresh-Rate & Performance Telemetry Badge (120 Hz verification)
        if (showTelemetry) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0x99030712)
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", realMeasuredFps)} FPS | ${String.format(Locale.US, "%.1f", realFrameTimeMs)}ms",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (realMeasuredFps >= 100f) Color(0xFF34D399) else primaryColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
