package com.lunacattus.conflux.ui.sections.connection

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionRoute
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ConnectionRoute : BaseRoute {
    override val name: String
        get() = "连接"
}

fun EntryProviderScope<NavKey>.connectSection() {
    entryWithNavAndVm<ConnectionRoute, ConnectionViewModel>(animated = false) { _, navigator, model ->
        ConnectionRoute(model)
    }
}