package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

data class BluetoothUiState(
    val loading: Boolean = false,
    val btState: Int = BluetoothAdapter.STATE_OFF,
    val discovery: Boolean = false,
    val discoveryDeviceList: List<DiscoveryDevice> = emptyList(),
    val bondedDeviceList: List<BondDevice> = emptyList(),
    val info: BtInfo = BtInfo(),
)

data class BtInfo(
    val profiles: String = "",
    val address: String = "",
    val name: String = "",
    val uuidList: List<String> = emptyList()
)

data class BondDevice(
    val device: BluetoothDevice,
    val isConnected: Boolean,
    val connecting: Boolean = false,
    val disconnecting: Boolean = false
)

data class DiscoveryDevice(
    val device: BluetoothDevice,
    val isBonding: Boolean = false
)
