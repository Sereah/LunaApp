package com.lunacattus.app.player.routes.main.video

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
import com.lunacattus.app.player.routes.base.NavRoute
import com.lunacattus.app.player.routes.main.video.mvi.VideoUiIntent
import com.lunacattus.app.player.routes.main.video.mvi.VideoUiState
import com.lunacattus.app.player.routes.main.video.mvi.VideoViewModel
import com.lunacattus.app.player.routes.player.navToPlayer

data object VideoGraph : NavRoute {
    override val route: String
        get() = "VideoGraph"
}

data object VideoRoute : NavRoute {
    override val route: String
        get() = "VideoRoute"
}

fun NavGraphBuilder.videoRouter(
    mainNavController: NavHostController,
    rootNavController: NavHostController
) {
    navigation(
        route = VideoGraph.route,
        startDestination = VideoRoute.route
    ) {
        composable(VideoRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(VideoGraph.route)
            }
            VideoScreen(
                uiState = graph.getUiState().value,
                sendUiIntent = graph.sendUiIntent(),
                navToPlayer = { video ->
                    rootNavController.navToPlayer(
                        video.sources.first(),
                        video.title
                    )
                }
            )
        }
    }
}

@Composable
private fun NavBackStackEntry.getUiState(): State<VideoUiState> {
    val viewModel: VideoViewModel = hiltViewModel(this)
    return viewModel.uiState.collectAsStateWithLifecycle()
}

@Composable
private fun NavBackStackEntry.sendUiIntent(): (VideoUiIntent) -> Unit {
    val viewModel: VideoViewModel = hiltViewModel(this)
    return {
        viewModel.handleUiIntent(it)
    }
}


