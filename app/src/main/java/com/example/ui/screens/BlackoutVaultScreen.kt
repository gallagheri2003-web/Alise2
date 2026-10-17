package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import com.example.ui.theme.VioletNeural

data class EncryptedSecret(
    val id: String,
    val title: String,
    val cipherText: String,
    val decryptedText: String,
    var isRevealed: Boolean = false
)

@Composable
fun BlackoutVaultScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBlackoutActive by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSecretText by remember { mutableStateOf("") }
    var isAddingSecret by remember { mutableStateOf(false) }

    val secretNotes = remember {
        mutableStateListOf(
            EncryptedSecret(
                id = "1",
                title = "Primary Bitcoin Cold Wallet Seed",
                cipherText = "U2FsdGVkX19v9q3...[AES-256-GCM]...4X1a",
                decryptedText = "witch collapse practice feed shame open despair creek road again ice bubble"
            ),
            EncryptedSecret(
                id = "2",
                title = "Server Root SSH Keypass & YubiKey PIN",
                cipherText = "U2FsdGVkX1+m9k...[AES-256-GCM]...9L2c",
                decryptedText = "9384-ALISE-OBSIDIAN-SECURE-KEY-2026"
            ),
            EncryptedSecret(
                id = "3",
                title = "Zero-Day Exploit POC Storage Hash",
                cipherText = "U2FsdGVkX18m9a...[AES-256-GCM]...1P0q",
                decryptedText = "SHA256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isBlackoutActive) Color(0xFF000000) else ObsidianVoid)
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Vault",
                            tint = if (isBlackoutActive) CrimsonDanger else SkyAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBlackoutActive) "BLACKOUT PROTOCOL ENGAGED" else "BLACKOUT VAULT & ENCRYPTION",
                            style = Typography.titleMedium,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlackoutActive) CrimsonDanger else JarvisTextPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "AES-256-GCM ZERO-KNOWLEDGE • STEALTH CLOAKING",
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isBlackoutActive) CrimsonDanger.copy(alpha = 0.8f) else SkyBright.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isBlackoutActive) Color(0x33EF4444) else Color(0x2238BDF8))
                        .border(1.dp, if (isBlackoutActive) CrimsonDanger else Color(0x6638BDF8), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isBlackoutActive) "CLOAKED" else "SHIELDED",
                        style = Typography.labelSmall,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBlackoutActive) CrimsonDanger else EmeraldNominal
                    )
                }
            }

            // Emergency Blackout Button Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isBlackoutActive) Brush.linearGradient(
                            listOf(Color(0xFF3D0A0A), Color(0xFF1F0505))
                        ) else Brush.linearGradient(
                            listOf(Color(0xD90E1C3C), Color(0xEB081028))
                        )
                    )
                    .border(
                        1.dp,
                        if (isBlackoutActive) CrimsonDanger else Color(0x4038BDF8),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { isBlackoutActive = !isBlackoutActive }
                    .padding(12.dp)
                    .testTag("toggle_blackout_protocol_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isBlackoutActive) Color(0x44EF4444) else Color(0x2238BDF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBlackoutActive) Icons.Default.Warning else Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = if (isBlackoutActive) CrimsonDanger else SkyAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isBlackoutActive) "BLACKOUT PROTOCOL ACTIVE" else "INITIATE BLACKOUT PROTOCOL",
                                style = Typography.labelLarge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBlackoutActive) CrimsonDanger else JarvisTextPrimary
                            )
                            Text(
                                text = if (isBlackoutActive) "Zero-trace mode. External telemetry muted." else "Tap to lock screen, flush clipboards & cloak IP.",
                                style = Typography.bodySmall,
                                fontSize = 9.sp,
                                color = JarvisTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBlackoutActive) CrimsonDanger else Color(0x330E1C3C))
                            .border(1.dp, if (isBlackoutActive) Color.White else SkyAccent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBlackoutActive) "DISENGAGE" else "ARM LOCK",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlackoutActive) Color.White else SkyBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vault Notes Header & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENCRYPTED VAULT SECRETS (${secretNotes.size})",
                    style = Typography.labelSmall,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyAccent,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x330E1C3C))
                        .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(6.dp))
                        .clickable { isAddingSecret = !isAddingSecret }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Secret",
                            tint = SkyAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isAddingSecret) "CLOSE" else "NEW SECRET",
                            style = Typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBright
                        )
                    }
                }
            }

            // Add Secret Form
            AnimatedVisibility(visible = isAddingSecret) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .obsidianGlassPanel(cornerRadius = 10.dp)
                        .padding(10.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("Secret Title (e.g. Master Key)", fontSize = 11.sp, color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyAccent,
                            unfocusedBorderColor = Color(0x3338BDF8),
                            focusedContainerColor = Color(0xCC0E1C3C),
                            unfocusedContainerColor = Color(0x99081028),
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newSecretText,
                        onValueChange = { newSecretText = it },
                        placeholder = { Text("Secret Content (will be encrypted with AES-256)", fontSize = 11.sp, color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyAccent,
                            unfocusedBorderColor = Color(0x3338BDF8),
                            focusedContainerColor = Color(0xCC0E1C3C),
                            unfocusedContainerColor = Color(0x99081028),
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newSecretText.isNotBlank()) {
                                secretNotes.add(
                                    0,
                                    EncryptedSecret(
                                        id = System.currentTimeMillis().toString(),
                                        title = newTitle,
                                        cipherText = "U2FsdGVkX1" + (100000..999999).random() + "...[AES-256-GCM]",
                                        decryptedText = newSecretText
                                    )
                                )
                                newTitle = ""
                                newSecretText = ""
                                isAddingSecret = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ENCRYPT & STORE IN VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF020408))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secrets List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vault_secrets_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(secretNotes, key = { it.id }) { secret ->
                    var isRevealed by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .obsidianGlassPanel(cornerRadius = 10.dp)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isRevealed) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "Secret",
                                        tint = if (isRevealed) EmeraldNominal else SkyAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = secret.title,
                                        style = Typography.labelMedium,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisTextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { isRevealed = !isRevealed },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Reveal",
                                            tint = SkyBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { secretNotes.remove(secret) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = CrimsonDanger,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isRevealed) secret.decryptedText else secret.cipherText,
                                style = Typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = if (isRevealed) EmeraldNominal else JarvisTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
