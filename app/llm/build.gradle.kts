plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.view)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.framework.jar)
}

frameworkJar {
    version = "13"
    custom = true
}

android {
    namespace = "com.lunacattus.llm"

    ndkVersion = "29.0.13113456"

    defaultConfig {
        applicationId = "com.lunacattus.llm"
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        externalNativeBuild {
            cmake {
                // 通用编译与调试配置
                arguments += "-DCMAKE_BUILD_TYPE=Release" // 指定编译模式为 Release（发行版）
                arguments += "-DCMAKE_MESSAGE_LOG_LEVEL=DEBUG" // 设置 CMake 配置阶段的日志级别为 DEBUG
                arguments += "-DCMAKE_VERBOSE_MAKEFILE=ON" // 开启冗长编译模式

                // 基础组件与依赖配置
                arguments += "-DBUILD_SHARED_LIBS=ON" // 强制将目标编译为动态链接库
                arguments += "-DLLAMA_BUILD_APP=OFF" // 关闭 llama.cpp 自带的命令行可执行程序的编译
                arguments += "-DLLAMA_BUILD_COMMON=ON" // 开启 llama.cpp common 基础公共库的编译
                arguments += "-DLLAMA_OPENSSL=OFF" // 关闭 OpenSSL 依赖

                // GGML/LLM 推理核心性能配置
                arguments += "-DGGML_NATIVE=OFF" // 关闭原生本地优化（-march=native）。
                arguments += "-DGGML_BACKEND_DL=ON" // 开启动态加载后端驱动（Dynamic Loading）。
                arguments += "-DGGML_CPU_ALL_VARIANTS=ON" // 开启 CPU 所有指令集变体的支持。
                arguments += "-DGGML_LLAMAFILE=OFF" // 关闭 llamafile 格式相关的支持
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    packaging {
        jniLibs {
            // 强制提取 so 文件到 native lib 目录，使 ggml_backend_load_all_from_path
            // 能通过 dlopen 加载 CPU 变体 MODULE 库，实现运行时指令集最佳选择
            useLegacyPackaging = true
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.31.6"
        }
    }
}

dependencies {
    implementation(libs.common)
    implementation(libs.logger)
}