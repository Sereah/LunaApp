package com.lunacattus.app.media.ui.base

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<
        VB : ViewBinding,
        INTENT : IUIIntent,
        STATE : IUIState,
        EFFECT : ISideEffect,
        VM : BaseViewModel<INTENT, STATE, EFFECT>>(
    private val inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    abstract val viewModel: VM

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var stateCollector: StateCollector<STATE>

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
        stateCollector = StateCollector(
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

    /**
     * 在具体的 DialogFragment 中重写此方法以提供自定义窗口配置。
     */
    protected open fun provideDialogConfig(): DialogConfig {
        return DialogConfig()
    }

    /**
     * 用于对话框窗口属性的配置类。
     * 所有的尺寸值（宽度、高度、边距）都以 DP 为单位。
     */
    protected class DialogConfig(
        /** 对话框宽度，单位为 DP，或使用像 MATCH_PARENT 这样的布局常量。 */
        val width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        /** 对话框高度，单位为 DP，或使用像 WRAP_CONTENT 这样的布局常量。 */
        val height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        /** 位置 */
        val gravity: Int = Gravity.CENTER,
        /** 水平边距，单位为 DP。 */
        val marginX: Int = 0,
        /** 垂直边距，单位为 DP。 */
        val marginY: Int = 0,
        /** 外部点击对话框消失 */
        val outCancelable: Boolean = true,
        /** 对话框背后的变暗程度 (0.0f 到 1.0f)。如果为 null，则使用系统默认值。 */
        val dimAmount: Float? = 0.2f,
        /** 如果你的布局有自己的形状（例如圆角），请设置为 true。 */
        val hasCustomBackground: Boolean = false,
        /** 用于窗口进入/退出动画的样式资源。 */
        @param:StyleRes val windowAnimations: Int? = null
    )

    private fun applyWindowConfiguration() {
        val config = provideDialogConfig()
        val window = dialog?.window ?: return
        val params = window.attributes

        // 1. 尺寸和水平边距
        if (config.width == WindowManager.LayoutParams.MATCH_PARENT) {
            // 当宽度为 MATCH_PARENT 时，我们手动计算宽度以实现边距效果
            val horizontalMarginInPx = config.marginX.dpToPx() * 2
            params.width = getScreenWidth() - horizontalMarginInPx
            params.x = 0 // 边距已通过宽度计算实现，x偏移应重置为0
        } else {
            // 对于 WRAP_CONTENT 或具体的DP值，正常设置宽度和x偏移
            params.width = if (config.width < 0) config.width else config.width.dpToPx()
            params.x = config.marginX.dpToPx()
        }

        // 高度和垂直边距/偏移
        params.height = if (config.height < 0) config.height else config.height.dpToPx()
        params.y = config.marginY.dpToPx()

        // 2. 位置
        params.gravity = config.gravity

        // 3. 自定义背景
        if (config.hasCustomBackground) {
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        // 4. 背景变暗程度
        config.dimAmount?.let {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            params.dimAmount = it
        }

        // 5. 动画
        config.windowAnimations?.let {
            window.setWindowAnimations(it)
        }

        // 一次性应用所有属性
        window.attributes = params
        isCancelable = config.outCancelable
    }

    // 将 DP 转换为像素的辅助方法
    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


    /**
     * 获取屏幕宽度（像素）
     */
    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }
}