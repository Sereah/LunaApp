package com.lunacattus.nav3test.ui.section.wifi

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.nav3test.ui.base.MainRoute
import com.lunacattus.nav3test.ui.section.wifi.screen.WifiRoute
import kotlinx.serialization.Serializable

@Serializable
data object WifiRoute : MainRoute {
    override val name: String
        get() = "Wi-Fi"
}

fun EntryProviderScope<NavKey>.wifiSection() {
    entry<WifiRoute> {
        WifiRoute()
    }
}