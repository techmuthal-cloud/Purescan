package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabDark
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SmokyBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergenBottomSheet(
    sheetState: SheetState,
    viewModel: ScanViewModel,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FloralWhite,
        modifier = Modifier.testTag("allergen_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OliveDrabMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = OliveDrab,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quick Allergy Filter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SmokyBlack
                        )
                        Text(
                            text = "Active toggles flag items instantly during scans",
                            fontSize = 12.sp,
                            color = OliveDrabDark
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_allergen_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SmokyBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Common Allergen Toggles
            val commonAllergenKeys = listOf(
                "pref_gluten" to ("Gluten & Wheat Derivatives" to "Hydrolyzed wheat protein, barley, rye"),
                "pref_tree_nuts" to ("Tree Nuts & Peanuts" to "Almond, peanut, macadamia, argan oils"),
                "pref_paraben_free" to ("Parabens (Preservatives)" to "Methylparaben, propylparaben, ethylparaben"),
                "pref_sulfate_free" to ("Sulfates (SLS / SLES)" to "Sodium lauryl sulfate, sodium laureth sulfate"),
                "pref_fragrance_free" to ("Fragrance & Parfum" to "Aroma, perfume, linalool, limonene"),
                "pref_soy" to ("Soy & Legume Derivatives" to "Glycine soja, hydrolyzed soy protein"),
                "pref_silicone_free" to ("Silicones" to "Dimethicone, cyclopentasiloxane, siloxanes"),
                "pref_essential_oils" to ("Essential Oils & Terpenes" to "Tea tree, eucalyptus, lavender, citrus oils")
            )

            commonAllergenKeys.forEach { (key, info) ->
                val (label, desc) = info
                val pref = preferences.firstOrNull { it.key == key }
                val isEnabled = pref?.isEnabled ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isEnabled) BoneLight else Color.Transparent)
                        .border(1.dp, if (isEnabled) CardBorder else Color.Transparent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("allergen_toggle_$key"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SmokyBlack
                        )
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = OliveDrabDark
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.togglePreference(key, isEnabled) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OliveDrab,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color(0xFFE5E3DD)
                        )
                    )
                }
            }

            // Custom Allergens Summary if any
            if (customAllergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Custom User Allergens (${customAllergens.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmokyBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customAllergens.take(4).forEach { custom ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (custom.severityLevel == "HIGH_RISK") SemanticCritical.copy(alpha = 0.15f) else SemanticCaution.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = custom.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (custom.severityLevel == "HIGH_RISK") SemanticCritical else SemanticCaution
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open Full Allergen Settings Button
            Button(
                onClick = {
                    onDismiss()
                    onOpenFullSettings()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_full_allergen_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OliveDrab,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage All Allergens & Custom Rules",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
