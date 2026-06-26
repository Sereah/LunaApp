#include <jni.h>

#include <memory>

#include "llm/LlmCppEngine.h"
#include "tool/logger.h"
#include "tool/utf8.h"

using lunacattus::llm::LlmCppEngine;
static std::unique_ptr<LlmCppEngine> g_engine;

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeInit(JNIEnv *env, jobject,
                                                                      jstring native_lib_path) {
    llama_log_set(AndroidLogCallback, nullptr);

    const auto *path = env->GetStringUTFChars(native_lib_path, nullptr);
    LOGI("%s: Loading nativeLibPath: %s", __func__, path);
    LlmCppEngine::InitBackend(path);
    env->ReleaseStringUTFChars(native_lib_path, path);

    g_engine = std::make_unique<LlmCppEngine>();
    LOGI("%s: init complete", __func__);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeLoadModel(JNIEnv *env, jobject,
                                                                           jstring model_path) {
    if (!g_engine) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }

    const auto *path = env->GetStringUTFChars(model_path, nullptr);
    const int result = g_engine->LoadModel(path);
    env->ReleaseStringUTFChars(model_path, path);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativePrepare(JNIEnv * /*env*/,
                                                                         jobject) {
    if (!g_engine) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }
    return g_engine->Prepare();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeIsReady(JNIEnv *env, jobject) {
    if (!g_engine) {
        LOGE("%s: engine not initialized", __func__);
        return false;
    }
    return g_engine->IsReady();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeProcessSystemPrompt(JNIEnv *env,
                                                                                     jobject,
                                                                                     jstring sys_prompt,
                                                                                     jboolean enable_thinking) {
    if (!g_engine) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }

    g_engine->ResetSystemTerm();
    g_engine->ResetUserTerm();

    const auto system_prompt = env->GetStringUTFChars(sys_prompt, nullptr);
    LOGI("%s: processSystemPrompt: %s, enable_thinking=%d", __func__, system_prompt,
         enable_thinking);
    std::string format_system_prompt = g_engine->FormatPrompt(true, system_prompt,
                                                              enable_thinking);
    env->ReleaseStringUTFChars(sys_prompt, system_prompt);

    auto tokens = g_engine->TokenizePrompt(format_system_prompt);
    if (LlmCppEngine::CheckTokensLength(true, tokens) != 0) {
        LOGE("%s: CheckTokensLength failed", __func__);
        return 1;
    }

    if (g_engine->DecodeTokensInBatches(tokens, false) != 0) {
        LOGE("%s: DecodeTokensInBatches failed", __func__);
        return 1;
    }

    // 记录位置信息，供后续增量解码使用
    auto &chat = g_engine->chat_term();
    chat.system_prompt_position = (int) tokens.size();
    chat.current_position = (int) tokens.size();
    chat.previous_prompt_tokens = (int) tokens.size();

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeProcessUserPrompt(JNIEnv *env,
                                                                                   jobject,
                                                                                   jstring user_prompt,
                                                                                   jint predict_length,
                                                                                   jboolean enable_thinking) {
    if (!g_engine) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }

    g_engine->ResetUserTerm();

    const char *const u_prompt = env->GetStringUTFChars(user_prompt, nullptr);
    LOGI("%s: processUserPrompt: %s, enable_thinking=%d", __func__, u_prompt, enable_thinking);
    std::string format_user_prompt = g_engine->FormatPrompt(false, u_prompt, enable_thinking);
    env->ReleaseStringUTFChars(user_prompt, u_prompt);

    std::vector<int> tokens = g_engine->TokenizePrompt(format_user_prompt);

    if (LlmCppEngine::CheckTokensLength(false, tokens) != 0) {
        LOGE("%s: CheckTokensLength failed", __func__);
        return 1;
    }

    // 增量解码：只处理本轮新增的 token，跳过已在 KV cache 中的历史
    auto &chat = g_engine->chat_term();
    const int prev_count = chat.previous_prompt_tokens;
    const int total_count = (int) tokens.size();
    const int new_count = total_count - prev_count;

    if (new_count > 0) {
        std::vector<int> new_tokens(tokens.begin() + prev_count, tokens.end());
        LOGI("%s: Incremental decode: %d new tokens (total=%d, prev=%d)", __func__,
             new_count, total_count, prev_count);
        if (g_engine->DecodeTokensInBatches(new_tokens, true, chat.current_position) != 0) {
            LOGE("%s: DecodeTokensInBatches failed", __func__);
            return 1;
        }
    } else {
        LOGI("%s: No new tokens to decode, skipping", __func__);
    }
    chat.previous_prompt_tokens = total_count;

    // 更新当前位置和停止生成位置
    chat.current_position += new_count;
    chat.stop_generation_position = chat.current_position + predict_length;

    return 0;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeGenerateNextToken(JNIEnv *env,
                                                                                   jobject) {
    if (!g_engine) {
        LOGE("%s: generateNextToken: engine not initialized", __func__);
        return nullptr;
    }

    // 上下文窗口满 → 执行滑动平移
    if (g_engine->IsContextFull()) {
        g_engine->ShiftContext();
    }

    // 到达预设的停止位置 → 结束生成
    auto &chat = g_engine->chat_term();
    if (chat.current_position >= chat.stop_generation_position) {
        LOGW("%s: STOP: hitting stop position: %d", __func__, chat.stop_generation_position);
        return nullptr;
    }

    const int new_token_id = g_engine->SampleNextToken();

    if (g_engine->PopulateBatchAndDecode(new_token_id) != 0) {
        LOGE("%s: PopulateBatchAndDecode failed", __func__);
        return nullptr;
    }

    chat.current_position++;

    // 检查是否为结束标记
    if (g_engine->CheckTokenEndOfGenerate(new_token_id)) {
        LOGD("%s: id: %d,\tIS EOG!\nSTOP.", __func__, new_token_id);
        return nullptr;
    }

    auto new_token_chars = g_engine->ConvertTokenToText(new_token_id);

    // 累积的 UTF-8 字节合法 → 输出；否则等待更多字节
    auto cached_chars = chat.cached_token_chars.c_str();
    jstring result = nullptr;
    if (lunacattus::llm::IsValidUtf8(cached_chars)) {
        result = env->NewStringUTF(cached_chars);
        LOGD("%s: id: %d,\tcached: `%s`,\tnew: `%s`", __func__, new_token_id, cached_chars,
             new_token_chars.c_str());

        chat.assistant_ss << chat.cached_token_chars;
        chat.cached_token_chars.clear();
    } else {
        LOGD("%s: id: %d,\tappend to cache", __func__, new_token_id);
        result = env->NewStringUTF("");
    }

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeUnload(JNIEnv * /*env*/, jobject) {
    if (!g_engine) {
        LOGE("%s: unload: engine not initialized", __func__);
        return;
    }
    g_engine->Unload();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_GenerateLLmRepository_nativeShutdown(JNIEnv * /*env*/,
                                                                          jobject) {
    LlmCppEngine::ShutDown();
}
