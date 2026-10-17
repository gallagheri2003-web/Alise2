package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AliseDatabase
import com.example.data.model.CyberTool
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography

@Composable
fun EducationCyberScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL", "RECON", "EXPLOIT", "WEB", "FORENSICS", "CRYPTO", "WIRELESS", "OSINT", "PRIVESC", "REVERSE", "MOBILE", "CLOUD", "PHISHING", "DEFENSE"
    )

    val filteredTools = remember(searchQuery, selectedCategory) {
        AliseDatabase.cyberTools.filter { tool ->
            val matchesCategory = selectedCategory == "ALL" || tool.tag.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    tool.name.contains(searchQuery, ignoreCase = true) ||
                    tool.desc.contains(searchQuery, ignoreCase = true) ||
                    tool.tag.contains(searchQuery, ignoreCase = true) ||
                    tool.use.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Cyber Vault",
                            tint = SkyAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CYBER EDUCATION & PROTOCOLS",
                            style = Typography.titleMedium,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "52 OFFENSIVE & DEFENSIVE SECURITY SUITES • CHEAT SHEETS & DOCS",
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        color = SkyBright.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2238BDF8))
                        .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredTools.size} / ${AliseDatabase.cyberTools.size} ARMED",
                        style = Typography.labelSmall,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkyBright
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("cyber_tool_search_input"),
                placeholder = {
                    Text(
                        text = "Search Nmap, Metasploit, Wireshark, Burp, Ghidra...",
                        style = Typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = JarvisTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SkyAccent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyAccent,
                    unfocusedBorderColor = Color(0x3338BDF8),
                    focusedContainerColor = Color(0xCC0E1C3C),
                    unfocusedContainerColor = Color(0x99081028),
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextSecondary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(Color(0xE60D2A6E), Color(0xF20A1E50))
                                ) else Brush.horizontalGradient(
                                    listOf(Color(0x330E1C3C), Color(0x33081028))
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SkyAccent else Color(0x2238BDF8),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("category_chip_$cat")
                    ) {
                        Text(
                            text = cat,
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SkyBright else JarvisTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tools List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cyber_tools_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTools, key = { it.id }) { tool ->
                    CyberToolCard(tool = tool) { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }
}

@Composable
fun CyberToolCard(
    tool: CyberTool,
    onOpenUrl: (String) -> Unit
) {
    val tagColor = when (tool.tag) {
        "RECON" -> Color(0xFF38BDF8)
        "EXPLOIT" -> Color(0xFFEF4444)
        "WEB" -> Color(0xFFF59E0B)
        "FORENSICS" -> Color(0xFFC4B5FD)
        "CRYPTO" -> Color(0xFF2BC764)
        "WIRELESS" -> Color(0xFFFDE68A)
        "OSINT" -> Color(0xFF67E8F9)
        "PRIVESC" -> Color(0xFFF472B6)
        "REVERSE" -> Color(0xFFA78BFA)
        "MOBILE" -> Color(0xFF34D399)
        "CLOUD" -> Color(0xFF818CF8)
        "PHISHING" -> Color(0xFFFB923C)
        else -> EmeraldNominal
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .obsidianGlassPanel(cornerRadius = 12.dp)
            .padding(12.dp)
            .testTag("tool_card_${tool.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.name,
                        style = Typography.titleMedium,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tagColor.copy(alpha = 0.15f))
                            .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tool.tag,
                            style = Typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                    }
                }

                Text(
                    text = tool.use,
                    style = Typography.labelSmall,
                    fontSize = 9.sp,
                    color = JarvisTextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tool.desc,
                style = Typography.bodyMedium,
                fontSize = 11.5.sp,
                color = JarvisTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tool.githubUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x330E1C3C))
                            .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(6.dp))
                            .clickable { onOpenUrl(tool.githubUrl) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Source",
                                tint = SkyAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GITHUB REPO",
                                style = Typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (tool.docsUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x330E1C3C))
                            .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(6.dp))
                            .clickable { onOpenUrl(tool.docsUrl) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Documentation",
                                tint = SkyBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OFFICIAL DOCS",
                                style = Typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBright
                            )
                        }
                    }
                }
            }
        }
    }
}
