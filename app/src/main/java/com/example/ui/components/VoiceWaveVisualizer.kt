package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.SkyBright
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-fidelity animated voice waveform visualizer with multi-pass neon aura,
 * frequency spectrum bars, harmonic sub-waves, and dynamic energy sparks.
 */
@Composable
fun VoiceWaveVisualizer(
    amplitude: Float, // 0.0 .. 1.0
    isActive: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    primaryColor: Color = SkyBright,
    secondaryColor: Color = JarvisArcGold
) {
    val smoothAmp by animateFloatAsState(
        targetValue = if (isActive) amplitude.coerceIn(0.12f, 1.0f) else 0.04f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SmoothWaveformAmp"
    )

    val transition = rememberInfiniteTransition(label = "WavePhaseTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 1000 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val glowPulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val path = remember { Path() }
    val pathHarmonic = remember { Path() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val ampMultiplier = smoothAmp * canvasHeight * 0.44f

        // 1. Subtle Holographic Center Baseline
        drawLine(
            color = Color(0x2238BDF8),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // 2. High-Fidelity Spectral Equalizer Frequency Bars
        val barCount = 36
        val barWidth = width / barCount
        for (i in 0 until barCount) {
            val normI = i.toFloat() / (barCount - 1)
            val envelope = sin(normI * PI).toFloat()
            val barAmp = (sin(normI * 6.0 + phase) * 0.4 + sin(normI * 12.0 - phase * 1.5) * 0.3 + 0.6) *
                    ampMultiplier * envelope
            val barX = i * barWidth + (barWidth / 2f)
            val barH = barAmp.toFloat().coerceAtLeast(1.5f)

            val barAlpha = if (isActive) (0.35f + smoothAmp * 0.5f) else 0.15f
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = barAlpha * glowPulse),
                        Color(0xFF0284C7).copy(alpha = barAlpha * 0.6f)
                    ),
                    startY = centerY - barH,
                    endY = centerY + barH
                ),
                start = Offset(barX, centerY - barH),
                end = Offset(barX, centerY + barH),
                strokeWidth = barWidth * 0.5f,
                cap = StrokeCap.Round
            )
        }

        // 3. Multi-harmonic Holographic Sine Waves
        path.rewind()
        pathHarmonic.rewind()

        val step = 3
        val maxSteps = width.toInt()
        for (x in 0..maxSteps step step) {
            val progress = (x / width)
            val envelope = sin(progress * PI).toFloat()

            val y = centerY + (sin(progress * 4.0 * PI + phase) * ampMultiplier * envelope +
                    sin(progress * 8.0 * PI + phase * 1.6) * (ampMultiplier * 0.25f * envelope)).toFloat()

            val yHarmonic = centerY + (sin((progress * 3.0 * PI) - phase * 1.2) * (ampMultiplier * 0.6f) * envelope).toFloat()

            if (x == 0) {
                path.moveTo(x.toFloat(), y)
                pathHarmonic.moveTo(x.toFloat(), yHarmonic)
            } else {
                path.lineTo(x.toFloat(), y)
                pathHarmonic.lineTo(x.toFloat(), yHarmonic)
            }
        }

        // Pass 1: Secondary Harmonic Wave (Golden Glow)
        drawPath(
            path = pathHarmonic,
            color = secondaryColor.copy(alpha = 0.5f * glowPulse),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pass 2: Primary Wave Atmospheric Glow Bloom
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primaryColor.copy(alpha = 0.45f * glowPulse),
                    Color.White.copy(alpha = 0.7f * glowPulse),
                    primaryColor.copy(alpha = 0.45f * glowPulse),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pass 3: Primary Wave Focused Core Beam
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primaryColor,
                    Color.White,
                    primaryColor,
                    Color.Transparent
                )
            ),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pass 4: Dynamic Energy Particle Sparks
        if (isActive) {
            val sparkCount = 4
            for (i in 0 until sparkCount) {
                val sparkProgress = ((phase / (2 * PI) + (i.toFloat() / sparkCount)) % 1.0f).toFloat()
                val px = sparkProgress * width
                val envelope = sin(sparkProgress * PI).toFloat()
                val py = centerY + (sin(sparkProgress * 4.0 * PI + phase) * ampMultiplier * envelope).toFloat()

                drawCircle(
                    color = primaryColor.copy(alpha = 0.6f * glowPulse),
                    radius = (3f + smoothAmp * 3f).dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}
