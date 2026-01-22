@echo off
chcp 65001 > nul
setlocal

echo 🚀 开始自动化翻译流程...

:: 1. 定义虚拟环境目录
set VENV_DIR=venv

:: 2. 如果虚拟环境不存在，则创建它
if not exist "%VENV_DIR%" (
    echo 📦 正在创建虚拟环境...
    python -m venv %VENV_DIR%
    if %errorlevel% neq 0 (
        echo ❌ 创建虚拟环境失败，请检查是否安装了 Python。
        pause
        exit /b %errorlevel%
    )
)

:: 3. 激活虚拟环境并安装/更新依赖
echo 🛠️ 正在检查并安装依赖...
call %VENV_DIR%\Scripts\activate

python -m pip install --upgrade pip
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
if %errorlevel% neq 0 (
    echo ❌ 安装依赖失败。
    pause
    exit /b %errorlevel%
)

:: 5. 执行 Python 翻译脚本
echo 🤖 正在调用 DeepSeek 翻译引擎...
python translate.py
if %errorlevel% neq 0 (
    echo ❌ 翻译脚本执行出错。
    pause
    exit /b %errorlevel%
)

:: 6. 完成后退出虚拟环境
call deactivate

echo ✅ 翻译任务完成！请检查 res/ 目录下的新文件夹。
pause
