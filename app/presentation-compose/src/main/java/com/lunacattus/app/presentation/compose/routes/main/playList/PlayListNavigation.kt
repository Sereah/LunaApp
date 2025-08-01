package com.lunacattus.app.presentation.compose.routes.main.playList

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

data object PlayListGraph : NavRoute {
    override val route: String
        get() = "PlayListGraph"
}

data object PlayListRoute : NavRoute {
    override val route: String
        get() = "PlayListRoute"
}

fun NavGraphBuilder.playListRouter(navController: NavHostController) {
    navigation(
        route = PlayListGraph.route,
        startDestination = PlayListRoute.route
    ) {
        composable(PlayListRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(PlayListGraph.route)
            }
            PlayListScreen(
                modifier = Modifier
                    .background(AppTheme.colors.background)
                    .safeDrawingPadding()
                    .fillMaxSize(),
            )
        }
    }
}



