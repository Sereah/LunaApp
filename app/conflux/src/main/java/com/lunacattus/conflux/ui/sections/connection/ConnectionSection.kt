package com.lunacattus.conflux.ui.sections.connection

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionRoute
import com.lunacattus.conflux.ui.sections.connection.homepage.ConnectionViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ConnectionRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.connection_title
}

fun EntryProviderScope<NavKey>.connectSection() {
    entryWithNavAndVm<ConnectionRoute, ConnectionViewModel> { _, _, _ ->
        ConnectionRoute()
    }
}