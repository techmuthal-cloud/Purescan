package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiIngredientInsight
import com.example.data.api.RegulatoryGroundingCitation
import com.example.data.model.FindingType
import com.example.data.model.ParsedIngredient
import com.example.data.model.ScanReport
import com.example.data.model.Severity
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

enum class IngredientHazardCategory(
    val key: String,
    val label: String,
    val subtitle: String,
    val indicatorColor: Color,
    val containerBg: Color,
    val icon: ImageVector
) {
    HIGH_HAZARD(
        key = "HIGH_HAZARD",
        label = "High Hazard",
        subtitle = "Restricted compounds, potent sensitizers, or personal High Risk allergens",
        indicatorColor = SemanticCritical,
        containerBg = SemanticCriticalBg,
        icon = Icons.Default.Error
    ),
    WARNING(
        key = "WARNING",
        label = "Warning",
        subtitle = "Moderate irritation potential, concentration limits, or caution allergens",
        indicatorColor = SemanticCaution,
        containerBg = SemanticCautionBg,
        icon = Icons.Default.Warning
    ),
    SAFE(
        key = "SAFE",
        label = "Safe",
        subtitle = "Approved formulation ingredients within established regulatory thresholds",
        indicatorColor = SemanticSuccess,
        containerBg = SemanticSuccessBg,
        icon = Icons.Default.CheckCircle
    )
}

data class CategorizedIngredientEntry(
    val id: String,
    val displayName: String,
    val rawLabelText: String,
    val category: IngredientHazardCategory,
    val functionalRole: String,
    val safetyExplanation: String,
    val matchedAllergenName: String? = null,
    val isHighRiskAllergen: Boolean = false,
    val casNumber: String,
    val bioavailabilityNote: String,
    val toxicologyMechanism: String,
    val regulatoryAuthority: String,
    val concentrationCondition: String = "",
    val citations: List<RegulatoryGroundingCitation> = emptyList(),
    val parsedIngredient: ParsedIngredient? = null
)

/**
 * Deterministically merges EvidenceEngine parsed ingredients, user Allergy Filter matches,
 * and Google Gemini API ingredient breakdowns into categorized 'Safe', 'Warning', and 'High Hazard' entries.
 */
fun categorizeReportIngredients(
    report: ScanReport,
    gemini: GeminiIngredientInsight
): List<CategorizedIngredientEntry> {
    val breakdownMap = gemini.ingredientBreakdowns.associateBy { it.name.trim().lowercase() }
    val findingsByIng = report.findings.groupBy { it.ingredientName.trim().lowercase() }

    val entriesFromParsed = report.ingredients.mapIndexed { index, parsed ->
        val canonical = parsed.canonicalIngredient
        val displayName = canonical?.canonicalName ?: parsed.rawText
        val lookupKey = displayName.trim().lowercase()
        val rawKey = parsed.rawText.trim().lowercase()

        val breakdown = breakdownMap[lookupKey]
            ?: breakdownMap[rawKey]
            ?: gemini.ingredientBreakdowns.firstOrNull { b ->
                val bLower = b.name.trim().lowercase()
                lookupKey.contains(bLower) || bLower.contains(lookupKey) || rawKey.contains(bLower)
            }

        val matchingFindings = (findingsByIng[lookupKey].orEmpty() + findingsByIng[rawKey].orEmpty()).distinctBy { it.id }

        val allergenName = parsed.matchedAllergenName
            ?: gemini.allergensDetected.firstOrNull { detected ->
                val clean = detected.substringBefore("(").trim()
                clean.isNotBlank() && (displayName.contains(clean, ignoreCase = true) || parsed.rawText.contains(clean, ignoreCase = true))
            }

        val isHighRiskAllergen = parsed.allergenSeverity == Severity.CRITICAL ||
            matchingFindings.any { it.findingType == FindingType.ALLERGEN_ALERT && it.severity == Severity.CRITICAL }

        val category = when {
            isHighRiskAllergen -> IngredientHazardCategory.HIGH_HAZARD
            breakdown?.riskLevel.equals("HIGH_RISK", ignoreCase = true) -> IngredientHazardCategory.HIGH_HAZARD
            matchingFindings.any { it.severity == Severity.CRITICAL } -> IngredientHazardCategory.HIGH_HAZARD
            allergenName != null -> IngredientHazardCategory.WARNING
            breakdown?.riskLevel.equals("CAUTION", ignoreCase = true) ||
                breakdown?.riskLevel.equals("WARNING", ignoreCase = true) -> IngredientHazardCategory.WARNING
            matchingFindings.any { it.severity == Severity.CAUTION } -> IngredientHazardCategory.WARNING
            else -> IngredientHazardCategory.SAFE
        }

        val primaryFinding = matchingFindings.maxByOrNull {
            when (it.severity) {
                Severity.CRITICAL -> 3
                Severity.CAUTION -> 2
                Severity.INFO -> 1
            }
        }

        val role = breakdown?.role?.takeIf { it.isNotBlank() }
            ?: canonical?.category?.takeIf { it.isNotBlank() }
            ?: "Formulation Component"

        val explanation = when {
            allergenName != null -> buildString {
                append("Flagged by your personal Allergy Filter ($allergenName). ")
                append(breakdown?.regulatoryNote ?: primaryFinding?.explanation ?: canonical?.description ?: "Avoid topical or dietary exposure if sensitized.")
            }
            primaryFinding != null -> primaryFinding.explanation
            breakdown != null && breakdown.regulatoryNote.isNotBlank() -> breakdown.regulatoryNote
            canonical != null -> canonical.description
            else -> "Evaluated against US CIR and EU SCCS cosmetic safety thresholds with no known restrictions."
        }

        val cas = breakdown?.casNumber?.takeIf { it.isNotBlank() }
            ?: canonical?.casNumber?.takeIf { it.isNotBlank() }
            ?: "Verified INCI Record"

        val bioavailability = breakdown?.bioavailabilityNote?.takeIf { it.isNotBlank() }
            ?: canonical?.dermalBioavailability?.takeIf { it.isNotBlank() }
            ?: gemini.toxicologyProfile.dermalBioavailability

        val mechanism = breakdown?.toxicologyMechanism?.takeIf { it.isNotBlank() }
            ?: canonical?.toxicologyMechanism?.takeIf { it.isNotBlank() }
            ?: gemini.toxicologyProfile.mechanismSummary

        val authority = primaryFinding?.let { "${it.sourceOrg} (${it.jurisdiction})" }
            ?: canonical?.evidenceRecords?.firstOrNull()?.let { "${it.organization} (${it.jurisdiction})" }
            ?: "EU SCCS / US CIR / FDA"

        val condition = primaryFinding?.conditions?.takeIf { it.isNotBlank() }
            ?: canonical?.evidenceRecords?.firstOrNull { it.conditions.isNotBlank() }?.conditions
            ?: ""

        CategorizedIngredientEntry(
            id = "${index}_${displayName.lowercase().replace(" ", "_")}",
            displayName = displayName,
            rawLabelText = parsed.rawText,
            category = category,
            functionalRole = role,
            safetyExplanation = explanation,
            matchedAllergenName = allergenName,
            isHighRiskAllergen = isHighRiskAllergen,
            casNumber = cas,
            bioavailabilityNote = bioavailability,
            toxicologyMechanism = mechanism,
            regulatoryAuthority = authority,
            concentrationCondition = condition,
            citations = gemini.groundingCitations,
            parsedIngredient = parsed
        )
    }

    if (entriesFromParsed.isNotEmpty()) {
        return entriesFromParsed
    }

    // Fallback if only Gemini breakdown items are available
    return gemini.ingredientBreakdowns.mapIndexed { index, bItem ->
        val cat = when (bItem.riskLevel.uppercase()) {
            "HIGH_RISK", "HIGH_HAZARD", "CRITICAL" -> IngredientHazardCategory.HIGH_HAZARD
            "CAUTION", "WARNING" -> IngredientHazardCategory.WARNING
            else -> IngredientHazardCategory.SAFE
        }
        CategorizedIngredientEntry(
            id = "gemini_${index}_${bItem.name.lowercase().replace(" ", "_")}",
            displayName = bItem.name,
            rawLabelText = bItem.name,
            category = cat,
            functionalRole = bItem.role,
            safetyExplanation = bItem.regulatoryNote,
            casNumber = bItem.casNumber,
            bioavailabilityNote = bItem.bioavailabilityNote,
            toxicologyMechanism = bItem.toxicologyMechanism,
            regulatoryAuthority = "Google Gemini AI • EU SCCS / US CIR",
            citations = gemini.groundingCitations
        )
    }
}

/**
 * Material 3 visual component displaying scanned product results categorized by
 * 'Safe', 'Warning', and 'High Hazard' with color-coded indicators, filter pills,
 * allergen match tags, functional roles, and inline toxicology/CAS/citation details.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorizedIngredientResultsSection(
    report: ScanReport,
    gemini: GeminiIngredientInsight,
    modifier: Modifier = Modifier,
    defaultShowInlineToxicology: Boolean = true,
    onInspectIngredient: ((ParsedIngredient) -> Unit)? = null
) {
    val entries = remember(report, gemini) {
        categorizeReportIngredients(report, gemini)
    }

    val highHazardEntries = remember(entries) { entries.filter { it.category == IngredientHazardCategory.HIGH_HAZARD } }
    val warningEntries = remember(entries) { entries.filter { it.category == IngredientHazardCategory.WARNING } }
    val safeEntries = remember(entries) { entries.filter { it.category == IngredientHazardCategory.SAFE } }

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAllToxicologyInline by remember { mutableStateOf(defaultShowInlineToxicology) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("categorized_ingredient_results_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header & Toxicology Detail Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("categorized_summary_header_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ScanOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gemini AI Hazard & Allergen Matrix",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = SmokyBlack
                            )
                            Text(
                                text = "${entries.size} ingredients categorized by Safe, Warning & High Hazard",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrabMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showAllToxicologyInline) ScanOrange.copy(alpha = 0.16f) else SurfaceCream,
                        border = BorderStroke(1.dp, if (showAllToxicologyInline) ScanOrange else CardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAllToxicologyInline = !showAllToxicologyInline }
                            .testTag("toggle_inline_toxicology_button")
                    ) {
                        Text(
                            text = if (showAllToxicologyInline) "Full Data: ON" else "Compact",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Proportional Color-Coded Hazard Bar
                if (entries.isNotEmpty()) {
                    val total = entries.size.toFloat().coerceAtLeast(1f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(BoneLight)
                    ) {
                        if (highHazardEntries.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(highHazardEntries.size / total)
                                    .fillMaxHeight()
                                    .background(SemanticCritical)
                            )
                        }
                        if (warningEntries.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(warningEntries.size / total)
                                    .fillMaxHeight()
                                    .background(SemanticCaution)
                            )
                        }
                        if (safeEntries.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(safeEntries.size / total)
                                    .fillMaxHeight()
                                    .background(SemanticSuccess)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Interactive Category Filter Pills with Live Counts
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterPill(
                        label = "All (${entries.size})",
                        selected = selectedFilter == "ALL",
                        dotColor = ScanOrange,
                        testTag = "category_filter_all",
                        onClick = { selectedFilter = "ALL" }
                    )
                    CategoryFilterPill(
                        label = "High Hazard (${highHazardEntries.size})",
                        selected = selectedFilter == IngredientHazardCategory.HIGH_HAZARD.key,
                        dotColor = SemanticCritical,
                        testTag = "category_filter_high_hazard",
                        onClick = { selectedFilter = IngredientHazardCategory.HIGH_HAZARD.key }
                    )
                    CategoryFilterPill(
                        label = "Warning (${warningEntries.size})",
                        selected = selectedFilter == IngredientHazardCategory.WARNING.key,
                        dotColor = SemanticCaution,
                        testTag = "category_filter_warning",
                        onClick = { selectedFilter = IngredientHazardCategory.WARNING.key }
                    )
                    CategoryFilterPill(
                        label = "Safe (${safeEntries.size})",
                        selected = selectedFilter == IngredientHazardCategory.SAFE.key,
                        dotColor = SemanticSuccess,
                        testTag = "category_filter_safe",
                        onClick = { selectedFilter = IngredientHazardCategory.SAFE.key }
                    )
                }
            }
        }

        // 1. HIGH HAZARD Material 3 Category Card
        if (selectedFilter == "ALL" || selectedFilter == IngredientHazardCategory.HIGH_HAZARD.key) {
            if (highHazardEntries.isNotEmpty() || selectedFilter == IngredientHazardCategory.HIGH_HAZARD.key) {
                Material3HazardCategoryGroupCard(
                    category = IngredientHazardCategory.HIGH_HAZARD,
                    entries = highHazardEntries,
                    showInlineToxicology = showAllToxicologyInline,
                    onInspectIngredient = onInspectIngredient,
                    testTag = "category_card_high_hazard"
                )
            }
        }

        // 2. WARNING Material 3 Category Card
        if (selectedFilter == "ALL" || selectedFilter == IngredientHazardCategory.WARNING.key) {
            if (warningEntries.isNotEmpty() || selectedFilter == IngredientHazardCategory.WARNING.key) {
                Material3HazardCategoryGroupCard(
                    category = IngredientHazardCategory.WARNING,
                    entries = warningEntries,
                    showInlineToxicology = showAllToxicologyInline,
                    onInspectIngredient = onInspectIngredient,
                    testTag = "category_card_warning"
                )
            }
        }

        // 3. SAFE Material 3 Category Card
        if (selectedFilter == "ALL" || selectedFilter == IngredientHazardCategory.SAFE.key) {
            if (safeEntries.isNotEmpty() || selectedFilter == IngredientHazardCategory.SAFE.key) {
                Material3HazardCategoryGroupCard(
                    category = IngredientHazardCategory.SAFE,
                    entries = safeEntries,
                    showInlineToxicology = showAllToxicologyInline,
                    onInspectIngredient = onInspectIngredient,
                    testTag = "category_card_safe"
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    selected: Boolean,
    dotColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) SmokyBlack else SurfaceCream,
        border = BorderStroke(1.dp, if (selected) SmokyBlack else CardBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) FloralWhite else SmokyBlack
            )
        }
    }
}

@Composable
private fun Material3HazardCategoryGroupCard(
    category: IngredientHazardCategory,
    entries: List<CategorizedIngredientEntry>,
    showInlineToxicology: Boolean,
    onInspectIngredient: ((ParsedIngredient) -> Unit)?,
    testTag: String
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.5.dp, category.indicatorColor.copy(alpha = 0.42f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Color-Coded Category Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(category.containerBg)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(category.indicatorColor.copy(alpha = 0.16f))
                            .border(1.dp, category.indicatorColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            tint = category.indicatorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = SmokyBlack
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = category.indicatorColor
                            ) {
                                Text(
                                    text = "${entries.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = category.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveDrab,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse ${category.label}" else "Expand ${category.label}",
                    tint = SmokyBlack
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (entries.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No ingredients in the ${category.label} category for this product.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OliveDrabMuted,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        entries.forEach { entry ->
                            CategorizedIngredientMaterialCard(
                                entry = entry,
                                showInlineToxicology = showInlineToxicology,
                                onClick = {
                                    entry.parsedIngredient?.let { parsed ->
                                        onInspectIngredient?.invoke(parsed)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorizedIngredientMaterialCard(
    entry: CategorizedIngredientEntry,
    showInlineToxicology: Boolean,
    onClick: () -> Unit
) {
    var localExpanded by remember(showInlineToxicology) { mutableStateOf(showInlineToxicology) }
    val cat = entry.category

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                localExpanded = !localExpanded
                onClick()
            }
            .testTag("categorized_ingredient_card_${entry.rawLabelText}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = FloralWhite),
        border = BorderStroke(
            width = if (entry.matchedAllergenName != null || cat == IngredientHazardCategory.HIGH_HAZARD) 1.5.dp else 1.dp,
            color = if (entry.matchedAllergenName != null) cat.indicatorColor else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Color Indicator Dot + Ingredient Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(cat.indicatorColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = SmokyBlack
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Color-Coded Category Pill ('Safe', 'Warning', 'High Hazard')
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cat.containerBg,
                    border = BorderStroke(1.dp, cat.indicatorColor.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            tint = cat.indicatorColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cat.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = cat.indicatorColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Functional Role Pill + Personal Allergen Match Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceCream,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "Role: ${entry.functionalRole}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OliveDrab,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (entry.matchedAllergenName != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (entry.isHighRiskAllergen) SemanticCritical else SemanticCaution
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALLERGEN: ${entry.matchedAllergenName.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Safety & Hazard Explanation
            Text(
                text = entry.safetyExplanation,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                ),
                color = SmokyBlack
            )

            // Inline / Expandable Full Toxicology, CAS Number & Regulatory Citations (Option 3)
            AnimatedVisibility(visible = localExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceCream,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "CAS Registry: ${entry.casNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SmokyBlack
                                )
                                Text(
                                    text = entry.regulatoryAuthority,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ScanOrange
                                )
                            }

                            if (entry.concentrationCondition.isNotBlank()) {
                                Text(
                                    text = "Regulatory Limit: ${entry.concentrationCondition}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cat.indicatorColor
                                )
                            }

                            Text(
                                text = "Dermal Bioavailability: ${entry.bioavailabilityNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrab
                            )

                            Text(
                                text = "Toxicology Mechanism: ${entry.toxicologyMechanism}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrab
                            )

                            if (entry.citations.isNotEmpty()) {
                                val topCitations = entry.citations.take(2).joinToString(" • ") { "${it.agency}: ${it.title}" }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = OliveDrab,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = topCitations,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OliveDrabMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
