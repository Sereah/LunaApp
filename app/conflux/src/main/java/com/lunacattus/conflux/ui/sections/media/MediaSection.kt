package com.lunacattus.conflux.ui.sections.media

import android.os.Environment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.base.entryWithVm
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesRoute
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesViewModel
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.homepage.MediaViewModel
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrRoute
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MediaRoute : BaseRoute

@Serializable
data object AsrRoute : BaseRoute

@Serializable
data class MediaFilesRoute(val path: String) : BaseRoute

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNavAndVm<MediaRoute, MediaViewModel> { _, navigator, viewModel ->
        MediaRoute(
            viewModel = viewModel,
            navToAsrScreen = { navigator.navigate(AsrRoute) },
            navToTTSScreen = {},
            navToMediaFilesScreen = {
                navigator.navigate(MediaFilesRoute(Environment.DIRECTORY_RECORDINGS))
            }
        )
    }
    entryWithVm<AsrRoute, AsrViewModel> { _, viewmodel ->
        AsrRoute(viewmodel)
    }
    entry<MediaFilesRoute> {
        val viewModel = hiltViewModel<MediaFilesViewModel, MediaFilesViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(it.path)
            }
        )
        MediaFilesRoute(viewModel, it.path)
    }
}