package com.lunacattus.app.connection.ui.routes.main.bluetooth

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.ui.routes.base.NavRoute

data object BluetoothGraph : NavRoute {
    override val route: String
        get() = "BluetoothGraph"
}

data object BluetoothRoute : NavRoute {
    override val route: String
        get() = "BluetoothRoute"
}

data object BtDiscoveryRoute : NavRoute {
    override val route: String
        get() = "BtDiscoveryRoute"
}

data object BtBondedRoute : NavRoute {
    override val route: String
        get() = "BtBondedRoute"
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
            BluetoothRoute((hiltViewModel(graph)),
                navToBtDiscovery = {mainNavController.navigate(BtDiscoveryRoute.route)},
                navToBtBonded = {mainNavController.navigate(BtBondedRoute.route)}
            )
        }
        composable(BtDiscoveryRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            BtDiscoveryRoute(hiltViewModel(graph)) {
                mainNavController.popBackStack()
            }
        }
        composable(BtBondedRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            BtBondedRoute(hiltViewModel(graph)) {
                mainNavController.popBackStack()
            }
        }
    }
}