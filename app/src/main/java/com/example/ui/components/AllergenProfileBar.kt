package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.theme.BrandScanOrange
import com.example.ui.theme.BrandSmokyBlack
import com.example.ui.theme.CardBorder
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabDark
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SmokyBlack

@Composable
fun SavedAllergenProfilesCard(
    viewModel: ScanViewModel,
    modifier: Modifier = Modifier
) {
    val allergenProfiles by viewModel.allergenProfiles.collectAsState()
    val activeAllergenProfile by viewModel.activeAllergenProfile.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val customAllergens by viewModel.customAllergens.collectAsState()

    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileDesc by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_allergen_profiles_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved Allergen Profiles",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Text(
                        text = activeAllergenProfile?.let { "Active: ${it.profileName}" }
                            ?: "Switch or save custom allergen profiles",
                        fontSize = 11.sp,
                        color = OliveDrabDark
                    )
                }
                OutlinedButton(
                    onClick = { showSaveProfileDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("save_allergen_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = OliveDrab
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Current", fontSize = 11.sp, color = OliveDrab, fontWeight = FontWeight.Bold)
                }
            }

            if (allergenProfiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allergenProfiles, key = { it.id }) { profile ->
                        FilterChip(
                            selected = profile.isActive,
                            onClick = { viewModel.activateAllergenProfile(profile.id) },
                            label = {
                                Text(
                                    text = profile.profileName,
                                    fontSize = 12.sp,
                                    fontWeight = if (profile.isActive) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (profile.isActive) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OliveDrab,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("allergen_profile_chip_${profile.id}")
                        )
                    }
                }
            }
        }
    }

    if (showSaveProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProfileDialog = false },
            title = { Text("Save Allergen Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Save your current ${preferences.count { it.isEnabled }} enabled filter toggles and ${customAllergens.count { it.isEnabled }} custom triggers as a reusable profile.",
                        fontSize = 12.sp,
                        color = OliveDrabDark
                    )
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g., Strict Nut & Paraben Free") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_profile_name_input")
                    )
                    OutlinedTextField(
                        value = newProfileDesc,
                        onValueChange = { newProfileDesc = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("e.g., Daily skincare & food filter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.saveCurrentAsAllergenProfile(newProfileName, newProfileDesc)
                            newProfileName = ""
                            newProfileDesc = ""
                            showSaveProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveDrab),
                    modifier = Modifier.testTag("confirm_save_profile_button")
                ) {
                    Text("Save Profile", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProfileDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            }
        )
    }
}

/**
 * Actionable CameraX capture failure recovery banner (PS-001).
 * Displayed when a camera capture attempt encounters an error, letting the user
 * retry CameraX capture, pick from gallery, or explicitly test with a demo preset.
 */
@Composable
fun CameraCaptureErrorBanner(
    errorMessage: String,
    onRetryCapture: () -> Unit,
    onPickFromGallery: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BrandSmokyBlack.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, SemanticCritical),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("camera_capture_error_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SemanticCritical,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Camera Capture Incomplete",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD8CFBC)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRetryCapture,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandScanOrange,
                        contentColor = BrandSmokyBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("retry_camera_capture_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry Capture", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onPickFromGallery,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("error_pick_gallery_button")
                ) {
                    Text("Use Gallery Photo", fontSize = 12.sp, color = Color.White)
                }
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Dismiss", fontSize = 12.sp, color = Color(0xFFD8CFBC))
                }
            }
        }
    }
}
