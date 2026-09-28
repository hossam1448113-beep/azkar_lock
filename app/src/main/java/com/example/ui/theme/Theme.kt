package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = Emerald400,
        onPrimary = Emerald900,
        primaryContainer = Emerald800,
        onPrimaryContainer = Emerald100,
        secondary = GoldMetallic,
        onSecondary = Emerald900,
        secondaryContainer = Emerald700,
        onSecondaryContainer = GoldLight,
        tertiary = AmberGlow,
        background = DarkCanvas,
        onBackground = PureWhite,
        surface = DarkSurface,
        onSurface = PureWhite,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = TextMutedLight,
        outline = CardBorderDark,
        error = ErrorRed
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Emerald700,
        onPrimary = PureWhite,
        primaryContainer = Emerald100,
        onPrimaryContainer = Emerald900,
        secondary = GoldDark,
        onSecondary = PureWhite,
        secondaryContainer = LightSurfaceVariant,
        onSecondaryContainer = Emerald800,
        tertiary = AmberGlow,
        background = LightCanvas,
        onBackground = TextDark,
        surface = LightSurface,
        onSurface = TextDark,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = TextMutedDark,
        outline = CardBorderLight,
        error = ErrorRed
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

