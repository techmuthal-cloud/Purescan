package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanReport
import com.example.data.model.SummaryStatus
import com.example.ui.ScanViewModel
import com.example.ui.Screen
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchReportScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.currentBatchSummary
    var selectedFilter by remember { mutableStateOf("all") } // "all", "allergen_free", "flagged"

    if (summary == null || summary.reports.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FloralWhite)
                .padding(24.dp)
                .testTag("batch_report_empty"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Batch Scan Results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SmokyBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Capture multiple product labels in Batch Mode to compare their safety verdicts and allergen matches side by side.",
                style = MaterialTheme.typography.bodyMedium,
                color = OliveDrabMuted
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.startBatchScanFromHome() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScanOrange,
                    contentColor = OnScanOrange
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Start Batch Scan", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val rankedReports = remember(summary.reports, selectedFilter) {
        val sorted = summary.reports.sortedByDescending { report ->
            report.geminiInsight?.safetyScore ?: when (report.summaryStatus) {
                SummaryStatus.NO_RELEVANT_CONCERN -> 90
                SummaryStatus.COMPLETED_WITH_UNKNOWNS -> 62
                SummaryStatus.CONCERNS_IDENTIFIED -> 28
            }
        }
        when (selectedFilter) {
            "allergen_free" -> sorted.filter { report ->
                report.preferenceNotes.isEmpty() &&
                    (report.geminiInsight?.allergensDetected?.isEmpty() != false) &&
                    report.ingredients.none { it.matchedAllergenName != null }
            }
            "flagged" -> sorted.filter { report ->
                report.preferenceNotes.isNotEmpty() ||
                    report.geminiInsight?.allergensDetected?.isNotEmpty() == true ||
                    report.ingredients.any { it.matchedAllergenName != null } ||
                    report.summaryStatus == SummaryStatus.CONCERNS_IDENTIFIED
            }
            else -> sorted
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("batch_report_screen")
    ) {
        // Top Editorial Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceCream)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("batch_report_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = SmokyBlack
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Batch Scan Report",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SmokyBlack
                    )
                    Text(
                        text = "${summary.reports.size} products analyzed collectively",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDrabMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SurfaceCream,
                modifier = Modifier
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewModel.startBatchScanFromHome() }
                    .testTag("batch_scan_more_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = ScanOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan More",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Aggregate Summary Hero Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .testTag("batch_summary_card"),
                    color = CardSurface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Collective Aisle Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SmokyBlack
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceCream,
                                modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = "${summary.reports.size} Scanned",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SmokyBlack,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BatchMetricTile(
                                modifier = Modifier.weight(1f),
                                count = summary.safeCount,
                                label = "Safe",
                                accentColor = SemanticSuccess,
                                bgColor = SemanticSuccessBg
                            )
                            BatchMetricTile(
                                modifier = Modifier.weight(1f),
                                count = summary.warningCount,
                                label = "Caution",
                                accentColor = SemanticCaution,
                                bgColor = SemanticCautionBg
                            )
                            BatchMetricTile(
                                modifier = Modifier.weight(1f),
                                count = summary.highRiskCount,
                                label = "High Risk",
                                accentColor = SemanticCritical,
                                bgColor = SemanticCriticalBg
                            )
                            BatchMetricTile(
                                modifier = Modifier.weight(1f),
                                count = summary.totalAllergenHits,
                                label = "Allergen Hits",
                                accentColor = if (summary.totalAllergenHits > 0) SemanticCritical else OliveDrab,
                                bgColor = if (summary.totalAllergenHits > 0) SemanticCriticalBg else BoneLight
                            )
                        }
                    }
                }
            }

            // 2. Collective Allergen Alert Banner (if any product triggered the user's Allergy Filter)
            if (summary.totalAllergenHits > 0) {
                item {
                    val allergenMatchedReports = summary.reports.filter {
                        it.preferenceNotes.isNotEmpty() ||
                            it.geminiInsight?.allergensDetected?.isNotEmpty() == true ||
                            it.ingredients.any { ing -> ing.matchedAllergenName != null }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, SemanticCritical.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .testTag("batch_allergen_alert_banner"),
                        color = SemanticCriticalBg,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = SemanticCritical,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Allergy Filter Alert: ${allergenMatchedReports.size} of ${summary.reports.size} Products Flagged",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticCritical
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ingredients matching your personal Allergy Filter were detected in: " +
                                    allergenMatchedReports.joinToString(", ") { it.productTitle } + ".",
                                style = MaterialTheme.typography.bodySmall,
                                color = SmokyBlack
                            )
                        }
                    }
                }
            }

            // 3. Safest Pick Spotlight Card
            summary.safestReport?.let { safest ->
                item {
                    val safestScore = safest.geminiInsight?.safetyScore ?: 88
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, SemanticSuccess.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.openReportFromBatch(safest.scanId) }
                            .testTag("batch_safest_pick_card"),
                        color = SemanticSuccessBg.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SemanticSuccess
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SAFEST PICK IN BATCH",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FloralWhite,
                                    modifier = Modifier.border(1.dp, SemanticSuccess.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = "Score $safestScore / 100",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SemanticSuccess,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = safest.productTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SmokyBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = safest.geminiInsight?.subheadline ?: safest.limitationsNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = SmokyBlack.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Inspect Safest Product Report",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = SemanticSuccess,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Filter Chips + Compare Top 2 Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "all" to "All (${summary.reports.size})",
                            "allergen_free" to "Allergen-Free",
                            "flagged" to "Flagged"
                        ).forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) SmokyBlack else SurfaceCream,
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) SmokyBlack else CardBorder, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedFilter = key }
                                    .testTag("batch_filter_$key")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) FloralWhite else SmokyBlack,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    if (summary.reports.size >= 2) {
                        val sorted = summary.reports.sortedByDescending { it.geminiInsight?.safetyScore ?: 50 }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ScanOrange,
                            modifier = Modifier
                                .border(1.dp, SmokyBlack, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.compareBatchProducts(sorted[0].scanId, sorted[1].scanId)
                                }
                                .testTag("batch_compare_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = OnScanOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Compare",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnScanOrange
                                )
                            }
                        }
                    }
                }
            }

            // 5. Ranked Product Cards
            itemsIndexed(rankedReports, key = { _, r -> r.scanId }) { index, report ->
                BatchProductReportCard(
                    rank = index + 1,
                    report = report,
                    onInspect = { viewModel.openReportFromBatch(report.scanId) }
                )
            }
        }
    }
}

@Composable
private fun BatchMetricTile(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    accentColor: Color,
    bgColor: Color
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = SmokyBlack,
                fontSize = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BatchProductReportCard(
    rank: Int,
    report: ScanReport,
    onInspect: () -> Unit
) {
    val score = report.geminiInsight?.safetyScore ?: when (report.summaryStatus) {
        SummaryStatus.NO_RELEVANT_CONCERN -> 90
        SummaryStatus.COMPLETED_WITH_UNKNOWNS -> 62
        SummaryStatus.CONCERNS_IDENTIFIED -> 28
    }
    val tier = report.geminiInsight?.safetyTier ?: when (report.summaryStatus) {
        SummaryStatus.NO_RELEVANT_CONCERN -> "SAFE"
        SummaryStatus.CONCERNS_IDENTIFIED -> "HIGH_RISK"
        SummaryStatus.COMPLETED_WITH_UNKNOWNS -> "WARNING"
    }
    val (tierColor, tierBg, tierLabel) = when (tier.uppercase()) {
        "SAFE" -> Triple(SemanticSuccess, SemanticSuccessBg, "Safe")
        "HIGH_RISK" -> Triple(SemanticCritical, SemanticCriticalBg, "High Risk")
        else -> Triple(SemanticCaution, SemanticCautionBg, "Caution")
    }

    val matchedAllergens = remember(report) {
        (report.geminiInsight?.allergensDetected.orEmpty() +
            report.ingredients.mapNotNull { it.matchedAllergenName })
            .distinct()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (matchedAllergens.isNotEmpty()) 1.5.dp else 1.dp,
                color = if (matchedAllergens.isNotEmpty()) SemanticCritical.copy(alpha = 0.55f) else CardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onInspect() }
            .testTag("batch_product_card_${report.scanId}"),
        color = CardSurface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tierBg)
                            .border(1.5.dp, tierColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = tierColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#$rank • ${report.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrabMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = report.productTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SmokyBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tierBg,
                    modifier = Modifier.border(1.dp, tierColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = tierLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.geminiInsight?.subheadline ?: report.limitationsNote,
                style = MaterialTheme.typography.bodySmall,
                color = SmokyBlack.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (matchedAllergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    matchedAllergens.forEach { allergen ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SemanticCriticalBg,
                            modifier = Modifier.border(1.dp, SemanticCritical.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SemanticCritical,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Allergen: $allergen",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticCritical
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${report.ingredients.size} ingredients • ${report.findings.size} findings",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveDrabMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("batch_inspect_${report.scanId}")
                ) {
                    Text(
                        text = "Inspect Report",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScanOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ScanOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
