package com.skytrace.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// SkyTrace color palette - dark astronomy theme
val Black = Color(0xFF000000)
val DarkNavy = Color(0xFF0A1628)
val Surface = Color(0xFF111B2E)
val SurfaceVariant = Color(0xFF162036)
val AccentBlue = Color(0xFF4A8FE7)
val AccentBlueDim = Color(0xFF2A5A9E)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9EAAB8)
val TextTertiary = Color(0xFF5A6A7A)
val NightRed = Color(0xFF8B2020)
val NightRedDim = Color(0xFF4A1010)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFA726)
val Error = Color(0xFFE53935)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlueDim,
    secondary = TextSecondary,
    onSecondary = Color.White,
    background = Black,
    onBackground = TextPrimary,
    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.White,
    outline = TextTertiary
)

// Night mode uses red tones to preserve dark adaptation
private val NightColorScheme = darkColorScheme(
    primary = NightRed,
    onPrimary = Color.White,
    primaryContainer = NightRedDim,
    secondary = Color(0xFF8B6060),
    onSecondary = Color.White,
    background = Black,
    onBackground = Color(0xFFCC6060),
    surface = Color(0xFF0A0505),
    onSurface = Color(0xFFCC6060),
    surfaceVariant = Color(0xFF150808),
    onSurfaceVariant = Color(0xFF885050),
    error = NightRed,
    onError = Color.White,
    outline = Color(0xFF4A2020)
)

@Composable
fun SkyTraceTheme(
    nightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (nightMode) NightColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
