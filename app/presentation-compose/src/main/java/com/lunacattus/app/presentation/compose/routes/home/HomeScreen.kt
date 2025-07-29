package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lunacattus.app.domain.model.videoUri
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiIntent
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    sendUiIntent: (HomeUiIntent) -> Unit,
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF8BC34A))
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFF2196F3))
                .align(Alignment.Center)
        ) {
            Player(
                modifier = Modifier.align(Alignment.Center),
                uri = videoUri
            )
        }
    }
}