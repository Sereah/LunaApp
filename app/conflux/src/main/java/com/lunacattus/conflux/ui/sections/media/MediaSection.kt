package com.lunacattus.conflux.ui.sections.media

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.RootRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.base.entryWithVm
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesRoute
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesViewModel
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.homepage.MediaViewModel
import com.lunacattus.conflux.ui.sections.media.player.MediaPlayerRoute
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrRoute
import com.lunacattus.conflux.ui.sections.media.speech.asr.AsrViewModel
import com.lunacattus.conflux.ui.sections.media.speech.tts.TtsRoute
import com.lunacattus.conflux.ui.sections.media.speech.tts.TtsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MediaRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.media_title
}

@Serializable
data object AsrRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.asr_title
}

@Serializable
data object TtsRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.tts_title
}

@Serializable
data class MediaFilesRoute(val type: MediaSourceType) : MainRoute {
    override val titleResId: Int
        get() = when (type) {
            MediaSourceType.AppRecording -> R.string.record_title
            MediaSourceType.SystemMusic -> R.string.music_title
        }
}

@Serializable
data class MediaPlayerRoute(val path: String) : RootRoute

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNavAndVm<MediaRoute, MediaViewModel> { _, navigator, viewModel ->
        MediaRoute(
            viewModel = viewModel,
            navToAsrScreen = { navigator.navigate(AsrRoute) },
            navToTTSScreen = { navigator.navigate(TtsRoute) },
            navToMediaFilesScreen = {
                navigator.navigate(MediaFilesRoute(it))
            }
        )
    }
    entryWithVm<AsrRoute, AsrViewModel> { _, viewmodel ->
        AsrRoute(viewmodel)
    }
    entryWithVm<TtsRoute, TtsViewModel> { _, viewmodel ->
        TtsRoute(viewmodel)
    }
    entryWithNavAndVm<MediaFilesRoute, MediaFilesViewModel>(
        viewModelProvider = { route ->
            hiltViewModel<MediaFilesViewModel, MediaFilesViewModel.Factory>(
                creationCallback = {
                    it.create(route.type)
                }
            )
        }
    ) { route, navigator, viewModel ->
        MediaFilesRoute(
            viewModel, route.type,
            navToMediaPlayerScreen = {
                navigator.navigate(MediaPlayerRoute(path = it.file.absolutePath))
            })
    }
}

fun EntryProviderScope<NavKey>.mediaRootSection() {
    entry<MediaPlayerRoute> {
        MediaPlayerRoute(filePath = it.path)
    }
}

@Serializable
sealed class MediaSourceType {

    @Serializable
    data object AppRecording : MediaSourceType()

    @Serializable
    data object SystemMusic : MediaSourceType()
}