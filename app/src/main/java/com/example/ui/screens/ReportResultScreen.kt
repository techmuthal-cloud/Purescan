package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiIngredientInsight
import com.example.data.api.GeminiService
import com.example.data.model.ParsedIngredient
import com.example.data.model.ReportFinding
import com.example.data.model.ScanReport
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.AllergenWarningBar
import com.example.ui.components.FindingCard
import com.example.ui.components.IngredientChip
import com.example.ui.components.toSafetyBadgeSpec
import com.example.ui.theme.Bone
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
import com.example.ui.util.PdfExportHelper

/**
 * Screens 6, 7, 8 (Result Summary: Safe / Warning / High Risk)
 * & Screen 9 (Product Details with Overview, Ingredients, Safety, Sources tabs).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportResultScreen(viewModel: ScanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val savedScans by viewModel.savedScans.collectAsState()
    val context = LocalContext.current
    val report = uiState.currentReport

    BackHandler {
        if (uiState.showFullProductDetails) {
            viewModel.setShowFullProductDetails(false)
        } else {
            viewModel.navigateTo(Screen.Home)
        }
    }

    if (report == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No report selected", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val isSaved = savedScans.any { it.id == report.scanId }
    val gemini = remember(report, uiState.selectedLanguage) {
        val raw = report.geminiInsight ?: GeminiService.generateFallbackInsight(
            report.correctedText,
            report.category,
            languageCode = uiState.selectedLanguage.code
        )
        GeminiService.localizeInsightForLanguage(raw, uiState.selectedLanguage.code)
    }

    var showShareModal by remember { mutableStateOf(false) }
    var inspectedFinding by remember { mutableStateOf<ReportFinding?>(null) }
    var inspectedIngredient by remember { mutableStateOf<ParsedIngredient?>(null) }

    if (uiState.showLanguageDialog) {
        com.example.ui.components.LanguageSelectorDialog(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.setAppLanguage(it) },
            onDismiss = { viewModel.setShowLanguageDialog(false) }
        )
    }

    if (!uiState.showFullProductDetails) {
        // SCREENS 6, 7, 8: Verdict Result Summary Screen
        VerdictSummaryView(
            report = report,
            gemini = gemini,
            language = uiState.selectedLanguage,
            onLanguageClick = { viewModel.setShowLanguageDialog(true) },
            onBackClick = { viewModel.navigateTo(Screen.Home) },
            onShareClick = { showShareModal = true },
            onViewDetailsClick = { viewModel.setShowFullProductDetails(true) },
            onCompareClick = { viewModel.navigateTo(Screen.CompareProducts) },
            onEditTextClick = { viewModel.editCurrentReportText() },
            onReanalyzeOcrText = { viewModel.reanalyzeCurrentReportWithText(it) },
            onInspectIngredient = { inspectedIngredient = it }
        )
    } else {
        // SCREEN 9: Product Details Screen (Overview, Ingredients, Safety, Sources)
        ProductDetailsView(
            report = report,
            gemini = gemini,
            isSaved = isSaved,
            onBackClick = { viewModel.setShowFullProductDetails(false) },
            onToggleSaved = { viewModel.toggleSaveCurrentReport() },
            onShareClick = { showShareModal = true },
            onCompareClick = { viewModel.navigateTo(Screen.CompareProducts) },
            onEditTextClick = { viewModel.editCurrentReportText() },
            onInspectFinding = { inspectedFinding = it },
            onInspectIngredient = { inspectedIngredient = it }
        )
    }

    // Share Summary Modal (Visual Infographic Card PNG, Clipboard Copy, Messaging Text, and System Share Sheet)
    if (showShareModal) {
        com.example.ui.components.ShareSummaryModal(
            report = report,
            gemini = gemini,
            onDismiss = { showShareModal = false }
        )
    }

    // Modal Sheet: In-depth Finding Evidence
    if (inspectedFinding != null) {
        val finding = inspectedFinding!!
        ModalBottomSheet(
            onDismissRequest = { inspectedFinding = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = FloralWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = finding.sourceOrg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OliveDrab
                    )
                    IconButton(onClick = { inspectedFinding = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = finding.ingredientName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = SmokyBlack
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    color = CardSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Scientific Evaluation",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OliveDrabMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = finding.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SmokyBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FindingMetadataRow(label = "Category", value = finding.category)
                FindingMetadataRow(label = "Evidence Strength", value = finding.evidenceStrength)
                FindingMetadataRow(label = "Jurisdiction", value = finding.jurisdiction)
                FindingMetadataRow(label = "Review Date", value = finding.reviewDate)
                if (finding.conditions.isNotBlank()) {
                    FindingMetadataRow(label = "Usage Condition", value = finding.conditions)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { inspectedFinding = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyBlack)
                ) {
                    Text("Close Evidence Inspector", color = FloralWhite)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal Sheet: In-depth Ingredient Detail
    if (inspectedIngredient != null) {
        val ingredient = inspectedIngredient!!
        val categorizedMap = remember(report, gemini) {
            com.example.ui.components.categorizeReportIngredients(report, gemini)
                .associateBy { it.rawLabelText.trim().lowercase() }
        }
        val matchedCatEntry = categorizedMap[ingredient.rawText.trim().lowercase()]
        val modalBadgeSpec = remember(ingredient, matchedCatEntry) {
            ingredient.toSafetyBadgeSpec(matchedCatEntry)
        }
        ModalBottomSheet(
            onDismissRequest = { inspectedIngredient = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = FloralWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.ui.components.IngredientSafetyBadge(
                        spec = modalBadgeSpec,
                        compact = false
                    )
                    IconButton(onClick = { inspectedIngredient = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ingredient.canonicalIngredient?.canonicalName ?: ingredient.rawText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = SmokyBlack
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Raw Text on Package: \"${ingredient.rawText}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = OliveDrabMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                com.example.ui.components.IngredientSafetyTooltipCard(
                    spec = modalBadgeSpec,
                    visible = true,
                    onDismiss = { inspectedIngredient = null }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    color = CardSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Functional Role & Details",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OliveDrabMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ingredient.canonicalIngredient?.description
                                ?: "This ingredient was transcribed from your label scan. Tap Safety or Sources tabs to inspect regulatory thresholds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SmokyBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { inspectedIngredient = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyBlack)
                ) {
                    Text("Close Details", color = FloralWhite)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Matches Screens 6, 7, 8 in the uploaded reference mockup:
 * - Screen 6: Result - Safe ("This Product Appears Safe", green leaf emblem, Highlights with 3 green checks, "View Full Details" button)
 * - Screen 7: Result - Warning ("Use with Caution", amber warning triangle emblem, Key Concerns with amber triangles, "View Details" button)
 * - Screen 8: Result - High Risk ("High Risk / Possible Harmful Ingredients", red exclamation emblem, Key Concerns with red exclamations, "View Details" button)
 */
@Composable
private fun VerdictSummaryView(
    report: ScanReport,
    gemini: GeminiIngredientInsight,
    language: com.example.ui.util.AppLanguage = com.example.ui.util.AppLanguage.ENGLISH,
    onLanguageClick: () -> Unit = {},
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    onCompareClick: () -> Unit = {},
    onEditTextClick: () -> Unit = {},
    onReanalyzeOcrText: (String) -> Unit = {},
    onInspectIngredient: ((ParsedIngredient) -> Unit)? = null
) {
    val tier = gemini.safetyTier.uppercase()
    val hasUnverifiedTokens = report.unmatchedIngredients.isNotEmpty()
    val isSafe = tier == "SAFE" && !hasUnverifiedTokens
    val isHighRisk = tier == "HIGH_RISK"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("report_result_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top App Bar: Back arrow on left, Language Selector + Share icon on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SmokyBlack
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.ui.components.LanguageSelectorPill(
                    selectedLanguage = language,
                    onClick = onLanguageClick
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share or Export PDF",
                        tint = SmokyBlack
                    )
                }
            }
        }

        // Scrollable Center Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Curved Soft Arch Status Header matching Screens 11, 12, 13
                val archBg = when {
                    isHighRisk -> SemanticCriticalBg
                    !isSafe -> SemanticCautionBg
                    else -> SemanticSuccessBg
                }
                val accentColor = when {
                    isHighRisk -> SemanticCritical
                    !isSafe -> SemanticCaution
                    else -> SemanticSuccess
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 44.dp, bottomEnd = 44.dp))
                        .border(
                            1.dp,
                            accentColor.copy(alpha = 0.25f),
                            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 44.dp, bottomEnd = 44.dp)
                        ),
                    color = archBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(CardSurface)
                                .border(2.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isHighRisk -> Icons.Default.PriorityHigh
                                    !isSafe -> Icons.Default.Warning
                                    else -> Icons.Default.Eco
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (language != com.example.ui.util.AppLanguage.ENGLISH) {
                                gemini.headline
                            } else {
                                when {
                                    isHighRisk -> "High Risk /\nPossible Harmful\nIngredients"
                                    hasUnverifiedTokens && tier == "SAFE" -> "Inconclusive —\nUnverified Tokens"
                                    !isSafe -> "Use with Caution"
                                    else -> "Compatible with\nYour Selected Rules"
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 32.sp
                            ),
                            color = accentColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = gemini.subheadline,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                lineHeight = 20.sp
                            ),
                            color = SmokyBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Product Card (Screens 11, 12, 13)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .clickable { onViewDetailsClick() },
                    color = CardSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductBottleThumbnail(modifier = Modifier.size(58.dp))

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.productTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = SmokyBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Brand: ${gemini.brandName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OliveDrab
                            )
                            Text(
                                text = "Category: ${report.category.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OliveDrabMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // P0 & Section 4 UX Review: 4-Question Clinical Trust & Evidence Summary Card
                val matchedRuleSummaries = remember(report.preferenceNotes, gemini.allergensDetected) {
                    if (report.preferenceNotes.isNotEmpty()) {
                        report.preferenceNotes.map { "${it.triggeredIngredient} → ${it.prefLabel}: ${it.note}" }
                    } else {
                        gemini.allergensDetected
                    }
                }
                val citationSummaries = remember(gemini.groundingCitations, report.findings) {
                    if (gemini.groundingCitations.isNotEmpty()) {
                        gemini.groundingCitations.map { "${it.agency}: ${it.title} — ${it.summary}" }
                    } else {
                        report.findings.map { "${it.sourceOrg}: ${it.ingredientName} (${it.evidenceStrength})" }
                    }
                }

                com.example.ui.components.TrustResultSummaryCard(
                    recognizedCount = report.ingredients.size.coerceAtLeast(gemini.ingredientBreakdowns.size),
                    ocrConfidence = gemini.ocrConfidence,
                    matchedRules = matchedRuleSummaries,
                    regulatoryNotes = citationSummaries,
                    language = language,
                    onReviewOcrClick = onEditTextClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                val categorizedEntries = remember(report, gemini) {
                    com.example.ui.components.categorizeReportIngredients(report, gemini)
                }
                val possibleDerivatives = remember(categorizedEntries) {
                    categorizedEntries
                        .filter { it.matchCertainty == com.example.ui.util.MatchCertainty.POSSIBLE_DERIVATIVE_MATCH }
                        .map { it.displayName }
                        .distinct()
                }

                com.example.ui.components.UncertaintyFindingsBanner(
                    unmatchedTokens = report.unmatchedIngredients.map { it.rawText },
                    possibleDerivativeTokens = possibleDerivatives,
                    ocrConfidence = gemini.ocrConfidence,
                    currentIngredientsText = report.correctedText,
                    onReanalyzeOcrText = onReanalyzeOcrText,
                    onOpenFullTextEditor = onEditTextClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Personal Allergen Alert Banner if triggered
            val combinedAllergens = (gemini.allergensDetected + report.ingredients.mapNotNull { it.matchedAllergenName }).distinct()
            if (combinedAllergens.isNotEmpty()) {
                item {
                    AllergenWarningBar(
                        allergens = combinedAllergens,
                        hasHighRiskAllergen = isHighRisk
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Highlights / Key Concerns Section (Exact match to Screens 6, 7, 8)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (isSafe) "Highlights" else "Key Concerns",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = SmokyBlack
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val itemsList = if (isSafe) {
                        gemini.highlights.ifEmpty {
                            listOf(
                                "No banned substances found",
                                "Complies with known regulations",
                                "No high-risk ingredients detected"
                            )
                        }
                    } else {
                        gemini.keyConcerns.ifEmpty {
                            listOf(
                                "Contains fragrance (potential skin irritation)",
                                "Includes preservatives with moderate risk",
                                "May not suit sensitive skin"
                            )
                        }
                    }

                    itemsList.forEachIndexed { idx, point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when {
                                isHighRisk -> {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFB94032),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                !isSafe -> {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFB8791A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF3E6B48),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = SmokyBlack
                            )
                        }
                        if (idx < itemsList.lastIndex) {
                            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Safer Alternatives Section when product is Warning or High Risk
                if (gemini.saferAlternatives.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Safer Alternatives",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = SmokyBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        gemini.saferAlternatives.forEach { alt ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                color = Color(0xFFDCE8DC).copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${alt.productName} • ${alt.brandName}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F4D32)
                                        )
                                        Text(
                                            text = alt.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF2E5738)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2E6B47)
                                    ) {
                                        Text(
                                            text = "${alt.safetyScore}/100",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Material 3 Categorized Ingredient Cards ('Safe', 'Warning', 'High Hazard') with Rule Badges, Certainty Tags & Expandable Citations
                com.example.ui.components.CategorizedIngredientResultsSection(
                    report = report,
                    gemini = gemini,
                    defaultShowInlineToxicology = true,
                    onInspectIngredient = onInspectIngredient
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Actions: Compare Product & Edit / Paste Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCompareClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("verdict_compare_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Compare",
                            style = MaterialTheme.typography.labelMedium,
                            color = SmokyBlack
                        )
                    }
                    OutlinedButton(
                        onClick = onEditTextClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("verdict_edit_text_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Edit OCR Text (${gemini.ocrConfidence}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = SmokyBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Actions: "View Full Details" & "Share Summary Report"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onViewDetailsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("view_details_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScanOrange,
                    contentColor = OnScanOrange
                )
            ) {
                Text(
                    text = if (isSafe) "View Full Details" else "View Details",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("share_report_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SmokyBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Summary Report",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Screen 9: Product Details Screen
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailsView(
    report: ScanReport,
    gemini: GeminiIngredientInsight,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onToggleSaved: () -> Unit,
    onShareClick: () -> Unit,
    onCompareClick: () -> Unit = {},
    onEditTextClick: () -> Unit = {},
    onInspectFinding: (ReportFinding) -> Unit,
    onInspectIngredient: (ParsedIngredient) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var ingredientRiskFilter by remember { mutableStateOf("ALL") }
    val tabs = listOf("Overview", "Ingredients", "Safety", "Sources")
    val isSafe = gemini.safetyTier.uppercase() == "SAFE"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("product_details_screen")
    ) {
        // Top App Bar: Back arrow on left, Heart & Share/PDF on right (Matches Screen 9)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("details_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Summary",
                    tint = SmokyBlack
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("details_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share or Export PDF",
                        tint = SmokyBlack
                    )
                }
                IconButton(
                    onClick = onToggleSaved,
                    modifier = Modifier.testTag("favorite_button")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from Favorites" else "Save to Favorites",
                        tint = if (isSaved) Color(0xFFB94032) else SmokyBlack
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
        ) {
            // Product Header Section (Matches Screen 9)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductBottleThumbnail(modifier = Modifier.size(78.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.productTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                lineHeight = 27.sp
                            ),
                            color = SmokyBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Brand: ${gemini.brandName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OliveDrab
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceCream
                            ) {
                                Text(
                                    text = report.category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SmokyBlack,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceCream
                            ) {
                                Text(
                                    text = if (report.category.equals("Moisturizer", ignoreCase = true)) "Skin Care" else "Moisturizer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SmokyBlack,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 4 Underline Tabs: Overview | Ingredients | Safety | Sources (Matches Screen 14)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = FloralWhite,
                    contentColor = SmokyBlack,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ScanOrange,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.testTag("details_tab_${title.lowercase()}"),
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) SmokyBlack else OliveDrabMuted
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            when (selectedTab) {
                // TAB 0: Overview (Matches Screen 14 layout)
                0 -> {
                    item {
                        val combinedAllergens = (gemini.allergensDetected + report.ingredients.mapNotNull { it.matchedAllergenName }).distinct()
                        if (combinedAllergens.isNotEmpty()) {
                            AllergenWarningBar(
                                allergens = combinedAllergens,
                                hasHighRiskAllergen = !isSafe && gemini.safetyTier.equals("HIGH_RISK", ignoreCase = true)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Two side-by-side status cards matching Screen 14
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                color = CardSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSafe) SemanticSuccessBg else SemanticCautionBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSafe) Icons.Default.Eco else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isSafe) SemanticSuccess else SemanticCaution,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isSafe) "Appears Safe" else "Caution",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SmokyBlack
                                        )
                                        Text(
                                            text = "Score: ${gemini.safetyScore}/100",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OliveDrabMuted
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                color = CardSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(BoneLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = OliveDrab,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${report.ingredients.size} Ingredients",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SmokyBlack
                                        )
                                        Text(
                                            text = "${report.region} Standards",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OliveDrabMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ScanOrange Pill CTA: "View Ingredient Analysis" (Matches Screen 14)
                        Button(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("view_ingredient_analysis_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ScanOrange,
                                contentColor = OnScanOrange
                            )
                        ) {
                            Text(
                                text = "View Ingredient Analysis",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // "About This Product" section (Matches Screen 9)
                        Text(
                            text = "About This Product",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = SmokyBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = gemini.scientificExplanation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 21.sp
                            ),
                            color = OliveDrab
                        )

                        if (gemini.safeUseTips.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceCream
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Recommended Safe Usage",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SmokyBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    gemini.safeUseTips.forEach { tip ->
                                        Text(
                                            text = "• $tip",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OliveDrab
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Bioscience & Toxicology Panel
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bioscience_toxicology_panel"),
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Bioscience & Toxicology Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = SmokyBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gemini.toxicologyProfile.mechanismSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FindingMetadataRow(
                                    label = "Dermal Bioavailability",
                                    value = gemini.toxicologyProfile.dermalBioavailability
                                )
                                FindingMetadataRow(
                                    label = "Bioaccumulation Potential",
                                    value = gemini.toxicologyProfile.bioaccumulationPotential
                                )
                                FindingMetadataRow(
                                    label = "Endocrine Disruption Risk",
                                    value = gemini.toxicologyProfile.endocrineDisruptionRisk
                                )
                                FindingMetadataRow(
                                    label = "Sensitization Index",
                                    value = gemini.toxicologyProfile.sensitizationIndex
                                )
                            }
                        }
                    }
                }

                // TAB 1: Ingredients with Regulatory Risk Filter Tabs & Per-Ingredient Breakdown
                1 -> {
                    item {
                        val categorizedEntries = remember(report, gemini) {
                            com.example.ui.components.categorizeReportIngredients(report, gemini)
                        }
                        val categorizedByRaw = remember(categorizedEntries) {
                            categorizedEntries.associateBy { it.rawLabelText.trim().lowercase() }
                        }
                        var activeChipTooltipSpec by remember {
                            mutableStateOf<com.example.ui.components.IngredientSafetyBadgeSpec?>(null)
                        }

                        val filteredIngredients = remember(report.ingredients, categorizedByRaw, ingredientRiskFilter) {
                            report.ingredients.filter { ing ->
                                val catEntry = categorizedByRaw[ing.rawText.trim().lowercase()]
                                val spec = ing.toSafetyBadgeSpec(catEntry)
                                when (ingredientRiskFilter) {
                                    "SAFE" -> spec.severity == com.example.ui.components.TrafficLightBadgeSeverity.GREEN_SAFE
                                    "CAUTION" -> spec.severity == com.example.ui.components.TrafficLightBadgeSeverity.YELLOW_CAUTION ||
                                        spec.severity == com.example.ui.components.TrafficLightBadgeSeverity.YELLOW_UNVERIFIED
                                    "HIGH_RISK" -> spec.severity == com.example.ui.components.TrafficLightBadgeSeverity.RED_HIGH_HAZARD
                                    else -> true
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Extracted Ingredients (${report.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SmokyBlack
                            )
                            Text(
                                text = "Edit Text",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SmokyBlack,
                                modifier = Modifier
                                    .clickable { onEditTextClick() }
                                    .testTag("details_edit_text_link")
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Filter by traffic-light safety tier or tap any ingredient's Safety Badge for a quick hazard summary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OliveDrabMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Regulatory Risk Filter Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "ALL" to "All",
                                "SAFE" to "Safe",
                                "CAUTION" to "Caution",
                                "HIGH_RISK" to "High Risk"
                            ).forEach { (filterKey, filterLabel) ->
                                val selected = ingredientRiskFilter == filterKey
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) ScanOrange else CardSurface,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (selected) ScanOrange else CardBorder,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { ingredientRiskFilter = filterKey }
                                        .testTag("ingredient_risk_tab_${filterKey.lowercase()}")
                                ) {
                                    Text(
                                        text = filterLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) OnScanOrange else SmokyBlack,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredIngredients.forEach { ing ->
                                val catEntry = categorizedByRaw[ing.rawText.trim().lowercase()]
                                IngredientChip(
                                    ingredient = ing,
                                    categorizedEntry = catEntry,
                                    onClick = { onInspectIngredient(ing) },
                                    onBadgeClick = { spec ->
                                        activeChipTooltipSpec = if (activeChipTooltipSpec?.ingredientKey == spec.ingredientKey) {
                                            null
                                        } else {
                                            spec
                                        }
                                    }
                                )
                            }
                        }

                        activeChipTooltipSpec?.let { tooltipSpec ->
                            com.example.ui.components.IngredientSafetyTooltipCard(
                                spec = tooltipSpec,
                                visible = true,
                                onDismiss = { activeChipTooltipSpec = null },
                                onInspectFullDetails = {
                                    val target = report.ingredients.firstOrNull {
                                        (it.canonicalIngredient?.canonicalName ?: it.rawText).equals(tooltipSpec.ingredientName, ignoreCase = true)
                                    }
                                    if (target != null) {
                                        onInspectIngredient(target)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        com.example.ui.components.CategorizedIngredientResultsSection(
                            report = report,
                            gemini = gemini,
                            defaultShowInlineToxicology = true,
                            onInspectIngredient = onInspectIngredient
                        )
                    }
                }

                // TAB 2: Safety Findings
                2 -> {
                    if (report.findings.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                color = SurfaceCream
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SemanticSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "No safety restrictions or bans found in checked regulatory databases.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SmokyBlack
                                    )
                                }
                            }
                        }
                    } else {
                        items(report.findings) { finding ->
                            FindingCard(
                                finding = finding,
                                onClick = { onInspectFinding(finding) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                // TAB 3: Sources & Live Google Search Grounding Citations
                3 -> {
                    item {
                        val uriHandlerContext = LocalContext.current
                        if (gemini.groundingCitations.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                    .testTag("live_grounding_citations_card"),
                                color = SurfaceCream
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Live Regulatory & Bioscience Grounding Citations",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SmokyBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Verified against Google Search Grounding, EU SCCS, US CIR, and NIH PubChem toxicology records.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OliveDrabMuted
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    gemini.groundingCitations.forEach { citation ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    try {
                                                        val browserIntent = android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            android.net.Uri.parse(citation.url)
                                                        ).apply {
                                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        uriHandlerContext.startActivity(browserIntent)
                                                    } catch (_: Exception) {
                                                    }
                                                },
                                            color = CardSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = citation.agency,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OliveDrab
                                                    )
                                                    Text(
                                                        text = "Open Source ↗",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SmokyBlack
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = citation.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SmokyBlack
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = citation.summary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = OliveDrab
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                            color = CardSurface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Checked Scientific Authorities",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SmokyBlack
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                AuthoritySourceRow(
                                    name = "EU SCCS & CosIng",
                                    jurisdiction = "European Union",
                                    desc = "Annex II & Annex III concentration ceilings and opinion records"
                                )
                                AuthoritySourceRow(
                                    name = "US CIR Expert Panel",
                                    jurisdiction = "United States",
                                    desc = "Peer-reviewed safety evaluations in cosmetics"
                                )
                                AuthoritySourceRow(
                                    name = "US FDA",
                                    jurisdiction = "United States",
                                    desc = "OTC monographs, color additive regulations, and FALCPA allergens"
                                )
                                AuthoritySourceRow(
                                    name = "NIH PubChem & EFSA / IFRA",
                                    jurisdiction = "Global",
                                    desc = "Molecular toxicology, LogP dermal partitioning & allergen standards"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom editorial cosmetic bottle line-art thumbnail matching Screens 6, 7, 8, 9 in the reference mockup.
 */
@Composable
fun ProductBottleThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE5DEC9)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

            val bottleW = w * 0.42f
            val bottleH = h * 0.56f
            val left = (w - bottleW) / 2f
            val top = h * 0.28f

            // Bottle body
            drawRoundRect(
                color = Color(0xFFFBF8F3),
                topLeft = Offset(left, top),
                size = Size(bottleW, bottleH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                color = SmokyBlack,
                topLeft = Offset(left, top),
                size = Size(bottleW, bottleH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = stroke
            )

            // Neck & cap
            val neckW = bottleW * 0.46f
            val neckLeft = (w - neckW) / 2f
            drawRect(
                color = SmokyBlack,
                topLeft = Offset(neckLeft, top - h * 0.11f),
                size = Size(neckW, h * 0.11f)
            )

            // Label line accent
            drawLine(
                color = OliveDrab,
                start = Offset(left + bottleW * 0.2f, top + bottleH * 0.42f),
                end = Offset(left + bottleW * 0.8f, top + bottleH * 0.42f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = OliveDrabMuted,
                start = Offset(left + bottleW * 0.25f, top + bottleH * 0.58f),
                end = Offset(left + bottleW * 0.75f, top + bottleH * 0.58f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AuthoritySourceRow(name: String, jurisdiction: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.titleSmall, color = SmokyBlack)
            Text(text = jurisdiction, style = MaterialTheme.typography.labelSmall, color = OliveDrabMuted)
        }
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = OliveDrab)
    }
}

@Composable
fun FindingMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = OliveDrabMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SmokyBlack)
    }
}
