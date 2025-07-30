package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiIntent
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiState
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeViewModel
import com.lunacattus.app.presentation.compose.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
private data object HomeGraph

@Serializable
private data object HomeRoute

fun NavController.navToHome() {
    navigate(HomeGraph)
}

fun NavGraphBuilder.homeRouter(navController: NavHostController) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            val graph = remember(it) {
                navController.getBackStackEntry<HomeGraph>()
            }
            HomeScreen(
                modifier = Modifier
                    .background(AppTheme.colors.background)
                    .safeDrawingPadding()
                    .fillMaxSize(),
                uiState = graph.getUiState().value,
                sendUiIntent = graph.sendUiIntent()
            )
        }
    }
}

@Composable
private fun NavBackStackEntry.getUiState(): State<HomeUiState> {
    val viewModel: HomeViewModel = hiltViewModel(this)
    return viewModel.uiState.collectAsStateWithLifecycle()
}

@Composable
private fun NavBackStackEntry.sendUiIntent(): (HomeUiIntent) -> Unit {
    val viewModel: HomeViewModel = hiltViewModel(this)
    return {
        viewModel.handleUiIntent(it)
    }
}


