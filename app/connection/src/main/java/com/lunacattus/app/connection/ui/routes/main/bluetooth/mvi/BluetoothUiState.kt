package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid

data class BluetoothUiState(
    val loading: Boolean = false,
    val btState: Int = BluetoothAdapter.STATE_OFF,
    val discovery: Boolean = false,
    val discoveryDeviceList: List<DiscoveryDevice> = emptyList(),
    val bondedDeviceList: List<BondDevice> = emptyList(),
    val currentDetailDevice: BondDevice? = null,
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
    val connectType: BondDeviceConnectType,
    val uuidList: List<DeviceUUID> = emptyList()
)

enum class BondDeviceConnectType {
    Connecting, Connected, Disconnecting, Disconnected
}

data class DeviceUUID(
    val name: String = "UNKNOWN",
    val uuid: ParcelUuid,
) {
    override fun toString(): String {
        return "$name: $uuid"
    }
}

data class DiscoveryDevice(
    val device: BluetoothDevice,
    val isBonding: Boolean = false
)
