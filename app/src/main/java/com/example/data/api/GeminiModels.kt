package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "google_search")
    val googleSearch: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val tools: List<GeminiTool>? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json",
    val temperature: Double = 0.2
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiWebChunk(
    val uri: String? = null,
    val title: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingChunk(
    val web: GeminiWebChunk? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingMetadata(
    val groundingChunks: List<GeminiGroundingChunk>? = null,
    val webSearchQueries: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?,
    val groundingMetadata: GeminiGroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class SaferAlternativeItem(
    val productName: String = "Clean Barrier Formula",
    val brandName: String = "PureBotanica",
    val safetyScore: Int = 95,
    val reason: String = "Free of flagged sensitizers and restricted preservatives"
)

@JsonClass(generateAdapter = true)
data class IngredientHazardItem(
    val name: String = "Ingredient",
    val riskLevel: String = "SAFE", // "SAFE", "CAUTION", "HIGH_RISK"
    val role: String = "Formulation Agent",
    val regulatoryNote: String = "Within standard regulatory thresholds",
    val casNumber: String = "Verified INCI",
    val bioavailabilityNote: String = "Low dermal absorption",
    val toxicologyMechanism: String = "Non-sensitizing topical agent"
)

@JsonClass(generateAdapter = true)
data class BioscienceToxicologyProfile(
    val dermalBioavailability: String = "Low (<5% stratum corneum penetration)",
    val bioaccumulationPotential: String = "Minimal (rapid metabolic clearance)",
    val endocrineDisruptionRisk: String = "None Identified",
    val sensitizationIndex: String = "Low Sensitizer",
    val mechanismSummary: String = "Maintains epidermal lipid matrix without receptor-mediated toxicity."
)

@JsonClass(generateAdapter = true)
data class RegulatoryGroundingCitation(
    val title: String = "EU SCCS & CosIng Scientific Opinion",
    val agency: String = "EU SCCS / US CIR",
    val url: String = "https://ec.europa.eu/growth/tools-databases/cosing/",
    val summary: String = "Verified regulatory concentration and toxicological threshold record"
)

@JsonClass(generateAdapter = true)
data class GeminiStructuredPayload(
    val productTitle: String? = null,
    val brandName: String? = null,
    val category: String? = null,
    val extractedIngredientsText: String? = null,
    val ocrConfidence: Int? = null,
    val safetyScore: Int? = null,
    val safetyTier: String? = null,
    val headline: String? = null,
    val subheadline: String? = null,
    val allergensDetected: List<String>? = null,
    val highlights: List<String>? = null,
    val keyConcerns: List<String>? = null,
    val scientificExplanation: String? = null,
    val safeUseTips: List<String>? = null,
    val saferAlternatives: List<SaferAlternativeItem>? = null,
    val ingredientBreakdowns: List<IngredientHazardItem>? = null,
    val toxicologyProfile: BioscienceToxicologyProfile? = null,
    val groundingCitations: List<RegulatoryGroundingCitation>? = null
)

data class GeminiIngredientInsight(
    val safetyScore: Int, // 0 - 100
    val safetyTier: String, // "SAFE", "WARNING", "HIGH_RISK"
    val headline: String, // e.g. "This Product Appears Safe", "Use with Caution", "High Risk / Possible Harmful Ingredients"
    val subheadline: String,
    val allergensDetected: List<String>,
    val highlights: List<String>,
    val keyConcerns: List<String>,
    val scientificExplanation: String,
    val safeUseTips: List<String>,
    val brandName: String = "Essential Care",
    val ocrConfidence: Int = 96,
    val saferAlternatives: List<SaferAlternativeItem> = emptyList(),
    val ingredientBreakdowns: List<IngredientHazardItem> = emptyList(),
    val toxicologyProfile: BioscienceToxicologyProfile = BioscienceToxicologyProfile(),
    val groundingCitations: List<RegulatoryGroundingCitation> = emptyList()
)

data class GeminiImageExtractionResult(
    val productTitle: String,
    val brandName: String,
    val category: String,
    val extractedIngredientsText: String,
    val ocrConfidence: Int = 96,
    val insight: GeminiIngredientInsight
)
