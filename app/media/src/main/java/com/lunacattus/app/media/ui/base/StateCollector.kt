package com.lunacattus.app.media.ui.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 为基于 data class 的 UiState 设计的状态收集器。
 * 它可以轻松地订阅单个或多个属性的变化，并自动处理生命周期和去重逻辑。
 *
 * @param STATE 你的 data class UiState 类型。
 * @param lifecycleOwner 通常是 Fragment 或 Activity。
 * @param uiStateFlow 来自 ViewModel 的 UiState Flow。
 */
class StateCollector<STATE : IUIState>(
    private val lifecycleOwner: LifecycleOwner,
    private val uiStateFlow: Flow<STATE>
) {

    /**
     * 订阅单个属性的变化。
     * 当该属性的值发生变化时，将触发 action。
     *
     * 使用示例：
     * ```
     * collector.collectProperty(
     *     property = { it.isLoading },
     *     action = { isLoading -> progressBar.isVisible = isLoading }
     * )
     * ```
     *
     * @param property 一个从完整状态中提取你关心的属性的 lambda。
     * @param action 当属性变化时要执行的回调。
     */
    fun <T> collectProperty(
        property: (STATE) -> T,
        action: suspend (T) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiStateFlow
                    .map(property)
                    .distinctUntilChanged()
                    .collect { value -> action(value) }
            }
        }
    }

    /**
     * 组合订阅两个属性的变化。
     * 当其中任何一个属性值改变时，都会触发 action。
     *
     * 使用示例：
     * ```
     * collector.collectProperties(
     *     property1 = { it.isLoggedIn },
     *     property2 = { it.userName },
     *     action = { isLoggedIn, userName ->
     *         welcomeText.text = if (isLoggedIn) "欢迎, $userName" else "请登录"
     *     }
     * )
     * ```
     */
    fun <T1, T2> collectProperties(
        property1: (STATE) -> T1,
        property2: (STATE) -> T2,
        action: suspend (T1, T2) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow1 = uiStateFlow.map(property1).distinctUntilChanged()
                val flow2 = uiStateFlow.map(property2).distinctUntilChanged()

                combine(flow1, flow2) { t1, t2 ->
                    action(t1, t2)
                }.collect() // 这里的 collect 只是为了触发上游的 combine
            }
        }
    }

    /**
     * 组合订阅三个属性的变化。
     */
    fun <T1, T2, T3> collectProperties(
        property1: (STATE) -> T1,
        property2: (STATE) -> T2,
        property3: (STATE) -> T3,
        action: suspend (T1, T2, T3) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow1 = uiStateFlow.map(property1).distinctUntilChanged()
                val flow2 = uiStateFlow.map(property2).distinctUntilChanged()
                val flow3 = uiStateFlow.map(property3).distinctUntilChanged()

                combine(flow1, flow2, flow3) { t1, t2, t3 ->
                    action(t1, t2, t3)
                }.collect()
            }
        }
    }
}