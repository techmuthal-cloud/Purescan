package com.example.ui.util

import com.example.data.local.AllergenProfileEntity
import com.example.data.local.CustomAllergenEntity
import com.example.data.local.PreferenceSettingEntity
import com.example.data.local.ScanEntity
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date range filter options for Scan History.
 */
enum class HistoryDateFilter(val id: String, val label: String) {
    ALL("all_dates", "All Dates"),
    TODAY("today", "Today"),
    LAST_7_DAYS("last_7_days", "Last 7 Days"),
    LAST_30_DAYS("last_30_days", "Last 30 Days");

    fun matches(createdAtEpochMs: Long, nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        return when (this) {
            ALL -> true
            TODAY -> {
                val calNow = Calendar.getInstance().apply { timeInMillis = nowEpochMs }
                val calScan = Calendar.getInstance().apply { timeInMillis = createdAtEpochMs }
                val sameYearDay = calNow.get(Calendar.YEAR) == calScan.get(Calendar.YEAR) &&
                    calNow.get(Calendar.DAY_OF_YEAR) == calScan.get(Calendar.DAY_OF_YEAR)
                val within24Hours = (nowEpochMs - createdAtEpochMs) in 0L..(24L * 60L * 60L * 1000L)
                sameYearDay || within24Hours
            }
            LAST_7_DAYS -> {
                val cutoff = nowEpochMs - (7L * 24L * 60L * 60L * 1000L)
                createdAtEpochMs >= cutoff
            }
            LAST_30_DAYS -> {
                val cutoff = nowEpochMs - (30L * 24L * 60L * 60L * 1000L)
                createdAtEpochMs >= cutoff
            }
        }
    }

    companion object {
        fun fromId(id: String): HistoryDateFilter {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL
        }
    }
}

/**
 * Represents a single bar in the 5-scan mini chart on the Home screen Allergy Impact Dashboard.
 */
data class RecentScanImpactPoint(
    val scanId: String,
    val title: String,
    val shortLabel: String,
    val safetyScore: Int,
    val safetyTier: String,
    val matchedAllergens: List<String>,
    val createdAt: Long
)

/**
 * Represents a profile allergen or sensitivity rule triggered across the user's last 5 scans.
 */
data class TriggeredAllergenStat(
    val allergenName: String,
    val classificationLabel: String, // "Medical Allergy", "Sensitivity", "Avoidance"
    val isHighRisk: Boolean,
    val hitCount: Int
)

/**
 * Summary model powering the Home screen Allergy Impact Score Dashboard.
 */
data class AllergyImpactSummary(
    val impactScore: Int, // 0..100 (higher = greater allergen impact/risk across recent scans)
    val safetyAlignmentScore: Int, // 0..100 (100 - impactScore)
    val impactTier: String, // "LOW", "MODERATE", "HIGH"
    val impactHeadline: String,
    val impactSubtitle: String,
    val activeProfileName: String,
    val enabledRulesCount: Int,
    val medicalAllergyRulesCount: Int,
    val sensitivityRulesCount: Int,
    val recentFiveScans: List<RecentScanImpactPoint>,
    val triggeredAllergens: List<TriggeredAllergenStat>,
    val flaggedScansCount: Int
)

object HistoryAndImpactAnalytics {

    private val preferenceSearchKeywords: Map<String, List<String>> = mapOf(
        "pref_fragrance_free" to listOf("fragrance", "parfum", "linalool", "limonene", "citronellol", "geraniol", "eugenol"),
        "pref_paraben_free" to listOf("paraben", "methylparaben", "propylparaben", "butylparaben", "ethylparaben"),
        "pref_sulfate_free" to listOf("sulfate", "sodium lauryl sulfate", "sodium laureth sulfate", "sls", "sles"),
        "pref_pregnancy_caution" to listOf("retinol", "retinoid", "retinyl", "salicylic acid", "hydroquinone", "tretinoin"),
        "pref_tree_nuts" to listOf("almond", "prunus amygdalus", "argan", "macadamia", "shea", "peanut", "arachis", "walnut", "cashew", "hazelnut"),
        "pref_gluten_soy" to listOf("wheat", "triticum", "gluten", "barley", "hordeum", "rye", "soy", "glycine soja", "oat", "avena"),
        "pref_dairy_lactose" to listOf("milk", "lactose", "whey", "casein", "lactic", "colostrum"),
        "pref_comedogenic" to listOf("isopropyl myristate", "coconut oil", "cocos nucifera", "cocoa butter", "lanolin")
    )

    /**
     * Multi-criteria filter for Scan History:
     * Filters by product name / brand / ingredient / formatted date query, safety rating category, and date window.
     */
    fun filterHistoryScans(
        scans: List<ScanEntity>,
        searchQuery: String,
        safetyFilter: String,
        dateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
        nowEpochMs: Long = System.currentTimeMillis()
    ): List<ScanEntity> {
        val rawQuery = searchQuery.trim()
        val normalizedQuery = normalizeVoiceCommandToSearchTerm(rawQuery)
        val dateFormatterShort = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val dateFormatterIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return scans.filter { scan ->
            val formattedDateShort = runCatching { dateFormatterShort.format(Date(scan.createdAt)) }.getOrDefault("")
            val formattedDateIso = runCatching { dateFormatterIso.format(Date(scan.createdAt)) }.getOrDefault("")

            val matchesQuery = normalizedQuery.isBlank() ||
                scan.title.contains(normalizedQuery, ignoreCase = true) ||
                scan.brandName.contains(normalizedQuery, ignoreCase = true) ||
                scan.correctedText.contains(normalizedQuery, ignoreCase = true) ||
                scan.rawOcrText.contains(normalizedQuery, ignoreCase = true) ||
                scan.aiHeadline.contains(normalizedQuery, ignoreCase = true) ||
                scan.category.contains(normalizedQuery, ignoreCase = true) ||
                scan.allergensDetectedJson.contains(normalizedQuery, ignoreCase = true) ||
                formattedDateShort.contains(normalizedQuery, ignoreCase = true) ||
                formattedDateIso.contains(normalizedQuery, ignoreCase = true)

            val matchesSafety = when (safetyFilter.lowercase(Locale.US)) {
                "safe", "clean" -> scan.safetyTier.equals("SAFE", ignoreCase = true) ||
                    scan.summaryStatus.equals("NO_RELEVANT_CONCERN", ignoreCase = true)
                "caution", "warning" -> scan.safetyTier.equals("WARNING", ignoreCase = true) ||
                    scan.safetyTier.equals("CAUTION", ignoreCase = true)
                "high_risk", "high" -> scan.safetyTier.equals("HIGH_RISK", ignoreCase = true)
                "concerns" -> scan.safetyTier.equals("WARNING", ignoreCase = true) ||
                    scan.safetyTier.equals("CAUTION", ignoreCase = true) ||
                    scan.safetyTier.equals("HIGH_RISK", ignoreCase = true) ||
                    scan.summaryStatus.equals("CONCERNS_IDENTIFIED", ignoreCase = true)
                "unknowns", "unknown" -> scan.safetyTier.equals("UNKNOWN", ignoreCase = true) ||
                    scan.summaryStatus.equals("COMPLETED_WITH_UNKNOWNS", ignoreCase = true) ||
                    scan.summaryStatus.equals("EVIDENCE_INSUFFICIENT", ignoreCase = true) ||
                    scan.unmatchedCount > 0
                else -> true
            }

            val matchesDate = dateFilter.matches(scan.createdAt, nowEpochMs)

            matchesQuery && matchesSafety && matchesDate
        }
    }

    /**
     * Cleans conversational voice prefixes (e.g., "Search for Cerave", "Show me fragrance scans")
     * into a crisp search term so voice commands match product names, ingredients, or dates accurately.
     */
    fun normalizeVoiceCommandToSearchTerm(rawVoiceInput: String): String {
        var cleaned = rawVoiceInput.trim()
        val conversationalPrefixes = listOf(
            "search history for ",
            "search for ",
            "find products with ",
            "find product ",
            "find ",
            "show me ",
            "show ",
            "look up ",
            "filter by "
        )
        for (prefix in conversationalPrefixes) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
                break
            }
        }
        return cleaned.trimEnd('.', '?', '!')
    }

    /**
     * Calculates the personalized Allergy Impact Dashboard summary based on the user's saved
     * allergen profile (enabled PreferenceSettingEntity + CustomAllergenEntity rules) and their last 5 scans.
     */
    fun calculateAllergyImpact(
        allScans: List<ScanEntity>,
        preferences: List<PreferenceSettingEntity>,
        customAllergens: List<CustomAllergenEntity>,
        activeProfile: AllergenProfileEntity? = null
    ): AllergyImpactSummary {
        val enabledPreferences = preferences.filter { it.isEnabled }
        val enabledCustom = customAllergens.filter { it.isEnabled }
        val totalRulesCount = enabledPreferences.size + enabledCustom.size

        val medicalCount = enabledPreferences.count { it.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true) } +
            enabledCustom.count {
                it.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true) ||
                    it.severityLevel.equals("HIGH_RISK", ignoreCase = true)
            }
        val sensitivityCount = (totalRulesCount - medicalCount).coerceAtLeast(0)

        val profileName = activeProfile?.profileName?.takeIf { it.isNotBlank() }
            ?: if (totalRulesCount > 0) "Active Personal Profile" else "Default Safety Profile"

        val lastFiveScans = allScans.take(5)
        if (lastFiveScans.isEmpty()) {
            return AllergyImpactSummary(
                impactScore = 0,
                safetyAlignmentScore = 100,
                impactTier = "LOW",
                impactHeadline = "No Recent Scans Yet",
                impactSubtitle = "$totalRulesCount active allergen rules ready to screen your next 5 products.",
                activeProfileName = profileName,
                enabledRulesCount = totalRulesCount,
                medicalAllergyRulesCount = medicalCount,
                sensitivityRulesCount = sensitivityCount,
                recentFiveScans = emptyList(),
                triggeredAllergens = emptyList(),
                flaggedScansCount = 0
            )
        }

        val triggeredHitMap = linkedMapOf<String, TriggeredAllergenStat>()
        var cumulativeRiskPoints = 0.0

        val scanPoints = lastFiveScans.map { scan ->
            val matchedForScan = mutableListOf<String>()
            val combinedText = buildString {
                append(scan.correctedText)
                append(" ")
                append(scan.rawOcrText)
                append(" ")
                append(scan.allergensDetectedJson)
                append(" ")
                append(scan.keyConcernsJson)
            }.lowercase(Locale.US)

            // 1. Check custom allergens
            for (custom in enabledCustom) {
                val tokens = buildList {
                    add(custom.name.trim().lowercase(Locale.US))
                    custom.aliasesCsv.split(",")
                        .map { it.trim().lowercase(Locale.US) }
                        .filter { it.isNotBlank() }
                        .forEach { add(it) }
                }.filter { it.isNotBlank() }

                if (tokens.any { token -> combinedText.contains(token) }) {
                    matchedForScan.add(custom.name)
                    val isMedical = custom.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true) ||
                        custom.severityLevel.equals("HIGH_RISK", ignoreCase = true)
                    val classLabel = when (custom.ruleClassification.uppercase(Locale.US)) {
                        "MEDICAL_ALLERGY" -> "Medical Allergy"
                        "PREFERENCE_AVOIDANCE" -> "Avoidance"
                        else -> "Sensitivity"
                    }
                    val existing = triggeredHitMap[custom.name]
                    triggeredHitMap[custom.name] = if (existing == null) {
                        TriggeredAllergenStat(
                            allergenName = custom.name,
                            classificationLabel = classLabel,
                            isHighRisk = isMedical,
                            hitCount = 1
                        )
                    } else {
                        existing.copy(hitCount = existing.hitCount + 1)
                    }
                }
            }

            // 2. Check standard preference allergen/sensitivity rules
            for (pref in enabledPreferences) {
                val keywords = preferenceSearchKeywords[pref.key]
                    ?: listOf(pref.label.lowercase(Locale.US))
                if (keywords.any { kw -> kw.isNotBlank() && combinedText.contains(kw) }) {
                    if (matchedForScan.none { it.equals(pref.label, ignoreCase = true) }) {
                        matchedForScan.add(pref.label)
                    }
                    val isMedical = pref.ruleClassification.equals("MEDICAL_ALLERGY", ignoreCase = true)
                    val classLabel = when (pref.ruleClassification.uppercase(Locale.US)) {
                        "MEDICAL_ALLERGY" -> "Medical Allergy"
                        "PREFERENCE_AVOIDANCE" -> "Avoidance"
                        else -> "Sensitivity"
                    }
                    val existing = triggeredHitMap[pref.label]
                    triggeredHitMap[pref.label] = if (existing == null) {
                        TriggeredAllergenStat(
                            allergenName = pref.label,
                            classificationLabel = classLabel,
                            isHighRisk = isMedical,
                            hitCount = 1
                        )
                    } else {
                        existing.copy(hitCount = existing.hitCount + 1)
                    }
                }
            }

            // 3. Also inspect allergensDetectedJson stored on the scan
            val parsedJsonAllergens = parseJsonStringList(scan.allergensDetectedJson)
            for (detected in parsedJsonAllergens) {
                if (matchedForScan.none { it.equals(detected, ignoreCase = true) }) {
                    matchedForScan.add(detected)
                    val existing = triggeredHitMap[detected]
                    if (existing == null) {
                        triggeredHitMap[detected] = TriggeredAllergenStat(
                            allergenName = detected,
                            classificationLabel = if (scan.safetyTier == "HIGH_RISK") "Medical Allergy" else "Sensitivity",
                            isHighRisk = scan.safetyTier == "HIGH_RISK",
                            hitCount = 1
                        )
                    } else {
                        triggeredHitMap[detected] = existing.copy(hitCount = existing.hitCount + 1)
                    }
                }
            }

            // Calculate per-scan risk contribution (0..100)
            val baseScanRisk = when (scan.safetyTier.uppercase(Locale.US)) {
                "HIGH_RISK" -> 82.0
                "WARNING", "CAUTION" -> 48.0
                "SAFE" -> (100 - scan.safetyScore.coerceIn(75, 100)).toDouble()
                else -> 30.0
            }
            val profileMatchPenalty = matchedForScan.size * 14.0
            val singleScanImpact = (baseScanRisk + profileMatchPenalty).coerceIn(0.0, 100.0)
            cumulativeRiskPoints += singleScanImpact

            val resolvedSafetyScore = when {
                scan.safetyScore > 0 -> scan.safetyScore.coerceIn(5, 100)
                scan.safetyTier.equals("SAFE", ignoreCase = true) -> 90
                scan.safetyTier.equals("WARNING", ignoreCase = true) -> 60
                scan.safetyTier.equals("HIGH_RISK", ignoreCase = true) -> 28
                else -> 50
            }

            RecentScanImpactPoint(
                scanId = scan.id,
                title = scan.title,
                shortLabel = abbreviateTitle(scan.title),
                safetyScore = resolvedSafetyScore,
                safetyTier = scan.safetyTier,
                matchedAllergens = matchedForScan,
                createdAt = scan.createdAt
            )
        }

        val averageImpact = (cumulativeRiskPoints / lastFiveScans.size.toDouble()).toInt().coerceIn(0, 100)
        val triggeredList = triggeredHitMap.values.sortedWith(
            compareByDescending<TriggeredAllergenStat> { it.isHighRisk }
                .thenByDescending { it.hitCount }
        )
        val flaggedScansCount = scanPoints.count {
            it.matchedAllergens.isNotEmpty() ||
                it.safetyTier.equals("HIGH_RISK", ignoreCase = true) ||
                it.safetyTier.equals("WARNING", ignoreCase = true)
        }

        val impactTier = when {
            averageImpact >= 55 || triggeredList.any { it.isHighRisk } -> "HIGH"
            averageImpact >= 25 || triggeredList.isNotEmpty() -> "MODERATE"
            else -> "LOW"
        }

        val impactHeadline = when (impactTier) {
            "HIGH" -> "High Allergy Impact Detected"
            "MODERATE" -> "Moderate Sensitivity Impact"
            else -> "Low Allergy Impact • Clean Streak"
        }

        val impactSubtitle = if (triggeredList.isEmpty()) {
            "0 profile allergens triggered across your last ${lastFiveScans.size} scans ($totalRulesCount rules active)."
        } else {
            "${triggeredList.size} allergen rule(s) triggered in $flaggedScansCount of your last ${lastFiveScans.size} scans."
        }

        return AllergyImpactSummary(
            impactScore = averageImpact,
            safetyAlignmentScore = (100 - averageImpact).coerceIn(0, 100),
            impactTier = impactTier,
            impactHeadline = impactHeadline,
            impactSubtitle = impactSubtitle,
            activeProfileName = profileName,
            enabledRulesCount = totalRulesCount,
            medicalAllergyRulesCount = medicalCount,
            sensitivityRulesCount = sensitivityCount,
            recentFiveScans = scanPoints,
            triggeredAllergens = triggeredList,
            flaggedScansCount = flaggedScansCount
        )
    }

    private fun abbreviateTitle(title: String): String {
        val cleaned = title.trim()
        if (cleaned.length <= 10) return cleaned
        val firstWord = cleaned.split(" ").firstOrNull().orEmpty()
        return if (firstWord.length in 3..10) {
            firstWord
        } else {
            cleaned.take(9) + "…"
        }
    }

    private fun parseJsonStringList(rawJson: String): List<String> {
        if (rawJson.isBlank() || rawJson == "[]") return emptyList()
        return runCatching {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i).trim()
                    if (item.isNotBlank()) add(item)
                }
            }
        }.getOrDefault(emptyList())
    }
}
