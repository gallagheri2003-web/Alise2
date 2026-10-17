package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan

@Composable
fun TacticalReticleOverlay(
    modifier: Modifier = Modifier,
    isTargetLocked: Boolean = false,
    scanProgress: Float = 0f,
    primaryColor: Color = JarvisCyan
) {
    val transition = rememberInfiniteTransition(label = "ReticleTransition")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_rotation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val reticleRadius = (size.minDimension / 2f) * 0.45f
        val color = if (isTargetLocked) JarvisAlertRed else primaryColor

        // 1. Center Crosshairs
        val crosshairLen = 24.dp.toPx()
        val gap = 12.dp.toPx()
        drawLine(color, Offset(center.x - crosshairLen, center.y), Offset(center.x - gap, center.y), 2.dp.toPx())
        drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + crosshairLen, center.y), 2.dp.toPx())
        drawLine(color, Offset(center.x, center.y - crosshairLen), Offset(center.x, center.y - gap), 2.dp.toPx())
        drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + crosshairLen), 2.dp.toPx())

        // 2. Rotating Segmented Tracking Circle
        rotate(degrees = rotation, pivot = center) {
            val segments = 4
            val sweep = 65f
            for (i in 0 until segments) {
                val start = i * 90f + 12.5f
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - reticleRadius, center.y - reticleRadius),
                    size = Size(reticleRadius * 2, reticleRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 3. Outer Corner Bounding Target Box
        val boxSize = reticleRadius * 1.5f
        val cornerLen = 28.dp.toPx()
        val left = center.x - boxSize
        val top = center.y - boxSize
        val right = center.x + boxSize
        val bottom = center.y + boxSize

        // Top-Left
        drawLine(JarvisArcGold, Offset(left, top), Offset(left + cornerLen, top), 2.5.dp.toPx())
        drawLine(JarvisArcGold, Offset(left, top), Offset(left, top + cornerLen), 2.5.dp.toPx())

        // Top-Right
        drawLine(JarvisArcGold, Offset(right, top), Offset(right - cornerLen, top), 2.5.dp.toPx())
        drawLine(JarvisArcGold, Offset(right, top), Offset(right, top + cornerLen), 2.5.dp.toPx())

        // Bottom-Left
        drawLine(JarvisArcGold, Offset(left, bottom), Offset(left + cornerLen, bottom), 2.5.dp.toPx())
        drawLine(JarvisArcGold, Offset(left, bottom), Offset(left, bottom - cornerLen), 2.5.dp.toPx())

        // Bottom-Right
        drawLine(JarvisArcGold, Offset(right, bottom), Offset(right - cornerLen, bottom), 2.5.dp.toPx())
        drawLine(JarvisArcGold, Offset(right, bottom), Offset(right, bottom - cornerLen), 2.5.dp.toPx())

        // 4. Scanline Sweep
        if (scanProgress > 0f) {
            val scanY = top + (boxSize * 2 * scanProgress)
            drawLine(
                color = JarvisCyan.copy(alpha = 0.8f),
                start = Offset(left, scanY),
                end = Offset(right, scanY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
