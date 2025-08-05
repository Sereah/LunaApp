package com.lunacattus.app.camera.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<
        VB : ViewBinding,
        INTENT : IUIIntent,
        STATE : IUIState,
        EFFECT : IUIEffect,
        VM : BaseViewModel<INTENT, STATE, EFFECT>>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    abstract val viewModel: VM

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
        applyWindowConfiguration()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected fun dispatchUiIntent(intent: INTENT) {
        viewModel.handleUiIntent(intent)
    }

    protected open fun provideDialogConfig(): DialogConfig {
        return DialogConfig()
    }

    protected class DialogConfig(
        val widthDp: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        val heightDp: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        val gravity: Int = Gravity.CENTER,
        val marginX: Int = 0,
        val marginY: Int = 0,
        val outCancelable: Boolean = true,
    )

    private fun applyWindowConfiguration() {
        val config = provideDialogConfig()
        val window = dialog?.window ?: return
        val params = window.attributes

        params.width = config.widthDp
        params.height = config.heightDp

        params.gravity = config.gravity
        if (config.gravity != Gravity.CENTER) {
            params.x = config.marginX
            params.y = config.marginY
        }
        window.attributes = params
        isCancelable = config.outCancelable
    }
}