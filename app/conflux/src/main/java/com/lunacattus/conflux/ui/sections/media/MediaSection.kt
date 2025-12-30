package com.lunacattus.conflux.ui.sections.media

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNav
import com.lunacattus.conflux.ui.base.entryWithVm
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrRoute
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MediaRoute : BaseRoute {
    override val name: String
        get() = "多媒体"
}

@Serializable
data object AsrRoute : BaseRoute {
    override val name: String
        get() = "ASR识别"
}

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNav<MediaRoute>(animated = false) { _, navigator ->
        MediaRoute(
            navToAsrScreen = { navigator.navigate(AsrRoute) },
            navToTTSScreen = {}
        )
    }
    entryWithVm<AsrRoute, AsrViewModel> { _, viewmodel ->
        AsrRoute(viewmodel)
    }
}