package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.components.BotanicalTopAppBar
import com.example.ui.theme.Bone
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticInfo
import com.example.ui.theme.SemanticInfoBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()

    var newAllergenText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val popularSuggestions = listOf(
        "Phenoxyethanol",
        "Linalool",
        "Salicylic Acid",
        "Dimethicone",
        "Limonene",
        "Niacinamide",
        "Methylisothiazolinone"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("preferences_screen")
    ) {
        BotanicalTopAppBar(
            title = "Settings & Preferences",
            showBackButton = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Appearance & Outdoor Readability Section (Light / Dark / System + Outdoor Mode)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .testTag("settings_theme_card"),
                    color = com.example.ui.theme.CardSurface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Theme & Display Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SmokyBlack,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Switch between Light, Dark, System, and Outdoor Daylight mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.ui.theme.ThemeMode.entries.forEach { mode ->
                                val isSelected = uiState.themeMode == mode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) SmokyBlack else CardBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setThemeMode(mode) }
                                        .testTag("settings_theme_${mode.name.lowercase()}"),
                                    color = if (isSelected) com.example.ui.theme.ScanOrange else SurfaceCream
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) com.example.ui.theme.OnScanOrange else SmokyBlack,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "High-Contrast Outdoor Readability",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack
                                )
                                Text(
                                    text = "Increases border contrast and font weight for bright stores & daylight",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                            }
                            Switch(
                                checked = uiState.outdoorMode,
                                onCheckedChange = { viewModel.setOutdoorMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = com.example.ui.theme.OnScanOrange,
                                    checkedTrackColor = com.example.ui.theme.ScanOrange,
                                    uncheckedThumbColor = OliveDrabMuted,
                                    uncheckedTrackColor = BoneLight
                                ),
                                modifier = Modifier.testTag("settings_outdoor_mode_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Subtle Haptic Feedback",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack
                                )
                                Text(
                                    text = "Physical confirmation for camera capture, analysis completion, and alerts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                            }
                            Switch(
                                checked = uiState.hapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = com.example.ui.theme.OnScanOrange,
                                    checkedTrackColor = com.example.ui.theme.ScanOrange,
                                    uncheckedThumbColor = OliveDrabMuted,
                                    uncheckedTrackColor = BoneLight
                                ),
                                modifier = Modifier.testTag("settings_haptics_switch")
                            )
                        }
                    }
                }
            }

            // Informative Hero Banner about Gemini API Personalization
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    color = SemanticInfoBg
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SemanticInfo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SemanticInfo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Personalized Gemini Protection",
                                style = MaterialTheme.typography.titleSmall,
                                color = SmokyBlack,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Your personal allergen list and dietary exclusions are provided to the Gemini API during scans. Any detected allergens are immediately flagged with custom warnings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SmokyBlack.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Custom Allergen Input & Chips Section
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    color = com.example.ui.theme.CardSurface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Custom Sensitive Ingredients",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SmokyBlack,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Type any ingredient or chemical you want flagged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                            }
                            if (customAllergens.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BoneLight,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${customAllergens.count { it.isEnabled }} active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OliveDrab,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Text Field and Add Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newAllergenText,
                                onValueChange = { newAllergenText = it },
                                placeholder = {
                                    Text(
                                        "e.g. Phenoxyethanol, Soy...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OliveDrabMuted
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newAllergenText.isNotBlank()) {
                                            viewModel.addCustomAllergen(newAllergenText)
                                            newAllergenText = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SmokyBlack,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = FloralWhite,
                                    unfocusedContainerColor = FloralWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_allergen_input")
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (newAllergenText.isNotBlank()) {
                                        viewModel.addCustomAllergen(newAllergenText)
                                        newAllergenText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = newAllergenText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SmokyBlack,
                                    contentColor = FloralWhite,
                                    disabledContainerColor = BoneLight,
                                    disabledContentColor = OliveDrabMuted
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("add_allergen_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Allergen",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        // Quick suggestions pills
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Common Sensitizers:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OliveDrab,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            popularSuggestions.forEach { suggestion ->
                                val alreadyAdded = customAllergens.any { it.name.equals(suggestion, ignoreCase = true) }
                                if (!alreadyAdded) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = FloralWhite,
                                        modifier = Modifier
                                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                viewModel.addCustomAllergen(suggestion)
                                            }
                                            .testTag("suggestion_${suggestion.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = OliveDrab,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = suggestion,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SmokyBlack
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Active Custom Allergen Chips
                        if (customAllergens.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Active Allergen Chips (tap badge to switch High Risk vs Warning):",
                                style = MaterialTheme.typography.labelMedium,
                                color = SmokyBlack,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                customAllergens.forEach { allergen ->
                                    val isHighRisk = !allergen.severityLevel.equals("WARNING", ignoreCase = true)
                                    val accentColor = if (isHighRisk) com.example.ui.theme.SemanticCritical else SemanticCaution
                                    val chipBg = when {
                                        !allergen.isEnabled -> BoneLight
                                        isHighRisk -> com.example.ui.theme.SemanticCriticalBg
                                        else -> SemanticCautionBg
                                    }
                                    val chipBorder = if (allergen.isEnabled) accentColor.copy(alpha = 0.55f) else CardBorder
                                    val chipText = if (allergen.isEnabled) SmokyBlack else OliveDrabMuted

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = chipBg,
                                        modifier = Modifier
                                            .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable {
                                                viewModel.toggleCustomAllergen(allergen.id, allergen.isEnabled)
                                            }
                                            .testTag("allergen_chip_${allergen.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (allergen.isEnabled) accentColor else OliveDrabMuted)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = allergen.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = chipText,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (allergen.isEnabled) accentColor else OliveDrabMuted,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.toggleCustomAllergenSeverity(
                                                            allergen.id,
                                                            allergen.severityLevel
                                                        )
                                                    }
                                                    .testTag("allergen_severity_${allergen.id}")
                                            ) {
                                                Text(
                                                    text = if (isHighRisk) "HIGH RISK" else "WARNING",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { viewModel.deleteCustomAllergen(allergen.id) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Delete ${allergen.name}",
                                                    tint = OliveDrab,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Curated Sensitizers & Chemicals
            item {
                Text(
                    text = "Dermatological & Chemical Sensitizers",
                    style = MaterialTheme.typography.titleMedium,
                    color = SmokyBlack,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            val cosmeticPreferences = preferences.filter { it.category == "cosmetic" || it.category == "special_care" }
            items(cosmeticPreferences, key = { it.key }) { pref ->
                PreferenceToggleCard(
                    pref = pref,
                    onToggle = { viewModel.togglePreference(pref.key, pref.isEnabled) }
                )
            }

            // Section 2: Dietary & Contact Allergens
            item {
                Text(
                    text = "Dietary & Food Allergen Exclusions",
                    style = MaterialTheme.typography.titleMedium,
                    color = SmokyBlack,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            val dietaryPreferences = preferences.filter { it.category == "allergen" || it.category == "dietary" }
            items(dietaryPreferences, key = { it.key }) { pref ->
                PreferenceToggleCard(
                    pref = pref,
                    onToggle = { viewModel.togglePreference(pref.key, pref.isEnabled) }
                )
            }
        }
    }
}

@Composable
private fun PreferenceToggleCard(
    pref: com.example.data.local.PreferenceSettingEntity,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .testTag("preference_item_${pref.key}"),
        color = com.example.ui.theme.CardSurface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pref.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = SmokyBlack,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pref.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OliveDrab,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = pref.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = com.example.ui.theme.OnScanOrange,
                    checkedTrackColor = com.example.ui.theme.ScanOrange,
                    uncheckedThumbColor = OliveDrabMuted,
                    uncheckedTrackColor = BoneLight
                ),
                modifier = Modifier.testTag("switch_${pref.key}")
            )
        }
    }
}
