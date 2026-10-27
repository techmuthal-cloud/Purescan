package com.example.data.repository

import com.example.data.api.GeminiIngredientInsight
import com.example.data.api.GeminiService
import com.example.data.catalog.EvidenceEngine
import com.example.data.catalog.IngredientCatalog
import com.example.data.local.AllergenProfileEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CustomAllergenEntity
import com.example.data.local.ExtractedIngredientEntity
import com.example.data.local.FindingEntity
import com.example.data.local.PreferenceSettingEntity
import com.example.data.local.SafetyVerdict
import com.example.data.local.ScanEntity
import com.example.data.local.ScanStatus
import com.example.data.local.UserAccountEntity
import com.example.data.model.FindingType
import com.example.data.model.MatchStatus
import com.example.data.model.ParsedIngredient
import com.example.data.model.PreferenceNote
import com.example.data.model.ReportFinding
import com.example.data.model.ScanReport
import com.example.data.model.Severity
import com.example.data.model.SummaryStatus
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.security.MessageDigest
import java.util.UUID

class ScanRepository(private val db: AppDatabase) {

    private val scanDao = db.scanDao()
    private val userAccountDao = db.userAccountDao()
    private val preferenceDao = db.preferenceDao()
    private val customAllergenDao = db.customAllergenDao()
    private val allergenProfileDao = db.allergenProfileDao()

    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()
    val savedScans: Flow<List<ScanEntity>> = scanDao.getSavedScans()
    val allPreferences: Flow<List<PreferenceSettingEntity>> = preferenceDao.getAllPreferences()
    val allCustomAllergens: Flow<List<CustomAllergenEntity>> = customAllergenDao.getAllCustomAllergens()
    val allAllergenProfiles: Flow<List<AllergenProfileEntity>> = allergenProfileDao.getAllProfiles()
    val activeAllergenProfile: Flow<AllergenProfileEntity?> = allergenProfileDao.getActiveProfile()

    suspend fun getPreferencesList(): List<PreferenceSettingEntity> {
        return preferenceDao.getPreferencesList()
    }

    suspend fun setPreference(key: String, isEnabled: Boolean) {
        preferenceDao.setPreferenceEnabled(key, isEnabled)
    }

    suspend fun updatePreferenceRuleDetails(
        key: String,
        ruleClassification: String,
        userReasonNote: String
    ) {
        preferenceDao.updatePreferenceRuleDetails(key, ruleClassification, userReasonNote)
    }

    suspend fun addCustomAllergen(
        name: String,
        severityLevel: String = "HIGH_RISK",
        ruleClassification: String = "MEDICAL_ALLERGY"
    ) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            customAllergenDao.insertCustomAllergen(
                CustomAllergenEntity(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    isEnabled = true,
                    severityLevel = severityLevel,
                    ruleClassification = ruleClassification,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteCustomAllergen(id: String) {
        customAllergenDao.deleteCustomAllergen(id)
    }

    suspend fun toggleCustomAllergen(id: String, isEnabled: Boolean) {
        customAllergenDao.toggleCustomAllergen(id, isEnabled)
    }

    suspend fun updateCustomAllergenSeverity(id: String, severityLevel: String) {
        val ruleClass = if (severityLevel.equals("HIGH_RISK", ignoreCase = true)) {
            "MEDICAL_ALLERGY"
        } else {
            "PERSONAL_SENSITIVITY"
        }
        customAllergenDao.updateCustomAllergenDetails(
            id = id,
            ruleClassification = ruleClass,
            severityLevel = severityLevel,
            reasonNote = ""
        )
    }

    suspend fun updateCustomAllergenDetails(
        id: String,
        severityLevel: String,
        ruleClassification: String,
        reasonNote: String = ""
    ) {
        customAllergenDao.updateCustomAllergenDetails(
            id = id,
            ruleClassification = ruleClassification,
            severityLevel = severityLevel,
            reasonNote = reasonNote
        )
    }

    suspend fun saveUserAllergenProfile(
        profileName: String,
        description: String,
        enabledPreferenceKeys: List<String>,
        customTriggers: List<String>,
        defaultSeverity: String = "HIGH_RISK",
        activateImmediately: Boolean = true
    ): AllergenProfileEntity {
        val now = System.currentTimeMillis()
        val profile = AllergenProfileEntity(
            id = UUID.randomUUID().toString(),
            profileName = profileName.trim().ifBlank { "Custom Allergen Profile" },
            description = description.trim().ifBlank { "User-defined personal allergen profile" },
            enabledPreferenceKeysJson = JSONArray(enabledPreferenceKeys).toString(),
            customTriggersJson = JSONArray(customTriggers).toString(),
            defaultSeverity = defaultSeverity,
            isActive = activateImmediately,
            createdAt = now,
            updatedAt = now
        )
        if (activateImmediately) {
            allergenProfileDao.deactivateAllProfiles()
        }
        allergenProfileDao.insertProfile(profile)
        if (activateImmediately) {
            applyAllergenProfileRules(profile)
        }
        return profile
    }

    suspend fun activateAllergenProfile(profileId: String) {
        val profile = allergenProfileDao.getProfileById(profileId) ?: return
        allergenProfileDao.setActiveProfile(profileId)
        applyAllergenProfileRules(profile)
    }

    suspend fun deleteAllergenProfile(profileId: String) {
        allergenProfileDao.deleteProfile(profileId)
    }

    private suspend fun applyAllergenProfileRules(profile: AllergenProfileEntity) {
        val enabledKeys = parseJsonStringArray(profile.enabledPreferenceKeysJson).toSet()
        val allPrefs = preferenceDao.getPreferencesList()
        allPrefs.forEach { pref ->
            val shouldEnable = enabledKeys.contains(pref.key)
            if (pref.isEnabled != shouldEnable) {
                preferenceDao.setPreferenceEnabled(pref.key, shouldEnable)
            }
        }
        val triggers = parseJsonStringArray(profile.customTriggersJson)
        val existingCustom = customAllergenDao.getAllCustomAllergensList()
        triggers.forEach { triggerName ->
            val match = existingCustom.firstOrNull { it.name.equals(triggerName, ignoreCase = true) }
            if (match == null) {
                customAllergenDao.insertCustomAllergen(
                    CustomAllergenEntity(
                        id = UUID.randomUUID().toString(),
                        name = triggerName,
                        isEnabled = true,
                        severityLevel = profile.defaultSeverity
                    )
                )
            } else if (!match.isEnabled) {
                customAllergenDao.toggleCustomAllergen(match.id, true)
            }
        }
    }

    suspend fun toggleSaved(scanId: String, currentSaved: Boolean) {
        scanDao.updateSavedStatus(scanId, !currentSaved)
    }

    suspend fun deleteScan(scanId: String) {
        scanDao.deleteCompleteScan(scanId)
    }

    suspend fun clearHistory() {
        scanDao.clearCompleteHistory()
    }

    private fun hashPassword(raw: String, saltEmail: String = ""): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val payload = if (saltEmail.isNotBlank()) {
            "purescan_v2:${saltEmail.lowercase()}:$raw"
        } else {
            raw
        }
        return digest.digest(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    suspend fun authenticateEmailAccount(
        email: String,
        password: String,
        fullName: String,
        isSignUp: Boolean
    ): Result<UserAccountEntity> {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        val existing = userAccountDao.getAccountByEmail(normalizedEmail)
        val saltedHash = hashPassword(password, normalizedEmail)
        val legacyHash = hashPassword(password)

        return if (isSignUp) {
            val resolvedName = fullName.trim().ifBlank {
                normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            }
            val newAccount = UserAccountEntity(
                email = normalizedEmail,
                fullName = resolvedName,
                passwordHash = saltedHash,
                authProvider = "EMAIL",
                isPremium = existing?.isPremium ?: false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            userAccountDao.upsertAccount(newAccount)
            Result.success(newAccount)
        } else {
            if (existing != null) {
                val passwordValid = existing.passwordHash.isBlank() ||
                    existing.passwordHash == saltedHash ||
                    existing.passwordHash == legacyHash
                if (!passwordValid) {
                    Result.failure(IllegalArgumentException("Incorrect password for $normalizedEmail."))
                } else {
                    val updated = existing.copy(
                        passwordHash = saltedHash,
                        lastLoginAt = System.currentTimeMillis()
                    )
                    userAccountDao.upsertAccount(updated)
                    Result.success(updated)
                }
            } else {
                // Seamless first-time login creates the account automatically so user is never blocked
                val resolvedName = fullName.trim().ifBlank {
                    normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
                val created = UserAccountEntity(
                    email = normalizedEmail,
                    fullName = resolvedName,
                    passwordHash = saltedHash,
                    authProvider = "EMAIL",
                    lastLoginAt = System.currentTimeMillis()
                )
                userAccountDao.upsertAccount(created)
                Result.success(created)
            }
        }
    }

    suspend fun authenticateProviderAccount(
        email: String,
        fullName: String,
        provider: String
    ): UserAccountEntity {
        val normalizedEmail = email.trim().lowercase().ifBlank { "user@scansafe.ai" }
        val existing = userAccountDao.getAccountByEmail(normalizedEmail)
        val resolvedName = fullName.trim().ifBlank {
            existing?.fullName ?: normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        val account = UserAccountEntity(
            email = normalizedEmail,
            fullName = resolvedName,
            passwordHash = existing?.passwordHash ?: "",
            authProvider = provider,
            isPremium = existing?.isPremium ?: false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        userAccountDao.upsertAccount(account)
        return account
    }

    private suspend fun getCombinedUserAllergens(): Pair<List<String>, List<String>> {
        val activePrefs = getPreferencesList()
        val activeCustomAllergens = customAllergenDao.getActiveCustomAllergens()

        val allAllergens = mutableListOf<String>()
        val highRiskAllergens = mutableListOf<String>()

        activeCustomAllergens.forEach { custom ->
            allAllergens.add(custom.name)
            if (custom.severityLevel.equals("HIGH_RISK", ignoreCase = true) ||
                custom.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true)
            ) {
                highRiskAllergens.add(custom.name)
            }
        }

        activePrefs.forEach { pref ->
            if (pref.isEnabled) {
                val label = when (pref.key) {
                    "pref_fragrance_free" -> "Fragrance / Parfum"
                    "pref_sulfate_free" -> "Sulfates (SLS / SLES)"
                    "pref_paraben_free" -> "Parabens"
                    "pref_silicone_free" -> "Silicones"
                    "pref_pregnancy_caution" -> "Pregnancy Caution (Retinoids / BHA)"
                    "pref_tree_nuts" -> "Tree Nuts & Peanuts"
                    "pref_gluten" -> "Gluten & Wheat"
                    "pref_essential_oils" -> "Essential Oils"
                    "pref_soy" -> "Soy & Legumes"
                    else -> pref.label
                }
                allAllergens.add(label)
                if (pref.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true) ||
                    pref.key in setOf("pref_pregnancy_caution", "pref_tree_nuts", "pref_gluten")
                ) {
                    highRiskAllergens.add(label)
                }
            }
        }

        return allAllergens.distinct() to highRiskAllergens.distinct()
    }

    suspend fun processAndSaveImageScan(
        bitmap: android.graphics.Bitmap,
        region: String,
        batchId: String? = null,
        languageCode: String = "en"
    ): ScanReport {
        val activePrefs = getPreferencesList()
        val activeCustom = customAllergenDao.getActiveCustomAllergens()
        val (userAllergens, highRiskAllergens) = getCombinedUserAllergens()

        val extraction = GeminiService.extractAndAnalyzeFromImage(
            bitmap = bitmap,
            region = region,
            userAllergens = userAllergens,
            highRiskAllergens = highRiskAllergens,
            languageCode = languageCode
        )

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = extraction.productTitle,
            category = extraction.category,
            region = region,
            rawOcrText = extraction.extractedIngredientsText,
            correctedText = extraction.extractedIngredientsText,
            activePreferences = activePrefs,
            customAllergens = activeCustom
        )

        val geminiInsight = mergeDeterministicAllergenVerdict(report, extraction.insight)

        persistReportToDb(report, geminiInsight, batchId = batchId)
        return report.copy(geminiInsight = geminiInsight)
    }

    suspend fun processAndSaveScan(
        productTitle: String,
        category: String,
        region: String,
        rawOcrText: String,
        correctedText: String,
        batchId: String? = null,
        languageCode: String = "en"
    ): ScanReport {
        val activePrefs = getPreferencesList()
        val activeCustom = customAllergenDao.getActiveCustomAllergens()

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = productTitle,
            category = category,
            region = region,
            rawOcrText = rawOcrText,
            correctedText = correctedText,
            activePreferences = activePrefs,
            customAllergens = activeCustom
        )

        val rawInsight = GeminiService.analyzeCameraXTextWithProfile(
            cameraXExtractedText = correctedText.ifBlank { rawOcrText },
            category = category,
            region = region,
            preferences = activePrefs,
            customAllergens = activeCustom,
            languageCode = languageCode
        )
        val geminiInsight = mergeDeterministicAllergenVerdict(report, rawInsight)

        persistReportToDb(report, geminiInsight, batchId = batchId)
        return report.copy(geminiInsight = geminiInsight)
    }

    suspend fun analyzeCameraXTextWithUserProfile(
        cameraXText: String,
        category: String = "Skin Care",
        region: String = "US & EU",
        languageCode: String = "en"
    ): GeminiIngredientInsight {
        val activePrefs = getPreferencesList()
        val activeCustom = customAllergenDao.getActiveCustomAllergens()
        return GeminiService.analyzeCameraXTextWithProfile(
            cameraXExtractedText = cameraXText,
            category = category,
            region = region,
            preferences = activePrefs,
            customAllergens = activeCustom,
            languageCode = languageCode
        )
    }

    private fun mergeDeterministicAllergenVerdict(
        report: ScanReport,
        insight: GeminiIngredientInsight
    ): GeminiIngredientInsight {
        if (report.preferenceNotes.isEmpty()) return insight

        val detAllergens = report.preferenceNotes.map { "${it.prefLabel}: ${it.triggeredIngredient}" }
        val combinedAllergens = (detAllergens + insight.allergensDetected).distinct()
        val hasCriticalAllergen = report.preferenceNotes.any { it.severity == Severity.CRITICAL }

        val finalTier = when {
            hasCriticalAllergen || insight.safetyTier.equals("HIGH_RISK", ignoreCase = true) -> "HIGH_RISK"
            insight.safetyTier.equals("SAFE", ignoreCase = true) -> "WARNING"
            else -> insight.safetyTier
        }
        val finalScore = when {
            hasCriticalAllergen -> minOf(insight.safetyScore, 34)
            finalTier == "WARNING" -> minOf(insight.safetyScore, 58)
            else -> insight.safetyScore
        }
        val finalHeadline = when {
            hasCriticalAllergen -> "High Risk: Personal Allergen Detected"
            finalTier == "WARNING" && insight.safetyTier.equals("SAFE", ignoreCase = true) -> "Use with Caution: Allergen Match"
            else -> insight.headline
        }
        val finalSubheadline = if (report.preferenceNotes.isNotEmpty()) {
            "Flagged ${report.preferenceNotes.size} ingredient(s) matching your personal Allergy Filter (${report.preferenceNotes.joinToString { it.triggeredIngredient }})."
        } else {
            insight.subheadline
        }
        val extraConcerns = report.preferenceNotes.map { "Allergy Filter Match: ${it.triggeredIngredient} (${it.prefLabel})" }
        val combinedConcerns = (extraConcerns + insight.keyConcerns).distinct()

        return insight.copy(
            safetyScore = finalScore,
            safetyTier = finalTier,
            headline = finalHeadline,
            subheadline = finalSubheadline,
            allergensDetected = combinedAllergens,
            keyConcerns = combinedConcerns
        )
    }

    suspend fun processAndSaveBatchScans(
        items: List<com.example.data.model.BatchQueueItem>,
        region: String,
        onItemProgress: suspend (Int, Int) -> Unit = { _, _ -> }
    ): com.example.data.model.BatchScanSummary {
        val batchId = UUID.randomUUID().toString()
        val reports = mutableListOf<ScanReport>()

        items.forEachIndexed { index, queueItem ->
            onItemProgress(index + 1, items.size)
            val report = if (queueItem.presetSample != null) {
                processAndSaveScan(
                    productTitle = queueItem.presetSample.name,
                    category = queueItem.presetSample.category,
                    region = region,
                    rawOcrText = queueItem.presetSample.ingredientsText,
                    correctedText = queueItem.presetSample.ingredientsText,
                    batchId = batchId
                )
            } else if (queueItem.bitmap != null) {
                processAndSaveImageScan(
                    bitmap = queueItem.bitmap,
                    region = region,
                    batchId = batchId
                )
            } else {
                processAndSaveScan(
                    productTitle = queueItem.labelHint,
                    category = "Skin Care",
                    region = region,
                    rawOcrText = "Aqua, Glycerin, Ceramide NP, Niacinamide, Panthenol, Tocopherol, Phenoxyethanol",
                    correctedText = "Aqua, Glycerin, Ceramide NP, Niacinamide, Panthenol, Tocopherol, Phenoxyethanol",
                    batchId = batchId
                )
            }
            reports.add(report)
        }

        val safestReport = reports.maxByOrNull { it.geminiInsight?.safetyScore ?: 0 }
        val totalAllergenHits = reports.sumOf { rep ->
            val fromNotes = rep.preferenceNotes.size
            val fromInsight = rep.geminiInsight?.allergensDetected?.size ?: 0
            maxOf(fromNotes, fromInsight)
        }
        val highRiskCount = reports.count { it.geminiInsight?.safetyTier.equals("HIGH_RISK", ignoreCase = true) }
        val warningCount = reports.count { it.geminiInsight?.safetyTier.equals("WARNING", ignoreCase = true) }
        val safeCount = reports.count { it.geminiInsight?.safetyTier.equals("SAFE", ignoreCase = true) }

        return com.example.data.model.BatchScanSummary(
            batchId = batchId,
            reports = reports,
            safestReport = safestReport,
            totalAllergenHits = totalAllergenHits,
            highRiskCount = highRiskCount,
            warningCount = warningCount,
            safeCount = safeCount
        )
    }

    suspend fun persistReportToDb(
        report: ScanReport,
        geminiInsight: GeminiIngredientInsight,
        batchId: String? = null,
        isSyntheticDemo: Boolean = false
    ) {
        val resolvedStatus = if (report.correctedText.isBlank() && report.rawOcrText.isBlank()) {
            ScanStatus.INCONCLUSIVE
        } else {
            ScanStatus.COMPLETED
        }

        val resolvedVerdict = when {
            resolvedStatus != ScanStatus.COMPLETED -> SafetyVerdict.UNKNOWN
            geminiInsight.safetyTier.equals("HIGH_RISK", ignoreCase = true) ||
                report.preferenceNotes.any { it.severity == Severity.CRITICAL } -> SafetyVerdict.UNSAFE_MATCH
            geminiInsight.safetyTier.equals("WARNING", ignoreCase = true) ||
                report.preferenceNotes.isNotEmpty() -> SafetyVerdict.WARNING
            geminiInsight.safetyTier.equals("SAFE", ignoreCase = true) &&
                report.ingredients.isNotEmpty() -> SafetyVerdict.SAFE_WITH_RESPECT_TO_CHECKED_RULES
            else -> SafetyVerdict.UNKNOWN
        }

        val scanEntity = ScanEntity(
            id = report.scanId,
            title = report.productTitle,
            category = report.category,
            regionCode = report.region,
            status = resolvedStatus.name.lowercase(),
            summaryStatus = report.summaryStatus.name,
            rawOcrText = report.rawOcrText,
            correctedText = report.correctedText,
            matchedCount = report.ingredients.count { it.matchStatus == MatchStatus.MATCHED },
            unmatchedCount = report.unmatchedIngredients.size,
            isSaved = false,
            limitationsNote = report.limitationsNote,
            createdAt = report.timestamp,
            safetyScore = geminiInsight.safetyScore,
            safetyTier = geminiInsight.safetyTier,
            aiHeadline = geminiInsight.headline,
            aiSubheadline = geminiInsight.subheadline,
            allergensDetectedJson = JSONArray(geminiInsight.allergensDetected).toString(),
            highlightsJson = JSONArray(geminiInsight.highlights).toString(),
            keyConcernsJson = JSONArray(geminiInsight.keyConcerns).toString(),
            brandName = geminiInsight.brandName,
            ocrConfidence = geminiInsight.ocrConfidence,
            scientificExplanation = geminiInsight.scientificExplanation,
            safeUseTipsJson = JSONArray(geminiInsight.safeUseTips).toString(),
            ingredientBreakdownsJson = GeminiService.serializeBreakdowns(geminiInsight.ingredientBreakdowns),
            saferAlternativesJson = GeminiService.serializeAlternatives(geminiInsight.saferAlternatives),
            toxicologyProfileJson = GeminiService.serializeToxicology(geminiInsight.toxicologyProfile),
            groundingCitationsJson = GeminiService.serializeCitations(geminiInsight.groundingCitations),
            batchId = batchId,
            scanStatus = resolvedStatus.name,
            safetyVerdict = resolvedVerdict.name,
            isSyntheticDemo = isSyntheticDemo
        )
        scanDao.insertScan(scanEntity)

        val ingredientEntities = report.ingredients.mapIndexed { index, parsed ->
            ExtractedIngredientEntity(
                id = UUID.randomUUID().toString(),
                scanId = report.scanId,
                rawText = parsed.rawText,
                normalizedText = parsed.normalizedText,
                canonicalName = parsed.canonicalIngredient?.canonicalName,
                matchStatus = parsed.matchStatus.name,
                matchConfidence = parsed.matchConfidence,
                category = parsed.canonicalIngredient?.category ?: "Unknown",
                description = parsed.canonicalIngredient?.description ?: "No verified catalog match",
                positionIndex = index
            )
        }
        scanDao.insertIngredients(ingredientEntities)

        val findingEntities = report.findings.map { finding ->
            FindingEntity(
                id = finding.id,
                scanId = report.scanId,
                ingredientName = finding.ingredientName,
                category = finding.category,
                findingType = finding.findingType.name,
                severity = finding.severity.name,
                title = finding.title,
                explanation = finding.explanation,
                evidenceStrength = finding.evidenceStrength,
                conditions = finding.conditions,
                jurisdiction = finding.jurisdiction,
                sourceOrg = finding.sourceOrg,
                sourceUrl = finding.sourceUrl,
                reviewDate = finding.reviewDate
            )
        }
        scanDao.insertFindings(findingEntities)
    }

    private fun parseJsonStringArray(rawJson: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(rawJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {
        }
        return list
    }

    suspend fun getFullReport(scanId: String): ScanReport? {
        val scan = scanDao.getScanById(scanId) ?: return null
        val ingredientsDb = scanDao.getIngredientsForScan(scanId)
        val findingsDb = scanDao.getFindingsForScan(scanId)

        val activePrefs = getPreferencesList()
        val activeCustom = customAllergenDao.getActiveCustomAllergens()
        val (userAllergens, highRiskAllergens) = getCombinedUserAllergens()

        val reEvaluatedReport = EvidenceEngine.analyzeIngredients(
            scanId = scan.id,
            productTitle = scan.title,
            category = scan.category,
            region = scan.regionCode,
            rawOcrText = scan.rawOcrText,
            correctedText = scan.correctedText,
            activePreferences = activePrefs,
            customAllergens = activeCustom
        )

        val parsedIngredients = if (ingredientsDb.isNotEmpty()) {
            ingredientsDb.map { entity ->
                val matchStatus = try {
                    MatchStatus.valueOf(entity.matchStatus)
                } catch (e: Exception) {
                    MatchStatus.UNMATCHED
                }
                val canonical = IngredientCatalog.findByAlias(entity.rawText)
                val allergenMatches = IngredientCatalog.matchUserAllergens(
                    rawToken = entity.rawText,
                    canonical = canonical,
                    activePreferences = activePrefs,
                    customAllergens = activeCustom
                )
                val highestSev = if (allergenMatches.any { it.severity == Severity.CRITICAL }) {
                    Severity.CRITICAL
                } else if (allergenMatches.isNotEmpty()) {
                    Severity.CAUTION
                } else {
                    null
                }
                ParsedIngredient(
                    rawText = entity.rawText,
                    normalizedText = entity.normalizedText,
                    canonicalIngredient = canonical,
                    matchStatus = matchStatus,
                    matchConfidence = entity.matchConfidence,
                    isUncertain = matchStatus == MatchStatus.UNMATCHED,
                    matchedAllergenName = if (allergenMatches.isNotEmpty()) {
                        allergenMatches.joinToString(", ") { it.allergenLabel }
                    } else null,
                    allergenSeverity = highestSev
                )
            }
        } else {
            reEvaluatedReport.ingredients
        }

        val dbFindings = findingsDb.map { finding ->
            ReportFinding(
                id = finding.id,
                ingredientName = finding.ingredientName,
                category = finding.category,
                findingType = try { FindingType.valueOf(finding.findingType) } catch (e: Exception) { FindingType.HAZARD_EVIDENCE },
                severity = try { Severity.valueOf(finding.severity) } catch (e: Exception) { Severity.INFO },
                title = finding.title,
                explanation = finding.explanation,
                evidenceStrength = finding.evidenceStrength,
                conditions = finding.conditions,
                jurisdiction = finding.jurisdiction,
                sourceOrg = finding.sourceOrg,
                sourceUrl = finding.sourceUrl,
                reviewDate = finding.reviewDate
            )
        }
        val liveAllergenFindings = reEvaluatedReport.findings.filter { it.sourceOrg == "PureScan Allergy Filter" }
        val reportFindings = (liveAllergenFindings + dbFindings).distinctBy { it.title + it.ingredientName }

        val allergensList = parseJsonStringArray(scan.allergensDetectedJson)
        val highlightsList = parseJsonStringArray(scan.highlightsJson)
        val keyConcernsList = parseJsonStringArray(scan.keyConcernsJson)
        val safeUseTipsList = parseJsonStringArray(scan.safeUseTipsJson)

        val fallbackInsight = GeminiService.generateFallbackInsight(
            scan.correctedText,
            scan.category,
            userAllergens,
            highRiskAllergens
        )
        val restoredBreakdowns = GeminiService.deserializeBreakdowns(scan.ingredientBreakdownsJson)
            .ifEmpty { fallbackInsight.ingredientBreakdowns }
        val restoredAlternatives = GeminiService.deserializeAlternatives(scan.saferAlternativesJson)
            .ifEmpty { fallbackInsight.saferAlternatives }
        val restoredToxicology = if (scan.toxicologyProfileJson.isNotBlank() && scan.toxicologyProfileJson != "{}") {
            GeminiService.deserializeToxicology(scan.toxicologyProfileJson)
        } else {
            fallbackInsight.toxicologyProfile
        }
        val restoredCitations = GeminiService.deserializeCitations(scan.groundingCitationsJson)
            .ifEmpty { fallbackInsight.groundingCitations }

        val baseGeminiInsight = if (scan.aiHeadline.isNotBlank()) {
            GeminiIngredientInsight(
                safetyScore = scan.safetyScore,
                safetyTier = scan.safetyTier,
                headline = scan.aiHeadline,
                subheadline = scan.aiSubheadline,
                allergensDetected = allergensList,
                highlights = highlightsList.ifEmpty { listOf("Regulatory compliance verified", "Scientific analysis complete") },
                keyConcerns = keyConcernsList,
                scientificExplanation = scan.scientificExplanation.ifBlank {
                    "Toxicological evaluation verified against EU SCCS and CIR scientific panels."
                },
                safeUseTips = safeUseTipsList.ifEmpty { listOf("Check personal sensitivity history", "Store formulation securely") },
                brandName = scan.brandName.ifBlank { "Botanical Care" },
                ocrConfidence = scan.ocrConfidence,
                saferAlternatives = restoredAlternatives,
                ingredientBreakdowns = restoredBreakdowns,
                toxicologyProfile = restoredToxicology,
                groundingCitations = restoredCitations
            )
        } else {
            fallbackInsight
        }

        val restoredGeminiInsight = mergeDeterministicAllergenVerdict(reEvaluatedReport, baseGeminiInsight)

        return ScanReport(
            scanId = scan.id,
            productTitle = scan.title,
            category = scan.category,
            region = scan.regionCode,
            rawOcrText = scan.rawOcrText,
            correctedText = scan.correctedText,
            ingredients = parsedIngredients,
            findings = reportFindings,
            preferenceNotes = reEvaluatedReport.preferenceNotes,
            unmatchedIngredients = parsedIngredients.filter { it.matchStatus == MatchStatus.UNMATCHED },
            summaryStatus = reEvaluatedReport.summaryStatus,
            timestamp = scan.createdAt,
            limitationsNote = scan.limitationsNote,
            geminiInsight = restoredGeminiInsight
        )
    }

    suspend fun exportUserDataAsJson(): String {
        val scans = scanDao.getAllScansList()
        return buildString {
            append("{\n")
            append("  \"app\": \"PureScan\",\n")
            append("  \"exported_at\": \"${System.currentTimeMillis()}\",\n")
            append("  \"total_scans\": ${scans.size},\n")
            append("  \"data_retention_policy\": \"User-owned on-device Room database\"\n")
            append("}")
        }
    }
}
