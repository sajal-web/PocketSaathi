package com.sajalweb.pocketsaathi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = TextOnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldAccent,
    onTertiary = TextPrimary,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardBg,
    onSurfaceVariant = TextSecondary,
    error = RedWarning,
    onError = Color.White,
    errorContainer = RedWarning.copy(alpha = 0.1f),
    onErrorContainer = RedWarning,
    outline = Divider,
    outlineVariant = BorderLight,
    inverseSurface = TextPrimary,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Primary,
    onPrimaryContainer = Color.White,
    secondary = SecondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = Secondary,
    onSecondaryContainer = Color.White,
    tertiary = GoldLight,
    onTertiary = Color.Black,
    tertiaryContainer = GoldAccent,
    onTertiaryContainer = Color.Black,
    background = Color(0xFF0F0F1A),
    onBackground = Color(0xFFE0E0E6),
    surface = Color(0xFF161625),
    onSurface = Color(0xFFE0E0E6),
    surfaceVariant = Color(0xFF232335),
    onSurfaceVariant = Color(0xFFB0B0C0),
    error = RedWarning,
    onError = Color.White,
    errorContainer = RedWarning.copy(alpha = 0.15f),
    onErrorContainer = RedWarning,
    outline = Color(0xFF303045),
    outlineVariant = Color(0xFF3A3A55),
    inverseSurface = Color(0xFFE0E0E6),
    inverseOnSurface = Color(0xFF1A1A2E),
    inversePrimary = Primary
)

val PremiumShapes =           Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SpendSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography,
        shapes = PremiumShapes,
        content = content
    )
}
