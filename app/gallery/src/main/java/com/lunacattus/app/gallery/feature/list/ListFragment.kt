package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.lunacattus.app.base.view.BaseFragment
import com.lunacattus.app.base.view.setOnClickListenerWithDebounce
import com.lunacattus.app.gallery.R
import com.lunacattus.app.base.R as baseR
import com.lunacattus.app.gallery.databinding.FragmentListBinding
import com.lunacattus.app.gallery.feature.list.mvi.ListUiEffect
import com.lunacattus.app.gallery.feature.list.mvi.ListUiIntent
import com.lunacattus.app.gallery.feature.list.mvi.ListUiState
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment :
    BaseFragment<FragmentListBinding, ListUiIntent, ListUiState, ListUiEffect, ListViewModel>(
        FragmentListBinding::inflate
    ) {

    override val viewModel: ListViewModel by viewModels()

    override fun handleSideEffect(effect: ListUiEffect) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dispatchUiIntent(ListUiIntent.Init)
        binding.text.setOnClickListenerWithDebounce {
            findNavController().navigate(
                resId = R.id.action_list_to_play,
                args = null,
                navOptions = navOptions {
                    anim {
                        enter = baseR.anim.slide_in_right
                        exit = baseR.anim.hold
                        popEnter = baseR.anim.hold
                        popExit = baseR.anim.slide_out_right
                    }
                }
            )
        }
    }
}