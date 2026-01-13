package com.lunacattus.conflux.ui.sections.media

import android.os.Environment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.base.entryWithVm
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesRoute
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesViewModel
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.homepage.MediaViewModel
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
data class MediaFilesRoute(val path: String) : MainRoute {
    override val titleResId: Int
        get() = when (path) {
            Environment.DIRECTORY_RECORDINGS -> R.string.record_title
            Environment.DIRECTORY_MUSIC -> R.string.music_title
            else -> R.string.files_title
        }
}

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNavAndVm<MediaRoute, MediaViewModel> { _, navigator, viewModel ->
        MediaRoute(
            viewModel = viewModel,
            navToAsrScreen = { navigator.navigate(AsrRoute) },
            navToTTSScreen = { navigator.navigate(TtsRoute) },
            navToMediaFilesScreen = {
                navigator.navigate(MediaFilesRoute(Environment.DIRECTORY_RECORDINGS))
            }
        )
    }
    entryWithVm<AsrRoute, AsrViewModel>(
        metadata = mapOf("title" to "ASR识别")
    ) { _, viewmodel ->
        AsrRoute(viewmodel)
    }
    entryWithVm<TtsRoute, TtsViewModel> { _, viewmodel ->
        TtsRoute(viewmodel)
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