package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Bone
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticInfo
import com.example.ui.theme.SemanticInfoBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

@Composable
fun SafetyLevelMeter(
    score: Int, // 0 - 100
    tier: String, // "SAFE", "WARNING", "HIGH_RISK"
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val (tierColor, tierBg, tierLabel) = when (tier.uppercase()) {
        "SAFE" -> Triple(SemanticSuccess, SemanticSuccessBg, "Safe Formulation")
        "WARNING" -> Triple(SemanticCaution, SemanticCautionBg, "Caution Advised")
        else -> Triple(SemanticCritical, SemanticCriticalBg, "High Risk Concerns")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .testTag("safety_level_meter"),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Arc progress gauge
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    // Track background
                    drawArc(
                        color = Color(0xFFEBE6DC),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Active progress
                    drawArc(
                        color = tierColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SmokyBlack
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Explanation
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tierBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tierLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (tier.uppercase()) {
                        "SAFE" -> "Low toxicity risk. Matched items meet FDA & EU CIR safe cosmetic criteria."
                        "WARNING" -> "Contains ingredients with strict concentration limits or potential sensitizers."
                        else -> "Contains flagged substances with known regulatory bans or endocrine alerts."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SmokyBlack
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllergenWarningBar(
    allergens: List<String>,
    modifier: Modifier = Modifier,
    hasHighRiskAllergen: Boolean = false
) {
    if (allergens.isEmpty()) return

    val accentColor = if (hasHighRiskAllergen) SemanticCritical else SemanticCaution
    val bgColor = if (hasHighRiskAllergen) SemanticCriticalBg else SemanticCautionBg

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(14.dp)
            .testTag("allergen_warning_bar")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasHighRiskAllergen) {
                    "Personal Allergy Filter Triggered — High Risk (${allergens.size})"
                } else {
                    "Allergen & Sensitizer Alert (${allergens.size})"
                },
                style = MaterialTheme.typography.titleSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (hasHighRiskAllergen) {
                "Matches your Personal Allergy Filter. Safety score escalated to High Risk and matched ingredients are highlighted below."
            } else {
                "Flagged by your Allergy Filter or EU 26 / FDA sensitizer rules."
            },
            style = MaterialTheme.typography.bodySmall,
            color = SmokyBlack.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allergens.forEach { allergen ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = allergen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SmokyBlack
                        )
                    }
                }
            }
        }
    }
}
