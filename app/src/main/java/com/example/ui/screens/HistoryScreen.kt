package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScanEntity
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.BotanicalTopAppBar
import com.example.ui.components.EmptyBotanicalState
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val allScans by viewModel.allScans.collectAsState()

    var showClearAllDialog by remember { mutableStateOf(false) }

    val filteredScans = remember(allScans, uiState.historySearchQuery, uiState.activeFilter) {
        allScans.filter { scan ->
            val query = uiState.historySearchQuery.trim()
            val matchesQuery = query.isBlank() ||
                    scan.title.contains(query, ignoreCase = true) ||
                    scan.brandName.contains(query, ignoreCase = true) ||
                    scan.correctedText.contains(query, ignoreCase = true) ||
                    scan.aiHeadline.contains(query, ignoreCase = true) ||
                    scan.allergensDetectedJson.contains(query, ignoreCase = true)

            val matchesFilter = when (uiState.activeFilter) {
                "concerns" -> scan.safetyTier == "WARNING" || scan.safetyTier == "HIGH_RISK" || scan.summaryStatus == "CONCERNS_IDENTIFIED"
                "clean" -> scan.safetyTier == "SAFE" || scan.summaryStatus == "NO_RELEVANT_CONCERN"
                "unknowns" -> scan.summaryStatus == "COMPLETED_WITH_UNKNOWNS" || scan.unmatchedCount > 0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("history_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BoneLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SmokyBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Scan History",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            com.example.ui.util.PdfExportHelper.exportAndShareHistoryPdf(
                                context = context,
                                scans = filteredScans.ifEmpty { allScans },
                                onError = { viewModel.showToast(it) }
                            )
                        }
                        .testTag("export_history_pdf_button"),
                    color = SmokyBlack,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export PDF Report",
                            tint = FloralWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Export PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = FloralWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (allScans.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("app_bar_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = SmokyBlack
                        )
                    }
                }
            }
        }

        // Search Bar with Clear Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = uiState.historySearchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = {
                    Text(
                        "Search by product, ingredient, or allergen...",
                        color = OliveDrabMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OliveDrab
                    )
                },
                trailingIcon = {
                    if (uiState.historySearchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = OliveDrab
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScanOrange,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedTextColor = SmokyBlack,
                    unfocusedTextColor = SmokyBlack
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Filter Pills Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "all" to "All Scans (${allScans.size})",
                "concerns" to "Concerns Found",
                "clean" to "Safe & Clean",
                "unknowns" to "With Unknowns"
            )
            items(filters) { (id, label) ->
                val isSelected = uiState.activeFilter == id
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, if (isSelected) ScanOrange else CardBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setActiveFilter(id) }
                        .testTag("filter_chip_$id"),
                    color = if (isSelected) ScanOrange else CardSurface
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) OnScanOrange else SmokyBlack,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        if (uiState.isDataLoading) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .testTag("history_skeleton_list"),
                contentPadding = PaddingValues(top = 6.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(4) { index ->
                    com.example.ui.components.SkeletonCard(
                        height = 122.dp,
                        cornerRadius = 16.dp,
                        showLeadingCircle = true,
                        showBottomPills = true,
                        testTag = if (index == 0) "skeleton_card" else "skeleton_card_$index"
                    )
                }
            }
        } else if (filteredScans.isEmpty()) {
            EmptyBotanicalState(
                icon = Icons.Default.History,
                title = if (allScans.isEmpty()) "No Scans Yet" else "No matching scans",
                message = if (uiState.historySearchQuery.isNotBlank()) {
                    "No previous records match \"${uiState.historySearchQuery}\". Try searching for another ingredient."
                } else if (allScans.isEmpty()) {
                    "Scan your first product label to build your personal evidence-backed history log."
                } else {
                    "No records match the active filter criteria."
                },
                actionLabel = if (allScans.isEmpty()) "Scan a Product" else "Show All Scans",
                onActionClick = {
                    if (allScans.isEmpty()) {
                        viewModel.navigateTo(Screen.Scanner)
                    } else {
                        viewModel.setHistorySearchQuery("")
                        viewModel.setActiveFilter("all")
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScans, key = { it.id }) { scan ->
                    HistoryItemCard(
                        scan = scan,
                        onClick = { viewModel.openReport(scan.id) },
                        onToggleSaved = { viewModel.toggleSavedScan(scan.id, scan.isSaved) },
                        onDelete = { viewModel.confirmDeleteScan(scan.id) }
                    )
                }
            }
        }
    }

    // Confirmation dialog for single scan deletion
    if (uiState.deleteConfirmationScanId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.confirmDeleteScan(null) },
            title = {
                Text(
                    "Delete Scan Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this scan and its saved evidence report? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmokyBlack
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticCritical)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.confirmDeleteScan(null) }) {
                    Text("Cancel", color = SmokyBlack)
                }
            },
            containerColor = FloralWhite
        )
    }

    // Confirmation dialog for clearing all history
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text(
                    "Clear Entire History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
            },
            text = {
                Text(
                    "This will permanently delete all ${allScans.size} scan records and ingredient analyses stored on your device. Continue?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmokyBlack
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllDialog = false
                        viewModel.clearAllHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticCritical)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            },
            containerColor = FloralWhite
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryItemCard(
    scan: ScanEntity,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(scan.createdAt) { dateFormat.format(Date(scan.createdAt)) }

    // Parse detected allergens JSON
    val allergensList = remember(scan.allergensDetectedJson) {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(scan.allergensDetectedJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (e: Exception) {
            // ignore
        }
        list
    }

    val (tierBg, tierFg, tierLabel) = when (scan.safetyTier) {
        "HIGH_RISK" -> Triple(SemanticCriticalBg, SemanticCritical, "High Risk")
        "WARNING" -> Triple(SemanticCautionBg, SemanticCaution, "Use with Caution")
        else -> Triple(SemanticSuccessBg, SemanticSuccess, "Safe Formulation")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("history_item_${scan.id}"),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Safety badge + action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(tierFg)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${scan.safetyScore}/100 • $tierLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = tierFg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSaved,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (scan.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (scan.isSaved) "Remove from watchlist" else "Save to watchlist",
                            tint = if (scan.isSaved) SmokyBlack else OliveDrabMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = OliveDrabMuted,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Category
            Text(
                text = scan.title,
                style = MaterialTheme.typography.titleMedium,
                color = SmokyBlack,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "${scan.brandName} • ${scan.category.replaceFirstChar { it.uppercase() }} • $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = OliveDrabMuted
            )

            // Detected Allergen Badges
            if (allergensList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allergensList.take(3).forEach { allergen ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SemanticCautionBg,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, SemanticCaution.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = SemanticCaution,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = allergen,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SmokyBlack,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    if (allergensList.size > 3) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BoneLight
                        ) {
                            Text(
                                text = "+${allergensList.size - 3} more",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrab,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ingredients count & hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${scan.matchedCount} ingredients evaluated",
                    style = MaterialTheme.typography.bodySmall,
                    color = OliveDrab
                )
                Text(
                    text = "Tap to inspect report →",
                    style = MaterialTheme.typography.labelSmall,
                    color = SmokyBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
