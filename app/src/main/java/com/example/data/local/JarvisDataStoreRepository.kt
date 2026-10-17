package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.jarvisDataStore: DataStore<Preferences> by preferencesDataStore(name = "jarvis_liquid_glass_prefs")

data class LiquidGlassConfig(
    val blurRadiusDp: Float = 0.0f,
    val enableBlur: Boolean = false,
    val scanlineIntensity: Float = 0.15f,
    val radialGradientAlpha: Float = 0.25f,
    val specularSheenOpacity: Float = 0.35f,
    val holographicPulseSpeed: Float = 1.0f,
    val frameBorderWidthDp: Float = 1.5f,
    val frameCornerRadiusDp: Float = 20.0f,
    val glassTransparencyAlpha: Float = 0.25f,
    val enable120Hz: Boolean = true,
    val enableScanlines: Boolean = false,
    val enableHoloGrid: Boolean = true,
    val hudDensityMode: String = "PICTURE_FRAME",
    val themePalette: String = "ALIENWARE_CYAN"
)

class JarvisDataStoreRepository(private val context: Context) {

    private object Keys {
        val BLUR_RADIUS = floatPreferencesKey("liquid_glass_blur_radius")
        val ENABLE_BLUR = booleanPreferencesKey("liquid_glass_enable_blur")
        val SCANLINE_INTENSITY = floatPreferencesKey("liquid_glass_scanline_intensity")
        val RADIAL_ALPHA = floatPreferencesKey("liquid_glass_radial_alpha")
        val SPECULAR_SHEEN = floatPreferencesKey("liquid_glass_specular_sheen")
        val PULSE_SPEED = floatPreferencesKey("liquid_glass_pulse_speed")
        val FRAME_BORDER_WIDTH = floatPreferencesKey("liquid_glass_frame_border_width")
        val FRAME_CORNER_RADIUS = floatPreferencesKey("liquid_glass_frame_corner_radius")
        val GLASS_TRANSPARENCY = floatPreferencesKey("liquid_glass_transparency")
        val ENABLE_120HZ = booleanPreferencesKey("liquid_glass_enable_120hz")
        val ENABLE_SCANLINES = booleanPreferencesKey("liquid_glass_enable_scanlines")
        val ENABLE_HOLO_GRID = booleanPreferencesKey("liquid_glass_enable_holo_grid")
        val HUD_DENSITY_MODE = stringPreferencesKey("liquid_glass_hud_density_mode")
        val THEME_PALETTE = stringPreferencesKey("liquid_glass_theme_palette")
        val PROTOCOL_DEFINITIONS_BACKUP = stringPreferencesKey("protocol_definitions_backup")
        val BIOMETRIC_CORE_LOCK_ENABLED = booleanPreferencesKey("jarvis_biometric_core_lock_enabled")
        val BIOMETRIC_AUTO_LOCK_BACKGROUND = booleanPreferencesKey("jarvis_biometric_auto_lock_background")
    }

    val liquidGlassConfig: Flow<LiquidGlassConfig> = context.jarvisDataStore.data.map { prefs ->
        LiquidGlassConfig(
            blurRadiusDp = prefs[Keys.BLUR_RADIUS] ?: 0.0f,
            enableBlur = prefs[Keys.ENABLE_BLUR] ?: false,
            scanlineIntensity = prefs[Keys.SCANLINE_INTENSITY] ?: 0.15f,
            radialGradientAlpha = prefs[Keys.RADIAL_ALPHA] ?: 0.25f,
            specularSheenOpacity = prefs[Keys.SPECULAR_SHEEN] ?: 0.35f,
            holographicPulseSpeed = prefs[Keys.PULSE_SPEED] ?: 1.0f,
            frameBorderWidthDp = prefs[Keys.FRAME_BORDER_WIDTH] ?: 1.5f,
            frameCornerRadiusDp = prefs[Keys.FRAME_CORNER_RADIUS] ?: 20.0f,
            glassTransparencyAlpha = prefs[Keys.GLASS_TRANSPARENCY] ?: 0.25f,
            enable120Hz = prefs[Keys.ENABLE_120HZ] ?: true,
            enableScanlines = prefs[Keys.ENABLE_SCANLINES] ?: false,
            enableHoloGrid = prefs[Keys.ENABLE_HOLO_GRID] ?: true,
            hudDensityMode = prefs[Keys.HUD_DENSITY_MODE] ?: "PICTURE_FRAME",
            themePalette = prefs[Keys.THEME_PALETTE] ?: "ALIENWARE_CYAN"
        )
    }

    suspend fun updateConfig(config: LiquidGlassConfig) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.BLUR_RADIUS] = config.blurRadiusDp
            prefs[Keys.ENABLE_BLUR] = config.enableBlur
            prefs[Keys.SCANLINE_INTENSITY] = config.scanlineIntensity
            prefs[Keys.RADIAL_ALPHA] = config.radialGradientAlpha
            prefs[Keys.SPECULAR_SHEEN] = config.specularSheenOpacity
            prefs[Keys.PULSE_SPEED] = config.holographicPulseSpeed
            prefs[Keys.FRAME_BORDER_WIDTH] = config.frameBorderWidthDp
            prefs[Keys.FRAME_CORNER_RADIUS] = config.frameCornerRadiusDp
            prefs[Keys.GLASS_TRANSPARENCY] = config.glassTransparencyAlpha
            prefs[Keys.ENABLE_120HZ] = config.enable120Hz
            prefs[Keys.ENABLE_SCANLINES] = config.enableScanlines
            prefs[Keys.ENABLE_HOLO_GRID] = config.enableHoloGrid
            prefs[Keys.HUD_DENSITY_MODE] = config.hudDensityMode
            prefs[Keys.THEME_PALETTE] = config.themePalette
        }
    }

    suspend fun resetConfig() {
        context.jarvisDataStore.edit { prefs ->
            prefs.remove(Keys.BLUR_RADIUS)
            prefs.remove(Keys.ENABLE_BLUR)
            prefs.remove(Keys.SCANLINE_INTENSITY)
            prefs.remove(Keys.RADIAL_ALPHA)
            prefs.remove(Keys.SPECULAR_SHEEN)
            prefs.remove(Keys.PULSE_SPEED)
            prefs.remove(Keys.FRAME_BORDER_WIDTH)
            prefs.remove(Keys.FRAME_CORNER_RADIUS)
            prefs.remove(Keys.GLASS_TRANSPARENCY)
            prefs.remove(Keys.ENABLE_120HZ)
            prefs.remove(Keys.ENABLE_SCANLINES)
            prefs.remove(Keys.ENABLE_HOLO_GRID)
            prefs.remove(Keys.HUD_DENSITY_MODE)
            prefs.remove(Keys.THEME_PALETTE)
        }
    }

    val protocolBackupJson: Flow<String?> = context.jarvisDataStore.data.map { prefs ->
        prefs[Keys.PROTOCOL_DEFINITIONS_BACKUP]
    }

    suspend fun saveProtocolBackup(json: String) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.PROTOCOL_DEFINITIONS_BACKUP] = json
        }
    }

    val isBiometricCoreLockEnabled: Flow<Boolean> = context.jarvisDataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_CORE_LOCK_ENABLED] ?: true
    }

    val isBiometricAutoLockBackground: Flow<Boolean> = context.jarvisDataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_AUTO_LOCK_BACKGROUND] ?: true
    }

    suspend fun setBiometricCoreLockEnabled(enabled: Boolean) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_CORE_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setBiometricAutoLockBackground(enabled: Boolean) {
        context.jarvisDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_AUTO_LOCK_BACKGROUND] = enabled
        }
    }
}
