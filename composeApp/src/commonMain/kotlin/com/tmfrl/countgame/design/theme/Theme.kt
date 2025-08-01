package com.tmfrl.countgame.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = Secondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = OnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

@Composable
fun CountGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}