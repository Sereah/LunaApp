package com.lunacattus.app.presentation.view.feature.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
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

    override val viewModel: HomeViewModel by viewModels()

    override fun handleSideEffect(effect: HomeUiEffect) {
        when (effect) {
            HomeUiEffect.NavToFeatureOne -> {
                findNavController().navigate(
                    resId = R.id.action_home_to_one,
                    args = null,
                    navOptions = navOptions {
                        anim {
                            enter = R.anim.slide_in_right
                            exit = R.anim.hold
                            popEnter = R.anim.hold
                            popExit = R.anim.slide_out_right
                        }
                    }
                )
            }

            HomeUiEffect.NavToFeatureTwo -> {
                findNavController().navigate(
                    resId = R.id.action_home_to_two,
                    args = null,
                    navOptions = navOptions {
                        anim {
                            enter = R.anim.slide_in_right
                            exit = R.anim.slide_out_left
                            popEnter = R.anim.slide_in_left
                            popExit = R.anim.slide_out_right
                        }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonOne.setOnClickListener {
            dispatchUiIntent(HomeUiIntent.ClickButtonOne)
        }
        binding.buttonTwo.setOnClickListener {
            dispatchUiIntent(HomeUiIntent.ClickButtonTwo)
        }
        collectUiState()
    }

    @SuppressLint("SetTextI18n")
    private fun collectUiState() {
        Logger.d(TAG, "collectUiState, $stateCollector")
        stateCollector.collectState<HomeUIState.Loading> {
            binding.state.text = "状态：Loading"
        }

        stateCollector.collectState(
            config = StateCollectorDelegate.CollectConfig<HomeUIState.Success, List<String>>(
                mapFn = { it.date },
            )
        ) {
            binding.state.text = "状态：Success，数据：$it"
            binding.buttonOne.visibility = View.VISIBLE
            binding.buttonTwo.visibility = View.VISIBLE
        }
    }

}