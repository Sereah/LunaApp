package com.lunacattus.app.presentation.compose.routes.main.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.presentation.compose.routes.base.NavRoute
import com.lunacattus.app.presentation.compose.theme.AppTheme

data object MusicGraph : NavRoute {
    override val route: String
        get() = "MusicGraph"
}

data object MusicRoute : NavRoute {
    override val route: String
        get() = "MusicRoute"
}

fun NavGraphBuilder.musicRouter(navController: NavHostController) {
    navigation(
        route = MusicGraph.route,
        startDestination = MusicRoute.route
    ) {
        composable(MusicRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(MusicGraph.route)
            }
            MusicScreen(
                modifier = Modifier
                    .background(AppTheme.colors.background)
                    .safeDrawingPadding()
                    .fillMaxSize(),
            )
        }
    }
}



