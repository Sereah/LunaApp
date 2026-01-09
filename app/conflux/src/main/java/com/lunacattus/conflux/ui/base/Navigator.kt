package com.lunacattus.conflux.ui.base

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.lunacattus.logger.Logger

class Navigator(
    private val innerState: NavigationState,
    private val rootBackStack: NavBackStack<NavKey>
) {

    companion object {
        const val TAG = "Navigator"
    }

    fun navigate(route: BaseRoute) {

        Logger.d(TAG, "navigate: $route, innerBackStack: ${innerState.backStacks[innerState.topLevelRoute]?.toList()}")
        innerState.lastRoute = innerState.currentRoute
        if (route in innerState.backStacks.keys) {
            innerState.topLevelRoute = route
            return
        }

        if (route is RootRoute) {
            rootBackStack.add(route)
            return
        }

        innerState.backStacks[innerState.topLevelRoute]?.add(route)
    }

    fun goBack() {
        Logger.d(TAG, "Start goBack, rootBackStack: ${rootBackStack.toList()}, " +
                "currentStack: ${innerState.backStacks[innerState.topLevelRoute]?.toList()}")
        innerState.lastRoute = innerState.currentRoute
        innerState.lastBackStackList = innerState.backStacks[innerState.topLevelRoute]?.toList() ?: emptyList()
        if (rootBackStack.size > 1) {
            rootBackStack.removeLastOrNull()
            return
        }

        val currentStack = innerState.backStacks[innerState.topLevelRoute]
            ?: error("Stack not found")
        val currentRoute = currentStack.last()

        if (currentRoute == innerState.topLevelRoute) {
            innerState.topLevelRoute = innerState.startRoute
        } else {
            currentStack.removeLastOrNull()
        }

        Logger.d(TAG, "Complete goBack, rootBackStack: ${rootBackStack.toList()}, " +
                "currentStack: ${innerState.backStacks[innerState.topLevelRoute]?.toList()}")
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator not provided")
}
