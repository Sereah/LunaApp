package com.lunacattus.app.connection.ui.routes.main.bluetooth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lunacattus.app.connection.ui.routes.base.NavRoute
import com.lunacattus.app.connection.ui.routes.base.animatedComposable
import com.lunacattus.app.connection.ui.routes.base.graphViewModel

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
        composable(BluetoothRoute.route) { entry ->
            BluetoothRoute(
                viewModel = entry.graphViewModel(mainNavController, BluetoothGraph.route),
                navToBtDiscovery = { mainNavController.navigate(BtDiscoveryRoute.route) },
                navToBtBonded = { mainNavController.navigate(BtBondedRoute.route) }
            )
        }
        animatedComposable(BtDiscoveryRoute.route) { entry ->
            BtDiscoveryRoute(
                viewModel = entry.graphViewModel(mainNavController, BluetoothGraph.route),
                onBack = { mainNavController.popBackStack() }
            )
        }
        animatedComposable(BtBondedRoute.route) { entry ->
            BtBondedRoute(
                viewModel = entry.graphViewModel(mainNavController, BluetoothGraph.route),
                navToDeviceDetail = {
                    mainNavController.navigate(DeviceDetailRoute.route)
                },
                onBack = { mainNavController.popBackStack() }
            )
        }
        animatedComposable(DeviceDetailRoute.route) { entry ->
            DeviceDetailRoute(
                viewModel = entry.graphViewModel(mainNavController, BluetoothGraph.route),
                onBack = {
                    mainNavController.popBackStack()
                }
            )
        }
    }
}