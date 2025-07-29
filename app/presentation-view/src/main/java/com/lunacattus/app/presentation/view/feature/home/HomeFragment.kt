package com.lunacattus.app.presentation.view.feature.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.lunacattus.app.domain.model.videoUri
import com.lunacattus.app.presentation.view.R
import com.lunacattus.app.presentation.view.base.BaseFragment
import com.lunacattus.app.presentation.view.base.StateCollectorDelegate
import com.lunacattus.app.presentation.view.databinding.FragmentHomeBinding
import com.lunacattus.app.presentation.view.feature.home.mvi.HomeUIState
import com.lunacattus.app.presentation.view.feature.home.mvi.HomeUiEffect
import com.lunacattus.app.presentation.view.feature.home.mvi.HomeUiIntent
import com.lunacattus.app.presentation.view.feature.home.mvi.HomeViewModel
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

    private lateinit var exoPlayer: ExoPlayer

    override val viewModel: HomeViewModel by viewModels()

    override fun handleSideEffect(effect: HomeUiEffect) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiState()
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri.toUri()))
            prepare()
            playWhenReady = true
            setVideoSurfaceView(binding.player)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.clearVideoSurface()
    }

    private fun collectUiState() {
        Logger.d(TAG, "collectUiState, $stateCollector")

    }

}