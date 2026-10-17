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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright

/**
 * Fixed Sci-Fi Bottom Dock Bar with One UI 8.5/9 Liquid Glass Surface.
 */
@Composable
fun JarvisBottomDockBar(
    onSearchClick: () -> Unit = {},
    onLaunchPhone: () -> Unit = {},
    onLaunchMessages: () -> Unit = {},
    onLaunchBrowser: () -> Unit = {},
    onLaunchMusic: () -> Unit = {},
    onOpenAppsDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search Trigger Button with Liquid Glass Surface
        Box(
            modifier = Modifier
                .liquidGlassSurface(
                    blurRadius = 16.dp,
                    cornerRadius = 20.dp,
                    transparencyAlpha = 0.35f,
                    accentColor = JarvisCyanBright
                )
                .clickable { onSearchClick() }
                .padding(horizontal = 18.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = JarvisCyanBright,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SEARCH",
                    color = JarvisCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Futuristic Docked Apps Container with One UI 8.5/9 Liquid Glass Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassSurface(
                    blurRadius = 24.dp,
                    cornerRadius = 24.dp,
                    transparencyAlpha = 0.40f,
                    accentColor = JarvisCyanBright
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIcon(
                    icon = Icons.Default.Phone,
                    color = Color(0xFF4CAF50),
                    onClick = onLaunchPhone
                )
                DockIcon(
                    icon = Icons.Default.ChatBubble,
                    color = Color(0xFF2196F3),
                    onClick = onLaunchMessages
                )
                DockIcon(
                    icon = Icons.Default.Language,
                    color = Color(0xFF00BCD4),
                    onClick = onLaunchBrowser
                )
                DockIcon(
                    icon = Icons.Default.MusicNote,
                    color = Color(0xFFE91E63),
                    onClick = onLaunchMusic
                )
                DockIcon(
                    icon = Icons.Default.Apps,
                    color = JarvisCyanBright,
                    onClick = onOpenAppsDrawer
                )
            }
        }
    }
}

@Composable
private fun DockIcon(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.25f))
            .border(1.dp, color, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Dock App",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
