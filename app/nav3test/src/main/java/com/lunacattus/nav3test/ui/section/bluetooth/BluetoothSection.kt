package com.lunacattus.nav3test.ui.section.bluetooth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.nav3test.ui.base.LocalNavigator
import com.lunacattus.nav3test.ui.base.MainRoute
import com.lunacattus.nav3test.ui.section.bluetooth.discovery.BluetoothDiscoveryViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.discovery.BtDiscoveryRoute
import com.lunacattus.nav3test.ui.section.bluetooth.homepage.BluetoothHomeViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.homepage.BluetoothRoute
import kotlinx.serialization.Serializable

@Serializable
data object BluetoothRoute : MainRoute {
    override val name: String
        get() = "Bluetooth"
}

@Serializable
data class BtDiscoveryRoute(val localDeviceName: String) : MainRoute {
    override val name: String
        get() = "Discovery"
}

@Serializable
data object BtBondedRoute : MainRoute {
    override val name: String
        get() = "Bonded"
}

@Serializable
data object BtDeviceDetailRoute : MainRoute {
    override val name: String
        get() = "Device Detail"
}

fun EntryProviderScope<NavKey>.bluetoothSection() {
    entry<BluetoothRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothHomeViewModel>()
        BluetoothRoute(
            viewModel = viewmodel,
            navToBtBonded = { navigator.navigate(BtBondedRoute) },
            navToBtDiscovery = {
                navigator.navigate(BtDiscoveryRoute(it))
            }
        )
    }
    entry<BtDiscoveryRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothDiscoveryViewModel>()
        BtDiscoveryRoute(it.localDeviceName, viewmodel) { navigator.goBack() }
    }
    entry<BtBondedRoute> {

    }
    entry<BtDeviceDetailRoute> {

    }
}

