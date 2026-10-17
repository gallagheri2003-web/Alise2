package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienCelestialEmerald
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.AlienHarmonyGold
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.Typography

enum class OneUiLayoutMode(val title: String, val badge: String) {
    ONE_UI_PHONE("One UI 8.5 Phone", "MOBILE 120HZ"),
    ONE_UI_DEX("One UI 9 DeX", "COMPUTER IN PHONE"),
    IOS_27_LIQUID("iOS 27 Spatial", "LIQUID GRID")
}

/**
 * One UI 8.5 / 9 Computer-in-a-Phone Customizable Widget Deck Shell.
 * Provides Samsung-style customization controls and responsive widget container layouts.
 */
@Composable
fun OneUiWidgetCustomizationBar(
    selectedMode: OneUiLayoutMode,
    onModeSelect: (OneUiLayoutMode) -> Unit,
    activeAccentColor: Color,
    onAccentColorSelect: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomizerPanel by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .alienSymbiontSurface(
                cornerRadius = 18.dp,
                glassColor = Color(0xF5081024),
                borderColor = activeAccentColor.copy(alpha = 0.6f),
                borderWidth = 1.dp
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Layout Mode Badges
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OneUiLayoutMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) activeAccentColor.copy(alpha = 0.35f)
                                    else Color(0x181E293B)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) activeAccentColor else Color(0x2200F5FF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onModeSelect(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("oneui_mode_${mode.name}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (mode) {
                                        OneUiLayoutMode.ONE_UI_PHONE -> Icons.Default.Smartphone
                                        OneUiLayoutMode.ONE_UI_DEX -> Icons.Default.Computer
                                        OneUiLayoutMode.IOS_27_LIQUID -> Icons.Default.GridView
                                    },
                                    contentDescription = mode.title,
                                    tint = if (isSelected) Color.White else JarvisTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = mode.badge,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else JarvisTextMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                // Right: Samsung Customizer Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showCustomizerPanel) activeAccentColor.copy(alpha = 0.4f) else Color(0x2200F5FF))
                        .border(1.dp, activeAccentColor, RoundedCornerShape(10.dp))
                        .clickable { showCustomizerPanel = !showCustomizerPanel }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("samsung_customizer_toggle")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Customize",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SAMSUNG THEME",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Samsung Customizer Expanded Panel
            AnimatedVisibility(
                visible = showCustomizerPanel,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF030A18))
                        .border(1.dp, Color(0x3300F5FF), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ONE UI 9 / SAMSUNG CUSTOMIZATION ENGINE",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = activeAccentColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bioluminescent Accent Color:",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 8.5.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                AlienEmpathyCyan,
                                AlienBioluminescentMagenta,
                                AlienCelestialEmerald,
                                AlienHarmonyGold
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (activeAccentColor == color) 2.dp else 1.dp,
                                            color = if (activeAccentColor == color) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onAccentColorSelect(color) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable One UI 8.5 / 9 Computer-in-a-Phone Widget Container Card.
 */
@Composable
fun OneUiWidgetCard(
    title: String,
    badge: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AlienEmpathyCyan,
    onExpandClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alienSymbiontSurface(
                cornerRadius = 20.dp,
                glassColor = Color(0xF2071228),
                borderColor = accentColor.copy(alpha = 0.5f),
                borderWidth = 1.dp,
                accentGlow = accentColor
            )
            .padding(12.dp)
    ) {
        Column {
            // Widget Title Bar (DeX / Computer-in-a-Phone Window Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Computer-in-a-phone window controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AlienHarmonyGold)
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AlienCelestialEmerald)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = title.uppercase(),
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { onExpandClick?.invoke() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}
