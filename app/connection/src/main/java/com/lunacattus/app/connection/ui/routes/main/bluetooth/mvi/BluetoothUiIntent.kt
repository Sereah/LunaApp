package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothDevice

sealed interface BluetoothUiIntent {
    data object SwitchEnable : BluetoothUiIntent
    data object LoadInfo : BluetoothUiIntent
    data class Discovery(val enable: Boolean) : BluetoothUiIntent
    data class PairNewDevice(val device: BluetoothDevice) : BluetoothUiIntent
    data object LoadBondedDevices : BluetoothUiIntent
    data class OnClickDeviceSetting(val device: BondDevice): BluetoothUiIntent
    data class ConnectDevice(val device: BondDevice): BluetoothUiIntent
    data class DisconnectDevice(val device: BondDevice): BluetoothUiIntent
    data class ForgetDevice(val device: BondDevice): BluetoothUiIntent
}