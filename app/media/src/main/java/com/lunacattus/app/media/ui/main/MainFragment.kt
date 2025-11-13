package com.lunacattus.app.media.ui.main

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.lunacattus.app.media.R
import com.lunacattus.app.media.databinding.FragmentMainBinding
import com.lunacattus.app.media.ui.base.BaseFragment
import com.lunacattus.app.media.ui.main.mvi.MainSideEffect
import com.lunacattus.app.media.ui.main.mvi.MainUiIntent
import com.lunacattus.app.media.ui.main.mvi.MainUiState
import com.lunacattus.app.media.ui.main.mvi.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment :
    BaseFragment<FragmentMainBinding, MainUiIntent, MainUiState, MainSideEffect, MainViewModel>(
        FragmentMainBinding::inflate
    ) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.global_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnShowDialog.setOnClickListener {
            dispatchUiIntent(
                MainUiIntent.ShowInfoDialog(
                    title = "操作确认",
                    message = "这是一个通过 MVI 架构显示的对话框。"
                )
            )
        }
    }

    override fun handleSideEffect(effect: MainSideEffect) {
        when (effect) {
            is MainSideEffect.NavigateToInfoDialog -> {
                findNavController().navigate(R.id.action_main_to_info_dialog)
            }
        }
    }

}
