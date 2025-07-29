package com.lunacattus.app.presentation.compose.routes.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiIntent
import com.lunacattus.app.presentation.compose.routes.home.mvi.HomeUiState
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    msgFromRoute: String,
    uiState: HomeUiState,
    sendUiIntent: (HomeUiIntent) -> Unit,
) {

    var text by remember { mutableStateOf("") }
    text = when (uiState) {
        is HomeUiState.Init -> "Init..."
        is HomeUiState.Fail -> uiState.msg
        HomeUiState.Loading -> "Loading..."
        is HomeUiState.Success -> uiState.msg
    }

    LaunchedEffect(Unit) {
        sendUiIntent(HomeUiIntent.Start)
    }

    Column(
        modifier = modifier
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = AppTheme.colors.mainText
            )
        ) {
            Text(
                text = "HomeScreen, $msgFromRoute",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp)
            )
            Text(
                text = "UiState: $text",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}