package com.naturewalls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naturewalls.app.ui.theme.ForestGreen
import com.naturewalls.app.ui.theme.ForestGreenDeep
import com.naturewalls.app.ui.theme.ForestGreenSoft
import com.naturewalls.app.ui.theme.Hairline
import com.naturewalls.app.ui.theme.Moss
import com.naturewalls.app.ui.theme.NatureBlack
import com.naturewalls.app.ui.theme.Sand
import com.naturewalls.app.ui.theme.TextPrimary
import com.naturewalls.app.ui.theme.TextSecondary
import com.naturewalls.app.ui.theme.TextTertiary
import com.naturewalls.app.util.TimeFormat

/** Frosted panel: faint white wash over black, one hairline border, no heavy gradient. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.075f), Color.White.copy(alpha = 0.022f))
                )
            )
            .border(1.dp, Hairline, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun NatureLogo(modifier: Modifier = Modifier, showTagline: Boolean = true) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ForestGreen, ForestGreenDeep))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Landscape,
                contentDescription = null,
                tint = NatureBlack,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text(
                text = "NatureWalls",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            if (showTagline) {
                Text(
                    text = "LIVE FROM WIKIMEDIA COMMONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenSoft,
                    letterSpacing = 1.3.sp
                )
            }
        }
    }
}

/** Small "LIVE · 2 min ago" pill, proving the shelf was fetched from the API just now. */
@Composable
fun LiveBadge(fetchedAt: Long, modifier: Modifier = Modifier) {
    if (fetchedAt <= 0L) return
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ForestGreen.copy(alpha = 0.12f))
            .border(1.dp, ForestGreen.copy(alpha = 0.30f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ForestGreen)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "LIVE · ${TimeFormat.relative(fetchedAt)}",
            style = MaterialTheme.typography.labelSmall,
            color = ForestGreenSoft
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = ForestGreenSoft,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun LicenseChip(licenseName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = licenseName,
            style = MaterialTheme.typography.labelSmall,
            color = Sand,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Hairline)
    )
}

@Composable
fun MetaRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Moss,
            modifier = Modifier.weight(1f)
        )
    }
}
