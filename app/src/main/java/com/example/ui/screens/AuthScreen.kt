package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.ThemeMode

/**
 * Minimalist Editorial Authentication Screen
 * Features a single, cohesive editorial-style card containing:
 * 1. The PureScan icon (R.drawable.ic_purescan_logo) + serif brand header & tagline
 * 2. The four brand pillars ("Safer Products", "Clear Ingredients", "Trusted Information", "Healthier You")
 * 3. Clean authentication controls and the primary ScanOrange call-to-action button
 */
@Composable
fun AuthScreen(viewModel: ScanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var googleCustomEmail by remember { mutableStateOf("techmuthal@gmail.com") }
    var googleCustomName by remember { mutableStateOf("Priya Sharma") }

    val borderStrokeWidth = if (uiState.outdoorMode) 2.dp else 1.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Minimalist Top Row: Theme button & Skip/Guest
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { showThemeSelectorDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("auth_theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (uiState.themeMode) {
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    },
                    contentDescription = "Theme (${uiState.themeMode.label})",
                    tint = if (uiState.outdoorMode) ScanOrange else OliveDrab
                )
            }

            TextButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier.testTag("auth_skip_guest_button")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SmokyBlack,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Single Cohesive Editorial Card containing PureScan Icon, 4 Brand Pillars, and Primary Orange CTA
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(borderStrokeWidth, CardBorder, RoundedCornerShape(24.dp))
                .testTag("auth_editorial_card"),
            color = CardSurface,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Purescan 3D Glossy Checkmark-Leaf Emblem & PURESCAN Wordmark Header
                com.example.ui.components.Purescan3DEmblem(
                    size = 76.dp,
                    cornerRadius = 20.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SmokyBlack)) {
                            append("PURE")
                        }
                        withStyle(SpanStyle(color = ScanOrange)) {
                            append("SCAN")
                        }
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 31.sp,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SCAN • UNDERSTAND • CHOOSE SAFE",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.5.sp
                    ),
                    color = OliveDrab,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Four PureScan Brand Pillars in a clean 2x2 grid inside the editorial card
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrandPillarBadge(
                            icon = Icons.Default.Shield,
                            title = "Safer Products",
                            modifier = Modifier.weight(1f)
                        )
                        BrandPillarBadge(
                            icon = Icons.Default.Science,
                            title = "Clear Ingredients",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrandPillarBadge(
                            icon = Icons.Default.VerifiedUser,
                            title = "Trusted Information",
                            modifier = Modifier.weight(1f)
                        )
                        BrandPillarBadge(
                            icon = Icons.Default.Favorite,
                            title = "Healthier You",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Minimalist Sign In / Create Account Segmented Pill
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                    color = SurfaceCream
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    isSignUp = false
                                    viewModel.clearAuthError()
                                }
                                .testTag("auth_tab_sign_in"),
                            color = if (!isSignUp) ScanOrange else Color.Transparent
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSignUp) OnScanOrange else SmokyBlack,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    isSignUp = true
                                    viewModel.clearAuthError()
                                }
                                .testTag("auth_tab_sign_up"),
                            color = if (isSignUp) ScanOrange else Color.Transparent
                        ) {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSignUp) OnScanOrange else SmokyBlack,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.authErrorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(1.dp, SemanticCritical.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = SemanticCriticalBg
                    ) {
                        Text(
                            text = uiState.authErrorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticCritical,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isSignUp) {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                viewModel.clearAuthError()
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("Priya Sharma") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = OliveDrab)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SmokyBlack,
                                unfocusedTextColor = SmokyBlack,
                                focusedBorderColor = ScanOrange,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = SmokyBlack,
                                unfocusedLabelColor = OliveDrab,
                                cursorColor = ScanOrange
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearAuthError()
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("name@purescan.ai") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = OliveDrab)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SmokyBlack,
                        unfocusedTextColor = SmokyBlack,
                        focusedBorderColor = ScanOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = SmokyBlack,
                        unfocusedLabelColor = OliveDrab,
                        cursorColor = ScanOrange
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearAuthError()
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Minimum 4 characters") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OliveDrab)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = OliveDrabMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SmokyBlack,
                        unfocusedTextColor = SmokyBlack,
                        focusedBorderColor = ScanOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = SmokyBlack,
                        unfocusedLabelColor = OliveDrab,
                        cursorColor = ScanOrange
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Primary Orange Call-To-Action Button
                Button(
                    onClick = {
                        if (email.isBlank() && password.isBlank()) {
                            viewModel.signInWithQuickProfile(
                                fullName = "Priya Sharma",
                                email = "priya@purescan.ai"
                            )
                        } else {
                            viewModel.signInWithEmail(
                                email = email,
                                password = password,
                                fullName = fullName,
                                isSignUp = isSignUp
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("auth_email_submit_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    )
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = OnScanOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OnScanOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                    Text(
                        text = "  OR  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDrabMuted
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Minimalist Google Sign-In & Instant Access Row
                OutlinedButton(
                    onClick = { showGoogleAccountDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_google_button"),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceCream,
                        contentColor = SmokyBlack
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = ScanOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SmokyBlack
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = {
                        viewModel.signInWithQuickProfile(
                            fullName = "Priya Sharma",
                            email = "priya@purescan.ai"
                        )
                    },
                    modifier = Modifier.testTag("auth_quick_profile_button")
                ) {
                    Text(
                        text = "Instant One-Tap Profile Login →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OliveDrab
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showGoogleAccountDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog = false },
            containerColor = FloralWhite,
            title = {
                Text(
                    text = "Choose Google Account",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select or confirm your Google account to sign in to PureScan:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    OutlinedTextField(
                        value = googleCustomName,
                        onValueChange = { googleCustomName = it },
                        label = { Text("Account Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SmokyBlack,
                            unfocusedTextColor = SmokyBlack,
                            focusedBorderColor = ScanOrange,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    OutlinedTextField(
                        value = googleCustomEmail,
                        onValueChange = { googleCustomEmail = it },
                        label = { Text("Google Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SmokyBlack,
                            unfocusedTextColor = SmokyBlack,
                            focusedBorderColor = ScanOrange,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGoogleAccountDialog = false
                        viewModel.signInWithGoogleAccount(
                            fullName = googleCustomName.ifBlank { "Google User" },
                            email = googleCustomEmail.ifBlank { "user@gmail.com" }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    ),
                    modifier = Modifier.testTag("confirm_google_signin_button")
                ) {
                    Text(
                        text = "Continue as ${googleCustomName.substringBefore(" ").ifBlank { "User" }}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAccountDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            }
        )
    }

    if (showThemeSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showThemeSelectorDialog = false },
            containerColor = FloralWhite,
            title = {
                Text(
                    text = "Appearance & Readability",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = uiState.themeMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) SmokyBlack else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setThemeMode(mode) },
                                color = if (isSelected) ScanOrange else CardSurface
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) OnScanOrange else SmokyBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) OnScanOrange else SmokyBlack
                                    )
                                }
                            }
                        }
                    }

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
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contrast,
                                    contentDescription = null,
                                    tint = ScanOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Outdoor Readability",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SmokyBlack
                                    )
                                    Text(
                                        text = "Stronger borders & daylight contrast",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OliveDrab
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.outdoorMode,
                                onCheckedChange = { viewModel.setOutdoorMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnScanOrange,
                                    checkedTrackColor = ScanOrange,
                                    uncheckedThumbColor = OliveDrabMuted,
                                    uncheckedTrackColor = BoneLight
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showThemeSelectorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun BrandPillarBadge(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        color = SurfaceCream
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ScanOrange.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ScanOrange,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = SmokyBlack,
                maxLines = 1
            )
        }
    }
}
