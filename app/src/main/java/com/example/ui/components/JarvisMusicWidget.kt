package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.JarvisTextMuted

/**
 * Music Player Widget matching Image 2 bottom-left layout.
 */
@Composable
fun JarvisMusicWidget(
    onPlaySpotify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }

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
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MUSIC",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(JarvisCyan.copy(alpha = 0.5f), Color(0xFF0A1E2F))
                            )
                        )
                        .border(1.dp, JarvisCyanBright, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interstellar",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Hans Zimmer",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar & Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1:06",
                    color = JarvisCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                LinearProgressIndicator(
                    progress = { 0.28f },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = JarvisCyanBright,
                    trackColor = JarvisSurfaceDark
                )
                Text(
                    text = "4:06",
                    color = JarvisTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        onPlaySpotify()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
