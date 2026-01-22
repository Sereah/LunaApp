#!/bin/bash

# 确保脚本在出错时停止
set -e

echo "🚀 开始自动化翻译流程..."

# 1. 定义虚拟环境目录
VENV_DIR="venv"

# 2. 如果虚拟环境不存在，则创建它
if [ ! -d "$VENV_DIR" ]; then
    echo "📦 正在创建虚拟环境..."
    python3 -m venv $VENV_DIR
fi

# 3. 激活虚拟环境并安装/更新依赖
echo "🛠️ 正在检查并安装依赖..."
source $VENV_DIR/bin/activate
pip install --upgrade pip
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 5. 执行 Python 翻译脚本
echo "🤖 正在调用 DeepSeek 翻译引擎..."
python3 translate.py

# 6. 完成后退出虚拟环境
deactivate

echo "✅ 翻译任务完成！请检查 res/ 目录下的新文件夹。"