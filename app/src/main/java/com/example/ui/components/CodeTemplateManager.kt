package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CodeTemplateEntity
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassPanel
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.SkyAccent

/**
 * Full Tab View for browsing, searching, previewing, and managing Boilerplate Templates.
 */
@Composable
fun CodeTemplatesVaultView(
    templates: List<CodeTemplateEntity>,
    onSelectTemplate: (CodeTemplateEntity, Boolean) -> Unit,
    onSaveNewTemplate: (title: String, category: String, description: String, code: String) -> Unit,
    onDeleteTemplate: (CodeTemplateEntity) -> Unit,
    onRestoreDefaults: () -> Unit,
    currentEditorCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var previewTemplate by remember { mutableStateOf<CodeTemplateEntity?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<CodeTemplateEntity?>(null) }

    val categories = remember(templates) {
        val cats = mutableListOf("ALL")
        cats.addAll(templates.map { it.category }.distinct().sorted())
        cats
    }

    val filteredTemplates = remember(templates, searchQuery, selectedCategory) {
        templates.filter { template ->
            val matchesCategory = (selectedCategory == "ALL") || template.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    template.title.contains(searchQuery, ignoreCase = true) ||
                    template.description.contains(searchQuery, ignoreCase = true) ||
                    template.category.contains(searchQuery, ignoreCase = true) ||
                    template.templateCode.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Search and Actions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search boilerplate templates...",
                        color = JarvisTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                textStyle = TextStyle(
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorderGlow,
                    focusedContainerColor = JarvisSurfaceElevated,
                    unfocusedContainerColor = JarvisSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Save Current Editor Code as Template Button
            Button(
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = JarvisVoid
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save Template", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Save Current",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory.equals(category, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) JarvisCyan else JarvisBorderGlow,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (category == "ALL") "All Boilerplates (${templates.size})" else "$category (${templates.count { it.category.equals(category, true) }})",
                        color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Template List or Empty State
        if (filteredTemplates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurfaceCard)
                    .border(1.dp, JarvisBorderGlow, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No templates matched your filter",
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing the search query or restore the standard library.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRestoreDefaults,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisSurfaceElevated,
                            contentColor = SkyAccent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore Built-in Library", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
                    TemplateItemCard(
                        template = template,
                        onLoadReplace = { onSelectTemplate(template, false) },
                        onLoadAppend = { onSelectTemplate(template, true) },
                        onPreview = { previewTemplate = template },
                        onDelete = { templateToDelete = template }
                    )
                }
            }
        }
    }

    // Code Preview Modal Dialog
    if (previewTemplate != null) {
        val template = previewTemplate!!
        AlertDialog(
            onDismissRequest = { previewTemplate = null },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onSelectTemplate(template, true)
                            previewTemplate = null
                            Toast.makeText(context, "Appended '${template.title}' to editor", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyAccent),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Append to Code", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = {
                            onSelectTemplate(template, false)
                            previewTemplate = null
                            Toast.makeText(context, "Loaded '${template.title}' into editor", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Load in Editor", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { previewTemplate = null }) {
                    Text("Close", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = template.title,
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = template.description,
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY: ${template.category.uppercase()} // ${template.language}",
                            color = SkyAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Template Code", template.templateCode))
                                Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = JarvisCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisVoid)
                            .border(1.dp, JarvisBorderGlow, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = PythonSyntaxHighlighter.highlight(template.templateCode),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            containerColor = JarvisSurfaceCard,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Save As Template Dialog
    if (showSaveDialog) {
        SaveAsTemplateDialog(
            initialCode = currentEditorCode,
            onDismiss = { showSaveDialog = false },
            onSave = { title, category, description, code ->
                onSaveNewTemplate(title, category, description, code)
                showSaveDialog = false
                Toast.makeText(context, "Saved template '$title' to library!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Template Confirmation Dialog
    if (templateToDelete != null) {
        val target = templateToDelete!!
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = {
                Text("Delete Template?", color = JarvisTextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove '${target.title}' from your template library?", color = JarvisTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTemplate(target)
                        templateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D), contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Delete", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Cancel", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = JarvisSurfaceCard,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Single Template Item Card with quick load, preview, copy, and delete buttons.
 */
@Composable
fun TemplateItemCard(
    template: CodeTemplateEntity,
    onLoadReplace: () -> Unit,
    onLoadAppend: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, JarvisBorderGlow, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Category Badge + Title + Custom Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (template.category.lowercase()) {
                                    "agents & bots", "crypto" -> Color(0xFF1E1B4B)
                                    "web & apis", "fastapi" -> Color(0xFF0F2634)
                                    "data science" -> Color(0xFF1C1917)
                                    "automation", "scraping" -> Color(0xFF064E3B)
                                    "database", "sqlite" -> Color(0xFF3B1E08)
                                    else -> Color(0xFF1E293B)
                                }
                            )
                            .border(
                                1.dp,
                                when (template.category.lowercase()) {
                                    "agents & bots", "crypto" -> Color(0xFFA5B4FC).copy(alpha = 0.5f)
                                    "web & apis", "fastapi" -> SkyAccent.copy(alpha = 0.5f)
                                    "data science" -> JarvisAmber.copy(alpha = 0.5f)
                                    "automation", "scraping" -> EmeraldNominal.copy(alpha = 0.5f)
                                    else -> JarvisCyan.copy(alpha = 0.5f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = template.category.uppercase(),
                            color = JarvisCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = template.title,
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (template.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(JarvisAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "CUSTOM",
                                color = JarvisAmber,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Quick Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Template Code", template.templateCode))
                            Toast.makeText(context, "Copied '${template.title}' code", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = JarvisTextSecondary, modifier = Modifier.size(13.dp))
                    }

                    if (template.isCustom) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            // Description
            if (template.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: Load, Append, Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${template.templateCode.lines().size} lines // Python",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Preview Button
                    OutlinedButton(
                        onClick = onPreview,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyAccent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Preview", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Append Button
                    OutlinedButton(
                        onClick = onLoadAppend,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldNominal),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+ Append", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Load (Replace) Button
                    Button(
                        onClick = onLoadReplace,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Load", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Quick Picker Dialog to quickly select and insert boilerplate templates from inside the Editor.
 */
@Composable
fun QuickTemplatePickerDialog(
    templates: List<CodeTemplateEntity>,
    onSelect: (CodeTemplateEntity, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = remember(templates) {
        val cats = mutableListOf("ALL")
        cats.addAll(templates.map { it.category }.distinct().sorted())
        cats
    }

    val filtered = remember(templates, searchQuery, selectedCategory) {
        templates.filter { template ->
            val matchesCategory = (selectedCategory == "ALL") || template.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    template.title.contains(searchQuery, ignoreCase = true) ||
                    template.description.contains(searchQuery, ignoreCase = true) ||
                    template.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Extension, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Insert Python Boilerplate",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter templates...", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp)) },
                    textStyle = TextStyle(color = JarvisTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedContainerColor = JarvisVoid,
                        unfocusedContainerColor = JarvisVoid
                    ),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisBorderGlow, RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Templates List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.id }) { template ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisSurfaceElevated)
                                .border(1.dp, JarvisBorderGlow, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.title,
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = template.category,
                                        color = SkyAccent,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (template.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = template.description,
                                        color = JarvisTextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSelect(template, true)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldNominal),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("+ Append", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            onSelect(template, false)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Text("Replace Code", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = JarvisSurfaceCard,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Dialog to save current script as a new reusable boilerplate template.
 */
@Composable
fun SaveAsTemplateDialog(
    initialCode: String,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, description: String, code: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Agents & Bots") }
    var description by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(initialCode) }

    val presetCategories = listOf("Agents & Bots", "Web & APIs", "Automation", "Data Science", "System & Utilities", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), category.trim(), description.trim(), code)
                    }
                },
                enabled = title.isNotBlank() && code.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save to Library", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Extension, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save as Boilerplate Template",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Template Title *", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("e.g. Asynchronous Web Crawler", color = JarvisTextMuted, fontSize = 11.sp) },
                    textStyle = TextStyle(color = JarvisTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedContainerColor = JarvisVoid,
                        unfocusedContainerColor = JarvisVoid
                    ),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection Chips
                Text("Category:", color = SkyAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(presetCategories) { cat ->
                        val isSelected = category.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisBorderGlow, RoundedCornerShape(12.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("Brief description of script functionality...", color = JarvisTextMuted, fontSize = 11.sp) },
                    textStyle = TextStyle(color = JarvisTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedContainerColor = JarvisVoid,
                        unfocusedContainerColor = JarvisVoid
                    ),
                    shape = RoundedCornerShape(6.dp),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Code Preview
                Text("Template Code Preview (${code.lines().size} lines):", color = SkyAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisVoid)
                        .border(1.dp, JarvisBorderGlow, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = PythonSyntaxHighlighter.highlight(code),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        containerColor = JarvisSurfaceCard,
        shape = RoundedCornerShape(12.dp)
    )
}
