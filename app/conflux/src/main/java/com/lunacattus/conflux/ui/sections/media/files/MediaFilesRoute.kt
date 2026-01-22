package com.lunacattus.conflux.ui.sections.media.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.common.util.toDurationStringShort
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.ui_design.compose.SwipeToRevealItem
import com.lunacattus.ui_design.compose.clickableWithDebounce

@Composable
fun MediaFilesRoute(
    viewModel: MediaFilesViewModel,
    path: String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MediaFilesScreen(
        uiState,
        selectFile = { viewModel.handleUiIntent(MediaFilesUiIntent.PlayMedia(it)) },
        deleteFile = { viewModel.handleUiIntent(MediaFilesUiIntent.DeleteMedia(it)) })
}

@Composable
fun MediaFilesScreen(
    uiState: MediaFilesUiState,
    selectFile: (MediaFile) -> Unit,
    deleteFile: (MediaFile) -> Unit
) {
    AnimatedVisibility(
        visible = uiState.hasLoaded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = LocalInnerPadding.current,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(items = uiState.mediaFiles, key = { it.file.name }) { mediaFile ->
                SwipeToRevealItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 20.dp)
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 400),
                            fadeOutSpec = tween(300),
                            placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                dampingRatio = Spring.DampingRatioLowBouncy
                            )
                        ),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    containerShape = RoundedCornerShape(20.dp),
                    foregroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    revealContent = {
                        Icon(
                            Icons.Rounded.Delete, contentDescription = "",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(32.dp)
                                .clickableWithDebounce {
                                    deleteFile(mediaFile)
                                    it()
                                }
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .clickableWithDebounce { selectFile(mediaFile) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
                            Icon(imageVector = Icons.Rounded.MusicNote, contentDescription = "")
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${mediaFile.file.name}", modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis, maxLines = 1
                            )
                            Text(text = mediaFile.duration.toDurationStringShort())
                        }
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