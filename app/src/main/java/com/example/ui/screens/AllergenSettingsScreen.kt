package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomAllergenEntity
import com.example.data.local.PreferenceSettingEntity
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabDark
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

data class AllergenCategoryGroup(
    val categoryId: String,
    val title: String,
    val description: String,
    val allergenKeys: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergenSettingsScreen(
    viewModel: ScanViewModel
) {
    BackHandler {
        viewModel.navigateTo(Screen.Profile)
    }

    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var sortAlphabetically by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    // Pre-defined allergen groups
    val categories = remember {
        listOf(
            AllergenCategoryGroup(
                categoryId = "dietary",
                title = "Dietary & Food Allergens",
                description = "Food-grade botanical derivatives frequently causing severe immune reactions",
                allergenKeys = listOf("pref_gluten", "pref_tree_nuts", "pref_soy", "pref_vegan")
            ),
            AllergenCategoryGroup(
                categoryId = "preservatives",
                title = "Preservatives & Biocides",
                description = "Antimicrobials linked to endocrine disruption or contact eczema",
                allergenKeys = listOf("pref_paraben_free")
            ),
            AllergenCategoryGroup(
                categoryId = "surfactants",
                title = "Cleansers & Harsh Surfactants",
                description = "Anionic detergents known to denature stratum corneum keratin",
                allergenKeys = listOf("pref_sulfate_free")
            ),
            AllergenCategoryGroup(
                categoryId = "fragrance",
                title = "Fragrance & Volatile Aromas",
                description = "Volatile terpenes and synthetic aroma compounds that oxidize into sensitizers",
                allergenKeys = listOf("pref_fragrance_free", "pref_essential_oils")
            ),
            AllergenCategoryGroup(
                categoryId = "special_care",
                title = "Special Care & Skin Barrier",
                description = "Active retinoids, strong acids, and occlusive silicone polymers",
                allergenKeys = listOf("pref_pregnancy_caution", "pref_silicone_free")
            )
        )
    }

    // Filtered allergen items
    val allAllergenItems = remember(preferences, searchQuery, selectedCategoryFilter, sortAlphabetically) {
        val filtered = preferences.filter { pref ->
            val matchesQuery = searchQuery.isBlank() ||
                pref.label.contains(searchQuery, ignoreCase = true) ||
                pref.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null ||
                categories.find { it.categoryId == selectedCategoryFilter }?.allergenKeys?.contains(pref.key) == true
            matchesQuery && matchesCategory
        }

        if (sortAlphabetically) {
            filtered.sortedBy { it.label }
        } else {
            filtered
        }
    }

    val activeCount = preferences.count { it.isEnabled } + customAllergens.count { it.isEnabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Allergy Filter Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack
                        )
                        Text(
                            text = "$activeCount active filters protecting your scans",
                            fontSize = 12.sp,
                            color = OliveDrabDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Profile) },
                        modifier = Modifier.testTag("allergen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SmokyBlack
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { sortAlphabetically = !sortAlphabetically },
                        modifier = Modifier.testTag("allergen_sort_button")
                    ) {
                        Icon(
                            imageVector = if (sortAlphabetically) Icons.Default.FilterList else Icons.Default.SortByAlpha,
                            contentDescription = "Toggle Sort",
                            tint = if (sortAlphabetically) OliveDrab else SmokyBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FloralWhite
                )
            )
        },
        containerColor = FloralWhite
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("allergen_settings_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("allergen_search_input"),
                    placeholder = { Text("Search allergens (e.g. gluten, paraben, almond)...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = OliveDrabDark,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SmokyBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OliveDrab,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // 1B. Saved User-Defined Allergen Profiles (Room Database)
            item {
                com.example.ui.components.SavedAllergenProfilesCard(viewModel = viewModel)
            }

            // 2. Preset Filter Bundles Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BoneLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = OliveDrab,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Quick Preset Bundles",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SmokyBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.togglePreference("pref_gluten", false)
                                        viewModel.togglePreference("pref_tree_nuts", false)
                                        viewModel.togglePreference("pref_soy", false)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🌾 Celiac & Nut-Free", fontSize = 12.sp, color = SmokyBlack)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.togglePreference("pref_paraben_free", false)
                                        viewModel.togglePreference("pref_sulfate_free", false)
                                        viewModel.togglePreference("pref_fragrance_free", false)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🍃 Clean & Fragrance-Free", fontSize = 12.sp, color = SmokyBlack)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.togglePreference("pref_pregnancy_caution", false)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("🤰 Pregnancy Safe", fontSize = 12.sp, color = SmokyBlack)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All (${preferences.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OliveDrab,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat.categoryId,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat.categoryId) null else cat.categoryId
                            },
                            label = { Text(cat.title.split("&").first().trim(), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OliveDrab,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 4. Common Allergen Toggles Section
            if (sortAlphabetically) {
                items(allAllergenItems, key = { it.key }) { pref ->
                    AllergenItemCard(
                        pref = pref,
                        onToggle = { key, isEnabled -> viewModel.togglePreference(key, isEnabled) },
                        onUpdateRuleDetails = { key, classification, reasonNote ->
                            viewModel.updatePreferenceRuleDetails(key, classification, reasonNote)
                        }
                    )
                }
            } else {
                categories.forEach { group ->
                    val groupItems = allAllergenItems.filter { group.allergenKeys.contains(it.key) }
                    if (groupItems.isNotEmpty()) {
                        item(key = group.categoryId) {
                            CategoryGroupContainer(
                                group = group,
                                items = groupItems,
                                onToggle = { key, isEnabled -> viewModel.togglePreference(key, isEnabled) },
                                onUpdateRuleDetails = { key, classification, reasonNote ->
                                    viewModel.updatePreferenceRuleDetails(key, classification, reasonNote)
                                }
                            )
                        }
                    }
                }
            }

            // 5. Custom Allergens Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom User Allergens",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SmokyBlack
                                )
                                Text(
                                    text = "Separate Medical Allergies from Sensitivities & Personal Avoidances",
                                    fontSize = 12.sp,
                                    color = OliveDrabDark
                                )
                            }

                            Button(
                                onClick = { showAddCustomDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ScanOrange,
                                    contentColor = OnScanOrange
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_custom_allergen_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (customAllergens.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BoneLight)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No custom allergens configured yet. Tap '+ Add' above to create custom flags (e.g. Niacinamide, Phenoxyethanol).",
                                    fontSize = 12.sp,
                                    color = OliveDrabDark,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                customAllergens.forEach { custom ->
                                    CustomAllergenRow(
                                        custom = custom,
                                        onToggle = { viewModel.toggleCustomAllergen(custom.id, custom.isEnabled) },
                                        onCycleClassification = {
                                            val nextClass = when (custom.ruleClassification.uppercase()) {
                                                "MEDICAL_ALLERGY" -> "PERSONAL_SENSITIVITY"
                                                "PERSONAL_SENSITIVITY" -> "PREFERENCE_AVOIDANCE"
                                                else -> "MEDICAL_ALLERGY"
                                            }
                                            val nextSev = if (nextClass == "MEDICAL_ALLERGY") "HIGH_RISK" else "WARNING"
                                            viewModel.updateCustomAllergenDetails(custom.id, nextSev, nextClass)
                                        },
                                        onDelete = { viewModel.deleteCustomAllergen(custom.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Add Custom Allergen Dialog with 3-tier Rule Classification
    if (showAddCustomDialog) {
        var newName by remember { mutableStateOf("") }
        var selectedClassification by remember { mutableStateOf("MEDICAL_ALLERGY") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = {
                Text(
                    text = "Add Custom Allergen Rule",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the ingredient name or INCI identifier and choose how PureScan should classify matches.",
                        fontSize = 13.sp,
                        color = OliveDrabDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Ingredient / Chemical Name") },
                        placeholder = { Text("e.g. Phenoxyethanol, Nickel, Linalool...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_allergen_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "RULE CLASSIFICATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OliveDrabDark,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val classOptions = listOf(
                        Triple("MEDICAL_ALLERGY", "Medical Allergy (High Risk)", "Diagnosed immune allergy; escalates product to High Hazard"),
                        Triple("PERSONAL_SENSITIVITY", "Personal Sensitivity (Caution)", "Dermal irritation or flare trigger; flags Amber Caution"),
                        Triple("PREFERENCE_AVOIDANCE", "Preference Avoidance (Caution)", "Lifestyle or ethical ingredient avoidance")
                    )
                    classOptions.forEach { (code, label, desc) ->
                        val isSelected = selectedClassification == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BoneLight else FloralWhite)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) ScanOrange else CardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedClassification = code }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (code == "MEDICAL_ALLERGY") SemanticCritical else SmokyBlack
                                )
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = OliveDrabDark
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ScanOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isNotBlank()) {
                            val sev = if (selectedClassification == "MEDICAL_ALLERGY") "HIGH_RISK" else "WARNING"
                            viewModel.addCustomAllergen(
                                name = trimmed,
                                severityLevel = sev,
                                ruleClassification = selectedClassification
                            )
                        }
                        showAddCustomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OliveDrab,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_add_allergen_button")
                ) {
                    Text("Save Allergen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            }
        )
    }
}

@Composable
fun CategoryGroupContainer(
    group: AllergenCategoryGroup,
    items: List<PreferenceSettingEntity>,
    onToggle: (String, Boolean) -> Unit,
    onUpdateRuleDetails: (String, String, String) -> Unit = { _, _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SmokyBlack
            )
            Text(
                text = group.description,
                fontSize = 11.sp,
                color = OliveDrabDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            items.forEachIndexed { index, pref ->
                AllergenItemCard(
                    pref = pref,
                    onToggle = onToggle,
                    onUpdateRuleDetails = onUpdateRuleDetails
                )
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AllergenItemCard(
    pref: PreferenceSettingEntity,
    onToggle: (String, Boolean) -> Unit,
    onUpdateRuleDetails: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var showEditReasonDialog by remember { mutableStateOf(false) }
    val classification = pref.ruleClassification.uppercase()
    val isMedical = classification == "MEDICAL_ALLERGY"
    val badgeLabel = when (classification) {
        "MEDICAL_ALLERGY" -> "MEDICAL ALLERGY"
        "PERSONAL_SENSITIVITY" -> "SENSITIVITY"
        else -> "AVOIDANCE"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (pref.isEnabled) BoneLight else FloralWhite)
            .border(1.dp, if (pref.isEnabled) CardBorder else CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("allergen_card_${pref.key}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pref.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SmokyBlack
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMedical) SemanticCriticalBg else SemanticCautionBg)
                            .clickable {
                                val nextClass = when (classification) {
                                    "MEDICAL_ALLERGY" -> "PERSONAL_SENSITIVITY"
                                    "PERSONAL_SENSITIVITY" -> "PREFERENCE_AVOIDANCE"
                                    else -> "MEDICAL_ALLERGY"
                                }
                                onUpdateRuleDetails(pref.key, nextClass, pref.userReasonNote)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("allergen_class_badge_${pref.key}")
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isMedical) SemanticCritical else SemanticCaution
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pref.description,
                    fontSize = 11.sp,
                    color = OliveDrabDark,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = pref.isEnabled,
                onCheckedChange = { onToggle(pref.key, pref.isEnabled) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OliveDrab,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color(0xFFE5E3DD)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .clickable { showEditReasonDialog = true }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("allergen_reason_row_${pref.key}"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (pref.userReasonNote.isNotBlank()) {
                    "Why flagged: ${pref.userReasonNote}"
                } else {
                    "Why flagged: Personal rule • Tap to edit reason note"
                },
                fontSize = 10.sp,
                color = SmokyBlack.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Edit Rule",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ScanOrange
            )
        }
    }

    if (showEditReasonDialog) {
        var draftClass by remember(pref.ruleClassification) { mutableStateOf(pref.ruleClassification) }
        var draftNote by remember(pref.userReasonNote) { mutableStateOf(pref.userReasonNote) }

        AlertDialog(
            onDismissRequest = { showEditReasonDialog = false },
            title = {
                Text(
                    text = "Configure ${pref.label}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Classify whether this rule is a diagnosed Medical Allergy, Personal Sensitivity, or Preference Avoidance:",
                        fontSize = 12.sp,
                        color = OliveDrabDark
                    )
                    listOf(
                        "MEDICAL_ALLERGY" to "Medical Allergy (Escalates to High Hazard)",
                        "PERSONAL_SENSITIVITY" to "Personal Sensitivity (Amber Caution)",
                        "PREFERENCE_AVOIDANCE" to "Preference Avoidance (Lifestyle Filter)"
                    ).forEach { (code, label) ->
                        val selected = draftClass.equals(code, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) BoneLight else FloralWhite)
                                .border(1.dp, if (selected) ScanOrange else CardBorder, RoundedCornerShape(8.dp))
                                .clickable { draftClass = code }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = SmokyBlack,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ScanOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draftNote,
                        onValueChange = { draftNote = it },
                        label = { Text("Why I avoid this ingredient (Reason Note)") },
                        placeholder = { Text("e.g. Dermatologist patch-test positive, triggers eczema...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("allergen_reason_input_${pref.key}")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateRuleDetails(pref.key, draftClass, draftNote.trim())
                        showEditReasonDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OliveDrab,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditReasonDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            }
        )
    }
}

@Composable
fun CustomAllergenRow(
    custom: CustomAllergenEntity,
    onToggle: () -> Unit,
    onCycleClassification: () -> Unit,
    onDelete: () -> Unit
) {
    val classification = custom.ruleClassification.uppercase()
    val isHighRisk = classification == "MEDICAL_ALLERGY" || custom.severityLevel.equals("HIGH_RISK", ignoreCase = true)
    val badgeText = when (classification) {
        "MEDICAL_ALLERGY" -> "MEDICAL ALLERGY • HIGH RISK"
        "PREFERENCE_AVOIDANCE" -> "AVOIDANCE • CAUTION"
        else -> if (isHighRisk) "MEDICAL ALLERGY • HIGH RISK" else "SENSITIVITY • CAUTION"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (custom.isEnabled) BoneLight else FloralWhite)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = custom.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isHighRisk) SemanticCriticalBg else SemanticCautionBg)
                        .clickable { onCycleClassification() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighRisk) SemanticCritical else SemanticCaution
                    )
                }
            }
            Text(
                text = "Tap badge to cycle Medical Allergy / Sensitivity / Avoidance",
                fontSize = 10.sp,
                color = OliveDrabDark
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = custom.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OliveDrab
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Allergen",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
