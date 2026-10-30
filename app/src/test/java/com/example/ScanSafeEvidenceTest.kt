package com.example

import com.example.data.catalog.EvidenceEngine
import com.example.data.catalog.IngredientCatalog
import com.example.data.local.PreferenceSettingEntity
import com.example.data.model.FindingType
import com.example.data.model.MatchStatus
import com.example.data.model.Severity
import com.example.data.model.SummaryStatus
import com.example.ui.components.toSafetyBadgeSpec
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

    @Test
    fun cloudSyncService_mergesLocalAndRemoteScansAndAllergenRulesBiDirectionally() {
        com.example.data.sync.CloudSyncService.clearInMemoryAndDiskForTest()
        val email = "priya.sync@scansafe.ai"

        // Device A uploads 1 scan and 1 custom allergen rule
        val deviceAScan = com.example.data.sync.CloudScanBundle(
            scan = com.example.data.local.ScanEntity(
                id = "scan-device-a-1",
                title = "Device A Hydrating Cleanser",
                category = "Skin Care",
                regionCode = "US",
                status = "completed",
                summaryStatus = "NO_RELEVANT_CONCERN",
                correctedText = "Aqua, Glycerin, Ceramide NP",
                safetyScore = 92,
                safetyTier = "SAFE"
            )
        )
        val deviceACustomAllergen = com.example.data.local.CustomAllergenEntity(
            id = "custom-a-1",
            name = "Methylisothiazolinone",
            isEnabled = true,
            severityLevel = "HIGH_RISK",
            ruleClassification = "MEDICAL_ALLERGY",
            notes = "Patch test confirmed"
        )

        val (_, summaryA) = com.example.data.sync.CloudSyncService.mergeLocalAndRemote(
            userEmail = email,
            localScans = listOf(deviceAScan),
            localProfiles = emptyList(),
            localPreferences = emptyList(),
            localCustomAllergens = listOf(deviceACustomAllergen)
        )
        assertEquals(1, summaryA.totalScansSynced)
        assertEquals(1, summaryA.uploadedScansCount)
        assertEquals(1, summaryA.totalRulesSynced)

        // Device B has a different offline scan and signs in with the same email
        val deviceBScan = com.example.data.sync.CloudScanBundle(
            scan = com.example.data.local.ScanEntity(
                id = "scan-device-b-2",
                title = "Device B Almond Night Oil",
                category = "Skin Care",
                regionCode = "EU",
                status = "completed",
                summaryStatus = "CONCERNS_IDENTIFIED",
                correctedText = "Aqua, Prunus Amygdalus Dulcis Oil, Parfum",
                safetyScore = 34,
                safetyTier = "HIGH_RISK"
            )
        )
        val deviceBCustomAllergen = com.example.data.local.CustomAllergenEntity(
            id = "custom-b-2",
            name = "Linalool",
            isEnabled = true,
            severityLevel = "WARNING",
            ruleClassification = "PERSONAL_SENSITIVITY",
            notes = "Fragrance terpene irritation"
        )

        val (mergedSnapshotB, summaryB) = com.example.data.sync.CloudSyncService.mergeLocalAndRemote(
            userEmail = email,
            localScans = listOf(deviceBScan),
            localProfiles = emptyList(),
            localPreferences = emptyList(),
            localCustomAllergens = listOf(deviceBCustomAllergen)
        )

        // Both Device A and Device B scans and allergen rules must be present after bi-directional sync
        assertEquals(2, summaryB.totalScansSynced)
        assertEquals(1, summaryB.downloadedScansCount)
        assertEquals(1, summaryB.uploadedScansCount)
        assertEquals(2, mergedSnapshotB.scanBundles.size)
        assertEquals(2, mergedSnapshotB.customAllergens.size)
        assertTrue(mergedSnapshotB.scanBundles.any { it.scan.id == "scan-device-a-1" })
        assertTrue(mergedSnapshotB.scanBundles.any { it.scan.id == "scan-device-b-2" })
    }

    @Test
    fun barcodeLookupService_resolvesVerifiedBarcodesAndHandlesUnrecognizedFallback() = kotlinx.coroutines.runBlocking {
        // 1. Lookup verified CeraVe barcode (3337875597395)
        val foundResult = com.example.data.api.BarcodeLookupService.lookupBarcode(
            rawBarcode = "3337875597395",
            allowNetworkQuery = false
        )
        assertTrue(foundResult is com.example.data.api.BarcodeLookupResult.Found)
        val foundProduct = (foundResult as com.example.data.api.BarcodeLookupResult.Found).product
        assertEquals("CeraVe Moisturizing Cream", foundProduct.productTitle)
        assertEquals("CeraVe", foundProduct.brandName)
        assertTrue(foundProduct.ingredientsText.contains("Ceramide NP", ignoreCase = true))

        // 2. Lookup verified high-allergen elixir barcode (4005808811571) and evaluate against nut/paraben rules
        val elixirLookup = com.example.data.api.BarcodeLookupService.lookupBarcode(
            rawBarcode = "4005808811571",
            allowNetworkQuery = false
        )
        assertTrue(elixirLookup is com.example.data.api.BarcodeLookupResult.Found)
        val elixirProduct = (elixirLookup as com.example.data.api.BarcodeLookupResult.Found).product
        val prefs = listOf(
            com.example.data.local.PreferenceSettingEntity(
                key = "pref_tree_nuts",
                label = "Tree Nuts & Peanuts",
                description = "Flag almond oil",
                category = "allergen",
                isEnabled = true,
                ruleClassification = "MEDICAL_ALLERGY"
            )
        )
        val report = EvidenceEngine.analyzeIngredients(
            productTitle = elixirProduct.productTitle,
            category = elixirProduct.category,
            region = "US",
            rawOcrText = elixirProduct.ingredientsText,
            correctedText = elixirProduct.ingredientsText,
            activePreferences = prefs,
            customAllergens = emptyList()
        )
        assertTrue(report.preferenceNotes.isNotEmpty())

        // 3. Unrecognized barcode returns NotFound with fallback guidance
        val missingResult = com.example.data.api.BarcodeLookupService.lookupBarcode(
            rawBarcode = "9999999999999",
            allowNetworkQuery = false
        )
        assertTrue(missingResult is com.example.data.api.BarcodeLookupResult.NotFound)
        val notFound = missingResult as com.example.data.api.BarcodeLookupResult.NotFound
        assertEquals("9999999999999", notFound.barcode)
        assertTrue(notFound.reason.contains("not found", ignoreCase = true))
    }

    @Test
    fun historyFilterEngine_filtersByNameDateRangeAndSafetyRatingCategories() {
        val nowMs = 1_760_000_000_000L
        val oneHourAgo = nowMs - (1L * 60L * 60L * 1000L)
        val threeDaysAgo = nowMs - (3L * 24L * 60L * 60L * 1000L)
        val twentyDaysAgo = nowMs - (20L * 24L * 60L * 60L * 1000L)
        val sixtyDaysAgo = nowMs - (60L * 24L * 60L * 60L * 1000L)

        val scans = listOf(
            com.example.data.local.ScanEntity(
                id = "scan-1",
                title = "CeraVe Hydrating Cleanser",
                brandName = "CeraVe",
                category = "skincare",
                regionCode = "US",
                correctedText = "Aqua, Glycerin, Ceramide NP",
                safetyScore = 94,
                safetyTier = "SAFE",
                summaryStatus = "NO_RELEVANT_CONCERN",
                createdAt = oneHourAgo
            ),
            com.example.data.local.ScanEntity(
                id = "scan-2",
                title = "Botanical Citrus Mist",
                brandName = "AromaGlow",
                category = "skincare",
                regionCode = "EU",
                correctedText = "Aqua, Limonene, Linalool, Parfum",
                safetyScore = 58,
                safetyTier = "WARNING",
                summaryStatus = "CONCERNS_IDENTIFIED",
                createdAt = threeDaysAgo
            ),
            com.example.data.local.ScanEntity(
                id = "scan-3",
                title = "Intense Almond Night Elixir",
                brandName = "LuxeDerm",
                category = "skincare",
                regionCode = "US",
                correctedText = "Prunus Amygdalus Dulcis Oil, Methylparaben",
                safetyScore = 24,
                safetyTier = "HIGH_RISK",
                summaryStatus = "CONCERNS_IDENTIFIED",
                createdAt = twentyDaysAgo
            ),
            com.example.data.local.ScanEntity(
                id = "scan-4",
                title = "Vintage Herbal Balm",
                brandName = "OldApothecary",
                category = "cosmetics",
                regionCode = "US",
                correctedText = "UnknownBotanicalExtract99",
                unmatchedCount = 1,
                safetyScore = 50,
                safetyTier = "UNKNOWN",
                summaryStatus = "COMPLETED_WITH_UNKNOWNS",
                createdAt = sixtyDaysAgo
            )
        )

        // 1. Voice-to-text query normalization + search by product name
        val voiceFiltered = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "Search for CeraVe",
            safetyFilter = "all",
            dateFilter = com.example.ui.util.HistoryDateFilter.ALL,
            nowEpochMs = nowMs
        )
        assertEquals(1, voiceFiltered.size)
        assertEquals("scan-1", voiceFiltered.first().id)

        // 2. Filter by Date Range: TODAY, LAST_7_DAYS, LAST_30_DAYS
        val todayScans = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "all",
            dateFilter = com.example.ui.util.HistoryDateFilter.TODAY,
            nowEpochMs = nowMs
        )
        assertEquals(1, todayScans.size)
        assertEquals("scan-1", todayScans.first().id)

        val weekScans = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "all",
            dateFilter = com.example.ui.util.HistoryDateFilter.LAST_7_DAYS,
            nowEpochMs = nowMs
        )
        assertEquals(2, weekScans.size)

        val monthScans = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "all",
            dateFilter = com.example.ui.util.HistoryDateFilter.LAST_30_DAYS,
            nowEpochMs = nowMs
        )
        assertEquals(3, monthScans.size)

        // 3. Filter by Safety Rating Categories: safe, caution, high_risk, unknowns
        val safeOnly = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "safe",
            dateFilter = com.example.ui.util.HistoryDateFilter.ALL,
            nowEpochMs = nowMs
        )
        assertEquals(1, safeOnly.size)
        assertEquals("scan-1", safeOnly.first().id)

        val cautionOnly = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "caution",
            dateFilter = com.example.ui.util.HistoryDateFilter.ALL,
            nowEpochMs = nowMs
        )
        assertEquals(1, cautionOnly.size)
        assertEquals("scan-2", cautionOnly.first().id)

        val highRiskOnly = com.example.ui.util.HistoryAndImpactAnalytics.filterHistoryScans(
            scans = scans,
            searchQuery = "",
            safetyFilter = "high_risk",
            dateFilter = com.example.ui.util.HistoryDateFilter.ALL,
            nowEpochMs = nowMs
        )
        assertEquals(1, highRiskOnly.size)
        assertEquals("scan-3", highRiskOnly.first().id)
    }

    @Test
    fun allergyImpactCalculator_computesScoreAndTriggeredAllergensFromProfileAndLastFiveScans() {
        val preferences = listOf(
            PreferenceSettingEntity(
                key = "pref_fragrance_free",
                label = "Fragrance & Parfum Free",
                description = "Avoid synthetic fragrance",
                category = "allergen",
                isEnabled = true,
                ruleClassification = "PERSONAL_SENSITIVITY"
            ),
            PreferenceSettingEntity(
                key = "pref_tree_nuts",
                label = "Tree Nuts & Peanuts",
                description = "Avoid almond and nut oils",
                category = "allergen",
                isEnabled = true,
                ruleClassification = "MEDICAL_ALLERGY"
            )
        )
        val customAllergens = listOf(
            com.example.data.local.CustomAllergenEntity(
                id = "cust-1",
                name = "Methylparaben",
                isEnabled = true,
                severityLevel = "HIGH_RISK",
                ruleClassification = "MEDICAL_ALLERGY"
            )
        )
        val activeProfile = com.example.data.local.AllergenProfileEntity(
            id = "prof-1",
            profileName = "Sensitive & Nut-Free",
            description = "3 active rules",
            isActive = true
        )

        // Provide 6 scans to verify only the last 5 are evaluated
        val scans = listOf(
            com.example.data.local.ScanEntity(
                id = "s1",
                title = "Almond Night Cream",
                category = "skincare",
                regionCode = "US",
                correctedText = "Aqua, Prunus Amygdalus Dulcis (Sweet Almond) Oil, Methylparaben",
                safetyScore = 25,
                safetyTier = "HIGH_RISK"
            ),
            com.example.data.local.ScanEntity(
                id = "s2",
                title = "Floral Body Mist",
                category = "skincare",
                regionCode = "US",
                correctedText = "Aqua, Parfum, Linalool",
                safetyScore = 55,
                safetyTier = "WARNING"
            ),
            com.example.data.local.ScanEntity(
                id = "s3",
                title = "Pure Squalane Oil",
                category = "skincare",
                regionCode = "US",
                correctedText = "Squalane, Tocopherol",
                safetyScore = 96,
                safetyTier = "SAFE"
            ),
            com.example.data.local.ScanEntity(
                id = "s4",
                title = "Ceramide Barrier Gel",
                category = "skincare",
                regionCode = "US",
                correctedText = "Aqua, Glycerin, Ceramide NP",
                safetyScore = 92,
                safetyTier = "SAFE"
            ),
            com.example.data.local.ScanEntity(
                id = "s5",
                title = "Botanical Rose Toner",
                category = "skincare",
                regionCode = "US",
                correctedText = "Aqua, Fragrance, Citronellol",
                safetyScore = 60,
                safetyTier = "WARNING"
            ),
            com.example.data.local.ScanEntity(
                id = "s6-older",
                title = "Older Ignored Scan",
                category = "skincare",
                regionCode = "US",
                correctedText = "Methylparaben",
                safetyScore = 20,
                safetyTier = "HIGH_RISK"
            )
        )

        val summary = com.example.ui.util.HistoryAndImpactAnalytics.calculateAllergyImpact(
            allScans = scans,
            preferences = preferences,
            customAllergens = customAllergens,
            activeProfile = activeProfile
        )

        assertEquals("Sensitive & Nut-Free", summary.activeProfileName)
        assertEquals(3, summary.enabledRulesCount)
        assertEquals(5, summary.recentFiveScans.size)
        assertEquals(3, summary.flaggedScansCount)
        assertTrue(summary.impactScore > 0)
        assertEquals("HIGH", summary.impactTier)
        assertTrue(summary.triggeredAllergens.any { it.allergenName.contains("Methylparaben", ignoreCase = true) })
        assertTrue(summary.triggeredAllergens.any { it.allergenName.contains("Tree Nuts", ignoreCase = true) })
        assertTrue(summary.triggeredAllergens.any { it.allergenName.contains("Fragrance", ignoreCase = true) && it.hitCount == 2 })
    }

    @Test
    fun trafficLightSafetyBadge_assignsGreenYellowUnverifiedAndRedCorrectly() {
        val sampleText = "Water, Salicylic Acid, Methylparaben, UnrecognizedBotanicalExtract77"
        val customAllergens = listOf(
            com.example.data.local.CustomAllergenEntity(
                name = "Methylparaben",
                isEnabled = true,
                severityLevel = "HIGH_RISK",
                ruleClassification = "MEDICAL_ALLERGY",
                notes = "Confirmed allergy"
            )
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Traffic Light Test Serum",
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

        val entries = com.example.ui.components.categorizeReportIngredients(report, gemini)
        assertEquals(4, entries.size)

        // 1. Water -> GREEN_SAFE (activeLampIndex = 2, pillLabel = "SAFE")
        val waterEntry = entries.first { it.displayName.contains("Water", ignoreCase = true) }
        val waterSpec = waterEntry.toSafetyBadgeSpec()
        assertEquals(com.example.ui.components.TrafficLightBadgeSeverity.GREEN_SAFE, waterSpec.severity)
        assertEquals(2, waterSpec.severity.activeLampIndex)
        assertEquals("SAFE", waterSpec.severity.pillLabel)
        assertEquals(false, waterSpec.isUnverified)

        // 2. Salicylic Acid -> YELLOW_CAUTION (activeLampIndex = 1, pillLabel = "CAUTION")
        val salicylicEntry = entries.first { it.displayName.contains("Salicylic", ignoreCase = true) }
        val salicylicSpec = salicylicEntry.toSafetyBadgeSpec()
        assertEquals(com.example.ui.components.TrafficLightBadgeSeverity.YELLOW_CAUTION, salicylicSpec.severity)
        assertEquals(1, salicylicSpec.severity.activeLampIndex)
        assertEquals("CAUTION", salicylicSpec.severity.pillLabel)

        // 3. Methylparaben -> RED_HIGH_HAZARD (activeLampIndex = 0, pillLabel = "HIGH HAZARD")
        val parabenEntry = entries.first { it.displayName.contains("Methylparaben", ignoreCase = true) }
        val parabenSpec = parabenEntry.toSafetyBadgeSpec()
        assertEquals(com.example.ui.components.TrafficLightBadgeSeverity.RED_HIGH_HAZARD, parabenSpec.severity)
        assertEquals(0, parabenSpec.severity.activeLampIndex)
        assertEquals("HIGH HAZARD", parabenSpec.severity.pillLabel)

        // 4. UnrecognizedBotanicalExtract77 -> YELLOW_UNVERIFIED (Yellow caution tier + UNVERIFIED tag)
        val unverifiedEntry = entries.first { it.rawLabelText == "UnrecognizedBotanicalExtract77" }
        assertEquals(com.example.ui.components.IngredientHazardCategory.WARNING, unverifiedEntry.category)
        val unverifiedSpec = unverifiedEntry.toSafetyBadgeSpec()
        assertEquals(com.example.ui.components.TrafficLightBadgeSeverity.YELLOW_UNVERIFIED, unverifiedSpec.severity)
        assertEquals(1, unverifiedSpec.severity.activeLampIndex)
        assertTrue(unverifiedSpec.severity.pillLabel.contains("UNVERIFIED"))
        assertTrue(unverifiedSpec.isUnverified)
    }
}






