package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AliseDatabase
import com.example.data.model.TacticalModule
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography

@Composable
fun TacticalActionsStrip(
    modifier: Modifier = Modifier,
    onModuleTriggered: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var lastTriggeredMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Tactical Modules",
                    tint = SkyAccent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "TACTICAL ACTION PROTOCOLS (50)",
                    style = Typography.labelSmall,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyAccent,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "DISPATCH LIVE",
                style = Typography.labelSmall,
                fontSize = 8.sp,
                color = JarvisTextMuted
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AliseDatabase.tacticalModules.forEach { module ->
                TacticalActionChip(
                    module = module,
                    onClick = {
                        lastTriggeredMessage = module.description
                        onModuleTriggered(module.description)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(module.targetUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun TacticalActionChip(
    module: TacticalModule,
    onClick: () -> Unit
) {
    val chipBgColor = try {
        Color(android.graphics.Color.parseColor(module.colorHex))
    } catch (_: Exception) {
        Color(0xFFF8FAFC)
    }

    val chipTextColor = try {
        Color(android.graphics.Color.parseColor(module.textColorHex))
    } catch (_: Exception) {
        Color(0xFF020408)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(chipBgColor)
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .testTag("tactical_chip_${module.key}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = module.label,
                style = Typography.labelSmall,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = chipTextColor,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = chipTextColor.copy(alpha = 0.6f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}
