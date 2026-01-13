package com.lunacattus.conflux.ui.sections.root

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.RootRoute
import kotlinx.serialization.Serializable

@Serializable
data object TestRoute : RootRoute

fun EntryProviderScope<NavKey>.rootSection() {
    entry<TestRoute> {
        TestRoute()
    }
}