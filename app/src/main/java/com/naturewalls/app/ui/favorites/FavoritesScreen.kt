package com.naturewalls.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naturewalls.app.ui.components.GridWallpaperCard
import com.naturewalls.app.ui.components.StatePanel
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onOpenWallpaper: (Long) -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

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
                    text = "Favourites",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (favorites.isEmpty()) {
                        "Saved wallpapers keep their photographer and licence with them."
                    } else {
                        "${favorites.size} saved · attribution stored for each"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        if (favorites.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StatePanel(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = "Nothing saved yet",
                    message = "Tap the heart on any wallpaper to keep it here. Credits and licence " +
                        "details are stored with it."
                )
            }
        } else {
            items(items = favorites, key = { it.id }) { wallpaper ->
                GridWallpaperCard(
                    wallpaper = wallpaper,
                    onClick = { onOpenWallpaper(wallpaper.id) }
                )
            }
        }
    }
}
