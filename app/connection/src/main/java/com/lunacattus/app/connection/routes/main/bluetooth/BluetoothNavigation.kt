package com.lunacattus.app.connection.routes.main.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.routes.base.NavRoute
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiEffect
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothViewModel

data object BluetoothGraph : NavRoute {
    override val route: String
        get() = "BluetoothGraph"
}

data object BluetoothRoute : NavRoute {
    override val route: String
        get() = "BluetoothRoute"
}

fun NavGraphBuilder.bluetoothRouter(
    mainNavController: NavHostController,
    rootNavController: NavHostController
) {
    navigation(
        route = BluetoothGraph.route,
        startDestination = BluetoothRoute.route
    ) {
        composable(BluetoothRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            val viewModel: BluetoothViewModel = hiltViewModel(graph)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(
                BluetoothUiEffect.Idle
            )
            BluetoothScreen(
                uiState = uiState,
                uiEffect = uiEffect,
                sendUiIntent = graph.sendUiIntent()
            )
        }
    }
}

@Composable
private fun NavBackStackEntry.sendUiIntent(): (BluetoothUiIntent) -> Unit {
    val viewModel: BluetoothViewModel = hiltViewModel(this)
    return {
        viewModel.handleUiIntent(it)
    }
}