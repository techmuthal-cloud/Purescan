package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchStatus
import com.example.data.model.ParsedIngredient
import com.example.data.model.Severity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream
import com.example.ui.util.MatchCertainty

/**
 * Traffic-light hazard severity tiers for individual ingredients:
 * - RED_HIGH_HAZARD: Red signal lamp + "HIGH HAZARD" pill
 * - YELLOW_CAUTION: Yellow signal lamp + "CAUTION" pill
 * - YELLOW_UNVERIFIED: Yellow signal lamp + "UNVERIFIED" caution pill for uncertain OCR / insufficient data
 * - GREEN_SAFE: Green signal lamp + "SAFE" pill
 */
enum class TrafficLightBadgeSeverity(
    val key: String,
    val pillLabel: String,
    val shortLabel: String,
    val signalHeadline: String,
    val activeLampIndex: Int, // 0 = Red, 1 = Yellow, 2 = Green
    val activeColor: Color,
    val containerBg: Color,
    val statusIcon: ImageVector
) {
    RED_HIGH_HAZARD(
        key = "RED_HIGH_HAZARD",
        pillLabel = "HIGH HAZARD",
        shortLabel = "High Hazard",
        signalHeadline = "Red Traffic-Light • High Hazard / Restricted",
        activeLampIndex = 0,
        activeColor = SemanticCritical,
        containerBg = SemanticCriticalBg,
        statusIcon = Icons.Default.Error
    ),
    YELLOW_CAUTION(
        key = "YELLOW_CAUTION",
        pillLabel = "CAUTION",
        shortLabel = "Caution",
        signalHeadline = "Yellow Traffic-Light • Moderate Caution",
        activeLampIndex = 1,
        activeColor = SemanticCaution,
        containerBg = SemanticCautionBg,
        statusIcon = Icons.Default.Warning
    ),
    YELLOW_UNVERIFIED(
        key = "YELLOW_UNVERIFIED",
        pillLabel = "CAUTION • UNVERIFIED",
        shortLabel = "Unverified",
        signalHeadline = "Yellow Traffic-Light • Unverified Token / Insufficient Data",
        activeLampIndex = 1,
        activeColor = SemanticCaution,
        containerBg = SemanticCautionBg,
        statusIcon = Icons.Default.HelpOutline
    ),
    GREEN_SAFE(
        key = "GREEN_SAFE",
        pillLabel = "SAFE",
        shortLabel = "Safe",
        signalHeadline = "Green Traffic-Light • Low Hazard / Approved",
        activeLampIndex = 2,
        activeColor = SemanticSuccess,
        containerBg = SemanticSuccessBg,
        statusIcon = Icons.Default.CheckCircle
    )
}

data class IngredientSafetyBadgeSpec(
    val ingredientKey: String,
    val ingredientName: String,
    val severity: TrafficLightBadgeSeverity,
    val summaryReason: String,
    val authorityNote: String,
    val isUnverified: Boolean,
    val matchedRuleLabel: String? = null
)

fun CategorizedIngredientEntry.toSafetyBadgeSpec(): IngredientSafetyBadgeSpec {
    val isUnverified = matchCertainty == MatchCertainty.EVIDENCE_INSUFFICIENT ||
        ruleClassificationLabel == "Unverified Label Token"
    val severity = when {
        category == IngredientHazardCategory.HIGH_HAZARD -> TrafficLightBadgeSeverity.RED_HIGH_HAZARD
        isUnverified -> TrafficLightBadgeSeverity.YELLOW_UNVERIFIED
        category == IngredientHazardCategory.WARNING || isUncertainOrUnverified -> TrafficLightBadgeSeverity.YELLOW_CAUTION
        else -> TrafficLightBadgeSeverity.GREEN_SAFE
    }

    val ruleLabel = when {
        matchedAllergenName != null && ruleClassificationLabel != null -> "$ruleClassificationLabel: $matchedAllergenName"
        matchedAllergenName != null -> "Allergen Match: $matchedAllergenName"
        isUnverified -> "Unverified OCR Label Token"
        isUncertainOrUnverified -> "Possible Botanical Derivative"
        else -> null
    }

    return IngredientSafetyBadgeSpec(
        ingredientKey = rawLabelText.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { id },
        ingredientName = displayName,
        severity = severity,
        summaryReason = safetyExplanation,
        authorityNote = regulatoryAuthority,
        isUnverified = isUnverified,
        matchedRuleLabel = ruleLabel
    )
}

fun ParsedIngredient.toSafetyBadgeSpec(
    categorizedEntry: CategorizedIngredientEntry? = null
): IngredientSafetyBadgeSpec {
    if (categorizedEntry != null) {
        return categorizedEntry.toSafetyBadgeSpec()
    }

    val isMatched = matchStatus == MatchStatus.MATCHED && canonicalIngredient != null
    val isHighRiskAllergen = allergenSeverity == Severity.CRITICAL
    val hasAllergen = matchedAllergenName != null

    val severity = when {
        hasAllergen && isHighRiskAllergen -> TrafficLightBadgeSeverity.RED_HIGH_HAZARD
        hasAllergen -> TrafficLightBadgeSeverity.YELLOW_CAUTION
        !isMatched -> TrafficLightBadgeSeverity.YELLOW_UNVERIFIED
        else -> TrafficLightBadgeSeverity.GREEN_SAFE
    }

    val displayName = canonicalIngredient?.canonicalName ?: rawText
    val explanation = when {
        hasAllergen -> "Matched your personal rule ($matchedAllergenName). Review concentration and exposure guidance."
        !isMatched -> "Token '$rawText' was transcribed from the label but is unverified in the canonical INCI catalog. Treated with yellow caution until verified."
        canonicalIngredient != null && canonicalIngredient.description.isNotBlank() -> canonicalIngredient.description
        else -> "Evaluated against US CIR and EU SCCS safety thresholds with no known restrictions."
    }

    val authority = canonicalIngredient?.evidenceRecords?.firstOrNull()?.let { "${it.organization} (${it.jurisdiction})" }
        ?: if (!isMatched) "Pending OCR Verification" else "EU SCCS / US CIR"

    return IngredientSafetyBadgeSpec(
        ingredientKey = rawText.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { "ingredient" },
        ingredientName = displayName,
        severity = severity,
        summaryReason = explanation,
        authorityNote = authority,
        isUnverified = !isMatched,
        matchedRuleLabel = matchedAllergenName?.let { "Allergen Rule: $it" }
            ?: if (!isMatched) "Unverified Label Token" else null
    )
}

/**
 * Miniature 3-lamp traffic-light housing icon (Red / Yellow / Green) that illuminates
 * the active hazard severity lamp at full brightness while keeping inactive lamps muted.
 */
@Composable
fun TrafficLightSignalIcon(
    severity: TrafficLightBadgeSeverity,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val lampSize = if (compact) 5.5.dp else 6.5.dp
    val spacing = if (compact) 2.5.dp else 3.dp
    val horizontalPad = if (compact) 4.5.dp else 5.5.dp
    val verticalPad = if (compact) 3.dp else 3.5.dp

    val redColor = SemanticCritical
    val yellowColor = SemanticCaution
    val greenColor = SemanticSuccess

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SmokyBlack.copy(alpha = 0.9f))
            .border(0.75.dp, severity.activeColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = horizontalPad, vertical = verticalPad),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val lamps = listOf(redColor, yellowColor, greenColor)
        lamps.forEachIndexed { index, lampColor ->
            val isActive = severity.activeLampIndex == index
            Box(
                modifier = Modifier
                    .size(if (isActive) lampSize + 1.5.dp else lampSize)
                    .clip(CircleShape)
                    .background(
                        if (isActive) lampColor else lampColor.copy(alpha = 0.22f)
                    )
                    .then(
                        if (isActive) {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

/**
 * Interactive color-coded Traffic-Light Safety Badge (Green / Yellow / Red) with
 * traffic-light icon + short severity label pill. Tapping the badge invokes [onClick]
 * to toggle the inline tap-to-view hazard tooltip.
 */
@Composable
fun IngredientSafetyBadge(
    spec: IngredientSafetyBadgeSpec,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val sev = spec.severity
    val pillText = if (compact) {
        when (sev) {
            TrafficLightBadgeSeverity.RED_HIGH_HAZARD -> "HIGH HAZARD"
            TrafficLightBadgeSeverity.YELLOW_UNVERIFIED -> "UNVERIFIED"
            TrafficLightBadgeSeverity.YELLOW_CAUTION -> "CAUTION"
            TrafficLightBadgeSeverity.GREEN_SAFE -> "SAFE"
        }
    } else {
        sev.pillLabel
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = sev.containerBg,
        border = BorderStroke(1.dp, sev.activeColor.copy(alpha = 0.55f)),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = "${spec.ingredientName} safety badge: ${sev.signalHeadline}"
            }
            .testTag("ingredient_safety_badge_${spec.ingredientKey}")
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 7.dp else 9.dp,
                vertical = if (compact) 3.5.dp else 4.5.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrafficLightSignalIcon(
                severity = sev,
                compact = compact
            )
            Spacer(modifier = Modifier.width(if (compact) 5.dp else 6.dp))
            Icon(
                imageVector = sev.statusIcon,
                contentDescription = null,
                tint = sev.activeColor,
                modifier = Modifier.size(if (compact) 11.dp else 13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = pillText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = sev.activeColor,
                fontSize = if (compact) 9.5.sp else 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tap-to-view hazard tooltip popover card that expands when a user taps an ingredient's
 * Traffic-Light Safety Badge across any report ingredient list.
 */
@Composable
fun IngredientSafetyTooltipCard(
    spec: IngredientSafetyBadgeSpec,
    visible: Boolean,
    onDismiss: () -> Unit,
    onInspectFullDetails: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sev = spec.severity

    AnimatedVisibility(visible = visible) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = FloralWhite,
            border = BorderStroke(1.5.dp, sev.activeColor.copy(alpha = 0.65f)),
            tonalElevation = 2.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("ingredient_safety_tooltip_${spec.ingredientKey}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(sev.containerBg.copy(alpha = 0.45f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        TrafficLightSignalIcon(severity = sev, compact = false)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = sev.signalHeadline,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = sev.activeColor
                            )
                            Text(
                                text = "${spec.ingredientName} • ${spec.authorityNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrabMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(SurfaceCream)
                            .clickable(onClick = onDismiss)
                            .testTag("dismiss_safety_tooltip_${spec.ingredientKey}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss hazard tooltip",
                            tint = SmokyBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                if (spec.matchedRuleLabel != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = sev.activeColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, sev.activeColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = sev.statusIcon,
                                contentDescription = null,
                                tint = sev.activeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = spec.matchedRuleLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = sev.activeColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = spec.summaryReason,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = SmokyBlack
                )

                if (onInspectFullDetails != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onInspectFullDetails() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = OliveDrab,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to inspect full toxicology & regulatory profile",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OliveDrab
                        )
                    }
                }
            }
        }
    }
}
