package com.lunacattus.app.connection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lunacattus.app.connection.routes.main.MainRoute
import com.lunacattus.app.connection.theme.AppTheme
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
                val rootNavController = rememberNavController()
                NavHost(
                    navController = rootNavController,
                    startDestination = "mainNav"
                ) {
                    composable(route = "mainNav") {
                        MainRoute(rootNavController)
                    }
                }
            }
        }
    }
}