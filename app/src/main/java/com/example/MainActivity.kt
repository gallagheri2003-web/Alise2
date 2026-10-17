package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.components.CompanionBackgroundCanvas
import com.example.ui.components.IosCompanionBottomBar
import com.example.ui.components.JarvisBiometricSecurityOverlay
import com.example.ui.components.iosGlassSurface
import com.example.ui.screens.AliseLiveConsoleScreen
import com.example.ui.screens.AutoCoderScreen
import com.example.ui.screens.BlackoutVaultScreen
import com.example.ui.screens.CompanionHomeScreen
import com.example.ui.screens.CompanionRoutinesScreen
import com.example.ui.screens.EducationCyberScreen
import com.example.ui.screens.LiquidGlassSettingsScreen
import com.example.ui.screens.QuantumProtocolMatrixScreen
import com.example.ui.screens.SelfAuditScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.theme.IosAccentAmber
import com.example.ui.theme.IosAccentIndigo
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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JarvisViewModel

enum class JarvisDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    CHAT("chat", "Companion", Icons.Default.AutoAwesome, "nav_chat"),
    DEFENSE("defense", "Care", Icons.Default.Spa, "nav_defense"),
    VOICE_STUDIO("voice", "Voice", Icons.Default.GraphicEq, "nav_voice"),
    VAULT("vault", "Sanctuary", Icons.Default.Shield, "nav_vault"),
    LIVE_CONSOLE("liveconsole", "Live", Icons.Default.GraphicEq, "nav_liveconsole"),
    AUTO_CODER("autocoder", "Coder", Icons.Default.Code, "nav_autocoder"),
    OPTICS("optics", "Optics", Icons.Default.CameraAlt, "nav_optics"),
    TERMINAL("terminal", "Terminal", Icons.Default.Terminal, "nav_terminal"),
    DIAGNOSTICS("diagnostics", "Health", Icons.Default.Dashboard, "nav_diagnostics"),
    SELF_AUDIT("selfaudit", "Audit", Icons.Default.Security, "nav_selfaudit"),
    INTEL_HUB("intel", "Intel", Icons.Default.Description, "nav_intel"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : FragmentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 120 FPS high refresh rate display optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                @Suppress("DEPRECATION")
                val modes = display?.supportedModes
                val maxRefreshMode = modes?.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null && maxRefreshMode.refreshRate >= 119f) {
                    window.attributes.preferredDisplayModeId = maxRefreshMode.modeId
                }
                window.attributes.preferredRefreshRate = 120f
            } catch (_: Exception) {}
        }

        setContent {
            MyApplicationTheme {
                JarvisAppRoot(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isBiometricAutoLockBackground.value && viewModel.isBiometricCoreLockEnabled.value) {
            viewModel.lockCoreInterface("BACKGROUND_LOCK")
        }
    }
}

@Composable
fun JarvisAppRoot(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(JarvisDestination.CHAT) }
    var showLiquidGlassSettings by remember { mutableStateOf(false) }

    val telemetry by viewModel.telemetry.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val voiceListenerState by viewModel.voiceListenerState.collectAsState()

    // Runtime permissions for Audio, Camera, Location, and Notifications
    val permissions = remember {
        mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.phoneLocationTracker.startTracking()
    }

    LaunchedEffect(Unit) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionsLauncher.launch(missing.toTypedArray())
        }
    }

    CompanionBackgroundCanvas {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                // ============================================================
                // iOS COMPANION TOP BAR (Elegance, not complexity)
                // ============================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .iosGlassSurface(cornerRadius = 24.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Companion Identity & Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IosAccentViolet.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "ALICE Companion",
                                    tint = IosAccentViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "ALICE",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IosTextPrimary,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Technology as a Companion",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = IosTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Right: Quick Ambient Companion Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Night Light Toggle Pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isFlashlightOn) IosAccentAmber else IosSurfaceCardElevated)
                                    .clickable {
                                        viewModel.toggleFlashlight()
                                        viewModel.soundSynthesizer.playAcknowledgeTone()
                                    }
                                    .padding(8.dp)
                                    .testTag("header_torch_btn")
                            ) {
                                Icon(
                                    imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                    contentDescription = "Torch",
                                    tint = if (isFlashlightOn) Color.Black else IosTextPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Vision / Camera Shortcut
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(IosSurfaceCardElevated)
                                    .clickable {
                                        currentDestination = if (currentDestination == JarvisDestination.OPTICS) JarvisDestination.CHAT else JarvisDestination.OPTICS
                                        viewModel.soundSynthesizer.playAcknowledgeTone()
                                    }
                                    .padding(8.dp)
                                    .testTag("header_optics_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Optics",
                                    tint = IosAccentSky,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Private Sanctuary Lock
                            val isCoreLocked by viewModel.isCoreInterfaceLocked.collectAsState()
                            val isBiometricEnabled by viewModel.isBiometricCoreLockEnabled.collectAsState()

                            if (isBiometricEnabled) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isCoreLocked) IosAccentRose.copy(alpha = 0.25f) else IosSurfaceCardElevated)
                                        .clickable {
                                            if (isCoreLocked) {
                                                val activity = context as? FragmentActivity
                                                if (activity != null) {
                                                    viewModel.authenticateCoreInterface(activity)
                                                }
                                            } else {
                                                viewModel.lockCoreInterface("OPERATOR_LOCK")
                                            }
                                        }
                                        .padding(8.dp)
                                        .testTag("header_biometric_lock_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Lock",
                                        tint = if (isCoreLocked) IosAccentRose else IosTextPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                val isCoreLocked by viewModel.isCoreInterfaceLocked.collectAsState()
                val isBiometricEnabled by viewModel.isBiometricCoreLockEnabled.collectAsState()

                if (!isCoreLocked || !isBiometricEnabled) {
                    // Primary 4-Tab iOS Companion Navigation Dock
                    val primaryTabs = listOf(
                        JarvisDestination.CHAT,
                        JarvisDestination.DEFENSE,
                        JarvisDestination.VOICE_STUDIO,
                        JarvisDestination.VAULT
                    )

                    IosCompanionBottomBar(
                        destinations = primaryTabs,
                        currentDestination = currentDestination,
                        onDestinationSelected = { dest ->
                            if (currentDestination != dest) {
                                currentDestination = dest
                                viewModel.soundSynthesizer.playAcknowledgeTone()
                            }
                        },
                        titleProvider = { it.title },
                        iconProvider = { it.icon },
                        tagProvider = { it.tag }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isCoreLocked by viewModel.isCoreInterfaceLocked.collectAsState()
                val isBiometricEnabled by viewModel.isBiometricCoreLockEnabled.collectAsState()

                if (isCoreLocked && isBiometricEnabled) {
                    JarvisBiometricSecurityOverlay(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "CompanionNavTransition"
                    ) { target ->
                        when (target) {
                            JarvisDestination.CHAT -> CompanionHomeScreen(
                                viewModel = viewModel,
                                onNavigateToRoutines = { currentDestination = JarvisDestination.DEFENSE },
                                onNavigateToVoice = { currentDestination = JarvisDestination.VOICE_STUDIO },
                                onNavigateToSanctuary = { currentDestination = JarvisDestination.VAULT }
                            )
                            JarvisDestination.DEFENSE -> CompanionRoutinesScreen(
                                viewModel = viewModel
                            )
                            JarvisDestination.VOICE_STUDIO -> VoiceSettingsScreen(
                                viewModel = viewModel
                            )
                            JarvisDestination.VAULT -> BlackoutVaultScreen(
                                onNavigateBack = { currentDestination = JarvisDestination.CHAT }
                            )
                            JarvisDestination.LIVE_CONSOLE -> AliseLiveConsoleScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = JarvisDestination.CHAT }
                            )
                            JarvisDestination.AUTO_CODER -> AutoCoderScreen(viewModel = viewModel)
                            JarvisDestination.OPTICS -> VisionScreen(viewModel = viewModel)
                            JarvisDestination.TERMINAL -> TerminalScreen(viewModel = viewModel)
                            JarvisDestination.DIAGNOSTICS -> TelemetryScreen(
                                viewModel = viewModel,
                                onRequestLocationPermission = {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            )
                            JarvisDestination.SELF_AUDIT -> SelfAuditScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = JarvisDestination.CHAT }
                            )
                            JarvisDestination.INTEL_HUB -> EducationCyberScreen(
                                onNavigateBack = { currentDestination = JarvisDestination.CHAT }
                            )
                            JarvisDestination.SETTINGS -> {
                                if (showLiquidGlassSettings) {
                                    LiquidGlassSettingsScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { showLiquidGlassSettings = false }
                                    )
                                } else {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onNavigateToDirectory = { currentDestination = JarvisDestination.DEFENSE },
                                        onNavigateToLiquidGlassSettings = { showLiquidGlassSettings = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
