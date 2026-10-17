package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.Typography

/**
 * High-performance, 120 FPS-optimized horizontal scrollable row with subtle
 * holographic HUD edge indicators and smooth inertial fling behavior.
 */
@Composable
fun HorizontalHudScrollRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    edgeFadeWidth: Dp = 16.dp,
    showIndicators: Boolean = true,
    tag: String = "horizontal_hud_scroll_row",
    content: LazyListScope.() -> Unit
) {
    val canScrollBackward by remember {
        derivedStateOf { state.canScrollBackward }
    }
    val canScrollForward by remember {
        derivedStateOf { state.canScrollForward }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        LazyRow(
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }

        if (showIndicators) {
            // Left Edge Indicator & Fading Gradient
            AnimatedVisibility(
                visible = canScrollBackward,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(edgeFadeWidth)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    JarvisVoid.copy(alpha = 0.85f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "‹",
                        color = JarvisCyan.copy(alpha = 0.7f),
                        style = Typography.labelSmall,
                        fontSize = 14.sp
                    )
                }
            }

            // Right Edge Indicator & Fading Gradient
            AnimatedVisibility(
                visible = canScrollForward,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(edgeFadeWidth)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    JarvisVoid.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "›",
                        color = JarvisCyan.copy(alpha = 0.7f),
                        style = Typography.labelSmall,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
