package com.lunacattus.conflux.ui.base

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.lunacattus.logger.Logger

class Navigator(
    private val mainNavState: NavigationState,
    private val rootBackStack: NavBackStack<NavKey>
) {

    companion object {
        const val TAG = "Navigator"
    }

    fun navigate(route: NavKey) {
        Logger.d(
            TAG, "start navigate to $route, currentRoute: ${mainNavState.currentRoute}, currentBackStack: ${mainNavState.currentBackStack.toList()}"
        )
        mainNavState.performNavigation {
            when (route) {
                in mainNavState.backStacks.keys -> {
                    mainNavState.topLevelRoute = route
                }

                is RootRoute -> {
                    rootBackStack.add(route)
                }

                else -> {
                    mainNavState.backStacks[mainNavState.topLevelRoute]?.add(route)
                }
            }
        }
        Logger.d(
            TAG, "complete navigate, currentRoute: ${mainNavState.currentRoute}, currentBackStack: ${mainNavState.currentBackStack.toList()}"
        )
    }

    fun goBack() {
        Logger.d(
            TAG, "Start goBack, currentRoute: ${mainNavState.currentRoute}, currentBackStack: ${mainNavState.currentBackStack.toList()}"
        )
        mainNavState.performNavigation {
            when {
                rootBackStack.size > 1 -> {
                    rootBackStack.removeLastOrNull()
                }

                mainNavState.currentRoute == mainNavState.topLevelRoute -> {
                    mainNavState.topLevelRoute = mainNavState.startRoute
                }

                else -> {
                    mainNavState.currentBackStack.removeLastOrNull()
                }
            }
        }

        Logger.d(
            TAG, "Complete goBack, currentRoute: ${mainNavState.currentRoute}, currentBackStack: ${mainNavState.currentBackStack.toList()}"
        )
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator not provided")
}
