package com.lunacattus.app.presentation.compose

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lunacattus.app.presentation.compose.routes.enter.EnterRoute
import com.lunacattus.app.presentation.compose.routes.enter.enterRouter
import com.lunacattus.app.presentation.compose.routes.home.homeRouter
import com.lunacattus.app.presentation.compose.routes.home.navToHome
import com.lunacattus.app.presentation.compose.theme.slideInFromRight
import com.lunacattus.app.presentation.compose.theme.slideOutFromRight
import com.lunacattus.app.presentation.compose.theme.stayStillIn
import com.lunacattus.app.presentation.compose.theme.stayStillOut

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = EnterRoute,
        enterTransition = { slideInFromRight },
        exitTransition = { stayStillOut },
        popEnterTransition = { stayStillIn },
        popExitTransition = { slideOutFromRight }
    ) {
        enterRouter(
            navToMain = { navController.navToHome("Msg from enter.") }
        )
        homeRouter(navController)
    }
}