package com.lunacattus.app.presentation.compose.routes.player

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lunacattus.app.presentation.compose.routes.base.NavRoute
import com.lunacattus.app.presentation.compose.theme.slideInFromRight
import com.lunacattus.app.presentation.compose.theme.slideOutFromRight
import com.lunacattus.app.presentation.compose.theme.stayStillIn
import com.lunacattus.app.presentation.compose.theme.stayStillOut
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data object PlayerRoute : NavRoute {
    const val ARG_URI: String = "URI"
    const val ARG_TITLE: String = "TITLE"
    private const val NAME = "PlayerRoute"
    override val route: String
        get() = "$NAME/{$ARG_URI}/{$ARG_TITLE}"

    fun createRoute(uri: String, title: String) = "$NAME/$uri/$title"
}

fun NavHostController.navToPlayer(uri: String, title: String) {
    val uri = URLEncoder.encode(uri, StandardCharsets.UTF_8.toString())
    navigate(PlayerRoute.createRoute(uri, title))
}

fun NavGraphBuilder.playerRouter() {
    composable(
        route = PlayerRoute.route,
        arguments = listOf(
            navArgument(PlayerRoute.ARG_URI) {
                type = NavType.StringType
            },
            navArgument(PlayerRoute.ARG_TITLE) {
                type = NavType.StringType
            },
        ),
        enterTransition = { slideInFromRight },
        exitTransition = { stayStillOut },
        popEnterTransition = { stayStillIn },
        popExitTransition = { slideOutFromRight }
    ) {
        val uri = it.arguments?.getString(PlayerRoute.ARG_URI) ?: ""
        val title = it.arguments?.getString(PlayerRoute.ARG_TITLE) ?: ""
        PlayerScreen(uri, title)
    }
}



