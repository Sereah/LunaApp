package com.lunacattus.conflux.domain.llm.tts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ITts {

    /** WebSocket 推送事件流（音频块、完成、错误等） */
    val wsEvents: Flow<TtsWsEvent>

    /** WebSocket 连接状态（true=已连接） */
    val wsConnectionState: StateFlow<Boolean>

    /** 更新服务端地址和端口 */
    fun updateConfig(config: TtsConfig)

    /** 通过 HTTP POST /api/tts 请求语音合成，返回所有分片 */
    suspend fun synthesize(request: TtsHttpRequest): Result<TtsHttpResponse>

    /** 通过 HTTP GET /api/health 查询服务端健康状态 */
    suspend fun healthCheck(): Result<HealthResponse>

    /** 建立 WebSocket 连接 */
    suspend fun connectWs()

    /** 断开 WebSocket 连接 */
    suspend fun disconnectWs()

    /** 通过 WebSocket 发送文本合成请求 */
    suspend fun sendWsText(request: TtsWsTextMessage): Boolean

    /** 通过 WebSocket 取消指定请求的合成任务 */
    suspend fun cancelWs(requestId: String)

    /** 通过 WebSocket 发送心跳 ping */
    suspend fun pingWs()
}
