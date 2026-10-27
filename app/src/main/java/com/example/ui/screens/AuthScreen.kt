package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.testTag
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
import com.example.ui.util.AppLocalization

/**
 * Clean, Clutter-Free Editorial Authentication Screen (Refactored per P0 UX Review):
 * - Removes promotional benefit chips and competing one-tap demo profile cards.
 * - Defaults to a dedicated Sign In view with email/password fields and primary Sign In CTA.
 * - Provides a prominent "Continue with Google" button.
 * - Features a clear bottom Account Creation path ("Don't have an account? Create Account")
 *   that transitions smoothly into the registration form.
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
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var googleCustomEmail by remember { mutableStateOf("techmuthal@gmail.com") }
    var googleCustomName by remember { mutableStateOf("Priya Sharma") }

    val borderStrokeWidth = if (uiState.outdoorMode) 2.dp else 1.dp

    if (uiState.showLanguageDialog) {
        com.example.ui.components.LanguageSelectorDialog(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.setAppLanguage(it) },
            onDismiss = { viewModel.setShowLanguageDialog(false) }
        )
    }

    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            containerColor = FloralWhite,
            title = {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SmokyBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the email address associated with your PureScan account to reset your password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_reset_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = resetEmail.trim()
                        if (target.isBlank() || !target.contains("@")) {
                            viewModel.showToast("Please enter a valid email address for password recovery")
                        } else {
                            showPasswordResetDialog = false
                            viewModel.showToast("Password recovery instructions sent to $target")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScanOrange, contentColor = OnScanOrange),
                    modifier = Modifier.testTag("auth_confirm_reset_button")
                ) {
                    Text("Send Recovery")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel", color = SmokyBlack)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Minimalist Top Utility Bar: Language Selector, Theme Toggle & Continue as Guest
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            com.example.ui.components.LanguageSelectorPill(
                selectedLanguage = uiState.selectedLanguage,
                onClick = { viewModel.setShowLanguageDialog(true) }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showThemeSelectorDialog = true },
                    modifier = Modifier
                        .size(48.dp)
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
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = OliveDrab,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated Authentication Card: Logo + 1 concise value proposition + isolated Sign-In / Google / Create Account
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
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Brand Emblem & Wordmark
                com.example.ui.components.Purescan3DEmblem(
                    size = 68.dp,
                    cornerRadius = 18.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                        fontSize = 28.sp,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Single concise value proposition
                Text(
                    text = AppLocalization.authValueProposition(uiState.selectedLanguage),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = OliveDrab,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Dedicated Mode Header (Sign In vs Create Account)
                Text(
                    text = if (isSignUp) {
                        AppLocalization.createAccountLabel(uiState.selectedLanguage)
                    } else {
                        AppLocalization.signInLabel(uiState.selectedLanguage)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    ),
                    color = SmokyBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_mode_header"),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSignUp) {
                        "Create a profile to save your personal allergy rules and scan history."
                    } else {
                        "Sign in with your email or Google account to access your saved rules."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = OliveDrabMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action-triggered error banner
                if (uiState.authErrorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(1.dp, SemanticCritical.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .testTag("auth_error_banner"),
                        shape = RoundedCornerShape(12.dp),
                        color = SemanticCriticalBg
                    ) {
                        Text(
                            text = uiState.authErrorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticCritical,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Full Name input (only shown when creating an account)
                AnimatedVisibility(visible = isSignUp) {
                    Column {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                viewModel.clearAuthError()
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("Enter your name") },
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

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearAuthError()
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("name@example.com") },
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

                // Password input
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

                if (!isSignUp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                resetEmail = email
                                showPasswordResetDialog = true
                            },
                            modifier = Modifier.testTag("auth_forgot_password_button")
                        ) {
                            Text(
                                text = "Forgot password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OliveDrab
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Primary Isolated Sign-In / Create Account Action Button
                Button(
                    onClick = {
                        viewModel.signInWithEmail(
                            email = email,
                            password = password,
                            fullName = fullName,
                            isSignUp = isSignUp
                        )
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
                        text = if (isSignUp) {
                            AppLocalization.createAccountLabel(uiState.selectedLanguage)
                        } else {
                            AppLocalization.signInLabel(uiState.selectedLanguage)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = OnScanOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OnScanOrange,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider separating primary email sign-in from Google sign-in
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Prominent Google Sign-In Button
                OutlinedButton(
                    onClick = { showGoogleAccountDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_google_button"),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, CardBorder),
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = SmokyBlack
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = CardBorder.copy(alpha = 0.65f))

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Dedicated Bottom Account Creation / Sign-In Switch Path
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isSignUp = !isSignUp
                            viewModel.clearAuthError()
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                        .testTag(if (isSignUp) "auth_tab_sign_in" else "auth_tab_sign_up"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) {
                            "Already have an account? "
                        } else {
                            "Don't have an account? "
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Text(
                        text = if (isSignUp) {
                            AppLocalization.signInLabel(uiState.selectedLanguage)
                        } else {
                            AppLocalization.createAccountLabel(uiState.selectedLanguage)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ScanOrange
                        ),
                        modifier = Modifier.testTag("auth_switch_mode_button")
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
                    text = "Continue with Google",
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
                        text = "Confirm your Google account details to sign in to PureScan:",
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
