#include "LlmCppEngine.h"

#include <bits/sysconf.h>
#include "common.h"

#include "tool/logger.h"
#include "sampling.h"

namespace lunacattus::llm {

    void LlmCppEngine::InitBackend(const char *native_lib_path) {
        ggml_backend_load_all_from_path(native_lib_path);
        llama_backend_init();
    }

    LlmCppEngine::~LlmCppEngine() {
        if (templates_ != nullptr) {
            templates_ = nullptr;
        }
        if (sampler_ != nullptr) {
            common_sampler_free(sampler_);
            sampler_ = nullptr;
        }
        llama_batch_free(batch_);
        if (context_ != nullptr) {
            llama_free(context_);
            context_ = nullptr;
        }
        if (model_ != nullptr) {
            llama_model_free(model_);
            model_ = nullptr;
        }
    }

    int LlmCppEngine::LoadModel(const char *model_path) {
        if (model_ != nullptr) {
            LOGW("%s: model already loaded, freeing existing model", __func__);
            llama_model_free(model_);
            model_ = nullptr;
        }

        const auto model_params = llama_model_default_params();
        model_ = llama_model_load_from_file(model_path, model_params);
        if (model_ == nullptr) {
            LOGE("%s: llama_model_load_from_file() failed", __func__);
            return 1;
        }
        return 0;
    }

    int LlmCppEngine::Prepare(const int context_size) {
        if (model_ == nullptr) {
            LOGE("%s: model must be loaded before calling prepare", __func__);
            return 1;
        }

        if (InitContext(context_size) != 0) {
            LOGE("%s: InitContext() failed", __func__);
            return 1;
        }
        batch_ = llama_batch_init(kBatchSize, 0, 1);

        templates_ = common_chat_templates_init(model_, "");
        if (templates_ == nullptr) {
            LOGE("%s: common_chat_templates_init() failed", __func__);
            return 1;
        }

        common_params_sampling params_sampling;
        params_sampling.temp = kDefaultSamplerTemp;
        sampler_ = common_sampler_init(model_, params_sampling);
        if (sampler_ == nullptr) {
            LOGE("%s: common_sampler_init() failed", __func__);
            return 1;
        }

        return 0;
    }

    bool LlmCppEngine::IsReady() const {
        LOGI("%s: model_=%p, context_=%p, templates_=%p, sampler_=%p",
             __func__, model_, context_, templates_.get(), sampler_);
        return model_ != nullptr && context_ != nullptr && templates_ != nullptr &&
               sampler_ != nullptr;
    }

    int LlmCppEngine::InitContext(int context_size) {
        if (context_ != nullptr) {
            LOGW("%s: context already created, freeing existing context", __func__);
            llama_free(context_);
            context_ = nullptr;
        }

        const int n_thread = std::max(kNThreadsMin,
                                      std::min(kNThreadsMax,
                                               static_cast<int>(sysconf(_SC_NPROCESSORS_ONLN)) -
                                               kNThreadsHeadroom));
        LOGI("%s: n_thread = %d", __func__, n_thread);

        llama_context_params context_params = llama_context_default_params();
        const int trained_context_size = llama_model_n_ctx_train(model_);
        if (context_size > trained_context_size) {
            LOGW("%s: context_size=%d > trained_context_size=%d, using context_size",
                 __func__, context_size, trained_context_size);
        }

        context_params.n_ctx = context_size;
        context_params.n_batch = kBatchSize;
        context_params.n_ubatch = kBatchSize;
        context_params.n_threads = n_thread;
        context_params.n_threads_batch = n_thread;

        context_ = llama_init_from_model(model_, context_params);
        if (context_ == nullptr) {
            LOGE("%s: llama_init_from_model() failed", __func__);
            return 1;
        }
        return 0;
    }

} // namespace lunacattus::llm
