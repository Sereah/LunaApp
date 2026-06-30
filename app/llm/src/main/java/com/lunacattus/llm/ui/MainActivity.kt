package com.lunacattus.llm.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.lunacattus.llm.R
import com.lunacattus.llm.databinding.ActivityMainBinding
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var pendingModelType: ModelType? = null

    private enum class ModelType { GENERATE, BERT }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onModelFileSelected(it) }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // 模型状态
        viewModel.state.map { it.generateState }.distinctUntilChanged().onEach {
            binding.generateState.text = "$it"
        }.launchIn(lifecycleScope)

        viewModel.state.map { it.bertState }.distinctUntilChanged().onEach {
            binding.bertState.text = "$it"
        }.launchIn(lifecycleScope)

        // 模型路径显示
        viewModel.state.map { it.generateModelPath }.distinctUntilChanged().onEach { path ->
            binding.generateModelPath.text = path.ifEmpty { "未选择模型文件" }
        }.launchIn(lifecycleScope)

        viewModel.state.map { it.bertModelPath }.distinctUntilChanged().onEach { path ->
            binding.bertModelPath.text = path.ifEmpty { "未选择模型文件" }
        }.launchIn(lifecycleScope)

        // 分类结果 & 耗时
        viewModel.state.map { it.classificationLabel }.distinctUntilChanged().onEach { label ->
            val idx = viewModel.state.value.classificationResult
            binding.classificationResult.text =
                if (label.isEmpty()) "" else "分类结果：$label（class $idx）"
        }.launchIn(lifecycleScope)

        viewModel.state.map { it.classificationTimeMs }.distinctUntilChanged().onEach { timeMs ->
            binding.classificationTime.text = if (timeMs != null) "耗时：${timeMs}ms" else ""
        }.launchIn(lifecycleScope)

        // 助手回复 & 耗时
        viewModel.state.map { it.assistantResponse }.distinctUntilChanged().onEach {
            binding.responseText.text = it.ifEmpty { "" }
        }.launchIn(lifecycleScope)

        viewModel.state.map { it.responseTimeMs }.distinctUntilChanged().onEach { timeMs ->
            binding.responseTime.text = if (timeMs != null) "耗时：${timeMs}ms" else ""
        }.launchIn(lifecycleScope)

        // 模型文件选择
        binding.selectGenerateModel.setOnClickListener {
            pendingModelType = ModelType.GENERATE
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        binding.selectBertModel.setOnClickListener {
            pendingModelType = ModelType.BERT
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        // 已有功能
        binding.sendClassic.setOnClickListener {
            viewModel.classify(binding.classicPrompt.text.toString())
        }

        binding.sendUserPrompt.setOnClickListener {
            val text = binding.userPrompt.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendUserPrompt(text)
            }
        }
    }

    private fun onModelFileSelected(uri: Uri) {
        val modelType = pendingModelType ?: return
        pendingModelType = null

        val fileName = resolveFileName(uri) ?: "unknown.gguf"
        Logger.d(TAG, "Selected file: $fileName, uri: $uri")

        lifecycleScope.launchWhenCreated {
            val resolvedPath = resolveToLocalPath(uri, fileName)
            if (resolvedPath != null) {
                Logger.d(TAG, "Resolved path: $resolvedPath")
                when (modelType) {
                    ModelType.GENERATE -> viewModel.initGenerateModel(resolvedPath)
                    ModelType.BERT -> viewModel.initBertModel(resolvedPath)
                }
            } else {
                Logger.e(TAG, "Failed to resolve file path for $uri")
            }
        }
    }

    private fun resolveFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return cursor.getString(nameIndex)
            }
        }
        return null
    }

    private suspend fun resolveToLocalPath(uri: Uri, fileName: String): String? =
        withContext(Dispatchers.IO) {
            // First, try to get a direct file path from the content URI
            val directPath = tryResolveDirectPath(uri)
            if (directPath != null) return@withContext directPath

            // Fallback: copy to internal storage
            Logger.d(TAG, "Direct path not available, copying to internal storage...")
            try {
                val destDir = File(filesDir, "gguf")
                destDir.mkdirs()
                val destFile = File(destDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Logger.d(TAG, "Copied to: ${destFile.absolutePath}")
                destFile.absolutePath
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to copy model file", e)
                null
            }
        }

    private fun tryResolveDirectPath(uri: Uri): String? {
        // Try _data column for file-based providers
        try {
            contentResolver.query(
                uri,
                arrayOf(android.provider.MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA)
                    if (index >= 0) {
                        val path = cursor.getString(index)
                        if (path != null && File(path).exists()) return path
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore and fall through to copy
        }
        return null
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
