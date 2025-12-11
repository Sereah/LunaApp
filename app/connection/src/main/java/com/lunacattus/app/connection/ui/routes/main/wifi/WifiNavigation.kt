package com.lunacattus.app.connection.ui.routes.main.wifi

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.ui.routes.base.NavRoute

data object WifiGraph : NavRoute {
    override val route: String
        get() = "WifiGraph"
}

data object WifiRoute : NavRoute {
    override val route: String
        get() = "WifiRoute"
}

fun NavGraphBuilder.wifiRouter(
    mainNavController: NavHostController,
    rootNavController: NavHostController
) {
    navigation(
        route = WifiGraph.route,
        startDestination = WifiRoute.route
    ) {
        composable(WifiRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(WifiGraph.route)
            }

        }
    }
}