package com.netkeep5g.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = CyanAccent,
    secondary = BlueAccent,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderDark,
    error = RedStop,
    onError = TextPrimary
)

@Composable
fun NetKeepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
