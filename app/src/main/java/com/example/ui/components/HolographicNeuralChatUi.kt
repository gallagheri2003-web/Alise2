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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Flowing, Glowing Neon Text Effect Composable.
 * Uses an infinite transition shifting gradient offsets to create a shimmering, glowing neon light movement.
 */
@Composable
fun GlowingNeonText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = AlienEmpathyCyan,
    glowSecondaryColor: Color = AlienBioluminescentMagenta,
    fontSize: Float = 12f,
    fontWeight: FontWeight = FontWeight.Normal,
    isFlowing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonFlowTransition")
    
    val phase by if (isFlowing) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "neon_phase"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    }

    val flowingBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            glowSecondaryColor,
            AlienCelestialEmerald,
            baseColor
        ),
        start = Offset(phase, 0f),
        end = Offset(phase + 500f, 200f)
    )

    Box(modifier = modifier) {
        // Glowing Neon Background Underlay
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = baseColor.copy(alpha = 0.45f)
            ),
            modifier = Modifier.padding(1.dp)
        )

        // Primary Flowing Gradient Neon Foreground Text
        Text(
            text = text,
            style = TextStyle(
                brush = flowingBrush,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                letterSpacing = 0.3.sp
            )
        )
    }
}

/**
 * Holographic Speech Bubble matching the Alien-Humanoid Aesthetic & One UI 8.5 / 9 spatial design.
 */
@Composable
fun HolographicSpeechBubble(
    sender: String,
    messageText: String,
    timestamp: Long,
    isUser: Boolean,
    modifier: Modifier = Modifier,
    modelUsed: String? = null,
    isSearchGrounded: Boolean = false,
    onSpeakClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val bubbleBorderColor = if (isUser) AlienEmpathyCyan else AlienBioluminescentMagenta
    val bubbleBackgroundColor = if (isUser) Color(0x280284C7) else Color(0x33100826)
    val accentGlow = if (isUser) AlienEmpathyCyan else AlienCelestialEmerald

    val infiniteTransition = rememberInfiniteTransition(label = "BubbleScanline")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 28.dp else 4.dp,
                end = if (isUser) 4.dp else 28.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .clip(
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                )
            )
            .background(bubbleBackgroundColor)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        bubbleBorderColor,
                        accentGlow.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                )
            )
            .drawWithContent {
                drawContent()

                // Holographic Scanline Overlay Effect
                val lineY = size.height * scanY
                drawLine(
                    color = bubbleBorderColor.copy(alpha = 0.25f),
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Specular Refraction Top Arc
                val specularPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(0f, 0f, size.width, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
                        )
                    )
                }
                drawPath(
                    path = specularPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.35f
                    ),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header Row: Sender Badge, Model Badge, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Glowing Orb Indicator
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isUser) AlienEmpathyCyan else AlienBioluminescentMagenta)
                    )

                    Text(
                        text = if (isUser) "HUMAN OPERATOR • $sender" else "ALISE NEURAL SYMBIONT",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (isUser) AlienEmpathyCyan else AlienBioluminescentMagenta,
                        fontSize = 8.5.sp,
                        letterSpacing = 1.sp
                    )

                    if (!isUser && modelUsed != null) {
                        Text(
                            text = "[$modelUsed]",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = AlienHarmonyGold,
                            fontSize = 7.5.sp
                        )
                    }

                    if (isSearchGrounded) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3300FF88))
                                .border(1.dp, AlienCelestialEmerald, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = AlienCelestialEmerald,
                                    modifier = Modifier.size(9.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "GROUNDED",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlienCelestialEmerald
                                )
                            }
                        }
                    }
                }

                Text(
                    text = timeStr,
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextMuted,
                    fontSize = 7.5.sp
                )
            }

            // Message Body: Flowing Glowing Neon Text
            GlowingNeonText(
                text = messageText,
                baseColor = if (isUser) Color.White else Color(0xFFF1F5F9),
                glowSecondaryColor = if (isUser) AlienEmpathyCyan else AlienBioluminescentMagenta,
                fontSize = 11.5f,
                fontWeight = FontWeight.Normal,
                isFlowing = !isUser
            )

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUser && onSpeakClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2200F5FF))
                            .border(1.dp, Color(0x4400F5FF), RoundedCornerShape(6.dp))
                            .clickable { onSpeakClick() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("bubble_speak_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                tint = AlienEmpathyCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SPEAK",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlienEmpathyCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(messageText))
                            onCopyClick?.invoke()
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("bubble_copy_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "COPY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
