package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lunacattus.app.domain.model.videoUri
import com.lunacattus.app.presentation.compose.common.components.CircleLoader
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiIntent
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    sendUiIntent: (HomeUiIntent) -> Unit,
) {

    LaunchedEffect(Unit) {
        sendUiIntent(HomeUiIntent.Start)
    }

    var showLoading by remember { mutableStateOf(true) }

    showLoading = when (uiState) {
        is HomeUiState.Success -> false
        else -> true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (showLoading) {
            CircleLoader(
                modifier = Modifier.size(40.dp).align(Alignment.Center),
                isVisible = true,
                color = Color(0xFF009688),
                secondColor = null
            )
        } else {
            Player(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                uri = videoUri
            )
        }
    }
}