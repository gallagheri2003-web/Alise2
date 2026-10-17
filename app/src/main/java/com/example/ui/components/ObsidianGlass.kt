package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SlatePanelBg

/**
 * Obsidian & Slate Glass Panel Modifier matching the visual design of https://sfs-2ee9enopyeto.app-ionos.space/
 * Features clean high-contrast dark slate background with refined translucent sky borders.
 */
fun Modifier.obsidianGlassPanel(
    cornerRadius: Dp = 12.dp,
    borderColor: Color = BorderSlate,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = SlatePanelBg
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.linearGradient(
            listOf(
                backgroundColor,
                ObsidianVoid.copy(alpha = 0.95f)
            )
        )
    )
    .border(
        width = borderWidth,
        color = borderColor,
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Adds a breathing holographic glow overlay effect over the component to enhance the JARVIS UI.
 */
fun Modifier.holographicBreathingGlow(
    color: Color = JarvisCyan,
    cornerRadius: Dp = 12.dp,
    minAlpha: Float = 0.02f,
    maxAlpha: Float = 0.12f,
    durationMs: Int = 2500
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "holographic_glow")
    val alpha = infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    ).value

    this.drawWithContent {
        drawContent()
        drawRoundRect(
            color = color.copy(alpha = alpha),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
        )
    }
}
