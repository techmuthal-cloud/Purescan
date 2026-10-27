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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.MatchStatus
import com.example.data.model.ParsedIngredient
import com.example.data.model.ScanReport
import com.example.data.model.Severity
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabDark
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
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppLocalization
import com.example.ui.util.MatchCertainty

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
        subtitle = "Restricted compounds, potent sensitizers, or confirmed Medical Allergy matches",
        indicatorColor = SemanticCritical,
        containerBg = SemanticCriticalBg,
        icon = Icons.Default.Error
    ),
    WARNING(
        key = "WARNING",
        label = "Warning",
        subtitle = "Moderate irritation potential, concentration limits, Sensitivities, or Avoidances",
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
    val ruleClassificationLabel: String? = null,
    val personalReasonNote: String? = null,
    val matchCertainty: MatchCertainty = MatchCertainty.DETECTED_NO_RULE_MATCH,
    val isUncertainOrUnverified: Boolean = false,
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
 * and Google Gemini API ingredient breakdowns into categorized entries with explicit
 * MatchCertainty and 3-tier Rule Classification metadata.
 */
fun categorizeReportIngredients(
    report: ScanReport,
    gemini: GeminiIngredientInsight
): List<CategorizedIngredientEntry> {
    val breakdownMap = gemini.ingredientBreakdowns.associateBy { it.name.trim().lowercase() }
    val findingsByIng = report.findings.groupBy { it.ingredientName.trim().lowercase() }
    val prefNoteByIng = report.preferenceNotes.associateBy { it.triggeredIngredient.trim().lowercase() }

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

        val matchedPrefNote = prefNoteByIng[lookupKey]
            ?: prefNoteByIng[rawKey]
            ?: report.preferenceNotes.firstOrNull { note ->
                lookupKey.contains(note.triggeredIngredient.trim().lowercase()) ||
                    rawKey.contains(note.triggeredIngredient.trim().lowercase())
            }

        val allergenName = parsed.matchedAllergenName
            ?: matchedPrefNote?.prefLabel
            ?: gemini.allergensDetected.firstOrNull { detected ->
                val clean = detected.substringBefore("(").trim()
                clean.isNotBlank() && (displayName.contains(clean, ignoreCase = true) || parsed.rawText.contains(clean, ignoreCase = true))
            }

        val isHighRiskAllergen = parsed.allergenSeverity == Severity.CRITICAL ||
            matchingFindings.any { it.findingType == FindingType.ALLERGEN_ALERT && it.severity == Severity.CRITICAL } ||
            breakdown?.regulatoryNote?.contains("Medical Allergy", ignoreCase = true) == true

        val isUnverifiedToken = parsed.matchStatus == MatchStatus.UNMATCHED && canonical == null

        val isDerivativeMatch = allergenName != null && (
            displayName.contains("extract", ignoreCase = true) ||
                displayName.contains("oil", ignoreCase = true) ||
                displayName.contains("hydrolyzed", ignoreCase = true) ||
                displayName.contains("derivative", ignoreCase = true)
            ) && !displayName.equals(allergenName, ignoreCase = true)

        val matchCertainty = when {
            allergenName != null && isDerivativeMatch -> MatchCertainty.POSSIBLE_DERIVATIVE_MATCH
            allergenName != null -> MatchCertainty.CONFIRMED_RULE_MATCH
            isUnverifiedToken -> MatchCertainty.EVIDENCE_INSUFFICIENT
            else -> MatchCertainty.DETECTED_NO_RULE_MATCH
        }

        val ruleClassificationLabel = when {
            allergenName != null && isHighRiskAllergen -> "Medical Allergy"
            allergenName != null && (
                matchedPrefNote?.note?.contains("Avoidance", ignoreCase = true) == true ||
                    breakdown?.regulatoryNote?.contains("Preference Avoidance", ignoreCase = true) == true
                ) -> "Preference Avoidance"
            allergenName != null -> "Personal Sensitivity"
            matchingFindings.any { it.findingType == FindingType.REGULATORY_RESTRICTION } -> "Regulatory Restriction"
            isUnverifiedToken -> "Unverified Label Token"
            else -> null
        }

        val personalReasonNote = matchedPrefNote?.note
            ?: matchingFindings.firstOrNull { it.findingType == FindingType.ALLERGEN_ALERT }?.explanation

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
            ?: if (isUnverifiedToken) "Unverified OCR Token" else "Formulation Component"

        val explanation = when {
            allergenName != null -> buildString {
                val tierName = ruleClassificationLabel ?: "Personal Rule"
                append("Matched your $tierName rule ($allergenName). ")
                if (!personalReasonNote.isNullOrBlank()) {
                    append("$personalReasonNote ")
                } else {
                    append(breakdown?.regulatoryNote ?: primaryFinding?.explanation ?: canonical?.description ?: "Avoid exposure based on your selected rule.")
                }
            }
            isUnverifiedToken -> "Token '${parsed.rawText}' was transcribed from the label but is not matched in the canonical INCI catalog. Absence of a match does not guarantee safety—tap to review or correct OCR spelling."
            primaryFinding != null -> primaryFinding.explanation
            breakdown != null && breakdown.regulatoryNote.isNotBlank() -> breakdown.regulatoryNote
            canonical != null -> canonical.description
            else -> "Evaluated against US CIR and EU SCCS cosmetic safety thresholds with no known restrictions."
        }

        val cas = breakdown?.casNumber?.takeIf { it.isNotBlank() }
            ?: canonical?.casNumber?.takeIf { it.isNotBlank() }
            ?: if (isUnverifiedToken) "Unverified (Pending OCR Check)" else "Verified INCI Record"

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
            ruleClassificationLabel = ruleClassificationLabel,
            personalReasonNote = personalReasonNote,
            matchCertainty = matchCertainty,
            isUncertainOrUnverified = isUnverifiedToken || matchCertainty == MatchCertainty.POSSIBLE_DERIVATIVE_MATCH,
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
        val isRuleMatch = bItem.regulatoryNote.contains("Confirmed match to your rule", ignoreCase = true) ||
            bItem.regulatoryNote.contains("ALLERGEN MATCH", ignoreCase = true)
        CategorizedIngredientEntry(
            id = "gemini_${index}_${bItem.name.lowercase().replace(" ", "_")}",
            displayName = bItem.name,
            rawLabelText = bItem.name,
            category = cat,
            functionalRole = bItem.role,
            safetyExplanation = bItem.regulatoryNote,
            ruleClassificationLabel = when {
                bItem.role.contains("Medical Allergy", ignoreCase = true) -> "Medical Allergy"
                bItem.role.contains("Personal Sensitivity", ignoreCase = true) -> "Personal Sensitivity"
                bItem.role.contains("Preference Avoidance", ignoreCase = true) -> "Preference Avoidance"
                else -> null
            },
            matchCertainty = if (isRuleMatch) MatchCertainty.CONFIRMED_RULE_MATCH else MatchCertainty.DETECTED_NO_RULE_MATCH,
            casNumber = bItem.casNumber,
            bioavailabilityNote = bItem.bioavailabilityNote,
            toxicologyMechanism = bItem.toxicologyMechanism,
            regulatoryAuthority = "Google Gemini AI • EU SCCS / US CIR",
            citations = gemini.groundingCitations
        )
    }
}

/**
 * Dedicated Uncertainty & Inconclusive Findings Banner with Unverified Token Chips
 * and an Inline OCR Text Editor that allows users to correct label text and re-analyze on the spot.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UncertaintyFindingsBanner(
    unmatchedTokens: List<String>,
    possibleDerivativeTokens: List<String>,
    ocrConfidence: Int,
    currentIngredientsText: String,
    onReanalyzeOcrText: (String) -> Unit,
    onOpenFullTextEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasUnverified = unmatchedTokens.isNotEmpty()
    val hasLowConfidence = ocrConfidence in 1..84
    val hasDerivatives = possibleDerivativeTokens.isNotEmpty()
    val hasUncertainty = hasUnverified || hasLowConfidence || hasDerivatives

    var showInlineOcrEditor by remember { mutableStateOf(hasUnverified || hasLowConfidence) }
    var editedOcrText by remember(currentIngredientsText) { mutableStateOf(currentIngredientsText) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("uncertainty_findings_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasUncertainty) SemanticCautionBg else SurfaceCream
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (hasUncertainty) SemanticCaution.copy(alpha = 0.55f) else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasUncertainty) SemanticCaution.copy(alpha = 0.18f)
                                else OliveDrab.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasUncertainty) Icons.Default.HelpOutline else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (hasUncertainty) SemanticCaution else OliveDrab,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                hasUnverified -> "Inconclusive / Unverified Tokens (${unmatchedTokens.size})"
                                hasDerivatives -> "Possible Botanical Derivative Cross-Reactivity"
                                hasLowConfidence -> "Low OCR Confidence ($ocrConfidence%) — Verify Text"
                                else -> "OCR Evidence Verification ($ocrConfidence% Confidence)"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = SmokyBlack
                        )
                        Text(
                            text = if (hasUncertainty) {
                                "Unrecognized or derivative tokens cannot be guaranteed safe. Tap a chip or edit OCR text below to re-analyze."
                            } else {
                                "All recognized tokens matched the canonical INCI catalog. You can still inspect or edit the transcribed label text."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = OliveDrabDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { showInlineOcrEditor = !showInlineOcrEditor },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_inline_ocr_editor_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = SmokyBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showInlineOcrEditor) "Hide OCR" else "Edit OCR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                }
            }

            // Unverified Token Chips & Possible Derivative Chips
            if (unmatchedTokens.isNotEmpty() || possibleDerivativeTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("uncertainty_token_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unmatchedTokens.forEach { token ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, SemanticCaution),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showInlineOcrEditor = true }
                                .testTag("unverified_token_chip_$token")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SemanticCaution,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Unverified: $token",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SmokyBlack
                                )
                            }
                        }
                    }

                    possibleDerivativeTokens.forEach { derivative ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, OliveDrab),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showInlineOcrEditor = true }
                                .testTag("derivative_token_chip_$derivative")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = OliveDrab,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Possible Derivative: $derivative",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Inline OCR Label Editor
            AnimatedVisibility(visible = showInlineOcrEditor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("inline_ocr_editor_container")
                ) {
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Inline OCR Ingredient List Editor",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Correct any misread or truncated ingredient names below and tap Re-Analyze to update this report immediately.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = OliveDrab
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedOcrText,
                        onValueChange = { editedOcrText = it },
                        label = { Text("Recognized Ingredient Label Text") },
                        minLines = 2,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inline_ocr_editor_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SmokyBlack,
                            unfocusedTextColor = SmokyBlack,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ScanOrange,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onReanalyzeOcrText(editedOcrText)
                                showInlineOcrEditor = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inline_ocr_reanalyze_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ScanOrange,
                                contentColor = OnScanOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Re-Analyze Corrected Text",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenFullTextEditor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("inline_ocr_full_editor_button")
                        ) {
                            Text(
                                text = "Full Editor",
                                style = MaterialTheme.typography.labelMedium,
                                color = SmokyBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unified Product Scan Result UI Component displaying:
 * 1. Analysis Status & Clinical Trust Summary Card
 * 2. Dedicated Uncertainty Banner with unverified token chips & inline OCR editor
 * 3. Interactive Categorized Ingredient Cards with Rule Classification Badges, Match Certainty Tags,
 *    evidence-backed matching rules, and expandable regulatory citations.
 */
@Composable
fun ProductScanResultComponent(
    report: ScanReport,
    gemini: GeminiIngredientInsight,
    language: AppLanguage = AppLanguage.ENGLISH,
    onReanalyzeOcrText: (String) -> Unit,
    onOpenFullTextEditor: () -> Unit,
    onInspectIngredient: ((ParsedIngredient) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val entries = remember(report, gemini) {
        categorizeReportIngredients(report, gemini)
    }

    val unmatchedTokens = remember(report.unmatchedIngredients, entries) {
        (report.unmatchedIngredients.map { it.rawText } + entries.filter { it.matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT }.map { it.rawLabelText })
            .distinct()
    }

    val possibleDerivativeTokens = remember(entries) {
        entries.filter { it.matchCertainty == MatchCertainty.POSSIBLE_DERIVATIVE_MATCH }
            .map { it.displayName }
            .distinct()
    }

    val matchedRuleSummaries = remember(report.preferenceNotes, entries, gemini.allergensDetected) {
        val fromEntries = entries.filter { it.matchedAllergenName != null }.map { entry ->
            val badge = entry.ruleClassificationLabel ?: "Personal Rule"
            val reason = entry.personalReasonNote?.takeIf { it.isNotBlank() } ?: entry.safetyExplanation
            "${entry.displayName} → [$badge: ${entry.matchedAllergenName}] $reason"
        }
        when {
            fromEntries.isNotEmpty() -> fromEntries
            report.preferenceNotes.isNotEmpty() -> report.preferenceNotes.map { "${it.triggeredIngredient} → ${it.prefLabel}: ${it.note}" }
            else -> gemini.allergensDetected
        }
    }

    val citationSummaries = remember(gemini.groundingCitations, report.findings) {
        if (gemini.groundingCitations.isNotEmpty()) {
            gemini.groundingCitations.map { "${it.agency}: ${it.title} — ${it.summary}" }
        } else {
            report.findings.map { "${it.sourceOrg}: ${it.ingredientName} (${it.evidenceStrength})" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_scan_result_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Clinical Evidence & Analysis Status Summary Card
        TrustResultSummaryCard(
            recognizedCount = report.ingredients.size.coerceAtLeast(gemini.ingredientBreakdowns.size),
            ocrConfidence = gemini.ocrConfidence,
            matchedRules = matchedRuleSummaries,
            regulatoryNotes = citationSummaries,
            language = language,
            onReviewOcrClick = onOpenFullTextEditor
        )

        // 2. Dedicated Uncertainty Banner with Unverified Token Chips & Inline OCR Editor
        UncertaintyFindingsBanner(
            unmatchedTokens = unmatchedTokens,
            possibleDerivativeTokens = possibleDerivativeTokens,
            ocrConfidence = gemini.ocrConfidence,
            currentIngredientsText = report.correctedText,
            onReanalyzeOcrText = onReanalyzeOcrText,
            onOpenFullTextEditor = onOpenFullTextEditor
        )

        // 3. Interactive Categorized Ingredient Insights with Rule Badges, Certainty Tags & Expandable Citations
        CategorizedIngredientResultsSection(
            report = report,
            gemini = gemini,
            defaultShowInlineToxicology = false,
            onInspectIngredient = onInspectIngredient
        )
    }
}

/**
 * Material 3 visual component displaying scanned product results categorized by
 * 'Safe', 'Warning', and 'High Hazard' with color-coded indicators, filter pills,
 * 3-tier Rule Classification badges, Match Certainty tags, and expandable citations.
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
    val uncertainEntries = remember(entries) { entries.filter { it.isUncertainOrUnverified } }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ScanOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Ingredient Insights & Rule Evidence",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = SmokyBlack
                            )
                            Text(
                                text = "${entries.size} ingredients • Rule classifications, certainty tags & citations",
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
                            text = if (showAllToxicologyInline) "Citations: Expanded" else "Expand Citations",
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

                // Interactive Category & Certainty Filter Pills with Live Counts
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
                    if (uncertainEntries.isNotEmpty()) {
                        CategoryFilterPill(
                            label = "Uncertain (${uncertainEntries.size})",
                            selected = selectedFilter == "UNCERTAIN",
                            dotColor = SemanticCaution,
                            testTag = "category_filter_uncertain",
                            onClick = { selectedFilter = "UNCERTAIN" }
                        )
                    }
                }
            }
        }

        if (selectedFilter == "UNCERTAIN") {
            Material3HazardCategoryGroupCard(
                category = IngredientHazardCategory.WARNING,
                customTitle = "Uncertain & Unverified Findings",
                customSubtitle = "Ingredients with insufficient catalog evidence or possible botanical cross-reactivity",
                entries = uncertainEntries,
                showInlineToxicology = showAllToxicologyInline,
                onInspectIngredient = onInspectIngredient,
                testTag = "category_card_uncertain"
            )
        } else {
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
    testTag: String,
    customTitle: String? = null,
    customSubtitle: String? = null
) {
    var expanded by remember { mutableStateOf(true) }
    val headerTitle = customTitle ?: category.label
    val headerSubtitle = customSubtitle ?: category.subtitle

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
                            contentDescription = headerTitle,
                            tint = category.indicatorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = headerTitle,
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
                            text = headerSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveDrab,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse $headerTitle" else "Expand $headerTitle",
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
                                text = "No ingredients in the $headerTitle category for this product.",
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

@OptIn(ExperimentalLayoutApi::class)
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
            width = if (entry.matchedAllergenName != null || cat == IngredientHazardCategory.HIGH_HAZARD || entry.isUncertainOrUnverified) 1.5.dp else 1.dp,
            color = when {
                entry.matchedAllergenName != null -> cat.indicatorColor
                entry.isUncertainOrUnverified -> SemanticCaution
                else -> CardBorder
            }
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
                            .background(if (entry.isUncertainOrUnverified && entry.matchedAllergenName == null) SemanticCaution else cat.indicatorColor)
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

                // Color-Coded Category Pill ('Safe', 'Warning', 'High Hazard', or 'Uncertain')
                val pillLabel = if (entry.matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT) {
                    "UNVERIFIED"
                } else {
                    cat.label.uppercase()
                }
                val pillColor = if (entry.matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT) {
                    SemanticCaution
                } else {
                    cat.indicatorColor
                }
                val pillBg = if (entry.matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT) {
                    SemanticCautionBg
                } else {
                    cat.containerBg
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pillBg,
                    border = BorderStroke(1.dp, pillColor.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (entry.matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT) Icons.Default.HelpOutline else cat.icon,
                            contentDescription = null,
                            tint = pillColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pillLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = pillColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Classification Badges Row: Functional Role + Rule Classification Badge + Match Certainty Tag
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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

                // Rule Classification Badge (Medical Allergy / Personal Sensitivity / Preference Avoidance)
                if (entry.ruleClassificationLabel != null) {
                    val ruleColor = when (entry.ruleClassificationLabel) {
                        "Medical Allergy" -> SemanticCritical
                        "Personal Sensitivity", "Unverified Label Token" -> SemanticCaution
                        else -> OliveDrab
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ruleColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, ruleColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ruleColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.ruleClassificationLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ruleColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Explicit Match Certainty Tag
                val certaintyColor = when (entry.matchCertainty) {
                    MatchCertainty.CONFIRMED_RULE_MATCH -> SemanticCritical
                    MatchCertainty.POSSIBLE_DERIVATIVE_MATCH, MatchCertainty.EVIDENCE_INSUFFICIENT -> SemanticCaution
                    MatchCertainty.DETECTED_NO_RULE_MATCH -> SemanticSuccess
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = certaintyColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, certaintyColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = entry.matchCertainty.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = certaintyColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (entry.matchedAllergenName != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (entry.isHighRiskAllergen) SemanticCritical else SemanticCaution
                    ) {
                        Text(
                            text = "RULE: ${entry.matchedAllergenName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Evidence-Backed Safety & Rule Match Explanation
            Text(
                text = entry.safetyExplanation,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                ),
                color = SmokyBlack
            )

            // Expandable Citations CTA Row
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (localExpanded) "Hide Regulatory Citations & CAS Data" else "View Evidence Citations (${entry.regulatoryAuthority})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OliveDrab
                )
                Icon(
                    imageVector = if (localExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = OliveDrab,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Inline / Expandable Full Toxicology, CAS Number & Regulatory Citations
            AnimatedVisibility(visible = localExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                text = "Certainty Rationale: ${entry.matchCertainty.explanation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SmokyBlack
                            )

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
                                Spacer(modifier = Modifier.height(2.dp))
                                entry.citations.take(2).forEach { citation ->
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
                                            text = "${citation.agency}: ${citation.title}",
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
}
