package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Extension
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

@Composable
fun SyntaxHighlightedCodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    title: String = "script.py",
    language: String = "Python",
    isModified: Boolean = false,
    templates: List<CodeTemplateEntity> = emptyList(),
    onLoadTemplate: ((CodeTemplateEntity, Boolean) -> Unit)? = null,
    onSaveTemplate: ((title: String, category: String, description: String, code: String) -> Unit)? = null,
    onSave: ((updatedTitle: String, updatedCode: String) -> Unit)? = null,
    onAutoFix: ((String) -> Unit)? = null,
    onSimulate: ((String) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTitle by remember(title) { mutableStateOf(title) }
    var isRenaming by remember { mutableStateOf(false) }
    var renameInput by remember(currentTitle) { mutableStateOf(currentTitle) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }

    // Search / Find in code
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }


    // Undo / Redo history
    val history = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }

    // Synchronized scroll states
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    // Python Quick Insert Shortcuts
    val pythonShortcuts = listOf(
        "def " to "def ():",
        "class " to "class :",
        "if " to "if :",
        "else:" to "else:",
        "elif " to "elif :",
        "for " to "for item in :",
        "while " to "while :",
        "try:" to "try:\n    \nexcept Exception as e:\n    print(e)",
        "return " to "return ",
        "print()" to "print()",
        "import " to "import ",
        "Tab (4sp)" to "    ",
        "# " to "# ",
        ":" to ":",
        "==" to " == ",
        "!=" to " != ",
        "->" to " -> ",
        "\"\"" to "\"\"",
        "''" to "''",
        "()" to "()",
        "[]" to "[]",
        "{}" to "{}"
    )

    fun pushHistory(newCode: String) {
        if (historyIndex >= 0 && historyIndex < history.size && history[historyIndex] == newCode) return
        while (history.size > historyIndex + 1) {
            history.removeAt(history.size - 1)
        }
        history.add(newCode)
        historyIndex = history.size - 1
    }

    LaunchedEffect(Unit) {
        if (history.isEmpty()) {
            history.add(code)
            historyIndex = 0
        }
    }

    val lines = remember(code) { code.lines() }
    val lineCount = lines.size
    val charCount = code.length

    // Search matches count
    val searchMatchCount = remember(code, searchQuery) {
        if (searchQuery.isBlank()) 0
        else {
            var count = 0
            var idx = code.indexOf(searchQuery, ignoreCase = true)
            while (idx != -1) {
                count++
                idx = code.indexOf(searchQuery, idx + searchQuery.length, ignoreCase = true)
            }
            count
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurfaceCard)
            .border(1.dp, if (isModified) JarvisAmber.copy(alpha = 0.6f) else JarvisBorderGlow, RoundedCornerShape(12.dp))
    ) {
        // TOP FILE HEADER & ACTIONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Title + Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF0F2634), CircleShape)
                        .border(1.dp, SkyAccent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code",
                        tint = SkyAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Editable Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            renameInput = currentTitle
                            isRenaming = true
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentTitle,
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Status Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isModified) JarvisAmber.copy(alpha = 0.2f) else EmeraldNominal.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isModified) "● Modified" else "✓ Saved",
                        color = if (isModified) JarvisAmber else EmeraldNominal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right: Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Find Button
                IconButton(
                    onClick = { isSearchOpen = !isSearchOpen },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FindInPage,
                        contentDescription = "Search",
                        tint = if (isSearchOpen) JarvisCyan else JarvisTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Templates / Boilerplate Button
                if (templates.isNotEmpty() && onLoadTemplate != null) {
                    Button(
                        onClick = { showTemplatePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = SkyAccent
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = "Boilerplates", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Boilerplates", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Undo Button

                IconButton(
                    onClick = {
                        if (historyIndex > 0) {
                            historyIndex--
                            onCodeChange(history[historyIndex])
                        }
                    },
                    enabled = historyIndex > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (historyIndex > 0) JarvisTextSecondary else JarvisTextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Redo Button
                IconButton(
                    onClick = {
                        if (historyIndex < history.size - 1) {
                            historyIndex++
                            onCodeChange(history[historyIndex])
                        }
                    },
                    enabled = historyIndex < history.size - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (historyIndex < history.size - 1) JarvisTextSecondary else JarvisTextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Auto Fix AI Button
                if (onAutoFix != null) {
                    Button(
                        onClick = { onAutoFix(code) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E1B4B),
                            contentColor = Color(0xFFA5B4FC)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Auto Fix", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Heal", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Simulate Run Button
                if (onSimulate != null) {
                    Button(
                        onClick = { onSimulate(code) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF064E3B),
                            contentColor = EmeraldNominal
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Simulate Run", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Run", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Save Button
                if (onSave != null) {
                    Button(
                        onClick = {
                            onSave(currentTitle, code)
                            pushHistory(code)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = JarvisVoid
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Save", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Close Button if opened in modal
                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // SEARCH / FIND OVERLAY BAR
        AnimatedVisibility(
            visible = isSearchOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1322))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .border(1.dp, JarvisBorderGlow, RoundedCornerShape(6.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(JarvisCyan),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Find in code...", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "$searchMatchCount match${if (searchMatchCount == 1) "" else "es"}",
                        color = if (searchMatchCount > 0) EmeraldNominal else JarvisAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = JarvisTextMuted, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // QUICK INSERT PYTHON SHORTCUTS TOOLBAR
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090E1A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(pythonShortcuts) { (label, insertValue) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x331E293B))
                        .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                        .clickable {
                            val newCode = code + insertValue
                            onCodeChange(newCode)
                            pushHistory(newCode)
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = label,
                        color = SkyAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // MAIN CODE EDITING WORKSPACE WITH LINE NUMBERS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(380.dp)
                .background(Color(0xFF060911))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // LINE NUMBERS GUTTER
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .background(Color(0xFF0A0F1D))
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // DIVIDER LINE
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height((lineCount * 18 + 24).dp)
                        .background(Color(0xFF1E293B))
                )

                // CODE TEXT AREA (SYNTAX HIGHLIGHTED)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(start = 10.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    BasicTextField(
                        value = code,
                        onValueChange = { newCode ->
                            onCodeChange(newCode)
                            pushHistory(newCode)
                        },
                        textStyle = TextStyle(
                            color = Color(0xFFF1F5F9),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        visualTransformation = PythonSyntaxVisualTransformation(language),
                        cursorBrush = SolidColor(JarvisCyan),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (code.isEmpty()) {
                        Text(
                            text = "# Write or paste Python code here...\n# Syntax highlighting will appear in real time.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // BOTTOM STATUS FOOTER & UTILITIES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Stats (Lines, Characters, Language)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$lineCount lines  |  $charCount chars  |  $language (UTF-8)",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Actions: Copy, Share, Clear
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Copy
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Code", code))
                        Toast.makeText(context, "Copied code to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Share
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, code)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Python Script"))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Save as Template
                if (onSaveTemplate != null) {
                    IconButton(
                        onClick = { showSaveTemplateDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = "Save as Template",
                            tint = SkyAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }

    // QUICK TEMPLATE PICKER DIALOG
    if (showTemplatePicker && onLoadTemplate != null) {
        QuickTemplatePickerDialog(
            templates = templates,
            onSelect = { template, append ->
                onLoadTemplate(template, append)
                pushHistory(code)
            },
            onDismiss = { showTemplatePicker = false }
        )
    }

    // SAVE AS TEMPLATE DIALOG
    if (showSaveTemplateDialog && onSaveTemplate != null) {
        SaveAsTemplateDialog(
            initialCode = code,
            onDismiss = { showSaveTemplateDialog = false },
            onSave = { title, category, description, templateCode ->
                onSaveTemplate(title, category, description, templateCode)
                showSaveTemplateDialog = false
            }
        )
    }

    // RENAME FILE DIALOG

    if (isRenaming) {
        AlertDialog(
            onDismissRequest = { isRenaming = false },
            containerColor = JarvisSurfaceCard,
            title = {
                Text(
                    text = "Rename File",
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("File Name", color = JarvisTextMuted, fontSize = 11.sp) },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            currentTitle = renameInput.trim()
                            onSave?.invoke(currentTitle, code)
                        }
                        isRenaming = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Apply", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { isRenaming = false }) {
                    Text("Cancel", color = JarvisTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        )
    }
}
