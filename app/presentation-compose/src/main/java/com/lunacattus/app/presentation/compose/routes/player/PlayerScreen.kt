package com.lunacattus.app.presentation.compose.routes.player

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import com.lunacattus.logger.Logger

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    uri: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var mediaItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0) }
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    val playPauseState = rememberPlayPauseButtonState(exoPlayer)
    LaunchedEffect(exoPlayer) {
        playPauseState.observe()
    }
    val nextState = rememberNextButtonState(exoPlayer)
    val preState = rememberPreviousButtonState(exoPlayer)

    LaunchedEffect(uri) {
        exoPlayer.apply {
            val mediaItem = MediaItem.fromUri(uri.toUri())
            setMediaItem(mediaItem)
            seekTo(mediaItemIndex, currentPosition)
            prepare()
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        PlayerSurface(
            player = exoPlayer,
            modifier = modifier
                .aspectRatio(videoAspectRatio)
                .align(Alignment.Center)
        )
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalContentColor provides Color.White
            ) {
                if (playPauseState.isEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        preState.onClick()
                                    }
                                }
                        )
                        Icon(
                            imageVector = if (playPauseState.showPlay) {
                                Icons.Rounded.PlayCircle
                            } else {
                                Icons.Rounded.PauseCircle
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        playPauseState.onClick()
                                    }
                                }
                        )
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .weight(1f)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        nextState.onClick()
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}