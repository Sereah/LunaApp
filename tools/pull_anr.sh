#!/bin/bash
# ===============================================================
# pull_anr.sh — ANR 发生后，一键拉取现场数据
# 用法:  ./pull_anr.sh [包名]
#       默认包名: com.lunacattus.performancedemo
# ===============================================================

set -e

PACKAGE="${1:-com.lunacattus.performancedemo}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ─── 询问输出路径 ────────────────────────────────────────────────
read -rp "输出目录（默认: ${SCRIPT_DIR}）: " CUSTOM_DIR
OUT_BASE="${CUSTOM_DIR:-$SCRIPT_DIR}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUTDIR="${OUT_BASE}/anr_captures_${TIMESTAMP}"

echo
echo "📁 保存到: $OUTDIR"
echo

# ─── adb root ────────────────────────────────────────────────────
echo "━━━ [0/5] adb root ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
ROOT_OUT=$(adb root 2>&1) || { echo "❌ adb root 失败: $ROOT_OUT"; exit 1; }
echo "  ✅ $ROOT_OUT"

REM_OUT=$(adb remount 2>&1) || { echo "❌ adb remount 失败: $REM_OUT"; exit 1; }
echo "  ✅ $REM_OUT"
echo

# ─── 1. 拉取 /data/anr/ 下的 trace 文件 ─────────────────────────
echo "━━━ [1/5] 拉取 /data/anr/ 文件 ━━━━━━━━━━━━━━━━━━━━━━━"
mkdir -p "$OUTDIR/anr"
adb shell ls /data/anr/ 2>/dev/null | while IFS= read -r f; do
    echo "    - $f"
done

adb pull /data/anr/ "$OUTDIR/anr" 2>&1 | tail -1 | sed 's/^/  /'

# 进程还在，赶紧抓 cpuinfo（bugreport 太慢，等它跑完进程早被杀了）
adb shell dumpsys cpuinfo 2>/dev/null | grep "$PACKAGE" > "$OUTDIR/cpuinfo.txt" 2>/dev/null
echo "  ✅ cpuinfo"
echo

# ─── 2. 导出 bugreport ──────────────────────────────────────────
echo "━━━ [2/5] 导出 bugreport（大约 10~30 秒）━━━━━━━━━━━━━"
adb bugreport "$OUTDIR/bugreport_${TIMESTAMP}.zip" 2>&1 | tail -1 | sed 's/^/  /'
echo

# ─── 3. 系统日志（events buffer + main buffer） ─────────────────
echo "━━━ [3/5] 导出系统日志 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
adb logcat -b events -d | grep -i "$PACKAGE\|am_anr\|am_kill\|am_proc_died" \
    > "$OUTDIR/events_anr.log" 2>/dev/null
echo "  ✅ events buffer: $(wc -l < "$OUTDIR/events_anr.log") 行"

adb logcat -d | grep -i "$PACKAGE" \
    > "$OUTDIR/main_anr.log" 2>/dev/null
echo "  ✅ main buffer: $(wc -l < "$OUTDIR/main_anr.log") 行"

adb logcat -d -t 2000 2>/dev/null > "$OUTDIR/main_full_tail.log"
echo "  ✅ main buffer 尾部 2000 行"
echo

# ─── 4. 进程状态快照 ────────────────────────────────────────────
echo "━━━ [4/5] dumpsys 进程状态 ━━━━━━━━━━━━━━━━━━━━━━━━━"
adb shell dumpsys meminfo "$PACKAGE" 2>/dev/null > "$OUTDIR/meminfo.txt"
echo "  ✅ meminfo"

adb shell dumpsys dropbox --print data_app_anr 2>/dev/null > "$OUTDIR/dropbox_anr.txt" \
    && echo "  ✅ dropbox ANR 记录" || echo "  ⚠️  无 dropbox 记录"
echo

# ─── 5. 生成摘要 ────────────────────────────────────────────────
echo "━━━ [5/5] 生成摘要 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
SUMMARY="$OUTDIR/SUMMARY.md"
{
    echo "# ANR 分析摘要"
    echo
    echo "采集时间: $(date)"
    echo "包名: $PACKAGE"
    echo
    echo "## 文件清单"
    echo
    find "$OUTDIR" -type f | sort | while IFS= read -r f; do
        size=$(du -h "$f" | cut -f1)
        echo "- \`${f#$OUTDIR/}\` (${size})"
    done
} > "$SUMMARY"
echo "  ✅ SUMMARY.md"

echo
echo "══════════════════════════════════════════════════"
echo "🎉 完成！全部文件在: $OUTDIR"
echo "══════════════════════════════════════════════════"
