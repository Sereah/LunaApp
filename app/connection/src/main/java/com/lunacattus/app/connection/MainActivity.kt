package com.lunacattus.app.connection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lunacattus.app.connection.ui.ActivityEvent
import com.lunacattus.app.connection.ui.ActivitySideEffect
import com.lunacattus.app.connection.ui.routes.main.MainRoute
import com.lunacattus.app.connection.ui.theme.AppTheme
import com.lunacattus.ui_design.compose.dialog.OverlayToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            AppTheme {

                var toastEvent by remember { mutableStateOf<Pair<String, Long>?>(null) }

                LaunchedEffect(Unit) {
                    ActivitySideEffect.events.collect {
                        toastEvent = when (it) {
                            is ActivityEvent.LogError -> {
                                it.throwable.toString() to it.id
                            }

                            is ActivityEvent.ShowToast -> {
                                it.message to it.id
                            }
                        }
                    }
                }

                val rootNavController = rememberNavController()
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background),
                    navController = rootNavController,
                    startDestination = "mainNav"
                ) {
                    composable(route = "mainNav") {
                        MainRoute(rootNavController)
                    }
                }

                OverlayToast(
                    modifier = Modifier.fillMaxSize(),
                    toastEvent
                )
            }
        }
    }
}