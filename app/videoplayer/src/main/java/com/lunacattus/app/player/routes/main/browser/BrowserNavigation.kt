package com.lunacattus.app.player.routes.main.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.player.routes.base.NavRoute
import com.lunacattus.app.player.routes.main.browser.mvi.BrowserUiIntent
import com.lunacattus.app.player.routes.main.browser.mvi.BrowserViewModel
import com.lunacattus.app.player.routes.player.navToPlayer

data object BrowserGraph : NavRoute {
    override val route: String
        get() = "BrowserGraph"
}

data object BrowserRoute : NavRoute {
    override val route: String
        get() = "BrowserRoute"
}

fun NavGraphBuilder.browserRouter(
    navController: NavHostController,
    rootNavController: NavHostController
) {
    navigation(
        route = BrowserGraph.route,
        startDestination = BrowserRoute.route
    ) {
        composable(BrowserRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(BrowserGraph.route)
            }
            BrowserScreen(sendUiIntent = graph.sendUiIntent()) {
                rootNavController.navToPlayer("", "")
            }
        }
    }
}

@Composable
private fun NavBackStackEntry.sendUiIntent(): (BrowserUiIntent) -> Unit {
    val viewModel: BrowserViewModel = hiltViewModel(this)
    return {
        viewModel.handleUiIntent(it)
    }
}

internal const val TAG = "BrowserRoute"



