package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.BrandBone
import com.example.ui.theme.BrandDarkCameraSurface
import com.example.ui.theme.BrandScanOrange
import kotlinx.coroutines.delay

/**
 * Screen 1: Minimalist Splash Screen
 * Strictly matches Screen 1 of the attached 5-screen starting UI image:
 * - Solid dark background (#11120D)
 * - Two-leaf orange botanical emblem at top
 * - Bold serif wordmark with white + orange accent
 * - Subtitle: "Scan. Check. Stay Safe."
 * - Center editorial illustration: Woman in orange sweater holding phone scanning a white product bottle, flanked by botanical leaves
 * - Bottom horizontal progress pill bar ([===---]) and quote: "A healthier tomorrow\nstarts with what you read today."
 */
@Composable
fun SplashScreen(viewModel: ScanViewModel) {
    val uiState = viewModel.uiState.value
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            viewModel.navigateTo(Screen.Home)
        } else {
            delay(2000)
            viewModel.navigateTo(Screen.Onboarding)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDarkCameraSurface)
            .clickable {
                if (uiState.isAuthenticated) {
                    viewModel.navigateTo(Screen.Home)
                } else {
                    viewModel.navigateTo(Screen.Onboarding)
                }
            }
            .testTag("splash_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Header (3D Glossy Checkmark-Leaf Emblem + PURESCAN Wordmark + Tagline)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                com.example.ui.components.Purescan3DEmblem(
                    size = 68.dp,
                    cornerRadius = 18.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            append("PURE")
                        }
                        withStyle(SpanStyle(color = BrandScanOrange)) {
                            append("SCAN")
                        }
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 38.sp,
                        letterSpacing = 1.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SCAN • UNDERSTAND • CHOOSE SAFE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp
                    ),
                    color = BrandBone.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }

            // Center Illustration: Woman in orange sweater scanning white product bottle with botanical leaves
            Box(
                modifier = Modifier.size(270.dp),
                contentAlignment = Alignment.Center
            ) {
                SplashEditorialIllustration()
            }

            // Bottom Progress Bar Pill & Quote
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horizontal pill progress indicator [===---] matching Screen 1
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandScanOrange)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "A healthier tomorrow\nstarts with what you read today.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    ),
                    color = BrandBone.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SplashEditorialIllustration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val inkDark = Color(0xFF11120D)
        val leafOlive = Color(0xFF2B3323)
        val leafStroke = Color(0xFF565E49)
        val skinTone = Color(0xFFF3D1B0)
        val sweaterOrange = Color(0xFFF5A623)
        val bottleWhite = Color(0xFFF9F6EE)
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Subtle dark circular aura
        drawCircle(
            color = Color(0xFF1A1C14),
            radius = w * 0.44f,
            center = Offset(w * 0.5f, h * 0.54f)
        )

        // Left & Right botanical background leaves
        val leftBranch = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            quadraticBezierTo(w * 0.08f, h * 0.62f, w * 0.14f, h * 0.40f)
            quadraticBezierTo(w * 0.24f, h * 0.52f, w * 0.22f, h * 0.82f)
            close()
        }
        drawPath(leftBranch, color = leafOlive)
        drawPath(leftBranch, color = leafStroke, style = stroke)

        val rightBranch = Path().apply {
            moveTo(w * 0.78f, h * 0.80f)
            quadraticBezierTo(w * 0.92f, h * 0.60f, w * 0.86f, h * 0.38f)
            quadraticBezierTo(w * 0.74f, h * 0.50f, w * 0.78f, h * 0.80f)
            close()
        }
        drawPath(rightBranch, color = leafOlive)
        drawPath(rightBranch, color = leafStroke, style = stroke)

        // Woman's dark hair backdrop
        val hairPath = Path().apply {
            moveTo(w * 0.34f, h * 0.26f)
            quadraticBezierTo(w * 0.44f, h * 0.14f, w * 0.53f, h * 0.25f)
            quadraticBezierTo(w * 0.57f, h * 0.42f, w * 0.50f, h * 0.50f)
            lineTo(w * 0.32f, h * 0.48f)
            quadraticBezierTo(w * 0.29f, h * 0.36f, w * 0.34f, h * 0.26f)
            close()
        }
        drawPath(hairPath, color = Color(0xFF1E2019))
        drawPath(hairPath, color = leafStroke, style = stroke)

        // Face & Neck
        drawCircle(
            color = skinTone,
            radius = w * 0.068f,
            center = Offset(w * 0.43f, h * 0.31f)
        )
        drawRoundRect(
            color = skinTone,
            topLeft = Offset(w * 0.405f, h * 0.36f),
            size = Size(w * 0.05f, h * 0.08f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Orange Sweater Torso
        val torsoPath = Path().apply {
            moveTo(w * 0.26f, h * 0.82f)
            quadraticBezierTo(w * 0.27f, h * 0.46f, w * 0.37f, h * 0.42f)
            lineTo(w * 0.49f, h * 0.42f)
            quadraticBezierTo(w * 0.58f, h * 0.47f, w * 0.60f, h * 0.82f)
            close()
        }
        drawPath(torsoPath, color = sweaterOrange)
        drawPath(torsoPath, color = inkDark, style = stroke)

        // Smartphone held in left hand
        drawRoundRect(
            color = Color(0xFF24261E),
            topLeft = Offset(w * 0.25f, h * 0.45f),
            size = Size(w * 0.11f, h * 0.20f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.25f, h * 0.45f),
            size = Size(w * 0.11f, h * 0.20f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Scan ray dashed lines between phone and bottle
        drawLine(
            color = sweaterOrange.copy(alpha = 0.75f),
            start = Offset(w * 0.38f, h * 0.50f),
            end = Offset(w * 0.60f, h * 0.46f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = sweaterOrange.copy(alpha = 0.75f),
            start = Offset(w * 0.38f, h * 0.57f),
            end = Offset(w * 0.60f, h * 0.59f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // White Cosmetic Bottle on right
        // Bottle neck & pump
        drawRoundRect(
            color = sweaterOrange,
            topLeft = Offset(w * 0.665f, h * 0.34f),
            size = Size(w * 0.05f, h * 0.06f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        // Bottle body
        drawRoundRect(
            color = bottleWhite,
            topLeft = Offset(w * 0.62f, h * 0.40f),
            size = Size(w * 0.14f, h * 0.34f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
        drawRoundRect(
            color = inkDark,
            topLeft = Offset(w * 0.62f, h * 0.40f),
            size = Size(w * 0.14f, h * 0.34f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = stroke
        )
        // Bottle label lines
        drawLine(
            color = sweaterOrange,
            start = Offset(w * 0.65f, h * 0.49f),
            end = Offset(w * 0.73f, h * 0.49f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = inkDark.copy(alpha = 0.6f),
            start = Offset(w * 0.65f, h * 0.55f),
            end = Offset(w * 0.72f, h * 0.55f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = inkDark.copy(alpha = 0.6f),
            start = Offset(w * 0.65f, h * 0.60f),
            end = Offset(w * 0.71f, h * 0.60f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Bottom base line
        drawLine(
            color = BrandBone.copy(alpha = 0.45f),
            start = Offset(w * 0.16f, h * 0.82f),
            end = Offset(w * 0.84f, h * 0.82f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
