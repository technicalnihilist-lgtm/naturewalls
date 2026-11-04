package com.naturewalls.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naturewalls.app.ui.components.CategoryPill
import com.naturewalls.app.ui.components.GridWallpaperCard
import com.naturewalls.app.ui.components.InlineError
import com.naturewalls.app.ui.components.LiveBadge
import com.naturewalls.app.ui.components.LoadingMore
import com.naturewalls.app.ui.components.NatureSearchField
import com.naturewalls.app.ui.components.ShimmerBox
import com.naturewalls.app.ui.components.StatePanel
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenWallpaper: (Long) -> Unit
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = "Search nature",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ask Commons for a subject — forests, peaks, water, bloom, weather.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                NatureSearchField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    onSearch = { viewModel.submit() },
                    modifier = Modifier.focusRequester(focusRequester),
                    placeholder = "e.g. misty pine forest"
                )
                Spacer(Modifier.height(14.dp))

                if (state.items.isNotEmpty() || state.loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiveBadge(fetchedAt = state.fetchedAt)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${state.items.size} photographs",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary
                        )
                    }
                } else {
                    Text(
                        text = "SUGGESTED SUBJECTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryPill(
                            text = SearchViewModel.SUGGESTED_SUBJECTS[0],
                            selected = false,
                            onClick = { viewModel.submit(SearchViewModel.SUGGESTED_SUBJECTS[0]) }
                        )
                        CategoryPill(
                            text = SearchViewModel.SUGGESTED_SUBJECTS[1],
                            selected = false,
                            onClick = { viewModel.submit(SearchViewModel.SUGGESTED_SUBJECTS[1]) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryPill(
                            text = SearchViewModel.SUGGESTED_SUBJECTS[2],
                            selected = false,
                            onClick = { viewModel.submit(SearchViewModel.SUGGESTED_SUBJECTS[2]) }
                        )
                        CategoryPill(
                            text = SearchViewModel.SUGGESTED_SUBJECTS[3],
                            selected = false,
                            onClick = { viewModel.submit(SearchViewModel.SUGGESTED_SUBJECTS[3]) }
                        )
                    }

                    if (history.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT SEARCHES",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelMedium,
                                color = ForestGreenSoft,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { viewModel.clearHistory() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Column {
                            history.forEach { entry ->
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.submit(entry) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
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
                    icon = Icons.Rounded.Search,
                    title = "Search failed",
                    message = state.error ?: "Unknown error",
                    actionLabel = "Retry",
                    onAction = { viewModel.submit() }
                )
            }
        } else if (state.items.isEmpty() && query.length >= 2) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StatePanel(
                    icon = Icons.Rounded.Search,
                    title = "No freely licensed match",
                    message = "Nothing on Commons matched \"$query\" with an accepted licence. " +
                        "Try a broader subject such as \"forest\" or \"waterfall\".",
                    actionLabel = "Clear",
                    onAction = {
                        viewModel.clearResults()
                        viewModel.onQueryChange("")
                    }
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
                    InlineError(message = state.error ?: "", onRetry = { viewModel.loadMore() })
                }
                if (!state.endReached && state.items.isNotEmpty()) {
                    LaunchedEffect(state.items.size) { viewModel.loadMore() }
                    LoadingMore(label = "Fetching more results…")
                }
            }
        }
    }
}
