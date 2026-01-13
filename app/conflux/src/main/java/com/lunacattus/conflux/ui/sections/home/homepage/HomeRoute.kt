package com.lunacattus.conflux.ui.sections.home.homepage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lunacattus.ui_design.compose.clickableWithDebounce

@Composable
fun HomeRoute(viewModel: HomeViewModel, navToRoot: () -> Unit) {
    HomeScreen(navToRoot)
}

@Composable
fun HomeScreen(navToRoot: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("首页", modifier = Modifier.clickableWithDebounce {
            navToRoot.invoke()
        })
    }
}