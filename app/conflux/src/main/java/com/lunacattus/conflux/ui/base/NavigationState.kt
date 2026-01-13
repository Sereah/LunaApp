package com.lunacattus.conflux.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * 创建并记住一个 [NavigationState] 实例。
 * 这个 Composable 函数是管理应用导航状态的核心。
 *
 * @param startRoute 导航的起始路由或主屏幕的 [NavKey]。
 * @param topLevelRoutesKey 一组代表应用中主要部分（通常是底部导航栏的标签页）的 [NavKey]。
 * @return 一个被记住的 [NavigationState] 实例，用于驱动导航逻辑。
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutesKey: Set<NavKey>
): NavigationState {

    // 使用 rememberSerializable 来创建和保存当前顶级路由的状态。
    // 这确保了即使在进程重启后，当前选中的标签页也能被恢复。
    val topLevelRouteState = rememberSerializable(
        startRoute, topLevelRoutesKey,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    // 为每一个顶级路由创建一个独立的返回堆栈（NavBackStack）。
    // 这使得每个标签页都能维护自己的导航历史。
    val backStacks = topLevelRoutesKey.associateWith { key -> rememberNavBackStack(key) }

    // 使用 remember 来创建 NavigationState 的实例，
    // 仅当其依赖项（startRoute, topLevelRoutesKey）发生变化时才重新创建。
    return remember(startRoute, topLevelRoutesKey) {
        NavigationState(
            startRoute = startRoute,
            topLevelRouteState = topLevelRouteState,
            backStacks = backStacks
        )
    }
}

/**
 * 一个状态容器类，用于封装和管理导航逻辑。
 * 它持有当前导航状态的所有权，并提供方法来驱动导航 UI。
 *
 * @param startRoute 导航图的起始目的地。
 * @param topLevelRouteState 一个可变的 state，持有当前选中的顶级路由（例如，当前活动的标签页）。
 * @param backStacks 一个从顶级路由 [NavKey]到其对应 [NavBackStack] 的映射。这允许多个并行的返回堆栈。
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRouteState: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {

    /** 当前选中的顶级路由。 */
    var topLevelRoute: NavKey by topLevelRouteState

    /** 计算正在使用的返回堆栈。 */
    val stackInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    /** 导航开始时的路由（旧路由） */
    var lastRoute by mutableStateOf<NavKey?>(null)

    /** 导航开始时的返回栈 */
    var lastBackStack by mutableStateOf<NavBackStack<NavKey>?>(null)

    /** 当前使用的路由 */
    val currentRoute: NavKey by derivedStateOf {
        val activeTopRoute = stackInUse.last()
        backStacks[activeTopRoute]?.lastOrNull() ?: error("CurrentRoute is null!")
    }

    /** 当前使用的返回栈 */
    val currentBackStack: NavBackStack<NavKey> by derivedStateOf {
        backStacks[topLevelRoute] ?: error("CurrentBackStack is null!")
    }

    fun performNavigation(action: () -> Unit) {
        lastRoute = currentRoute
        lastBackStack = currentBackStack
        action()
    }
}

/**
 * 一个扩展函数，将 [NavigationState] 中的抽象导航键（NavKey）转换为可以在UI上渲染的具体条目（NavEntry）。
 *
 * @param entryProvider 一个工厂函数，它接收一个 [NavKey] 并返回一个对应的 [NavEntry]。这个 [NavEntry] 包含了要显示的 Composable 和相关状态。
 * @return 一个 [SnapshotStateList]，其中包含了当前应该显示的所有 [NavEntry]。这个列表是响应式的，当它变化时会触发UI重组。
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {

    // 定义一组装饰器（Decorators）来增强 NavEntry 的功能。
    val decorators = listOf(
        // 这个装饰器至关重要，它能确保在返回堆栈中的 Composable 的状态（例如ViewModel、滚动位置）被保存和恢复。
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        rememberViewModelStoreNavEntryDecorator()
    )

    // 为每个顶级路由的返回堆栈中的 NavKey 创建和装饰 NavEntry。
    val topRouteToEntries = backStacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    // 将当前使用的堆栈（由 stackInUse 决定）中的条目列表和覆盖层条目列表合并。
    // .flatMap { entries[it] ?: emptyList() } 从映射中安全地获取当前活动堆栈的条目。
    // 最后，将合并后的列表转换为 SnapshotStateList，以便Compose可以观察其变化。
    return stackInUse
        .flatMap { topRoute ->
            topRouteToEntries[topRoute] ?: emptyList()
        }
        .toMutableStateList()
}
