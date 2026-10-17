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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.directory.FunctionalStatus
import com.example.data.directory.ImplementationType
import com.example.data.directory.JarvisSystemDirectoryRepository
import com.example.data.directory.SystemDirectoryItem
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

@Composable
fun SystemDirectoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier,
    onNavigateToScreen: ((Int) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var activeFilterTag by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<SystemDirectoryItem?>(null) }

    val stats = remember { JarvisSystemDirectoryRepository.getStatistics() }
    val allItems = JarvisSystemDirectoryRepository.items

    val filteredItems = remember(searchQuery, selectedCategory, activeFilterTag) {
        allItems.filter { item ->
            val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.locationScreen.contains(searchQuery, ignoreCase = true) ||
                    item.howToAccess.contains(searchQuery, ignoreCase = true) ||
                    item.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesFilterTag = when (activeFilterTag) {
                "FUNCTIONAL" -> item.functionalStatus == FunctionalStatus.FUNCTIONAL
                "PARTIAL" -> item.functionalStatus == FunctionalStatus.PARTIAL
                "NOT_IMPLEMENTED" -> item.functionalStatus == FunctionalStatus.NOT_IMPLEMENTED
                "VISIBLE" -> item.visibleInUi
                "HIDDEN" -> !item.visibleInUi
                "REQUIRES_PERM" -> item.requiredPermissions.isNotEmpty()
                "ANDROID_NATIVE" -> item.implementationType == ImplementationType.ANDROID_NATIVE
                "UI_ONLY" -> item.implementationType == ImplementationType.UI_ONLY
                "BACKEND_SERVICE" -> item.implementationType == ImplementationType.BACKEND_SERVICE
                else -> true
            }

            matchesCategory && matchesSearch && matchesFilterTag
        }
    }

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
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JARVIS SYSTEM DIRECTORY",
                        style = Typography.displayMedium,
                        fontSize = 20.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "COMPREHENSIVE SUBSYSTEM INVENTORY • OPERATOR: IAN GALLAGHER",
                        style = Typography.labelSmall,
                        color = JarvisTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .border(1.dp, JarvisCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredItems.size}/${stats.totalCount} ITEMS",
                        style = Typography.labelSmall,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Summary Statistics Horizontal Scroll Row (Interactive Filter Pills)
            Text(
                text = "SYSTEM METRICS AUDIT (TAP TO FILTER)",
                style = Typography.labelSmall,
                fontSize = 9.sp,
                color = JarvisCyan,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            HorizontalHudScrollRow(
                tag = "directory_stats_row",
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    DirectoryStatChip(
                        label = "TOTAL ITEMS",
                        count = stats.totalCount,
                        color = JarvisCyan,
                        isSelected = activeFilterTag == null,
                        onClick = { activeFilterTag = null }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "FUNCTIONAL",
                        count = stats.functionalCount,
                        color = JarvisSuccessGreen,
                        isSelected = activeFilterTag == "FUNCTIONAL",
                        onClick = { activeFilterTag = if (activeFilterTag == "FUNCTIONAL") null else "FUNCTIONAL" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "PARTIAL",
                        count = stats.partialCount,
                        color = JarvisArcGold,
                        isSelected = activeFilterTag == "PARTIAL",
                        onClick = { activeFilterTag = if (activeFilterTag == "PARTIAL") null else "PARTIAL" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "NOT IMPLEMENTED",
                        count = stats.notImplementedCount,
                        color = JarvisAlertRed,
                        isSelected = activeFilterTag == "NOT_IMPLEMENTED",
                        onClick = { activeFilterTag = if (activeFilterTag == "NOT_IMPLEMENTED") null else "NOT_IMPLEMENTED" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "VISIBLE IN UI",
                        count = stats.visibleCount,
                        color = JarvisCyanBright,
                        isSelected = activeFilterTag == "VISIBLE",
                        onClick = { activeFilterTag = if (activeFilterTag == "VISIBLE") null else "VISIBLE" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "HIDDEN / SYSTEM",
                        count = stats.hiddenCount,
                        color = JarvisTextMuted,
                        isSelected = activeFilterTag == "HIDDEN",
                        onClick = { activeFilterTag = if (activeFilterTag == "HIDDEN") null else "HIDDEN" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "REQUIRES PERMISSION",
                        count = stats.requiresPermissionCount,
                        color = JarvisArcGold,
                        isSelected = activeFilterTag == "REQUIRES_PERM",
                        onClick = { activeFilterTag = if (activeFilterTag == "REQUIRES_PERM") null else "REQUIRES_PERM" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "ANDROID-NATIVE",
                        count = stats.androidNativeCount,
                        color = JarvisSuccessGreen,
                        isSelected = activeFilterTag == "ANDROID_NATIVE",
                        onClick = { activeFilterTag = if (activeFilterTag == "ANDROID_NATIVE") null else "ANDROID_NATIVE" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "UI-ONLY",
                        count = stats.uiOnlyCount,
                        color = JarvisCyan,
                        isSelected = activeFilterTag == "UI_ONLY",
                        onClick = { activeFilterTag = if (activeFilterTag == "UI_ONLY") null else "UI_ONLY" }
                    )
                }
                item {
                    DirectoryStatChip(
                        label = "BACKEND/SERVICE",
                        count = stats.backendServiceCount,
                        color = JarvisArcGold,
                        isSelected = activeFilterTag == "BACKEND_SERVICE",
                        onClick = { activeFilterTag = if (activeFilterTag == "BACKEND_SERVICE") null else "BACKEND_SERVICE" }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search 420+ components, permissions, commands, screens...",
                        style = Typography.bodyMedium,
                        fontSize = 12.sp,
                        color = JarvisTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorderActive.copy(alpha = 0.5f),
                    focusedContainerColor = JarvisSurfaceElevated.copy(alpha = 0.5f),
                    unfocusedContainerColor = JarvisSurfaceCard.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("directory_search_input")
            )

            // Category Horizontal Scroll Filter Ribbon
            HorizontalHudScrollRow(
                tag = "directory_category_ribbon",
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(JarvisSystemDirectoryRepository.categories, key = { it }) { category ->
                    val isSelected = selectedCategory.equals(category, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) JarvisCyan else JarvisBorderActive.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                selectedCategory = category
                                viewModel.soundSynthesizer.playAcknowledgeTone()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isSelected) JarvisCyan else JarvisTextSecondary
                        )
                    }
                }
            }

            // Scrollable Component Directory Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO COMPONENTS MATCHING FILTER CRITERIA",
                            style = Typography.labelMedium,
                            color = JarvisTextMuted
                        )
                        Text(
                            text = "Try clearing search keywords or selecting 'ALL' category",
                            style = Typography.labelSmall,
                            fontSize = 10.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        SystemDirectoryItemCard(
                            item = item,
                            onClick = {
                                selectedItem = item
                                viewModel.soundSynthesizer.playAcknowledgeTone()
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog when an item is selected
    selectedItem?.let { item ->
        SystemDirectoryDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onNavigate = { screenIndex ->
                selectedItem = null
                onNavigateToScreen?.invoke(screenIndex)
            }
        )
    }
}

@Composable
fun DirectoryStatChip(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color.copy(alpha = 0.22f) else JarvisSurfaceCard)
            .border(
                1.dp,
                if (isSelected) color else JarvisBorderActive.copy(alpha = 0.35f),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$count",
                style = Typography.labelLarge,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = Typography.labelSmall,
                fontSize = 9.sp,
                color = if (isSelected) JarvisTextPrimary else JarvisTextMuted
            )
        }
    }
}

@Composable
fun SystemDirectoryItemCard(
    item: SystemDirectoryItem,
    onClick: () -> Unit
) {
    val statusColor = when (item.functionalStatus) {
        FunctionalStatus.FUNCTIONAL -> JarvisSuccessGreen
        FunctionalStatus.PARTIAL -> JarvisArcGold
        FunctionalStatus.NOT_IMPLEMENTED -> JarvisAlertRed
    }

    val typeColor = when (item.implementationType) {
        ImplementationType.ANDROID_NATIVE -> JarvisSuccessGreen
        ImplementationType.UI_ONLY -> JarvisCyan
        ImplementationType.BACKEND_SERVICE -> JarvisArcGold
    }

    HudFrameContainer(
        title = item.name,
        statusTag = item.category,
        accentColor = statusColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = item.description,
                style = Typography.bodyMedium,
                fontSize = 12.sp,
                color = JarvisTextPrimary
            )

            // Metadata Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Implementation Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.implementationType.label,
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        color = typeColor
                    )
                }

                // UI Visibility Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (item.visibleInUi) JarvisCyan.copy(alpha = 0.15f) else JarvisTextMuted.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (item.visibleInUi) JarvisCyan.copy(alpha = 0.4f) else JarvisTextMuted.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.visibleInUi) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (item.visibleInUi) JarvisCyan else JarvisTextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (item.visibleInUi) "UI: YES" else "UI: NO",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (item.visibleInUi) JarvisCyan else JarvisTextMuted
                        )
                    }
                }

                // Functional Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.functionalStatus.label,
                        style = Typography.labelSmall,
                        fontSize = 9.sp,
                        color = statusColor
                    )
                }
            }

            // Location & How to Access preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOC: ${item.locationScreen}",
                    style = Typography.labelSmall,
                    fontSize = 9.sp,
                    color = JarvisTextMuted
                )

                Text(
                    text = "TAP FOR FULL DETAILS ➔",
                    style = Typography.labelSmall,
                    fontSize = 9.sp,
                    color = JarvisCyan
                )
            }
        }
    }
}

@Composable
fun SystemDirectoryDetailDialog(
    item: SystemDirectoryItem,
    onDismiss: () -> Unit,
    onNavigate: (Int) -> Unit
) {
    val statusColor = when (item.functionalStatus) {
        FunctionalStatus.FUNCTIONAL -> JarvisSuccessGreen
        FunctionalStatus.PARTIAL -> JarvisArcGold
        FunctionalStatus.NOT_IMPLEMENTED -> JarvisAlertRed
    }

    val typeColor = when (item.implementationType) {
        ImplementationType.ANDROID_NATIVE -> JarvisSuccessGreen
        ImplementationType.UI_ONLY -> JarvisCyan
        ImplementationType.BACKEND_SERVICE -> JarvisArcGold
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = Typography.titleMedium,
                        fontSize = 16.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "CATEGORY: ${item.category}",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisArcGold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = JarvisTextMuted
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Description Card
                item {
                    Text(
                        text = "DESCRIPTION",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = item.description,
                        style = Typography.bodyMedium,
                        fontSize = 12.sp,
                        color = JarvisTextPrimary
                    )
                }

                // Status & Readiness
                item {
                    Text(
                        text = "CURRENT OPERATING STATUS",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = item.status,
                        style = Typography.bodyMedium,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Subsystem Specifications Matrix
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisSurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, JarvisBorderActive.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailRow(label = "VISIBLE IN UI", value = if (item.visibleInUi) "YES (DIRECTLY ACCESSIBLE)" else "NO (INTERNAL SUBSYSTEM)", valueColor = if (item.visibleInUi) JarvisCyan else JarvisTextMuted)
                        DetailRow(label = "FUNCTIONALITY", value = item.functionalStatus.label, valueColor = statusColor)
                        DetailRow(label = "FRAMEWORK LAYER", value = item.implementationType.label, valueColor = typeColor)
                        DetailRow(
                            label = "REQUIRED PERMISSIONS",
                            value = if (item.requiredPermissions.isEmpty()) "NONE (LOCAL EXECUTION)" else item.requiredPermissions.joinToString(", "),
                            valueColor = if (item.requiredPermissions.isEmpty()) JarvisSuccessGreen else JarvisArcGold
                        )
                        DetailRow(label = "SOURCE / LOCATION", value = item.locationScreen, valueColor = JarvisTextPrimary)
                    }
                }

                // Step-by-Step "HOW TO ACCESS IT"
                item {
                    Text(
                        text = "HOW TO ACCESS & TRIGGER",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisArcGold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisSurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, JarvisArcGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = item.howToAccess,
                            style = Typography.bodyMedium,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )
                    }
                }

                // Tags
                if (item.tags.isNotEmpty()) {
                    item {
                        Text(
                            text = "SEARCH TAGS",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = JarvisTextMuted
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(JarvisSurfaceCard)
                                        .border(1.dp, JarvisBorderActive.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = Typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = JarvisCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val targetScreen = when {
                item.locationScreen.contains("MainHud", ignoreCase = true) -> 0
                item.locationScreen.contains("Protocol", ignoreCase = true) -> 1
                item.locationScreen.contains("Vision", ignoreCase = true) -> 2
                item.locationScreen.contains("Telemetry", ignoreCase = true) -> 3
                item.locationScreen.contains("Vault", ignoreCase = true) || item.locationScreen.contains("Memory", ignoreCase = true) -> 4
                else -> null
            }

            if (targetScreen != null) {
                Button(
                    onClick = { onNavigate(targetScreen) },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid)
                ) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JUMP TO SCREEN", style = Typography.labelMedium)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid)
                ) {
                    Text("ACKNOWLEDGE", style = Typography.labelMedium)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = JarvisTextSecondary)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = Typography.labelSmall,
            fontSize = 9.sp,
            color = JarvisTextMuted
        )
        Text(
            text = value,
            style = Typography.bodyMedium,
            fontSize = 11.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}
