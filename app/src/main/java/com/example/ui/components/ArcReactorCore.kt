package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisPurplePulse
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    amplitude: Float = 0f, // 0.0 .. 1.0 audio reactivity
    isActive: Boolean = true,
    isAlert: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Continuous outer ring rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingRotation"
    )

    // Inner counter-rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRingRotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    val primaryColor = when {
        isAlert -> JarvisAlertRed
        amplitude > 0.4f -> JarvisCyanBright
        else -> JarvisCyan
    }

    val secondaryColor = if (isAlert) JarvisArcGold else JarvisBlueCore

    val trianglePath = remember { Path() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_core")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.88f
            val dynamicScale = (breathingPulse + (amplitude * 0.25f)).coerceIn(0.85f, 1.4f)
            val currentRadius = baseRadius * dynamicScale

            // 1. Ambient Background Glow Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f + (amplitude * 0.4f)),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.15f
                ),
                center = center,
                radius = currentRadius * 1.15f
            )

            // 1b. Liquid Glass Lens Specular Glare Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        JarvisCyanBright.copy(alpha = 0.6f),
                        JarvisBlueCore.copy(alpha = 0.2f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.5f),
                        JarvisCyan.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.85f)
                    ),
                    center = center
                ),
                radius = currentRadius * 1.05f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Outer Static Grid Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = currentRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Rotating Segmented Outer Ring
            rotate(degrees = outerRotation, pivot = center) {
                val segments = 12
                val arcAngle = 360f / segments
                val arcSize = Size(currentRadius * 2, currentRadius * 2)
                val arcTopLeft = Offset(center.x - currentRadius, center.y - currentRadius)
                val tickWidth = 1.5.dp.toPx()
                val strokeWidth = 3.dp.toPx()

                for (i in 0 until segments) {
                    val startAngle = i * arcAngle
                    drawArc(
                        color = if (i % 3 == 0) JarvisArcGold else primaryColor,
                        startAngle = startAngle + 4f,
                        sweepAngle = arcAngle - 8f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Outer tick markers
                    val rad = Math.toRadians((startAngle + 4f).toDouble())
                    val cosVal = cos(rad).toFloat()
                    val sinVal = sin(rad).toFloat()
                    val tickOuter = Offset(
                        center.x + (currentRadius + 8.dp.toPx()) * cosVal,
                        center.y + (currentRadius + 8.dp.toPx()) * sinVal
                    )
                    val tickInner = Offset(
                        center.x + (currentRadius + 2.dp.toPx()) * cosVal,
                        center.y + (currentRadius + 2.dp.toPx()) * sinVal
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.7f),
                        start = tickInner,
                        end = tickOuter,
                        strokeWidth = tickWidth
                    )
                }
            }

            // 4. Middle Rotating Gear Blades
            val midRadius = currentRadius * 0.72f
            val midSize = Size(midRadius * 2, midRadius * 2)
            val midTopLeft = Offset(center.x - midRadius, center.y - midRadius)
            val gearStrokeWidth = 4.dp.toPx()

            rotate(degrees = innerRotation, pivot = center) {
                val bladeCount = 10
                val bladeAngle = 360f / bladeCount
                for (i in 0 until bladeCount) {
                    val angle = i * bladeAngle
                    drawArc(
                        color = secondaryColor.copy(alpha = 0.85f),
                        startAngle = angle + 6f,
                        sweepAngle = bladeAngle - 12f,
                        useCenter = false,
                        topLeft = midTopLeft,
                        size = midSize,
                        style = Stroke(width = gearStrokeWidth, cap = StrokeCap.Butt)
                    )
                }
            }

            // 5. Inner Core Rings
            val innerCoreRadius = currentRadius * 0.46f
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = innerCoreRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 6. Central Holographic Triangle / Arc Nodes (reusing path)
            val triangleRadius = innerCoreRadius * 0.78f
            rotate(degrees = outerRotation * 0.5f, pivot = center) {
                trianglePath.rewind()
                for (i in 0..2) {
                    val angleRad = Math.toRadians((i * 120.0) - 90.0)
                    val x = center.x + triangleRadius * cos(angleRad).toFloat()
                    val y = center.y + triangleRadius * sin(angleRad).toFloat()
                    if (i == 0) trianglePath.moveTo(x, y) else trianglePath.lineTo(x, y)
                }
                trianglePath.close()

                // Triangle fill
                drawPath(
                    path = trianglePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.4f),
                            secondaryColor.copy(alpha = 0.1f)
                        ),
                        center = center,
                        radius = triangleRadius
                    )
                )

                // Triangle border
                drawPath(
                    path = trianglePath,
                    color = primaryColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 7. Center Nucleus Core (Bright White / Cyan)
            val nucleusRadius = 9.dp.toPx() * (1f + amplitude * 0.5f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = nucleusRadius * 1.6f
                ),
                center = center,
                radius = nucleusRadius * 1.6f
            )
            drawCircle(
                color = Color.White,
                center = center,
                radius = nucleusRadius * 0.65f
            )
        }
    }
}
