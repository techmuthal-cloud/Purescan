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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.AllergenBottomSheet
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.Bone
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SmokyBlack

/**
 * Screen 10: Profile & Settings Screen
 * Faithfully matches Screen 10 in the user reference image:
 * - Top bar: "Profile" title in serif, Settings gear icon on right
 * - Avatar: Large circular badge with botanical leaf
 * - Name: "Priya Sharma", email: "priya@mail.com"
 * - Premium Plan banner with gold crown and unlimited scans text
 * - Menu List:
 *   - My Preferences
 *   - Allergens & Dietary Needs
 *   - My Lists
 *   - Scan History
 *   - Notifications
 *   - Help & Support
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val allScans by viewModel.allScans.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editedName by remember(uiState.userName) { mutableStateOf(uiState.userName) }
    var editedEmail by remember(uiState.userEmail) { mutableStateOf(uiState.userEmail) }
    var quickAllergenInput by remember { mutableStateOf("") }
    var quickAllergenSeverity by remember { mutableStateOf("HIGH_RISK") }
    var showAllergenBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("profile_screen")
    ) {
        if (uiState.showLanguageDialog) {
            com.example.ui.components.LanguageSelectorDialog(
                selectedLanguage = uiState.selectedLanguage,
                onSelectLanguage = { viewModel.setAppLanguage(it) },
                onDismiss = { viewModel.setShowLanguageDialog(false) }
            )
        }

        // Top Bar: Left Purescan 3D Emblem, Center PURESCAN • Profile, Right Language Pill + Gear icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            com.example.ui.components.Purescan3DEmblem(
                size = 34.dp,
                cornerRadius = 9.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                com.example.ui.components.PurescanBrandWordmark(
                    fontSize = 18.sp,
                    primaryTextColor = SmokyBlack
                )
                Text(
                    text = com.example.ui.util.AppLocalization.profileTab(uiState.selectedLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveDrabMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.ui.components.LanguageSelectorPill(
                    selectedLanguage = uiState.selectedLanguage,
                    onClick = { viewModel.setShowLanguageDialog(true) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Preferences) },
                    modifier = Modifier.testTag("profile_settings_gear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SmokyBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Avatar & Name Section (Centered as in Screen 10)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditProfileDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.example.ui.components.Purescan3DEmblem(
                        size = 84.dp,
                        cornerRadius = 24.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = SmokyBlack
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = uiState.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OliveDrabMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val providerBadgeText = when (uiState.authProvider) {
                        "GOOGLE" -> "Verified Google Account • Auto-Login Active"
                        "EMAIL" -> "Email Account • Auto-Login Active"
                        "LOCAL_PROFILE" -> "Local Profile • Auto-Login Active"
                        else -> "Guest Session"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BoneLight
                    ) {
                        Text(
                            text = providerBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OliveDrab,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Premium Plan Card (Screen 10)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.showPaywall(true) }
                        .testTag("profile_premium_card"),
                    color = Color(0xFFEFE8DB),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE4DAC8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF8B6B23),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (uiState.isPremium) "PureScan Premium" else "Premium Plan",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = SmokyBlack
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.isPremium) "Active • Unlimited scientific lookups" else "Unlock full access to all features",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SmokyBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Custom Language Selection Card (8 Languages for App UI & Gemini AI Reports)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.setShowLanguageDialog(true) }
                        .testTag("profile_language_card"),
                    color = com.example.ui.theme.CardSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = com.example.ui.util.AppLocalization.languageSelectorTitle(uiState.selectedLanguage),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SmokyBlack
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${uiState.selectedLanguage.flagEmoji} ${uiState.selectedLanguage.nativeName} (${uiState.selectedLanguage.englishName}) • Tap to choose from 8 languages",
                                style = MaterialTheme.typography.bodySmall,
                                color = OliveDrab
                            )
                        }
                        com.example.ui.components.LanguageSelectorPill(
                            selectedLanguage = uiState.selectedLanguage,
                            onClick = { viewModel.setShowLanguageDialog(true) }
                        )
                    }
                }
            }

            // Appearance & Theme Selector Card (Light / Dark / System + Outdoor Readability)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .testTag("profile_theme_card"),
                    color = com.example.ui.theme.CardSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Appearance & Readability",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SmokyBlack
                            )
                            Text(
                                text = uiState.themeMode.label + if (uiState.outdoorMode) " • Outdoor" else "",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.ScanOrange
                            )
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) SmokyBlack else CardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setThemeMode(mode) }
                                        .testTag("profile_theme_${mode.name.lowercase()}"),
                                    color = if (isSelected) com.example.ui.theme.ScanOrange else com.example.ui.theme.SurfaceCream
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) com.example.ui.theme.OnScanOrange else SmokyBlack,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 9.dp)
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
                                    text = "Outdoor High-Contrast Mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack
                                )
                                Text(
                                    text = "Enhanced contrast & borders for bright daylight scanning",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OliveDrabMuted
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = uiState.outdoorMode,
                                onCheckedChange = { viewModel.setOutdoorMode(it) },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = com.example.ui.theme.OnScanOrange,
                                    checkedTrackColor = com.example.ui.theme.ScanOrange
                                ),
                                modifier = Modifier.testTag("profile_outdoor_mode_switch")
                            )
                        }
                    }
                }
            }

            // Inline Allergy Filter & Sensitivities Card
            item {
                val quickFilterKeys = listOf(
                    "pref_tree_nuts" to "Tree Nuts",
                    "pref_gluten" to "Gluten",
                    "pref_soy" to "Soy",
                    "pref_fragrance_free" to "Fragrance",
                    "pref_sulfate_free" to "Sulfates",
                    "pref_paraben_free" to "Parabens",
                    "pref_essential_oils" to "Essential Oils"
                )
                val activePrefCount = preferences.count { it.isEnabled }
                val activeCustomCount = customAllergens.count { it.isEnabled }
                val totalActiveFilters = activePrefCount + activeCustomCount

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .testTag("profile_allergy_filter_card"),
                    color = com.example.ui.theme.CardSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.ScanOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Allergy Filter",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = SmokyBlack
                                    )
                                    Text(
                                        text = "Auto-flags matching ingredients in every scan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OliveDrabMuted
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (totalActiveFilters > 0) com.example.ui.theme.SemanticCriticalBg else BoneLight,
                                modifier = Modifier.border(
                                    1.dp,
                                    if (totalActiveFilters > 0) com.example.ui.theme.SemanticCritical.copy(alpha = 0.45f) else CardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                            ) {
                                Text(
                                    text = "$totalActiveFilters Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalActiveFilters > 0) com.example.ui.theme.SemanticCritical else OliveDrab,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Quick Allergen Filter Toggles
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickFilterKeys.forEach { (key, label) ->
                                val prefEntity = preferences.firstOrNull { it.key == key }
                                val isEnabled = prefEntity?.isEnabled == true
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (isEnabled) 1.5.dp else 1.dp,
                                            color = if (isEnabled) com.example.ui.theme.ScanOrange else CardBorder,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            viewModel.togglePreference(key, isEnabled)
                                        }
                                        .testTag("profile_quick_filter_$key"),
                                    color = if (isEnabled) com.example.ui.theme.ScanOrange.copy(alpha = 0.14f) else FloralWhite
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isEnabled) com.example.ui.theme.ScanOrange else OliveDrabMuted.copy(alpha = 0.45f)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Medium,
                                            color = SmokyBlack
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Personal Allergen Chips with Severity Toggle
                        if (customAllergens.isNotEmpty()) {
                            Text(
                                text = "Personal Allergen List (tap badge to switch High Risk / Warning):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OliveDrab
                            )
                            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                customAllergens.forEach { allergen ->
                                    val isHighRisk = !allergen.severityLevel.equals("WARNING", ignoreCase = true)
                                    val badgeColor = if (isHighRisk) com.example.ui.theme.SemanticCritical else com.example.ui.theme.SemanticCaution
                                    val chipBg = when {
                                        !allergen.isEnabled -> BoneLight
                                        isHighRisk -> com.example.ui.theme.SemanticCriticalBg
                                        else -> com.example.ui.theme.SemanticCautionBg
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = chipBg,
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                if (allergen.isEnabled) badgeColor.copy(alpha = 0.55f) else CardBorder,
                                                RoundedCornerShape(18.dp)
                                            )
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable {
                                                viewModel.toggleCustomAllergen(allergen.id, allergen.isEnabled)
                                            }
                                            .testTag("profile_allergen_chip_${allergen.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = allergen.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (allergen.isEnabled) SmokyBlack else OliveDrabMuted
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (allergen.isEnabled) badgeColor else OliveDrabMuted,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.toggleCustomAllergenSeverity(
                                                            allergen.id,
                                                            allergen.severityLevel
                                                        )
                                                    }
                                                    .testTag("profile_allergen_severity_${allergen.id}")
                                            ) {
                                                Text(
                                                    text = if (isHighRisk) "HIGH RISK" else "WARNING",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.deleteCustomAllergen(allergen.id) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "×",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OliveDrab
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Quick-Add Custom Allergen Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.OutlinedTextField(
                                value = quickAllergenInput,
                                onValueChange = { quickAllergenInput = it },
                                placeholder = {
                                    Text(
                                        text = "Add allergen (e.g. Peanut, Lanolin)...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OliveDrabMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_quick_add_allergen_input")
                            )

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        quickAllergenSeverity = if (quickAllergenSeverity == "HIGH_RISK") "WARNING" else "HIGH_RISK"
                                    }
                                    .testTag("profile_quick_add_severity_toggle"),
                                color = if (quickAllergenSeverity == "HIGH_RISK") {
                                    com.example.ui.theme.SemanticCriticalBg
                                } else {
                                    com.example.ui.theme.SemanticCautionBg
                                }
                            ) {
                                Text(
                                    text = if (quickAllergenSeverity == "HIGH_RISK") "High Risk" else "Warning",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quickAllergenSeverity == "HIGH_RISK") {
                                        com.example.ui.theme.SemanticCritical
                                    } else {
                                        com.example.ui.theme.SemanticCaution
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (quickAllergenInput.isNotBlank()) {
                                        viewModel.addCustomAllergen(quickAllergenInput, quickAllergenSeverity)
                                        quickAllergenInput = ""
                                    }
                                },
                                enabled = quickAllergenInput.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.ScanOrange,
                                    contentColor = com.example.ui.theme.OnScanOrange
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier.testTag("profile_quick_add_allergen_button")
                            ) {
                                Text(
                                    text = "Add",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Link to Full Allergy Editor & Quick Sheet
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(com.example.ui.theme.SurfaceCream)
                                    .clickable { viewModel.navigateTo(Screen.AllergenSettings) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                                    .testTag("profile_open_full_allergy_editor"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Allergy & Sensitizer Settings",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SmokyBlack
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.ScanOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                    .clickable { showAllergenBottomSheet = true }
                                    .testTag("profile_quick_allergen_sheet_trigger"),
                                color = BoneLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = OliveDrab,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Quick Sheet",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SmokyBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Menu Section: The 6 exact menu items from Screen 10 + Ingredient Glossary + PDF Export
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileMenuRow(
                        icon = Icons.Default.Tune,
                        title = "My Preferences",
                        testTag = "menu_preferences",
                        onClick = { viewModel.navigateTo(Screen.Preferences) }
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Shield,
                        title = "Allergens & Dietary Needs",
                        testTag = "menu_allergens",
                        onClick = { viewModel.navigateTo(Screen.AllergenSettings) }
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Eco,
                        title = "Ingredient Glossary (A–Z)",
                        testTag = "menu_glossary",
                        onClick = { viewModel.navigateTo(Screen.Glossary) }
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Bookmark,
                        title = "My Lists",
                        testTag = "menu_lists",
                        onClick = { viewModel.navigateTo(Screen.Saved) }
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.History,
                        title = "Scan History",
                        testTag = "menu_history",
                        onClick = { viewModel.navigateTo(Screen.History) }
                    )

                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        testTag = "menu_notifications",
                        onClick = { showNotificationsDialog = true }
                    )

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help & Support",
                        testTag = "menu_help",
                        onClick = { showHelpDialog = true }
                    )
                }
            }

            // Export History as PDF Report CTA
            item {
                Button(
                    onClick = {
                        com.example.ui.util.PdfExportHelper.exportAndShareHistoryPdf(
                            context = context,
                            scans = allScans,
                            onError = { viewModel.showToast(it) }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_export_pdf_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SmokyBlack,
                        contentColor = FloralWhite
                    )
                ) {
                    Text(
                        text = "Export Scan History as PDF Report",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_sign_out_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SmokyBlack)
                ) {
                    Text(
                        text = if (uiState.isAuthenticated) "Sign Out / Switch Account" else "Sign In to Account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = FloralWhite,
            title = {
                Text("Edit Profile", style = MaterialTheme.typography.titleLarge, color = SmokyBlack)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editedEmail,
                        onValueChange = { editedEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(editedName, editedEmail)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyBlack, contentColor = FloralWhite)
                ) {
                    Text("Save")
                }
            }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = FloralWhite,
            title = {
                Text("Notifications & Alerts", style = MaterialTheme.typography.titleLarge, color = SmokyBlack)
            },
            text = {
                Text(
                    "You will receive alerts when products in your saved lists undergo regulatory reassessment by US CIR, EU SCCS, or FDA panels.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OliveDrab
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyBlack, contentColor = FloralWhite)
                ) {
                    Text("Done")
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = FloralWhite,
            title = {
                Text("Help & Scientific Grounding", style = MaterialTheme.typography.titleLarge, color = SmokyBlack)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "PureScan cross-examines ingredient lists against peer-reviewed toxicology panels: US Cosmetic Ingredient Review (CIR), EU Scientific Committee on Consumer Safety (SCCS), and FDA regulations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Text(
                        "For support or to report missing formulations, contact: support@purescan.ai",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrabMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyBlack, contentColor = FloralWhite)
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showAllergenBottomSheet) {
        AllergenBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            viewModel = viewModel,
            onOpenFullSettings = { viewModel.navigateTo(Screen.AllergenSettings) },
            onDismiss = { showAllergenBottomSheet = false }
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = com.example.ui.theme.CardSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SmokyBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = SmokyBlack
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OliveDrabMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
