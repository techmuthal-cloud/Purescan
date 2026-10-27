package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchStatus
import com.example.data.model.ScanReport
import com.example.data.model.Severity
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabDark
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.util.AppLanguage
import com.example.ui.util.AppLocalization

@Composable
fun LanguageSelectorPill(
    selectedLanguage: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkSurface: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (darkSurface) Color(0xFF20211B) else BoneLight,
        border = BorderStroke(
            1.dp,
            if (darkSurface) Color.White.copy(alpha = 0.22f) else CardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("language_selector_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Select Language",
                tint = if (darkSurface) ScanOrange else OliveDrab,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${selectedLanguage.badge} • ${selectedLanguage.nativeName}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkSurface) Color.White else SmokyBlack
            )
        }
    }
}

@Composable
fun LanguageSelectorDialog(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FloralWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = OliveDrab,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Select Language (8 Languages)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Text(
                        text = "Localizes App UI and Gemini AI scan reports while keeping INCI chemical names accurate",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrabDark,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("language_options_list")
            ) {
                AppLanguage.entries.forEach { lang ->
                    val isSelected = lang == selectedLanguage
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) OliveDrab.copy(alpha = 0.12f) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OliveDrab else CardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onLanguageSelected(lang)
                                onDismiss()
                            }
                            .testTag("language_option_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) OliveDrab else BoneLight
                                ) {
                                    Text(
                                        text = lang.badge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else SmokyBlack,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SmokyBlack
                                    )
                                    Text(
                                        text = "${lang.englishName} • AI Report + UI",
                                        fontSize = 11.sp,
                                        color = OliveDrabDark
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = OliveDrab,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = OliveDrab, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun LanguageSelectorDialog(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    useSelectAlias: Boolean = true
) {
    LanguageSelectorDialog(
        selectedLanguage = selectedLanguage,
        onLanguageSelected = onSelectLanguage,
        onDismiss = onDismiss
    )
}

@Composable
fun HowItWorksDialog(
    language: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit,
    onStartScan: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FloralWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = OliveDrab,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How Purescan Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("how_it_works_dialog_content")
            ) {
                Text(
                    text = "1. Capture or Select an Ingredient Label",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SmokyBlack
                )
                Text(
                    text = "Photograph the printed ingredient list on cosmetics, skincare, or food packaging using CameraX, or review/correct recognized text before analysis.",
                    fontSize = 12.sp,
                    color = OliveDrabDark
                )

                Text(
                    text = "2. Personal Rule & Regulatory Evidence Check",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SmokyBlack
                )
                Text(
                    text = "Every recognized ingredient is checked against your Room-saved Medical Allergies, Personal Sensitivities, and Preference Avoidances alongside EU SCCS, US CIR, and FDA toxicology records.",
                    fontSize = 12.sp,
                    color = OliveDrabDark
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SemanticCautionBg,
                    border = BorderStroke(1.dp, SemanticCaution.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "What Purescan Cannot Verify",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SemanticCaution
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Exact chemical concentrations not printed on the label\n• Cross-contamination during manufacturing\n• Unrecognized or blurred OCR words (always flagged for manual review)\n• Universal medical safety for every individual",
                            fontSize = 11.sp,
                            color = SmokyBlack
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onStartScan()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OliveDrab),
                modifier = Modifier.testTag("how_it_works_scan_now_button")
            ) {
                Text("Scan a Product Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SmokyBlack)
            }
        }
    )
}

/**
 * 4-Question Clinical Trust Summary Card (Section 4 & P0 of UX Readiness Review).
 * Answers:
 * 1. What product or ingredient list was recognized? (with Review/Correct OCR CTA)
 * 2. Did the scan identify any matches against my selected rules? (with exact rule reason)
 * 3. What evidence supports the finding?
 * 4. What should I do if the result is uncertain? (non-universal safety notice)
 */
@Composable
fun TrustResultSummaryCard(
    report: ScanReport,
    selectedLanguage: AppLanguage,
    onEditRecognizedText: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localized = AppLocalization.stringsFor(selectedLanguage)
    val matchedCount = report.ingredients.count { it.matchStatus == MatchStatus.MATCHED }
    val unmatchedCount = report.unmatchedIngredients.size
    val totalCount = report.ingredients.size
    val citationCount = report.geminiInsight?.groundingCitations?.size ?: report.findings.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clinical_trust_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = OliveDrab,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = localized.trustSummaryHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BoneLight
                ) {
                    Text(
                        text = "${selectedLanguage.badge} Report",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OliveDrabDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Q1: Ingredient list recognized + Review CTA
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BoneLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Ingredient List Recognized ($totalCount items)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack
                        )
                        Text(
                            text = if (unmatchedCount == 0) {
                                "$matchedCount/$totalCount verified in canonical catalog • Review extracted text anytime"
                            } else {
                                "$matchedCount verified • $unmatchedCount unverified token(s) require manual label check"
                            },
                            fontSize = 11.sp,
                            color = OliveDrabDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onEditRecognizedText,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("trust_card_review_ocr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = OliveDrab
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Text", fontSize = 11.sp, color = OliveDrab, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Q2: Personal rule match status with exact reason
            val hasRuleMatches = report.preferenceNotes.isNotEmpty()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hasRuleMatches) SemanticCriticalBg else SemanticSuccessBg,
                border = BorderStroke(
                    1.dp,
                    if (hasRuleMatches) SemanticCritical.copy(alpha = 0.35f) else SemanticSuccess.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasRuleMatches) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (hasRuleMatches) SemanticCritical else SemanticSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasRuleMatches) {
                                "2. Confirmed Match to Your Personal Rules (${report.preferenceNotes.size})"
                            } else {
                                "2. No Matches Against Your Selected Personal Rules"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasRuleMatches) SemanticCritical else SemanticSuccess
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (hasRuleMatches) {
                        report.preferenceNotes.take(3).forEach { note ->
                            Text(
                                text = "• ${note.triggeredIngredient} → ${note.prefLabel}: ${note.note}",
                                fontSize = 11.sp,
                                color = SmokyBlack,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "None of the recognized ingredients triggered your active Medical Allergy, Sensitivity, or Avoidance rules.",
                            fontSize = 11.sp,
                            color = SmokyBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Q3 & Q4: Evidence & Uncertainty / Non-Universal Safety Notice
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = OliveDrabDark,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "3. Evidence & Limitations ($citationCount regulatory sources checked)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Text(
                        text = localized.nonUniversalSafetyNotice,
                        fontSize = 11.sp,
                        color = OliveDrabDark,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TrustResultSummaryCard(
    recognizedCount: Int,
    ocrConfidence: Int,
    matchedRules: List<String>,
    regulatoryNotes: List<String>,
    language: AppLanguage,
    onReviewOcrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localized = AppLocalization.stringsFor(language)
    val confidencePercent = ocrConfidence.coerceIn(0, 100)
    val hasRuleMatches = matchedRules.isNotEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clinical_trust_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = OliveDrab,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = localized.trustSummaryHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BoneLight
                ) {
                    Text(
                        text = "${language.badge} • ${confidencePercent}% OCR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OliveDrabDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Q1: Ingredient list recognized + Review CTA
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BoneLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Ingredient List Recognized ($recognizedCount items)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack
                        )
                        Text(
                            text = "OCR Confidence: $confidencePercent% • Tap to inspect or correct extracted label text",
                            fontSize = 11.sp,
                            color = OliveDrabDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onReviewOcrClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("trust_card_review_ocr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = OliveDrab
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Text", fontSize = 11.sp, color = OliveDrab, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Q2: Personal rule match status with exact reason
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hasRuleMatches) SemanticCriticalBg else SemanticSuccessBg,
                border = BorderStroke(
                    1.dp,
                    if (hasRuleMatches) SemanticCritical.copy(alpha = 0.35f) else SemanticSuccess.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasRuleMatches) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (hasRuleMatches) SemanticCritical else SemanticSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasRuleMatches) {
                                "2. Confirmed Match to Your Personal Rules (${matchedRules.size})"
                            } else {
                                "2. No Matches Against Your Selected Personal Rules"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasRuleMatches) SemanticCritical else SemanticSuccess
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (hasRuleMatches) {
                        matchedRules.take(3).forEach { ruleSummary ->
                            Text(
                                text = "• $ruleSummary",
                                fontSize = 11.sp,
                                color = SmokyBlack,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "None of the recognized ingredients triggered your active Medical Allergy, Sensitivity, or Avoidance rules.",
                            fontSize = 11.sp,
                            color = SmokyBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Q3 & Q4: Evidence & Uncertainty / Non-Universal Safety Notice
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = OliveDrabDark,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "3. Evidence & Limitations (${regulatoryNotes.size} regulatory sources checked)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Text(
                        text = localized.nonUniversalSafetyNotice,
                        fontSize = 11.sp,
                        color = OliveDrabDark,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
