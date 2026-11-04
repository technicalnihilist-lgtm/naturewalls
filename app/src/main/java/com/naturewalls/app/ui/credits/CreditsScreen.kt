package com.naturewalls.app.ui.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naturewalls.app.ui.components.GlassPanel
import com.naturewalls.app.ui.components.HairlineDivider
import com.naturewalls.app.ui.favorites.CreditsViewModel
import com.naturewalls.app.ui.theme.ForestGreen
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.Moss
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.Sand
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary
import com.naturewalls.app.util.ShareHelper

/**
 * The complete attribution ledger: every photograph this device has displayed, with its
 * photographer, licence, licence URL and Commons source page.
 */
@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    onBack: () -> Unit
) {
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NatureBlack),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp)
    ) {
        item {
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
                Text(
                    text = "Credits & licences",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(18.dp))

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "How NatureWalls sources photography",
                    style = MaterialTheme.typography.titleMedium,
                    color = Sand
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Every wallpaper is retrieved live from the official Wikimedia Commons " +
                        "API (commons.wikimedia.org/w/api.php) at the moment it is shown. The app " +
                        "reads each file's own metadata — artist, licence, licence URL and source " +
                        "page — and only displays photographs whose licence is free for reuse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(14.dp))
                HairlineDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "ACCEPTED LICENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenSoft
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "CC0 · Public domain · CC BY · CC BY-SA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "REJECTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "NonCommercial, NoDerivatives, fair use, unknown or missing licence data, " +
                        "and files with no published creator where attribution is required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "CC BY-SA files are redistributed unmodified; the ShareAlike condition " +
                        "therefore applies to any copy you pass on. Every share from this app " +
                        "includes the full credit block.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Moss
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = if (credits.isEmpty()) {
                    "No photographs recorded on this device yet"
                } else {
                    "${credits.size} photographs shown on this device"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap any entry to open its Wikimedia Commons source page.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(14.dp))
        }

        itemsIndexed(items = credits, key = { _, item -> item.id }) { index, wallpaper ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.035f))
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                    .clickable { ShareHelper.openUrl(context, wallpaper.sourcePageUrl) }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = (index + 1).toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreenSoft,
                    modifier = Modifier.width(26.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = wallpaper.creator ?: "Creator not stated on Commons",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = wallpaper.licenseName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Sand
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = wallpaper.resolutionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = "Open source",
                    tint = ForestGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(18.dp))
            HairlineDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "NatureWalls is an independent app and is not affiliated with the Wikimedia " +
                    "Foundation. Photographs remain the property of their photographers and are " +
                    "used under the licences stated above. Text and metadata come from Wikimedia " +
                    "Commons under CC BY-SA 4.0.",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Data source: commons.wikimedia.org",
                style = MaterialTheme.typography.labelSmall,
                color = ForestGreenSoft,
                modifier = Modifier.clickable {
                    ShareHelper.openUrl(context, "https://commons.wikimedia.org/")
                }
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}
