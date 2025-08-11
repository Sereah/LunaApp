package com.lunacattus.app.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class BaseFragment<
        VB : ViewBinding,
        INTENT : IUIIntent,
        STATE : IUIState,
        EFFECT : IUIEffect,
        VM : BaseViewModel<INTENT, STATE, EFFECT>>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    abstract val viewModel: VM
    protected abstract fun handleSideEffect(effect: EFFECT)

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var stateCollector: StateCollectorDelegate<STATE>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            _binding = inflateBinding(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateCollector = StateCollectorDelegate(
            lifecycleOwner = viewLifecycleOwner,
            uiStateFlow = viewModel.uiState
        )
        collectUiEffect()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 子fragment分发UIIntent， 传递给对应的viewmodel处理事件
     */
    protected fun dispatchUiIntent(intent: INTENT) {
        viewModel.handleUiIntent(intent)
    }

    /**
     * 接收viewmodel发送过来的一次性事件，传递给子fragment处理
     */
    private fun collectUiEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    handleSideEffect(effect)
                }
            }
        }
    }
}