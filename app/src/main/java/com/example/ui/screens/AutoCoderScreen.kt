package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AutoCodeSnippetEntity
import com.example.data.local.entities.CodeTemplateEntity
import com.example.domain.AutoCodeMode
import com.example.domain.AutoCodeResult
import com.example.domain.CodeIssueFix
import com.example.ui.components.CodeTemplatesVaultView
import com.example.ui.components.PythonSyntaxHighlighter
import com.example.ui.components.SyntaxHighlightedCodeEditor
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
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun AutoCoderScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val editorCode by viewModel.autoCoderEditorCode.collectAsState()
    val currentLanguage by viewModel.autoCoderLanguage.collectAsState()
    val currentMode by viewModel.autoCoderMode.collectAsState()
    val isRunning by viewModel.isAutoCoderRunning.collectAsState()
    val codeResult by viewModel.autoCodeResult.collectAsState()
    val savedSnippets by viewModel.autoCodeSnippets.collectAsState()
    val selectedSnippet by viewModel.selectedCodeSnippet.collectAsState()
    val codeTemplates by viewModel.codeTemplates.collectAsState()

    // Screen Main Mode: 0 = Syntax Editor, 1 = Boilerplates, 2 = Saved Python Files, 3 = AI Surgeon Results
    var mainScreenTab by remember { mutableIntStateOf(0) }
    var surgeonSubTab by remember { mutableIntStateOf(0) }
    var showSampleMenu by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("my_agent.py") }
    var fileSearchQuery by remember { mutableStateOf("") }

    val languages = listOf("Python", "Kotlin", "JavaScript", "Java", "C++", "SQL", "Bash", "JSON", "Auto-Detect")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(JarvisVoid, Color(0xFF060D1F), JarvisVoid)
                )
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // --- TOP HUD STATUS BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) JarvisAmber else EmeraldNominal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JARVIS // PYTHON SYNTAX STUDIO",
                        color = SkyAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (selectedSnippet != null) "Editing: ${selectedSnippet!!.title}" else "Live Syntax Editor & Autonomous Python Runtime",
                    color = if (selectedSnippet != null) JarvisCyan else JarvisTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // New File Action
                Button(
                    onClick = { showNewFileDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisSurfaceElevated,
                        contentColor = JarvisCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Script", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("New", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Boilerplate Templates Quick Jump Button
                Button(
                    onClick = { mainScreenTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisSurfaceElevated,
                        contentColor = SkyAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Extension, contentDescription = "Boilerplates", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Boilerplates", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Sample Code Picker Button
                Box {
                    Button(
                        onClick = { showSampleMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisSurfaceElevated,
                            contentColor = SkyAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Samples",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Samples", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    DropdownMenu(
                        expanded = showSampleMenu,
                        onDismissRequest = { showSampleMenu = false },
                        modifier = Modifier.background(JarvisSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Python: Crypto Wallet Recovery Agent", color = JarvisTextPrimary, fontSize = 12.sp) },
                            onClick = {
                                viewModel.loadSampleBuggyCode("python_wallet_recovery")
                                mainScreenTab = 0
                                showSampleMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Python: Syntax & Missing Imports", color = JarvisTextPrimary, fontSize = 12.sp) },
                            onClick = {
                                viewModel.loadSampleBuggyCode("python_syntax_errors")
                                mainScreenTab = 0
                                showSampleMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Python: Data Filter & Statistics Bug", color = JarvisTextPrimary, fontSize = 12.sp) },
                            onClick = {
                                viewModel.loadSampleBuggyCode("python_data_filter")
                                mainScreenTab = 0
                                showSampleMenu = false
                            }
                        )
                    }
                }
            }
        }

        // --- PRIMARY SECTION TABS (Editor, Boilerplates, Saved Files, Surgeon) ---
        TabRow(
            selectedTabIndex = mainScreenTab,
            containerColor = JarvisSurfaceElevated,
            contentColor = SkyAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[mainScreenTab]),
                    color = JarvisCyan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = mainScreenTab == 0,
                onClick = { mainScreenTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (mainScreenTab == 0) JarvisCyan else JarvisTextMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editor", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = if (mainScreenTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = mainScreenTab == 1,
                onClick = { mainScreenTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (mainScreenTab == 1) SkyAccent else JarvisTextMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Boilerplates (${codeTemplates.size})", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = if (mainScreenTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = mainScreenTab == 2,
                onClick = { mainScreenTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (mainScreenTab == 2) SkyAccent else JarvisTextMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Files (${savedSnippets.size})", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = if (mainScreenTab == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = mainScreenTab == 3,
                onClick = { mainScreenTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (mainScreenTab == 3) EmeraldNominal else JarvisTextMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Surgeon ${if (codeResult != null) "✓" else ""}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = if (mainScreenTab == 3) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TAB 0: SYNTAX-HIGHLIGHTED CODE EDITOR ---
        if (mainScreenTab == 0) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Language & Mode Selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(languages) { lang ->
                            val isSelected = currentLanguage == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SkyAccent.copy(alpha = 0.25f) else JarvisSurfaceCard)
                                    .border(1.dp, if (isSelected) SkyAccent else JarvisBorderGlow, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setAutoCoderLanguage(lang) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lang,
                                    color = if (isSelected) SkyAccent else JarvisTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Dedicated Syntax Highlighted Code Editor Component
                SyntaxHighlightedCodeEditor(
                    code = editorCode,
                    onCodeChange = { viewModel.updateAutoCoderEditorCode(it) },
                    title = selectedSnippet?.title ?: "script.py",
                    language = currentLanguage,
                    isModified = selectedSnippet != null && editorCode != selectedSnippet!!.correctedCode,
                    templates = codeTemplates,
                    onLoadTemplate = { template, append ->
                        viewModel.loadTemplateIntoEditor(template, append)
                        Toast.makeText(context, "Loaded template: ${template.title}", Toast.LENGTH_SHORT).show()
                    },
                    onSaveTemplate = { title, category, description, templateCode ->
                        viewModel.saveCustomCodeTemplate(title, category, description, templateCode)
                        Toast.makeText(context, "Saved boilerplate template '$title'", Toast.LENGTH_SHORT).show()
                    },
                    onSave = { updatedTitle, updatedCode ->
                        if (selectedSnippet != null) {
                            viewModel.updateSavedSnippet(selectedSnippet!!, updatedTitle, updatedCode)
                            Toast.makeText(context, "Saved changes to '${updatedTitle}'", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveAutoCoderSnippet(
                                title = updatedTitle,
                                original = updatedCode,
                                corrected = updatedCode,
                                language = currentLanguage,
                                summary = "Saved Python file",
                                issuesCount = 0
                            )
                            Toast.makeText(context, "Created & saved '${updatedTitle}' to Vault", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAutoFix = { codeToFix ->
                        viewModel.executeAutoCoder(
                            code = codeToFix,
                            language = currentLanguage,
                            mode = currentMode
                        )
                        mainScreenTab = 3
                    },
                    onSimulate = { codeToSimulate ->
                        viewModel.executeAutoCoder(
                            code = codeToSimulate,
                            language = currentLanguage,
                            mode = AutoCodeMode.EXPLAIN
                        )
                        mainScreenTab = 3
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Bar: Run AI Surgeon Button
                Button(
                    onClick = {
                        viewModel.executeAutoCoder(
                            code = editorCode,
                            language = currentLanguage,
                            mode = currentMode
                        )
                        mainScreenTab = 3
                    },
                    enabled = editorCode.isNotBlank() && !isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyAccent,
                        contentColor = JarvisVoid,
                        disabledContainerColor = JarvisSurfaceElevated,
                        disabledContentColor = JarvisTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = JarvisVoid,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPILING & ANALYZING PYTHON...", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Run", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RUN AUTO CODER SURGEON ON SCRIPT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- TAB 1: PYTHON BOILERPLATE TEMPLATES VAULT ---
        else if (mainScreenTab == 1) {
            CodeTemplatesVaultView(
                templates = codeTemplates,
                onSelectTemplate = { template, append ->
                    viewModel.loadTemplateIntoEditor(template, append)
                    mainScreenTab = 0
                    Toast.makeText(context, "Loaded template '${template.title}' into editor", Toast.LENGTH_SHORT).show()
                },
                onSaveNewTemplate = { title, category, description, templateCode ->
                    viewModel.saveCustomCodeTemplate(title, category, description, templateCode)
                    Toast.makeText(context, "Template '$title' saved successfully!", Toast.LENGTH_SHORT).show()
                },
                onDeleteTemplate = { template ->
                    viewModel.deleteCodeTemplate(template)
                    Toast.makeText(context, "Deleted template '${template.title}'", Toast.LENGTH_SHORT).show()
                },
                onRestoreDefaults = {
                    viewModel.restoreDefaultTemplates()
                    Toast.makeText(context, "Restored built-in boilerplate templates", Toast.LENGTH_SHORT).show()
                },
                currentEditorCode = editorCode,
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- TAB 2: SAVED PYTHON FILES VAULT ---
        else if (mainScreenTab == 2) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar for Saved Files
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceCard)
                        .border(1.dp, JarvisBorderGlow, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = SkyAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = fileSearchQuery,
                        onValueChange = { fileSearchQuery = it },
                        placeholder = { Text("Search saved Python files...", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredFiles = savedSnippets.filter {
                    fileSearchQuery.isBlank() ||
                    it.title.contains(fileSearchQuery, ignoreCase = true) ||
                    it.language.contains(fileSearchQuery, ignoreCase = true) ||
                    it.originalCode.contains(fileSearchQuery, ignoreCase = true)
                }

                if (filteredFiles.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorderGlow, Color(0x2238BDF8))))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = JarvisTextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (fileSearchQuery.isBlank()) "No saved Python files yet." else "No files matching '$fileSearchQuery'",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create a new script or save your current editor buffer.",
                                    color = JarvisTextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { showNewFileDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Create Python File", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
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
                        items(filteredFiles) { snippet ->
                            SavedPythonFileCard(
                                snippet = snippet,
                                isSelected = selectedSnippet?.id == snippet.id,
                                onOpenInEditor = {
                                    viewModel.selectCodeSnippet(snippet)
                                    mainScreenTab = 0
                                    Toast.makeText(context, "Opened '${snippet.title}' in Syntax Editor", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteAutoCoderSnippet(snippet) }
                            )
                        }
                    }
                }
            }
        }

        // --- TAB 3: AI SURGEON & COMPILER RESULTS ---
        else if (mainScreenTab == 3) {
            if (codeResult == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorderGlow, Color(0x2238BDF8))))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "AI Code Surgeon Ready",
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Execute the Auto Coder from the Syntax Editor to analyze, debug, and repair your Python code.",
                                color = JarvisTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (editorCode.isNotBlank()) {
                                        viewModel.executeAutoCoder(code = editorCode, language = currentLanguage, mode = currentMode)
                                    } else {
                                        mainScreenTab = 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyAccent, contentColor = JarvisVoid),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (editorCode.isNotBlank()) "Run Surgeon Now" else "Open Syntax Editor", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                    // Surgeon Subtabs
                    ScrollableTabRow(
                        selectedTabIndex = surgeonSubTab,
                        containerColor = JarvisSurfaceElevated,
                        contentColor = SkyAccent,
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[surgeonSubTab]),
                                color = JarvisCyan
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = surgeonSubTab == 0,
                            onClick = { surgeonSubTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = EmeraldNominal)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Healed Code", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        )
                        Tab(
                            selected = surgeonSubTab == 1,
                            onClick = { surgeonSubTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = SkyAccent)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fixes (${codeResult?.fixesApplied?.size ?: 0})", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        )
                        Tab(
                            selected = surgeonSubTab == 2,
                            onClick = { surgeonSubTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(13.dp), tint = JarvisAmber)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Diagnostics & Output", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (surgeonSubTab) {
                            0 -> CorrectedCodeTab(
                                result = codeResult!!,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Corrected Code", codeResult!!.correctedCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Corrected code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, codeResult!!.correctedCode)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Corrected Code")
                                    context.startActivity(shareIntent)
                                },
                                onUseInEditor = {
                                    viewModel.updateAutoCoderEditorCode(codeResult!!.correctedCode)
                                    mainScreenTab = 0
                                    Toast.makeText(context, "Loaded into Syntax Editor", Toast.LENGTH_SHORT).show()
                                },
                                onSave = {
                                    viewModel.saveAutoCoderSnippet(
                                        title = "AutoCode - ${codeResult!!.language} (${codeResult!!.fixesApplied.size} fixes)",
                                        original = codeResult!!.originalCode,
                                        corrected = codeResult!!.correctedCode,
                                        language = codeResult!!.language,
                                        summary = codeResult!!.summary,
                                        issuesCount = codeResult!!.fixesApplied.size
                                    )
                                    Toast.makeText(context, "Saved snippet to local Vault", Toast.LENGTH_SHORT).show()
                                },
                                onReRun = {
                                    viewModel.executeAutoCoder(
                                        code = editorCode,
                                        language = currentLanguage,
                                        mode = currentMode
                                    )
                                }
                            )

                            1 -> FixesBreakdownTab(fixes = codeResult!!.fixesApplied, summary = codeResult!!.summary)

                            2 -> SimulationOutputTab(output = codeResult!!.executionOutput, provider = codeResult!!.providerUsed, latencyMs = codeResult!!.latencyMs)
                        }
                    }
                }
            }
        }
    }

    // --- CREATE NEW PYTHON FILE DIALOG ---
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            containerColor = JarvisSurfaceCard,
            title = {
                Text(
                    text = "Create New Python Script",
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the script name. A starter template with main entry point will be generated.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        label = { Text("File Name (.py)", color = JarvisTextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fileName = if (newFileNameInput.endsWith(".py")) newFileNameInput else "$newFileNameInput.py"
                        viewModel.createPythonFile(title = fileName)
                        showNewFileDialog = false
                        mainScreenTab = 0
                        Toast.makeText(context, "Created '$fileName' in Vault", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Create", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = JarvisTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
fun SavedPythonFileCard(
    snippet: AutoCodeSnippetEntity,
    isSelected: Boolean,
    onOpenInEditor: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenInEditor() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF0F1E36) else JarvisSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isSelected) listOf(JarvisCyan, SkyAccent) else listOf(JarvisBorderGlow, Color(0x2238BDF8))
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF0A1828), CircleShape)
                            .border(1.dp, SkyAccent.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = SkyAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = snippet.title,
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${snippet.language}  •  ${snippet.originalCode.lines().size} lines  •  ${java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(snippet.timestamp))}",
                            color = JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onOpenInEditor,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated, contentColor = JarvisCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Edit", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code Preview Snippet Box (Syntax-styled)
            val previewCode = snippet.correctedCode.ifBlank { snippet.originalCode }.lines().take(3).joinToString("\n")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF060911))
                    .padding(8.dp)
            ) {
                Text(
                    text = PythonSyntaxHighlighter.highlight(previewCode, snippet.language),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CorrectedCodeTab(
    result: AutoCodeResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onUseInEditor: () -> Unit,
    onSave: () -> Unit,
    onReRun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = JarvisGlassPanel),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldNominal.copy(alpha = 0.5f), JarvisCyan.copy(alpha = 0.3f))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Action Bar for corrected code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldNominal.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${result.language} • ZERO ERRORS",
                            color = EmeraldNominal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${result.fixesApplied.size} fixes applied",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SkyAccent, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = JarvisCyan, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onSave, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Save to Vault", tint = JarvisAmber, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code Display Box with Syntax Highlighting
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x334ADE80), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Line numbers
                    val lines = result.correctedCode.lines()
                    val lineNums = (1..lines.size).joinToString("\n")
                    Text(
                        text = lineNums,
                        color = JarvisTextMuted.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Code Content (Syntax Highlighted)
                    Text(
                        text = PythonSyntaxHighlighter.highlight(result.correctedCode, result.language),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldNominal, contentColor = JarvisVoid),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = onUseInEditor,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated, contentColor = SkyAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Editor", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun FixesBreakdownTab(fixes: List<CodeIssueFix>, summary: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "EXECUTIVE REPAIR SUMMARY",
                        color = SkyAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary,
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(fixes) { fix ->
            val severity = when {
                fix.issueType.contains("SYNTAX", ignoreCase = true) || fix.issueType.contains("SECURITY", ignoreCase = true) || fix.issueType.contains("BUG", ignoreCase = true) -> "CRITICAL"
                fix.issueType.contains("TYPE", ignoreCase = true) || fix.issueType.contains("INDENT", ignoreCase = true) -> "WARNING"
                else -> "OPTIMIZATION"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        when (severity) {
                            "CRITICAL" -> listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            "WARNING" -> listOf(JarvisAmber, Color(0xFFD97706))
                            else -> listOf(JarvisCyan, SkyAccent)
                        }
                    )
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (severity) {
                                            "CRITICAL" -> Color(0x33EF4444)
                                            "WARNING" -> Color(0x33F59E0B)
                                            else -> Color(0x3338BDF8)
                                        }
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = severity,
                                    color = when (severity) {
                                        "CRITICAL" -> Color(0xFFEF4444)
                                        "WARNING" -> JarvisAmber
                                        else -> SkyAccent
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = fix.issueType,
                                color = JarvisTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (fix.lineNumber > 0) {
                            Text(
                                text = "Line ${fix.lineNumber}",
                                color = JarvisTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = fix.explanation,
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    if (fix.originalSnippet.isNotBlank() && fix.fixedSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF030712))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "- ${fix.originalSnippet}",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+ ${fix.fixedSnippet}",
                                color = EmeraldNominal,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationOutputTab(output: String, provider: String, latencyMs: Long) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0x4438BDF8), Color(0x2238BDF8))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = JarvisAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TERMINAL & LINT DIAGNOSTICS",
                        color = JarvisAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "$provider (${latencyMs}ms)",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = output,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
