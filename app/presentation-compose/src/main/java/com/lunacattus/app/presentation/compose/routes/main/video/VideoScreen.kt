package com.lunacattus.app.presentation.compose.routes.main.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.lunacattus.app.domain.model.Video
import com.lunacattus.app.presentation.compose.R
import com.lunacattus.app.presentation.compose.common.components.overScrollVertical
import com.lunacattus.app.presentation.compose.common.extensions.clickableWithDebounce
import com.lunacattus.app.presentation.compose.routes.main.video.mvi.VideoUiIntent
import com.lunacattus.app.presentation.compose.routes.main.video.mvi.VideoUiState
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    uiState: VideoUiState,
    sendUiIntent: (VideoUiIntent) -> Unit,
    navToPlayer: (Video) -> Unit
) {

    LaunchedEffect(Unit) {
        sendUiIntent(VideoUiIntent.Init)
    }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) } //todo skeleton
    var jsonVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
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
        itemsIndexed(items = jsonVideos) { index, video ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickableWithDebounce {
                        navToPlayer(video)
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.sources.first())
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .videoFrameMillis(0)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop
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