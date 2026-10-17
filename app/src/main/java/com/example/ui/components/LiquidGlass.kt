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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.Typography
import kotlinx.coroutines.launch

/**
 * One UI 8.5 / 9 Custom Glassmorphism Surface Modifier Extension.
 * Applies multi-layer background blur, translucent frosted glass fill,
 * refractive specular edge highlights, animated liquid sheen, and tactile liquid ripple.
 */
@Composable
fun Modifier.glassmorphicSurface(
    blurRadius: Dp = 0.dp,
    cornerRadius: Dp = 20.dp,
    glassColor: Color = Color.White.copy(alpha = 0.08f),
    borderColor: Color = Color.White.copy(alpha = 0.40f),
    accentColor: Color = JarvisCyanBright,
    borderWidth: Dp = 1.2.dp,
    showSpecularSheen: Boolean = true,
    refractiveGlow: Boolean = true,
    dropShadow: Boolean = true,
    interactiveRipple: Boolean = true
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "GlassmorphicSheenTransition")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_progress"
    )

    val distortionPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "distortion_phase"
    )

    var breathingTargetState by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2400L)
            breathingTargetState = !breathingTargetState
        }
    }

    val refractiveBreathingAlpha by animateFloatAsState(
        targetValue = if (breathingTargetState) 0.38f else 0.16f,
        animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
        label = "refractive_breathing"
    )

    var touchOffset by remember { mutableStateOf<Offset?>(null) }
    val rippleRadius = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    return this
        .then(
            if (dropShadow) {
                Modifier.drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.04f),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                        size = Size(size.width, size.height + 4.dp.toPx())
                    )
                }
            } else Modifier
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            Brush.linearGradient(
                colors = listOf(
                    glassColor,
                    Color.White.copy(alpha = 0.12f),
                    Color(0x18FFFFFF),
                    Color.White.copy(alpha = 0.04f)
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
                    Color.White.copy(alpha = 0.60f),
                    accentColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.20f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .drawWithContent {
            val width = size.width
            val height = size.height

            // 0. Ambient Refractive Inner Glow
            if (refractiveGlow && width > 0 && height > 0) {
                val focalX = width * (0.5f + 0.18f * kotlin.math.sin(distortionPhase.toDouble()).toFloat())
                val focalY = height * (0.5f + 0.15f * kotlin.math.cos(distortionPhase.toDouble()).toFloat())
                val dynamicRadius = kotlin.math.max(width, height) * (0.70f + 0.20f * kotlin.math.sin(distortionPhase.toDouble() * 1.4).toFloat())

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f * refractiveBreathingAlpha),
                            JarvisBlueCore.copy(alpha = 0.25f * refractiveBreathingAlpha),
                            Color.Transparent
                        ),
                        center = Offset(focalX, focalY),
                        radius = dynamicRadius
                    )
                )
            }

            drawContent()

            // 1. Specular Sheen Light Sweep
            if (showSpecularSheen && width > 0 && height > 0) {
                val sheenX = width * sheenProgress
                val sheenWidth = width * 0.35f

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.38f),
                            accentColor.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        start = Offset(sheenX - sheenWidth, 0f),
                        end = Offset(sheenX + sheenWidth, height)
                    ),
                    blendMode = BlendMode.Screen
                )
            }

            // 2. Specular Top Rim Refraction
            if (width > 0 && height > 0) {
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
                            Color.White.copy(alpha = 0.50f),
                            accentColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height * 0.35f
                    ),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }

            // 3. Interactive Touch Liquid Ripple Wave
            touchOffset?.let { offset ->
                val maxRadius = kotlin.math.max(width, height) * 1.2f
                if (rippleRadius.value > 0f && rippleRadius.value < maxRadius) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.45f * (1f - rippleRadius.value / maxRadius)),
                                JarvisBlueCore.copy(alpha = 0.25f * (1f - rippleRadius.value / maxRadius)),
                                Color.Transparent
                            ),
                            center = offset,
                            radius = rippleRadius.value
                        ),
                        center = offset,
                        radius = rippleRadius.value
                    )
                }
            }
        }
        .then(
            if (interactiveRipple) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        touchOffset = offset
                        scope.launch {
                            rippleRadius.snapTo(0f)
                            rippleRadius.animateTo(
                                targetValue = 500f,
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                }
            } else Modifier
        )
}

/**
 * Convenience modifier extension tuned specifically for Samsung One UI 8.5 / One UI 9 'Liquid Glass' UI containers.
 */
@Composable
fun Modifier.liquidGlassSurface(
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 22.dp,
    transparencyAlpha: Float = 0.30f,
    accentColor: Color = JarvisCyanBright,
    interactiveRipple: Boolean = true
): Modifier = this.glassmorphicSurface(
    blurRadius = blurRadius,
    cornerRadius = cornerRadius,
    glassColor = JarvisGlassPanel.copy(alpha = transparencyAlpha),
    borderColor = Color.White.copy(alpha = 0.70f),
    accentColor = accentColor,
    borderWidth = 1.2.dp,
    showSpecularSheen = true,
    refractiveGlow = true,
    dropShadow = true,
    interactiveRipple = interactiveRipple
)

/**
 * Liquid Glass Modifier Extension.
 * Renders a semi-transparent cyan-blue glass body with double-layer light glare,
 * liquid specular reflection sheen pass, and touch ripple distortion.
 */
@Composable
fun Modifier.liquidGlassEffect(
    cornerRadius: Dp = 16.dp,
    cyanAlpha: Float = 0.25f,
    blueAlpha: Float = 0.35f,
    showSpecularSheen: Boolean = true,
    interactiveRipple: Boolean = true
): Modifier {
    return this.glassmorphicSurface(
        blurRadius = 16.dp,
        cornerRadius = cornerRadius,
        glassColor = JarvisGlassPanel.copy(alpha = (cyanAlpha + blueAlpha) / 2f),
        accentColor = JarvisCyanBright,
        showSpecularSheen = showSpecularSheen,
        interactiveRipple = interactiveRipple
    )
}

/**
 * Glassmorphic Surface Composable Container suitable for One UI 8.5 / 9 layouts.
 * Replaces standard M3 Surface with a translucent glass backdrop featuring background blur,
 * specular edge lighting, and depth separation.
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 18.dp,
    cornerRadius: Dp = 20.dp,
    glassColor: Color = Color(0x22101F38),
    borderColor: Color = Color.White.copy(alpha = 0.65f),
    accentColor: Color = JarvisCyanBright,
    borderWidth: Dp = 1.2.dp,
    showSpecularSheen: Boolean = true,
    interactiveRipple: Boolean = true,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphicSurface(
                blurRadius = blurRadius,
                cornerRadius = cornerRadius,
                glassColor = glassColor,
                borderColor = borderColor,
                accentColor = accentColor,
                borderWidth = borderWidth,
                showSpecularSheen = showSpecularSheen,
                interactiveRipple = interactiveRipple
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * One UI 8.5 / 9 Liquid Glass Surface Composable Container.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 22.dp,
    transparencyAlpha: Float = 0.30f,
    accentColor: Color = JarvisCyanBright,
    title: String? = null,
    statusTag: String? = null,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    LiquidGlassContainer(
        modifier = modifier,
        cornerRadius = cornerRadius,
        cyanAlpha = transparencyAlpha,
        title = title,
        statusTag = statusTag,
        accentColor = accentColor,
        content = content
    )
}

/**
 * Liquid Glass Container / Card Component with Refractive Light Sheen and Cyan-Blue Gradient.
 */
@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    cyanAlpha: Float = 0.25f,
    blueAlpha: Float = 0.35f,
    title: String? = null,
    statusTag: String? = null,
    accentColor: Color = JarvisCyan,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphicSurface(
                blurRadius = 16.dp,
                cornerRadius = cornerRadius,
                glassColor = JarvisGlassPanel.copy(alpha = (cyanAlpha + blueAlpha) / 2f),
                accentColor = accentColor,
                showSpecularSheen = true,
                interactiveRipple = true
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (title != null || statusTag != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Glass Indicator Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title.uppercase(),
                                style = Typography.labelMedium,
                                fontSize = 11.sp,
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    if (statusTag != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusTag.uppercase(),
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

/**
 * Custom Liquid Glass Button with Sheen, Fluid Tap Scaling, and Cyan Glow.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = JarvisCyan,
    cornerRadius: Dp = 12.dp,
    testTag: String = "liquid_glass_button",
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "liquid_button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.28f),
                        JarvisBlueCore.copy(alpha = 0.35f),
                        JarvisCyanDark.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        accentColor,
                        JarvisBlueCore.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Liquid Glass Toggle Switch with Smooth Sliding Glass Bulb and Cyan Glow Track.
 */
@Composable
fun LiquidGlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "liquid_glass_toggle"
) {
    val bulbOffset by animateFloatAsState(
        targetValue = if (checked) 24f else 0f,
        animationSpec = spring(stiffness = 300f),
        label = "bulb_position"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            JarvisCyanDark.copy(alpha = 0.6f),
                            JarvisCyan.copy(alpha = 0.45f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            JarvisSurfaceDark,
                            JarvisSurfaceElevated
                        )
                    )
                }
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        if (checked) JarvisCyanBright else JarvisTextMuted.copy(alpha = 0.5f),
                        if (checked) JarvisBlueCore else JarvisSurfaceCard
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding Liquid Glass Bulb Knob
        Box(
            modifier = Modifier
                .offset(x = bulbOffset.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            if (checked) JarvisCyanBright else JarvisTextSecondary,
                            if (checked) JarvisBlueCore else JarvisSurfaceCard
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

/**
 * Helper spring for smooth toggle animation without needing external imports
 */
private fun <T> spring(stiffness: Float) = tween<T>(durationMillis = 200, easing = FastOutSlowInEasing)

/**
 * Liquid Glass Slider Component with Refractive Track and Glossy Glass Knob.
 */
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedRange<Float> = 0f..1f,
    accentColor: Color = JarvisCyan,
    testTag: String = "liquid_glass_slider"
) {
    var widthPx by remember { mutableStateOf(1f) }

    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newNorm = (normalizedValue + dragAmount.x / widthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newNorm = (offset.x / widthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            widthPx = size.width

            // Track Background (Glassy)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        JarvisSurfaceDark,
                        JarvisGlassPanel,
                        JarvisSurfaceElevated
                    )
                ),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // Active Progress Fill (Liquid Cyan Gradient)
            if (normalizedValue > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            JarvisBlueCore.copy(alpha = 0.6f),
                            accentColor.copy(alpha = 0.85f),
                            JarvisCyanBright
                        )
                    ),
                    size = Size(size.width * normalizedValue, size.height),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
            }

            // Glass Rim Border
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.7f),
                        JarvisBorderGlow
                    )
                ),
                style = Stroke(width = 1.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
        }

        // Draggable Glass Thumb Knob
        val thumbOffset = ((widthPx * normalizedValue) - 10.dp.value).coerceAtLeast(0f)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            JarvisCyanBright,
                            accentColor,
                            JarvisBlueCore
                        )
                    )
                )
                .border(1.2.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Liquid Glass Segmented Bottom Tabs with Morphing Glass Pill Indicator.
 */
@Composable
fun LiquidGlassBottomTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = JarvisCyan,
    testTag: String = "liquid_glass_tabs"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        JarvisVoid.copy(alpha = 0.85f),
                        JarvisGlassPanel.copy(alpha = 0.75f),
                        JarvisVoid.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        accentColor.copy(alpha = 0.8f),
                        JarvisBlueCore.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (selected) {
                                Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                accentColor.copy(alpha = 0.35f),
                                                JarvisBlueCore.copy(alpha = 0.45f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.White, accentColor)
                                        ),
                                        RoundedCornerShape(18.dp)
                                    )
                            } else Modifier
                        )
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) JarvisCyanBright else JarvisTextSecondary
                    )
                }
            }
        }
    }
}
