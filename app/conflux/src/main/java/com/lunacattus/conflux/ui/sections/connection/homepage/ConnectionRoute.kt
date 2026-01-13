package com.lunacattus.conflux.ui.sections.connection.homepage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ConnectionRoute(model: ConnectionViewModel) {
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