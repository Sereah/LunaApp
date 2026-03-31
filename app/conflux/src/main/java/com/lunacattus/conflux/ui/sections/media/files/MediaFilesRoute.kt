package com.lunacattus.conflux.ui.sections.media.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lunacattus.common.utils.toFileSizeString
import com.lunacattus.common.utils.toSmartDateString
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
import com.lunacattus.ui_design.compose.clickableWithDebounce

@Composable
fun MediaFilesRoute(
    viewModel: MediaFilesViewModel,
    type: MediaSourceType,
    navToMediaPlayerScreen: (MediaFileItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MediaFilesScreen(
        uiState,
        selectFile = {
            viewModel.handleUiIntent(MediaFilesUiIntent.PlayMedia(it))
        },
        deleteFile = { viewModel.handleUiIntent(MediaFilesUiIntent.DeleteMedia(it)) })
}

@Composable
fun MediaFilesScreen(
    uiState: MediaFilesUiState,
    selectFile: (MediaFileItem) -> Unit,
    deleteFile: (MediaFileItem) -> Unit
) {
    AnimatedVisibility(
        visible = uiState.hasLoaded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentPadding = LocalInnerPadding.current,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(items = uiState.mediaFiles, key = { it.mediaItem.mediaId }) { mediaFile ->
                when {
                    mediaFile.isAudio -> {
                        MediaItem(
                            Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .animateItem(
                                    fadeInSpec = tween(durationMillis = 400),
                                    fadeOutSpec = tween(300),
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    )
                                ),
                            mediaFile,
                            onClick = { selectFile(mediaFile) },
                            onDelete = { deleteFile(mediaFile) }
                        )
                    }
                }
            }
        }
    }
    AnimatedVisibility(
        visible = uiState.isLoading || (uiState.hasLoaded && uiState.mediaFiles.isEmpty()),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val text = when {
            uiState.isLoading -> stringResource(R.string.loading)
            uiState.hasLoaded && uiState.mediaFiles.isEmpty() -> stringResource(R.string.empty)
            else -> ""
        }
        Box(contentAlignment = Alignment.Center) {
            Text(text)
        }
    }
}

@Composable
private fun MediaItem(
    modifier: Modifier,
    mediaFileItem: MediaFileItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.clickableWithDebounce { onClick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val artworkUri = mediaFileItem.mediaItem.mediaMetadata.artworkUri
            if (artworkUri != null) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mediaFileItem.mediaItem.mediaMetadata.title.toString(),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = "${mediaFileItem.size.toFileSizeString()} • ${
                    mediaFileItem.dateModified.toSmartDateString(
                        stringResource(R.string.today),
                        stringResource(R.string.yesterday),
                        stringResource(R.string.day_before_yesterday),
                    )
                }",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                shape = RoundedCornerShape(20.dp),
                onDismissRequest = { expanded = false }
            ) {
                if (mediaFileItem.isLocalPrivate) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.play)) },
                    onClick = {
                        expanded = false
                        onClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }

    }
}
