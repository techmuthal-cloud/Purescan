package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

/**
 * Screens 8 & 10 of the 15-screen ScanSafe UI/UX Reference:
 * - Circular ScanOrange progress ring at top center with percentage readout
 * - "Analyzing Ingredients..." bold headline + subtitle
 * - 6-step vertical progress checklist with green checkmarks for completed steps and orange active indicator
 * - Bottom privacy shield reassurance card
 */
@Composable
fun AnalysisProgressScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.analysisStep.coerceIn(1, 6)

    val steps = listOf(
        "Reading text from image",
        "Extracting ingredients",
        "Checking safety data",
        "Cross-referencing databases",
        "Evaluating health impact",
        "Generating final report"
    )

    val progressFraction = ((currentStep.toFloat() / steps.size.toFloat()) * 0.92f).coerceIn(0.18f, 0.95f)
    val percentage = (progressFraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("analysis_progress_screen"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier.testTag("progress_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancel",
                    tint = SmokyBlack
                )
            }
        }

        // Center Content: Circular Progress Ring + Title + 6-Step Checklist (Screens 8 & 10)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(116.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = SurfaceCream
                val arcColor = ScanOrange
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 9.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeW)
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = SmokyBlack
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (uiState.batchTotalCount > 1) {
                    "Analyzing Batch (${uiState.batchProgressIndex} of ${uiState.batchTotalCount})..."
                } else {
                    "Analyzing Ingredients..."
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = SmokyBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (uiState.batchTotalCount > 1) {
                    "Evaluating collective safety verdicts & personal Allergy Filter across ${uiState.batchTotalCount} labels"
                } else {
                    "Please wait while we evaluate the product formula"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OliveDrab,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                color = CardSurface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    steps.forEachIndexed { index, stepText ->
                        // Map the 4 ViewModel steps across the 6 visual checklist rows
                        val mappedActiveIndex = when (currentStep) {
                            1 -> 0
                            2 -> 1
                            3 -> 3
                            else -> 4
                        }
                        val isCompleted = index < mappedActiveIndex
                        val isCurrent = index == mappedActiveIndex

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> SemanticSuccess
                                            isCurrent -> ScanOrange.copy(alpha = 0.18f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = when {
                                            isCompleted -> SemanticSuccess
                                            isCurrent -> ScanOrange
                                            else -> CardBorder
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                } else if (isCurrent) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = ScanOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCompleted || isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 15.sp
                                ),
                                color = when {
                                    isCompleted || isCurrent -> SmokyBlack
                                    else -> OliveDrabMuted
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Privacy Reassurance Pill (Screen 10)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            color = SurfaceCream
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OliveDrab,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your scan is private and processed securely",
                    style = MaterialTheme.typography.labelMedium,
                    color = SmokyBlack
                )
            }
        }
    }
}
