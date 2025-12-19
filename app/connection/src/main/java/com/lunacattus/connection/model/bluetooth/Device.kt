package com.lunacattus.connection.model.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid

data class BtLocalInfo(
    val profiles: List<String> = emptyList(),
    val address: String = "",
    val name: String = "",
    val uuidList: List<ParcelUuid> = emptyList()
)

data class DiscoveryDevice(
    val device: BluetoothDevice,
    val isBonding: Boolean = false
)

data class BondDevice(
    val device: BluetoothDevice,
    val connectType: BondDeviceConnectType,
    val uuidList: List<ParcelUuid> = emptyList()
)

enum class BondDeviceConnectType {
    Connecting, Connected, Disconnecting, Disconnected
}