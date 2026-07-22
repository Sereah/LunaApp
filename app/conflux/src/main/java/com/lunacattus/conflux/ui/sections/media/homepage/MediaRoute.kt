package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.common.utils.toDurationStringShort
import com.lunacattus.conflux.R
import com.lunacattus.conflux.permission.RationaleDialogConfig
import com.lunacattus.conflux.permission.SettingsDialogConfig
import com.lunacattus.conflux.permission.rememberPermissionState
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.conflux.ui.base.IconSource
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
import com.lunacattus.ui_design.compose.overScrollVertical
import com.lunacattus.ui_design.compose.section.ClassifyHeader
import com.lunacattus.ui_design.compose.section.SectionHeaderCard
import kotlinx.coroutines.launch

@Composable
fun MediaRoute(
    viewModel: MediaViewModel,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sendUiIntent = viewModel::handleUiIntent

    val recordPermission = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO,
        rationaleConfig = RationaleDialogConfig(
            title = "需要录音权限",
            message = "为了录制音频，我们需要使用你的麦克风功能。",
            confirmText = "继续授权",
            dismissText = "取消"
        ),
        settingsConfig = SettingsDialogConfig(
            title = "需要权限",
            message = "录音权限已被永久拒绝，请前往设置页面手动开启。",
            confirmText = "前往设置",
            dismissText = "取消"
        )
    )

    MediaScreen(
        uiState,
        switchRecord = { isRecord ->
            if (isRecord) {
                recordPermission.request {
                    sendUiIntent(MediaHomeUiIntent.SwitchRecord(true))
                }
            } else {
                sendUiIntent(MediaHomeUiIntent.SwitchRecord(false))
            }
        },
        navToMediaFilesScreen = navToMediaFilesScreen
    )
}

@Composable
private fun MediaScreen(
    uiState: MediaHomeUiState,
    switchRecord: (isRecord: Boolean) -> Unit,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {

    val scope = rememberCoroutineScope()
    val toast = stringResource(R.string.media_stop_recording_first)

    val mediaUtilItems = listOf(
        SwitchItem(
            title = stringResource(R.string.record),
            summary = if (uiState.isRecord) uiState.recordTimes.toDurationStringShort() else null,
            icon = IconSource.Vector(Icons.Rounded.KeyboardVoice),
            iconTint = MaterialTheme.colorScheme.tertiary,
            accentColor = MaterialTheme.colorScheme.primary,
            checked = uiState.isRecord,
            onCheckedChange = switchRecord
        ),
        NavigationItem(
            title = stringResource(R.string.open_record_file),
            icon = IconSource.Resource(R.drawable.ic_record_file),
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = {
                if (uiState.isRecord) {
                    scope.launch { ActivityToastEvent.send(ToastEvent.ShowToast(toast)) }
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
            iconTint = MaterialTheme.colorScheme.secondary,
            onClick = {
                navToMediaFilesScreen(MediaSourceType.SystemMusic)
            }
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 20.dp)
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {
            SectionHeaderCard(
                title = stringResource(R.string.media_title),
                subtitle = stringResource(R.string.media_subtitle),
                icon = Icons.Rounded.LocalMovies,
                gradientColors = listOf(
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                ),
                iconTint = MaterialTheme.colorScheme.tertiary,
                glowTint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            ClassifyHeader(stringResource(R.string.record))
            Spacer(Modifier.height(12.dp))
        }

        item {
            ItemCard(mediaUtilItems)
            Spacer(Modifier.height(20.dp))
        }

        item {
            ClassifyHeader(stringResource(R.string.media_music))
            Spacer(Modifier.height(12.dp))
        }

        item {
            ItemCard(mediaFilesItems)
        }
    }
}