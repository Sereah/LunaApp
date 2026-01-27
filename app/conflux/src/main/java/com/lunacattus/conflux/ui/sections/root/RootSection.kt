package com.lunacattus.conflux.ui.sections.root

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.RootRoute
import com.lunacattus.conflux.ui.sections.media.mediaRootSection
import kotlinx.serialization.Serializable

@Serializable
data object TestRoute : RootRoute

fun EntryProviderScope<NavKey>.rootSection() {
    mediaRootSection()
    entry<TestRoute> {
        TestRoute()
    }
}