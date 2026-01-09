package com.lunacattus.conflux.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
    startRoute: BaseRoute,
    topLevelRoutesKey: Set<BaseRoute>
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
    val startRoute: BaseRoute,
    topLevelRouteState: MutableState<BaseRoute>,
    val backStacks: Map<BaseRoute, NavBackStack<NavKey>>
) {
    /**
     * 当前选中的顶级路由。
     * 通过属性代理到 [topLevelRouteState]，使得状态的读写能够被 Compose 框架观察到。
     */
    var topLevelRoute: BaseRoute by topLevelRouteState

    var lastRoute: BaseRoute? = null
        internal set

    var lastBackStackList: List<NavKey> = emptyList() //todo 空了整理一下路由的变化和返回栈的变化
        internal set

    val currentRoute: BaseRoute
        get() = computeCurrentRoute()

    /**
     * 计算出当前正在使用的导航堆栈组合。
     * 这个列表决定了哪些返回堆栈的内容应该被显示出来。
     * - 如果当前顶级路由就是起始路由，那么只使用起始路由的堆栈。
     * - 否则，将起始路由的堆栈和当前选定顶级路由的堆栈组合起来。这通常用于实现“主页 -> 详情页”然后在不同标签页切换的场景。
     */
    val stackInUse: List<BaseRoute>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    private fun computeCurrentRoute(): BaseRoute {
        val activeTopRoute = stackInUse.last()
        return backStacks[activeTopRoute]
            ?.lastOrNull() as? BaseRoute
            ?: error("currentRoute is null.")
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
    val entries = backStacks.mapValues { (_, stack) ->
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
        .flatMap { entries[it] ?: emptyList() }
        .toMutableStateList()
}
