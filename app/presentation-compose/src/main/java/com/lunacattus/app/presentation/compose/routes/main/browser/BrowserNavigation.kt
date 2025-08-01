package com.lunacattus.app.presentation.compose.routes.main.browser

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.presentation.compose.routes.base.NavRoute

data object BrowserGraph : NavRoute {
    override val route: String
        get() = "BrowserGraph"
}

data object BrowserRoute : NavRoute {
    override val route: String
        get() = "BrowserRoute"
}

fun NavGraphBuilder.browserRouter(navController: NavHostController) {
    navigation(
        route = BrowserGraph.route,
        startDestination = BrowserRoute.route
    ) {
        composable(BrowserRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(BrowserGraph.route)
            }
            BrowserScreen()
        }
    }
}



