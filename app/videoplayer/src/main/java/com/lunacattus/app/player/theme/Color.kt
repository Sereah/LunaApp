package com.lunacattus.app.player.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalColorScheme = staticCompositionLocalOf { appLightThemeColor }

data class ColorScheme(
    val background: Color,
    val primary: Color,
    val inversePrimary: Color,
)

internal val appDarkThemeColor = ColorScheme(
    background = Color(0xFF000000),
    primary = Color(0xFF9D506E),
    inversePrimary = Color.Gray
)

internal val appLightThemeColor = ColorScheme(
    background = Color(0xFFFFFFFF),
    primary = Color(0xFF9D506E),
    inversePrimary = Color.Gray
)