package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JarvisDataCleanerCard(
    onCleanData: (onComplete: (cleanedMb: Int) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "jarvis_data_cleaner_card"
) {
    var isCleaning by remember { mutableStateOf(false) }
    var lastCleanResult by remember { mutableStateOf<String?>(null) }
    var junkCacheMb by remember { mutableStateOf(1840) } // Initial 1.84 GB junk cache
    var memoryBufferMb by remember { mutableStateOf(420) }

    val cleanProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "S25 ULTRA DATA CLEANER & MEMORY SWEEPER",
        statusTag = if (junkCacheMb > 0) "${junkCacheMb} MB JUNK" else "CLEAN & OPTIMIZED",
        accentColor = if (junkCacheMb > 0) JarvisArcGold else JarvisSuccessGreen,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Cache Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Junk Cache Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisSurfaceDark.copy(alpha = 0.5f))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = JarvisArcGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "APP JUNK CACHE",
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (junkCacheMb > 0) "${String.format("%.2f", junkCacheMb / 1024f)} GB" else "0.00 GB",
                            style = Typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (junkCacheMb > 0) JarvisArcGold else JarvisSuccessGreen
                        )
                    }
                }

                // RAM Swap Buffer Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisSurfaceDark.copy(alpha = 0.5f))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = JarvisCyanBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STALE RAM SWAP",
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$memoryBufferMb MB",
                            style = Typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyanBright
                        )
                    }
                }
            }

            // Cleaning Progress Laser Bar (Active during clean sweep)
            if (isCleaning) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SWEEPING JUNK & PURGING MEMORY...",
                            style = Typography.labelSmall,
                            fontSize = 9.5.sp,
                            color = JarvisCyanBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(cleanProgress.value * 100).toInt()}%",
                            style = Typography.labelSmall,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyanBright
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { cleanProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = JarvisCyanBright,
                        trackColor = JarvisSurfaceDark
                    )
                }
            }

            // Result Log Banner
            lastCleanResult?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSuccessGreen.copy(alpha = 0.15f))
                        .border(1.dp, JarvisSuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "✓ $msg",
                        style = Typography.bodySmall,
                        fontSize = 10.sp,
                        color = JarvisSuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action Button: Clean Data Now
            LiquidGlassButton(
                onClick = {
                    if (!isCleaning) {
                        isCleaning = true
                        scope.launch {
                            cleanProgress.snapTo(0f)
                            cleanProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
                            )
                            onCleanData { freedMb ->
                                junkCacheMb = 0
                                memoryBufferMb = 120
                                isCleaning = false
                                lastCleanResult = "CLEAN DATA COMPLETE: Reclaimed ${freedMb} MB of cache storage & flushed background RAM buffer."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                accentColor = if (junkCacheMb > 0) JarvisArcGold else JarvisCyanBright,
                testTag = "btn_clean_data_now"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clean Data Now",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCleaning) "SWEEPING SYSTEM DATA..." else "CLEAN DATA NOW (${junkCacheMb} MB)",
                        style = Typography.labelLarge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
