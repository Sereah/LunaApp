package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.SpatialAudioOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.CardLockState
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun MediaRoute(
    viewModel: MediaViewModel,
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
    navToMediaFilesScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sendUiIntent = viewModel::handleUiIntent
    MediaScreen(
        uiState, navToAsrScreen, navToTTSScreen,
        unLockVoiceBasicFeature = { sendUiIntent(MediaHomeUiIntent.InitVoiceBasic) },
        switchRecord = { sendUiIntent(MediaHomeUiIntent.SwitchRecord(it)) },
        openRecordingFile = navToMediaFilesScreen
    )
}

@Composable
fun MediaScreen(
    uiState: MediaHomeUiState,
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
    unLockVoiceBasicFeature: () -> Unit,
    switchRecord: (isRecord: Boolean) -> Unit,
    openRecordingFile: () -> Unit
) {

    val voiceBasicItems = listOf(
        NavigationItem(title = "ASR识别", icon = Icons.Rounded.SpatialAudioOff, onClick = navToAsrScreen),
        NavigationItem(title = "TTS合成", icon = Icons.Rounded.SettingsVoice, onClick = navToTTSScreen),
    )
    var voiceBasicCardLockState by remember { mutableStateOf(CardLockState.Lock) }
    LaunchedEffect(uiState.voiceBasicInitState) {
        voiceBasicCardLockState = when (uiState.voiceBasicInitState) {
            VoiceBasicState.UnAuth -> CardLockState.Lock
            VoiceBasicState.Authing -> CardLockState.UnLocking
            VoiceBasicState.Authed -> CardLockState.UnLock
        }
    }

    val mediaUtilItems = listOf(
        SwitchItem(
            title = "录制音频",
            summary = if (uiState.isRecord) "录制中" else null,
            icon = Icons.Rounded.KeyboardVoice,
            checked = uiState.isRecord,
            onCheckedChange = switchRecord
        ),
        NavigationItem(
            title = "打开录音文件",
            icon = Icons.Rounded.KeyboardVoice,
            onClick = openRecordingFile
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
            ItemCard(voiceBasicItems, "语音基础功能", lockState = voiceBasicCardLockState) {
                unLockVoiceBasicFeature()
            }
        }

        item {
            ItemCard(mediaUtilItems, "录音能力")
        }
    }
}