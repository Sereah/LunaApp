package com.lunacattus.conflux.ui.sections.home.homepage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lunacattus.conflux.ui.LocalSetTopBarTitle
import com.lunacattus.conflux.ui.TopBarTitle

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    LocalSetTopBarTitle.current.invoke(TopBarTitle("首页"))
    HomeScreen()
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("首页")
    }
}