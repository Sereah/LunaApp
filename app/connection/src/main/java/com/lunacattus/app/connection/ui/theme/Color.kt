package com.lunacattus.app.connection.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalColorScheme = staticCompositionLocalOf { appLightThemeColor }

data class ColorScheme(
    val background: Color,
    val card: Color,
    val primary: Color,
    val inversePrimary: Color,
    val icon: Color,
    val button: Color,
    val divider: Color,
)

internal val appDarkThemeColor = ColorScheme(
    background = Color(0xFF000000),
    card = Color(0xFFFFFFFF),
    primary = Color(0xFF35898F),
    inversePrimary = Color.Gray,
    icon = Color(0xFF3B86F7),
    button = Color(0xFF65C466),
    divider = Color(0xFFE7E7E8)
)

internal val appLightThemeColor = ColorScheme(
    background = Color(0xFFF2F2F6),
    card = Color(0xFFFFFFFF),
    primary = Color(0xFF000000),
    inversePrimary = Color(0xFF7F7F7F),
    icon = Color(0xFF3B86F7),
    button = Color(0xFF65C466),
    divider = Color(0xFFE7E7E8)
)