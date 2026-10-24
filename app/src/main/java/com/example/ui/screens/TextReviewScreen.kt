package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.BotanicalTopAppBar
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticInfo
import com.example.ui.theme.SemanticInfoBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

/**
 * Screen 9 of the 15-screen ScanSafe UI/UX Reference ("Review Ingredients"):
 * - Extracted Ingredients checklist with orange checkmarks + remove/add item controls
 * - Raw text editor fallback (`ingredients_text_editor`)
 * - Bottom bar with Retake and ScanOrange "Analyze Ingredients ->" button
 */
@Composable
fun TextReviewScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var newIngredientInput by remember { mutableStateOf("") }

    val categories = listOf("skincare" to "Skincare", "cosmetics" to "Cosmetics", "food" to "Food Label")

    val extractedList = remember(uiState.editableText) {
        uiState.editableText
            .split(Regex("[,;\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("text_review_screen")
    ) {
        BotanicalTopAppBar(
            title = "Review Ingredients",
            showBackButton = true,
            onBackClick = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Transparency notice
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                color = SemanticInfoBg
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SemanticInfo,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Verify extracted ingredients below. Tap any item to remove it, add missing ingredients, or edit the full text directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmokyBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Name input
            Text(
                text = "Product Name (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = SmokyBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.productTitle,
                onValueChange = { viewModel.setProductTitle(it) },
                placeholder = { Text("e.g. Daily Moisture Barrier Cream", color = OliveDrabMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScanOrange,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedTextColor = SmokyBlack,
                    unfocusedTextColor = SmokyBlack
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category selector
            Text(
                text = "Product Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = SmokyBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catId, catLabel) ->
                    val isSelected = uiState.category == catId
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) ScanOrange else CardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setCategory(catId) }
                            .testTag("category_chip_$catId"),
                        color = if (isSelected) ScanOrange else CardSurface
                    ) {
                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnScanOrange else SmokyBlack,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Screen 9 Interactive Extracted Ingredients Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted Ingredients (${extractedList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
                if (extractedList.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OliveDrab,
                        modifier = Modifier
                            .clickable { viewModel.setEditableText("") }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (extractedList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    extractedList.forEachIndexed { idx, itemText ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                            color = CardSurface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ScanOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = OnScanOrange,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = itemText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = SmokyBlack
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updated = extractedList.toMutableList().apply { removeAt(idx) }
                                        viewModel.setEditableText(updated.joinToString(", "))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove $itemText",
                                        tint = OliveDrabMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Quick "+ Add Ingredient" row matching Screen 9
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newIngredientInput,
                    onValueChange = { newIngredientInput = it },
                    placeholder = { Text("Add single ingredient...", color = OliveDrabMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_single_ingredient_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScanOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedTextColor = SmokyBlack,
                        unfocusedTextColor = SmokyBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val trimmed = newIngredientInput.trim()
                        if (trimmed.isNotEmpty()) {
                            val updated = if (uiState.editableText.isBlank()) {
                                trimmed
                            } else {
                                "${uiState.editableText.trimEnd(',', ' ')}, $trimmed"
                            }
                            viewModel.setEditableText(updated)
                            newIngredientInput = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCream,
                        contentColor = SmokyBlack
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("add_ingredient_chip_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Ingredient", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Raw Ingredients Text Area (Preserves `ingredients_text_editor` testTag)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Full Ingredients Text (Comma separated)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SmokyBlack
                )
                if (uiState.rawOcrText.isNotBlank()) {
                    Text(
                        text = "Raw OCR preserved",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDrabMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = uiState.editableText,
                onValueChange = { viewModel.setEditableText(it) },
                placeholder = { Text("Water, Glycerin, Niacinamide, Retinol...", color = OliveDrabMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("ingredients_text_editor"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScanOrange,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedTextColor = SmokyBlack,
                    unfocusedTextColor = SmokyBlack
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${extractedList.size} item(s) ready for analysis",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveDrabMuted
                )

                if (uiState.editableText.isNotBlank()) {
                    Text(
                        text = "Queue Offline",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack,
                        modifier = Modifier
                            .clickable { viewModel.queueCurrentScanOffline() }
                            .padding(4.dp)
                            .testTag("queue_offline_scan_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Actions (Screen 9: Retake + ScanOrange "Analyze Ingredients ->")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Scanner) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("retake_scan_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmokyBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retake",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SmokyBlack
                    )
                }

                Button(
                    onClick = { viewModel.startAnalysis() },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("analyze_ingredients_button"),
                    enabled = uiState.editableText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    )
                ) {
                    Text(
                        text = "Analyze Ingredients",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
