package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProtocolEntity
import com.example.ui.components.IosCompanionCard
import com.example.ui.components.iosGlassSurface
import com.example.ui.theme.IosAccentAmber
import com.example.ui.theme.IosAccentIndigo
import com.example.ui.theme.IosAccentPeach
import com.example.ui.theme.IosAccentRose
import com.example.ui.theme.IosAccentSage
import com.example.ui.theme.IosAccentSky
import com.example.ui.theme.IosAccentViolet
import com.example.ui.theme.IosBorderGlass
import com.example.ui.theme.IosBorderSubtle
import com.example.ui.theme.IosSurfaceCard
import com.example.ui.theme.IosSurfaceCardElevated
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary
import com.example.ui.viewmodel.JarvisViewModel

data class CompanionRoutineItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val category: String,
    val durationText: String,
    val protocolCode: String
)

@Composable
fun CompanionRoutinesScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val executionState by viewModel.protocolExecution.collectAsState()
    val btcOracle by viewModel.btcOracleTelemetry.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val routines = listOf(
        CompanionRoutineItem(
            id = "focus",
            title = "Calm Sanctuary",
            description = "Silences harsh notifications, softens media volume to 15%, and establishes peaceful focus.",
            icon = Icons.Default.Spa,
            accentColor = IosAccentViolet,
            category = "PEACE OF MIND",
            durationText = "Instant",
            protocolCode = "S25_DEEP_FOCUS"
        ),
        CompanionRoutineItem(
            id = "shield",
            title = "Guardian Shield",
            description = "Purges stale caches, inspects Wi-Fi link security, and confirms system protection.",
            icon = Icons.Default.Shield,
            accentColor = IosAccentSage,
            category = "PROTECTION",
            durationText = "15s scan",
            protocolCode = "S25_WIFI_DEFENSE"
        ),
        CompanionRoutineItem(
            id = "clean",
            title = "Fresh Slate Refresh",
            description = "Gently releases background memory, resets display refresh pacing, and leaves device refreshed.",
            icon = Icons.Default.Favorite,
            accentColor = IosAccentSky,
            category = "WELLBEING",
            durationText = "Complete",
            protocolCode = "S25_CLEAN_SLATE"
        ),
        CompanionRoutineItem(
            id = "night",
            title = "Rest & Battery Saver",
            description = "Softens display refresh to 60Hz and reduces background polling for all-night battery peace.",
            icon = Icons.Default.Nightlight,
            accentColor = IosAccentIndigo,
            category = "REST",
            durationText = "Overnight",
            protocolCode = "S25_BATTERY_SAVER"
        ),
        CompanionRoutineItem(
            id = "chime",
            title = "Gentle Phone Chime",
            description = "Rings a soft acoustic locator tone and flashes light gently so you can locate your phone.",
            icon = Icons.Default.NotificationsActive,
            accentColor = IosAccentRose,
            category = "CONVENIENCE",
            durationText = "3s chime",
            protocolCode = "ACOUSTIC_SONAR_PING"
        ),
        CompanionRoutineItem(
            id = "health",
            title = "Device Health Benchmark",
            description = "Analyzes Snapdragon 8 Elite thermals, battery cell balance, and 12GB memory health.",
            icon = Icons.Default.BatteryChargingFull,
            accentColor = IosAccentAmber,
            category = "VITALITY",
            durationText = "Benchmark",
            protocolCode = "S25_DIAGNOSTIC_SCAN"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ============================================================
        // 1. ROUTINES HEADER
        // ============================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MOMENTS & CARE",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IosAccentViolet,
                    letterSpacing = 1.2.sp,
                    fontSize = 13.sp
                )
                Text(
                    text = "Thoughtful Routines",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    fontSize = 28.sp
                )
                Text(
                    text = "Gentle actions designed to care for your peace of mind and device vitality.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = IosTextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // Active Routine Progress Banner
        if (executionState.isExecuting) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(IosAccentViolet.copy(alpha = 0.16f))
                        .border(1.dp, IosAccentViolet.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = executionState.currentProtocol?.displayName ?: "Routine in progress",
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${(executionState.progressPercent * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = IosAccentViolet,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = executionState.currentStepText,
                            color = IosTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ============================================================
        // 2. BITCOIN & LIQUIDITY ORACLE WIDGET (Apple Stocks Style)
        // ============================================================
        item {
            IosCompanionCard(
                title = "Bitcoin & Market Glance",
                subtitle = "Decentralized Oracle & Liquidity Feed",
                icon = Icons.Default.CurrencyBitcoin,
                badgeText = "Live Feed",
                badgeColor = IosAccentAmber
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Big Apple Stocks Style Price Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "BTC / USD",
                                fontSize = 12.sp,
                                color = IosTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%,.2f", btcOracle.btcUsd)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(IosAccentSage.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+${String.format(java.util.Locale.US, "%.2f", btcOracle.effectiveYieldApy)}% APY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosAccentSage
                            )
                        }
                    }

                    // Key Metrics Pill Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(IosSurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("1M VOLATILITY", fontSize = 9.sp, color = IosTextTertiary, fontWeight = FontWeight.Bold)
                                Text("${btcOracle.vix1moPercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IosAccentAmber)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(IosSurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("ETH GAS MEDIAN", fontSize = 9.sp, color = IosTextTertiary, fontWeight = FontWeight.Bold)
                                Text("${btcOracle.ethGasGweiMedian} Gwei", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IosAccentViolet)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(IosSurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("POOL DEPTH", fontSize = 9.sp, color = IosTextTertiary, fontWeight = FontWeight.Bold)
                                Text("${String.format(java.util.Locale.US, "%,.0f", btcOracle.poolDepthBtc)} BTC", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IosAccentSky)
                            }
                        }
                    }

                    // Interaction Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val speech = "Bitcoin Oracle verified. Price is $${btcOracle.btcUsd.toInt()} dollars. Volatility is ${btcOracle.vix1moPercent} percent, Ethereum gas is ${btcOracle.ethGasGweiMedian} Gwei, and yield APY is ${String.format(java.util.Locale.US, "%.2f", btcOracle.effectiveYieldApy)} percent."
                                viewModel.voiceEngine.speak(speech, playChimeBefore = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IosAccentAmber),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.5f).height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Read Aloud",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.tickBtcOracleMetrics(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = IosSurfaceCardElevated),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("+100", color = IosAccentSage, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.tickBtcOracleMetrics(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = IosSurfaceCardElevated),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("-100", color = IosAccentRose, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ============================================================
        // 3. COMPANION ROUTINES LIST
        // ============================================================
        item {
            Text(
                text = "CARING ROUTINES",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = IosTextTertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        items(routines) { routine ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .iosGlassSurface(cornerRadius = 22.dp)
                    .clickable {
                        viewModel.executeProtocol(
                            ProtocolEntity(
                                codeName = routine.protocolCode,
                                displayName = routine.title,
                                description = routine.description,
                                actionsJson = "[]",
                                category = routine.category,
                                isPreset = true
                            )
                        )
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soft Apple Style Icon Squircle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(routine.accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = routine.icon,
                            contentDescription = null,
                            tint = routine.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Description text
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = routine.title,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = IosTextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "•  ${routine.durationText}",
                                color = IosTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = routine.description,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = IosTextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // Play / Engage button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(IosSurfaceCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Engage",
                            tint = routine.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
