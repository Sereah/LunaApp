package com.lunacattus.nav3test.ui.section.bluetooth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.nav3test.ui.base.LocalNavigator
import com.lunacattus.nav3test.ui.base.MainRoute
import com.lunacattus.nav3test.ui.section.bluetooth.screen.BluetoothRoute
import com.lunacattus.nav3test.ui.section.bluetooth.screen.BtDiscoveryRoute
import com.lunacattus.nav3test.ui.section.root.FullScreenDetail
import kotlinx.serialization.Serializable

@Serializable
data object BluetoothRoute : MainRoute

@Serializable
data object BtDiscoveryRoute : MainRoute

@Serializable
data object BtBondedRoute : MainRoute

@Serializable
data object BtDeviceDetailRoute : MainRoute

fun EntryProviderScope<NavKey>.bluetoothSection() {
    entry<BluetoothRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothHomeViewModel>()
        BluetoothRoute { navigator.navigate(BtDiscoveryRoute) }
    }
    entry<BtDiscoveryRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothDiscoveryViewModel>()
        BtDiscoveryRoute {
            navigator.navigate(FullScreenDetail)
        }
    }
    entry<BtBondedRoute> {

    }
    entry<BtDeviceDetailRoute> {

    }
}

