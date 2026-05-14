package com.lunacattus.conflux.ui.sections.media

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.RootRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesRoute
import com.lunacattus.conflux.ui.sections.media.files.MediaFilesViewModel
import com.lunacattus.conflux.ui.sections.media.homepage.MediaRoute
import com.lunacattus.conflux.ui.sections.media.homepage.MediaViewModel
import com.lunacattus.conflux.ui.sections.media.player.MediaPlayerRoute
import kotlinx.serialization.Serializable

@Serializable
data object MediaRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.media_title
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
data class MediaPlayerRoute(val uri: String) : RootRoute

fun EntryProviderScope<NavKey>.mediaSection() {
    entryWithNavAndVm<MediaRoute, MediaViewModel> { _, navigator, viewModel ->
        MediaRoute(
            viewModel = viewModel,
            navToMediaFilesScreen = {
                navigator.navigate(MediaFilesRoute(it))
            }
        )
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
                navigator.navigate(MediaPlayerRoute(uri = it.mediaItem.localConfiguration?.uri.toString()))
            })
    }
}

fun EntryProviderScope<NavKey>.mediaRootSection() {
    entry<MediaPlayerRoute> {
        MediaPlayerRoute(path = it.uri)
    }
}

@Serializable
sealed class MediaSourceType {

    @Serializable
    data object AppRecording : MediaSourceType()

    @Serializable
    data object SystemMusic : MediaSourceType()
}