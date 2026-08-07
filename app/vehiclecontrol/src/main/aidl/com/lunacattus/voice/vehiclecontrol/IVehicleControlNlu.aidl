// IVehicleControlNlu.aidl
package com.lunacattus.voice.vehiclecontrol;

import com.lunacattus.voice.vehiclecontrol.IControlStateCallback;

/**
 * 车控 NLU 跨进程通信接口。
 *
 * 外部进程通过 bindService 获取此接口，提交 function-call 并注册状态回调。
 */
interface IVehicleControlNlu {
    /**
     * 提交 function-call JSON 字符串。
     * 每个调用会创建一个 ControlTask 并异步执行。
     */
    void submitFunctionCall(String json);

    /**
     * 注册车控状态观察者。
     * 回调通过 IControlStateCallback 发送执行状态变更。
     */
    void registerObserver(IControlStateCallback callback);

    /**
     * 取消注册观察者。
     */
    void unregisterObserver(IControlStateCallback callback);
}
