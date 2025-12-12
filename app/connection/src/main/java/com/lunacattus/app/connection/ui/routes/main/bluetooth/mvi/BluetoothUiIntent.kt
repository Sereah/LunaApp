package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothDevice

sealed interface BluetoothUiIntent {
    data object SwitchEnable : BluetoothUiIntent
    data object LoadInfo : BluetoothUiIntent
    data class Discovery(val enable: Boolean) : BluetoothUiIntent
    data class PairNewDevice(val device: BluetoothDevice) : BluetoothUiIntent
    data object LoadBondedDevices : BluetoothUiIntent
}