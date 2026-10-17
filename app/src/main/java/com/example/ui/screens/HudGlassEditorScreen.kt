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
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
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
 * Dedicated HUD & Liquid Glass Studio / Editor Screen.
 * Provides intuitive, live-updating customization for:
 * - Disabling/Enabling blur (0dp crystal clear mode)
 * - Picture frame encasing thickness & corner radius
 * - Liquid glass opacity & glass translucency
 * - Alienware & Arc Reactor color palettes
 * - Sheen, glow, and holographic scanline toggles
 * - HUD layout density presets
 */
@Composable
fun HudGlassEditorScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    val currentConfig by viewModel.liquidGlassConfig.collectAsState()

    var enableBlur by remember(currentConfig) { mutableStateOf(currentConfig.enableBlur) }
    var blurRadius by remember(currentConfig) { mutableFloatStateOf(currentConfig.blurRadiusDp) }
    var borderWidth by remember(currentConfig) { mutableFloatStateOf(currentConfig.frameBorderWidthDp) }
    var cornerRadius by remember(currentConfig) { mutableFloatStateOf(currentConfig.frameCornerRadiusDp) }
    var glassOpacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.glassTransparencyAlpha) }
    var scanlineIntensity by remember(currentConfig) { mutableFloatStateOf(currentConfig.scanlineIntensity) }
    var specularOpacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.specularSheenOpacity) }
    var pulseSpeed by remember(currentConfig) { mutableFloatStateOf(currentConfig.holographicPulseSpeed) }
    
    var enable120Hz by remember(currentConfig) { mutableStateOf(currentConfig.enable120Hz) }
    var enableScanlines by remember(currentConfig) { mutableStateOf(currentConfig.enableScanlines) }
    var enableHoloGrid by remember(currentConfig) { mutableStateOf(currentConfig.enableHoloGrid) }
    var hudDensityMode by remember(currentConfig) { mutableStateOf(currentConfig.hudDensityMode) }
    var themePalette by remember(currentConfig) { mutableStateOf(currentConfig.themePalette) }

    val scrollState = rememberScrollState()

    val activeAccentColor = when (themePalette) {
        "ICE_WHITE" -> Color.White
        "ARC_GOLD" -> JarvisArcGold
        "PLASMA_RED" -> JarvisAlertRed
        "CYBER_EMERALD" -> Color(0xFF00FF9D)
        "QUANTUM_PURPLE" -> Color(0xFFBD00FF)
        else -> JarvisCyanBright
    }

    Scaffold(
        containerColor = JarvisVoid,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(JarvisGlassPanel, JarvisVoid)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.5f), activeAccentColor)
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
                                .testTag("btn_back_from_hud_editor")
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = activeAccentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "HUD & GLASS STUDIO",
                                style = Typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Picture Frame Encasing & Glass Optics Editor",
                                style = Typography.labelSmall,
                                color = JarvisCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeAccentColor.copy(alpha = 0.2f))
                            .border(1.dp, activeAccentColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (!enableBlur || blurRadius == 0f) "CRYSTAL CLEAR" else "${blurRadius.toInt()}dp BLUR",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = activeAccentColor,
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
            // 1. Live Interactive HUD Glass Picture Frame Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                JarvisGlassPanel.copy(alpha = glassOpacity),
                                JarvisSurfaceDark.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        width = borderWidth.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                activeAccentColor,
                                JarvisBlueCore.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = activeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE HUD GLASS PREVIEW",
                                style = Typography.labelMedium,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeAccentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PICTURE FRAME ACTIVE",
                                style = Typography.labelSmall,
                                fontSize = 8.5.sp,
                                color = activeAccentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Everything inside the liquid glass is 100% crisp, sharp, and readable — like a photo inside a picture frame.",
                        style = Typography.bodySmall,
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mini Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FRAME BORDER", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 9.sp)
                            Text("${String.format("%.1f", borderWidth)} dp", style = Typography.bodyMedium, color = activeAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("CORNER RADIUS", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 9.sp)
                            Text("${cornerRadius.toInt()} dp", style = Typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("TRANSPARENCY", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 9.sp)
                            Text("${(glassOpacity * 100).toInt()}%", style = Typography.bodyMedium, color = activeAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("BLUR STATUS", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 9.sp)
                            Text(if (enableBlur && blurRadius > 0f) "${blurRadius.toInt()}dp" else "OFF", style = Typography.bodyMedium, color = if (enableBlur) JarvisArcGold else Color(0xFF00FF9D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Blur Control Section (Ability to turn off blur completely!)
            LiquidGlassContainer(
                title = "1. BLUR & CRYSTAL CLEAR CONTROLS",
                statusTag = if (!enableBlur || blurRadius == 0f) "BLUR DISABLED (CRISP)" else "BLUR ENABLED",
                accentColor = if (enableBlur) JarvisArcGold else Color(0xFF00FF9D)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (enableBlur) Icons.Default.BlurOn else Icons.Default.BlurOff,
                                contentDescription = null,
                                tint = if (enableBlur) JarvisArcGold else Color(0xFF00FF9D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Enable Background Blur Effect",
                                    style = Typography.bodyMedium,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Turn OFF for 100% crisp crystal clear UI text",
                                    style = Typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = JarvisTextMuted
                                )
                            }
                        }

                        LiquidGlassToggle(
                            checked = enableBlur,
                            onCheckedChange = {
                                enableBlur = it
                                if (!it) blurRadius = 0f
                            },
                            testTag = "toggle_enable_blur"
                        )
                    }

                    if (enableBlur) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Blur Radius Strength", style = Typography.labelSmall, color = JarvisTextMuted)
                                Text("${blurRadius.toInt()} dp", style = Typography.labelSmall, color = JarvisArcGold, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LiquidGlassSlider(
                                value = blurRadius,
                                onValueChange = { blurRadius = it },
                                valueRange = 0f..25f,
                                accentColor = JarvisArcGold,
                                testTag = "slider_blur_radius"
                            )
                        }
                    }
                }
            }

            // 3. Picture Frame Encasing & Border Customization
            LiquidGlassContainer(
                title = "2. PICTURE FRAME ENCASING & BORDERS",
                statusTag = "${String.format("%.1f", borderWidth)}dp FRAME",
                accentColor = activeAccentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Border Thickness Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Frame Border Stroke Thickness", style = Typography.labelSmall, color = JarvisTextMuted)
                            Text("${String.format("%.1f", borderWidth)} dp", style = Typography.labelSmall, color = activeAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = borderWidth,
                            onValueChange = { borderWidth = it },
                            valueRange = 0.5f..5.0f,
                            accentColor = activeAccentColor,
                            testTag = "slider_frame_border_width"
                        )
                    }

                    // Corner Radius Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Frame Corner Roundness", style = Typography.labelSmall, color = JarvisTextMuted)
                            Text("${cornerRadius.toInt()} dp", style = Typography.labelSmall, color = activeAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = cornerRadius,
                            onValueChange = { cornerRadius = it },
                            valueRange = 4f..32f,
                            accentColor = activeAccentColor,
                            testTag = "slider_frame_corner_radius"
                        )
                    }

                    // Glass Transparency Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Glass Backing Transparency", style = Typography.labelSmall, color = JarvisTextMuted)
                            Text("${(glassOpacity * 100).toInt()}%", style = Typography.labelSmall, color = activeAccentColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LiquidGlassSlider(
                            value = glassOpacity,
                            onValueChange = { glassOpacity = it },
                            valueRange = 0.05f..0.85f,
                            accentColor = activeAccentColor,
                            testTag = "slider_glass_transparency"
                        )
                    }
                }
            }

            // 4. Color Palette Selection
            LiquidGlassContainer(
                title = "3. ALIENWARE & HUD COLOR PALETTE",
                statusTag = themePalette,
                accentColor = activeAccentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose the primary coolant & glow signature for the liquid glass frame:",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted,
                        fontSize = 10.5.sp
                    )

                    val colorOptions = listOf(
                        Triple("ALIENWARE_CYAN", "Liquid Cyan", JarvisCyanBright),
                        Triple("ICE_WHITE", "Pure Ice White", Color.White),
                        Triple("ARC_GOLD", "Arc Gold", JarvisArcGold),
                        Triple("PLASMA_RED", "Plasma Red", JarvisAlertRed),
                        Triple("CYBER_EMERALD", "Cyber Emerald", Color(0xFF00FF9D)),
                        Triple("QUANTUM_PURPLE", "Quantum Purple", Color(0xFFBD00FF))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.take(3).forEach { (id, name, color) ->
                            val isSelected = themePalette == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else JarvisSurfaceDark)
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else JarvisGlassPanel, RoundedCornerShape(10.dp))
                                    .clickable { themePalette = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = Typography.labelSmall,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) Color.White else JarvisTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.drop(3).forEach { (id, name, color) ->
                            val isSelected = themePalette == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else JarvisSurfaceDark)
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else JarvisGlassPanel, RoundedCornerShape(10.dp))
                                    .clickable { themePalette = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = Typography.labelSmall,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) Color.White else JarvisTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. HUD Layout Density Presets
            LiquidGlassContainer(
                title = "4. HUD DENSITY PRESETS",
                statusTag = hudDensityMode,
                accentColor = activeAccentColor
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        Triple("PICTURE_FRAME", "Picture Frame Enclosure", "Framed borders around every HUD card with spacious margins."),
                        Triple("STANDARD", "S25 Edge-to-Edge Glass", "Standard full screen liquid glass layout for S25."),
                        Triple("COMPACT", "Tactical Compact HUD", "High density compact view maximizing visible metrics.")
                    )

                    presets.forEach { (id, name, desc) ->
                        val isSelected = hudDensityMode == id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeAccentColor.copy(alpha = 0.18f) else JarvisSurfaceDark)
                                .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeAccentColor else JarvisGlassPanel, RoundedCornerShape(10.dp))
                                .clickable { hudDensityMode = id }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        style = Typography.bodyMedium,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) activeAccentColor else Color.White
                                    )
                                    Text(
                                        text = desc,
                                        style = Typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = JarvisTextMuted
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = activeAccentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Action Buttons: Apply & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = {
                        viewModel.updateLiquidGlassConfig(
                            LiquidGlassConfig(
                                blurRadiusDp = blurRadius,
                                enableBlur = enableBlur,
                                frameBorderWidthDp = borderWidth,
                                frameCornerRadiusDp = cornerRadius,
                                glassTransparencyAlpha = glassOpacity,
                                scanlineIntensity = scanlineIntensity,
                                specularSheenOpacity = specularOpacity,
                                holographicPulseSpeed = pulseSpeed,
                                enable120Hz = enable120Hz,
                                enableScanlines = enableScanlines,
                                enableHoloGrid = enableHoloGrid,
                                hudDensityMode = hudDensityMode,
                                themePalette = themePalette
                            )
                        )
                    },
                    modifier = Modifier.weight(1.2f),
                    accentColor = activeAccentColor,
                    testTag = "btn_save_hud_glass_config"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = activeAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "APPLY TO HUD",
                            style = Typography.labelSmall,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccentColor
                        )
                    }
                }

                LiquidGlassButton(
                    onClick = {
                        viewModel.resetLiquidGlassConfig()
                        enableBlur = false
                        blurRadius = 0f
                        borderWidth = 1.5f
                        cornerRadius = 20f
                        glassOpacity = 0.25f
                        scanlineIntensity = 0.15f
                        specularOpacity = 0.35f
                        pulseSpeed = 1.0f
                        enable120Hz = true
                        enableScanlines = false
                        enableHoloGrid = true
                        hudDensityMode = "PICTURE_FRAME"
                        themePalette = "ALIENWARE_CYAN"
                    },
                    modifier = Modifier.weight(0.8f),
                    accentColor = JarvisBlueCore,
                    testTag = "btn_reset_hud_glass_config"
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
                            text = "RESET",
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
