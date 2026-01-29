package com.lunacattus.conflux.domain.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val sessionToken = SessionToken(
        context,
        ComponentName(context, PlaybackService::class.java)
    )

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    init {
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            _controller.value = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    fun play(item: MediaItem) {
        _controller.value?.let { player ->
            player.setMediaItem(item)
            player.prepare()
            player.play()
        }
    }

    fun togglePlayPause() {
        _controller.value?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }
}