#include <jni.h>

#include <memory>

#include "llm/LlmCppEngine.h"
#include "tool/logger.h"

static std::unique_ptr<lunacattus::llm::LlmCppEngine> g_engine;

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_initModel(JNIEnv *env, jobject,
                                                                jstring native_lib_path) {
    llama_log_set(AndroidLogCallback, nullptr);

    const auto *path = env->GetStringUTFChars(native_lib_path, nullptr);
    LOGI("Loading nativeLibPath: %s", path);
    lunacattus::llm::LlmCppEngine::InitBackend(path);
    env->ReleaseStringUTFChars(native_lib_path, path);

    g_engine = std::make_unique<lunacattus::llm::LlmCppEngine>();
    LOGI("init complete");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_loadModel(JNIEnv *env, jobject,
                                                                jstring model_path) {
    if (!g_engine) {
        LOGE("loadModel: engine not initialized");
        return 1;
    }

    const auto *path = env->GetStringUTFChars(model_path, nullptr);
    const int result = g_engine->LoadModel(path);
    env->ReleaseStringUTFChars(model_path, path);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_prepare(JNIEnv * /*env*/, jobject) {
    if (!g_engine) {
        LOGE("prepare: engine not initialized");
        return 1;
    }
    return g_engine->Prepare();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_isReady(JNIEnv *env, jobject) {
    if (!g_engine) {
        LOGE("isReady: engine not initialized");
        return false;
    }
    return g_engine->IsReady();
}
