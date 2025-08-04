package com.lunacattus.app.presentation.compose.routes.main.playList

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
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.app.presentation.compose.common.components.SwipeToRevealItem
import com.lunacattus.app.presentation.compose.common.components.overScrollVertical
import com.lunacattus.app.presentation.compose.common.extensions.clickableWithDebounce
import com.lunacattus.app.presentation.compose.routes.main.playList.mvi.PlayListUiIntent
import com.lunacattus.app.presentation.compose.routes.main.playList.mvi.PlayListUiState
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun PlayListScreen(
    modifier: Modifier = Modifier,
    uiState: PlayListUiState,
    sendUiIntent: (PlayListUiIntent) -> Unit,
) {

    LaunchedEffect(Unit) {
        sendUiIntent(PlayListUiIntent.Init)
    }

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
                            // TODO:
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector = Icons.Rounded.VideoLibrary,
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
            if (video != playList.last()) {
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