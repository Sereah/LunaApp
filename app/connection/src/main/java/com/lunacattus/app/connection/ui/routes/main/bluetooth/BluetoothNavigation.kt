package com.lunacattus.app.connection.ui.routes.main.bluetooth

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.ui.routes.base.NavRoute
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothViewModel

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
            BluetoothRoute(viewModel)
        }
    }
}