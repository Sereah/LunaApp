package com.lunacattus.connection.ui.base

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface BaseRoute : NavKey {
    val name: String
}

interface RootRoute : BaseRoute //根栈的路由分类

interface MainRoute : BaseRoute //嵌套NavDisplay的路由分类

@Serializable
data object MainGraph: NavKey //代表嵌套NavDisplay的路由放在根栈

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithVm(
    crossinline content: @Composable (R, VM) -> Unit
) {
    entry<R> {
        content(it, hiltViewModel())
    }
}

inline fun <reified R : NavKey>
        EntryProviderScope<NavKey>.entryWithNav(
    crossinline content: @Composable (R, Navigator) -> Unit
) {
    entry<R> {
        content(it, LocalNavigator.current)
    }
}


inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithNavAndVm(
    crossinline content: @Composable (R, Navigator, VM) -> Unit
) {
    entry<R> {
        content(it, LocalNavigator.current, hiltViewModel())
    }
}