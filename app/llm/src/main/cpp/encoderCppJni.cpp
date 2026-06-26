#include <jni.h>
#include <memory>

#include "encoder/EncoderCppEngine.h"
#include "tool/logger.h"

using lunacattus::encoder::EncoderCppEngine;
static std::unique_ptr<EncoderCppEngine> g_encoder;

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeInit(JNIEnv *env, jobject,
                                                                  jstring native_lib_path) {
    llama_log_set(AndroidLogCallback, nullptr);

    const auto *path = env->GetStringUTFChars(native_lib_path, nullptr);
    LOGI("%s: nativeLibPath=%s", __func__, path);
    EncoderCppEngine::InitBackend(path);
    env->ReleaseStringUTFChars(native_lib_path, path);

    g_encoder = std::make_unique<EncoderCppEngine>();
    LOGI("%s: init complete", __func__);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeLoadModel(JNIEnv *env, jobject,
                                                                       jstring model_path) {
    if (!g_encoder) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }
    const auto *path = env->GetStringUTFChars(model_path, nullptr);
    const int result = g_encoder->LoadModel(path);
    env->ReleaseStringUTFChars(model_path, path);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativePrepare(JNIEnv * /*env*/, jobject) {
    if (!g_encoder) {
        LOGE("%s: engine not initialized", __func__);
        return 1;
    }
    return g_encoder->Prepare();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeIsReady(JNIEnv * /*env*/, jobject) {
    if (!g_encoder) return false;
    return g_encoder->IsReady();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeClassifyText(JNIEnv *env, jobject,
                                                                          jstring input) {
    if (!g_encoder) {
        LOGE("%s: engine not initialized", __func__);
        return -1;
    }

    const char *text = env->GetStringUTFChars(input, nullptr);
    LOGI("%s: text=%s", __func__, text);
    std::string text_str(text);
    env->ReleaseStringUTFChars(input, text);

    const auto tokens = g_encoder->Tokenize(text_str);
    LOGI("%s: token count=%d", __func__, (int) tokens.size());

    if (g_encoder->Encode(tokens) != 0) {
        LOGE("%s: Encode failed", __func__);
        return -1;
    }

    return (jint) g_encoder->GetTopClassIndex();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeUnload(JNIEnv * /*env*/, jobject) {
    if (!g_encoder) return;
    g_encoder->Unload();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_BertLLmRepository_nativeShutdown(JNIEnv * /*env*/, jobject) {
    EncoderCppEngine::ShutDown();
}
