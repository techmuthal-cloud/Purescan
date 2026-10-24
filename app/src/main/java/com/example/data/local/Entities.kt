package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String, // "cosmetics", "skincare", "food"
    val regionCode: String,
    val status: String,
    val summaryStatus: String,
    val rawOcrText: String,
    val correctedText: String,
    val matchedCount: Int,
    val unmatchedCount: Int,
    val isSaved: Boolean = false,
    val limitationsNote: String,
    val createdAt: Long = System.currentTimeMillis(),
    val safetyScore: Int = 85,
    val safetyTier: String = "SAFE", // "SAFE", "WARNING", "HIGH_RISK"
    val aiHeadline: String = "This Product Appears Safe",
    val aiSubheadline: String = "No major safety concerns found in our available databases.",
    val allergensDetectedJson: String = "[]",
    val highlightsJson: String = "[]",
    val keyConcernsJson: String = "[]",
    val brandName: String = "Botanical Care",
    val ocrConfidence: Int = 96,
    val scientificExplanation: String = "",
    val safeUseTipsJson: String = "[]",
    val ingredientBreakdownsJson: String = "[]",
    val saferAlternativesJson: String = "[]",
    val toxicologyProfileJson: String = "{}",
    val groundingCitationsJson: String = "[]",
    val batchId: String? = null
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
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val severityLevel: String = "HIGH_RISK", // "HIGH_RISK" or "WARNING"
    val createdAt: Long = System.currentTimeMillis()
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
    val isEnabled: Boolean
)
