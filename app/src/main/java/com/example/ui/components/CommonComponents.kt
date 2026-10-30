package com.example.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FindingType
import com.example.data.model.MatchStatus
import com.example.data.model.ParsedIngredient
import com.example.data.model.ReportFinding
import com.example.data.model.Severity
import com.example.ui.Screen
import com.example.ui.theme.Bone
import com.example.ui.theme.BoneLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.OnScanOrange
import com.example.ui.theme.ScanOrange
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticCritical
import com.example.ui.theme.SemanticCriticalBg
import com.example.ui.theme.SemanticInfo
import com.example.ui.theme.SemanticInfoBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticUnknownBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream

@Composable
fun Purescan3DEmblem(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 10.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF11120D))
            .border(1.dp, ScanOrange.copy(alpha = 0.65f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_purescan_logo),
            contentDescription = "Purescan 3D Checkmark Leaf Emblem",
            modifier = Modifier.size(size),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@Composable
fun PurescanBrandWordmark(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    primaryTextColor: Color = SmokyBlack
) {
    Text(
        text = androidx.compose.ui.text.buildAnnotatedString {
            append(
                androidx.compose.ui.text.AnnotatedString(
                    text = "PURE",
                    spanStyle = androidx.compose.ui.text.SpanStyle(color = primaryTextColor)
                )
            )
            append(
                androidx.compose.ui.text.AnnotatedString(
                    text = "SCAN",
                    spanStyle = androidx.compose.ui.text.SpanStyle(color = ScanOrange)
                )
            )
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = 1.sp
        ),
        modifier = modifier
    )
}

@Composable
fun BotanicalTopAppBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onActionClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FloralWhite,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("back_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SmokyBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Purescan3DEmblem(
                        size = 34.dp,
                        cornerRadius = 9.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = SmokyBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (actionIcon != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("app_bar_action_button")
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionDescription ?: "Action",
                        tint = SmokyBlack
                    )
                }
            }
        }
    }
}

@Composable
fun FindingCard(
    finding: ReportFinding,
    onClick: () -> Unit
) {
    val (badgeBg, badgeFg, icon) = when (finding.severity) {
        Severity.CRITICAL -> Triple(SemanticCriticalBg, SemanticCritical, Icons.Default.Warning)
        Severity.CAUTION -> Triple(SemanticCautionBg, SemanticCaution, Icons.Default.Warning)
        Severity.INFO -> Triple(SemanticInfoBg, SemanticInfo, Icons.Default.Info)
    }

    val typeLabel = when (finding.findingType) {
        FindingType.REGULATORY_RESTRICTION -> "Regulatory Restriction"
        FindingType.HAZARD_EVIDENCE -> "Hazard Evidence"
        FindingType.ALLERGEN_ALERT -> "Allergen Alert"
        FindingType.PREFERENCE_MISMATCH -> "Preference Note"
        FindingType.BENIGN_EVIDENCE -> "Approved Record"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("finding_card_${finding.id}"),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Type & severity badge (Paired icon + label per Design.md)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeFg,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Authority & Jurisdiction tag
                Text(
                    text = "${finding.sourceOrg} • ${finding.jurisdiction}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveDrabMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = finding.ingredientName,
                style = MaterialTheme.typography.titleMedium,
                color = SmokyBlack
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = finding.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = OliveDrab,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (finding.conditions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceCream)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Condition: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SmokyBlack
                    )
                    Text(
                        text = finding.conditions,
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDrab,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evidence: ${finding.evidenceStrength} • ${finding.reviewDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveDrabMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Sources",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SmokyBlack
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SmokyBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IngredientChip(
    ingredient: ParsedIngredient,
    onClick: () -> Unit,
    categorizedEntry: CategorizedIngredientEntry? = null,
    onBadgeClick: ((IngredientSafetyBadgeSpec) -> Unit)? = null
) {
    val badgeSpec = androidx.compose.runtime.remember(ingredient, categorizedEntry) {
        ingredient.toSafetyBadgeSpec(categorizedEntry)
    }
    val sev = badgeSpec.severity
    val hasAllergen = ingredient.matchedAllergenName != null || categorizedEntry?.matchedAllergenName != null
    val borderColor = when (sev) {
        TrafficLightBadgeSeverity.RED_HIGH_HAZARD -> SemanticCritical
        TrafficLightBadgeSeverity.YELLOW_CAUTION, TrafficLightBadgeSeverity.YELLOW_UNVERIFIED -> SemanticCaution
        TrafficLightBadgeSeverity.GREEN_SAFE -> CardBorder
    }
    val bgColor = when (sev) {
        TrafficLightBadgeSeverity.RED_HIGH_HAZARD -> SemanticCriticalBg.copy(alpha = 0.55f)
        TrafficLightBadgeSeverity.YELLOW_CAUTION, TrafficLightBadgeSeverity.YELLOW_UNVERIFIED -> SemanticCautionBg.copy(alpha = 0.55f)
        TrafficLightBadgeSeverity.GREEN_SAFE -> CardSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                if (hasAllergen || sev != TrafficLightBadgeSeverity.GREEN_SAFE) 1.5.dp else 1.dp,
                borderColor,
                RoundedCornerShape(10.dp)
            )
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("ingredient_chip_${ingredient.rawText}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ingredient.canonicalIngredient?.canonicalName ?: ingredient.rawText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (hasAllergen || sev == TrafficLightBadgeSeverity.RED_HIGH_HAZARD) FontWeight.Bold else FontWeight.Medium,
                color = SmokyBlack
            )
            Spacer(modifier = Modifier.width(6.dp))
            IngredientSafetyBadge(
                spec = badgeSpec,
                compact = true,
                onClick = if (onBadgeClick != null) {
                    { onBadgeClick(badgeSpec) }
                } else {
                    onClick
                }
            )
        }
    }
}

@Composable
fun ScanSafeBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val destinations = listOf(
        Triple(Screen.Home, "Home", Icons.Default.Home),
        Triple(Screen.History, "History", Icons.Default.History),
        Triple(Screen.Scanner, "Scan", Icons.Default.QrCodeScanner),
        Triple(Screen.Saved, "Lists", Icons.Default.Bookmark),
        Triple(Screen.Profile, "Profile", Icons.Default.Person)
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder)
            .testTag("bottom_navigation_bar"),
        containerColor = FloralWhite,
        tonalElevation = 0.dp
    ) {
        destinations.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            val isScanner = screen == Screen.Scanner

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    if (isScanner) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(ScanOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = OnScanOrange,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = if (isScanner) null else {
                    {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ScanOrange,
                    selectedTextColor = ScanOrange,
                    unselectedIconColor = OliveDrabMuted,
                    unselectedTextColor = OliveDrabMuted,
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}

@Composable
fun EmptyBotanicalState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceCream)
                .border(1.dp, CardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ScanOrange,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = SmokyBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = OliveDrabMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(ScanOrange)
                    .border(1.dp, SmokyBlack, RoundedCornerShape(24.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("empty_state_action_button"),
                color = ScanOrange
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnScanOrange
                )
            }
        }
    }
}

/**
 * Reusable warm cream and bone shimmer brush matching the ScanSafe.AI / PureScan palette.
 */
@Composable
fun rememberWarmBoneShimmerBrush(): androidx.compose.ui.graphics.Brush {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "skeleton_shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 900f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1150,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "skeleton_shimmer_offset"
    )

    val shimmerColors = listOf(
        SurfaceCream,
        BoneLight.copy(alpha = 0.85f),
        Bone.copy(alpha = 0.65f),
        BoneLight.copy(alpha = 0.85f),
        SurfaceCream
    )

    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim.value - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim.value, 260f)
    )
}

/**
 * Reusable SkeletonCard component for Home and History screens while data is loading.
 * Matches existing card shapes, warm cream surfaces, and 1dp borders so the app remains responsive.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 116.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    showLeadingCircle: Boolean = true,
    showBottomPills: Boolean = true,
    testTag: String = "skeleton_card"
) {
    val shimmerBrush = rememberWarmBoneShimmerBrush()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(cornerRadius))
            .testTag(testTag),
        color = CardSurface,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showLeadingCircle) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.46f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerBrush)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(shimmerBrush)
                )
            }

            if (showBottomPills) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Home skeleton layout matching the exact geometry of Screen 5 (2 side-by-side hero cards,
 * 4 quick-action tiles, and bottom editorial banner card) while initial data is loading.
 */
@Composable
fun HomeSkeletonLoadingSection(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberWarmBoneShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_skeleton_section")
    ) {
        // Two Equal-Width Side-by-Side Hero Skeleton Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(2) { idx ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(198.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .testTag("home_hero_skeleton_$idx"),
                    color = SurfaceCream,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(shimmerBrush)
                        )
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(shimmerBrush)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Row of 4 Quick-Action Tile Skeletons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(4) { idx ->
                SkeletonCard(
                    modifier = Modifier.weight(1f),
                    height = 84.dp,
                    cornerRadius = 16.dp,
                    showLeadingCircle = false,
                    showBottomPills = false,
                    testTag = "home_quick_skeleton_$idx"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Promotional Editorial Banner Skeleton Card
        SkeletonCard(
            height = 138.dp,
            cornerRadius = 22.dp,
            showLeadingCircle = true,
            showBottomPills = true,
            testTag = "home_banner_skeleton"
        )
    }
}

