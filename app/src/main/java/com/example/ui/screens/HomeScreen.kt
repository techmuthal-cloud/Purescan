package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.SampleLabel
import com.example.data.local.ScanEntity
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
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen 5: Minimalist Home Screen
 * Strictly matches Screen 5 in the attached 5-screen starting UI image:
 * 1. Top Bar: Hamburger menu icon on left, orange two-leaf mark + "ScanSafe" serif title in center, bell notification icon on right
 * 2. Search Bar: Rounded cream pill ("Search products or ingredients...")
 * 3. Two Equal-Width Side-by-Side Hero Cards:
 *    - Left (ScanOrange): Camera icon, "Scan a Product", "Tap to scan barcode or\ningredient label", white circle with orange -> arrow
 *    - Right (Warm Cream): Search icon, "Search", "Find products\nand ingredients", white circle with dark -> arrow
 * 4. Single Horizontal Row of 4 Minimalist Quick-Action Cards:
 *    - History | My Lists | Safety Tips | Settings
 * 5. Promotional Editorial Banner Card:
 *    - Illustration on left (woman in orange sweater holding product bottle & phone with botanical leaves)
 *    - Bold serif copy on right: "Small Habits\nSafer Choices\nBrighter Tomorrow." + -> arrow
 */
@Composable
fun HomeScreen(viewModel: ScanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSafetyTipsDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .padding(horizontal = 20.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
    ) {
        // 1. Minimalist App Header (Hamburger on Left, Leaf + Brand Title in Center, Bell on Right)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Preferences) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = SmokyBlack
                    )
                }

                // Centered 3D Glossy Checkmark-Leaf Emblem + PURESCAN Wordmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showThemeSelectorDialog = true }
                        .testTag("home_brand_header")
                ) {
                    com.example.ui.components.Purescan3DEmblem(
                        size = 32.dp,
                        cornerRadius = 9.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    com.example.ui.components.PurescanBrandWordmark(
                        fontSize = 22.sp,
                        primaryTextColor = SmokyBlack
                    )
                }

                IconButton(
                    onClick = { showThemeSelectorDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications & Appearance",
                        tint = SmokyBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Minimalist Search Pill Bar (matching Screen 5 of attached image)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 2) {
                        viewModel.navigateTo(Screen.Glossary)
                    }
                },
                placeholder = {
                    Text(
                        text = "Search products or ingredients...",
                        color = OliveDrabMuted,
                        fontSize = 14.5.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OliveDrab
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CardBorder,
                    unfocusedBorderColor = CardBorder.copy(alpha = 0.6f),
                    focusedContainerColor = SurfaceCream,
                    unfocusedContainerColor = SurfaceCream,
                    focusedTextColor = SmokyBlack,
                    unfocusedTextColor = SmokyBlack
                ),
                shape = RoundedCornerShape(26.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        if (uiState.isDataLoading) {
            item {
                com.example.ui.components.HomeSkeletonLoadingSection()
            }
        } else {
        // 3. Two Equal-Width Side-by-Side Hero Cards ("Scan a Product" & "Search")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Card: Solid ScanOrange "Scan a Product"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(198.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewModel.navigateTo(Screen.Scanner) }
                        .testTag("scan_hero_card"),
                    color = ScanOrange,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = OnScanOrange,
                            modifier = Modifier.size(28.dp)
                        )

                        Column {
                            Text(
                                text = "Scan a Product",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = OnScanOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to scan barcode or\ningredient label",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = OnScanOrange.copy(alpha = 0.85f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Scan",
                                    tint = ScanOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = OnScanOrange.copy(alpha = 0.16f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.startBatchScanFromHome() }
                                    .testTag("home_batch_scan_chip")
                            ) {
                                Text(
                                    text = "Batch Scan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnScanOrange,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Right Card: Soft Warm Cream "Search"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(198.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .clickable { viewModel.navigateTo(Screen.Glossary) }
                        .testTag("home_glossary_card"),
                    color = SurfaceCream,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SmokyBlack,
                            modifier = Modifier.size(28.dp)
                        )

                        Column {
                            Text(
                                text = "Search",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = SmokyBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Find products\nand ingredients",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = OliveDrab
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Search",
                                tint = SmokyBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. Single Horizontal Row of 4 Minimalist Quick-Action Cards (History, My Lists, Safety Tips, Settings)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "History",
                    icon = Icons.Default.History,
                    testTag = "quick_action_scan_history",
                    onClick = { viewModel.navigateTo(Screen.History) }
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "My Lists",
                    icon = Icons.Default.Bookmark,
                    testTag = "quick_action_my_lists",
                    onClick = { viewModel.navigateTo(Screen.Saved) }
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "Safety Tips",
                    icon = Icons.Default.Lightbulb,
                    testTag = "quick_action_safety_tips",
                    onClick = { showSafetyTipsDialog = true }
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    testTag = "quick_action_settings",
                    onClick = { viewModel.navigateTo(Screen.Preferences) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. Promotional Editorial Banner Card ("Small Habits / Safer Choices / Brighter Tomorrow.")
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .clickable { viewModel.navigateTo(Screen.Glossary) },
                color = SurfaceCream,
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Editorial Illustration (Person in orange sweater scanning bottle + leaves)
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HomeBannerEditorialIllustration()
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Right Copy & Arrow
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Small Habits\nSafer Choices\nBrighter Tomorrow.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                lineHeight = 22.sp
                            ),
                            color = SmokyBlack
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SmokyBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        }
    }

    // Theme & Outdoor Readability Selector Modal Dialog
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
                    Text(
                        text = "Choose your preferred color theme or enable High-Contrast Outdoor Readability for bright shop and daylight environments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )

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
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .testTag("theme_option_${mode.name.lowercase()}"),
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

                    // Outdoor High-Contrast Readability Toggle
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
                                ),
                                modifier = Modifier.testTag("outdoor_mode_switch")
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

    if (showSafetyTipsDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyTipsDialog = false },
            containerColor = FloralWhite,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ScanOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safety Tips & Guidelines",
                        style = MaterialTheme.typography.titleLarge,
                        color = SmokyBlack
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Predominance Order: Ingredients are listed in descending order by concentration. The first 5 ingredients typically constitute 80%+ of the formula.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Text(
                        text = "2. Hidden Fragrance Allergens: Terms like 'Parfum' or 'Fragrance' may mask dozens of sensitizers such as Limonene, Linalool, or Eugenol.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Text(
                        text = "3. Peer-Reviewed Panels: We evaluate hazard claims against CIR (Cosmetic Ingredient Review) and EU SCCS scientific consensus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                    Text(
                        text = "4. Regional Harmonization: Ingredients restricted in the EU may still be permitted under FDA or CDSCO regulations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrab
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafetyTipsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanOrange,
                        contentColor = OnScanOrange
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun HomeBannerEditorialIllustration() {
    val primaryInk = SmokyBlack
    val sweaterOrange = Color(0xFFF5A623)
    val sageGreen = Color(0xFF8FA876)
    val skinTone = Color(0xFFF3D1B0)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Botanical leaf on left
        val leafPath = Path().apply {
            moveTo(w * 0.24f, h * 0.88f)
            quadraticBezierTo(w * 0.05f, h * 0.62f, w * 0.14f, h * 0.35f)
            quadraticBezierTo(w * 0.28f, h * 0.50f, w * 0.24f, h * 0.88f)
            close()
        }
        drawPath(leafPath, color = sageGreen.copy(alpha = 0.65f))
        drawPath(leafPath, color = primaryInk, style = stroke)

        // Person head & hair
        drawCircle(
            color = primaryInk,
            radius = w * 0.11f,
            center = Offset(w * 0.45f, h * 0.26f)
        )
        drawCircle(
            color = skinTone,
            radius = w * 0.085f,
            center = Offset(w * 0.47f, h * 0.28f)
        )

        // Orange sweater torso
        val torso = Path().apply {
            moveTo(w * 0.28f, h * 0.90f)
            quadraticBezierTo(w * 0.30f, h * 0.44f, w * 0.40f, h * 0.40f)
            lineTo(w * 0.54f, h * 0.40f)
            quadraticBezierTo(w * 0.64f, h * 0.46f, w * 0.64f, h * 0.90f)
            close()
        }
        drawPath(torso, color = sweaterOrange)
        drawPath(torso, color = primaryInk, style = stroke)

        // White cosmetic bottle on right
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.72f, h * 0.38f),
            size = Size(w * 0.18f, h * 0.44f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        drawRoundRect(
            color = primaryInk,
            topLeft = Offset(w * 0.72f, h * 0.38f),
            size = Size(w * 0.18f, h * 0.44f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = stroke
        )
    }
}

private val BrandScanOrangeRef = Color(0xFFF5A623)

@Composable
fun QuickActionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    testTag: String = "quick_action_${title.lowercase().replace(" ", "_")}",
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = CardSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SurfaceCream),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = SmokyBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = SmokyBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SampleCard(
    sample: SampleLabel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sample_card_${sample.id}"),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = sample.name,
                style = MaterialTheme.typography.titleSmall,
                color = SmokyBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentScanRow(
    scan: ScanEntity,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(scan.createdAt) { dateFormat.format(Date(scan.createdAt)) }

    val (statusBg, statusFg, statusLabel) = when (scan.summaryStatus) {
        "CONCERNS_IDENTIFIED" -> Triple(SemanticCautionBg, SemanticCaution, "Use with Caution")
        "COMPLETED_WITH_UNKNOWNS" -> Triple(SurfaceCream, OliveDrab, "With Unknowns")
        else -> Triple(SemanticSuccessBg, SemanticSuccess, "Safe")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("recent_scan_row_${scan.id}"),
        color = CardSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SmokyBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusFg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$formattedDate • ${scan.matchedCount} ingredients",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDrabMuted
                    )
                }
            }

            IconButton(onClick = onToggleSaved) {
                Icon(
                    imageVector = if (scan.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (scan.isSaved) "Remove from saved" else "Save scan",
                    tint = if (scan.isSaved) ScanOrange else OliveDrabMuted
                )
            }
        }
    }
}
