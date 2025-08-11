package com.lunacattus.app.player.routes.main.playList

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lunacattus.app.base.compose.components.SwipeToRevealItem
import com.lunacattus.app.base.compose.components.overScrollVertical
import com.lunacattus.app.base.compose.extensions.clickableWithDebounce
import com.lunacattus.app.domain.model.VideoType
import com.lunacattus.app.player.MainActivity
import com.lunacattus.app.player.R
import com.lunacattus.app.player.routes.main.playList.mvi.PlayListUiIntent
import com.lunacattus.app.player.routes.main.playList.mvi.PlayListUiState
import com.lunacattus.app.player.routes.player.mvi.MediaItems
import com.lunacattus.app.player.routes.player.mvi.PlayerViewModel
import com.lunacattus.app.player.theme.AppTheme

@Composable
fun PlayListScreen(
    modifier: Modifier = Modifier,
    uiState: PlayListUiState,
    sendUiIntent: (PlayListUiIntent) -> Unit,
    navToPlayer: () -> Unit
) {

    LaunchedEffect(Unit) {
        sendUiIntent(PlayListUiIntent.Init)
    }
    val context = LocalContext.current
    val activity = remember { context as MainActivity }
    val playerViewModel = hiltViewModel<PlayerViewModel>(activity)

    val playList = when (uiState) {
        is PlayListUiState.Fail,
        PlayListUiState.Init,
        PlayListUiState.Loading -> emptyList()

        is PlayListUiState.Success -> {
            uiState.playList
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .overScrollVertical(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 100.dp, start = 10.dp, end = 10.dp, bottom = 90.dp)
    ) {
        val mediaList = playList.map { video ->
            MediaItem.Builder().apply {
                setMediaId(video.id)
                setUri(video.uri)
                val mediaMetadata = MediaMetadata.Builder().apply {
                    setTitle(video.title)
                    setDisplayTitle(video.title)
                    setArtist(video.subtitle)
                    setArtworkUri(video.coverPic.toUri())
                    setDescription(video.description)
                }.build()
                setMediaMetadata(mediaMetadata)
            }.build()
        }
        itemsIndexed(
            items = playList,
            key = { _, video -> video.id }) { index, video ->
            SwipeToRevealItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                revealContent = { close ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray)
                            .clickableWithDebounce {
                                sendUiIntent(PlayListUiIntent.RemoveVideo(video))
                                close.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircleOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .clickableWithDebounce {
                            navToPlayer.invoke()
                            playerViewModel.setPlayList(
                                MediaItems(mediaList, index, types = playList.map { it.type })
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = when (video.type) {
                            VideoType.JsonFile -> painterResource(R.drawable.ic_json_media)
                            VideoType.Unknow -> painterResource(R.drawable.ic_unknow)
                            VideoType.WebStream -> painterResource(R.drawable.ic_web_video)
                            VideoType.LocalVideo -> painterResource(R.drawable.ic_local_video)
                        },
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = video.title, fontSize = 18.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (video.description != "") {
                            Text(
                                text = video.description,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .height(0.5.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray)
            )
        }
    }
}