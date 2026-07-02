package com.lunacattus.llm.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lunacattus.llm.R
import com.lunacattus.llm.databinding.FragmentGenerativeBinding
import com.lunacattus.llm.model.ModelState
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class GenerativeFragment : Fragment() {

    private val viewModel: GenerativeViewModel by viewModels()

    private var _binding: FragmentGenerativeBinding? = null
    private val binding get() = _binding!!

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onModelFileSelected(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerativeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 模型状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.generateState }.distinctUntilChanged().collect {
                    binding.generateState.text = "$it"
                }
            }
        }

        // 模型路径
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.generateModelPath }.distinctUntilChanged().collect { path ->
                    binding.generateModelPath.text = path.ifEmpty { "未选择模型文件" }
                }
            }
        }

        // 助手回复
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.assistantResponse }.distinctUntilChanged().collect {
                    binding.responseText.text = it.ifEmpty { "" }
                }
            }
        }

        // 耗时
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.responseTimeMs }.distinctUntilChanged().collect { timeMs ->
                    binding.responseTime.text = if (timeMs != null) "耗时：${timeMs}ms" else ""
                }
            }
        }

        // 模型加载完成后启用交互
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.generateState }.distinctUntilChanged().collect { state ->
                    val loaded = state is ModelState.Loaded
                    binding.sendUserPrompt.isEnabled = loaded
                    binding.userPrompt.isEnabled = loaded
                }
            }
        }

        // 按钮事件
        binding.selectGenerateModel.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        binding.sendUserPrompt.setOnClickListener {
            val text = binding.userPrompt.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendUserPrompt(text)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onModelFileSelected(uri: Uri) {
        val fileName = resolveFileName(uri) ?: "unknown.gguf"
        Logger.d(TAG, "Selected file: $fileName, uri: $uri")

        lifecycleScope.launch {
            val resolvedPath = resolveToLocalPath(uri, fileName)
            if (resolvedPath != null) {
                Logger.d(TAG, "Resolved path: $resolvedPath")
                viewModel.initModel(resolvedPath)
            } else {
                Logger.e(TAG, "Failed to resolve file path for $uri")
            }
        }
    }

    private fun resolveFileName(uri: Uri): String? {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return cursor.getString(nameIndex)
            }
        }
        return null
    }

    private suspend fun resolveToLocalPath(uri: Uri, fileName: String): String? =
        withContext(Dispatchers.IO) {
            val directPath = tryResolveDirectPath(uri)
            if (directPath != null) return@withContext directPath

            Logger.d(TAG, "Direct path not available, copying to internal storage...")
            try {
                val destDir = File(requireContext().filesDir, "gguf")
                destDir.mkdirs()
                val destFile = File(destDir, fileName)
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
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
        try {
            requireContext().contentResolver.query(
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
                        if (path != null && File(path).exists() && File(path).canRead()) return path
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore and fall through to copy
        }
        return null
    }

    companion object {
        const val TAG = "GenerativeFragment"
    }
}
