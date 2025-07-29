package com.lunacattus.app.presentation.compose.routes.enter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lunacattus.app.presentation.compose.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable
data object EnterRoute

fun NavGraphBuilder.enterRouter(navToMain: () -> Unit) {
    composable<EnterRoute> {
        EnterScreen(
            modifier = Modifier.Companion
                .background(AppTheme.colors.background)
                .safeDrawingPadding()
                .fillMaxSize(),
            navToMain = navToMain
        )
    }
}