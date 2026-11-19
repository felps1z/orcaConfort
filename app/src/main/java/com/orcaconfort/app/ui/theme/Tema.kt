package com.orcaconfort.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlack,
    onPrimary = SurfaceWhite,
    secondary = TextSecondary,
    onSecondary = SurfaceWhite,
    background = BackgroundGray,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BadgeBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderGray
)

@Composable
fun OrcaConfortTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
