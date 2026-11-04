package com.naturewalls.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.ui.components.GlassPanel
import com.naturewalls.app.ui.components.HairlineDivider
import com.naturewalls.app.ui.components.LicenseChip
import com.naturewalls.app.ui.components.MetaRow
import com.naturewalls.app.ui.components.StatePanel
import com.naturewalls.app.ui.theme.ForestGreen
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.Moss
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.ScrimBottom
import com.naturewalls.app.ui.theme.Sand
import com.naturewalls.app.ui.theme.SurfaceRaised
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary
import com.naturewalls.app.util.ShareHelper
import com.naturewalls.app.util.TimeFormat
import com.naturewalls.app.util.WallpaperTarget
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = NatureBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val wallpaper = state.wallpaper

            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ForestGreen, strokeWidth = 2.dp)
                }

                wallpaper == null -> StatePanel(
                    icon = Icons.AutoMirrored.Rounded.OpenInNew,
                    title = "Wallpaper unavailable",
                    message = state.error ?: "This photograph could not be loaded.",
                    actionLabel = "Go back",
                    onAction = onBack
                )

                else -> DetailContent(
                    wallpaper = wallpaper,
                    isFavorite = state.isFavorite,
                    isDownloaded = state.isDownloaded,
                    onBack = onBack,
                    onFavorite = viewModel::toggleFavorite,
                    onDownload = viewModel::download,
                    onShare = {
                        scope.launch {
                            val file = viewModel.localImageFile()
                            ShareHelper.share(context, wallpaper, file)
                        }
                    },
                    onSetWallpaper = { target ->
                        viewModel.setWallpaper(
                            target = target,
                            screenWidth = configuration.screenWidthDp,
                            screenHeight = configuration.screenHeightDp
                        )
                    }
                )
            }

            val busy = state.busyAction
            if (busy != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f)),
                    contentAlignment = Alignment.Center
                ) {
                    GlassPanel(
                        contentPadding = 22.dp,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = ForestGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = busy,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onSetWallpaper: (WallpaperTarget) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .background(SurfaceRaised)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.previewUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.55f),
                            0.35f to Color.Transparent,
                            1f to ScrimBottom
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    description = "Back",
                    onClick = onBack
                )
                Spacer(Modifier.weight(1f))
                RoundIconButton(
                    icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    description = "Favourite",
                    onClick = onFavorite,
                    tint = if (isFavorite) ForestGreen else TextPrimary
                )
                Spacer(Modifier.width(10.dp))
                RoundIconButton(
                    icon = Icons.Rounded.Share,
                    description = "Share",
                    onClick = onShare
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                LicenseChip(wallpaper.licenseName)
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
                    color = TextSecondary
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = wallpaper.locationLabel?.let { "Location · $it" }
                        ?: "Subject · ${wallpaper.categories.firstOrNull() ?: "Nature"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryAction(
                    icon = Icons.Rounded.Download,
                    label = if (isDownloaded) "Saved" else "Download",
                    onClick = onDownload,
                    modifier = Modifier.weight(1f)
                )
                PrimaryAction(
                    icon = Icons.Rounded.Smartphone,
                    label = "Home",
                    onClick = { onSetWallpaper(WallpaperTarget.HOME) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryAction(
                    icon = Icons.Rounded.Lock,
                    label = "Lock screen",
                    onClick = { onSetWallpaper(WallpaperTarget.LOCK) },
                    modifier = Modifier.weight(1f)
                )
                PrimaryAction(
                    icon = Icons.Rounded.PhoneIphone,
                    label = "Both",
                    onClick = { onSetWallpaper(WallpaperTarget.BOTH) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Wallpaper credits",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Attribution required by the licence — reproduced exactly as published on Commons.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(14.dp))

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                MetaRow(label = "Title", value = wallpaper.title)
                Spacer(Modifier.height(10.dp))
                MetaRow(
                    label = "Photographer",
                    value = wallpaper.creator ?: "Not stated on Commons"
                )
                if (wallpaper.credit != null && wallpaper.credit != wallpaper.creator) {
                    Spacer(Modifier.height(10.dp))
                    MetaRow(label = "Credit", value = wallpaper.credit)
                }
                Spacer(Modifier.height(10.dp))
                MetaRow(label = "Licence", value = wallpaper.licenseName)
                if (wallpaper.licenseUrl != null) {
                    Spacer(Modifier.height(10.dp))
                    MetaRow(label = "Licence URL", value = wallpaper.licenseUrl)
                }
                Spacer(Modifier.height(10.dp))
                MetaRow(
                    label = "Resolution",
                    value = "${wallpaper.resolutionLabel} · ${wallpaper.megapixels}"
                )
                TimeFormat.commonsDate(wallpaper.uploadDate)?.let { date ->
                    Spacer(Modifier.height(10.dp))
                    MetaRow(label = "Published", value = date)
                }
                Spacer(Modifier.height(10.dp))
                MetaRow(label = "Modification", value = wallpaper.modificationNote)

                if (wallpaper.description != null) {
                    Spacer(Modifier.height(14.dp))
                    HairlineDivider()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = wallpaper.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Moss
                    )
                }

                Spacer(Modifier.height(16.dp))
                HairlineDivider()
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(ForestGreen.copy(alpha = 0.14f))
                        .border(1.dp, ForestGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { ShareHelper.openUrl(context, wallpaper.sourcePageUrl) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        tint = ForestGreenSoft,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Open source page on Wikimedia Commons",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestGreenSoft
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = wallpaper.sourcePageUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        ShareHelper.openUrl(context, wallpaper.sourcePageUrl)
                    }
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "Licence terms",
                style = MaterialTheme.typography.titleMedium,
                color = Sand
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = licenceSummary(wallpaper),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(28.dp))
            HairlineDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Image delivered live from the Wikimedia Commons API. Downloads keep the " +
                    "original file untouched; the preview you see is Commons' own rendered thumbnail.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            Spacer(Modifier.height(40.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

private fun licenceSummary(wallpaper: Wallpaper): String {
    val name = wallpaper.licenseName.lowercase()
    return when {
        name.startsWith("cc0") || "public domain" in name ->
            "Public-domain dedication: no conditions. Credit is shown as a courtesy and for provenance."

        "sa" in name || "share" in name ->
            "CC BY-SA: you must credit the photographer, link the licence, and share any adapted " +
                "version under the same licence. NatureWalls redistributes the file unmodified."

        else ->
            "CC BY: credit the photographer and link the licence. NatureWalls redistributes the " +
                "file unmodified."
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun PrimaryAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreenSoft,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary
        )
    }
}
