package com.novaai.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColors = darkColorScheme(
    primary = NovaBlue,
    onPrimary = Color.White,
    secondary = NovaPurple,
    onSecondary = Color.White,
    tertiary = NovaCyan,
    background = NovaBlack,
    onBackground = Color.White,
    surface = NovaSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = NovaSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC9C9D9)
)

private val NovaLightColors = lightColorScheme(
    primary = NovaBlueDeep,
    onPrimary = Color.White,
    secondary = NovaPurpleDeep,
    onSecondary = Color.White,
    tertiary = NovaCyan,
    background = NovaLightBackground,
    onBackground = NovaInk,
    surface = Color.White,
    onSurface = NovaInk,
    surfaceVariant = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF3A3A4A)
)

@Composable
fun NovaAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NovaDarkColors else NovaLightColors,
        content = content
    )
}
