package com.lunacattus.connection.ui.section.bluetooth

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.connection.ui.base.LocalNavigator
import com.lunacattus.connection.ui.base.MainRoute
import com.lunacattus.connection.ui.base.entryWithNavAndVm
import com.lunacattus.connection.ui.section.bluetooth.bonded.BluetoothBondedRoute
import com.lunacattus.connection.ui.section.bluetooth.bonded.BluetoothBondedViewModel
import com.lunacattus.connection.ui.section.bluetooth.detail.BtDeviceDetailRoute
import com.lunacattus.connection.ui.section.bluetooth.detail.BtDeviceDetailViewModel
import com.lunacattus.connection.ui.section.bluetooth.discovery.BluetoothDiscoveryRoute
import com.lunacattus.connection.ui.section.bluetooth.discovery.BluetoothDiscoveryViewModel
import com.lunacattus.connection.ui.section.bluetooth.homepage.BluetoothHomeRoute
import com.lunacattus.connection.ui.section.bluetooth.homepage.BluetoothHomeViewModel
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
    entryWithNavAndVm<BluetoothHomeRoute, BluetoothHomeViewModel> { _, navigator, viewModel ->
        BluetoothHomeRoute(
            viewModel = viewModel,
            navToBtBonded = { navigator.navigate(BluetoothBondedRoute) },
            navToBtDiscovery = {
                navigator.navigate(BluetoothDiscoveryRoute(it))
            }
        )
    }
    entryWithNavAndVm<BluetoothDiscoveryRoute, BluetoothDiscoveryViewModel> { it, navigator, viewModel ->
        BluetoothDiscoveryRoute(it.localDeviceName, viewModel) { navigator.goBack() }
    }
    entryWithNavAndVm<BluetoothBondedRoute, BluetoothBondedViewModel> { _, navigator, viewModel ->
        BluetoothBondedRoute(viewModel) { navigator.navigate(BtDeviceDetailRoute(it)) }
    }
    entry<BtDeviceDetailRoute> { key ->
        val viewModel = hiltViewModel<BtDeviceDetailViewModel, BtDeviceDetailViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(key.address)
            }
        )
        val navigator = LocalNavigator.current
        BtDeviceDetailRoute(viewModel) {
            navigator.goBack()
        }
    }
}

