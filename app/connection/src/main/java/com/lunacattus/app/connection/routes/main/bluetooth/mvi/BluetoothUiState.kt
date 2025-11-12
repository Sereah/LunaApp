package com.lunacattus.app.connection.routes.main.bluetooth.mvi

data class BluetoothUiState(
    val loading: Boolean = false,
    val info: BtInfo = BtInfo(),
    val dialogItem: ItemData? = null
)

data class BtInfo(
    val profiles: String = "",
    val address: String = "",
    val name: String = "",
)

enum class ItemData(val title: String) {
    Profile("Profile"),
    Address("Address"),
    Name("Name")
}
