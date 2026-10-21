package com.pingoo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val PrimaryColor = Color(0xFF1E88E5)
val PrimaryVariant = Color(0xFF1565C0)
val SecondaryColor = Color(0xFF26A69A)
val BackgroundColor = Color(0xFFF8F9FA)
val SurfaceColor = Color(0xFFFFFFFF)
val OnPrimaryColor = Color(0xFFFFFFFF)
val OnSurfaceColor = Color(0xFF212121)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = OnPrimaryColor,
    onSurface = OnSurfaceColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryVariant,
    secondary = SecondaryColor
)

@Composable
fun PingooTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
