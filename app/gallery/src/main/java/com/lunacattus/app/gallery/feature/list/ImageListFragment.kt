package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lunacattus.app.base.common.dpToPx
import com.lunacattus.app.base.view.ItemSpacingDecoration
import com.lunacattus.app.base.view.base.BaseFragment
import com.lunacattus.app.gallery.R
import com.lunacattus.app.gallery.databinding.ItemViewpagerPageBinding
import com.lunacattus.app.gallery.feature.list.ListFragment.Companion.SPACE_COUNT
import com.lunacattus.app.gallery.feature.list.mvi.ListUiEffect
import com.lunacattus.app.gallery.feature.list.mvi.ListUiIntent
import com.lunacattus.app.gallery.feature.list.mvi.ListUiState
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.lunacattus.app.base.R as baseR

@AndroidEntryPoint
class ImageListFragment :
    BaseFragment<ItemViewpagerPageBinding, ListUiIntent, ListUiState, ListUiEffect, ListViewModel>(
        ItemViewpagerPageBinding::inflate
    ) {

    private lateinit var listAdapter: GalleryAdapter
    override val viewModel: ListViewModel by activityViewModels()

    override fun handleSideEffect(effect: ListUiEffect) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = GalleryAdapter(requireContext()) {
            findNavController().navigate(
                resId = R.id.action_list_to_play,
                args = null,
                navOptions = navOptions {
                    anim {
                        enter = baseR.anim.slide_in_right
                        exit = baseR.anim.slide_out_left
                        popEnter = baseR.anim.slide_in_left
                        popExit = baseR.anim.slide_out_right
                    }
                }
            )
        }
        binding.root.apply {
            adapter = listAdapter
            layoutManager = GridLayoutManager(context, SPACE_COUNT, RecyclerView.VERTICAL, false)
            addItemDecoration(
                ItemSpacingDecoration(
                    5f.dpToPx(this@ImageListFragment.requireContext()).toInt(), SPACE_COUNT
                )
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.imageFlow.collectLatest {
                    listAdapter.submitData(it)
                }
            }
        }
    }
}