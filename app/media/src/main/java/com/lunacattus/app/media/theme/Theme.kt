package com.lunacattus.app.media.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun LunaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) appDarkThemeColor else appLightThemeColor
    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        content = content
    )
}

object LunaAppTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalColorScheme.current
}