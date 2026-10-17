package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LiquidGlassConfig
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassContainer
import com.example.ui.components.LiquidGlassSlider
import com.example.ui.components.LiquidGlassToggle
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel

/**
 * Dedicated Liquid Glass Settings Screen providing sub-settings for:
 * - Glass Blur Radius (0 - 25 dp)
 * - Refractive Cyan Ambient Density (10% - 90%)
 * - Scanline Shader Opacity (0% - 100%)
 * - Specular Sheen Reflection Opacity (0% - 100%)
 * - Holographic Radial Pulse Speed (0.5x - 3.0x)
 * - 120Hz High-Refresh Rate Mode
 * - Scanline Raster Overlay Toggle
 * - Coordinate Mesh Grid Toggle
 */
@Composable
fun LiquidGlassSettingsScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    val currentConfig by viewModel.liquidGlassConfig.collectAsState()

    var blurRadius by remember(currentConfig) { mutableFloatStateOf(currentConfig.blurRadiusDp) }
    var scanlineIntensity by remember(currentConfig) { mutableFloatStateOf(currentConfig.scanlineIntensity) }
    var radialAlpha by remember(currentConfig) { mutableFloatStateOf(currentConfig.radialGradientAlpha) }
    var specularOpacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.specularSheenOpacity) }
    var pulseSpeed by remember(currentConfig) { mutableFloatStateOf(currentConfig.holographicPulseSpeed) }
    var enable120Hz by remember(currentConfig) { mutableStateOf(currentConfig.enable120Hz) }
    var enableScanlines by remember(currentConfig) { mutableStateOf(currentConfig.enableScanlines) }
    var enableHoloGrid by remember(currentConfig) { mutableStateOf(currentConfig.enableHoloGrid) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = JarvisVoid,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                JarvisGlassPanel,
                                JarvisVoid
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.5f), JarvisCyanBright)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .testTag("btn_back_from_liquid_glass_settings")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = JarvisCyanBright
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LIQUID GLASS OPTICS",
                                style = Typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Samsung S25 Ultra Refraction Sub-Settings",
                                style = Typography.labelSmall,
                                color = JarvisCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisCyan.copy(alpha = 0.2f))
                            .border(1.dp, JarvisCyanBright, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "120Hz ACTIVE",
                            style = Typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisCyanBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Optical Glass Preview Component
            LiquidGlassContainer(
                title = "LIVE GLASS OPTICAL PREVIEW",
                statusTag = "${blurRadius.toInt()}dp BLUR • ${(scanlineIntensity * 100).toInt()}% SHADER",
                accentColor = JarvisCyanBright,
                cornerRadius = 18.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    JarvisCyanBright.copy(alpha = 0.4f * radialAlpha),
                                    JarvisBlueCore.copy(alpha = 0.3f * radialAlpha),
                                    JarvisSurfaceDark
                                )
                            )
                        )
                        .blur(blurRadius.dp)
                        .border(1.2.dp, JarvisCyanBright.copy(alpha = specularOpacity), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCyanBright)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIQUID GLASS CORE",
                                style = Typography.labelMedium,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Samsung S25 High-Refresh Shader Simulation",
                            style = Typography.bodySmall,
                            fontSize = 10.sp,
                            color = JarvisCyan
                        )
                    }
                }
            }

            // 2. Sub-Settings Group 1: Glass Blur & Ambient Refraction
            LiquidGlassContainer(
                title = "BLUR & REFRACTION OPTICS",
                statusTag = "HARDWARE SHADER",
                accentColor = JarvisCyan,
                cornerRadius = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Glass Blur Radius Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BlurOn,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Glass Blur Radius",
                                    style = Typography.bodyMedium,
                                    fontSize = 11.5.sp,
                                    color = JarvisTextPrimary
                                )
                            }
                            Text(
                                text = "${blurRadius.toInt()} dp",
                                style = Typography.labelSmall,
                                fontSize = 11.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = blurRadius,
                            onValueChange = { blurRadius = it },
                            valueRange = 0f..25f,
                            testTag = "slider_blur_radius"
                        )
                    }

                    // Refractive Radial Gradient Alpha Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Liquid Cyan Ambient Glow Density",
                                    style = Typography.bodyMedium,
                                    fontSize = 11.5.sp,
                                    color = JarvisTextPrimary
                                )
                            }
                            Text(
                                text = "${(radialAlpha * 100).toInt()}%",
                                style = Typography.labelSmall,
                                fontSize = 11.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = radialAlpha,
                            onValueChange = { radialAlpha = it },
                            valueRange = 0.10f..0.90f,
                            testTag = "slider_radial_alpha"
                        )
                    }
                }
            }

            // 3. Sub-Settings Group 2: Scanlines & Specular Light Sheen
            LiquidGlassContainer(
                title = "SCANLINE & SPECULAR SHADERS",
                statusTag = "S25 AMOLED MATRIX",
                accentColor = JarvisCyanBright,
                cornerRadius = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Scanline Shader Opacity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Scanner,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scanline Raster Intensity",
                                    style = Typography.bodyMedium,
                                    fontSize = 11.5.sp,
                                    color = JarvisTextPrimary
                                )
                            }
                            Text(
                                text = "${(scanlineIntensity * 100).toInt()}%",
                                style = Typography.labelSmall,
                                fontSize = 11.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = scanlineIntensity,
                            onValueChange = { scanlineIntensity = it },
                            valueRange = 0.05f..0.85f,
                            testTag = "slider_scanline_shader"
                        )
                    }

                    // Specular Sheen Reflection
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Specular Light Reflection Opacity",
                                    style = Typography.bodyMedium,
                                    fontSize = 11.5.sp,
                                    color = JarvisTextPrimary
                                )
                            }
                            Text(
                                text = "${(specularOpacity * 100).toInt()}%",
                                style = Typography.labelSmall,
                                fontSize = 11.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = specularOpacity,
                            onValueChange = { specularOpacity = it },
                            valueRange = 0.0f..1.0f,
                            testTag = "slider_specular_sheen"
                        )
                    }

                    // Toggle: Scanline Raster
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Scanline Raster Shader Pass",
                            style = Typography.bodyMedium,
                            fontSize = 11.5.sp,
                            color = JarvisTextPrimary
                        )
                        LiquidGlassToggle(
                            checked = enableScanlines,
                            onCheckedChange = { enableScanlines = it },
                            testTag = "toggle_enable_scanlines"
                        )
                    }
                }
            }

            // 4. Sub-Settings Group 3: Motion & Coordinate Mesh
            LiquidGlassContainer(
                title = "BREATHING MOTION & COORDINATE GRID",
                statusTag = "SMOOTH FPS",
                accentColor = JarvisCyan,
                cornerRadius = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Pulse Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Holographic Breathing Pulse Frequency",
                                    style = Typography.bodyMedium,
                                    fontSize = 11.5.sp,
                                    color = JarvisTextPrimary
                                )
                            }
                            Text(
                                text = String.format("%.1fx", pulseSpeed),
                                style = Typography.labelSmall,
                                fontSize = 11.sp,
                                color = JarvisCyanBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = pulseSpeed,
                            onValueChange = { pulseSpeed = it },
                            valueRange = 0.5f..3.0f,
                            testTag = "slider_pulse_speed"
                        )
                    }

                    // Toggle: 120Hz Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "120Hz Ultra High-Refresh Motion",
                                style = Typography.bodyMedium,
                                fontSize = 11.5.sp,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Synchronizes Compose rendering with S25 120Hz AMOLED",
                                style = Typography.labelSmall,
                                fontSize = 9.5.sp,
                                color = JarvisTextMuted
                            )
                        }
                        LiquidGlassToggle(
                            checked = enable120Hz,
                            onCheckedChange = { enable120Hz = it },
                            testTag = "toggle_120hz_motion"
                        )
                    }

                    // Toggle: Coordinate Mesh
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Grid4x4,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Holographic Mesh Coordinate Grid",
                                style = Typography.bodyMedium,
                                fontSize = 11.5.sp,
                                color = JarvisTextPrimary
                            )
                        }
                        LiquidGlassToggle(
                            checked = enableHoloGrid,
                            onCheckedChange = { enableHoloGrid = it },
                            testTag = "toggle_holo_grid"
                        )
                    }
                }
            }

            // 5. Action Buttons: Save & Recalibrate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = {
                        viewModel.updateLiquidGlassConfig(
                            LiquidGlassConfig(
                                blurRadiusDp = blurRadius,
                                scanlineIntensity = scanlineIntensity,
                                radialGradientAlpha = radialAlpha,
                                specularSheenOpacity = specularOpacity,
                                holographicPulseSpeed = pulseSpeed,
                                enable120Hz = enable120Hz,
                                enableScanlines = enableScanlines,
                                enableHoloGrid = enableHoloGrid
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    accentColor = JarvisCyanBright,
                    testTag = "btn_save_liquid_glass_config"
                ) {
                    Text(
                        text = "SAVE OPTIC CONFIG",
                        style = Typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyanBright
                    )
                }

                LiquidGlassButton(
                    onClick = {
                        viewModel.resetLiquidGlassConfig()
                        blurRadius = 12.0f
                        scanlineIntensity = 0.28f
                        radialAlpha = 0.35f
                        specularOpacity = 0.35f
                        pulseSpeed = 1.0f
                        enable120Hz = true
                        enableScanlines = true
                        enableHoloGrid = true
                    },
                    modifier = Modifier.weight(1f),
                    accentColor = JarvisBlueCore,
                    testTag = "btn_reset_liquid_glass_config"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = JarvisTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESET DEFAULT",
                            style = Typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }
        }
    }
}
