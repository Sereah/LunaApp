package com.lunacattus.app.connection.routes.main.bluetooth.mvi

sealed interface BluetoothSideEffect {
    data class ShowToast(val msg: String) : BluetoothSideEffect
}