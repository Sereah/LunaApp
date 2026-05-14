package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.conflux.ui.base.IconSource
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
import com.lunacattus.ui_design.compose.overScrollVertical
import kotlinx.coroutines.launch

@Composable
fun MediaRoute(
    viewModel: MediaViewModel,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sendUiIntent = viewModel::handleUiIntent
    MediaScreen(
        uiState,
        switchRecord = { sendUiIntent(MediaHomeUiIntent.SwitchRecord(it)) },
        navToMediaFilesScreen = navToMediaFilesScreen
    )
}

@Composable
fun MediaScreen(
    uiState: MediaHomeUiState,
    switchRecord: (isRecord: Boolean) -> Unit,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {

    val scope = rememberCoroutineScope()

    val mediaUtilItems = listOf(
        SwitchItem(
            title = stringResource(R.string.record),
            summary = if (uiState.isRecord) stringResource(R.string.recording) else null,
            icon = IconSource.Vector(Icons.Rounded.KeyboardVoice),
            iconTint = if (uiState.isRecord) Color.Green else LocalContentColor.current,
            checked = uiState.isRecord,
            onCheckedChange = switchRecord
        ),
        NavigationItem(
            title = stringResource(R.string.open_record_file),
            icon = IconSource.Resource(R.drawable.ic_record_file),
            iconTint = LocalContentColor.current,
            onClick = {
                if (uiState.isRecord) {
                    scope.launch { ActivityToastEvent.send(ToastEvent.ShowToast("请先停止录制")) }
                } else {
                    navToMediaFilesScreen(MediaSourceType.AppRecording)
                }
            }
        )
    )

    val mediaFilesItems = listOf(
        NavigationItem(
            title = stringResource(R.string.open_music_file),
            icon = IconSource.Vector(Icons.Rounded.MusicNote),
            iconTint = LocalContentColor.current,
            onClick = {
                navToMediaFilesScreen(MediaSourceType.SystemMusic)
            }
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        item {
            ItemCard(mediaUtilItems, stringResource(R.string.record_skill))
        }

        item {
            ItemCard(mediaFilesItems, stringResource(R.string.media_file))
        }
    }
}