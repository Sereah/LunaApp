package com.lunacattus.nav3test.ui.section.bluetooth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.nav3test.ui.base.LocalNavigator
import com.lunacattus.nav3test.ui.base.MainRoute
import com.lunacattus.nav3test.ui.section.bluetooth.bonded.BluetoothBondedRoute
import com.lunacattus.nav3test.ui.section.bluetooth.bonded.BluetoothBondedViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.detail.BtDeviceDetailRoute
import com.lunacattus.nav3test.ui.section.bluetooth.detail.BtDeviceDetailViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.discovery.BluetoothDiscoveryViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.discovery.BluetoothDiscoveryRoute
import com.lunacattus.nav3test.ui.section.bluetooth.homepage.BluetoothHomeViewModel
import com.lunacattus.nav3test.ui.section.bluetooth.homepage.BluetoothHomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object BluetoothHomeRoute : MainRoute {
    override val name: String
        get() = "蓝牙"
}

@Serializable
data class BluetoothDiscoveryRoute(val localDeviceName: String) : MainRoute {
    override val name: String
        get() = "连接新设备"
}

@Serializable
data object BluetoothBondedRoute : MainRoute {
    override val name: String
        get() = "保存的设备"
}

@Serializable
data class BtDeviceDetailRoute(val address: String) : MainRoute {
    override val name: String
        get() = "设备详细信息"
}

fun EntryProviderScope<NavKey>.bluetoothSection() {
    entry<BluetoothHomeRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothHomeViewModel>()
        BluetoothHomeRoute(
            viewModel = viewmodel,
            navToBtBonded = { navigator.navigate(BluetoothBondedRoute) },
            navToBtDiscovery = {
                navigator.navigate(BluetoothDiscoveryRoute(it))
            }
        )
    }
    entry<BluetoothDiscoveryRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothDiscoveryViewModel>()
        BluetoothDiscoveryRoute(it.localDeviceName, viewmodel) { navigator.goBack() }
    }
    entry<BluetoothBondedRoute> {
        val navigator = LocalNavigator.current
        val viewmodel = hiltViewModel<BluetoothBondedViewModel>()
        BluetoothBondedRoute(viewmodel) { navigator.navigate(BtDeviceDetailRoute(it))}
    }
    entry<BtDeviceDetailRoute> {
        val viewmodel = hiltViewModel<BtDeviceDetailViewModel>()
        val navigator = LocalNavigator.current
        BtDeviceDetailRoute(it.address, viewmodel) {
            navigator.goBack()
        }
    }
}

