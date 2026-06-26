package com.lunacattus.llm.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.lunacattus.llm.R
import com.lunacattus.llm.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 模型状态
        viewModel.state.map { it.generateState }.distinctUntilChanged().onEach {
            binding.generateState.text = "$it"
        }.launchIn(lifecycleScope)

        viewModel.state.map { it.bertState }.distinctUntilChanged().onEach {
            binding.bertState.text = "$it"
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

    companion object {
        const val TAG = "MainActivity"
    }
}