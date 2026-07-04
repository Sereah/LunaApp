package com.lunacattus.conflux.ui.base

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface RootRoute : NavKey //根栈的路由分类
interface MainRoute: NavKey {
    val titleResId: Int
}

@Serializable
data object Main : NavKey //代表嵌套NavDisplay的路由放在根栈

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithVm(
    metadata: Map<String, Any> = emptyMap(),
    crossinline content: @Composable (R, VM) -> Unit
) {
    entry<R>(metadata = metadata) {
        content(it, hiltViewModel())
    }
}

inline fun <reified R : NavKey>
        EntryProviderScope<NavKey>.entryWithNav(
    metadata: Map<String, Any> = emptyMap(),
    crossinline content: @Composable (R, Navigator) -> Unit
) {
    entry<R>(metadata = metadata) {
        content(it, LocalNavigator.current)
    }
}

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithNavAndVm(
    metadata: Map<String, Any> = emptyMap(),
    crossinline content: @Composable (R, Navigator, VM) -> Unit
) {
    entry<R>(metadata = metadata) {
        content(it, LocalNavigator.current, hiltViewModel())
    }
}

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithNavAndVm(
    metadata: Map<String, Any> = emptyMap(),
    crossinline viewModelProvider: @Composable (R) -> VM,
    crossinline content: @Composable (R, Navigator, VM) -> Unit
) {
    entry<R>(metadata = metadata) { key ->
        val navigator = LocalNavigator.current
        val vm = viewModelProvider(key)
        content(key, navigator, vm)
    }
}