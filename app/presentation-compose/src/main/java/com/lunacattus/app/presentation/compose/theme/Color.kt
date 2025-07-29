package com.lunacattus.app.presentation.compose.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalColorScheme = staticCompositionLocalOf { appLightThemeColor }

data class ColorScheme(
    val background: Color,
    val mainText: Color,
)

internal val appDarkThemeColor = ColorScheme(
    background = Color(0xFF000000),
    mainText = Color(0xFFFFFFFF)
)

internal val appLightThemeColor = ColorScheme(
    background = Color(0xFFFFFFFF),
    mainText = Color(0xFF000000),
)