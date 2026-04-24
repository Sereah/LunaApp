package com.lunacattus.conflux.domain.llm

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.lunacattus.common.utils.AssetUtils
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GemmaManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ILLMManager {

    companion object {
        private const val TAG = "GemmaManager"

        // 核心修改 1：放弃原生 ToolSet，手动把工具列表写进系统提示词
        private val SYSTEM_PROMPT = """
            你是一个车载电话助手。
            可用工具：
            - make_call(contact_name, phone_number): 拨打电话给指定的联系人或号码。
            - search_contact(query): 当用户没说清打给谁，或者需要查找联系人信息时使用。

            要求：
            1. 必须且只能返回 JSON 格式的调用，绝对不要输出任何其他解释文字。
            2. 格式示例：{"call": "make_call", "args": {"contact_name": "老婆"}}
        """.trimIndent()
    }

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    override suspend fun initModel(): Boolean {
        return withContext(Dispatchers.Default) {
            val modelPath = AssetUtils.copyToFiles(context, "llm") + "/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
            Logger.d(TAG, "modelPath: $modelPath")

            val backends = listOf(Backend.GPU(), Backend.CPU())
            var initializedEngine: Engine? = null

            for (backend in backends) {
                try {
                    Logger.d(TAG, "尝试使用后端初始化: $backend")
                    val engineConfig = EngineConfig(
                        modelPath = modelPath,
                        backend = backend,
                        maxNumTokens = 1024
                    )
                    val newEngine = Engine(engineConfig)
                    newEngine.initialize()
                    initializedEngine = newEngine
                    Logger.d(TAG, "✅ 引擎初始化成功: $backend")
                    break
                } catch (e: Exception) {
                    Logger.e(TAG, "后端 $backend 初始化失败: ${e.message}")
                }
            }

            if (initializedEngine == null) return@withContext false
            engine = initializedEngine

            createConversationInstance()
            return@withContext true
        }
    }

    // 独立出创建会话的方法，方便复用
    private fun createConversationInstance(): Boolean {
        return try {
            val currentEngine = engine ?: return false
            conversation = currentEngine.createConversation(
                ConversationConfig(
                    systemInstruction = Contents.of(SYSTEM_PROMPT),
                    // 核心修改 2：移除 tools = listOf(...)，不使用原生 API
                    samplerConfig = SamplerConfig(
                        topK = 1,          // 降低随机性
                        topP = 0.1,       // 降低随机性
                        temperature = 0.0 // 强制为 0.0，保证 JSON 稳定输出
                    )
                )
            )
            Logger.d(TAG, "Conversation 创建成功")
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Conversation 创建失败: ${e.message}")
            false
        }
    }

    override suspend fun generate(prompt: String): Flow<String> = callbackFlow {
        val currentConversation = conversation
        if (currentConversation == null) {
            close(IllegalStateException("Model not initialized"))
            return@callbackFlow
        }

        currentConversation.sendMessageAsync(
            Contents.of(prompt),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    // 核心修改 3：因为 API 变动可能没有 .text，我们用 toString() 但强制剔除异常 token
                    var textChunk = message.toString()

                    // 过滤掉大模型崩溃时吐出的占位符，防止污染 JSON
                    if (textChunk.contains("<pad>") || textChunk.contains("Message")) {
                        textChunk = textChunk.replace("<pad>", "").replace("Message(content=[Text(text=", "").replace(")])", "")
                    }

                    if (textChunk.isNotBlank()) {
                        trySend(textChunk)
                    }
                }

                override fun onDone() {
                    close()
                    Logger.d(TAG, "推理流完成")

                    // 重置会话，清理上下文缓存（防止下次对话带入干扰）
                    conversation?.close()
                    createConversationInstance()
                }

                override fun onError(throwable: Throwable) {
                    close(throwable)
                    Logger.e(TAG, "推理报错: ${throwable.message}")
                }
            }
        )
        awaitClose {
            Logger.d(TAG, "Flow 关闭。")
        }
    }

    override fun release() {
        conversation?.close()
        engine?.close()
        conversation = null
        engine = null
    }
}