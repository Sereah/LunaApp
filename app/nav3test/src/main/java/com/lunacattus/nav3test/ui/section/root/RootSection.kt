package com.lunacattus.nav3test.ui.section.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.nav3test.ui.base.LocalNavigator
import com.lunacattus.nav3test.ui.base.RootRoute
import kotlinx.serialization.Serializable

@Serializable
data object FullScreenDetail : RootRoute

fun EntryProviderScope<NavKey>.rootSection() {
    entry<FullScreenDetail> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<FullScreenDetailViewModel>()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("I am Full Screen!", color = Color.White)
                Button(onClick = { navigator.goBack() }) {
                    Text("Go Back")
                }
            }
        }
    }
}