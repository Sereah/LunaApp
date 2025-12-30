package com.lunacattus.conflux.ui.sections.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.home.homepage.HomeRoute
import com.lunacattus.conflux.ui.sections.home.homepage.HomeViewModel
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : BaseRoute {
    override val name: String
        get() = "首页"
}

fun EntryProviderScope<NavKey>.homeSection() {
    entryWithNavAndVm<HomeRoute, HomeViewModel>(animated = false) { _, navigator, viewModel ->
        HomeRoute(viewModel)
    }
}

