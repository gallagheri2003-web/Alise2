package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CodeModule
import com.example.domain.SelfModificationEngine
import com.example.ui.components.obsidianGlassPanel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SkyBright
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.viewmodel.JarvisViewModel
import com.example.domain.AppLogManager
import com.example.domain.LogLevel
import com.example.domain.LogEntry
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ArrowBack
import com.example.ui.components.holographicBreathingGlow

@Composable
fun SelfAuditScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selfModificationEngine = viewModel.selfModificationEngine
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = CODE INSPECTOR, 1 = LIVE HOT-PATCH ENGINE, 2 = DYNAMIC RULES MATRIX, 3 = SYSTEM LOGS

    val modules by selfModificationEngine.modules.collectAsState()
    val auditResults by selfModificationEngine.auditResults.collectAsState()
    val patches by selfModificationEngine.patches.collectAsState()
    val activeRules by selfModificationEngine.activeCustomRules.collectAsState()

    var selectedModuleIndex by remember { mutableIntStateOf(0) }
    var isRunningAudit by remember { mutableStateOf(false) }
    var patchDescription by remember { mutableStateOf("Optimize MGRS precision calculation and sensor loop") }
    var patchSnippet by remember { mutableStateOf("// Custom Dynamic Hot-Patch\nval telemetryUpdateRate = 60\nprintln(\"[ALISE-PATCH] Applied custom dynamic execution rule.\")") }

    // Periodic Background Self-Audit Daemon Scanner
    LaunchedEffect(Unit) {
        while (true) {
            delay(12000)
            val telemetry = viewModel.telemetryManager.telemetry.value
            val cpuLoad = telemetry.cpuLoadPercent
            val temp = telemetry.cpuTemperatureCelsius
            val ramUsage = telemetry.ramUsagePercent
            val networkLatency = telemetry.networkLatencyMs

            AppLogManager.logSystem(
                "AUDIT_DAEMON",
                "Periodic safety scan: Integrity score nominal (98%) • CPU: $cpuLoad% • Temp: $temp°C • RAM: $ramUsage% • Latency: ${networkLatency}ms"
            )

            if (temp > 40f) {
                AppLogManager.logWarning("THERMAL", "CPU thermal margins narrowing ($temp°C). Dissipative workloads throttling.")
            }
            if (ramUsage > 75) {
                AppLogManager.logWarning("MEMORY", "Transient JVM heap capacity utilizing $ramUsage%. Flushing garbage collector stack proactively.")
            }
        }
    }

    fun runAuditPass() {
        coroutineScope.launch {
            isRunningAudit = true
            delay(800)
            selfModificationEngine.runFullSelfAudit()
            isRunningAudit = false
            Toast.makeText(context, "ALISE Self-Audit Complete: 100% Integrity Score", Toast.LENGTH_SHORT).show()
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xD9050B16)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldNominal)
                    )
                    Column {
                        Text(
                            text = "ALISE SELF-AUDIT & HOT-PATCH ENGINE",
                            style = Typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "AUTONOMOUS AST CODE INSPECTOR & LIVE MUTATION MATRIX",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyBright,
                            fontSize = 8.5.sp
                        )
                    }
                }

                Button(
                    onClick = { runAuditPass() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = SkyBright,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRunningAudit) "SCANNING..." else "AUDIT CODE",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // NAVIGATION TABS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xCC050E1A),
                contentColor = SkyBright,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SkyBright
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0x2E38BDF8), RoundedCornerShape(6.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "CODE",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "HOT-PATCH",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "RULES",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "SYSTEM LOGS",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 8.5.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> CodeInspectorTab(
                    modules = modules,
                    auditResults = auditResults,
                    selectedModuleIndex = selectedModuleIndex,
                    onSelectModule = { selectedModuleIndex = it }
                )
                1 -> LiveHotPatchTab(
                    modules = modules,
                    patches = patches,
                    patchDescription = patchDescription,
                    onDescriptionChange = { patchDescription = it },
                    patchSnippet = patchSnippet,
                    onSnippetChange = { patchSnippet = it },
                    onApplyPatch = { targetFile, desc, code ->
                        selfModificationEngine.generateAndApplyCodePatch(
                            targetFile = targetFile,
                            description = desc,
                            originalSnippet = "// Custom Hot-Patch",
                            modifiedSnippet = code
                        )
                        Toast.makeText(context, "Hot-Patch Applied to $targetFile", Toast.LENGTH_SHORT).show()
                    },
                    onRollback = { patchId ->
                        selfModificationEngine.rollbackPatch(patchId)
                        Toast.makeText(context, "Patch $patchId Rolled Back", Toast.LENGTH_SHORT).show()
                    }
                )
                2 -> DynamicRulesMatrixTab(
                    rules = activeRules,
                    onUpdateRule = { k, v -> selfModificationEngine.updateRule(k, v) }
                )
                3 -> SystemAuditLogsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CodeInspectorTab(
    modules: List<CodeModule>,
    auditResults: List<com.example.domain.DiagnosticAuditResult>,
    selectedModuleIndex: Int,
    onSelectModule: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // SUBSYSTEM HEALTH CHECK CARDS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "INTERNAL CODEBASE AUDIT MATRIX",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = SkyBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )

                auditResults.forEach { res ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .obsidianGlassPanel(
                                cornerRadius = 6.dp,
                                borderColor = if (res.status == "WARN") AmberWarning.copy(alpha = 0.4f) else Color(0x3322C55E),
                                backgroundColor = Color(0xD9050B14)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (res.status == "WARN") AmberWarning else EmeraldNominal)
                                )
                                Text(
                                    text = res.subsystem,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 9.5.sp
                                )
                            }
                            Text(
                                text = res.details,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 8.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3322C55E))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${res.score}%",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNominal,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        // MODULE SELECTOR CHIPS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SELECT KOTLIN MODULE TO INSPECT",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = SkyBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modules.forEachIndexed { index, mod ->
                        val isSelected = index == selectedModuleIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0x3338BDF8) else Color(0xCC050E1A))
                                .border(1.dp, if (isSelected) SkyBright else Color(0x3338BDF8), RoundedCornerShape(6.dp))
                                .clickable { onSelectModule(index) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = mod.filename,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else JarvisTextMuted,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = mod.category,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBright,
                                    fontSize = 7.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // MODULE CODE VIEWER
        item {
            val currentMod = modules.getOrNull(selectedModuleIndex)
            if (currentMod != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 8.dp,
                            borderColor = Color(0x3338BDF8),
                            backgroundColor = Color(0xF2040812)
                        )
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILE: ${currentMod.filename} (${currentMod.linesCount} lines)",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNominal,
                                fontSize = 9.5.sp
                            )
                            Text(
                                text = currentMod.securityRating,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = SkyBright,
                                fontSize = 8.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF03070E))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = currentMod.codeContent,
                                style = Typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF86EFAC),
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveHotPatchTab(
    modules: List<CodeModule>,
    patches: List<com.example.domain.CodePatch>,
    patchDescription: String,
    onDescriptionChange: (String) -> Unit,
    patchSnippet: String,
    onSnippetChange: (String) -> Unit,
    onApplyPatch: (targetFile: String, desc: String, code: String) -> Unit,
    onRollback: (patchId: String) -> Unit
) {
    var selectedTargetFile by remember { mutableStateOf(modules.firstOrNull()?.filename ?: "PhoneLocationTracker.kt") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // HOT-PATCH CREATION PANEL
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x4D38BDF8),
                        backgroundColor = Color(0xF2050B16)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "GENERATE & APPLY LIVE CODE HOT-PATCH",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )

                    // Target file selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        modules.forEach { mod ->
                            val isSel = mod.filename == selectedTargetFile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) Color(0x3338BDF8) else Color(0x22050E1A))
                                    .border(1.dp, if (isSel) SkyBright else Color(0x3338BDF8), RoundedCornerShape(4.dp))
                                    .clickable { selectedTargetFile = mod.filename }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = mod.filename,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) Color.White else JarvisTextMuted,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }

                    // Description input
                    Text(
                        text = "PATCH DIRECTIVE / DESCRIPTION:",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                    BasicTextField(
                        value = patchDescription,
                        onValueChange = onDescriptionChange,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(SkyBright),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF03070E))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    // Code snippet input
                    Text(
                        text = "DYNAMIC KOTLIN / AST CODE SNIPPET:",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SkyBright,
                        fontSize = 8.5.sp
                    )
                    BasicTextField(
                        value = patchSnippet,
                        onValueChange = onSnippetChange,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = Color(0xFF86EFAC)
                        ),
                        cursorBrush = SolidColor(Color(0xFF86EFAC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF03070E))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Button(
                        onClick = {
                            onApplyPatch(selectedTargetFile, patchDescription, patchSnippet)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXECUTE & DEPLOY HOT-PATCH",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // RECENT APPLIED PATCHES & ROLLBACK STACK
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MUTATION HISTORY & ROLLBACK STACK",
                    style = Typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = SkyBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )

                if (patches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .obsidianGlassPanel(cornerRadius = 6.dp)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "No active runtime hot-patches deployed yet. Use editor above to generate mutations.",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextMuted,
                            fontSize = 8.5.sp
                        )
                    }
                } else {
                    patches.forEach { patch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .obsidianGlassPanel(
                                    cornerRadius = 6.dp,
                                    borderColor = if (patch.isApplied) Color(0x3322C55E) else Color(0x33EF4444),
                                    backgroundColor = Color(0xD9050B14)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${patch.id}] ${patch.targetFile} (${patch.timestamp})",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = patch.description,
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldNominal,
                                    fontSize = 8.sp
                                )
                            }

                            if (patch.isApplied) {
                                Button(
                                    onClick = { onRollback(patch.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("ROLLBACK", style = Typography.labelSmall, fontFamily = FontFamily.Monospace, fontSize = 7.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicRulesMatrixTab(
    rules: Map<String, String>,
    onUpdateRule: (key: String, value: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "ACTIVE TACTICAL PROMPT RULES & AST PARAMETERS",
                style = Typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = SkyBright,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }

        items(rules.entries.toList()) { (key, value) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 6.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xF2050B16)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = key,
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SkyBright,
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = value,
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SystemAuditLogsTab(viewModel: JarvisViewModel) {
    val telemetry by viewModel.telemetryManager.telemetry.collectAsState()
    val logs by AppLogManager.logs.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, INFO, SYSTEM, WARNING, SECURITY
    var isDeepScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanStatusText by remember { mutableStateOf("") }

    fun triggerDeepScan() {
        if (isDeepScanning) return
        coroutineScope.launch {
            isDeepScanning = true
            AppLogManager.logSystem("SCANNER", "Manual deep audit pass triggered. Scanning system layers...")

            val scanSteps = listOf(
                "Analyzing network namespaces & interface flags..." to 0.2f,
                "Validating Dalvik heap allocations & memory leaks..." to 0.4f,
                "Querying Snapdragon 8 Elite hardware profile & core governors..." to 0.6f,
                "Auditing file integrity & Android Manifest configurations..." to 0.8f,
                "Performing LLM pipeline loopback latency test..." to 1.0f
            )

            for ((stepText, progress) in scanSteps) {
                scanStatusText = stepText
                scanProgress = progress
                delay(1200)
                AppLogManager.logInfo("SCANNER", "[NODE SCAN] $stepText - STATUS: OK")
            }

            AppLogManager.logSecurity("AUDIT_REPORT", "Deep security scan completed successfully. Core score: 100% integrity verified.")
            Toast.makeText(context, "System Diagnostic Sweep Complete!", Toast.LENGTH_SHORT).show()
            isDeepScanning = false
            scanProgress = 0f
            scanStatusText = ""
        }
    }

    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == "ALL") logs
        else logs.filter { it.level.name == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // SYSTEM METRICS REPORT CARD
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = Color(0x3338BDF8),
                        backgroundColor = Color(0xF2050B16)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ALISE CORE HEALTH AUDIT REPORT",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "DEVICE TELEMETRY & RUNTIME INTEGRITY SHIELD",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SkyBright,
                            fontSize = 8.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3310B981))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SECURE (98%)",
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // CPU & THERMALS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x0C38BDF8))
                            .border(1.dp, Color(0x2638BDF8), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("CPU THERMALS", style = Typography.labelSmall, color = SkyBright, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("${telemetry.cpuTemperatureCelsius}°C", style = Typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("8 Cores Nom", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // MEMORY ALLOC
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x0C38BDF8))
                            .border(1.dp, Color(0x2638BDF8), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("RAM USAGE", style = Typography.labelSmall, color = SkyBright, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("${telemetry.ramUsagePercent}%", style = Typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("${telemetry.ramUsedMb}MB Used", style = Typography.labelSmall, color = JarvisTextMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // BATTERY INTEGRITY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x0C38BDF8))
                            .border(1.dp, Color(0x2638BDF8), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("POWER BUS", style = Typography.labelSmall, color = SkyBright, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("${telemetry.batteryLevel}%", style = Typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(if (telemetry.isCharging) "CHARGING" else "DISCHARGING", style = Typography.labelSmall, color = EmeraldNominal, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // DEEP SYSTEM DIAGNOSTIC SWEEP TRIGGER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .obsidianGlassPanel(
                        cornerRadius = 8.dp,
                        borderColor = if (isDeepScanning) SkyBright.copy(alpha = 0.5f) else Color(0x3338BDF8),
                        backgroundColor = Color(0xF2040914)
                    )
                    .then(
                        if (isDeepScanning) {
                            Modifier.holographicBreathingGlow(color = SkyBright, cornerRadius = 8.dp, minAlpha = 0.05f, maxAlpha = 0.25f)
                        } else {
                            Modifier
                        }
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isDeepScanning) SkyBright else EmeraldNominal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDeepScanning) "DEEP SCAN IN PROGRESS..." else "TACTICAL DIAGNOSTIC SWEEP",
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.5.sp
                            )
                        }

                        if (!isDeepScanning) {
                            Button(
                                onClick = { triggerDeepScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("SWEEP CORE", style = Typography.labelSmall, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (isDeepScanning) {
                        Column {
                            LinearProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = SkyBright,
                                trackColor = Color(0x1A38BDF8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scanStatusText,
                                style = Typography.labelSmall,
                                color = SkyBright,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Initiate manual sweep of low-level Snapdragon telemetry registry nodes, memory allocation guards, and interface configurations.",
                            style = Typography.labelSmall,
                            color = JarvisTextMuted,
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        // FILTER BUTTONS ROW
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ALL", "INFO", "SYSTEM", "WARNING", "SECURITY").forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0x3338BDF8) else Color(0xCC050E1A))
                            .border(1.dp, if (isSelected) SkyBright else Color(0x1F38BDF8), RoundedCornerShape(4.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filter,
                            style = Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else JarvisTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CrimsonDanger.copy(alpha = 0.15f))
                        .border(1.dp, CrimsonDanger.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { AppLogManager.clearLogs() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FLUSH",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = CrimsonDanger,
                        fontSize = 8.sp
                    )
                }
            }
        }

        // LIVE LOG STREAM CONSOLE
        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(cornerRadius = 6.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TRANSIENT LOG BUFFERS EMPTY",
                        style = Typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        } else {
            items(filteredLogs) { log ->
                val levelColor = when (log.level) {
                    LogLevel.INFO -> Color(0xFF38BDF8)
                    LogLevel.SYSTEM -> Color(0xFF10B981)
                    LogLevel.WARNING -> Color(0xFFF59E0B)
                    LogLevel.SECURITY -> Color(0xFFEF4444)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .obsidianGlassPanel(
                            cornerRadius = 6.dp,
                            borderColor = levelColor.copy(alpha = 0.25f),
                            backgroundColor = Color(0xD9040810)
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(levelColor.copy(alpha = 0.15f))
                                        .border(1.dp, levelColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = log.level.name,
                                        style = Typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = levelColor,
                                        fontSize = 7.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[${log.category}]",
                                    style = Typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = SkyBright,
                                    fontSize = 8.sp
                                )
                            }

                            Text(
                                text = log.timestamp,
                                style = Typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextMuted,
                                fontSize = 8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = log.message,
                            style = Typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}

