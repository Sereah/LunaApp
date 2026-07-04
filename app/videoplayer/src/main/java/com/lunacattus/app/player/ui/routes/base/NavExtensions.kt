package com.lunacattus.app.player.ui.routes.base

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.lunacattus.app.player.ui.theme.slideInFromLeft
import com.lunacattus.app.player.ui.theme.slideInFromRight
import com.lunacattus.app.player.ui.theme.slideOutFromLeft
import com.lunacattus.app.player.ui.theme.slideOutFromRight

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.graphViewModel(
    navController: NavHostController,
    graphRoute: String
): VM {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(graphRoute)
    }
    return hiltViewModel(parentEntry)
}

fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = { slideInFromRight },
        exitTransition = { slideOutFromLeft },
        popEnterTransition = { slideInFromLeft },
        popExitTransition = { slideOutFromRight },
        content = content
    )
}