package com.naturewalls.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.naturewalls.app.data.model.DownloadedWallpaper
import com.naturewalls.app.ui.components.StatePanel
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.SurfaceRaised
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary
import com.naturewalls.app.util.TimeFormat

/** Downloads: real files saved on the device (and in the gallery), listed with their credits. */
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onOpenWallpaper: (Long) -> Unit
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Originals saved to this device and to Pictures/NatureWalls.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(18.dp))
        }

        if (downloads.isEmpty()) {
            item {
                StatePanel(
                    icon = Icons.Rounded.DownloadDone,
                    title = "No downloads yet",
                    message = "Use Download on any wallpaper to keep the full-resolution original. " +
                        "You can set it as wallpaper later without fetching again."
                )
            }
        } else {
            items(items = downloads, key = { it.wallpaper.id }) { item ->
                DownloadRow(
                    item = item,
                    onClick = { onOpenWallpaper(item.wallpaper.id) },
                    onDelete = { viewModel.remove(item.wallpaper.id) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadedWallpaper,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceRaised)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 82.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NatureBlack)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.wallpaper.thumbUrl)
                    .allowHardware(false)
                    .build(),
                contentDescription = item.wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.wallpaper.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.wallpaper.creator ?: "Creator not stated on Commons",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.wallpaper.resolutionLabel} · ${item.sizeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (item.inGallery) "· gallery" else "· app storage",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenSoft
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Saved ${TimeFormat.relative(item.savedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Delete download",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
