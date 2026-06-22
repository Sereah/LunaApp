#pragma once

#include <llama.h>
#include "chat.h"

namespace lunacattus::llm {

    class LlmCppEngine {
    public:
        LlmCppEngine() = default;

        ~LlmCppEngine();

        // Non-copyable, non-movable (raw pointers owned)
        LlmCppEngine(const LlmCppEngine &) = delete;

        LlmCppEngine &operator=(const LlmCppEngine &) = delete;

        /// One-time global backend initialization. Must be called before any instance methods.
        static void InitBackend(const char *native_lib_path);

        /// Load a model from the given path. Returns 0 on success, non-zero on failure.
        int LoadModel(const char *model_path);

        /// Create an inference context from the loaded model.
        /// Returns 0 on success, non-zero on failure.
        int Prepare(int context_size = kDefaultContextSize);

        [[nodiscard]] bool IsReady() const;

    private:
        static constexpr int kDefaultContextSize = 8192;
        static constexpr int kNThreadsMin = 2;
        static constexpr int kNThreadsMax = 4;
        static constexpr int kNThreadsHeadroom = 2;
        static constexpr int kBatchSize = 512;
        static constexpr float kDefaultSamplerTemp = 0.3f;

        llama_model *model_ = nullptr;
        llama_context *context_ = nullptr;
        llama_batch batch_ = {};
        common_chat_templates_ptr templates_ = nullptr;
        common_sampler *sampler_ = nullptr;

        int InitContext(int context_size);
    };

} // namespace lunacattus::llm

