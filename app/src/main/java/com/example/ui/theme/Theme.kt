package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisVoid,
    primaryContainer = JarvisSurfaceCard,
    onPrimaryContainer = JarvisCyanBright,
    secondary = JarvisBlueCore,
    onSecondary = JarvisVoid,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisArcGold,
    onTertiary = JarvisVoid,
    tertiaryContainer = JarvisSurfaceDark,
    onTertiaryContainer = JarvisArcGold,
    background = JarvisVoid,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurfaceDark,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceCard,
    onSurfaceVariant = JarvisTextSecondary,
    error = JarvisAlertRed,
    onError = JarvisVoid,
    outline = JarvisBorderGlow,
    outlineVariant = JarvisBorderActive
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = JarvisVoid.toArgb()
            window.navigationBarColor = JarvisVoid.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

