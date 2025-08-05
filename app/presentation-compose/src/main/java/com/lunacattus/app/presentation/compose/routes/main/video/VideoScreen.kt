package com.lunacattus.app.presentation.compose.routes.main.video

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
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lunacattus.app.domain.model.JsonVideo
import com.lunacattus.app.domain.model.VideoType
import com.lunacattus.app.presentation.compose.MainActivity
import com.lunacattus.app.presentation.compose.R
import com.lunacattus.app.presentation.compose.common.components.SwipeToRevealItem
import com.lunacattus.app.presentation.compose.common.components.overScrollVertical
import com.lunacattus.app.presentation.compose.common.extensions.clickableWithDebounce
import com.lunacattus.app.presentation.compose.routes.main.video.mvi.VideoUiIntent
import com.lunacattus.app.presentation.compose.routes.main.video.mvi.VideoUiState
import com.lunacattus.app.presentation.compose.routes.player.mvi.MediaItems
import com.lunacattus.app.presentation.compose.routes.player.mvi.PlayerViewModel
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    uiState: VideoUiState,
    sendUiIntent: (VideoUiIntent) -> Unit,
    navToPlayer: (JsonVideo) -> Unit
) {

    LaunchedEffect(Unit) {
        sendUiIntent(VideoUiIntent.Init)
    }
    val context = LocalContext.current
    val activity = remember { context as MainActivity }
    val playerViewModel = hiltViewModel<PlayerViewModel>(viewModelStoreOwner = activity)
    var isLoading by remember { mutableStateOf(true) } //todo skeleton
    var jsonVideos by remember { mutableStateOf<List<JsonVideo>>(emptyList()) }
    when (uiState) {
        is VideoUiState.Fail -> {}
        VideoUiState.Init,
        VideoUiState.Loading -> {
            isLoading = true
        }

        is VideoUiState.Success -> {
            isLoading = false
            jsonVideos = uiState.jsonVideo
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
        itemsIndexed(
            items = jsonVideos,
            key = { _, video -> video.title }) { index, video ->
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
                                sendUiIntent(VideoUiIntent.AddToPlayList(video))
                                close.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .clickableWithDebounce {
                            navToPlayer(video)
                            val mediaItem = MediaItem.Builder().apply {
                                setUri(video.sources.first())
                                val mediaMetadata = MediaMetadata.Builder().apply {
                                    setDisplayTitle(video.title)
                                    setTitle(video.title)
                                    setArtist(video.subtitle)
                                    setDescription(video.description)
                                }.build()
                                setMediaMetadata(mediaMetadata)
                            }.build()
                            playerViewModel.setPlayList(
                                MediaItems(
                                    list = listOf(mediaItem),
                                    types = listOf(VideoType.JsonFile)
                                )
                            )
                        },
                ) {
//                    AsyncImage(
//                        model = ImageRequest.Builder(context)
//                            .data(video.sources.first())
//                            .decoderFactory(VideoFrameDecoder.Factory())
//                            .videoFrameMillis(0)
//                            .crossfade(true)
//                            .build(),
//                        placeholder = painterResource(R.drawable.logo),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(80.dp)
//                            .clip(RoundedCornerShape(5.dp)),
//                        contentScale = ContentScale.Crop
//                    )
                    Image(
                        painter = painterResource(R.drawable.ic_json_media),
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
                        Text(text = video.title, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = video.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (video != jsonVideos.last()) {
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
}