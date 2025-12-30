package com.lunacattus.conflux.ui.base

import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.conflux.ui.theme.slideInFromLeft
import com.lunacattus.conflux.ui.theme.slideInFromRight
import com.lunacattus.conflux.ui.theme.slideOutFromLeft
import com.lunacattus.conflux.ui.theme.slideOutFromRight
import kotlinx.serialization.Serializable

interface BaseRoute : NavKey {
    val name: String
}

interface RootRoute : BaseRoute //根栈的路由分类

interface MainRoute : BaseRoute //嵌套NavDisplay的路由分类

@Serializable
data object MainGraph : NavKey //代表嵌套NavDisplay的路由放在根栈

val entryAnimate = NavDisplay.transitionSpec {
    slideInFromRight togetherWith slideOutFromLeft
} + NavDisplay.popTransitionSpec {
    slideInFromLeft togetherWith slideOutFromRight
}

//子页面默认右进左出动画，首页的route传递animated=false，来使用NavDisplay配置的动画
fun entryMetadata(
    animated: Boolean
) = if (animated) entryAnimate else emptyMap()

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithVm(
    animated: Boolean = true,
    crossinline content: @Composable (R, VM) -> Unit
) {
    entry<R>(metadata = entryMetadata(animated)) {
        content(it, hiltViewModel())
    }
}

inline fun <reified R : NavKey>
        EntryProviderScope<NavKey>.entryWithNav(
    animated: Boolean = true,
    crossinline content: @Composable (R, Navigator) -> Unit
) {
    entry<R>(metadata = entryMetadata(animated)) {
        content(it, LocalNavigator.current)
    }
}

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithNavAndVm(
    animated: Boolean = true,
    crossinline content: @Composable (R, Navigator, VM) -> Unit
) {
    entry<R>(metadata = entryMetadata(animated)) {
        content(it, LocalNavigator.current, hiltViewModel())
    }
}

inline fun <reified R : NavKey, reified VM : ViewModel>
        EntryProviderScope<NavKey>.entryWithNavAndVm(
    animated: Boolean = true,
    crossinline viewModelProvider: @Composable (R) -> VM,
    crossinline content: @Composable (R, Navigator, VM) -> Unit
) {
    entry<R>(metadata = entryMetadata(animated)) { key ->
        val navigator = LocalNavigator.current
        val vm = viewModelProvider(key)
        content(key, navigator, vm)
    }
}