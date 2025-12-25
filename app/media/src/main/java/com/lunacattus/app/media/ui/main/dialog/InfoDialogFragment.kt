package com.lunacattus.app.media.ui.main.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.lunacattus.app.media.R
import com.lunacattus.app.media.databinding.DialogInfoBinding
import com.lunacattus.app.media.ui.base.BaseDialogFragment
import com.lunacattus.app.media.ui.main.mvi.MainSideEffect
import com.lunacattus.app.media.ui.main.mvi.MainUiIntent
import com.lunacattus.app.media.ui.main.mvi.MainUiState
import com.lunacattus.app.media.ui.main.mvi.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoDialogFragment : BaseDialogFragment<
        DialogInfoBinding,
        MainUiIntent,
        MainUiState,
        MainSideEffect,
        MainViewModel>(
    DialogInfoBinding::inflate
) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.global_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateCollector.collectProperty(MainUiState::infoDialogData) { info ->
            binding.tvTitle.text = info.dialogTitle
            binding.tvMessage.text = info.dialogMessage
        }
    }

    override fun provideDialogConfig(): DialogConfig {
        return DialogConfig(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            marginX = 40,
            gravity = Gravity.CENTER,
            hasCustomBackground = true,
            windowAnimations = R.style.AnimationScaleFade
        )
    }
}
