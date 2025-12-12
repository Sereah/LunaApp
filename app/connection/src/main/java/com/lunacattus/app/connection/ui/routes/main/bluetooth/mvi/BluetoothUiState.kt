package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

data class BluetoothUiState(
    val loading: Boolean = false,
    val btState: Int = BluetoothAdapter.STATE_OFF,
    val discovery: Boolean = false,
    val discoveryDeviceList: List<BluetoothDevice> = emptyList(),
    val bondedDeviceList: List<BluetoothDevice> = emptyList(),
    val info: BtInfo = BtInfo(),
)

data class BtInfo(
    val profiles: String = "",
    val address: String = "",
    val name: String = "",
    val uuidList: List<String> = emptyList()
)
