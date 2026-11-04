package com.naturewalls.app.ui.browse

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naturewalls.app.ui.components.CategoryPill
import com.naturewalls.app.ui.components.GridWallpaperCard
import com.naturewalls.app.ui.components.InlineError
import com.naturewalls.app.ui.components.LiveBadge
import com.naturewalls.app.ui.components.LoadingMore
import com.naturewalls.app.ui.components.ShimmerBox
import com.naturewalls.app.ui.components.StatePanel
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary

@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onBack: () -> Unit,
    onOpenWallpaper: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedSubId by viewModel.selectedSubId.collectAsStateWithLifecycle()
    val group = viewModel.group

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.055f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group?.name ?: "Nature",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = group?.tagline ?: "Live from Wikimedia Commons",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    LiveBadge(fetchedAt = state.fetchedAt)
                }

                Spacer(Modifier.height(18.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = group?.subCategories.orEmpty(), key = { it.id }) { sub ->
                        CategoryPill(
                            text = sub.name,
                            selected = sub.id == selectedSubId,
                            onClick = { viewModel.selectSubCategory(sub) }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Pull a new search for every sub-category — results come straight " +
                        "from Commons, filtered to freely licensed photographs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )

                Spacer(Modifier.height(16.dp))
            }
        }

        if (state.loading) {
            items(6) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(18.dp)
                )
            }
        } else if (state.items.isEmpty() && state.error != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StatePanel(
                    icon = Icons.Rounded.CloudOff,
                    title = "Commons did not answer",
                    message = state.error ?: "Unknown error",
                    actionLabel = "Try again",
                    onAction = { viewModel.reload() }
                )
            }
        } else if (state.items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StatePanel(
                    icon = Icons.Rounded.CloudOff,
                    title = "No photographs matched",
                    message = "Try another sub-category — the licence filter keeps this shelf strict.",
                    actionLabel = "Reload",
                    onAction = { viewModel.reload() }
                )
            }
        } else {
            items(items = state.items, key = { it.id }) { wallpaper ->
                GridWallpaperCard(
                    wallpaper = wallpaper,
                    onClick = { onOpenWallpaper(wallpaper.id) }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.error != null && state.items.isNotEmpty()) {
                    InlineError(
                        message = state.error ?: "",
                        onRetry = { viewModel.loadMore() }
                    )
                }
                if (!state.endReached && state.items.isNotEmpty()) {
                    LaunchedEffect(state.items.size) { viewModel.loadMore() }
                    LoadingMore()
                } else if (state.endReached && state.items.isNotEmpty()) {
                    Text(
                        text = "END OF THIS LIVE SHELF · ${state.items.size} photographs",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreenSoft,
                        modifier = Modifier.padding(vertical = 22.dp)
                    )
                }
            }
        }
    }
}
