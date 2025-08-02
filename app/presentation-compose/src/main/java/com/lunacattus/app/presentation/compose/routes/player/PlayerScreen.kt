package com.lunacattus.app.presentation.compose.routes.player

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import com.lunacattus.app.presentation.compose.common.components.VideoSlider
import com.lunacattus.logger.Logger
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
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
    var isMediaReady by remember { mutableStateOf(false) }
    var isMediaPlaying by remember { mutableStateOf(false) }
    var playFraction by remember { mutableFloatStateOf(0f) }
    var bufferFraction by remember { mutableFloatStateOf(0f) }
    var isUserSlide by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        exoPlayer.apply {
            val mediaItem = MediaItem.fromUri(uri.toUri())
            setMediaItem(mediaItem)
            seekTo(mediaItemIndex, currentPosition)
            prepare()
        }
    }

    LaunchedEffect(isMediaPlaying) {
        while (isMediaPlaying) {
            if (exoPlayer.duration > 0) {
                if (!isUserSlide) {
                    playFraction =
                        exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
                }
                bufferFraction = exoPlayer.bufferedPosition.toFloat() / exoPlayer.duration.toFloat()
            } else {
                playFraction = 0f
                bufferFraction = 0f
            }
            delay(1000)
        }
    }

    DisposableEffect(exoPlayer) {
        val playerListener = object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                Logger.d(TAG, "onVideoSizeChanged: $videoSize")
                videoAspectRatio = if (videoSize.height == 0) {
                    16f / 9f
                } else {
                    (videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Logger.d(TAG, "onIsPlayingChanged: $isPlaying")
                isMediaPlaying = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Logger.d(TAG, "onPlaybackStateChanged: $playbackState")
                isMediaReady = playbackState >= STATE_BUFFERING
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
            Logger.d(TAG, "LifecycleEventObserver: ${event.name}")
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
        if (isMediaReady) {
            MediaControlView(
                isMediaPlaying = isMediaPlaying,
                playFraction = playFraction,
                bufferFraction = bufferFraction,
                onPlayingClick = {
                    if (isMediaPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                onUserSlide = {
                    isUserSlide = true
                    playFraction = it
                },
                onUserSlideFinish = {
                    exoPlayer.seekTo((playFraction * exoPlayer.duration).toLong())
                    isUserSlide = false
                },
                duration = exoPlayer.duration.coerceAtLeast(0),
                currentDuration = exoPlayer.currentPosition
            )
        }
    }
}

@Composable
fun MediaControlView(
    isMediaPlaying: Boolean,
    playFraction: Float,
    bufferFraction: Float,
    onPlayingClick: () -> Unit,
    onUserSlide: (Float) -> Unit,
    onUserSlideFinish: () -> Unit,
    duration: Long,
    currentDuration: Long
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.Center)
            ) {
                Icon(
                    imageVector = if (isMediaPlaying) {
                        Icons.Rounded.PauseCircle
                    } else {
                        Icons.Rounded.PlayCircle
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onPlayingClick()
                            }
                        }
                )
            }
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                VideoSlider(
                    enable = isMediaPlaying,
                    playFraction = playFraction,
                    onPlayFractionChange = onUserSlide,
                    onPlayFractionChangeFinish = onUserSlideFinish,
                    bufferFraction = bufferFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(text = currentDuration.formatDuration())
                        Text(text = " - ")
                        Text(text = duration.formatDuration())
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures {

                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures {

                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}