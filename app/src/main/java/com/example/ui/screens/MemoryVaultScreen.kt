package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.VoiceNoteEntity
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
fun MemoryVaultScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("DIRECTIVES", "VOICE NOTES", "SYSTEM AUDIT")

    val directives by viewModel.directives.collectAsState()
    val voiceNotes by viewModel.voiceNotes.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()

    var showAddDirectiveDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(JarvisVoid, JarvisSurfaceDark, JarvisVoid)
                )
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURE MEMORY VAULT",
                        style = Typography.displayMedium,
                        fontSize = 20.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "ENCRYPTED PERSISTENCE • OPERATOR: IAN GALLAGHER",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted
                    )
                }

                if (selectedTab == 0) {
                    IconButton(
                        onClick = { showAddDirectiveDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisCyan, RoundedCornerShape(6.dp))
                            .testTag("add_directive_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Directive",
                            tint = JarvisCyan
                        )
                    }
                } else if (selectedTab == 1) {
                    IconButton(
                        onClick = { showAddNoteDialog = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisArcGold, RoundedCornerShape(6.dp))
                            .testTag("add_voice_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Note",
                            tint = JarvisArcGold
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, JarvisAlertRed, RoundedCornerShape(6.dp))
                            .testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Logs",
                            tint = JarvisAlertRed
                        )
                    }
                }
            }

            // Horizontal Vault Filter Ribbon (Part 1 Spec)
            val vaultFilters = listOf(
                Triple("ALL DIRECTIVES", 0, Icons.Default.Description),
                Triple("VOICE MEMOS", 1, Icons.Default.Mic),
                Triple("AUDIT TRAILS", 2, Icons.Default.Security),
                Triple("ENCRYPTED DATA", 0, Icons.Default.Lock)
            )

            HorizontalHudScrollRow(
                tag = "vault_filter_ribbon_row",
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vaultFilters, key = { it.first }) { (label, targetTab, icon) ->
                    val isSelected = selectedTab == targetTab
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) JarvisCyan else JarvisBorderActive.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                selectedTab = targetTab
                                viewModel.soundSynthesizer.playAcknowledgeTone()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) JarvisCyan else JarvisTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isSelected) JarvisCyan else JarvisTextSecondary
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = JarvisSurfaceDark,
                contentColor = JarvisCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = JarvisCyan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            viewModel.soundSynthesizer.playAcknowledgeTone()
                        },
                        text = {
                            Text(
                                text = title,
                                style = Typography.labelSmall,
                                color = if (selectedTab == index) JarvisCyan else JarvisTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Contents
            when (selectedTab) {
                0 -> DirectivesList(directives = directives, viewModel = viewModel)
                1 -> VoiceNotesList(notes = voiceNotes, viewModel = viewModel)
                2 -> SystemLogsList(logs = logs)
            }
        }
    }

    if (showAddDirectiveDialog) {
        CreateDirectiveDialog(
            onDismiss = { showAddDirectiveDialog = false },
            onConfirm = { title, desc, priority ->
                viewModel.addDirective(title, desc, priority)
                showAddDirectiveDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        CreateVoiceNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content ->
                viewModel.saveVoiceNote(title, content)
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
fun DirectivesList(
    directives: List<DirectiveEntity>,
    viewModel: JarvisViewModel
) {
    if (directives.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "NO ACTIVE DIRECTIVES RECORDED",
                style = Typography.labelMedium,
                color = JarvisTextMuted
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(directives, key = { it.id }) { directive ->
                HudFrameContainer(
                    title = directive.title,
                    statusTag = if (directive.isActive) "ACTIVE" else "COMPLETED",
                    accentColor = if (directive.isActive) JarvisCyan else JarvisTextMuted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = directive.content,
                                style = Typography.bodyMedium,
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PRIORITY: ${directive.priority} • CREATED: ${SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(directive.timestamp))}",
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = JarvisTextMuted
                            )
                        }

                        Row {
                            IconButton(onClick = { viewModel.toggleDirective(directive) }) {
                                Icon(
                                    imageVector = if (directive.isActive) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                                    contentDescription = "Toggle Status",
                                    tint = if (directive.isActive) JarvisCyan else JarvisSuccessGreen
                                )
                            }
                            IconButton(onClick = { viewModel.deleteDirective(directive) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = JarvisAlertRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceNotesList(
    notes: List<VoiceNoteEntity>,
    viewModel: JarvisViewModel
) {
    if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "NO VOICE MEMOS ARCHIVED IN VAULT",
                style = Typography.labelMedium,
                color = JarvisTextMuted
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                HudFrameContainer(
                    title = note.title,
                    statusTag = "VAULT_MEMO",
                    accentColor = JarvisArcGold,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = note.transcript,
                            style = Typography.bodyMedium,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = JarvisTextMuted
                            )

                            IconButton(
                                onClick = { viewModel.deleteVoiceNote(note) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = JarvisAlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemLogsList(logs: List<SystemLogEntity>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "SYSTEM AUDIT TRAIL EMPTY",
                style = Typography.labelMedium,
                color = JarvisTextMuted
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                val accent = when (log.level) {
                    "CRITICAL", "ERROR" -> JarvisAlertRed
                    "WARNING", "WARN" -> JarvisArcGold
                    else -> JarvisCyan
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisSurfaceElevated, RoundedCornerShape(4.dp))
                        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "[${log.tag}] ${log.message}",
                            style = Typography.bodyMedium,
                            fontSize = 11.sp,
                            color = JarvisTextPrimary
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp)),
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = JarvisTextMuted
                        )
                    }

                    Text(
                        text = log.level,
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateDirectiveDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        title = {
            Text(
                text = "RECORD NEW DIRECTIVE",
                style = Typography.titleMedium,
                color = JarvisCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Directive Title", color = JarvisCyan) },
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
                    label = { Text("Directive Instructions", color = JarvisCyan) },
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
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid)
            ) {
                Text("RECORD DIRECTIVE", style = Typography.labelLarge)
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
fun CreateVoiceNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        title = {
            Text(
                text = "ENCRYPT MEMO TO VAULT",
                style = Typography.titleMedium,
                color = JarvisArcGold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Memo Title", color = JarvisArcGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisArcGold,
                        unfocusedBorderColor = JarvisBorderActive
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Memo Content / Transcript", color = JarvisArcGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisArcGold,
                        unfocusedBorderColor = JarvisBorderActive
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, content)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisArcGold, contentColor = JarvisVoid)
            ) {
                Text("ENCRYPT & SAVE", style = Typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextSecondary)
            }
        }
    )
}
