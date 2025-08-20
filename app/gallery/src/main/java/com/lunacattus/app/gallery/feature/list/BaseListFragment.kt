package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lunacattus.app.base.common.dpToPx
import com.lunacattus.app.base.view.ItemSpacingDecoration
import com.lunacattus.app.base.view.base.BaseFragment
import com.lunacattus.app.gallery.R
import com.lunacattus.app.gallery.databinding.ItemViewpagerPageBinding
import com.lunacattus.app.gallery.feature.list.adapter.GalleryListAdapter.Companion.TYPE_TITLE
import com.lunacattus.app.gallery.feature.list.ListFragment.Companion.SPACE_COUNT
import com.lunacattus.app.gallery.feature.list.adapter.GalleryListAdapter
import com.lunacattus.app.gallery.feature.list.mvi.ListUiEffect
import com.lunacattus.app.gallery.feature.list.mvi.ListUiIntent
import com.lunacattus.app.gallery.feature.list.mvi.ListUiState
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel.Companion.PAGE_SIZE
import dagger.hilt.android.AndroidEntryPoint
import com.lunacattus.app.base.R as baseR

@AndroidEntryPoint
abstract class BaseListFragment :
    BaseFragment<ItemViewpagerPageBinding, ListUiIntent, ListUiState, ListUiEffect, ListViewModel>(
        ItemViewpagerPageBinding::inflate
    ) {

    protected lateinit var listAdapter: GalleryListAdapter
    protected lateinit var gridLayoutManager: GridLayoutManager

    override val viewModel: ListViewModel by activityViewModels()

    override fun handleSideEffect(effect: ListUiEffect) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dispatchUiIntent(ListUiIntent.Init)
        initRecyclerView()
    }

    abstract fun loadMore()

    private fun initRecyclerView() {
        listAdapter = GalleryListAdapter {
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
        gridLayoutManager =
            GridLayoutManager(context, SPACE_COUNT, RecyclerView.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (listAdapter.getItemViewType(position)) {
                            TYPE_TITLE -> SPACE_COUNT
                            else -> 1
                        }
                    }
                }
            }
        binding.root.apply {
            adapter = listAdapter
            layoutManager = gridLayoutManager
            addItemDecoration(
                ItemSpacingDecoration(
                    space = 5f.dpToPx(this@BaseListFragment.requireContext()).toInt(),
                    spanCount = SPACE_COUNT,
                    orientation = RecyclerView.VERTICAL,
                    layoutManager = gridLayoutManager
                )
            )
            addOnScrollListener(scrollListener)
        }
    }

    override fun onDestroyView() {
        binding.root.removeOnScrollListener(scrollListener)
        super.onDestroyView()
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        private val VISIBLE_THRESHOLD = PAGE_SIZE * 0.4

        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            if (dy <= 0) return

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val lastItem = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            if (totalItemCount >= 0 && lastItem >= totalItemCount - VISIBLE_THRESHOLD) {
                loadMore()
            }
        }
    }

    companion object {
        const val TAG = "ImageListFragment"
    }
}