package com.lunacattus.nav3test.ui.section.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid

data class BtLocalInfo(
    val profiles: String = "",
    val address: String = "",
    val name: String = "",
    val uuidList: List<String> = emptyList()
)

data class DiscoveryDevice(
    val device: BluetoothDevice,
    val isBonding: Boolean = false
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