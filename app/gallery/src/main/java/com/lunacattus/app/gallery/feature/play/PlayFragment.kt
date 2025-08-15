package com.lunacattus.app.gallery.feature.play

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.lunacattus.app.base.view.BottomNavItem
import com.lunacattus.app.base.view.base.BaseFragment
import com.lunacattus.app.gallery.R
import com.lunacattus.app.gallery.databinding.FragmentPlayBinding
import com.lunacattus.app.gallery.feature.play.mvi.PlayUiEffect
import com.lunacattus.app.gallery.feature.play.mvi.PlayUiIntent
import com.lunacattus.app.gallery.feature.play.mvi.PlayUiState
import com.lunacattus.app.gallery.feature.play.mvi.PlayViewModel
import com.lunacattus.logger.Logger

class PlayFragment :
    BaseFragment<FragmentPlayBinding, PlayUiIntent, PlayUiState, PlayUiEffect, PlayViewModel>(
        FragmentPlayBinding::inflate
    ) {

    override val viewModel: PlayViewModel by hiltNavGraphViewModels(R.id.play_nav)

    override fun handleSideEffect(effect: PlayUiEffect) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}