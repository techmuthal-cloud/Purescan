package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.BotanicalTopAppBar
import com.example.ui.components.EmptyBotanicalState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.OliveDrab
import com.example.ui.theme.OliveDrabMuted
import com.example.ui.theme.SemanticCaution
import com.example.ui.theme.SemanticCautionBg
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticSuccessBg
import com.example.ui.theme.SmokyBlack
import com.example.ui.theme.SurfaceCream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedWatchlistScreen(viewModel: ScanViewModel) {
    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val savedScans by viewModel.savedScans.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloralWhite)
            .testTag("saved_watchlist_screen")
    ) {
        BotanicalTopAppBar(
            title = "Saved Watchlist",
            showBackButton = false
        )

        if (savedScans.isEmpty()) {
            EmptyBotanicalState(
                icon = Icons.Default.Bookmark,
                title = "Your Watchlist is empty",
                message = "Save important products to monitor formulation findings and easily re-inspect their scientific evidence.",
                actionLabel = "Browse Recent Scans",
                onActionClick = { viewModel.navigateTo(Screen.History) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Saved Products (${savedScans.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = SmokyBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bookmarked products for rapid retrieval and comparison.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveDrabMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(savedScans, key = { it.id }) { scan ->
                    val formattedDate = remember(scan.createdAt) { dateFormat.format(Date(scan.createdAt)) }
                    val (statusBg, statusFg, statusLabel) = when (scan.summaryStatus) {
                        "CONCERNS_IDENTIFIED" -> Triple(SemanticCautionBg, SemanticCaution, "Concerns Noted")
                        "COMPLETED_WITH_UNKNOWNS" -> Triple(SurfaceCream, OliveDrab, "With Unknowns")
                        else -> Triple(SemanticSuccessBg, SemanticSuccess, "Clear Evidence")
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.openReport(scan.id) }
                            .testTag("watchlist_item_${scan.id}"),
                        color = CardSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusFg,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = scan.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OliveDrabMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = scan.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SmokyBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Saved on $formattedDate • ${scan.matchedCount} ingredients",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OliveDrab
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleSavedScan(scan.id, scan.isSaved) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Remove from watchlist",
                                        tint = SmokyBlack
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = OliveDrabMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
