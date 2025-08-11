package com.lunacattus.app.player.routes.player

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.app.base.compose.components.VideoSlider
import com.lunacattus.app.player.R

@Composable
fun PlayerControlView(
    mediaTitle: String,
    isMediaPlaying: Boolean,
    playFraction: Float,
    bufferFraction: Float,
    onPlayingClick: () -> Unit,
    onUserSlide: (Float) -> Unit,
    onUserSlideFinish: () -> Unit,
    duration: Long,
    currentDuration: Long,
    hasNextMediaItem: Boolean,
    hasPreMediaItem: Boolean,
    onNextClick: () -> Unit,
    onPreClick: () -> Unit,
    onSeekBackClick: () -> Unit,
    onSeekForwardClick: () -> Unit,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    onPlayListClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            Text(
                text = mediaTitle,
                fontSize = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.ic_back_5s),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onSeekBackClick()
                            }
                        }
                )
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
                Icon(
                    painter = painterResource(R.drawable.ic_foward_15s),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onSeekForwardClick()
                            }
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                VideoSlider(
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
                        Text(text = currentDuration.coerceAtLeast(0).formatDuration())
                        Text(text = " - ")
                        Text(text = duration.formatDuration())
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_play_list),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .weight(1f)
                            .align(Alignment.Top)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    onPlayListClick.invoke()
                                }
                            }
                    )
                    Row {
                        Icon(
                            imageVector = if (isFullScreen) {
                                Icons.Rounded.FullscreenExit
                            } else {
                                Icons.Rounded.Fullscreen
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        onFullScreenClick()
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = null,
                            tint = if (hasPreMediaItem) {
                                LocalContentColor.current
                            } else {
                                LocalContentColor.current.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        onPreClick()
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = null,
                            tint = if (hasNextMediaItem) {
                                LocalContentColor.current
                            } else {
                                LocalContentColor.current.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        onNextClick()
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