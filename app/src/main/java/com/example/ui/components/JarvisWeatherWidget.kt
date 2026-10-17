package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import kotlin.math.cos
import kotlin.math.sin

/**
 * Weather & World Orb Widget matching Image 2 top-right widget layout.
 */
@Composable
fun JarvisWeatherWidget(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherOrbTransitions")
    val orbRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherOrbRotate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisSurfaceDark.copy(alpha = 0.9f),
                        Color(0xFF08121C).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(JarvisArcGold.copy(alpha = 0.4f), JarvisCyan.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEW YORK 📍",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "72°",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SUNNY ",
                        color = JarvisArcGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sunny",
                        tint = JarvisArcGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "H: 74°  L: 58°",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Glowing Sun Wireframe Orb Canvas
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(68.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f * 0.75f

                    // Glow background
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                JarvisArcGold.copy(alpha = 0.5f),
                                JarvisCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.3f
                        ),
                        center = center,
                        radius = radius * 1.3f
                    )

                    // Outer orbit ring
                    drawCircle(
                        color = JarvisArcGold.copy(alpha = 0.6f),
                        center = center,
                        radius = radius,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Rotating wireframe grid
                    rotate(degrees = orbRotation, pivot = center) {
                        val lines = 8
                        for (i in 0 until lines) {
                            val angleRad = Math.toRadians((i * (180.0 / lines)))
                            val dx = radius * cos(angleRad).toFloat()
                            val dy = radius * sin(angleRad).toFloat()
                            drawLine(
                                color = JarvisArcGold.copy(alpha = 0.4f),
                                start = Offset(center.x - dx, center.y - dy),
                                end = Offset(center.x + dx, center.y + dy),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // Bright core sun nucleus
                    drawCircle(
                        color = Color.White,
                        center = center,
                        radius = 5.dp.toPx()
                    )
                }
            }
        }
    }
}
