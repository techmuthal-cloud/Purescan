package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandScanOrange,
    onPrimary = BrandSmokyBlack,
    primaryContainer = Color(0xFFFFF0D8),
    onPrimaryContainer = BrandSmokyBlack,
    secondary = BrandSmokyBlack,
    onSecondary = BrandFloralWhite,
    secondaryContainer = BrandBone,
    onSecondaryContainer = BrandSmokyBlack,
    tertiary = BrandSuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = BrandSoftGreenSurface,
    onTertiaryContainer = BrandSuccessGreen,
    background = BrandFloralWhite,
    onBackground = BrandSmokyBlack,
    surface = BrandFloralWhite,
    onSurface = BrandSmokyBlack,
    surfaceVariant = Color(0xFFF4EFE4),
    onSurfaceVariant = BrandOliveDrab,
    outline = BrandDivider,
    outlineVariant = Color(0xFFEBE6DC),
    error = BrandRiskRed,
    onError = Color.White,
    errorContainer = BrandSoftRedSurface,
    onErrorContainer = BrandRiskRed
)

private val OutdoorLightColorScheme = lightColorScheme(
    primary = Color(0xFFF39C12),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFFE5B4),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8CFBC),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF1B4D26),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEAF1E2),
    onTertiaryContainer = Color(0xFF1B4D26),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF6F2E9),
    onSurfaceVariant = Color(0xFF22231B),
    outline = Color.Black,
    outlineVariant = Color(0xFF565449),
    error = Color(0xFF7D120C),
    onError = Color.White,
    errorContainer = Color(0xFFFBE8E5),
    onErrorContainer = Color(0xFF7D120C)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandScanOrange,
    onPrimary = BrandSmokyBlack,
    primaryContainer = Color(0xFF3A2B12),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = BrandBone,
    onSecondary = BrandSmokyBlack,
    secondaryContainer = Color(0xFF282A22),
    onSecondaryContainer = BrandBone,
    tertiary = Color(0xFF8CD79B),
    onTertiary = BrandSmokyBlack,
    tertiaryContainer = Color(0xFF1C3322),
    onTertiaryContainer = Color(0xFFBCE3C3),
    background = BrandSmokyBlack,
    onBackground = BrandFloralWhite,
    surface = Color(0xFF191B15),
    onSurface = BrandFloralWhite,
    surfaceVariant = Color(0xFF24271F),
    onSurfaceVariant = BrandBone,
    outline = Color(0xFF383B30),
    outlineVariant = Color(0xFF2D3026),
    error = Color(0xFFFF8A82),
    onError = BrandSmokyBlack,
    errorContainer = Color(0xFF491816),
    onErrorContainer = Color(0xFFFFD4D1)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    outdoorHighContrast: Boolean = false,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Update reactive theme controller immediately during composition
    ScanSafeThemeController.isDark = darkTheme
    ScanSafeThemeController.isOutdoorHighContrast = outdoorHighContrast

    SideEffect {
        ScanSafeThemeController.isDark = darkTheme
        ScanSafeThemeController.isOutdoorHighContrast = outdoorHighContrast
    }

    val colorScheme = when {
        darkTheme -> DarkColorScheme
        outdoorHighContrast -> OutdoorLightColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = createScanSafeTypography(outdoorHighContrast),
        content = content
    )
}
