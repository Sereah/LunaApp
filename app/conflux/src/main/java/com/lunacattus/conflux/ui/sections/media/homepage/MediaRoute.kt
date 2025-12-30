package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.SpatialAudioOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun MediaRoute(
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
) {
    MediaScreen(navToAsrScreen, navToTTSScreen)
}

@Composable
fun MediaScreen(
    navToAsrScreen: () -> Unit,
    navToTTSScreen: () -> Unit,
) {

    val speechItems = listOf(
        NavigationItem(title = "ASR识别", icon = Icons.Rounded.SpatialAudioOff, onClick = navToAsrScreen),
        NavigationItem(title = "TTS合成", icon = Icons.Rounded.SettingsVoice, onClick = navToTTSScreen),
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            ItemCard(speechItems, "语音功能")
        }
    }
}