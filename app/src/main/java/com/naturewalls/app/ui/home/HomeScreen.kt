package com.naturewalls.app.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naturewalls.app.ui.components.HairlineDivider
import com.naturewalls.app.ui.components.HeroWallpaperCard
import com.naturewalls.app.ui.components.InlineError
import com.naturewalls.app.ui.components.LiveBadge
import com.naturewalls.app.ui.components.NatureLogo
import com.naturewalls.app.ui.components.NatureSearchField
import com.naturewalls.app.ui.components.ShimmerBox
import com.naturewalls.app.ui.components.WallpaperRail
import com.naturewalls.app.ui.theme.ForestGreen
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary
import com.naturewalls.app.util.TimeFormat

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenWallpaper: (Long) -> Unit,
    onOpenCategory: (String, String?) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCredits: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshIfStale()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack),
        contentPadding = PaddingValues(bottom = 128.dp)
    ) {
        item(key = "header") {
            HomeHeader(
                fetchedAt = state.fetchedAt,
                refreshing = state.refreshing,
                onRefresh = { viewModel.load(force = true) },
                onOpenCredits = onOpenCredits
            )
        }

        item(key = "search") {
            NatureSearchField(
                value = "",
                onValueChange = {},
                onSearch = onOpenSearch,
                readOnly = true,
                onClick = onOpenSearch,
                modifier = Modifier.padding(horizontal = 20.dp),
                placeholder = "Search forests, mountains, waterfalls…"
            )
        }

        item(key = "daily") {
            val daily = state.daily
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                when {
                    daily != null -> HeroWallpaperCard(
                        wallpaper = daily,
                        onClick = { onOpenWallpaper(daily.id) }
                    )

                    state.dailyLoading -> ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(430.dp),
                        shape = RoundedCornerShape(26.dp)
                    )

                    else -> InlineError(
                        message = state.dailyError ?: "The daily wallpaper is unavailable right now.",
                        onRetry = { viewModel.load(force = true) }
                    )
                }
            }
        }

        item(key = "featured") {
            Spacer(Modifier.height(34.dp))
            WallpaperRail(
                title = "Curated nature",
                subtitle = "Commons' quality nature collection · live",
                items = state.featured,
                loading = state.featuredLoading,
                error = state.featuredError,
                onRetry = { viewModel.load(force = true) },
                onItemClick = { onOpenWallpaper(it.id) },
                actionLabel = "Credits",
                onAction = onOpenCredits
            )
        }

        item(key = "trending") {
            Spacer(Modifier.height(34.dp))
            WallpaperRail(
                title = "Trending landscapes",
                subtitle = "A rotating set of searches, refreshed daily",
                items = state.trending,
                loading = state.trendingLoading,
                error = state.trendingError,
                onRetry = { viewModel.load(force = true) },
                onItemClick = { onOpenWallpaper(it.id) }
            )
        }

        items(items = state.collections, key = { it.group.id }) { shelf ->
            Spacer(Modifier.height(34.dp))
            WallpaperRail(
                title = shelf.group.name,
                subtitle = shelf.group.tagline,
                items = shelf.items,
                loading = shelf.loading,
                error = shelf.error,
                onRetry = { viewModel.load(force = true) },
                onItemClick = { onOpenWallpaper(it.id) },
                actionLabel = "See all",
                onAction = { onOpenCategory(shelf.group.id, null) }
            )
        }

        item(key = "recently-added") {
            Spacer(Modifier.height(34.dp))
            WallpaperRail(
                title = "Recently added",
                subtitle = "Newest uploads in the quality nature collection",
                items = state.recentlyAdded,
                loading = state.recentLoading,
                error = state.recentError,
                onRetry = { viewModel.load(force = true) },
                onItemClick = { onOpenWallpaper(it.id) }
            )
        }

        if (state.recentViews.isNotEmpty()) {
            item(key = "recently-viewed") {
                Spacer(Modifier.height(34.dp))
                WallpaperRail(
                    title = "Recently viewed",
                    subtitle = "Your last wallpapers on this device",
                    items = state.recentViews,
                    onItemClick = { onOpenWallpaper(it.id) }
                )
            }
        }

        item(key = "footer") {
            Spacer(Modifier.height(40.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                HairlineDivider()
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ForestGreen)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "LIVE SOURCE · WIKIMEDIA COMMONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreenSoft
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Every wallpaper in NatureWalls is fetched from the Wikimedia Commons API " +
                        "each time you open the app — no bundled catalogue, no stock filler. Only CC0, " +
                        "public domain, CC BY and CC BY-SA photographs are shown, each with its " +
                        "photographer, licence and source page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Open full credits & licences →",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreenSoft,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenCredits)
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    fetchedAt: Long,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenCredits: () -> Unit
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NatureLogo(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.055f))
                    .clickable(onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        color = ForestGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh live feed",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveBadge(fetchedAt = fetchedAt)
            Spacer(Modifier.size(10.dp))
            Text(
                text = TimeFormat.todayLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onOpenCredits)
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = "Credits",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenCredits)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Peaceful places,\none screen at a time.",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
    }
}
