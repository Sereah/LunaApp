package com.lunacattus.conflux.ui.sections.media

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.homepage.MediaViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MediaRoute : BaseRoute {
    override val name: String
        get() = "多媒体"
}

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNavAndVm<MediaRoute, MediaViewModel> { _, navigator, model ->
        MediaRoute(model)
    }
}