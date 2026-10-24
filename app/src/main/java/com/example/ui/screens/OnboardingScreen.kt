package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.Bone
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

/**
 * Screens 2, 3, 4: Minimalist Onboarding Screens 1–3
 * Strictly matches Screens 2, 3, and 4 in the attached 5-screen image:
 * - Screen 2 (Step 0): Top-right "Skip", Headline "Scan\nAny Product\nAnytime.", subtitle, center walking illustration, bottom 4 dots + circular orange -> button.
 * - Screen 3 (Step 1): Top-right "Skip", Headline "Get Clear\nInsights.", subtitle, center magnifying glass & bottles illustration, bottom 4 dots + circular orange -> button.
 * - Screen 4 (Step 2): Top-right "Skip", Top/Center sitting on green hill under warm sun illustration, followed by Headline "Make Safer\nChoices.", subtitle, centered 4 dots, and full-width orange "Get Started ->" button.
 */
@Composable
fun OnboardingScreen(viewModel: ScanViewModel) {
    var stepIndex by remember { mutableIntStateOf(0) }

    val mainHeadlines = listOf(
        "Scan\nAny Product\n",
        "Get Clear\n",
        "Make Safer\n"
    )
    val accentWords = listOf(
        "Anytime.",
        "Insights.",
        "Choices."
    )
    val subtitles = listOf(
        "Scan barcodes or ingredient\nlabels to understand\nwhat's inside.",
        "See ingredient analysis,\nsafety information and\ntrusted sources.",
        "Protect yourself, your family\nand the environment."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .padding(horizontal = 26.dp, vertical = 20.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Minimalist Top Bar: Only "Skip" aligned to the right, matching Screens 2, 3 & 4 of image
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    val dest = if (viewModel.uiState.value.isAuthenticated) Screen.Home else Screen.Auth
                    viewModel.navigateTo(dest)
                },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SmokyBlack,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                )
            }
        }

        if (stepIndex < 2) {
            // Steps 0 & 1 (Screens 2 & 3 of attached image):
            // Headline & Subtitle on top, Illustration in center, Dots + Circular Orange Button at bottom
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SmokyBlack)) {
                            append(mainHeadlines[stepIndex])
                        }
                        withStyle(SpanStyle(color = ScanOrange)) {
                            append(accentWords[stepIndex])
                        }
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        lineHeight = 44.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = subtitles[stepIndex],
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    ),
                    color = OliveDrab
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
                contentAlignment = Alignment.Center
            ) {
                OnboardingStepIllustration(stepIndex = stepIndex)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaginationDotsRow(currentStep = stepIndex)

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(ScanOrange)
                        .clickable { stepIndex++ }
                        .testTag("onboarding_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next step",
                        tint = OnScanOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Step 2 (Screen 4 of attached image):
            // Illustration at top/center, Headline & Subtitle below illustration, Centered dots, Full-width "Get Started ->" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                OnboardingStepIllustration(stepIndex = 2)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SmokyBlack)) {
                            append(mainHeadlines[2])
                        }
                        withStyle(SpanStyle(color = ScanOrange)) {
                            append(accentWords[2])
                        }
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        lineHeight = 44.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subtitles[2],
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    ),
                    color = OliveDrab
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PaginationDotsRow(currentStep = 2)

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        val dest = if (viewModel.uiState.value.isAuthenticated) Screen.Home else Screen.Auth
                        viewModel.navigateTo(dest)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
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
                }
            }
        }
    }
}

@Composable
private fun PaginationDotsRow(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..3) {
            val isSelected = i == currentStep
            Box(
                modifier = Modifier
                    .size(if (isSelected) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SmokyBlack else Bone)
            )
        }
    }
}

@Composable
private fun OnboardingStepIllustration(stepIndex: Int) {
    val primaryInk = SmokyBlack
    val accentOrange = ScanOrange
    val surfaceFill = SurfaceCream
    val sageGreen = Color(0xFF9EB384)
    val leafGreen = Color(0xFF6B8E4E)
    val skinTone = Color(0xFFF3D1B0)

    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeMain = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

            // Warm organic circle backdrop
            drawCircle(
                color = surfaceFill,
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.52f)
            )

            when (stepIndex) {
                0 -> {
                    // Screen 2: Woman walking in orange sweater holding phone, flanked by botanical leaves
                    // Left & Right botanical leaves
                    val leftLeaf = Path().apply {
                        moveTo(w * 0.25f, h * 0.80f)
                        quadraticBezierTo(w * 0.12f, h * 0.60f, w * 0.18f, h * 0.42f)
                        quadraticBezierTo(w * 0.28f, h * 0.55f, w * 0.25f, h * 0.80f)
                        close()
                    }
                    drawPath(leftLeaf, color = sageGreen.copy(alpha = 0.55f))
                    drawPath(leftLeaf, color = primaryInk, style = strokeMain)

                    val rightLeaf = Path().apply {
                        moveTo(w * 0.74f, h * 0.80f)
                        quadraticBezierTo(w * 0.88f, h * 0.62f, w * 0.82f, h * 0.44f)
                        quadraticBezierTo(w * 0.70f, h * 0.55f, w * 0.74f, h * 0.80f)
                        close()
                    }
                    drawPath(rightLeaf, color = sageGreen.copy(alpha = 0.55f))
                    drawPath(rightLeaf, color = primaryInk, style = strokeMain)

                    // Hair & Head
                    drawCircle(
                        color = primaryInk,
                        radius = w * 0.08f,
                        center = Offset(w * 0.46f, h * 0.24f)
                    )
                    drawCircle(
                        color = skinTone,
                        radius = w * 0.06f,
                        center = Offset(w * 0.49f, h * 0.25f)
                    )

                    // Orange Sweater Torso
                    val sweater = Path().apply {
                        moveTo(w * 0.36f, h * 0.58f)
                        quadraticBezierTo(w * 0.38f, h * 0.33f, w * 0.45f, h * 0.32f)
                        lineTo(w * 0.53f, h * 0.32f)
                        quadraticBezierTo(w * 0.60f, h * 0.38f, w * 0.58f, h * 0.58f)
                        close()
                    }
                    drawPath(sweater, color = accentOrange)
                    drawPath(sweater, color = primaryInk, style = strokeMain)

                    // Dark trousers/legs
                    drawLine(
                        color = primaryInk,
                        start = Offset(w * 0.43f, h * 0.58f),
                        end = Offset(w * 0.39f, h * 0.82f),
                        strokeWidth = 10.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryInk,
                        start = Offset(w * 0.52f, h * 0.58f),
                        end = Offset(w * 0.56f, h * 0.82f),
                        strokeWidth = 10.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Smartphone held in front
                    drawRoundRect(
                        color = primaryInk,
                        topLeft = Offset(w * 0.62f, h * 0.34f),
                        size = Size(w * 0.08f, h * 0.14f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
                1 -> {
                    // Screen 3: Person in orange shirt holding large magnifying glass over a leaf + two cosmetic bottles on right shelf
                    // Shelf on top-right with 2 bottles
                    drawLine(
                        color = primaryInk,
                        start = Offset(w * 0.58f, h * 0.48f),
                        end = Offset(w * 0.88f, h * 0.48f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Bottle 1 on shelf
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.63f, h * 0.26f),
                        size = Size(w * 0.09f, h * 0.22f),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                    drawRoundRect(
                        color = primaryInk,
                        topLeft = Offset(w * 0.63f, h * 0.26f),
                        size = Size(w * 0.09f, h * 0.22f),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                        style = strokeMain
                    )
                    // Bottle 2 on shelf
                    drawRoundRect(
                        color = accentOrange.copy(alpha = 0.35f),
                        topLeft = Offset(w * 0.75f, h * 0.30f),
                        size = Size(w * 0.08f, h * 0.18f),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                    drawRoundRect(
                        color = primaryInk,
                        topLeft = Offset(w * 0.75f, h * 0.30f),
                        size = Size(w * 0.08f, h * 0.18f),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                        style = strokeMain
                    )

                    // Person on left in orange shirt
                    drawCircle(
                        color = skinTone,
                        radius = w * 0.065f,
                        center = Offset(w * 0.34f, h * 0.34f)
                    )
                    val shirtPath = Path().apply {
                        moveTo(w * 0.20f, h * 0.78f)
                        quadraticBezierTo(w * 0.22f, h * 0.44f, w * 0.30f, h * 0.42f)
                        lineTo(w * 0.40f, h * 0.42f)
                        quadraticBezierTo(w * 0.48f, h * 0.48f, w * 0.46f, h * 0.78f)
                        close()
                    }
                    drawPath(shirtPath, color = accentOrange)
                    drawPath(shirtPath, color = primaryInk, style = strokeMain)

                    // Magnifying glass inspecting green leaf
                    val magCenter = Offset(w * 0.54f, h * 0.56f)
                    drawCircle(color = Color.White, radius = w * 0.14f, center = magCenter)
                    val leafInside = Path().apply {
                        moveTo(magCenter.x - w * 0.05f, magCenter.y + h * 0.06f)
                        quadraticBezierTo(magCenter.x - w * 0.06f, magCenter.y - h * 0.06f, magCenter.x + w * 0.06f, magCenter.y - h * 0.05f)
                        quadraticBezierTo(magCenter.x + w * 0.06f, magCenter.y + h * 0.05f, magCenter.x - w * 0.05f, magCenter.y + h * 0.06f)
                        close()
                    }
                    drawPath(leafInside, color = leafGreen)
                    drawCircle(color = primaryInk, radius = w * 0.14f, center = magCenter, style = Stroke(width = 4.dp.toPx()))
                    drawLine(
                        color = primaryInk,
                        start = Offset(magCenter.x - w * 0.10f, magCenter.y + w * 0.10f),
                        end = Offset(w * 0.36f, h * 0.74f),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                else -> {
                    // Screen 4: Woman in orange sweater sitting on a sage-green hill under a warm half-sun, reading her phone
                    // Warm golden half-sun in upper backdrop
                    drawCircle(
                        color = accentOrange.copy(alpha = 0.28f),
                        radius = w * 0.26f,
                        center = Offset(w * 0.50f, h * 0.36f)
                    )

                    // Sage green hill/cushion at bottom
                    val hillPath = Path().apply {
                        moveTo(w * 0.14f, h * 0.78f)
                        quadraticBezierTo(w * 0.50f, h * 0.52f, w * 0.86f, h * 0.78f)
                        close()
                    }
                    drawPath(hillPath, color = sageGreen)
                    drawPath(hillPath, color = primaryInk, style = strokeMain)

                    // Seated woman in orange sweater
                    drawCircle(
                        color = primaryInk,
                        radius = w * 0.07f,
                        center = Offset(w * 0.47f, h * 0.30f)
                    )
                    drawCircle(
                        color = skinTone,
                        radius = w * 0.055f,
                        center = Offset(w * 0.49f, h * 0.31f)
                    )
                    val seatedTorso = Path().apply {
                        moveTo(w * 0.38f, h * 0.64f)
                        quadraticBezierTo(w * 0.39f, h * 0.40f, w * 0.46f, h * 0.38f)
                        lineTo(w * 0.54f, h * 0.38f)
                        quadraticBezierTo(w * 0.60f, h * 0.44f, w * 0.58f, h * 0.64f)
                        close()
                    }
                    drawPath(seatedTorso, color = accentOrange)
                    drawPath(seatedTorso, color = primaryInk, style = strokeMain)

                    // Crossed legs on hill
                    drawRoundRect(
                        color = primaryInk,
                        topLeft = Offset(w * 0.34f, h * 0.62f),
                        size = Size(w * 0.32f, h * 0.09f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    // Phone held in hands
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.54f, h * 0.44f),
                        size = Size(w * 0.07f, h * 0.11f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = primaryInk,
                        topLeft = Offset(w * 0.54f, h * 0.44f),
                        size = Size(w * 0.07f, h * 0.11f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = strokeMain
                    )
                }
            }
        }
    }
}
