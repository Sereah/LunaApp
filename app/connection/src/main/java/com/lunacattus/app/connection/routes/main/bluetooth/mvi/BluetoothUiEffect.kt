package com.lunacattus.app.connection.routes.main.bluetooth.mvi

sealed interface BluetoothUiEffect {
    data object Idle: BluetoothUiEffect
    data class Error(val error: String) : BluetoothUiEffect
}