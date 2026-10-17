package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.AlienBioluminescentMagenta
import com.example.ui.theme.AlienEmpathyCyan
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.delay

@Composable
fun JarvisBiometricSecurityOverlay(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isLocked by viewModel.isCoreInterfaceLocked.collectAsState()
    val authStatus by viewModel.biometricAuthStatus.collectAsState()
    val capability by viewModel.biometricCapability.collectAsState()
    val failureCount by viewModel.authFailureCount.collectAsState()

    var showPasscodeDialog by remember { mutableStateOf(false) }
    var enteredPasscode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }
    var autoPromptAttempted by remember { mutableStateOf(false) }

    // Auto-prompt on initial lock display
    LaunchedEffect(isLocked) {
        if (isLocked && !autoPromptAttempted && activity != null) {
            autoPromptAttempted = true
            delay(400)
            viewModel.authenticateCoreInterface(activity)
        }
    }

    // Scanning animation loops
    val infiniteTransition = rememberInfiniteTransition(label = "BiometricScanner")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanLine"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF07142E),
                        ObsidianVoid,
                        Color(0xFF010307)
                    ),
                    radius = 1200f
                )
            )
            .testTag("biometric_security_overlay")
    ) {
        // Holographic Background HUD Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val gridColor = Color(0x0C00F5FF)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Security Classification & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Shield",
                        tint = if (failureCount > 0) CrimsonDanger else AlienEmpathyCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JARVIS QUANTUM DEFENSE ENCLAVE",
                        style = Typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (failureCount > 0) CrimsonDanger else AlienEmpathyCyan,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "CORE INTERFACE SECURED",
                    style = Typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.8.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (failureCount > 0) Color(0x33EF4444) else Color(0x2200E5FF)
                        )
                        .border(
                            1.dp,
                            if (failureCount > 0) CrimsonDanger else JarvisCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (failureCount > 0) "SECURITY ALERT: RE-AUTHENTICATION REQUIRED" else "BIOMETRIC PROMPT LAYER ACTIVE",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (failureCount > 0) CrimsonDanger else JarvisCyanBright
                    )
                }
            }

            // Central Biometric Scanner HUD Reticle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clickable {
                        if (activity != null) {
                            viewModel.authenticateCoreInterface(activity)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Rotating Arc Rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 2.dp.toPx()
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 10.dp.toPx()

                    // Background circle
                    drawCircle(
                        color = Color(0x1800F5FF),
                        radius = radius,
                        center = centerOffset
                    )

                    // Outer segmented glowing reticle
                    val ringColor = if (failureCount > 0) CrimsonDanger else JarvisCyan
                    drawArc(
                        color = ringColor.copy(alpha = 0.85f),
                        startAngle = rotationAngle,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor.copy(alpha = 0.85f),
                        startAngle = rotationAngle + 120f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor.copy(alpha = 0.85f),
                        startAngle = rotationAngle + 240f,
                        sweepAngle = 80f,
                        useCenter = false,
                        topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Laser scan horizontal beam
                    val beamY = size.height * scanLineY
                    drawLine(
                        color = if (failureCount > 0) CrimsonDanger.copy(alpha = 0.8f) else JarvisCyanBright.copy(alpha = 0.85f),
                        start = Offset(size.width * 0.18f, beamY),
                        end = Offset(size.width * 0.82f, beamY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }

                // Inner Fingerprint Glyph Container
                Box(
                    modifier = Modifier
                        .size((120 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (failureCount > 0) Color(0x66EF4444) else Color(0x4000F5FF),
                                    Color(0x1007142E),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            if (failureCount > 0) CrimsonDanger else JarvisCyan,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Sensor",
                        tint = if (failureCount > 0) CrimsonDanger else JarvisCyanBright,
                        modifier = Modifier.size(68.dp)
                    )
                }
            }

            // Telemetry & Hardware Readout Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD90E1C3C))
                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Sensor Status",
                            tint = SkyAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HARDWARE SENSOR TELEMETRY",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = if (capability.isAvailable) "ARMED" else "READY / FALLBACK",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (capability.isAvailable) EmeraldNominal else SkyAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sensor Profile:",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = JarvisTextMuted
                    )
                    Text(
                        text = capability.hardwareSensorType,
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = JarvisTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cipher Status:",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = JarvisTextMuted
                    )
                    Text(
                        text = "AES-256 SESSION KEY GUARDED",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = AlienEmpathyCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Biometric Sensor:",
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = JarvisTextMuted
                    )
                    Text(
                        text = capability.statusText,
                        style = Typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (capability.isAvailable) EmeraldNominal else SkyAccent
                    )
                }

                if (authStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (failureCount > 0) Color(0x33EF4444) else Color(0x2200E5FF)
                            )
                            .border(
                                1.dp,
                                if (failureCount > 0) CrimsonDanger else JarvisCyan,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = authStatus ?: "",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = if (failureCount > 0) CrimsonDanger else JarvisCyanBright,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Interactive Actions Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Biometric Prompt Button
                Button(
                    onClick = {
                        if (activity != null) {
                            viewModel.authenticateCoreInterface(activity)
                        } else {
                            viewModel.emergencyPasscodeUnlock("2026")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("biometric_authenticate_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Scan Fingerprint",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AUTHENTICATE VIA BIOMETRIC SENSOR",
                            style = Typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Fallback / Emergency Passcode Button
                Button(
                    onClick = {
                        showPasscodeDialog = !showPasscodeDialog
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("biometric_passcode_toggle_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33101F38),
                        contentColor = SkyAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Emergency Override",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showPasscodeDialog) "HIDE PASSCODE OVERRIDE" else "EMERGENCY PASSCODE OVERRIDE",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Passcode Entry Input
                AnimatedVisibility(
                    visible = showPasscodeDialog,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xE6081028))
                            .border(1.dp, Color(0x5538BDF8), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "EMERGENCY HARDWARE OVERRIDE (PASSKEY: 2026)",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = SkyAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = enteredPasscode,
                                onValueChange = {
                                    enteredPasscode = it
                                    passcodeError = null
                                },
                                placeholder = {
                                    Text(
                                        "Enter '2026' or 'JARVIS'",
                                        style = Typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = JarvisTextMuted
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    val success = viewModel.emergencyPasscodeUnlock(enteredPasscode)
                                    if (!success) passcodeError = "Code Rejected"
                                }),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("biometric_passcode_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = Color(0x4438BDF8),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    val success = viewModel.emergencyPasscodeUnlock(enteredPasscode)
                                    if (!success) passcodeError = "Code Rejected"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x3300F5FF),
                                    contentColor = JarvisCyanBright
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("biometric_passcode_submit_btn")
                            ) {
                                Text(
                                    text = "VERIFY",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (passcodeError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = passcodeError ?: "",
                                color = CrimsonDanger,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
