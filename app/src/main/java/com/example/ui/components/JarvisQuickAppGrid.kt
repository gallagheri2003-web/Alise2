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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSurfaceDark

/**
 * Quick App Grid Widget matching Image 2 mid-right layout.
 */
@Composable
fun JarvisQuickAppGrid(
    onLaunchApp: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val apps = listOf(
        "Messages" to Icons.Default.ChatBubble,
        "Camera" to Icons.Default.CameraAlt,
        "Mail" to Icons.Default.Email,
        "Photos" to Icons.Default.PhotoLibrary,
        "Notes" to Icons.Default.Note,
        "Reminders" to Icons.Default.CheckCircle,
        "Files" to Icons.Default.Folder,
        "Clock" to Icons.Default.Schedule,
        "App Store" to Icons.Default.ShoppingBag,
        "Settings" to Icons.Default.Settings
    )

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
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Render in 2 columns
            for (i in apps.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AppTile(
                        title = apps[i].first,
                        icon = apps[i].second,
                        onClick = { onLaunchApp(apps[i].first) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < apps.size) {
                        AppTile(
                            title = apps[i + 1].first,
                            icon = apps[i + 1].second,
                            onClick = { onLaunchApp(apps[i + 1].first) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            JarvisCyan.copy(alpha = 0.25f),
                            Color(0xFF0D253A)
                        )
                    )
                )
                .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = JarvisCyanBright,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
