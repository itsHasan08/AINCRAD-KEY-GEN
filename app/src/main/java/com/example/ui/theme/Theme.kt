package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TitleDarkCharcoal,
    onPrimary = Color.White,
    secondary = PortalBtnBorder,
    onSecondary = Color.White,
    background = BackgroundWhite,
    onBackground = TitleDarkCharcoal,
    surface = BackgroundWhite,
    onSurface = TitleDarkCharcoal,
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
