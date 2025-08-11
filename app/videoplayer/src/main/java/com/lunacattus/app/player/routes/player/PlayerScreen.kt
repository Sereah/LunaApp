package com.lunacattus.app.player.routes.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.compose.PlayerSurface
import com.lunacattus.app.base.compose.extensions.clickableWithDebounce
import com.lunacattus.app.player.MainActivity
import com.lunacattus.app.player.routes.player.mvi.PlayerViewModel
import com.lunacattus.app.player.setLightStatusBarIcons
import com.lunacattus.logger.Logger
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    navBack: () -> Unit,
) {
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = remember { context as MainActivity }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    val viewModel = hiltViewModel<PlayerViewModel>(viewModelStoreOwner = activity)
    val mediaItems = viewModel.mediaItems.collectAsStateWithLifecycle()
    var playState by remember { mutableIntStateOf(-1) }
    var isMediaPlaying by remember { mutableStateOf(false) }
    var playFraction by remember { mutableFloatStateOf(0f) }
    var bufferFraction by remember { mutableFloatStateOf(0f) }
    var isUserSlide by remember { mutableStateOf(false) }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var showListDialog by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(showControls, isMediaPlaying) {
        if (showControls && isMediaPlaying) {
            delay(10_000)
            showControls = false
        }
    }

    BackHandler(enabled = isFullScreen) {
        activity.exitFullScreen()
        isFullScreen = false
    }

    BackHandler(enabled = !isFullScreen) {
        navBack()
    }

    LaunchedEffect(mediaItems) {
        exoPlayer.apply {
            exoPlayer.setMediaItems(
                mediaItems.value.list,
                mediaItems.value.startIndex,
                mediaItems.value.startPosition
            )
            prepare()
            playWhenReady = true
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
        activity.setLightStatusBarIcons(false)
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
                playState = playbackState
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Logger.d(TAG, "onMediaItemTransition, item: $mediaItem, reason: $reason")
                playFraction = 0f
                bufferFraction = 0f
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Logger.d(TAG, "onMediaMetadataChanged, data: $mediaMetadata")
            }
        }
        exoPlayer.addListener(playerListener)

        onDispose {
            activity.setLightStatusBarIcons(true)
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
                }

                Lifecycle.Event.ON_RESUME -> {}
                Lifecycle.Event.ON_PAUSE -> {}
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    viewModel.updateMediaItems(
                        mediaItems.value.copy(
                            startIndex = exoPlayer.currentMediaItemIndex,
                            startPosition = exoPlayer.currentPosition
                        )
                    )
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
            .background(Color.Black)
            .clickableWithDebounce {
                showControls = !showControls
            },
    ) {
        PlayerSurface(
            player = exoPlayer,
            modifier = modifier
                .aspectRatio(videoAspectRatio)
                .align(Alignment.Center)
        )
        if (playState >= 2 && showControls) {
            PlayerControlView(
                mediaTitle = exoPlayer.currentMediaItem?.mediaMetadata?.title.toString(),
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
                        activity.exitFullScreen()
                        isFullScreen = false
                    } else {
                        activity.enterFullScreen()
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
            playList = mediaItems.value.list,
            typeList = mediaItems.value.types,
            currentPlayIndex = exoPlayer.currentMediaItemIndex,
            onSelectItem = { index ->
                exoPlayer.seekTo(index, 0L)
                showListDialog = false
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