package com.lunacattus.app.connection.ui.routes.main.bluetooth

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.ui.routes.base.NavRoute
import com.lunacattus.app.connection.ui.routes.main.animatedComposable

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

data object DeviceDetailRoute : NavRoute {
    override val route: String = "DeviceDetailRoute"
}

fun NavGraphBuilder.bluetoothRouter(
    mainNavController: NavHostController
) {
    navigation(
        route = BluetoothGraph.route,
        startDestination = BluetoothRoute.route,
    ) {
        composable(BluetoothRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            BluetoothRoute(
                hiltViewModel(graph),
                navToBtDiscovery = { mainNavController.navigate(BtDiscoveryRoute.route) },
                navToBtBonded = { mainNavController.navigate(BtBondedRoute.route) }
            )
        }
        animatedComposable(BtDiscoveryRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            BtDiscoveryRoute(hiltViewModel(graph)) {
                mainNavController.popBackStack()
            }
        }
        animatedComposable(BtBondedRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            BtBondedRoute(
                hiltViewModel(graph),
                navToDeviceDetail = {
                    mainNavController.navigate(DeviceDetailRoute.route)
                },
                onBack = { mainNavController.popBackStack() }
            )
        }
        animatedComposable(DeviceDetailRoute.route) {
            val graph = remember(it) {
                mainNavController.getBackStackEntry(BluetoothGraph.route)
            }
            DeviceDetailRoute(
                hiltViewModel(viewModelStoreOwner = graph),
                onBack = {
                    mainNavController.popBackStack()
                }
            )
        }
    }
}