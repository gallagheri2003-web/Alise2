package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class TacticalVisionMode(val label: String, val hudColor: Color, val overlayFilter: Color) {
    NORM_CYAN("CYAN MIL-STD", SkyBright, Color(0x1A0284C7)),
    NVG_PHOSPHOR("NVG GEN-4", Color(0xFF22C55E), Color(0x3315803D)),
    FLIR_WHITE_HOT("FLIR WHITE-HOT", Color(0xFFE2E8F0), Color(0x33475569)),
    FLIR_IRONBOW("FLIR THERMAL", Color(0xFFF97316), Color(0x2EF97316)),
    AR_FUSION("AR FUSION GRID", Color(0xFF38BDF8), Color(0x260284C7))
}

@Composable
fun VisionScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val telemetry by viewModel.telemetry.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var visionMode by remember { mutableStateOf(TacticalVisionMode.NORM_CYAN) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var isTargetLocked by remember { mutableStateOf(true) }
    var isLaserRangefinderActive by remember { mutableStateOf(true) }
    var measuredRangeMeters by remember { mutableStateOf(342) }
    var threatLevel by remember { mutableStateOf("EVALUATING") }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Dynamic laser rangefinder tick
    LaunchedEffect(isLaserRangefinderActive) {
        while (isLaserRangefinderActive) {
            kotlinx.coroutines.delay(1800)
            measuredRangeMeters = (320..365).random()
            threatLevel = if (measuredRangeMeters < 330) "HOSTILE IDENT" else "STANDBY"
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticlePulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // 1. Real Camera Viewfinder Feed (or Tactical Background Matrix)
        if (hasCameraPermission) {
            DisposableEffect(lifecycleOwner) {
                onDispose {
                    try {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        if (cameraProviderFuture.isDone) {
                            cameraProviderFuture.get().unbindAll()
                        }
                    } catch (_: Exception) {}
                }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                onRelease = {
                    try {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        if (cameraProviderFuture.isDone) {
                            cameraProviderFuture.get().unbindAll()
                        }
                    } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated sensor grid canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF0F172A), ObsidianVoid)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Permission Required",
                        tint = visionMode.hudColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TACTICAL OPTICAL SENSOR INITIALIZING...",
                        style = Typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = visionMode.hudColor)
                    ) {
                        Text("ENGAGE CAMERA HARDWARE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Vision Mode Tint & Phosphor / Scanline Filter
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(visionMode.overlayFilter)
        )

        // 3. Military HUD Tactical Overlay Canvas (Horizon Ladder, Azimuth Compass Tape, Stadiametric Reticle)
        MilitaryTacticalHudCanvas(
            hudColor = visionMode.hudColor,
            pitch = telemetry.sensorPitch,
            roll = telemetry.sensorRoll,
            azimuth = telemetry.sensorAzimuth,
            scanProgress = scanLineY,
            isLocked = isTargetLocked,
            rangeMeters = measuredRangeMeters,
            reticlePulse = reticlePulse,
            visionMode = visionMode
        )

        // 4. Tactical HUD Telemetry Corners & Status Blocks
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Azimuth heading tape & Zulu Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top Left: MGRS & Altitude MSL
                Box(
                    modifier = Modifier
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = visionMode.hudColor.copy(alpha = 0.4f),
                            backgroundColor = Color(0xCC050B14)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "GRID: ${locationState.mgrsCoordinate}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = visionMode.hudColor,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "LAT/LNG: ${locationState.dmsLatitude} ${locationState.dmsLongitude}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 8.5.sp
                        )
                        Text(
                            text = "ALT: ${String.format(Locale.US, "%.1f", locationState.altitudeFeet)} FT MSL | SPD: ${String.format(Locale.US, "%.1f", locationState.speedKnots)} KTS",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldNominal,
                            fontSize = 8.5.sp
                        )
                    }
                }

                // Top Right: Military Zulu Time & Target Telemetry
                Box(
                    modifier = Modifier
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = visionMode.hudColor.copy(alpha = 0.4f),
                            backgroundColor = Color(0xCC050B14)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ZULU: ${SimpleDateFormat("HH:mm:ss'Z'", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date())}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = visionMode.hudColor,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "HDG: ${locationState.cardinalHeading}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 8.5.sp
                        )
                        Text(
                            text = "LRF: ${if (isLaserRangefinderActive) "$measuredRangeMeters M (ACTIVE)" else "OFF"}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isLaserRangefinderActive) AmberWarning else JarvisTextMuted,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }

            // MIDDLE FLOATING TARGET INFO (If locked)
            if (isTargetLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .obsidianGlassPanel(
                            cornerRadius = 6.dp,
                            borderColor = CrimsonDanger,
                            backgroundColor = Color(0xDD180404)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CrimsonDanger)
                        )
                        Text(
                            text = "TGT-ALPHA [LOCKED] • RNG: ${measuredRangeMeters}m • THREAT: $threatLevel",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = CrimsonDanger,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // BOTTOM CONTROLS & VISION SELECTOR STRIP
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Zoom & Rangefinder controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = visionMode.hudColor.copy(alpha = 0.3f),
                            backgroundColor = Color(0xD9050B14)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ZOOM ${String.format(Locale.US, "%.1fx", zoomLevel)}",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = visionMode.hudColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(62.dp)
                        )
                        Slider(
                            value = zoomLevel,
                            onValueChange = { zoomLevel = it },
                            valueRange = 1.0f..8.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = visionMode.hudColor,
                                activeTrackColor = visionMode.hudColor,
                                inactiveTrackColor = Color(0x3338BDF8)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLaserRangefinderActive) visionMode.hudColor.copy(alpha = 0.25f) else Color(0x221E293B))
                                .border(1.dp, if (isLaserRangefinderActive) visionMode.hudColor else Color(0x4438BDF8), RoundedCornerShape(6.dp))
                                .clickable {
                                    isLaserRangefinderActive = !isLaserRangefinderActive
                                    viewModel.soundSynthesizer.playAcknowledgeTone()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "LRF",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isLaserRangefinderActive) visionMode.hudColor else JarvisTextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTargetLocked) CrimsonDanger.copy(alpha = 0.25f) else Color(0x221E293B))
                                .border(1.dp, if (isTargetLocked) CrimsonDanger else Color(0x4438BDF8), RoundedCornerShape(6.dp))
                                .clickable {
                                    isTargetLocked = !isTargetLocked
                                    viewModel.soundSynthesizer.playAcknowledgeTone()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isTargetLocked) "LOCK" else "FREE",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isTargetLocked) CrimsonDanger else JarvisTextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFlashlightOn) AmberWarning.copy(alpha = 0.25f) else Color(0x221E293B))
                                .border(1.dp, if (isFlashlightOn) AmberWarning else Color(0x4438BDF8), RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.toggleFlashlight()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Torch",
                                tint = if (isFlashlightOn) AmberWarning else JarvisTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Tactical Vision Modes Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TacticalVisionMode.values().forEach { mode ->
                        val selected = visionMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selected) mode.hudColor.copy(alpha = 0.25f) else Color(0xCC050B14)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) mode.hudColor else Color(0x2E38BDF8),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    visionMode = mode
                                    viewModel.soundSynthesizer.playAcknowledgeTone()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("vision_mode_${mode.name}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(mode.hudColor)
                                )
                                Text(
                                    text = mode.label,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else JarvisTextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilitaryTacticalHudCanvas(
    hudColor: Color,
    pitch: Float,
    roll: Float,
    azimuth: Float,
    scanProgress: Float,
    isLocked: Boolean,
    rangeMeters: Int,
    reticlePulse: Float,
    visionMode: TacticalVisionMode
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 1. CRT / Phosphor Scanline
        drawLine(
            color = hudColor.copy(alpha = 0.25f),
            start = Offset(0f, h * scanProgress),
            end = Offset(w, h * scanProgress),
            strokeWidth = 2f
        )

        // 2. Top Compass Azimuth Heading Tape
        val tapeY = 65f
        drawLine(
            color = hudColor.copy(alpha = 0.6f),
            start = Offset(cx - 150f, tapeY),
            end = Offset(cx + 150f, tapeY),
            strokeWidth = 1.5f
        )
        // Center Heading Indicator Tick
        drawPath(
            path = Path().apply {
                moveTo(cx, tapeY + 10f)
                lineTo(cx - 5f, tapeY + 2f)
                lineTo(cx + 5f, tapeY + 2f)
                close()
            },
            color = hudColor
        )

        // Compass Ticks around azimuth
        for (i in -4..4) {
            val tickAngle = (azimuth.toInt() + i * 15)
            val normAngle = (tickAngle % 360 + 360) % 360
            val tickX = cx + (i * 35f)
            val isMajor = normAngle % 45 == 0
            drawLine(
                color = if (isMajor) hudColor else hudColor.copy(alpha = 0.4f),
                start = Offset(tickX, tapeY),
                end = Offset(tickX, if (isMajor) tapeY - 8f else tapeY - 4f),
                strokeWidth = if (isMajor) 2f else 1f
            )
        }

        // 3. Artificial Horizon Pitch Ladder (Rotating with Roll and translating with Pitch)
        rotate(degrees = -roll, pivot = Offset(cx, cy)) {
            val pitchOffset = (pitch * 4f).coerceIn(-120f, 120f)
            val horizonY = cy + pitchOffset

            // Center Horizon Line
            drawLine(
                color = hudColor.copy(alpha = 0.8f),
                start = Offset(cx - 80f, horizonY),
                end = Offset(cx - 20f, horizonY),
                strokeWidth = 2f
            )
            drawLine(
                color = hudColor.copy(alpha = 0.8f),
                start = Offset(cx + 20f, horizonY),
                end = Offset(cx + 80f, horizonY),
                strokeWidth = 2f
            )

            // Pitch +10 Ladder rung
            val p10Y = horizonY - 45f
            drawLine(
                color = hudColor.copy(alpha = 0.6f),
                start = Offset(cx - 45f, p10Y),
                end = Offset(cx - 15f, p10Y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = hudColor.copy(alpha = 0.6f),
                start = Offset(cx + 15f, p10Y),
                end = Offset(cx + 45f, p10Y),
                strokeWidth = 1.5f
            )
            // Downward pip on +10 rung
            drawLine(color = hudColor.copy(alpha = 0.6f), start = Offset(cx - 45f, p10Y), end = Offset(cx - 45f, p10Y + 6f), strokeWidth = 1.5f)
            drawLine(color = hudColor.copy(alpha = 0.6f), start = Offset(cx + 45f, p10Y), end = Offset(cx + 45f, p10Y + 6f), strokeWidth = 1.5f)

            // Pitch -10 Ladder rung (Dashed)
            val m10Y = horizonY + 45f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            drawLine(
                color = hudColor.copy(alpha = 0.6f),
                start = Offset(cx - 45f, m10Y),
                end = Offset(cx - 15f, m10Y),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )
            drawLine(
                color = hudColor.copy(alpha = 0.6f),
                start = Offset(cx + 15f, m10Y),
                end = Offset(cx + 45f, m10Y),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )
            // Upward pip on -10 rung
            drawLine(color = hudColor.copy(alpha = 0.6f), start = Offset(cx - 45f, m10Y), end = Offset(cx - 45f, m10Y - 6f), strokeWidth = 1.5f)
            drawLine(color = hudColor.copy(alpha = 0.6f), start = Offset(cx + 45f, m10Y), end = Offset(cx + 45f, m10Y - 6f), strokeWidth = 1.5f)
        }

        // 4. Stadiametric Mil-Dot Reticle (Center Optics)
        val reticleRadius = 40f * reticlePulse
        drawCircle(
            color = hudColor.copy(alpha = 0.4f),
            radius = reticleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )

        // Center Crosshair pips
        drawLine(color = hudColor, start = Offset(cx - 12f, cy), end = Offset(cx - 3f, cy), strokeWidth = 2f)
        drawLine(color = hudColor, start = Offset(cx + 3f, cy), end = Offset(cx + 12f, cy), strokeWidth = 2f)
        drawLine(color = hudColor, start = Offset(cx, cy - 12f), end = Offset(cx, cy - 3f), strokeWidth = 2f)
        drawLine(color = hudColor, start = Offset(cx, cy + 3f), end = Offset(cx, cy + 12f), strokeWidth = 2f)

        // Mil-Dots on Horizontal Axis
        val milSpacing = 16f
        for (m in 1..4) {
            drawCircle(color = hudColor, radius = 1.5f, center = Offset(cx + m * milSpacing, cy))
            drawCircle(color = hudColor, radius = 1.5f, center = Offset(cx - m * milSpacing, cy))
            drawCircle(color = hudColor, radius = 1.5f, center = Offset(cx, cy + m * milSpacing))
            drawCircle(color = hudColor, radius = 1.5f, center = Offset(cx, cy - m * milSpacing))
        }

        // 5. Target Acquisition Bracket (If Locked)
        if (isLocked) {
            val boxSize = 55f
            val tgtX = cx + 8f
            val tgtY = cy - 12f
            val color = CrimsonDanger

            // 4 Corner Brackets
            val cornerLen = 14f
            // Top Left
            drawLine(color = color, start = Offset(tgtX - boxSize, tgtY - boxSize), end = Offset(tgtX - boxSize + cornerLen, tgtY - boxSize), strokeWidth = 2.5f)
            drawLine(color = color, start = Offset(tgtX - boxSize, tgtY - boxSize), end = Offset(tgtX - boxSize, tgtY - boxSize + cornerLen), strokeWidth = 2.5f)
            // Top Right
            drawLine(color = color, start = Offset(tgtX + boxSize, tgtY - boxSize), end = Offset(tgtX + boxSize - cornerLen, tgtY - boxSize), strokeWidth = 2.5f)
            drawLine(color = color, start = Offset(tgtX + boxSize, tgtY - boxSize), end = Offset(tgtX + boxSize, tgtY - boxSize + cornerLen), strokeWidth = 2.5f)
            // Bottom Left
            drawLine(color = color, start = Offset(tgtX - boxSize, tgtY + boxSize), end = Offset(tgtX - boxSize + cornerLen, tgtY + boxSize), strokeWidth = 2.5f)
            drawLine(color = color, start = Offset(tgtX - boxSize, tgtY + boxSize), end = Offset(tgtX - boxSize, tgtY + boxSize - cornerLen), strokeWidth = 2.5f)
            // Bottom Right
            drawLine(color = color, start = Offset(tgtX + boxSize, tgtY + boxSize), end = Offset(tgtX + boxSize - cornerLen, tgtY + boxSize), strokeWidth = 2.5f)
            drawLine(color = color, start = Offset(tgtX + boxSize, tgtY + boxSize), end = Offset(tgtX + boxSize, tgtY + boxSize - cornerLen), strokeWidth = 2.5f)
        }
    }
}
