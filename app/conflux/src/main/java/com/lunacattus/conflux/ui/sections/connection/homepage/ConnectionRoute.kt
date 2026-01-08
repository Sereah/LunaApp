package com.lunacattus.conflux.ui.sections.connection.homepage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lunacattus.conflux.ui.LocalSetTopBarTitle
import com.lunacattus.conflux.ui.TopBarTitle

@Composable
fun ConnectionRoute(model: ConnectionViewModel) {
    LocalSetTopBarTitle.current.invoke(TopBarTitle("连接"))
    ConnectionScreen()
}

@Composable
fun ConnectionScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("连接")
    }
}