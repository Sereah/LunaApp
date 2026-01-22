#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android本地化翻译配置文件 - 简化版
"""

# =====================================================
# 1. DeepSeek API 配置（必填）
# =====================================================
# 从 https://platform.deepseek.com/api_keys 获取
# DEEPSEEK_API_KEY = "sk-你的API密钥"
DEEPSEEK_API_KEY = "sk-78f5a99160704487b9df1adc2190122a"

# =====================================================
# 2. 项目路径配置（根据实际情况修改）
# =====================================================
# 说明：脚本位于项目根目录/tools/i18n/下
# 项目结构示例：
# 项目根目录/
# ├── app/
# │   └── src/main/res/
# │       ├── values/strings.xml
# │       └── values-zh/
# └── tools/i18n/           <- 脚本位置
#     ├── config.py
#     └── translate.py

# 源文件相对于项目根目录的路径
SOURCE_RELATIVE_PATH = "app/conflux/src/main/res/values/strings.xml"

# =====================================================
# 3. 目标语言配置
# =====================================================
TARGET_LANGS = {
    "English": "values-en",
    "Simplified Chinese": "values-zh",
    "Traditional Chinese": "values-zh-rTW",
    "Japanese": "values-ja",
    "Korean": "values-ko",
    "German": "values-de",
    "French": "values-fr",
    "Russian": "values-ru",
    "Arabic": "values-ar"
}

# =====================================================
# 4. 高级配置（一般不需要修改）
# =====================================================
# API设置
API_URL = "https://api.deepseek.com/v1/chat/completions"
MODEL = "deepseek-chat"

# 请求设置
REQUEST_DELAY = 1.5      # 请求间隔，避免频率限制
REQUEST_TIMEOUT = 30     # 请求超时时间

# 代理设置（如果需要）
USE_PROXY = True
PROXY_URL = "http://127.0.0.1:7890"