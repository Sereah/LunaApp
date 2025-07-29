package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.lunacattus.logger.Logger

val playerListener = object : Player.Listener {
    override fun onEvents(
        player: Player,
        events: Player.Events
    ) {

    }
}

@Composable
fun Player(
    modifier: Modifier = Modifier,
    uri: String
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    var mediaItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0) }

    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(uri.toUri())
            setMediaItem(mediaItem)
            Logger.d(message = "mediaItemIndex: $mediaItemIndex, currentPosition: $currentPosition")
            seekTo(mediaItemIndex, currentPosition)
            prepare()
            playWhenReady = true
            addListener(playerListener)
        }
    }

    DisposableEffect(lifecycleOwner) {

        val observe = LifecycleEventObserver { owner, event ->
            Logger.d(message = "Life: ${event.name}")
            when (event) {
                Lifecycle.Event.ON_CREATE -> {}

                Lifecycle.Event.ON_START -> {
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {}
                Lifecycle.Event.ON_PAUSE -> {}
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    mediaItemIndex = exoPlayer.currentMediaItemIndex
                    currentPosition = exoPlayer.currentPosition
                }

                Lifecycle.Event.ON_ANY -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer = observe)

        onDispose {
            exoPlayer.apply {
                removeListener(playerListener)
                release()
            }
            lifecycleOwner.lifecycle.removeObserver(observe)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
            }
        }
    )
}