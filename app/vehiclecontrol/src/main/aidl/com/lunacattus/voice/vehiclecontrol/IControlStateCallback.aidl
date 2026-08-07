// IControlStateCallback.aidl
package com.lunacattus.voice.vehiclecontrol;

/**
 * 车控状态回调接口（oneway：异步通知，不阻塞调用方）。
 */
oneway interface IControlStateCallback {
    /**
     * 任务状态发生变更。
     * @param taskJson ControlTask 的 JSON 序列化字符串。
     */
    void onTaskStateChanged(String taskJson);

    /**
     * 单个车控操作执行完成。
     * @param resultJson ControlResult 的 JSON 序列化字符串。
     */
    void onControlResult(String resultJson);
}
