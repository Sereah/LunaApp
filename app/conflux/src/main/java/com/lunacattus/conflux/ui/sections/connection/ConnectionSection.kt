package com.lunacattus.conflux.ui.sections.connection

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionRoute
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ConnectionRoute : NavKey

fun EntryProviderScope<NavKey>.connectSection() {
    entryWithNavAndVm<ConnectionRoute, ConnectionViewModel> { _, navigator, model ->
        ConnectionRoute(model)
    }
}