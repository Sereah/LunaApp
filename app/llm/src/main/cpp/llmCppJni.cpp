//
// Created by glacien on 2026/6/18.
//

#include <jni.h>
#include <llama.h>
#include <common.h>
#include <chat.h>
#include "logger.h"

static llama_model *g_model;

extern "C"
JNIEXPORT void JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_initModel(JNIEnv *env, jobject,
                                                                jstring nativeLibPath) {
    llama_log_set(android_log_callback, nullptr);
    const auto *path = env->GetStringUTFChars(nativeLibPath, nullptr);
    LOGi("Loading nativeLibPath: %s", path);
    ggml_backend_load_all_from_path(path);
    env->ReleaseStringUTFChars(nativeLibPath, path);
    llama_backend_init();
    LOGi("init complete");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_loadModel(JNIEnv *env, jobject,
                                                                jstring modelPath) {
    const auto model_params = llama_model_default_params();
    const auto *model_path = env->GetStringUTFChars(modelPath, nullptr);
    auto *model = llama_model_load_from_file(model_path, model_params);
    env->ReleaseStringUTFChars(modelPath, model_path);
    if (!model) return 1;
    g_model = model;
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lunacattus_llm_domain_local_LlmCppRepository_prepare(JNIEnv *env, jobject) {
    // todo
    return 0;
}
