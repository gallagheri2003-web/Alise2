package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurfaceDark

/**
 * Shortcuts / Quick Controls Widget matching Image 2 mid-left bottom layout.
 */
@Composable
fun JarvisShortcutsWidget(
    isFlashlightOn: Boolean = false,
    onToggleFlashlight: () -> Unit = {},
    onToggleWifi: () -> Unit = {},
    onToggleBluetooth: () -> Unit = {},
    onToggleDnd: () -> Unit = {},
    onToggleAirplane: () -> Unit = {},
    onToggleAutoRotate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisSurfaceDark.copy(alpha = 0.9f),
                        Color(0xFF07121C).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.dp,
                JarvisCyan.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SHORTCUTS",
                color = JarvisCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2x3 Grid of Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShortcutItem(
                    title = "Wi-Fi",
                    icon = Icons.Default.Wifi,
                    isActive = true,
                    onClick = onToggleWifi
                )
                ShortcutItem(
                    title = "Bluetooth",
                    icon = Icons.Default.Bluetooth,
                    isActive = true,
                    onClick = onToggleBluetooth
                )
                ShortcutItem(
                    title = "Do Not Disturb",
                    icon = Icons.Default.DoNotDisturbOn,
                    isActive = false,
                    onClick = onToggleDnd
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShortcutItem(
                    title = "Airplane Mode",
                    icon = Icons.Default.AirplanemodeActive,
                    isActive = false,
                    onClick = onToggleAirplane
                )
                ShortcutItem(
                    title = "Flashlight",
                    icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    isActive = isFlashlightOn,
                    onClick = onToggleFlashlight
                )
                ShortcutItem(
                    title = "Auto Rotate",
                    icon = Icons.Default.ScreenRotation,
                    isActive = true,
                    onClick = onToggleAutoRotate
                )
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceDark
                )
                .border(
                    1.dp,
                    if (isActive) JarvisCyanBright else Color.White.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) JarvisCyanBright else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
