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

    @Test
    fun allergyFilter_flagsGlutenNutsAndParabensAccurately() {
        val sampleText = "Water, Hydrolyzed Wheat Protein, Sweet Almond Oil, Methylparaben"
        val activePrefs = listOf(
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_gluten",
                label = "Gluten / Wheat Derivatives",
                description = "Flag hydrolyzed wheat protein",
                category = "allergen",
                isEnabled = true
            ),
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_tree_nuts",
                label = "Tree Nut & Peanut Allergens",
                description = "Flag almond and peanut",
                category = "allergen",
                isEnabled = true
            ),
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_paraben_free",
                label = "Paraben-Free",
                description = "Flag methylparaben",
                category = "cosmetic",
                isEnabled = true
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Gentle Nourishing Lotion",
            category = "Skin Care",
            region = "GLOBAL",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = activePrefs,
            customAllergens = emptyList()
        )

        val wheatItem = report.ingredients.first { it.rawText.contains("Wheat", ignoreCase = true) }
        val almondItem = report.ingredients.first { it.rawText.contains("Almond", ignoreCase = true) }
        val parabenItem = report.ingredients.first { it.rawText.contains("Methylparaben", ignoreCase = true) }

        assertNotNull(wheatItem.matchedAllergenName)
        assertTrue(wheatItem.matchedAllergenName!!.contains("Gluten", ignoreCase = true))

        assertNotNull(almondItem.matchedAllergenName)
        assertTrue(almondItem.matchedAllergenName!!.contains("Nut", ignoreCase = true))

        assertNotNull(parabenItem.matchedAllergenName)
        assertTrue(parabenItem.matchedAllergenName!!.contains("Paraben", ignoreCase = true))

        assertTrue(report.findings.any { it.sourceOrg == "PureScan Allergy Filter" })
    }

    @Test
    fun reportShareUtil_generatesFormattedTextWithAllergenAlerts() {
        val sampleText = "Water, Hydrolyzed Wheat Protein, Glycerin"
        val activePrefs = listOf(
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_gluten",
                label = "Gluten / Wheat Derivatives",
                description = "Flag wheat",
                category = "allergen",
                isEnabled = true
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Botanical Conditioner",
            category = "Hair Care",
            region = "US",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = activePrefs,
            customAllergens = emptyList()
        )
        val gemini = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = sampleText,
            category = "Hair Care",
            userAllergens = listOf("Gluten / Wheat"),
            highRiskAllergens = listOf("Gluten / Wheat")
        )

        val shareText = com.example.util.ReportShareUtil.generateShareableText(report, gemini)

        assertTrue(shareText.contains("PURESCAN PRODUCT SAFETY REPORT"))
        assertTrue(shareText.contains("Botanical Conditioner"))
        assertTrue(shareText.contains("PERSONAL ALLERGEN ALERT"))
        assertTrue(shareText.contains("Gluten"))
        assertTrue(shareText.contains("#PureScan"))
    }

    @Test
    fun scanEntity_defaultsToUnknownVerdictAndPendingStatus() {
        val defaultScan = com.example.data.local.ScanEntity(
            id = "pending-scan-1",
            title = "Unverified Capture",
            category = "Skin Care",
            regionCode = "US"
        )
        assertEquals(0, defaultScan.safetyScore)
        assertEquals("UNKNOWN", defaultScan.safetyTier)
        assertEquals(com.example.data.local.ScanStatus.PENDING.name, defaultScan.scanStatus)
        assertEquals(com.example.data.local.SafetyVerdict.UNKNOWN.name, defaultScan.safetyVerdict)
    }

    @Test
    fun appLanguage_supportsAllEightLanguagesAndLocalizesGeminiInsights() {
        val allLanguages = com.example.ui.util.AppLanguage.entries
        assertEquals(8, allLanguages.size)
        val expectedCodes = setOf("en", "es", "fr", "de", "hi", "ar", "ja", "pt")
        assertEquals(expectedCodes, allLanguages.map { it.code }.toSet())

        allLanguages.forEach { lang ->
            val scanTitle = com.example.ui.util.AppLocalization.scanProductTitle(lang)
            val scanSubtitle = com.example.ui.util.AppLocalization.scanProductSubtitle(lang)
            assertTrue(scanTitle.isNotBlank())
            assertTrue(scanSubtitle.isNotBlank())
        }

        val spanishInsight = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = "Aqua, Glycerin, Prunus Amygdalus Dulcis (Sweet Almond) Oil",
            category = "Skin Care",
            userAllergens = listOf("Sweet Almond"),
            highRiskAllergens = listOf("Sweet Almond"),
            languageCode = "es"
        )
        assertEquals("HIGH_RISK", spanishInsight.safetyTier)
        assertTrue(spanishInsight.headline.contains("Alto Riesgo", ignoreCase = true))
        assertTrue(spanishInsight.scientificExplanation.contains("[Informe en Español]"))
    }

    @Test
    fun analyzeCameraXTextWithProfile_respectsRoomRuleClassificationAndReasonNote() = kotlinx.coroutines.runBlocking {
        val cameraText = "Aqua, Glycerin, Parfum, Hydrolyzed Wheat Protein"
        val prefs = listOf(
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_fragrance_free",
                label = "Fragrance / Parfum",
                description = "Flag synthetic parfum",
                category = "fragrance",
                isEnabled = true,
                ruleClassification = "PERSONAL_SENSITIVITY",
                userReasonNote = "Triggers facial redness"
            ),
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_gluten",
                label = "Gluten & Wheat",
                description = "Flag wheat proteins",
                category = "dietary",
                isEnabled = true,
                ruleClassification = "MEDICAL_ALLERGY",
                userReasonNote = "Diagnosed celiac allergy"
            )
        )

        val insight = com.example.data.api.GeminiService.analyzeCameraXTextWithProfile(
            cameraXExtractedText = cameraText,
            category = "Skin Care",
            region = "US",
            preferences = prefs,
            customAllergens = emptyList(),
            languageCode = "en"
        )

        assertEquals("HIGH_RISK", insight.safetyTier)
        val wheatItem = insight.ingredientBreakdowns.first { it.name.contains("Wheat", ignoreCase = true) }
        val parfumItem = insight.ingredientBreakdowns.first { it.name.contains("Parfum", ignoreCase = true) }
        assertEquals("HIGH_RISK", wheatItem.riskLevel)
        assertTrue(wheatItem.regulatoryNote.contains("Medical Allergy", ignoreCase = true))
        assertTrue(wheatItem.regulatoryNote.contains("Diagnosed celiac allergy", ignoreCase = true))
        assertEquals("CAUTION", parfumItem.riskLevel)
        assertTrue(parfumItem.regulatoryNote.contains("Personal Sensitivity", ignoreCase = true))
    }

    @Test
    fun categorizeReportIngredients_assignsMatchCertaintyRuleClassificationAndUncertaintyStates() {
        val sampleText = "Water, Methylparaben, Prunus Amygdalus Dulcis (Sweet Almond) Oil, MysteryBotanicalTokenXYZ"
        val customAllergens = listOf(
            com.example.data.local.CustomAllergenEntity(
                name = "Methylparaben",
                isEnabled = true,
                severityLevel = "HIGH_RISK",
                ruleClassification = "MEDICAL_ALLERGY",
                notes = "Severe contact dermatitis"
            )
        )
        val prefs = listOf(
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_tree_nuts",
                label = "Tree Nuts & Peanuts",
                description = "Flag tree nuts",
                category = "dietary",
                isEnabled = true,
                ruleClassification = "PERSONAL_SENSITIVITY",
                userReasonNote = "Avoid almond oil derivatives"
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Test Botanical Serum",
            category = "Skin Care",
            region = "US",
            rawOcrText = sampleText,
            correctedText = sampleText,
            activePreferences = prefs,
            customAllergens = customAllergens
        )
        val gemini = com.example.data.api.GeminiService.generateFallbackInsight(
            ingredientsText = sampleText,
            category = "Skin Care",
            userAllergens = listOf("Methylparaben", "Tree Nuts & Peanuts"),
            highRiskAllergens = listOf("Methylparaben")
        )

        val entries = com.example.ui.components.categorizeReportIngredients(report, gemini)
        assertEquals(4, entries.size)

        val waterEntry = entries.first { it.displayName.contains("Water", ignoreCase = true) }
        assertEquals(com.example.ui.util.MatchCertainty.DETECTED_NO_RULE_MATCH, waterEntry.matchCertainty)
        assertEquals(false, waterEntry.isUncertainOrUnverified)

        val parabenEntry = entries.first { it.displayName.contains("Methylparaben", ignoreCase = true) }
        assertEquals(com.example.ui.util.MatchCertainty.CONFIRMED_RULE_MATCH, parabenEntry.matchCertainty)
        assertEquals("Medical Allergy", parabenEntry.ruleClassificationLabel)

        val almondEntry = entries.first { it.displayName.contains("Almond", ignoreCase = true) }
        assertEquals(com.example.ui.util.MatchCertainty.POSSIBLE_DERIVATIVE_MATCH, almondEntry.matchCertainty)
        assertTrue(almondEntry.isUncertainOrUnverified)

        val mysteryEntry = entries.first { it.rawLabelText == "MysteryBotanicalTokenXYZ" }
        assertEquals(com.example.ui.util.MatchCertainty.EVIDENCE_INSUFFICIENT, mysteryEntry.matchCertainty)
        assertTrue(mysteryEntry.isUncertainOrUnverified)
        assertEquals("Unverified Label Token", mysteryEntry.ruleClassificationLabel)
    }
}



