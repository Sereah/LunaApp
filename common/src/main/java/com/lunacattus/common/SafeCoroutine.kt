package com.lunacattus.common

import com.lunacattus.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 协程安全辅助工具类
 *
 * 提供带有异常捕获机制的协程作用域创建及启动方法。
 * 核心目标：防止单个协程崩溃导致整个应用或父作用域崩溃。
 */
object SafeCoroutine {
    private const val TAG = "SafeCoroutine"

    /**
     * 全局通用的协程异常处理器
     * 捕获未被 try-catch 拦截的严重异常，并记录日志
     */
    private val defaultExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        val coroutineName = context[CoroutineName]?.name ?: "Unknown"

        if (throwable is CancellationException) return@CoroutineExceptionHandler

        Logger.e(
            TAG,
            "⚠️ 协程未捕获异常 | 名称：$coroutineName\n" +
                    "📚 类型：${throwable.javaClass.simpleName}\n" +
                    "💬 信息：${throwable.message ?: "无"}"
        )
        Logger.e(TAG, "📜 堆栈详情：$throwable")
    }

    /**
     * 创建一个受保护的协程作用域 (CoroutineScope)
     *
     * @param scopeName 作用域名称，用于日志追踪
     * @param dispatcher 调度器，默认使用 IO 线程池
     * @return CoroutineScope 默认使用 SupervisorJob，子协程失败不会影响兄弟协程
     */
    fun createCoroutineScope(
        scopeName: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): CoroutineScope {
        Logger.d(TAG, "🛠️ 创建协程作用域：$scopeName")
        return CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineName(scopeName) + defaultExceptionHandler
        )
    }

    /**
     * 安全启动协程 (launch)
     *
     * @param name 协程任务名称
     * @param context 附加的协程上下文（如 Dispatchers.Main）
     * @param block 协程执行体
     */
    fun CoroutineScope.launchSafe(
        name: String,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launch(CoroutineName(name) + context) {
            try {
                block()
            } catch (e: CancellationException) {
                Logger.d(TAG, "ℹ️ 协程[$name]已安全取消")
                throw e
            } catch (t: Throwable) {
                Logger.e(TAG, "❌ 协程[$name]运行出错: ${t.message}")
                throw t
            }
        }
    }

    /**
     * 安全启动带有返回值的协程 (async)
     * 使用此方法时，调用 await() 仍需注意处理异常
     */
    fun <T> CoroutineScope.asyncSafe(
        name: String,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        return this.async(CoroutineName(name) + context) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Logger.e(TAG, "❌ 异步任务[$name]执行失败")
                throw t
            }
        }
    }

    /**
     * 安全关闭协程作用域
     */
    fun CoroutineScope.cancelSafe() {
        val name = this.coroutineContext[CoroutineName]?.name ?: "Unknown"
        if (this.isActive) {
            this.cancel()
            Logger.d(TAG, "🛑 作用域[$name]及其子协程已申请取消")
        }
    }
}