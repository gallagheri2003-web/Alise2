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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosAccentAmber
import com.example.ui.theme.IosAccentIndigo
import com.example.ui.theme.IosAccentPeach
import com.example.ui.theme.IosAccentRose
import com.example.ui.theme.IosAccentSage
import com.example.ui.theme.IosAccentSky
import com.example.ui.theme.IosAccentViolet
import com.example.ui.theme.IosBorderGlass
import com.example.ui.theme.IosBorderSubtle
import com.example.ui.theme.IosCanvasDark
import com.example.ui.theme.IosCanvasSoft
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSurfaceCardElevated
import com.example.ui.theme.IosSurfaceGlass
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary
import com.example.voice.VoiceState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * iOS Apple-style Frosted Glass Surface Modifier.
 * Provides soft squircle rounding, subtle depth, and specular edge highlight.
 */
fun Modifier.iosGlassSurface(
    cornerRadius: Dp = 24.dp,
    borderColor: Color = IosBorderGlass,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = IosSurfaceCard
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.linearGradient(
            listOf(
                backgroundColor.copy(alpha = 0.85f),
                IosSurfaceGlass.copy(alpha = 0.65f),
                IosCanvasSoft.copy(alpha = 0.90f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 800f)
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            listOf(
                borderColor.copy(alpha = 0.55f),
                borderColor.copy(alpha = 0.15f),
                Color.Transparent
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * Fullscreen atmospheric canvas for the iOS companion experience.
 * Replaces stark black grids with deep cosmic twilight and warm organic radial glows.
 */
@Composable
fun CompanionBackgroundCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_drift")
    val driftAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base deep cosmic canvas
                drawRect(color = IosCanvasDark)

                // Top ambient twilight glow (whisper of serene violet)
                val topCenter = Offset(
                    x = size.width * (0.3f + 0.4f * driftAnim),
                    y = size.height * 0.12f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IosAccentViolet.copy(alpha = 0.18f),
                            IosAccentIndigo.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = topCenter,
                        radius = size.width * 0.85f
                    ),
                    center = topCenter,
                    radius = size.width * 0.85f
                )

                // Bottom ambient warm glow (whisper of warm dusk rose)
                val bottomCenter = Offset(
                    x = size.width * (0.7f - 0.4f * driftAnim),
                    y = size.height * 0.85f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IosAccentRose.copy(alpha = 0.10f),
                            IosAccentPeach.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = bottomCenter,
                        radius = size.width * 0.9f
                    ),
                    center = bottomCenter,
                    radius = size.width * 0.9f
                )
            }
    ) {
        content()
    }
}

/**
 * THE COMPANION AURA — The living, breathing heart of ALICE.
 * Renders an ethereal, fluid, harmonic living entity that feels like an empathetic companion.
 */
@Composable
fun CompanionAura(
    voiceState: VoiceState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura_breath")
    
    // Gentle breathing pulse
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Gentle orbital phase
    val orbitalPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital"
    )

    // Quick active ripple for listening or speaking
    val activeRipple by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "active_ripple"
    )

    val isListening = voiceState is VoiceState.ListeningWakeWord || voiceState is VoiceState.ListeningCommand
    val isSpeaking = voiceState is VoiceState.Speaking
    val isProcessing = voiceState is VoiceState.Processing

    // Primary aura colors based on emotional companion state
    val (primaryAura, secondaryAura, haloAura) = when {
        isSpeaking -> Triple(IosAccentRose, IosAccentViolet, IosAccentPeach)
        isListening -> Triple(IosAccentSky, IosAccentViolet, IosAccentSage)
        isProcessing -> Triple(IosAccentViolet, IosAccentIndigo, IosAccentPeach)
        else -> Triple(IosAccentViolet, IosAccentIndigo, IosAccentSky)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.width / 2f) * 0.72f
            val dynamicRadius = baseRadius * breathScale * (1f + amplitude * 0.35f)

            // 1. Soft Ambient Luminescence (outer halo)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        haloAura.copy(alpha = if (isListening || isSpeaking) 0.35f else 0.18f),
                        primaryAura.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.50f
                ),
                center = center,
                radius = size.width * 0.50f
            )

            // 2. Dynamic Expanding Waves (when active)
            if (isListening || isSpeaking) {
                val rippleRadius = dynamicRadius * (1f + activeRipple * 0.45f)
                val rippleAlpha = (1f - activeRipple).coerceIn(0f, 1f) * 0.40f
                drawCircle(
                    color = primaryAura.copy(alpha = rippleAlpha),
                    center = center,
                    radius = rippleRadius,
                    style = Stroke(width = 2.5f)
                )
            }

            // 3. Fluid Ethereal Petals (Multi-layered harmonic ellipses)
            val numPetals = 4
            val radPhase = Math.toRadians(orbitalPhase.toDouble())
            for (i in 0 until numPetals) {
                val angle = radPhase + (i * PI * 2 / numPetals)
                val offsetDist = dynamicRadius * 0.18f
                val petalCenter = Offset(
                    center.x + (cos(angle) * offsetDist).toFloat(),
                    center.y + (sin(angle) * offsetDist).toFloat()
                )
                val petalColor = if (i % 2 == 0) primaryAura else secondaryAura

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            petalColor.copy(alpha = 0.65f),
                            petalColor.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = petalCenter,
                        radius = dynamicRadius * 0.85f
                    ),
                    center = petalCenter,
                    radius = dynamicRadius * 0.85f,
                    blendMode = BlendMode.Screen
                )
            }

            // 4. Luminous Nucleus (The Living Core)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        IosTextPrimary.copy(alpha = 0.80f),
                        primaryAura.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 0.45f
                ),
                center = center,
                radius = dynamicRadius * 0.45f
            )

            // 5. Specular Gloss Reflection (iOS Liquid Sheen)
            val glossCenter = Offset(center.x - dynamicRadius * 0.15f, center.y - dynamicRadius * 0.22f)
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                center = glossCenter,
                radius = dynamicRadius * 0.12f
            )
        }
    }
}

/**
 * iOS Inset Grouped Companion Card with soft frosted material and title.
 */
@Composable
fun IosCompanionCard(
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    badgeText: String? = null,
    badgeColor: Color = IosAccentViolet,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .iosGlassSurface(cornerRadius = 24.dp)
            .padding(18.dp)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = IosTextPrimary,
                            fontSize = 16.sp
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = IosTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        content()
    }
}

/**
 * iOS-style Segmented Selector with smooth pill selection.
 */
@Composable
fun <T> IosSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IosSurfaceCard)
            .border(1.dp, IosBorderSubtle, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) IosSurfaceCardElevated else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) IosBorderGlass else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) IosTextPrimary else IosTextSecondary,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

/**
 * iOS Companion Floating Bottom Navigation Bar.
 * Translucent glass capsule with soft icons and labels that float comfortably above bottom safe insets.
 */
@Composable
fun <T> IosCompanionBottomBar(
    destinations: List<T>,
    currentDestination: T,
    onDestinationSelected: (T) -> Unit,
    titleProvider: (T) -> String,
    iconProvider: (T) -> ImageVector,
    tagProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            IosSurfaceCardElevated.copy(alpha = 0.92f),
                            IosCanvasSoft.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(1.dp, IosBorderGlass.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                val isSelected = dest == currentDestination
                val activeColor = IosAccentViolet

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onDestinationSelected(dest) }
                        .testTag(tagProvider(dest))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.20f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconProvider(dest),
                            contentDescription = titleProvider(dest),
                            tint = if (isSelected) activeColor else IosTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = titleProvider(dest),
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) IosTextPrimary else IosTextTertiary,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}
