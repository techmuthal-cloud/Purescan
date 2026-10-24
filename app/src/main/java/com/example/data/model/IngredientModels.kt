package com.example.data.model

data class CanonicalIngredient(
    val id: String,
    val canonicalName: String,
    val category: String, // "Humectant", "Surfactant", "Active", "Preservative", "Antioxidant", "Emollient", "Fragrance", "UV Filter", "Colorant", "Food Allergen", "Botanical"
    val aliases: List<String>,
    val description: String,
    val isAllergen: Boolean = false,
    val isFragrance: Boolean = false,
    val isParaben: Boolean = false,
    val isSulfate: Boolean = false,
    val isSilicone: Boolean = false,
    val isAnimalDerived: Boolean = false,
    val pregnancyCaution: Boolean = false,
    val casNumber: String = "",
    val chemicalClass: String = "",
    val maxSafeConcentration: String = "",
    val dermalBioavailability: String = "",
    val toxicologyMechanism: String = "",
    val saferAlternatives: List<String> = emptyList(),
    val evidenceRecords: List<EvidenceRecord> = emptyList()
)

data class EvidenceRecord(
    val id: String,
    val organization: String, // "EU SCCS", "US CIR", "US FDA", "EFSA", "WHO / IARC", "IFRA"
    val findingType: FindingType,
    val severity: Severity,
    val summary: String,
    val evidenceStrength: String, // "Strong", "Moderate", "Limited"
    val conditions: String, // e.g. "Safe up to 0.3% leave-on", "Not for aerosol spray"
    val jurisdiction: String, // "EU", "US", "GLOBAL", "IN"
    val sourceUrl: String,
    val reviewDate: String
)

enum class FindingType {
    REGULATORY_RESTRICTION,
    HAZARD_EVIDENCE,
    ALLERGEN_ALERT,
    PREFERENCE_MISMATCH,
    BENIGN_EVIDENCE
}

enum class Severity {
    INFO,
    CAUTION,
    CRITICAL
}

enum class MatchStatus {
    MATCHED,
    UNMATCHED,
    USER_REVIEWED
}

data class ParsedIngredient(
    val rawText: String,
    val normalizedText: String,
    val canonicalIngredient: CanonicalIngredient?,
    val matchStatus: MatchStatus,
    val matchConfidence: Float,
    val isUncertain: Boolean = false,
    val matchedAllergenName: String? = null,
    val allergenSeverity: Severity? = null
)

data class ScanReport(
    val scanId: String,
    val productTitle: String,
    val category: String,
    val region: String,
    val rawOcrText: String,
    val correctedText: String,
    val ingredients: List<ParsedIngredient>,
    val findings: List<ReportFinding>,
    val preferenceNotes: List<PreferenceNote>,
    val unmatchedIngredients: List<ParsedIngredient>,
    val summaryStatus: SummaryStatus,
    val timestamp: Long,
    val limitationsNote: String,
    val geminiInsight: com.example.data.api.GeminiIngredientInsight? = null
)

enum class SummaryStatus {
    NO_RELEVANT_CONCERN,
    CONCERNS_IDENTIFIED,
    COMPLETED_WITH_UNKNOWNS
}

data class ReportFinding(
    val id: String,
    val ingredientName: String,
    val category: String,
    val findingType: FindingType,
    val severity: Severity,
    val title: String,
    val explanation: String,
    val evidenceStrength: String,
    val conditions: String,
    val jurisdiction: String,
    val sourceOrg: String,
    val sourceUrl: String,
    val reviewDate: String
)

data class PreferenceNote(
    val prefKey: String,
    val prefLabel: String,
    val triggeredIngredient: String,
    val note: String,
    val severity: Severity = Severity.CRITICAL
)

data class BatchQueueItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bitmap: android.graphics.Bitmap? = null,
    val presetSample: com.example.data.catalog.SampleLabel? = null,
    val labelHint: String = "Queued Label",
    val capturedAt: Long = System.currentTimeMillis()
)

data class BatchScanSummary(
    val batchId: String = java.util.UUID.randomUUID().toString(),
    val reports: List<ScanReport>,
    val safestReport: ScanReport?,
    val totalAllergenHits: Int,
    val highRiskCount: Int,
    val warningCount: Int,
    val safeCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)
