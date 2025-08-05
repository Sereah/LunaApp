package com.lunacattus.app.camera.feature.two

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.lunacattus.app.camera.base.BaseFragment
import com.lunacattus.app.camera.feature.two.mvi.TwoUIState
import com.lunacattus.app.camera.feature.two.mvi.TwoUiEffect
import com.lunacattus.app.camera.feature.two.mvi.TwoUiIntent
import com.lunacattus.app.camera.feature.two.mvi.TwoViewModel
import com.lunacattus.app.camera.R
import com.lunacattus.app.camera.databinding.FragmentTwoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TwoFragment :
    BaseFragment<FragmentTwoBinding, TwoUiIntent, TwoUIState, TwoUiEffect, TwoViewModel>(
        FragmentTwoBinding::inflate
    ) {

    override val viewModel: TwoViewModel by hiltNavGraphViewModels(R.id.two_navigation)

    override fun handleSideEffect(effect: TwoUiEffect) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}