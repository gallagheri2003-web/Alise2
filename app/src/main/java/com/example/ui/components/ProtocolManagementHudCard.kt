package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProtocolEntity
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBlueCore
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.Typography

/**
 * Management component within HUD to list, add, edit, or remove system protocols.
 * Supports searching, category filtering, and direct HUD protocol execution.
 */
@Composable
fun ProtocolManagementHudCard(
    protocols: List<ProtocolEntity>,
    onExecuteProtocol: (ProtocolEntity) -> Unit,
    onAddProtocol: (name: String, desc: String, code: String) -> Unit,
    onUpdateProtocol: (ProtocolEntity) -> Unit,
    onDeleteProtocol: (ProtocolEntity) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "protocol_management_hud_card"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    var showAddModal by remember { mutableStateOf(false) }
    var editingProtocol by remember { mutableStateOf<ProtocolEntity?>(null) }
    var deletingProtocol by remember { mutableStateOf<ProtocolEntity?>(null) }

    val filteredProtocols = remember(protocols, searchQuery, selectedCategory) {
        protocols.filter { protocol ->
            val matchesCategory = (selectedCategory == "ALL" || protocol.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    protocol.displayName.contains(searchQuery, ignoreCase = true) ||
                    protocol.codeName.contains(searchQuery, ignoreCase = true) ||
                    protocol.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LiquidGlassContainer(
        modifier = modifier.testTag(testTag),
        title = "SYSTEM PROTOCOLS MANAGEMENT",
        statusTag = "${protocols.size} DEFINED",
        accentColor = JarvisCyanBright,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Search & Add Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("protocol_hud_search_input"),
                    placeholder = {
                        Text("Search protocols...", fontSize = 11.sp, color = JarvisTextMuted)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarvisSurfaceDark,
                        unfocusedContainerColor = JarvisSurfaceDark,
                        focusedBorderColor = JarvisCyanBright,
                        unfocusedBorderColor = JarvisCyan.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Add Protocol Button
                LiquidGlassButton(
                    onClick = { showAddModal = true },
                    modifier = Modifier.height(46.dp),
                    accentColor = JarvisCyanBright,
                    testTag = "btn_add_new_protocol"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Protocol",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NEW",
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanBright
                        )
                    }
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "PRODUCTIVITY", "TACTICAL", "SECURITY").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceDark)
                            .border(1.dp, if (isSelected) JarvisCyanBright else JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = Typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) JarvisCyanBright else JarvisTextSecondary
                        )
                    }
                }
            }

            // Scrollable Protocol List (Height limited for HUD container)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisSurfaceDark.copy(alpha = 0.6f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(6.dp)
            ) {
                if (filteredProtocols.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching protocols defined.",
                            style = Typography.labelSmall,
                            color = JarvisTextMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredProtocols, key = { it.id }) { protocol ->
                            ProtocolHudItem(
                                protocol = protocol,
                                onExecute = { onExecuteProtocol(protocol) },
                                onEdit = { editingProtocol = protocol },
                                onDelete = { deletingProtocol = protocol }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Protocol
    if (showAddModal) {
        ProtocolDialog(
            title = "ADD NEW SYSTEM PROTOCOL",
            initialName = "",
            initialCode = "",
            initialDesc = "",
            onConfirm = { name, code, desc ->
                onAddProtocol(name, desc, code)
                showAddModal = false
            },
            onDismiss = { showAddModal = false }
        )
    }

    // Modal: Edit Protocol
    editingProtocol?.let { protocol ->
        ProtocolDialog(
            title = "EDIT SYSTEM PROTOCOL",
            initialName = protocol.displayName,
            initialCode = protocol.codeName,
            initialDesc = protocol.description,
            onConfirm = { name, code, desc ->
                onUpdateProtocol(
                    protocol.copy(
                        displayName = name,
                        codeName = code,
                        description = desc
                    )
                )
                editingProtocol = null
            },
            onDismiss = { editingProtocol = null }
        )
    }

    // Modal: Confirm Delete
    deletingProtocol?.let { protocol ->
        AlertDialog(
            onDismissRequest = { deletingProtocol = null },
            title = {
                Text(
                    text = "PURGE PROTOCOL?",
                    style = Typography.titleMedium,
                    color = JarvisAlertRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete protocol '${protocol.displayName}'? This action cannot be undone.",
                    style = Typography.bodyMedium,
                    color = JarvisTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProtocol(protocol)
                        deletingProtocol = null
                    }
                ) {
                    Text("PURGE", color = JarvisAlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProtocol = null }) {
                    Text("CANCEL", color = JarvisTextMuted)
                }
            },
            containerColor = JarvisSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ProtocolHudItem(
    protocol: ProtocolEntity,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisGlassPanel)
            .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (protocol.isPreset) JarvisArcGold else JarvisCyanBright)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = protocol.displayName,
                        style = Typography.labelMedium,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${protocol.codeName}]",
                        style = Typography.labelSmall,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = protocol.description,
                    style = Typography.bodySmall,
                    fontSize = 9.5.sp,
                    color = JarvisTextSecondary,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Execute Button
                Box(
                    modifier = Modifier
                        .testTag("btn_execute_protocol_${protocol.codeName}")
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisCyan.copy(alpha = 0.25f))
                        .border(1.dp, JarvisCyanBright, RoundedCornerShape(6.dp))
                        .clickable { onExecute() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Execute",
                        tint = JarvisCyanBright,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Delete Button (if not a non-deletable core system preset)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = JarvisAlertRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolDialog(
    title: String,
    initialName: String,
    initialCode: String,
    initialDesc: String,
    onConfirm: (name: String, code: String, desc: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf(initialCode) }
    var desc by remember { mutableStateOf(initialDesc) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = JarvisCyanBright,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Protocol Display Name", color = JarvisTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanBright,
                        unfocusedBorderColor = JarvisCyan.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Protocol Code Name (e.g. S25_TURBO)", color = JarvisTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanBright,
                        unfocusedBorderColor = JarvisCyan.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Protocol Directives & Description", color = JarvisTextMuted) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyanBright,
                        unfocusedBorderColor = JarvisCyan.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onConfirm(name.trim(), code.trim(), desc.trim())
                    }
                }
            ) {
                Text("SAVE PROTOCOL", color = JarvisCyanBright, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextMuted)
            }
        },
        containerColor = JarvisSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    )
}
