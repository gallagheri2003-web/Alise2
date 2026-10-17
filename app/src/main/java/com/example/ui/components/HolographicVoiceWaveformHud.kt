package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.voice.VoiceState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class HolographicWaveformStyle {
    CYBER_OSCILLOSCOPE,
    SPECTRAL_EQUALIZER,
    ACOUSTIC_RADIAL_IRIS
}

/**
 * High-fidelity, animated voice waveform visualization component for the holographic HUD overlay.
 * Features real-time dynamic amplitude responsiveness synchronized with AI voice synthesis output,
 * multi-frequency harmonic oscillations, glowing neon passes, FFT spectral bars, and acoustic radar mode.
 */
@Composable
fun HolographicVoiceWaveformHud(
    amplitude: Float,
    voiceState: VoiceState,
    modifier: Modifier = Modifier,
    activeVoiceName: String = "ALISE Slate Natural",
    heightDp: Dp = 120.dp,
    showControls: Boolean = true,
    showTelemetryReadout: Boolean = true,
    onTestVoiceClick: (() -> Unit)? = null,
    onBargeInClick: (() -> Unit)? = null,
    initialStyle: HolographicWaveformStyle = HolographicWaveformStyle.CYBER_OSCILLOSCOPE
) {
    var selectedStyle by remember { mutableStateOf(initialStyle) }

    val isSpeaking = voiceState is VoiceState.Speaking
    val isListening = voiceState is VoiceState.ListeningCommand || voiceState is VoiceState.ListeningWakeWord
    val isRecordingOrProcessing = voiceState is VoiceState.Processing

    // Smooth interpolated amplitude response with natural attack and release spring
    val animatedAmp by animateFloatAsState(
        targetValue = when {
            isSpeaking -> amplitude.coerceIn(0.18f, 1.0f)
            isListening -> (amplitude * 1.25f).coerceIn(0.10f, 1.0f)
            else -> 0.05f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SmoothedWaveformAmplitude"
    )

    // Primary infinite transition for wave velocity, shimmer, and particle travel
    val infiniteTransition = rememberInfiniteTransition(label = "HoloWaveformInfinite")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 900 else 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val harmonicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -(2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 1400 else 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "harmonic_phase"
    )

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 450 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Dynamic thematic color accents based on current voice state
    val coreColor by animateColorAsState(
        targetValue = when {
            isSpeaking -> SkyBright
            isListening -> EmeraldNominal
            isRecordingOrProcessing -> AmberWarning
            else -> SkyBright.copy(alpha = 0.75f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "coreColor"
    )

    val glowAccentColor by animateColorAsState(
        targetValue = when {
            isSpeaking -> Color(0xFF38BDF8)
            isListening -> Color(0xFF22C55E)
            isRecordingOrProcessing -> Color(0xFFF59E0B)
            else -> Color(0xFF0284C7)
        },
        animationSpec = tween(durationMillis = 300),
        label = "glowAccentColor"
    )

    // dB Calculation for realistic military/holographic HUD audio readout
    val calculatedDb = remember(animatedAmp) {
        if (animatedAmp <= 0.05f) -48.0f
        else (-3.0f - (1.0f - animatedAmp) * 32.0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .obsidianGlassPanel(
                cornerRadius = 10.dp,
                borderColor = coreColor.copy(alpha = if (isSpeaking) 0.6f else 0.25f),
                backgroundColor = Color(0xF2020612)
            )
            .padding(8.dp)
            .testTag("holographic_voice_waveform_hud")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. TOP STATUS & ACOUSTIC TELEMETRY BANNER
            if (showTelemetryReadout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: State Indicator & Voice Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(coreColor)
                                .border(1.dp, coreColor.copy(alpha = pulseGlow), CircleShape)
                        )
                        Text(
                            text = when {
                                isSpeaking -> "AI VOCAL OUTPUT • PULSING"
                                isListening -> "MIC INPUT • MONITORING"
                                isRecordingOrProcessing -> "PROCESSING SPEECH STREAM"
                                else -> "VOICE HUD • AMBIENT"
                            },
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = coreColor,
                            fontSize = 8.5.sp
                        )

                        Text(
                            text = "• ${activeVoiceName.take(16).uppercase()}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.5.sp
                        )
                    }

                    // Right: dB & Frequency Metrics
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x2238BDF8))
                                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", calculatedDb)} dB",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (calculatedDb > -12f) AmberWarning else SkyBright,
                                fontSize = 7.5.sp
                            )
                        }

                        Text(
                            text = "48 kHz • 32b PCM",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 7.sp
                        )
                    }
                }
            }

            // 2. CENTRAL HIGH-FIDELITY ANIMATED WAVEFORM CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, Color(0x2238BDF8), RoundedCornerShape(6.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("waveform_render_canvas")
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f

                    // Draw Holographic Grid Backdrop
                    drawHolographicGrid(width, height, centerY)

                    when (selectedStyle) {
                        HolographicWaveformStyle.CYBER_OSCILLOSCOPE -> {
                            drawCyberOscilloscopeWaveform(
                                width = width,
                                height = height,
                                centerY = centerY,
                                amplitude = animatedAmp,
                                wavePhase = wavePhase,
                                harmonicPhase = harmonicPhase,
                                pulseGlow = pulseGlow,
                                isSpeaking = isSpeaking,
                                coreColor = coreColor,
                                glowAccentColor = glowAccentColor
                            )
                        }
                        HolographicWaveformStyle.SPECTRAL_EQUALIZER -> {
                            drawSpectralEqualizer(
                                width = width,
                                height = height,
                                centerY = centerY,
                                amplitude = animatedAmp,
                                wavePhase = wavePhase,
                                pulseGlow = pulseGlow,
                                isSpeaking = isSpeaking,
                                coreColor = coreColor,
                                glowAccentColor = glowAccentColor
                            )
                        }
                        HolographicWaveformStyle.ACOUSTIC_RADIAL_IRIS -> {
                            drawAcousticRadialIris(
                                width = width,
                                height = height,
                                amplitude = animatedAmp,
                                radarRotation = radarRotation,
                                pulseGlow = pulseGlow,
                                isSpeaking = isSpeaking,
                                coreColor = coreColor,
                                glowAccentColor = glowAccentColor
                            )
                        }
                    }

                    // Tactical HUD Corner Reticles
                    drawTacticalCornerReticles(width, height, coreColor.copy(alpha = 0.7f))
                }
            }

            // 3. BOTTOM VISUALIZER STYLE TOGGLES & INTERACTION BAR
            if (showControls) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Visualizer Mode Selector Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HolographicStyleChip(
                            label = "OSCILLO",
                            icon = Icons.Default.Waves,
                            isSelected = selectedStyle == HolographicWaveformStyle.CYBER_OSCILLOSCOPE,
                            accentColor = coreColor,
                            onClick = { selectedStyle = HolographicWaveformStyle.CYBER_OSCILLOSCOPE }
                        )

                        HolographicStyleChip(
                            label = "SPECTRUM",
                            icon = Icons.Default.GraphicEq,
                            isSelected = selectedStyle == HolographicWaveformStyle.SPECTRAL_EQUALIZER,
                            accentColor = coreColor,
                            onClick = { selectedStyle = HolographicWaveformStyle.SPECTRAL_EQUALIZER }
                        )

                        HolographicStyleChip(
                            label = "RADAR IRIS",
                            icon = Icons.Default.Radar,
                            isSelected = selectedStyle == HolographicWaveformStyle.ACOUSTIC_RADIAL_IRIS,
                            accentColor = coreColor,
                            onClick = { selectedStyle = HolographicWaveformStyle.ACOUSTIC_RADIAL_IRIS }
                        )
                    }

                    // Quick Action Buttons: Preview Speech / Mute
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSpeaking && onBargeInClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, CrimsonDanger, RoundedCornerShape(4.dp))
                                    .clickable { onBargeInClick() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("btn_waveform_stop_speaking")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Mute", tint = CrimsonDanger, modifier = Modifier.size(10.dp))
                                    Text("BARGE IN", style = Typography.labelSmall, fontFamily = FontFamily.Monospace, color = CrimsonDanger, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (onTestVoiceClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x2238BDF8))
                                    .border(1.dp, Color(0x5538BDF8), RoundedCornerShape(4.dp))
                                    .clickable { onTestVoiceClick() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("btn_waveform_test_voice")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test Voice", tint = SkyBright, modifier = Modifier.size(10.dp))
                                    Text("TEST VOICE", style = Typography.labelSmall, fontFamily = FontFamily.Monospace, color = SkyBright, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HolographicStyleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0x151E293B))
            .border(1.dp, if (isSelected) accentColor else Color(0x2238BDF8), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else JarvisTextMuted,
                modifier = Modifier.size(9.dp)
            )
            Text(
                text = label,
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else JarvisTextMuted,
                fontSize = 7.5.sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// CUSTOM CANVAS GRAPHICS & HOLOGRAPHIC RENDERING PIPELINE
// -------------------------------------------------------------------------------------------------

private fun DrawScope.drawHolographicGrid(width: Float, height: Float, centerY: Float) {
    // Horizontal center zero-line
    drawLine(
        color = Color(0x2238BDF8),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )

    // Vertical measurement divisions
    val divisions = 10
    val stepX = width / divisions
    for (i in 1 until divisions) {
        val x = i * stepX
        drawLine(
            color = Color(0x1038BDF8),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
    }

    // Top and Bottom amplitude limit guide lines
    drawLine(
        color = Color(0x0C38BDF8),
        start = Offset(0f, centerY - height * 0.38f),
        end = Offset(width, centerY - height * 0.38f),
        strokeWidth = 1f
    )
    drawLine(
        color = Color(0x0C38BDF8),
        start = Offset(0f, centerY + height * 0.38f),
        end = Offset(width, centerY + height * 0.38f),
        strokeWidth = 1f
    )
}

private fun DrawScope.drawCyberOscilloscopeWaveform(
    width: Float,
    height: Float,
    centerY: Float,
    amplitude: Float,
    wavePhase: Float,
    harmonicPhase: Float,
    pulseGlow: Float,
    isSpeaking: Boolean,
    coreColor: Color,
    glowAccentColor: Color
) {
    val ampMultiplier = (amplitude.coerceAtLeast(0.06f) * height * 0.44f)

    val primaryPath = Path()
    val harmonicPath = Path()
    val tertiaryPath = Path()

    val step = 3
    val totalSteps = width.toInt()

    // 1. Compute multi-pass harmonic sinusoidal curves
    for (x in 0..totalSteps step step) {
        val xNorm = x / width
        // Bell-curve envelope window so waveform tapers cleanly at screen edges
        val envelope = sin(xNorm * PI).toFloat()

        // Primary Core Sine Wave (Carrier + Dynamic Formant Modulation)
        val progress = xNorm * 5.0 * PI
        val yPrimary = centerY + (sin(progress + wavePhase) * ampMultiplier * envelope +
                sin(progress * 2.2 + wavePhase * 1.5) * (ampMultiplier * 0.35f * envelope)).toFloat()

        // Harmonic Secondary Wave (Counter-phase with golden/amber resonance)
        val yHarmonic = centerY + (sin((progress * 1.6) + harmonicPhase) * (ampMultiplier * 0.65f) * envelope).toFloat()

        // Tertiary Sub-harmonic Wave (Broad ambient bass envelope)
        val yTertiary = centerY + (sin((progress * 0.7) - wavePhase * 0.6) * (ampMultiplier * 0.4f) * envelope).toFloat()

        if (x == 0) {
            primaryPath.moveTo(x.toFloat(), yPrimary)
            harmonicPath.moveTo(x.toFloat(), yHarmonic)
            tertiaryPath.moveTo(x.toFloat(), yTertiary)
        } else {
            primaryPath.lineTo(x.toFloat(), yPrimary)
            harmonicPath.lineTo(x.toFloat(), yHarmonic)
            tertiaryPath.lineTo(x.toFloat(), yTertiary)
        }
    }

    // 2. Pass 1: Tertiary Ambient Shadow Ribbon
    drawPath(
        path = tertiaryPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x33A855F7),
                Color(0x5538BDF8),
                Color.Transparent
            )
        ),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // 3. Pass 2: Secondary Harmonic Wave (Authoritative Golden/Amber Resonance)
    drawPath(
        path = harmonicPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                JarvisArcGold.copy(alpha = 0.3f),
                JarvisArcGold.copy(alpha = 0.85f * pulseGlow),
                JarvisArcGold.copy(alpha = 0.3f),
                Color.Transparent
            )
        ),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    // 4. Pass 3: Primary Neon Aura Stroke (Atmospheric Bloom)
    drawPath(
        path = primaryPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                glowAccentColor.copy(alpha = 0.4f * pulseGlow),
                glowAccentColor.copy(alpha = 0.8f * pulseGlow),
                glowAccentColor.copy(alpha = 0.4f * pulseGlow),
                Color.Transparent
            )
        ),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
    )

    // 5. Pass 4: Primary Sharp Focused Core Beam
    drawPath(
        path = primaryPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                coreColor.copy(alpha = 0.6f),
                Color.White,
                coreColor.copy(alpha = 0.6f),
                Color.Transparent
            )
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // 6. Pass 5: Dynamic Traveling Energy Particles along Wave Peaks
    val particleCount = if (isSpeaking) 6 else 3
    for (i in 0 until particleCount) {
        val particleProgress = ((wavePhase / (2 * PI) + (i.toFloat() / particleCount)) % 1.0f).toFloat()
        val px = particleProgress * width
        val pNorm = px / width
        val pEnvelope = sin(pNorm * PI).toFloat()
        val py = centerY + (sin(pNorm * 5.0 * PI + wavePhase) * ampMultiplier * pEnvelope).toFloat()

        // Outer glow
        drawCircle(
            color = coreColor.copy(alpha = 0.5f * pulseGlow),
            radius = (3.5f + amplitude * 3.5f).dp.toPx(),
            center = Offset(px, py)
        )
        // Core white spark
        drawCircle(
            color = Color.White,
            radius = (1.5f + amplitude * 1.5f).dp.toPx(),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawSpectralEqualizer(
    width: Float,
    height: Float,
    centerY: Float,
    amplitude: Float,
    wavePhase: Float,
    pulseGlow: Float,
    isSpeaking: Boolean,
    coreColor: Color,
    glowAccentColor: Color
) {
    val barCount = 44
    val barSpacing = 2.5.dp.toPx()
    val totalSpacing = barSpacing * (barCount - 1)
    val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2f)

    val maxBarHeight = height * 0.88f

    for (i in 0 until barCount) {
        val normalizedIndex = i.toFloat() / (barCount - 1)
        val x = i * (barWidth + barSpacing)

        // Frequency Curve Simulation: Bass on left, mids in center, treble on right
        val frequencyWeight = when {
            normalizedIndex < 0.25f -> 1.25f + sin(normalizedIndex * 4f * PI.toFloat()) * 0.3f
            normalizedIndex < 0.70f -> 1.0f + cos((normalizedIndex - 0.25f) * 2f * PI.toFloat()) * 0.4f
            else -> 0.75f + sin(normalizedIndex * PI.toFloat()) * 0.5f
        }

        val harmonicOscillation = sin(normalizedIndex * 8.0 + wavePhase) * 0.35 +
                cos(normalizedIndex * 14.0 - wavePhase * 1.3) * 0.25 + 0.6

        val rawBarAmp = (amplitude.coerceAtLeast(0.08f) * frequencyWeight * harmonicOscillation.toFloat())
            .coerceIn(0.05f, 1.0f)

        val barH = rawBarAmp * (maxBarHeight / 2f)

        val topY = centerY - barH
        val bottomY = centerY + barH

        // Bar Gradient
        val barBrush = Brush.verticalGradient(
            colors = listOf(
                when {
                    rawBarAmp > 0.82f -> AmberWarning
                    rawBarAmp > 0.60f -> SkyBright
                    else -> glowAccentColor
                },
                coreColor,
                glowAccentColor
            ),
            startY = topY,
            endY = bottomY
        )

        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(x, topY),
            size = Size(barWidth, (bottomY - topY).coerceAtLeast(3f)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Peak Hold Dot
        val peakY = topY - 3.dp.toPx()
        drawCircle(
            color = if (rawBarAmp > 0.80f) AmberWarning else Color.White.copy(alpha = 0.85f * pulseGlow),
            radius = (barWidth * 0.45f).coerceIn(1.2f, 2.5.dp.toPx()),
            center = Offset(x + barWidth / 2f, peakY)
        )
    }
}

private fun DrawScope.drawAcousticRadialIris(
    width: Float,
    height: Float,
    amplitude: Float,
    radarRotation: Float,
    pulseGlow: Float,
    isSpeaking: Boolean,
    coreColor: Color,
    glowAccentColor: Color
) {
    val center = Offset(width / 2f, height / 2f)
    val baseRadius = (height * 0.28f).coerceAtLeast(20.dp.toPx())
    val maxRadius = (height * 0.46f).coerceAtLeast(35.dp.toPx())

    // 1. Concentric Holographic Range Rings
    drawCircle(
        color = Color(0x1838BDF8),
        radius = baseRadius * 0.6f,
        center = center,
        style = Stroke(width = 1f)
    )

    drawCircle(
        color = Color(0x2838BDF8),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
    )

    // Dynamic Expanding Sound Pulse Ring
    val dynamicPulseRadius = baseRadius + (amplitude * (maxRadius - baseRadius))
    drawCircle(
        color = coreColor.copy(alpha = (0.5f * (1.0f - (dynamicPulseRadius - baseRadius) / (maxRadius - baseRadius))).coerceIn(0.1f, 0.8f) * pulseGlow),
        radius = dynamicPulseRadius,
        center = center,
        style = Stroke(width = (2f + amplitude * 2f).dp.toPx())
    )

    // 2. 36-Spoke Radial Amplitude Iris
    val spokeCount = 36
    val angleStep = 360f / spokeCount

    for (i in 0 until spokeCount) {
        val angleDeg = i * angleStep + radarRotation
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val harmonicSpike = sin(i * 0.6 + Math.toRadians(radarRotation.toDouble()) * 2.0) * 0.4 + 0.6
        val spokeLength = (baseRadius * 0.2f) + (amplitude * (maxRadius - baseRadius) * harmonicSpike.toFloat())

        val startX = center.x + (baseRadius * cos(angleRad)).toFloat()
        val startY = center.y + (baseRadius * sin(angleRad)).toFloat()

        val endX = center.x + ((baseRadius + spokeLength) * cos(angleRad)).toFloat()
        val endY = center.y + ((baseRadius + spokeLength) * sin(angleRad)).toFloat()

        drawLine(
            color = if (i % 4 == 0) Color.White.copy(alpha = 0.9f) else coreColor.copy(alpha = 0.65f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = (1.5.dp.toPx()),
            cap = StrokeCap.Round
        )
    }

    // 3. Central Core Glowing Acoustic Emitter
    drawCircle(
        color = coreColor.copy(alpha = 0.35f * pulseGlow),
        radius = baseRadius * 0.35f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = (3.dp.toPx() + amplitude * 3.dp.toPx()),
        center = center
    )
}

private fun DrawScope.drawTacticalCornerReticles(width: Float, height: Float, reticleColor: Color) {
    val bracketLen = 8.dp.toPx()
    val strokeW = 1.2.dp.toPx()

    // Top-Left
    drawLine(reticleColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
    drawLine(reticleColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

    // Top-Right
    drawLine(reticleColor, Offset(width, 0f), Offset(width - bracketLen, 0f), strokeW)
    drawLine(reticleColor, Offset(width, 0f), Offset(width, bracketLen), strokeW)

    // Bottom-Left
    drawLine(reticleColor, Offset(0f, height), Offset(bracketLen, height), strokeW)
    drawLine(reticleColor, Offset(0f, height), Offset(0f, height - bracketLen), strokeW)

    // Bottom-Right
    drawLine(reticleColor, Offset(width, height), Offset(width - bracketLen, height), strokeW)
    drawLine(reticleColor, Offset(width, height), Offset(width, height - bracketLen), strokeW)
}
