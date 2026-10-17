package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProtocolEntity
import com.example.ui.components.HorizontalHudScrollRow
import com.example.ui.components.HudFrameContainer
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBorderActive
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtocolsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val protocols by viewModel.protocols.collectAsState()
    val execState by viewModel.protocolExecution.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var protocolToEdit by remember { mutableStateOf<ProtocolEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "SECURITY", "ENTERTAINMENT", "PRODUCTIVITY", "TACTICAL", "CUSTOM")

    val filteredProtocols = remember(protocols, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            protocols
        } else {
            protocols.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisVoid,
                        JarvisSurfaceDark,
                        JarvisVoid
                    )
                )
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TACTICAL PROTOCOLS",
                        style = Typography.displayMedium,
                        fontSize = 20.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "AUTOMATED DIRECTIVE SEQUENCES • IAN GALLAGHER",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisCyan, RoundedCornerShape(6.dp))
                        .testTag("add_protocol_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Protocol",
                        tint = JarvisCyan
                    )
                }
            }

            // Horizontal Category Filter Row (Part 1 Spec)
            HorizontalHudScrollRow(
                tag = "protocols_category_filter_row",
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it }) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val color = when (cat) {
                        "SECURITY" -> JarvisAlertRed
                        "ENTERTAINMENT" -> JarvisArcGold
                        "PRODUCTIVITY" -> JarvisCyanBright
                        else -> JarvisCyan
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) color.copy(alpha = 0.25f) else JarvisSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) color else JarvisBorderActive.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isSelected) color else JarvisTextSecondary
                        )
                    }
                }
            }

            // Active Execution Banner
            AnimatedVisibility(
                visible = execState.isExecuting,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HudFrameContainer(
                    title = "EXECUTING: ${execState.currentProtocol?.displayName ?: ""}",
                    statusTag = "${(execState.progressPercent * 100).toInt()}%",
                    accentColor = JarvisArcGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column {
                        Text(
                            text = execState.currentStepText,
                            style = Typography.bodyMedium,
                            color = JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { execState.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JarvisArcGold,
                            trackColor = JarvisSurfaceDark
                        )
                    }
                }
            }

            // Protocol Cards List (Vertically scrollable)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProtocols, key = { it.id }) { protocol ->
                    ProtocolCardItem(
                        protocol = protocol,
                        isExecuting = execState.isExecuting && execState.currentProtocol?.id == protocol.id,
                        onExecute = { viewModel.executeProtocol(protocol) },
                        onEdit = { protocolToEdit = protocol },
                        onDelete = { viewModel.deleteProtocol(protocol) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProtocolDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc, code ->
                viewModel.addProtocol(name, desc, code)
                showCreateDialog = false
            }
        )
    }

    protocolToEdit?.let { protocol ->
        EditProtocolDialog(
            protocol = protocol,
            onDismiss = { protocolToEdit = null },
            onConfirm = { updatedProtocol ->
                viewModel.updateProtocol(updatedProtocol)
                protocolToEdit = null
            }
        )
    }
}

@Composable
fun ProtocolCardItem(
    protocol: ProtocolEntity,
    isExecuting: Boolean,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, categoryColor) = when (protocol.category) {
        "SECURITY" -> Icons.Default.Security to JarvisAlertRed
        "ENTERTAINMENT" -> Icons.Default.Celebration to JarvisArcGold
        "PRODUCTIVITY" -> Icons.Default.Headphones to JarvisCyanBright
        else -> Icons.Default.Shield to JarvisCyan
    }

    HudFrameContainer(
        title = protocol.displayName,
        statusTag = protocol.category,
        accentColor = categoryColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = protocol.description,
                style = Typography.bodyMedium,
                fontSize = 12.sp,
                color = JarvisTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Metadata, Edit/Delete Controls, and Execution Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXECUTED ${protocol.executionCount} TIMES",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisTextMuted
                    )
                    if (protocol.lastExecuted > 0) {
                        val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(protocol.lastExecuted))
                        Text(
                            text = "LAST: $dateStr",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = JarvisTextMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .testTag("edit_protocol_${protocol.codeName.lowercase()}_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Protocol",
                            tint = JarvisCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisAlertRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .testTag("delete_protocol_${protocol.codeName.lowercase()}_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Protocol",
                            tint = JarvisAlertRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Button(
                        onClick = onExecute,
                        enabled = !isExecuting,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExecuting) JarvisArcGold else categoryColor.copy(alpha = 0.25f),
                            contentColor = if (isExecuting) JarvisVoid else categoryColor
                        ),
                        modifier = Modifier
                            .border(1.dp, categoryColor, RoundedCornerShape(6.dp))
                            .testTag("execute_${protocol.codeName.lowercase()}_button")
                    ) {
                        Icon(
                            imageVector = if (isExecuting) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = "Engage",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExecuting) "ENGAGING..." else "ENGAGE",
                            style = Typography.labelLarge,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProtocolDialog(
    protocol: ProtocolEntity,
    onDismiss: () -> Unit,
    onConfirm: (updatedProtocol: ProtocolEntity) -> Unit
) {
    var displayName by remember { mutableStateOf(protocol.displayName) }
    var description by remember { mutableStateOf(protocol.description) }
    var category by remember { mutableStateOf(protocol.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        title = {
            Text(
                text = "EDIT PROTOCOL: ${protocol.codeName}",
                style = Typography.titleMedium,
                color = JarvisCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.uppercase() },
                    label = { Text("Category (SECURITY, PRODUCTIVITY, TACTICAL)", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Directive Description", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isNotBlank()) {
                        onConfirm(
                            protocol.copy(
                                displayName = displayName,
                                description = description,
                                category = category
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid)
            ) {
                Text("UPDATE PROTOCOL", style = Typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextSecondary)
            }
        }
    )
}

@Composable
fun CreateProtocolDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        title = {
            Text(
                text = "INITIALIZE NEW PROTOCOL",
                style = Typography.titleMedium,
                color = JarvisCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Protocol Name", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Codename (e.g. OMEGA_SCAN)", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Directive Description", color = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderActive
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onConfirm(name, desc, code)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid)
            ) {
                Text("SAVE PROTOCOL", style = Typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextSecondary)
            }
        }
    )
}
