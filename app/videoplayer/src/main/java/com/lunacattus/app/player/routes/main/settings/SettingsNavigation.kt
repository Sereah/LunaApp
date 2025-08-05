package com.lunacattus.app.player.routes.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.player.routes.base.NavRoute
import com.lunacattus.app.player.theme.AppTheme

data object SettingsGraph : NavRoute {
    override val route: String
        get() = "SettingsGraph"
}

data object SettingsRoute : NavRoute {
    override val route: String
        get() = "SettingsRoute"
}

fun NavGraphBuilder.settingsRouter(navController: NavHostController) {
    navigation(
        route = SettingsGraph.route,
        startDestination = SettingsRoute.route
    ) {
        composable(SettingsRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(SettingsGraph.route)
            }
            SettingsScreen(
                modifier = Modifier.Companion
                    .background(AppTheme.colors.background)
                    .safeDrawingPadding()
                    .fillMaxSize(),
            )
        }
    }
}



