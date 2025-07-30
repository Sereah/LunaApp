package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import com.lunacattus.logger.Logger

@Composable
fun Player(
    modifier: Modifier = Modifier,
    uri: String
) {

    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var mediaItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0) }

    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(uri) {
        exoPlayer.apply {
            val mediaItem = MediaItem.fromUri(uri.toUri())
            setMediaItem(mediaItem)
            seekTo(mediaItemIndex, currentPosition)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {

        val playerListener = object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                videoAspectRatio = if (videoSize.height == 0) {
                    16f / 9f
                } else {
                    (videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height
                }
            }
        }
        exoPlayer.addListener(playerListener)

        onDispose {
            exoPlayer.apply {
                removeListener(playerListener)
                release()
            }
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
            lifecycleOwner.lifecycle.removeObserver(observe)
        }
    }

    PlayerSurface(
        player = exoPlayer,
        modifier = modifier.aspectRatio(videoAspectRatio)
    )
}