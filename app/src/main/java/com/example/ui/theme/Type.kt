package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Editorial serif font family from local bundled Google Font
val DmSerifFamily = FontFamily(
    Font(R.font.dm_serif_display, FontWeight.Normal)
)

fun createScanSafeTypography(outdoorHighContrast: Boolean = false): Typography {
    val bodyBoost = if (outdoorHighContrast) 1f else 0f
    return Typography(
        displayLarge = TextStyle(
            fontFamily = DmSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (34 + bodyBoost).sp,
            lineHeight = (40 + bodyBoost).sp,
            letterSpacing = (-0.25).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = DmSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (28 + bodyBoost).sp,
            lineHeight = (34 + bodyBoost).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = DmSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (24 + bodyBoost).sp,
            lineHeight = (30 + bodyBoost).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = DmSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (20 + bodyBoost).sp,
            lineHeight = (26 + bodyBoost).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 + bodyBoost).sp,
            lineHeight = (24 + bodyBoost).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 + bodyBoost).sp,
            lineHeight = (22 + bodyBoost).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (14.5 + bodyBoost).sp,
            lineHeight = (20 + bodyBoost).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (outdoorHighContrast) FontWeight.Medium else FontWeight.Normal,
            fontSize = (16 + bodyBoost).sp,
            lineHeight = (23 + bodyBoost).sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (outdoorHighContrast) FontWeight.Medium else FontWeight.Normal,
            fontSize = (14.5 + bodyBoost).sp,
            lineHeight = (21 + bodyBoost).sp,
            letterSpacing = 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (outdoorHighContrast) FontWeight.Medium else FontWeight.Normal,
            fontSize = (12.5 + bodyBoost).sp,
            lineHeight = (17 + bodyBoost).sp,
            letterSpacing = 0.3.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 + bodyBoost).sp,
            lineHeight = (20 + bodyBoost).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (12.5 + bodyBoost).sp,
            lineHeight = (16 + bodyBoost).sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5 + bodyBoost).sp,
            lineHeight = (16 + bodyBoost).sp,
            letterSpacing = 0.4.sp
        )
    )
}

val ScanSafeTypography = createScanSafeTypography(false)
