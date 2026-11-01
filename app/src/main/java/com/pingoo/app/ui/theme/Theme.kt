package com.pingoo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern Cyan / Deep Blue Palette for Pingoo
val BrandCyanPrimary = Color(0xFF00838F) // Deep Cyan
val BrandCyanSecondary = Color(0xFF00ACC1) // Vibrant Cyan
val BrandCyanTertiary = Color(0xFF26C6DA) // Electric Cyan Accent
val BrandCyanContainer = Color(0xFFE0F7FA) // Soft Cyan Tint
val BrandOnCyanContainer = Color(0xFF00363A)

val BrandDarkPrimary = Color(0xFF80DEEA) // Light Cyan for Dark Mode
val BrandDarkSecondary = Color(0xFF4DD0E1)
val BrandDarkTertiary = Color(0xFF00E5FF)
val BrandDarkContainer = Color(0xFF004D40)
val BrandDarkSurface = Color(0xFF121E24)
val BrandDarkBackground = Color(0xFF0A1114)

private val LightColorScheme = lightColorScheme(
    primary = BrandCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandCyanContainer,
    onPrimaryContainer = BrandOnCyanContainer,
    secondary = BrandCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = BrandCyanTertiary,
    background = Color(0xFFF6FAF9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF455A64)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandDarkPrimary,
    onPrimary = Color(0xFF00363A),
    primaryContainer = BrandDarkContainer,
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = BrandDarkSecondary,
    onSecondary = Color(0xFF00363A),
    tertiary = BrandDarkTertiary,
    background = BrandDarkBackground,
    surface = BrandDarkSurface,
    onBackground = Color(0xFFE1E3E4),
    onSurface = Color(0xFFE1E3E4),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

@Composable
fun PingooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
