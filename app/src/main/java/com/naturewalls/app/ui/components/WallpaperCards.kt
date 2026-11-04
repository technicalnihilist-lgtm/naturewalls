package com.naturewalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.Hairline
import com.naturewalls.app.ui.theme.Moss
import com.naturewalls.app.ui.theme.ScrimBottom
import com.naturewalls.app.ui.theme.SurfaceRaised
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary

private fun imageRequest(context: android.content.Context, url: String): ImageRequest =
    ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        // ARGB_8888 bitmaps: hardware bitmaps render black on software-GPU devices
        // (emulators, some low-end hardware) and cannot be re-used by WallpaperManager.
        .allowHardware(false)
        .build()

/** Portrait card used inside horizontal rails. */
@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: androidx.compose.ui.unit.Dp = 168.dp,
    cardHeight: androidx.compose.ui.unit.Dp = 232.dp,
    showLicense: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(shape)
            .background(SurfaceRaised)
            .border(1.dp, Hairline, shape)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageRequest(LocalContext.current, wallpaper.thumbUrl),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1f to ScrimBottom
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = wallpaper.shortTitle,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (showLicense) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = wallpaper.licenseName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Moss,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Grid tile: 3:4 portrait, so a two-column grid feels like a print wall. */
@Composable
fun GridWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(shape)
            .background(SurfaceRaised)
            .border(1.dp, Hairline, shape)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageRequest(LocalContext.current, wallpaper.thumbUrl),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to ScrimBottom
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(11.dp)
        ) {
            Text(
                text = wallpaper.shortTitle,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = wallpaper.licenseName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Moss,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (footer != null) {
                    Spacer(Modifier.width(6.dp))
                    footer()
                }
            }
        }
    }
}

/** Large cinematic card for the daily wallpaper. */
@Composable
fun HeroWallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String = "DAILY NATURE WALLPAPER",
    trailing: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(430.dp)
            .clip(shape)
            .background(SurfaceRaised)
            .border(1.dp, Hairline, shape)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageRequest(LocalContext.current, wallpaper.previewUrl),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        1f to ScrimBottom
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenSoft
                )
                Spacer(Modifier.width(8.dp))
                LicenseChip(wallpaper.licenseName)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = wallpaper.creator ?: "Creator not stated on Commons",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View wallpaper",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                if (trailing != null) {
                    Spacer(Modifier.width(10.dp))
                    trailing()
                }
            }
        }
    }
}

/**
 * A titled horizontal shelf. When the feed is still loading it shows shimmer placeholders instead
 * of collapsing, so the home screen never jumps around.
 */
@Composable
fun WallpaperRail(
    title: String,
    items: List<Wallpaper>,
    onItemClick: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    loading: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            actionLabel = actionLabel,
            onAction = onAction,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        when {
            error != null && items.isEmpty() -> {
                InlineError(
                    message = error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            items.isEmpty() && loading -> {
                LazyRow(
                    contentPadding = contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(4) { ShimmerBox(Modifier.width(168.dp).height(232.dp)) }
                }
            }

            items.isEmpty() -> {
                Text(
                    text = "Nothing here right now — pull a refresh to look again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            else -> {
                LazyRow(
                    contentPadding = contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = items, key = { it.id }) { wallpaper ->
                        WallpaperCard(wallpaper = wallpaper, onClick = { onItemClick(wallpaper) })
                    }
                }
            }
        }
    }
}
