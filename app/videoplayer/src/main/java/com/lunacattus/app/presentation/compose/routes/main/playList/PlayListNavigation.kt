package com.lunacattus.app.presentation.compose.routes.main.playList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.presentation.compose.routes.base.NavRoute
import com.lunacattus.app.presentation.compose.routes.main.playList.mvi.PlayListUiIntent
import com.lunacattus.app.presentation.compose.routes.main.playList.mvi.PlayListUiState
import com.lunacattus.app.presentation.compose.routes.main.playList.mvi.PlayListViewModel
import com.lunacattus.app.presentation.compose.routes.player.navToPlayer

data object PlayListGraph : NavRoute {
    override val route: String
        get() = "PlayListGraph"
}

data object PlayListRoute : NavRoute {
    override val route: String
        get() = "PlayListRoute"
}

fun NavGraphBuilder.playListRouter(
    navController: NavHostController,
    rootNavController: NavHostController
) {
    navigation(
        route = PlayListGraph.route,
        startDestination = PlayListRoute.route
    ) {
        composable(PlayListRoute.route) {
            val graph = remember(it) {
                navController.getBackStackEntry(PlayListGraph.route)
            }
            PlayListScreen(
                uiState = graph.getUiState().value,
                sendUiIntent = graph.sendUiIntent(),
                navToPlayer = {rootNavController.navToPlayer("", "")}
            )
        }
    }
}

@Composable
private fun NavBackStackEntry.getUiState(): State<PlayListUiState> {
    val viewModel: PlayListViewModel = hiltViewModel(this)
    return viewModel.uiState.collectAsStateWithLifecycle()
}

@Composable
private fun NavBackStackEntry.sendUiIntent(): (PlayListUiIntent) -> Unit {
    val viewModel: PlayListViewModel = hiltViewModel(this)
    return {
        viewModel.handleUiIntent(it)
    }
}



