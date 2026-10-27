package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Explicit lifecycle status of a scan operation.
 * Separated from [SafetyVerdict] so incomplete or failed scans never default to SAFE.
 */
enum class ScanStatus {
    PENDING,
    CAPTURING,
    PROCESSING,
    COMPLETED,
    INCONCLUSIVE,
    FAILED
}

/**
 * Explicit safety verdict after ingredient evidence and personal allergen evaluation.
 * Defaults to [UNKNOWN] until verified.
 */
enum class SafetyVerdict {
    UNKNOWN,
    SAFE_WITH_RESPECT_TO_CHECKED_RULES,
    WARNING,
    UNSAFE_MATCH
}

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String, // "cosmetics", "skincare", "food"
    val regionCode: String,
    val status: String = ScanStatus.PENDING.name.lowercase(),
    val summaryStatus: String = "UNKNOWN",
    val rawOcrText: String = "",
    val correctedText: String = "",
    val matchedCount: Int = 0,
    val unmatchedCount: Int = 0,
    val isSaved: Boolean = false,
    val limitationsNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val safetyScore: Int = 0,
    val safetyTier: String = "UNKNOWN", // "UNKNOWN", "SAFE", "WARNING", "HIGH_RISK"
    val aiHeadline: String = "Analysis Incomplete",
    val aiSubheadline: String = "Scan has not been fully verified yet.",
    val allergensDetectedJson: String = "[]",
    val highlightsJson: String = "[]",
    val keyConcernsJson: String = "[]",
    val brandName: String = "Unknown Brand",
    val ocrConfidence: Int = 0,
    val scientificExplanation: String = "",
    val safeUseTipsJson: String = "[]",
    val ingredientBreakdownsJson: String = "[]",
    val saferAlternativesJson: String = "[]",
    val toxicologyProfileJson: String = "{}",
    val groundingCitationsJson: String = "[]",
    val batchId: String? = null,
    val scanStatus: String = ScanStatus.PENDING.name,
    val safetyVerdict: String = SafetyVerdict.UNKNOWN.name,
    val imagePath: String? = null,
    val isSyntheticDemo: Boolean = false
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val email: String,
    val fullName: String,
    val passwordHash: String = "",
    val authProvider: String = "EMAIL", // "GOOGLE", "EMAIL", "LOCAL_PROFILE"
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_allergens")
data class CustomAllergenEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val severityLevel: String = "HIGH_RISK", // "HIGH_RISK" or "WARNING"
    val createdAt: Long = System.currentTimeMillis(),
    val profileCategory: String = "custom", // "dietary", "cosmetic", "custom"
    val aliasesCsv: String = "",
    val notes: String = "",
    val ruleClassification: String = "MEDICAL_ALLERGY" // "MEDICAL_ALLERGY", "PERSONAL_SENSITIVITY", "PREFERENCE_AVOIDANCE"
)

/**
 * Room entity storing named user-defined allergen profiles (e.g., "Nut & Gluten Free",
 * "Sensitive Skin Barrier", "Pregnancy Safe") for instant profile switching during scanning.
 */
@Entity(tableName = "allergen_profiles")
data class AllergenProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileName: String,
    val description: String,
    val enabledPreferenceKeysJson: String = "[]",
    val customTriggersJson: String = "[]",
    val defaultSeverity: String = "HIGH_RISK",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "extracted_ingredients")
data class ExtractedIngredientEntity(
    @PrimaryKey
    val id: String,
    val scanId: String,
    val rawText: String,
    val normalizedText: String,
    val canonicalName: String?,
    val matchStatus: String,
    val matchConfidence: Float,
    val category: String,
    val description: String,
    val positionIndex: Int
)

@Entity(tableName = "report_findings")
data class FindingEntity(
    @PrimaryKey
    val id: String,
    val scanId: String,
    val ingredientName: String,
    val category: String,
    val findingType: String,
    val severity: String,
    val title: String,
    val explanation: String,
    val evidenceStrength: String,
    val conditions: String,
    val jurisdiction: String,
    val sourceOrg: String,
    val sourceUrl: String,
    val reviewDate: String
)

@Entity(tableName = "user_preferences")
data class PreferenceSettingEntity(
    @PrimaryKey
    val key: String,
    val label: String,
    val description: String,
    val category: String, // "safety", "allergen", "dietary", "cosmetic"
    val isEnabled: Boolean,
    val ruleClassification: String = "PERSONAL_SENSITIVITY", // "MEDICAL_ALLERGY", "PERSONAL_SENSITIVITY", "PREFERENCE_AVOIDANCE"
    val userReasonNote: String = ""
)
