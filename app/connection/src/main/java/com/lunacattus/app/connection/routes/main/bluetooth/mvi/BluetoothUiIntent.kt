package com.lunacattus.app.connection.routes.main.bluetooth.mvi

sealed interface BluetoothUiIntent {
    data object GetBluetoothProfile: BluetoothUiIntent
    data object GetAddress: BluetoothUiIntent
    data object GetName: BluetoothUiIntent
}