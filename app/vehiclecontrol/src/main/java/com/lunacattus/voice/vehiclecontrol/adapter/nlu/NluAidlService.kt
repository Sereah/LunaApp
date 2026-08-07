package com.lunacattus.voice.vehiclecontrol.adapter.nlu

import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.lifecycle.LifecycleService
import com.google.gson.Gson
import com.lunacattus.voice.vehiclecontrol.IControlStateCallback
import com.lunacattus.voice.vehiclecontrol.IVehicleControlNlu
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskExecutor
import com.lunacattus.voice.vehiclecontrol.domain.port.FunctionCallParser
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AIDL 车控 NLU 服务。
 *
 * 运行在独立进程 `:vehiclecontrol` 中，接收外部进程传入的 function-call JSON，
 * 解析为 [ControlTask] 后交由 [ControlTaskExecutor] 执行。
 * 通过 [RemoteCallbackList] 管理跨进程回调。
 */
@AndroidEntryPoint
class NluAidlService : LifecycleService() {

    @Inject lateinit var parser: FunctionCallParser
    @Inject lateinit var executor: ControlTaskExecutor
    @Inject lateinit var adapter: VehicleControlAdapter
    @Inject lateinit var gson: Gson

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val callbackList = RemoteCallbackList<IControlStateCallback>()

    private val binder = object : IVehicleControlNlu.Stub() {

        override fun submitFunctionCall(json: String) {
            Logger.d(TAG, "收到 function-call: $json")

            val controls = parser.parse(json)
            if (controls.isEmpty()) {
                Logger.w(TAG, "解析结果为空，忽略: $json")
                return
            }

            val task = ControlTask(rawInput = json, controls = controls)

            // 启动任务执行
            serviceScope.launch {
                executor.execute(task).collect { snapshot ->
                    notifyTaskStateChanged(snapshot)
                    snapshot.results.values.forEach { result ->
                        notifyControlResult(result)
                    }
                }
            }
        }

        override fun registerObserver(callback: IControlStateCallback?) {
            callback ?: return
            callbackList.register(callback)
            Logger.d(TAG, "注册 observer: ${callback.hashCode()}")
        }

        override fun unregisterObserver(callback: IControlStateCallback?) {
            callback ?: return
            callbackList.unregister(callback)
            Logger.d(TAG, "取消注册 observer: ${callback.hashCode()}")
        }
    }

    override fun onBind(intent: android.content.Intent) = binder

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "NluAidlService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        callbackList.kill()
        Logger.d(TAG, "NluAidlService onDestroy")
    }

    private fun notifyTaskStateChanged(task: ControlTask) {
        val taskJson = gson.toJson(task)
        val count = callbackList.beginBroadcast()
        for (i in 0 until count) {
            try {
                callbackList.getBroadcastItem(i).onTaskStateChanged(taskJson)
            } catch (e: RemoteException) {
                Logger.e(TAG, "回调 onTaskStateChanged 失败: ${e.message}")
            }
        }
        callbackList.finishBroadcast()
    }

    private fun notifyControlResult(result: com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult) {
        val resultJson = gson.toJson(result)
        val count = callbackList.beginBroadcast()
        for (i in 0 until count) {
            try {
                callbackList.getBroadcastItem(i).onControlResult(resultJson)
            } catch (e: RemoteException) {
                Logger.e(TAG, "回调 onControlResult 失败: ${e.message}")
            }
        }
        callbackList.finishBroadcast()
    }

    companion object {
        private const val TAG = "NluAidlService"
    }
}
