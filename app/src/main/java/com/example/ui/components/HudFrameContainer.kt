package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography

@Composable
fun HudFrameContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    statusTag: String? = null,
    accentColor: Color = JarvisCyan,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphicSurface(
                cornerRadius = 16.dp,
                glassColor = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.35f),
                accentColor = accentColor,
                borderWidth = 1.2.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (title != null || statusTag != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            style = Typography.titleMedium,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            letterSpacing = 0.8.sp
                        )
                    }

                    if (statusTag != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusTag.uppercase(),
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}
