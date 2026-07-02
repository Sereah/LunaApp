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
import com.lunacattus.llm.databinding.FragmentClassificationBinding
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
class ClassificationFragment : Fragment() {

    private val viewModel: ClassificationViewModel by viewModels()

    private var _binding: FragmentClassificationBinding? = null
    private val binding get() = _binding!!

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onModelFileSelected(it) }
    }

    private var pendingModelType: ModelType? = null

    private enum class ModelType { BERT, ONNX }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── BERT (llama.cpp) 观察 ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.bertState }.distinctUntilChanged().collect {
                    binding.bertState.text = "$it"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.bertModelPath }.distinctUntilChanged().collect { path ->
                    binding.bertModelPath.text = path.ifEmpty { "未选择模型文件" }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.bertLabel }.distinctUntilChanged().collect { label ->
                    val idx = viewModel.state.value.bertResult
                    binding.bertResult.text =
                        if (label.isEmpty()) "" else "分类结果：$label（class $idx）"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.bertTimeMs }.distinctUntilChanged().collect { timeMs ->
                    binding.bertTime.text = if (timeMs != null) "耗时：${timeMs}ms" else ""
                }
            }
        }

        // 模型加载完成后启用按钮
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.bertState }.distinctUntilChanged().collect { state ->
                    val loaded = state is ModelState.Loaded
                    binding.sendBert.isEnabled = loaded
                    binding.bertPrompt.isEnabled = loaded
                }
            }
        }

        // ── ONNX Runtime 观察 ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxState }.distinctUntilChanged().collect {
                    binding.onnxState.text = "$it"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxModelPath }.distinctUntilChanged().collect { path ->
                    binding.onnxModelPath.text = path.ifEmpty { "未选择模型文件" }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxLabel }.distinctUntilChanged().collect { label ->
                    val idx = viewModel.state.value.onnxResult
                    binding.onnxResult.text =
                        if (label.isEmpty()) "" else "分类结果：$label（class $idx）"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxTimeMs }.distinctUntilChanged().collect { timeMs ->
                    binding.onnxTime.text = if (timeMs != null) "耗时：${timeMs}ms" else ""
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxState }.distinctUntilChanged().collect { state ->
                    val loaded = state is ModelState.Loaded
                    binding.sendOnnx.isEnabled = loaded
                    binding.onnxPrompt.isEnabled = loaded
                }
            }
        }

        // NPU 开关：加载中不可操作
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxState }.distinctUntilChanged().collect { state ->
                    val loading = state is ModelState.Loading
                    binding.npuSwitch.isEnabled = !loading
                }
            }
        }

        // NPU 开关状态同步
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.onnxUseNpu }.distinctUntilChanged().collect { useNpu ->
                    if (binding.npuSwitch.isChecked != useNpu) {
                        binding.npuSwitch.isChecked = useNpu
                    }
                }
            }
        }

        // ── 按钮事件 ──
        binding.selectBertModel.setOnClickListener {
            pendingModelType = ModelType.BERT
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        binding.selectOnnxModel.setOnClickListener {
            pendingModelType = ModelType.ONNX
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }

        binding.sendBert.setOnClickListener {
            viewModel.classifyBert(binding.bertPrompt.text.toString())
        }

        binding.sendOnnx.setOnClickListener {
            viewModel.classifyOnnx(binding.onnxPrompt.text.toString())
        }

        binding.npuSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.state.value.onnxUseNpu) {
                viewModel.toggleNpu(isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── 模型文件选择处理 ──

    private fun onModelFileSelected(uri: Uri) {
        val modelType = pendingModelType ?: return
        pendingModelType = null

        val fileName = resolveFileName(uri) ?: "unknown"
        Logger.d(TAG, "Selected file: $fileName, uri: $uri")

        lifecycleScope.launch {
            val resolvedPath = resolveToLocalPath(uri, fileName)
            if (resolvedPath != null) {
                Logger.d(TAG, "Resolved path: $resolvedPath")
                when (modelType) {
                    ModelType.BERT -> viewModel.initBertModel(resolvedPath)
                    ModelType.ONNX -> viewModel.initOnnxModel(resolvedPath)
                }
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
        const val TAG = "ClassificationFragment"
    }
}
