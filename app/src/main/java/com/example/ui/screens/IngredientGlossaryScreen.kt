package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.IngredientCatalog
import com.example.data.model.CanonicalIngredient
import com.example.data.model.Severity
import com.example.ui.ScanViewModel
import com.example.ui.theme.Bone
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

/**
 * Interactive Ingredient Glossary & Regulatory Toxicology Encyclopedia Screen
 * Uses a searchable LazyColumn to display detailed regulatory and toxicology information
 * for common ingredients, with filtering by hazard level:
 * All | Safe | Use with Caution | High Risk | EU Restricted | Allergens
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IngredientGlossaryScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("all") }
    var selectedIngredient by remember { mutableStateOf<CanonicalIngredient?>(null) }
    var expandedCardIds by remember { mutableStateOf(setOf<String>()) }

    val allItems = IngredientCatalog.allIngredients

    val safeCount = remember(allItems) {
        allItems.count { (it.evidenceRecords.maxOfOrNull { ev -> ev.severity } ?: Severity.INFO) == Severity.INFO && !it.isAllergen }
    }
    val cautionCount = remember(allItems) {
        allItems.count { (it.evidenceRecords.maxOfOrNull { ev -> ev.severity } ?: Severity.INFO) == Severity.CAUTION }
    }
    val highRiskCount = remember(allItems) {
        allItems.count { (it.evidenceRecords.maxOfOrNull { ev -> ev.severity } ?: Severity.INFO) == Severity.CRITICAL }
    }
    val euRestrictedCount = remember(allItems) {
        allItems.count { ing -> ing.evidenceRecords.any { it.jurisdiction.contains("EU", ignoreCase = true) && it.severity != Severity.INFO } }
    }
    val allergenCount = remember(allItems) {
        allItems.count { it.isAllergen || it.isFragrance }
    }

    val filterTabs = listOf(
        "all" to "All (${allItems.size})",
        "safe" to "Safe ($safeCount)",
        "caution" to "Use with Caution ($cautionCount)",
        "high_risk" to "High Risk ($highRiskCount)",
        "eu_restricted" to "EU Restricted ($euRestrictedCount)",
        "allergens" to "Allergens ($allergenCount)"
    )

    val filteredIngredients = remember(searchQuery, activeFilter, allItems) {
        allItems.filter { ing ->
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() ||
                ing.canonicalName.contains(q, ignoreCase = true) ||
                ing.casNumber.contains(q, ignoreCase = true) ||
                ing.chemicalClass.contains(q, ignoreCase = true) ||
                ing.category.contains(q, ignoreCase = true) ||
                ing.description.contains(q, ignoreCase = true) ||
                ing.toxicologyMechanism.contains(q, ignoreCase = true) ||
                ing.aliases.any { it.contains(q, ignoreCase = true) }

            val maxSeverity = ing.evidenceRecords.maxOfOrNull { it.severity } ?: Severity.INFO
            val hasEuRecord = ing.evidenceRecords.any {
                it.jurisdiction.contains("EU", ignoreCase = true) && it.severity != Severity.INFO
            }

            val matchesTab = when (activeFilter) {
                "safe" -> maxSeverity == Severity.INFO && !ing.isAllergen
                "caution" -> maxSeverity == Severity.CAUTION
                "high_risk" -> maxSeverity == Severity.CRITICAL
                "eu_restricted" -> hasEuRecord
                "allergens" -> ing.isAllergen || ing.isFragrance
                else -> true
            }

            matchesSearch && matchesTab
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("ingredient_glossary_screen")
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
                modifier = Modifier
                    .size(48.dp)
                    .testTag("glossary_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SmokyBlack
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(ScanOrange.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = SmokyBlack,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ingredient Glossary",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SmokyBlack
                )
            }

            Spacer(modifier = Modifier.width(48.dp))
        }

        // Hazard Summary Strip (High-contrast outdoor readable pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlossaryStatPill(
                modifier = Modifier.weight(1f),
                label = "Safe",
                count = safeCount,
                bgColor = SemanticSuccessBg,
                fgColor = SemanticSuccess,
                isActive = activeFilter == "safe",
                onClick = { activeFilter = if (activeFilter == "safe") "all" else "safe" }
            )
            GlossaryStatPill(
                modifier = Modifier.weight(1f),
                label = "Caution",
                count = cautionCount,
                bgColor = SemanticCautionBg,
                fgColor = SemanticCaution,
                isActive = activeFilter == "caution",
                onClick = { activeFilter = if (activeFilter == "caution") "all" else "caution" }
            )
            GlossaryStatPill(
                modifier = Modifier.weight(1f),
                label = "High Risk",
                count = highRiskCount,
                bgColor = SemanticCriticalBg,
                fgColor = SemanticCritical,
                isActive = activeFilter == "high_risk",
                onClick = { activeFilter = if (activeFilter == "high_risk") "all" else "high_risk" }
            )
        }

        // Search Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search INCI name, CAS #, class, or mechanism...",
                        color = OliveDrabMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Glossary",
                        tint = OliveDrab
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("glossary_clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = OliveDrab
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("glossary_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScanOrange,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedTextColor = SmokyBlack,
                    unfocusedTextColor = SmokyBlack
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Regulatory & Hazard Filter Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTabs) { (tabId, label) ->
                val isSelected = activeFilter == tabId
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SmokyBlack else CardBorder,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { activeFilter = tabId }
                        .testTag("glossary_filter_$tabId"),
                    color = if (isSelected) ScanOrange else CardSurface
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) OnScanOrange else SmokyBlack,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Result Count Subtitle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredIngredients.size} of ${allItems.size} cataloged ingredients",
                style = MaterialTheme.typography.labelSmall,
                color = OliveDrab
            )
            Text(
                text = "Tap card for full dossier",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = OliveDrabMuted
            )
        }

        // Searchable LazyColumn of Ingredients
        if (filteredIngredients.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = OliveDrabMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching ingredients found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing your search or switching hazard level filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            activeFilter = "all"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScanOrange,
                            contentColor = OnScanOrange
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Reset Filters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .testTag("glossary_lazy_list"),
                contentPadding = PaddingValues(top = 6.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredIngredients, key = { it.id }) { ingredient ->
                    val isExpanded = expandedCardIds.contains(ingredient.id)
                    GlossaryIngredientCard(
                        ingredient = ingredient,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedCardIds = if (isExpanded) {
                                expandedCardIds - ingredient.id
                            } else {
                                expandedCardIds + ingredient.id
                            }
                        },
                        onOpenDossier = { selectedIngredient = ingredient }
                    )
                }
            }
        }
    }

    // Bottom Sheet Modal for Full Toxicology & Regulatory Dossier
    selectedIngredient?.let { ing ->
        val maxSeverity = ing.evidenceRecords.maxOfOrNull { it.severity } ?: Severity.INFO
        val (badgeBg, badgeFg, badgeLabel) = when (maxSeverity) {
            Severity.CRITICAL -> Triple(SemanticCriticalBg, SemanticCritical, "High Risk")
            Severity.CAUTION -> Triple(SemanticCautionBg, SemanticCaution, "Use with Caution")
            Severity.INFO -> Triple(SemanticSuccessBg, SemanticSuccess, "Safe")
        }

        ModalBottomSheet(
            onDismissRequest = { selectedIngredient = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = FloralWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("glossary_detail_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeFg.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (maxSeverity) {
                                        Severity.CRITICAL -> Icons.Default.Error
                                        Severity.CAUTION -> Icons.Default.Warning
                                        Severity.INFO -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = badgeFg,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = badgeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg
                                )
                            }
                        }

                        if (ing.casNumber.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BoneLight
                            ) {
                                Text(
                                    text = "CAS ${ing.casNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { selectedIngredient = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SmokyBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = ing.canonicalName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )

                if (ing.chemicalClass.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ing.category} • ${ing.chemicalClass}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OliveDrab
                    )
                }

                if (ing.aliases.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "INCI / Label Synonyms: ${ing.aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrabMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = ing.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SmokyBlack
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bioscience & Toxicology Profile Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCream,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = SmokyBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Toxicology & Pharmacokinetic Profile",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SmokyBlack
                            )
                        }

                        if (ing.maxSafeConcentration.isNotBlank()) {
                            DossierKeyValueRow(
                                label = "Regulatory Limit",
                                value = ing.maxSafeConcentration
                            )
                        }
                        if (ing.dermalBioavailability.isNotBlank()) {
                            DossierKeyValueRow(
                                label = "Bioavailability",
                                value = ing.dermalBioavailability
                            )
                        }
                        if (ing.toxicologyMechanism.isNotBlank()) {
                            DossierKeyValueRow(
                                label = "Mechanism",
                                value = ing.toxicologyMechanism
                            )
                        }
                    }
                }

                // Safer Alternatives if available
                if (ing.saferAlternatives.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SemanticSuccessBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SemanticSuccess.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = SemanticSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Evidence-Backed Safer Alternatives",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ing.saferAlternatives.forEach { alt ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CardSurface
                                    ) {
                                        Text(
                                            text = alt,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SmokyBlack,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Regulatory & Scientific Evidence (${ing.evidenceRecords.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )

                Spacer(modifier = Modifier.height(8.dp))

                ing.evidenceRecords.forEach { ev ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = SmokyBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${ev.organization} (${ev.jurisdiction})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SmokyBlack
                                    )
                                }
                                Text(
                                    text = "${ev.evidenceStrength} • ${ev.reviewDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OliveDrabMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ev.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OliveDrab
                            )
                            if (ev.conditions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceCream
                                ) {
                                    Text(
                                        text = "Threshold: ${ev.conditions}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SmokyBlack,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { selectedIngredient = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Close", color = SmokyBlack, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val target = ing
                            selectedIngredient = null
                            viewModel.testGlossaryIngredientInAnalyzer(
                                ingredientName = target.canonicalName,
                                category = "Skin Care"
                            )
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(50.dp)
                            .testTag("glossary_test_in_analyzer_button"),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScanOrange,
                            contentColor = OnScanOrange
                        )
                    ) {
                        Text(
                            text = "Test in Analyzer",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GlossaryStatPill(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    bgColor: androidx.compose.ui.graphics.Color,
    fgColor: androidx.compose.ui.graphics.Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) fgColor else CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = fgColor
            )
        }
    }
}

@Composable
private fun DossierKeyValueRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OliveDrabMuted,
            fontSize = 10.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = SmokyBlack
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GlossaryIngredientCard(
    ingredient: CanonicalIngredient,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenDossier: () -> Unit
) {
    val maxSeverity = ingredient.evidenceRecords.maxOfOrNull { it.severity } ?: Severity.INFO
    val (badgeBg, badgeFg, badgeLabel) = when (maxSeverity) {
        Severity.CRITICAL -> Triple(SemanticCriticalBg, SemanticCritical, "High Risk")
        Severity.CAUTION -> Triple(SemanticCautionBg, SemanticCaution, "Use with Caution")
        Severity.INFO -> Triple(SemanticSuccessBg, SemanticSuccess, "Safe")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenDossier)
            .testTag("glossary_card_${ingredient.id}"),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.canonicalName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = SmokyBlack
                    )
                    if (ingredient.chemicalClass.isNotBlank() || ingredient.casNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildString {
                                if (ingredient.casNumber.isNotBlank()) append("CAS ${ingredient.casNumber}")
                                if (ingredient.casNumber.isNotBlank() && ingredient.chemicalClass.isNotBlank()) append(" • ")
                                if (ingredient.chemicalClass.isNotBlank()) append(ingredient.chemicalClass)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveDrabMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Paired Status Icon + Label Pill (Design.md requirement: never rely on color alone)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeFg.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (maxSeverity) {
                                Severity.CRITICAL -> Icons.Default.Error
                                Severity.CAUTION -> Icons.Default.Warning
                                Severity.INFO -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ingredient.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OliveDrab
            )

            if (ingredient.maxSafeConcentration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceCream
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Limit: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack
                        )
                        Text(
                            text = ingredient.maxSafeConcentration,
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveDrab,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expandable Inline Toxicology Mechanism & Bioavailability Preview
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (ingredient.toxicologyMechanism.isNotBlank()) {
                        Text(
                            text = "Toxicology Mechanism: ${ingredient.toxicologyMechanism}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmokyBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (ingredient.dermalBioavailability.isNotBlank()) {
                        Text(
                            text = "Dermal Kinetics: ${ingredient.dermalBioavailability}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OliveDrab
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BoneLight
                    ) {
                        Text(
                            text = ingredient.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SmokyBlack,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (ingredient.isAllergen) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SemanticCautionBg
                        ) {
                            Text(
                                text = "Allergen",
                                style = MaterialTheme.typography.labelSmall,
                                color = SemanticCaution,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (ingredient.pregnancyCaution) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SemanticCriticalBg
                        ) {
                            Text(
                                text = "Pregnancy Caution",
                                style = MaterialTheme.typography.labelSmall,
                                color = SemanticCritical,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    ingredient.evidenceRecords.map { it.jurisdiction }.distinct().forEach { jur ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceCream
                        ) {
                            Text(
                                text = "$jur Panel",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveDrab,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse toxicology preview" else "Expand toxicology preview",
                        tint = SmokyBlack
                    )
                }
            }
        }
    }
}
