package com.lunacattus.app.base.view

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lunacattus.app.base.view.base.IUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 状态收集委托类，封装通用的状态收集逻辑
 */
class StateCollectorDelegate<STATE : IUIState>(
    val lifecycleOwner: LifecycleOwner,
    val uiStateFlow: Flow<STATE>
) {

    /**
     * 配置类：用于封装流处理参数（映射、过滤、去重逻辑）
     *
     * `mapFn`: 状态映射函数
     *
     * `filterFn`: 结果过滤条件（默认不过滤）
     *
     * `distinctFn`: 去重比较逻辑（默认对象相等）
     */
    class CollectConfig<T, R>(
        val mapFn: (T) -> R,
        val filterFn: (R) -> Boolean = { true },
        val distinctFn: (old: R, new: R) -> Boolean = { old, new -> old == new }
    )

    /**
     * 简化版 状态收集函数 - 直接收集特定状态类型
     *
     * 使用场景：当只需要监听特定状态类型，不需要额外转换时
     *
     * 示例：
     * ```
     * collectState<LoadingState> { state ->
     *     progress.visibility = if (state.isLoading) VISIBLE else GONE
     * }
     * ```
     *
     * @param T 要收集的状态类型（必须是 STATE 的子类型）
     * @param filterFn 状态过滤条件（默认不过滤）
     * @param collectFn 状态收集回调
     */
    inline fun <reified T : STATE> collectState(
        noinline filterFn: (T) -> Boolean = { true },
        crossinline collectFn: (T) -> Unit
    ) {
        collectState<T, T>(
            config = CollectConfig(
                mapFn = { it },
                filterFn = filterFn,
                distinctFn = { old, new -> old == new }
            ),
            collectFn = collectFn
        )
    }

    /**
     * 完整版 状态收集函数 - 通过配置对象收集处理状态
     *
     * 使用场景：需要对状态进行复杂转换、过滤或自定义去重逻辑时
     *
     * 示例：
     * ```
     * val config = CollectConfig<SuccessState, List<Item>>(
     *     mapFn = { it.items },           // 提取列表数据
     *     filterFn = { it.isNotEmpty() }, // 过滤空列表
     *     distinctFn = { old, new -> old.size == new.size } // 自定义去重
     * )
     *
     * collectState(config) { items ->
     *     adapter.submitList(items)  // 更新RecyclerView
     * }
     * ```
     *
     * @param T 原始状态类型（必须是 STATE 的子类型）
     * @param R 转换后的结果类型
     * @param config 状态处理配置对象
     * @param collectFn 最终结果回调
     */
    inline fun <reified T : STATE, R> collectState(
        config: CollectConfig<T, R>,
        crossinline collectFn: (R) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiStateFlow
                    .filterIsInstance<T>()
                    .map { config.mapFn(it) }
                    .filter { config.filterFn(it) }
                    .distinctUntilChanged(config.distinctFn)
                    .collect { collectFn(it) }
            }
        }
    }

    /**
     * 组合状态收集函数 - 同时监听两个派生状态
     *
     * 使用场景：当UI依赖两个独立但同源的状态时
     *
     * 示例：
     * ```
     * val configA = CollectConfig<AuthState, Boolean>(
     *     mapFn = { it.isLoggedIn }
     * )
     *
     * val configB = CollectConfig<AuthState, String>(
     *     mapFn = { it.userName },
     *     filterFn = { it.isNotBlank() }
     * )
     *
     * collectCombined(configA, configB) { isLoggedIn, userName ->
     *     tvWelcome.text = if (isLoggedIn) "欢迎 $userName" else "请登录"
     * }
     * ```
     *
     * @param T 原始状态类型（必须是 STATE 的子类型）
     * @param A 第一个派生状态类型
     * @param B 第二个派生状态类型
     * @param flowA 第一个状态的收集配置
     * @param flowB 第二个状态的收集配置
     * @param collectFn 组合结果回调（A, B → Unit）
     */
    inline fun <reified T : STATE, A, B> collectCombined(
        flowA: CollectConfig<T, A>,
        flowB: CollectConfig<T, B>,
        crossinline collectFn: (A, B) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val sharedFlow = uiStateFlow.filterIsInstance<T>()

                val processedFlowA = sharedFlow
                    .map { flowA.mapFn(it) }
                    .filter { flowA.filterFn(it) }
                    .distinctUntilChanged(flowA.distinctFn)

                val processedFlowB = sharedFlow
                    .map { flowB.mapFn(it) }
                    .filter { flowB.filterFn(it) }
                    .distinctUntilChanged(flowB.distinctFn)

                processedFlowA
                    .combine(processedFlowB) { a, b -> Pair(a, b) }
                    .collect { (a, b) -> collectFn(a, b) }
            }
        }
    }
}