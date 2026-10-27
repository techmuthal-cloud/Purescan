package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Global reactive theme state synced by MyApplicationTheme so that both
 * MaterialTheme.colorScheme, LocalScanSafePalette, and direct token references
 * update instantaneously when switching Light, Dark, System, or Outdoor Readability modes.
 */
object ScanSafeThemeController {
    var isDark by mutableStateOf(false)
    var isOutdoorHighContrast by mutableStateOf(false)
}

// Official Design.md Brand Constants (Static Reference Tokens)
val BrandSmokyBlack = Color(0xFF11120D)
val BrandOliveDrab = Color(0xFF565449)
val BrandBone = Color(0xFFD8CFBC)
val BrandFloralWhite = Color(0xFFFFFBF4)
val BrandScanOrange = Color(0xFFF5A623)
val BrandSuccessGreen = Color(0xFF2F6B3B)
val BrandCautionAmber = Color(0xFF8A4B00)
val BrandRiskRed = Color(0xFFA5221B)
val BrandSoftGreenSurface = Color(0xFFEAF1E2)
val BrandSoftAmberSurface = Color(0xFFFFF0D8)
val BrandSoftRedSurface = Color(0xFFFBE8E5)
val BrandDivider = Color(0xFFD8D1C5)
val BrandDarkCameraSurface = Color(0xFF11120D)

// Adaptive Tokens (Automatically adapt to Light, Dark, and Outdoor High-Contrast modes)
val SmokyBlack: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFFFFBF4)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF000000)
        else -> Color(0xFF11120D)
    }

val OliveDrab: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFD8CFBC)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF22231B)
        else -> Color(0xFF565449)
    }

val OliveDrabMuted: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFA8A393)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF35342B)
        else -> Color(0xFF6B685B)
    }

val OliveDrabDark: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFC4BEAE)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF1A1A14)
        else -> Color(0xFF3E3C34)
    }

val Bone: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF33362C)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFFC7BDA6)
        else -> Color(0xFFD8CFBC)
    }

val BoneLight: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF262920)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFFE2DAC8)
        else -> Color(0xFFEBE6DC)
    }

val FloralWhite: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF11120D)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFFFFFFFF)
        else -> Color(0xFFFFFBF4)
    }

val SurfaceCream: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF1B1D16)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFFF6F2E9)
        else -> Color(0xFFF4EFE4)
    }

val CardSurface: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF1C1E17)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFFFFFFFF)
        else -> Color(0xFFFFFFFF)
    }

val CardBorder: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF383B30)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF11120D)
        else -> Color(0xFFD8D1C5)
    }

// Primary Accent: scanOrange (#F5A623) with dark text (#11120D) per Design.md WCAG AA contrast rule
val ScanOrange: Color
    get() = if (ScanSafeThemeController.isOutdoorHighContrast) Color(0xFFF39C12) else Color(0xFFF5A623)

val ScanOrangeSoft: Color
    get() = if (ScanSafeThemeController.isDark) Color(0xFF3A2B12) else Color(0xFFFFF0D8)

val OnScanOrange: Color
    get() = Color(0xFF11120D)

// Semantic Palette (Design.md Evidence-Based Status Tokens)
val SemanticSuccess: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF8CD79B)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF1B4D26)
        else -> Color(0xFF2F6B3B)
    }

val SemanticSuccessBg: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF1C3322)
        else -> Color(0xFFEAF1E2)
    }

val SemanticCaution: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFFFC266)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF6B3800)
        else -> Color(0xFF8A4B00)
    }

val SemanticCautionBg: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF38260E)
        else -> Color(0xFFFFF0D8)
    }

val SemanticCritical: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFFFF8A82)
        ScanSafeThemeController.isOutdoorHighContrast -> Color(0xFF7D120C)
        else -> Color(0xFFA5221B)
    }

val SemanticCriticalBg: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF3D1715)
        else -> Color(0xFFFBE8E5)
    }

val SemanticInfo: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF8CC4E8)
        else -> Color(0xFF315C78)
    }

val SemanticInfoBg: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF182A36)
        else -> Color(0xFFE9F1F6)
    }

val SemanticUnknown: Color
    get() = OliveDrab

val SemanticUnknownBg: Color
    get() = when {
        ScanSafeThemeController.isDark -> Color(0xFF25271F)
        else -> Color(0xFFEDEBE5)
    }
