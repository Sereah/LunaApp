package com.lunacattus.app.connection.routes.main.bluetooth.mvi

data class BluetoothUiState(
    val loading: Boolean = false,
    val profiles: String = "",
    val address: String = "",
    val name: String = ""
)
