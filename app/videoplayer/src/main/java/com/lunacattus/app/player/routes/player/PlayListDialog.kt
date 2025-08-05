package com.lunacattus.app.player.routes.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.MediaItem
import com.lunacattus.app.domain.model.VideoType
import com.lunacattus.app.player.common.components.MusicBars
import com.lunacattus.app.player.common.extensions.clickableWithDebounce
import com.lunacattus.app.player.theme.AppTheme
import com.lunacattus.app.player.R

@Composable
fun PlayListDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    playList: List<MediaItem>,
    currentPlayIndex: Int,
    onSelectItem: (Int) -> Unit,
    typeList: List<VideoType>
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0.2f)
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = Color(0xFF2A2A2A),
                contentColor = Color.White
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                itemsIndexed(items = playList) { index, mediaItem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.Companion.clickableWithDebounce {
                            onSelectItem(index)
                        }
                    ) {
                        Image(
                            painter = when (typeList[index]) {
                                VideoType.JsonFile -> painterResource(R.drawable.ic_json_media)
                                VideoType.Unknow -> painterResource(R.drawable.ic_unknow)
                                VideoType.WebStream -> painterResource(R.drawable.ic_web_video)
                                VideoType.LocalVideo -> painterResource(R.drawable.ic_local_video)
                            },
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (index == currentPlayIndex) {
                                    AppTheme.colors.primary
                                } else {
                                    Color.White
                                }
                            ),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mediaItem.mediaMetadata.title.toString(),
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (index == currentPlayIndex) {
                                    AppTheme.colors.primary
                                } else {
                                    Color.White
                                }
                            )
                            if (mediaItem.mediaMetadata.artist.toString() != "") {
                                Text(
                                    text = mediaItem.mediaMetadata.artist.toString(),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (index == currentPlayIndex) {
                                        AppTheme.colors.primary
                                    } else {
                                        Color.White
                                    }
                                )
                            }
                        }
                        if (index == currentPlayIndex) {
                            Spacer(modifier = Modifier.width(10.dp))
                            MusicBars(
                                modifier = Modifier.size(20.dp),
                                barCount = 4,
                                barColor = AppTheme.colors.primary,
                                barSpacing = 2.dp,
                                minHeightFraction = 0.2f,
                                cycleDurationMillis = 1000
                            )
                        }
                    }
                }
            }
        }
    }
}