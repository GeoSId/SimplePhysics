package com.geosid.simplephysics.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = ScienceDarkBg,
    primaryContainer = ScienceDarkSurfaceVariant,
    onPrimaryContainer = CyanNeon,
    secondary = AmberVibrant,
    onSecondary = ScienceDarkBg,
    secondaryContainer = ScienceDarkSurfaceVariant,
    onSecondaryContainer = AmberVibrant,
    tertiary = PurpleNeon,
    onTertiary = ScienceDarkBg,
    background = ScienceDarkBg,
    onBackground = TextPrimary,
    surface = ScienceDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = ScienceDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ScienceBorder
)

@Composable
fun SimplePhysicsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
