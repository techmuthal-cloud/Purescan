package com.example

import com.example.data.catalog.EvidenceEngine
import com.example.data.catalog.IngredientCatalog
import com.example.data.local.PreferenceSettingEntity
import com.example.data.model.FindingType
import com.example.data.model.MatchStatus
import com.example.data.model.Severity
import com.example.data.model.SummaryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanSafeEvidenceTest {

    @Test
    fun parseIngredients_correctlyIdentifiesCanonicalMatches() {
        val rawInput = "Water, Glycerin, Niacinamide, Retinol"
        val parsed = EvidenceEngine.parseIngredientsText(rawInput)

        assertEquals(4, parsed.size)
        assertTrue(parsed.all { it.matchStatus == MatchStatus.MATCHED })
        assertEquals("Water / Aqua", parsed[0].canonicalIngredient?.canonicalName)
        assertEquals("Glycerin", parsed[1].canonicalIngredient?.canonicalName)
        assertEquals("Niacinamide (Vitamin B3)", parsed[2].canonicalIngredient?.canonicalName)
        assertEquals("Retinol", parsed[3].canonicalIngredient?.canonicalName)
    }

    @Test
    fun parseIngredients_preservesUnknownIngredientsWithoutFalseClaims() {
        val rawInput = "Water, UnknownSecretHerbExtractXYZ, Glycerin"
        val parsed = EvidenceEngine.parseIngredientsText(rawInput)

        assertEquals(3, parsed.size)
        val unknown = parsed.first { it.rawText == "UnknownSecretHerbExtractXYZ" }
        assertEquals(MatchStatus.UNMATCHED, unknown.matchStatus)
        assertEquals(null, unknown.canonicalIngredient)
    }

    @Test
    fun analyzeIngredients_flagsRetinolPregnancyAndRegulatoryConditions() {
        val sampleText = "Water, Glycerin, Retinol"
        val prefs = listOf(
            PreferenceSettingEntity(
                key = "pref_pregnancy_caution",
                label = "Pregnancy Caution",
                description = "Avoid retinoids",
                category = "special_care",
                isEnabled = true
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Retinol Night Oil",
            category = "skincare",
            region = "EU",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = prefs
        )

        assertTrue(report.findings.any { it.findingType == FindingType.REGULATORY_RESTRICTION && it.jurisdiction == "EU" })
        assertTrue(report.preferenceNotes.any { it.prefKey == "pref_pregnancy_caution" })
        assertEquals(SummaryStatus.CONCERNS_IDENTIFIED, report.summaryStatus)
    }

    @Test
    fun analyzeIngredients_cleanFormulaResultsInNoRelevantConcern() {
        val sampleText = "Water, Glycerin, Sodium Hyaluronate"
        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Hydrating Serum",
            category = "skincare",
            region = "US",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = emptyList()
        )

        assertEquals(SummaryStatus.NO_RELEVANT_CONCERN, report.summaryStatus)
        assertEquals(0, report.unmatchedIngredients.size)
    }

    @Test
    fun geminiService_flagsUserCustomAllergens() {
        val sampleText = "Aqua, Glycerin, Phenoxyethanol, Disodium EDTA"
        val customAllergens = listOf("Phenoxyethanol")

        val insight = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = sampleText,
            category = "skincare",
            userAllergens = customAllergens
        )

        assertEquals("WARNING", insight.safetyTier)
        assertTrue(insight.allergensDetected.any { it.contains("Phenoxyethanol") })
        assertTrue(insight.keyConcerns.any { it.contains("Phenoxyethanol") })
        assertEquals("Use with Caution", insight.headline)
    }

    @Test
    fun customAllergenEntity_initializesWithDefaults() {
        val entity = com.example.data.local.CustomAllergenEntity(
            name = "Linalool"
        )
        assertEquals("Linalool", entity.name)
        assertTrue(entity.isEnabled)
        assertNotNull(entity.id)
    }

    @Test
    fun geminiService_parsesStructuredJsonWithConfidenceAndSaferAlternatives() {
        val structuredJson = """
            {
              "safetyScore": 62,
              "safetyTier": "WARNING",
              "headline": "Use with Caution",
              "subheadline": "Contains sensitizing botanical oils.",
              "ocrConfidence": 94,
              "allergensDetected": ["Sweet Almond Oil"],
              "highlights": ["No banned substances"],
              "keyConcerns": ["Contains tree nut derivative"],
              "scientificExplanation": "Contains Prunus Amygdalus Dulcis Oil.",
              "safeUseTips": ["Patch test before use"],
              "brandName": "Lumina Derm",
              "saferAlternatives": [
                {
                  "productName": "Squalane Barrier Cream",
                  "brandName": "PureBotanica",
                  "safetyScore": 96,
                  "reason": "Nut-free and fragrance-free"
                }
              ],
              "ingredientBreakdowns": [
                {
                  "name": "Sweet Almond Oil",
                  "riskLevel": "CAUTION",
                  "role": "Emollient",
                  "regulatoryNote": "FALCPA / Tree Nut allergen disclosure"
                }
              ]
            }
        """.trimIndent()

        val insight = com.example.data.api.GeminiService.parseGeminiJson(
            rawJson = structuredJson,
            originalText = "Aqua, Prunus Amygdalus Dulcis (Sweet Almond) Oil, Glycerin",
            category = "Skin Care",
            userAllergens = listOf("Tree Nuts & Peanuts")
        )

        assertEquals(62, insight.safetyScore)
        assertEquals("WARNING", insight.safetyTier)
        assertEquals(94, insight.ocrConfidence)
        assertEquals(1, insight.saferAlternatives.size)
        assertEquals("Squalane Barrier Cream", insight.saferAlternatives.first().productName)
        assertEquals(1, insight.ingredientBreakdowns.size)
        assertEquals("CAUTION", insight.ingredientBreakdowns.first().riskLevel)
        assertTrue(insight.allergensDetected.any { it.contains("Tree Nuts", ignoreCase = true) })
    }

    @Test
    fun allergyFilter_escalatesHighRiskAndFlagsUnmatchedAndSynonymIngredients() {
        val sampleText = "Water, Glycerin, Prunus Amygdalus Dulcis (Sweet Almond) Oil, CustomRarePeanutExtract"
        val customAllergens = listOf(
            com.example.data.local.CustomAllergenEntity(
                name = "Peanut",
                isEnabled = true,
                severityLevel = "HIGH_RISK"
            )
        )
        val prefs = listOf(
            PreferenceSettingEntity(
                key = "pref_tree_nuts",
                label = "Tree Nuts & Peanuts",
                description = "Flag tree nuts",
                category = "dietary",
                isEnabled = true
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Botanical Body Lotion",
            category = "Skin Care",
            region = "US",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = prefs,
            customAllergens = customAllergens
        )

        // Verdict escalates to CONCERNS_IDENTIFIED (High Risk) because a HIGH_RISK allergen matched
        assertEquals(SummaryStatus.CONCERNS_IDENTIFIED, report.summaryStatus)
        // Both canonical Sweet Almond Oil and unmatched CustomRarePeanutExtract are tagged with matchedAllergenName
        val almondIng = report.ingredients.first { it.rawText.contains("Almond", ignoreCase = true) }
        val peanutIng = report.ingredients.first { it.rawText.contains("Peanut", ignoreCase = true) }
        assertNotNull(almondIng.matchedAllergenName)
        assertNotNull(peanutIng.matchedAllergenName)
        assertEquals(Severity.CRITICAL, peanutIng.allergenSeverity)
        assertTrue(report.findings.any { it.findingType == FindingType.ALLERGEN_ALERT && it.sourceOrg == "PureScan Allergy Filter" })
    }

    @Test
    fun geminiService_escalatesHighRiskWhenHighRiskAllergenMatches() {
        val sampleText = "Aqua, Glycerin, Phenoxyethanol"
        val insight = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = sampleText,
            category = "Skin Care",
            userAllergens = listOf("Phenoxyethanol"),
            highRiskAllergens = listOf("Phenoxyethanol")
        )

        assertEquals("HIGH_RISK", insight.safetyTier)
        assertTrue(insight.safetyScore <= 35)
        assertTrue(insight.allergensDetected.any { it.contains("Phenoxyethanol", ignoreCase = true) })
        val breakdown = insight.ingredientBreakdowns.first { it.name.contains("Phenoxyethanol", ignoreCase = true) }
        assertEquals("HIGH_RISK", breakdown.riskLevel)
        assertTrue(breakdown.regulatoryNote.contains("ALLERGEN MATCH", ignoreCase = true))
    }

    @Test
    fun categorizeReportIngredients_accuratelyBucketsSafeWarningAndHighHazard() {
        val sampleText = "Water, Glycerin, Salicylic Acid, Methylparaben"
        val customAllergens = listOf(
            com.example.data.local.CustomAllergenEntity(
                name = "Methylparaben",
                isEnabled = true,
                severityLevel = "HIGH_RISK"
            )
        )
        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Daily Lotion",
            category = "Skin Care",
            region = "EU",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = emptyList(),
            customAllergens = customAllergens
        )
        val gemini = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = sampleText,
            category = "Skin Care",
            userAllergens = listOf("Methylparaben"),
            highRiskAllergens = listOf("Methylparaben")
        )

        val categorized = com.example.ui.components.categorizeReportIngredients(report, gemini)
        assertEquals(4, categorized.size)

        val highHazard = categorized.filter { it.category == com.example.ui.components.IngredientHazardCategory.HIGH_HAZARD }
        val warning = categorized.filter { it.category == com.example.ui.components.IngredientHazardCategory.WARNING }
        val safe = categorized.filter { it.category == com.example.ui.components.IngredientHazardCategory.SAFE }

        assertTrue(highHazard.any { it.displayName.contains("Methylparaben", ignoreCase = true) })
        assertTrue(warning.any { it.displayName.contains("Salicylic Acid", ignoreCase = true) })
        assertTrue(safe.any { it.displayName.contains("Water", ignoreCase = true) || it.displayName.contains("Glycerin", ignoreCase = true) })
        assertTrue(categorized.all { it.casNumber.isNotBlank() && it.toxicologyMechanism.isNotBlank() })
    }
}


