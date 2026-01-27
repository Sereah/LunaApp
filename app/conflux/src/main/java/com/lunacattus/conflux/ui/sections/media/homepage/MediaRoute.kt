package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.SpatialAudioOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.conflux.ui.base.CardLockState
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
import com.lunacattus.ui_design.compose.overScrollVertical
import kotlinx.coroutines.launch

@Composable
fun MediaRoute(
    viewModel: MediaViewModel,
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sendUiIntent = viewModel::handleUiIntent
    MediaScreen(
        uiState, navToAsrScreen, navToTTSScreen,
        unLockVoiceBasicFeature = { sendUiIntent(MediaHomeUiIntent.InitVoiceBasic) },
        switchRecord = { sendUiIntent(MediaHomeUiIntent.SwitchRecord(it)) },
        navToMediaFilesScreen = navToMediaFilesScreen
    )
}

@Composable
fun MediaScreen(
    uiState: MediaHomeUiState,
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
    unLockVoiceBasicFeature: () -> Unit,
    switchRecord: (isRecord: Boolean) -> Unit,
    navToMediaFilesScreen: (type: MediaSourceType) -> Unit
) {

    val scope = rememberCoroutineScope()

    val voiceBasicItems = listOf(
        NavigationItem(
            title = stringResource(R.string.asr),
            icon = Icons.Rounded.SpatialAudioOff,
            onClick = navToAsrScreen
        ),
        NavigationItem(
            title = stringResource(R.string.tts),
            icon = Icons.Rounded.SettingsVoice,
            onClick = navToTTSScreen
        ),
    )
    val toastText = if (uiState.voiceBasicInitState == VoiceBasicState.Authed) {
        stringResource(R.string.speech_init_success)
    } else {
        stringResource(id= R.string.speech_init_fail, uiState.initMsg)
    }
    var voiceBasicCardLockState by remember { mutableStateOf(CardLockState.Lock) }
    var lastVoiceState by remember { mutableStateOf(uiState.voiceBasicInitState) }
    LaunchedEffect(uiState.voiceBasicInitState) {
        voiceBasicCardLockState = when (uiState.voiceBasicInitState) {
            VoiceBasicState.UnAuth -> CardLockState.Lock
            VoiceBasicState.Authing -> CardLockState.UnLocking
            VoiceBasicState.Authed -> CardLockState.UnLock
        }
        if (lastVoiceState == VoiceBasicState.Authing && uiState.voiceBasicInitState != VoiceBasicState.Authing) {
            ActivityToastEvent.send(
                ToastEvent.ShowToast(toastText)
            )
        }
        lastVoiceState = uiState.voiceBasicInitState
    }

    val mediaUtilItems = listOf(
        SwitchItem(
            title = stringResource(R.string.record),
            summary = if (uiState.isRecord) stringResource(R.string.recording) else null,
            icon = Icons.Rounded.KeyboardVoice,
            checked = uiState.isRecord,
            onCheckedChange = switchRecord
        ),
        NavigationItem(
            title = stringResource(R.string.open_record_file),
            icon = Icons.Rounded.KeyboardVoice,
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
            icon = Icons.Rounded.MusicNote,
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
            ItemCard(voiceBasicItems, stringResource(R.string.speech_basic_skill), lockState = voiceBasicCardLockState) {
                unLockVoiceBasicFeature()
            }
        }

        item {
            ItemCard(mediaUtilItems, stringResource(R.string.record_skill))
        }

        item {
            ItemCard(mediaFilesItems, stringResource(R.string.media_file))
        }
    }
}