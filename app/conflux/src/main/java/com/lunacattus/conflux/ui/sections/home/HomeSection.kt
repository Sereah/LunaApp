package com.lunacattus.conflux.ui.sections.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.home.homepage.HomeRoute
import com.lunacattus.conflux.ui.sections.home.homepage.HomeViewModel
import com.lunacattus.conflux.ui.sections.root.TestRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.home_title
}

fun EntryProviderScope<NavKey>.homeSection() {
    entryWithNavAndVm<HomeRoute, HomeViewModel> { _, navigator, viewModel ->
        HomeRoute(viewModel) { navigator.navigate(TestRoute) }
    }
}

