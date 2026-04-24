package com.lunacattus.common.utils

import android.content.Context
import com.lunacattus.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object AssetUtils {
    private const val TAG = "AssetUtils"

    /**
     * 将 assets 目录下的指定文件夹及其所有子文件/文件夹，复制到应用的 files 目录下
     *
     * @param context Context
     * @param assetDirName assets 中的目标文件夹名
     * @return 复制后的真实完整绝对路径，如果发生异常可能返回 null
     */
    fun copyToFiles(context: Context, assetDirName: String): String? {
        val targetDir = File(context.filesDir, assetDirName)

        return try {
            Logger.d(TAG, "开始将 assets/$assetDirName 复制到 ${targetDir.absolutePath}")
            copyAssetDir(context, assetDirName, targetDir)
            Logger.d(TAG, "复制完成！")
            targetDir.absolutePath
        } catch (e: Exception) {
            Logger.e(TAG, "copyToFiles 过程中出现异常: ${e.message}")
            null
        }
    }

    /**
     * 递归复制文件夹内部实现
     */
    private fun copyAssetDir(context: Context, assetPath: String, targetDir: File) {
        val assetManager = context.assets

        try {
            // 1. 如果目标文件夹不存在，则创建它
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // 2. 获取当前 asset 目录下的所有文件和子文件夹名称
            val files = assetManager.list(assetPath) ?: return

            for (fileName in files) {
                // 拼接出当前文件在 assets 中的相对路径
                val currentAssetPath = if (assetPath.isEmpty()) fileName else "$assetPath/$fileName"
                val currentTargetFile = File(targetDir, fileName)

                // 3. Android AssetManager 没有直接判断是否为文件夹的 API。
                // 常见做法是通过再次 list()，如果有子项说明是文件夹。
                val subFiles = assetManager.list(currentAssetPath)

                if (!subFiles.isNullOrEmpty()) {
                    // 是子文件夹，递归调用
                    copyAssetDir(context, currentAssetPath, currentTargetFile)
                } else {
                    // 可能是文件，尝试进行流复制
                    copySingleFile(context, currentAssetPath, currentTargetFile)
                }
            }
        } catch (e: IOException) {
            Logger.e(TAG, "复制文件夹路径失败: $assetPath")
        }
    }

    /**
     * 复制单个文件
     */
    private fun copySingleFile(context: Context, assetFilePath: String, targetFile: File) {
        try {
            // 使用 Kotlin 的 .use 块，它可以保证无论是否发生异常，流都会被安全关闭
            context.assets.open(assetFilePath).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    // inputStream.copyTo() 是 Kotlin 标准库极其强大的扩展函数，自带 buffer
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            // 如果 open() 抛出异常，说明这个路径很可能是一个空文件夹，而不是文件。
            // 对于空文件夹，我们在上一层已经 mkdirs() 过了，这里直接忽略即可。
            Logger.e(TAG, "跳过无法打开的文件 (可能是空目录): $assetFilePath")
        }
    }
}