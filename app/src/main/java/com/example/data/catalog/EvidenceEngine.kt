package com.example.data.catalog

import com.example.data.local.PreferenceSettingEntity
import com.example.data.model.CanonicalIngredient
import com.example.data.model.FindingType
import com.example.data.model.MatchStatus
import com.example.data.model.ParsedIngredient
import com.example.data.model.PreferenceNote
import com.example.data.model.ReportFinding
import com.example.data.model.ScanReport
import com.example.data.model.Severity
import com.example.data.model.SummaryStatus
import java.util.UUID

object EvidenceEngine {

    /**
     * Splits raw OCR or user input text into clean candidate tokens
     */
    fun parseIngredientsText(rawText: String): List<ParsedIngredient> {
        if (rawText.isBlank()) return emptyList()

        // Remove introductory noise like "Ingredients:", "Contains:", "Active ingredients:"
        val cleanIntro = rawText
            .replace(Regex("""(?i)^(ingredients|inactive ingredients|active ingredients|contains|composition)\s*:\s*"""), "")

        // Split by commas, semicolons, bullets, or newlines
        val rawTokens = cleanIntro.split(Regex("""[,;\n•·]+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 1 }

        return rawTokens.map { token ->
            val canonical = IngredientCatalog.findByAlias(token)
            if (canonical != null) {
                // High confidence match
                val isUncertain = token.contains("?") || token.length < 3
                ParsedIngredient(
                    rawText = token,
                    normalizedText = IngredientCatalog.normalizeString(token),
                    canonicalIngredient = canonical,
                    matchStatus = MatchStatus.MATCHED,
                    matchConfidence = if (isUncertain) 0.78f else 0.98f,
                    isUncertain = isUncertain
                )
            } else {
                // Unmatched - exact text is preserved
                ParsedIngredient(
                    rawText = token,
                    normalizedText = IngredientCatalog.normalizeString(token),
                    canonicalIngredient = null,
                    matchStatus = MatchStatus.UNMATCHED,
                    matchConfidence = 0.0f,
                    isUncertain = true
                )
            }
        }
    }

    /**
     * Evaluates deterministic rules against parsed ingredients, user preferences, custom allergens, and region
     */
    fun analyzeIngredients(
        scanId: String = UUID.randomUUID().toString(),
        productTitle: String,
        category: String,
        region: String,
        rawOcrText: String,
        correctedText: String,
        activePreferences: List<PreferenceSettingEntity>,
        customAllergens: List<com.example.data.local.CustomAllergenEntity> = emptyList()
    ): ScanReport {
        val rawParsed = parseIngredientsText(correctedText.ifBlank { rawOcrText })

        val findings = mutableListOf<ReportFinding>()
        val preferenceNotes = mutableListOf<PreferenceNote>()

        val enrichedParsed = rawParsed.map { item ->
            val canonical = item.canonicalIngredient
            val displayIngName = canonical?.canonicalName ?: item.rawText

            // 1. Gather reviewed evidence from matched canonical ingredients
            if (canonical != null) {
                for (record in canonical.evidenceRecords) {
                    val isJurisdictionRelevant = record.jurisdiction == "GLOBAL" ||
                        record.jurisdiction.contains(region, ignoreCase = true) ||
                        region == "GLOBAL"

                    if (isJurisdictionRelevant) {
                        findings.add(
                            ReportFinding(
                                id = UUID.randomUUID().toString(),
                                ingredientName = canonical.canonicalName,
                                category = canonical.category,
                                findingType = record.findingType,
                                severity = record.severity,
                                title = "${canonical.canonicalName} (${record.organization})",
                                explanation = record.summary,
                                evidenceStrength = record.evidenceStrength,
                                conditions = record.conditions,
                                jurisdiction = record.jurisdiction,
                                sourceOrg = record.organization,
                                sourceUrl = record.sourceUrl,
                                reviewDate = record.reviewDate
                            )
                        )
                    }
                }
            }

            // 2. Evaluate active personal preferences & custom allergens across BOTH matched and unmatched tokens
            val allergenMatches = IngredientCatalog.matchUserAllergens(
                rawToken = item.rawText,
                canonical = canonical,
                activePreferences = activePreferences,
                customAllergens = customAllergens
            )

            allergenMatches.forEach { match ->
                preferenceNotes.add(
                    PreferenceNote(
                        prefKey = match.prefKey,
                        prefLabel = match.allergenLabel,
                        triggeredIngredient = displayIngName,
                        note = match.explanation,
                        severity = match.severity
                    )
                )

                // Also emit a deterministic ALLERGEN_ALERT finding so it surfaces in the Safety tab
                findings.add(
                    ReportFinding(
                        id = UUID.randomUUID().toString(),
                        ingredientName = displayIngName,
                        category = canonical?.category ?: "User Allergy Filter",
                        findingType = FindingType.ALLERGEN_ALERT,
                        severity = match.severity,
                        title = "$displayIngName • ${match.allergenLabel}",
                        explanation = match.explanation,
                        evidenceStrength = "User Profile Rule",
                        conditions = if (match.severity == Severity.CRITICAL) {
                            "Escalated to High Risk per your Allergy Filter settings"
                        } else {
                            "Flagged with Caution per your Allergy Filter settings"
                        },
                        jurisdiction = "PERSONAL",
                        sourceOrg = "PureScan Allergy Filter",
                        sourceUrl = "https://www.fda.gov/food/food-labeling-nutrition/food-allergies",
                        reviewDate = "Active Rule"
                    )
                )
            }

            if (allergenMatches.isNotEmpty()) {
                val highestSeverity = if (allergenMatches.any { it.severity == Severity.CRITICAL }) {
                    Severity.CRITICAL
                } else {
                    Severity.CAUTION
                }
                val combinedLabel = allergenMatches.joinToString(", ") { it.allergenLabel }
                item.copy(
                    matchedAllergenName = combinedLabel,
                    allergenSeverity = highestSeverity
                )
            } else {
                item
            }
        }

        val unmatched = enrichedParsed.filter { it.matchStatus == MatchStatus.UNMATCHED }

        // Determine summary status
        val hasCritical = findings.any { it.severity == Severity.CRITICAL } ||
            preferenceNotes.any { it.severity == Severity.CRITICAL }
        val hasCaution = findings.any { it.severity == Severity.CAUTION } || preferenceNotes.isNotEmpty()
        val hasUnknowns = unmatched.isNotEmpty()

        val summaryStatus = when {
            hasCritical || hasCaution -> SummaryStatus.CONCERNS_IDENTIFIED
            hasUnknowns -> SummaryStatus.COMPLETED_WITH_UNKNOWNS
            else -> SummaryStatus.NO_RELEVANT_CONCERN
        }

        val limitations = buildString {
            append("Report compiled against reviewed CIR, EU SCCS, FDA records, and your personal Allergy Filter. ")
            if (unmatched.isNotEmpty()) {
                append("${unmatched.size} ingredient(s) could not be matched to canonical catalog records and require manual check. Unmatched items prove neither harm nor safety. ")
            }
            append("Not a guarantee of 100% universal product safety, medical diagnosis, or counterfeit detection.")
        }

        return ScanReport(
            scanId = scanId,
            productTitle = productTitle.ifBlank { "Scanned Product" },
            category = category,
            region = region,
            rawOcrText = rawOcrText,
            correctedText = correctedText,
            ingredients = enrichedParsed,
            findings = findings,
            preferenceNotes = preferenceNotes.distinctBy { it.triggeredIngredient + it.prefKey },
            unmatchedIngredients = unmatched,
            summaryStatus = summaryStatus,
            timestamp = System.currentTimeMillis(),
            limitationsNote = limitations
        )
    }
}
