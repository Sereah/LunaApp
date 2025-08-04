package com.lunacattus.app.presentation.compose.routes.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.MediaItem

@Composable
fun PlayListDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    playList: List<MediaItem>
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
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = playList) {
                    Text(text = "item...${it.mediaMetadata}")
                }
            }
        }
    }
}