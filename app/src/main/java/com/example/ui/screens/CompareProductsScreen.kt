package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.data.catalog.PresetSamples
import com.example.ui.ScanViewModel
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import org.json.JSONArray

private data class ComparableProductItem(
    val id: String,
    val title: String,
    val brand: String,
    val category: String,
    val safetyScore: Int,
    val safetyTier: String,
    val ingredientCount: Int,
    val allergens: List<String>,
    val highlights: List<String>,
    val concerns: List<String>
)

@Composable
fun CompareProductsScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val allScans by viewModel.allScans.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val comparablePool = remember(allScans, uiState.currentReport) {
        val list = mutableListOf<ComparableProductItem>()

        // 1. Add user's scanned products from Room DB
        allScans.forEach { scan ->
            val allergens = parseJsonStringList(scan.allergensDetectedJson)
            val highlights = parseJsonStringList(scan.highlightsJson)
            val concerns = parseJsonStringList(scan.keyConcernsJson)
            list.add(
                ComparableProductItem(
                    id = scan.id,
                    title = scan.title,
                    brand = "Scanned Formula",
                    category = scan.category,
                    safetyScore = scan.safetyScore,
                    safetyTier = scan.safetyTier,
                    ingredientCount = scan.matchedCount + scan.unmatchedCount,
                    allergens = allergens,
                    highlights = highlights.ifEmpty { listOf("Evaluated against ${scan.regionCode} standards") },
                    concerns = concerns
                )
            )
        }

        // 2. Add preset reference formulations so comparison always has rich options even on first launch
        PresetSamples.samples.forEach { sample ->
            if (list.none { it.title.equals(sample.name, ignoreCase = true) }) {
                val insight = GeminiService.generateFallbackInsight(sample.ingredientsText, sample.category)
                val count = sample.ingredientsText.split(",").count { it.isNotBlank() }
                list.add(
                    ComparableProductItem(
                        id = "preset_${sample.id}",
                        title = sample.name,
                        brand = "Reference Catalog",
                        category = sample.category,
                        safetyScore = insight.safetyScore,
                        safetyTier = insight.safetyTier,
                        ingredientCount = count,
                        allergens = insight.allergensDetected,
                        highlights = insight.highlights,
                        concerns = insight.keyConcerns
                    )
                )
            }
        }
        list
    }

    var leftIndex by remember(comparablePool.size, uiState.compareLeftScanId) {
        val idx = uiState.compareLeftScanId?.let { id -> comparablePool.indexOfFirst { it.id == id } } ?: -1
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }
    var rightIndex by remember(comparablePool.size, uiState.compareRightScanId) {
        val idx = uiState.compareRightScanId?.let { id -> comparablePool.indexOfFirst { it.id == id } } ?: -1
        mutableIntStateOf(if (idx >= 0) idx else if (comparablePool.size > 1) 1 else 0)
    }

    val leftProduct = comparablePool.getOrNull(leftIndex) ?: comparablePool.first()
    val rightProduct = comparablePool.getOrNull(rightIndex) ?: comparablePool.last()

    val saferProduct = if (leftProduct.safetyScore >= rightProduct.safetyScore) leftProduct else rightProduct
    val scoreDelta = kotlin.math.abs(leftProduct.safetyScore - rightProduct.safetyScore)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("compare_products_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("compare_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SmokyBlack
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = SmokyBlack,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compare Formulations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )
            }

            Spacer(modifier = Modifier.width(40.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Product A Selector Row
            item {
                Text(
                    text = "Select Product A",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OliveDrab
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(comparablePool.size) { idx ->
                        val item = comparablePool[idx]
                        val selected = idx == leftIndex
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) SmokyBlack else Color.White,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) SmokyBlack else CardBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { leftIndex = idx }
                                .testTag("compare_select_left_$idx")
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) FloralWhite else SmokyBlack,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Product B Selector Row
            item {
                Text(
                    text = "Select Product B",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OliveDrab
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(comparablePool.size) { idx ->
                        val item = comparablePool[idx]
                        val selected = idx == rightIndex
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) SmokyBlack else Color.White,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) SmokyBlack else CardBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { rightIndex = idx }
                                .testTag("compare_select_right_$idx")
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) FloralWhite else SmokyBlack,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Winner Recommendation Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("comparison_verdict_banner"),
                    color = Color(0xFFDCE8DC)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E6B47)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Safer Choice: ${saferProduct.title}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1F4D32)
                            )
                            Text(
                                text = if (scoreDelta == 0) {
                                    "Both products have an identical safety score (${saferProduct.safetyScore}/100)."
                                } else {
                                    "Scores +$scoreDelta points higher (${saferProduct.safetyScore}/100) with fewer flagged sensitizers."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E5738)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Side-by-Side Comparison Columns
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ComparisonProductColumn(
                        product = leftProduct,
                        isWinner = leftProduct.safetyScore >= rightProduct.safetyScore,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonProductColumn(
                        product = rightProduct,
                        isWinner = rightProduct.safetyScore >= leftProduct.safetyScore,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonProductColumn(
    product: ComparableProductItem,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    val tier = product.safetyTier.uppercase()
    val (tierBg, tierFg, tierLabel) = when (tier) {
        "HIGH_RISK" -> Triple(SemanticCriticalBg, SemanticCritical, "High Risk")
        "WARNING" -> Triple(SemanticCautionBg, SemanticCaution, "Caution")
        else -> Triple(SemanticSuccessBg, SemanticSuccess, "Safe")
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isWinner) 1.5.dp else 1.dp,
                color = if (isWinner) SemanticSuccess else CardBorder,
                shape = RoundedCornerShape(18.dp)
            ),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProductBottleThumbnail(modifier = Modifier.size(56.dp))

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = SmokyBlack,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Text(
                text = product.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = OliveDrabMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Safety Score Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(tierBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${product.safetyScore}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = tierFg
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = tierFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tierBg
            ) {
                Text(
                    text = tierLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tierFg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BoneLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Metrics
            ComparisonMetricBlock(
                label = "Evaluated Ingredients",
                value = "${product.ingredientCount} items"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricBlock(
                label = "Allergen Triggers",
                value = if (product.allergens.isEmpty()) "None detected" else "${product.allergens.size} flagged"
            )

            if (product.allergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                product.allergens.take(2).forEach { allergen ->
                    Text(
                        text = "• $allergen",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SemanticCaution,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BoneLight)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (product.concerns.isNotEmpty()) "Key Concerns" else "Highlights",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SmokyBlack,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            val points = if (product.concerns.isNotEmpty()) product.concerns else product.highlights
            points.take(3).forEach { pt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = when {
                            tier == "HIGH_RISK" -> Icons.Default.Error
                            product.concerns.isNotEmpty() -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = tierFg,
                        modifier = Modifier
                            .size(13.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = pt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        ),
                        color = OliveDrab
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonMetricBlock(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = OliveDrabMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SmokyBlack
        )
    }
}

private fun parseJsonStringList(raw: String): List<String> {
    val result = mutableListOf<String>()
    try {
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            result.add(arr.getString(i))
        }
    } catch (_: Exception) {
    }
    return result
}
