package com.naturewalls.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val NatureColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = NatureBlack,
    primaryContainer = ForestGreenDeep,
    onPrimaryContainer = TextPrimary,
    secondary = Moss,
    onSecondary = NatureBlack,
    tertiary = Sand,
    onTertiary = NatureBlack,
    background = NatureBlack,
    onBackground = TextPrimary,
    surface = SurfaceDeep,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = HairlineStrong,
    outlineVariant = Hairline,
    error = ErrorRed,
    onError = NatureBlack,
    scrim = NatureBlack
)

private val NatureShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun NatureWallsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NatureColorScheme,
        typography = NatureTypography,
        shapes = NatureShapes,
        content = content
    )
}
