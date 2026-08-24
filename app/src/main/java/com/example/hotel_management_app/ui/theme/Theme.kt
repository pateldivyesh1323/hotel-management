package com.example.hotel_management_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val LightColorScheme = lightColorScheme(
    primary = LimePrimary,
    onPrimary = LimeOnPrimary,
    primaryContainer = LimeContainer,
    onPrimaryContainer = LimeOnContainer,
    secondary = StoneSecondary,
    onSecondary = StoneOnSecondary,
    secondaryContainer = StoneContainer,
    onSecondaryContainer = StoneOnContainer,
    tertiary = BlueTertiary,
    onTertiary = BlueOnTertiary,
    tertiaryContainer = BlueContainer,
    onTertiaryContainer = BlueOnContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightBackground,
    surfaceContainerHighest = LightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = LimePrimaryDark,
    onPrimary = LimeOnPrimaryDark,
    primaryContainer = LimeContainerDark,
    onPrimaryContainer = LimeOnContainerDark,
    secondary = StoneSecondaryDark,
    onSecondary = StoneOnSecondaryDark,
    secondaryContainer = StoneContainerDark,
    onSecondaryContainer = StoneOnContainerDark,
    tertiary = BlueTertiaryDark,
    onTertiary = BlueOnTertiaryDark,
    tertiaryContainer = BlueContainerDark,
    onTertiaryContainer = BlueOnContainerDark,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = DarkSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

/** True when the app is painting its dark scheme, inferred from the surface colour. */
@Composable
@ReadOnlyComposable
internal fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

/**
 * The ordered series palette every chart draws from, so a donut slice, a bar and an area
 * fill for the same data always land on the same colour.
 *
 * The order is fixed and never cycled: it is what keeps adjacent series separable under
 * colour-blind simulation, and both modes were validated as a set. See [ChartLime].
 */
@Composable
@ReadOnlyComposable
fun chartPalette(): List<Color> = if (isDarkScheme()) {
    listOf(ChartLimeDark, ChartRoseDark, ChartBlueDark, ChartOrangeDark, ChartVioletDark, ChartAquaDark)
} else {
    listOf(ChartLime, ChartRose, ChartBlue, ChartOrange, ChartViolet, ChartAqua)
}

/**
 * Dynamic colour is deliberately off: a property's brand should look the same on every
 * device at the front desk.
 */
@Composable
fun Hotel_Management_AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
