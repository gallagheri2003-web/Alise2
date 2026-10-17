package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hardware.GeoFenceStatus
import com.example.hardware.LivePhoneLocationState
import com.example.hardware.PhoneLocationTracker
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisBorderActive
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Realistic Live Phone Location Tracking HUD Card.
 * Uses high-transparency liquid glass with radial gradient refractive sweep
 * and live Android GPS radar visualization.
 */
@Composable
fun LivePhoneTrackerHudCard(
    locationState: LivePhoneLocationState,
    tracker: PhoneLocationTracker?,
    modifier: Modifier = Modifier,
    onRequestLocationPermission: () -> Unit = {}
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepTransition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                cornerRadius = 20.dp,
                cyanAlpha = 0.30f,
                blueAlpha = 0.40f,
                showSpecularSheen = true
            )
            .padding(16.dp)
            .testTag("live_phone_tracker_card")
    ) {
        // 1. Header Title & Live Status Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(JarvisCyanBright.copy(alpha = 0.4f), JarvisBlueCore.copy(alpha = 0.1f))
                            )
                        )
                        .border(1.dp, JarvisCyanBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Live Phone Tracking",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "LIVE PHONE TRACKING MATRIX",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = JarvisCyanBright,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (locationState.isRealGpsActive) "REAL GPS SATELLITE LOCK • LIVE TELEMETRY" else "HIGH-PRECISION SATELLITE RADAR • ACTIVE",
                        style = Typography.labelSmall,
                        color = if (locationState.isRealGpsActive) JarvisCyan else JarvisArcGold
                    )
                }
            }

            // Geo-Fence Badge
            val fenceColor = when (locationState.geoFenceStatus) {
                GeoFenceStatus.INSIDE_SAFE_ZONE -> JarvisCyan
                GeoFenceStatus.PERIMETER_WARNING -> JarvisArcGold
                GeoFenceStatus.BREACH_ALERT -> JarvisAlertRed
            }
            val fenceText = when (locationState.geoFenceStatus) {
                GeoFenceStatus.INSIDE_SAFE_ZONE -> "SAFE PERIMETER"
                GeoFenceStatus.PERIMETER_WARNING -> "PERIMETER WARN"
                GeoFenceStatus.BREACH_ALERT -> "BREACH ALERT"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(fenceColor.copy(alpha = 0.18f))
                    .border(1.dp, fenceColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = fenceText,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = fenceColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Interactive Holographic Radar Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            JarvisCyanBright.copy(alpha = 0.15f),
                            JarvisBlueCore.copy(alpha = 0.10f),
                            JarvisSurfaceDark.copy(alpha = 0.85f)
                        ),
                        center = Offset.Unspecified,
                        radius = 400f
                    )
                )
                .border(1.dp, JarvisBorderGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = kotlin.math.min(cx, cy) - 16.dp.toPx()

                // Radial distance rings
                for (i in 1..3) {
                    val r = radius * (i / 3f)
                    drawCircle(
                        color = JarvisCyan.copy(alpha = 0.25f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Crosshairs
                drawLine(
                    color = JarvisCyan.copy(alpha = 0.35f),
                    start = Offset(cx - radius, cy),
                    end = Offset(cx + radius, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = JarvisCyan.copy(alpha = 0.35f),
                    start = Offset(cx, cy - radius),
                    end = Offset(cx, cy + radius),
                    strokeWidth = 1.dp.toPx()
                )

                // Safe Zone Perimeter Ring
                drawCircle(
                    color = JarvisCyanBright.copy(alpha = 0.5f),
                    radius = radius * 0.7f * pulseScale,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                )

                // Breadcrumb trail history dots
                locationState.breadcrumbHistory.takeLast(8).forEachIndexed { idx, point ->
                    val alpha = (idx + 1) / 9f * 0.7f
                    val angleOffset = (idx * 35) * Math.PI / 180.0
                    val bx = cx + (radius * 0.45f * sin(angleOffset)).toFloat()
                    val by = cy - (radius * 0.45f * cos(angleOffset)).toFloat()
                    drawCircle(
                        color = JarvisCyanBright.copy(alpha = alpha),
                        radius = 3.dp.toPx(),
                        center = Offset(bx, by)
                    )
                }

                // Rotating Radar Beam
                rotate(degrees = sweepAngle, pivot = Offset(cx, cy)) {
                    val path = Path().apply {
                        moveTo(cx, cy)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(cx - radius, cy - radius, cx + radius, cy + radius),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 45f,
                            forceMoveTo = false
                        )
                        lineTo(cx, cy)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                JarvisCyanBright.copy(alpha = 0.35f),
                                JarvisCyan.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = radius
                        )
                    )
                    drawLine(
                        color = JarvisCyanBright,
                        start = Offset(cx, cy),
                        end = Offset(cx, cy - radius),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Center Target Pin - Samsung Galaxy S25
                rotate(degrees = locationState.bearingDegrees, pivot = Offset(cx, cy)) {
                    // Bearing Direction Triangle
                    val triPath = Path().apply {
                        moveTo(cx, cy - 18.dp.toPx())
                        lineTo(cx - 7.dp.toPx(), cy + 6.dp.toPx())
                        lineTo(cx + 7.dp.toPx(), cy + 6.dp.toPx())
                        close()
                    }
                    drawPath(triPath, color = JarvisCyanBright)
                }

                // Glowing Center Target Reticle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(JarvisCyanBright, JarvisBlueCore.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = 16.dp.toPx()
                    ),
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }

            // Overlay Radar Metrics Labels
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "ACCURACY: ±${String.format(Locale.ROOT, "%.1f", locationState.accuracyMeters)}m",
                    style = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = JarvisCyanBright
                )
                Text(
                    text = "HEADING: ${String.format(Locale.ROOT, "%.0f", locationState.bearingDegrees)}° NE",
                    style = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = JarvisTextSecondary
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "SATS: ${locationState.satelliteCount} LOCKED",
                    style = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = JarvisCyanBright
                )
                Text(
                    text = "DIST: ${String.format(Locale.ROOT, "%.1f", locationState.homeDistanceMeters)}m",
                    style = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = JarvisArcGold
                )
            }

            // Bottom Lat/Lng Quick Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisSurfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, JarvisBorderActive.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${String.format(Locale.ROOT, "%.6f", locationState.latitude)}° N,  ${String.format(Locale.ROOT, "%.6f", locationState.longitude)}° W",
                    style = Typography.labelMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                    color = JarvisCyanBright
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Detailed Telemetry Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Box 1: Altitude & Speed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisGlassPanel)
                    .border(1.dp, JarvisBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "ALTITUDE / KINEMATICS",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(Locale.ROOT, "%.1f", locationState.altitudeMeters)}m (${String.format(Locale.ROOT, "%.0f", locationState.altitudeMeters * 3.28084)} ft)",
                        style = Typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "SPEED: ${String.format(Locale.ROOT, "%.1f", locationState.speedMph)} mph",
                        style = Typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = JarvisCyan
                    )
                }
            }

            // Box 2: Network / Cell Tower
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisGlassPanel)
                    .border(1.dp, JarvisBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "CELL & NETWORK TOWER",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = locationState.wifiBssid,
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = JarvisTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = locationState.cellTowerInfo,
                        style = Typography.labelSmall,
                        color = JarvisTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Tactical Control Dock (Share, Alarm, Flashlight, Maps, Safe Zone)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Open Maps
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisCyanBright.copy(alpha = 0.18f))
                    .border(1.dp, JarvisCyanBright, RoundedCornerShape(10.dp))
                    .clickable {
                        tracker?.openGoogleMapsLocation()
                        Toast.makeText(context, "Opening Live Phone Location on Maps...", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Maps",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MAPS",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = JarvisCyanBright
                    )
                }
            }

            // Sonar Acoustic Alarm
            val sonarBg = if (locationState.isSonarAlarmRinging) JarvisAlertRed else JarvisGlassPanel
            val sonarTint = if (locationState.isSonarAlarmRinging) Color.White else JarvisAlertRed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(sonarBg)
                    .border(1.dp, JarvisAlertRed, RoundedCornerShape(10.dp))
                    .clickable {
                        tracker?.triggerAcousticSonarAlarm(!locationState.isSonarAlarmRinging)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Sonar Alarm",
                        tint = sonarTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (locationState.isSonarAlarmRinging) "STOP ALARM" else "ALARM",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = sonarTint
                    )
                }
            }

            // Flash Beacon
            val flashBg = if (locationState.isFlashlightBeaconOn) JarvisArcGold else JarvisGlassPanel
            val flashTint = if (locationState.isFlashlightBeaconOn) Color.Black else JarvisArcGold
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(flashBg)
                    .border(1.dp, JarvisArcGold, RoundedCornerShape(10.dp))
                    .clickable {
                        tracker?.triggerFlashlightBeacon(!locationState.isFlashlightBeaconOn)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flash Beacon",
                        tint = flashTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (locationState.isFlashlightBeaconOn) "STROBING" else "BEACON",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = flashTint
                    )
                }
            }

            // Share Matrix
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisGlassPanel)
                    .border(1.dp, JarvisBorderActive, RoundedCornerShape(10.dp))
                    .clickable {
                        val shareUrl = tracker?.getGoogleMapsShareUrl() ?: "https://maps.google.com/?q=${locationState.latitude},${locationState.longitude}"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("JARVIS Live Phone GPS", shareUrl)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied Live Phone GPS Link!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = JarvisTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHARE",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = JarvisTextPrimary
                    )
                }
            }
        }

        // 5. GPS Permission Prompt banner if missing real lock
        if (!locationState.isRealGpsActive) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisArcGold.copy(alpha = 0.12f))
                    .border(1.dp, JarvisArcGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { onRequestLocationPermission() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Permission",
                        tint = JarvisArcGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap to grant fine Android GPS permission for hardware lock",
                        style = Typography.labelSmall,
                        color = JarvisArcGold
                    )
                }
                Text(
                    text = "GRANT",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = JarvisArcGold
                )
            }
        }
    }
}
