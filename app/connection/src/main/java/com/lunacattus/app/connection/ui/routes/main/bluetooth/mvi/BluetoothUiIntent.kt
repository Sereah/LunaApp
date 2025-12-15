package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

sealed interface BluetoothUiIntent {
    data object SwitchEnable : BluetoothUiIntent
    data object LoadInfo : BluetoothUiIntent
    data class Discovery(val enable: Boolean) : BluetoothUiIntent
    data class PairNewDevice(val device: DiscoveryDevice) : BluetoothUiIntent
    data object LoadBondedDevices : BluetoothUiIntent
    data class OnClickDeviceSetting(val device: BondDevice): BluetoothUiIntent
    data class ConnectDevice(val device: BondDevice): BluetoothUiIntent
    data class DisconnectDevice(val device: BondDevice): BluetoothUiIntent
    data class ForgetDevice(val device: BondDevice): BluetoothUiIntent
    data class RequestUuid(val device: BondDevice): BluetoothUiIntent
}