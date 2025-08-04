package com.lunacattus.app.presentation.compose.routes.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.compose.PlayerSurface
import com.lunacattus.app.presentation.compose.MainActivity
import com.lunacattus.logger.Logger
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uri: String,
    title: String,
    modifier: Modifier = Modifier,
    navBack: () -> Unit,
) {
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var mediaItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0) }
    val context = LocalContext.current
    val activity = remember { context as? MainActivity }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    var isMediaReady by remember { mutableStateOf(false) }
    var isMediaPlaying by remember { mutableStateOf(false) }
    var playFraction by remember { mutableFloatStateOf(0f) }
    var bufferFraction by remember { mutableFloatStateOf(0f) }
    var isUserSlide by remember { mutableStateOf(false) }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var showListDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullScreen) {
        activity?.exitFullScreen()
        isFullScreen = false
    }

    BackHandler(enabled = !isFullScreen) {
        navBack()
    }

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
                Logger.d(TAG, "onVideoSizeChanged: ${videoSize.width}x${videoSize.height}")
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
            PlayerControlView(
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
                    exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    exoPlayer.seekTo((playFraction * exoPlayer.duration).toLong())
                    isUserSlide = false
                },
                duration = exoPlayer.duration.coerceAtLeast(0),
                currentDuration = exoPlayer.currentPosition,
                hasNextMediaItem = exoPlayer.hasNextMediaItem(),
                hasPreMediaItem = exoPlayer.hasPreviousMediaItem(),
                onNextClick = {
                    if (exoPlayer.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                        exoPlayer.seekToNextMediaItem()
                    }
                },
                onPreClick = {
                    if (exoPlayer.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)) {
                        exoPlayer.seekToPreviousMediaItem()
                    }
                },
                onSeekBackClick = {
                    exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    exoPlayer.seekTo(
                        (exoPlayer.currentPosition - 5 * 1000).coerceAtLeast(0)
                    )
                },
                onSeekForwardClick = {
                    exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                    exoPlayer.seekTo(
                        (exoPlayer.currentPosition + 15 * 1000)
                            .coerceAtMost(exoPlayer.duration)
                    )
                },
                isFullScreen = isFullScreen,
                onFullScreenClick = {
                    if (isFullScreen) {
                        activity?.exitFullScreen()
                        isFullScreen = false
                    } else {
                        activity?.enterFullScreen()
                        isFullScreen = true
                    }
                },
                onPlayListClick = {
                    showListDialog = !showListDialog
                }
            )
        }
    }

    if (showListDialog) {
        PlayListDialog(
            modifier = Modifier
                .width(300.dp)
                .height(500.dp),
            onDismiss = { showListDialog = false },
            playList = if (exoPlayer.currentMediaItem == null) {
                emptyList()
            } else {
                listOf(exoPlayer.currentMediaItem!!)
            }
        )
    }
}

fun Activity.enterFullScreen() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.exitFullScreen() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}