package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

sealed interface BluetoothUiIntent {
    data class LoadItem(val item: ItemData) : BluetoothUiIntent
    data object DismissDialog : BluetoothUiIntent
}