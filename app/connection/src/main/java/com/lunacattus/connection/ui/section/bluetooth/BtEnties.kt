package com.lunacattus.connection.ui.section.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothUuid
import android.os.ParcelUuid

data class BtLocalInfo(
    val profiles: List<String> = emptyList(),
    val address: String = "",
    val name: String = "",
    val uuidList: List<DeviceUUID> = emptyList()
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
    val uuid: ParcelUuid = BluetoothUuid.BASS,
    val connectState: Int = BluetoothProfile.STATE_DISCONNECTED
) {
    override fun toString(): String {
        return "$name: $uuid"
    }
}