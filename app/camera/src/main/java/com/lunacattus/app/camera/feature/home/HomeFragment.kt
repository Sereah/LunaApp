package com.lunacattus.app.camera.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.lunacattus.app.camera.base.BaseFragment
import com.lunacattus.app.camera.databinding.FragmentHomeBinding
import com.lunacattus.app.camera.feature.home.mvi.HomeUIState
import com.lunacattus.app.camera.feature.home.mvi.HomeUiEffect
import com.lunacattus.app.camera.feature.home.mvi.HomeUiIntent
import com.lunacattus.app.camera.feature.home.mvi.HomeViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment :
    BaseFragment<FragmentHomeBinding, HomeUiIntent, HomeUIState, HomeUiEffect, HomeViewModel>(
        FragmentHomeBinding::inflate
    ) {

    companion object {
        const val TAG = "HomeFragment"
    }

    override val viewModel: HomeViewModel by viewModels()

    override fun handleSideEffect(effect: HomeUiEffect) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun collectUiState() {
        Logger.d(TAG, "collectUiState, $stateCollector")

    }

}