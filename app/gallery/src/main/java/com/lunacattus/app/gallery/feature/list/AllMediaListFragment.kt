package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lunacattus.app.base.common.dpToPx
import com.lunacattus.app.base.view.ItemSpacingDecoration
import com.lunacattus.app.base.view.base.BaseFragment
import com.lunacattus.app.domain.model.id
import com.lunacattus.app.gallery.R
import com.lunacattus.app.gallery.databinding.ItemViewpagerPageBinding
import com.lunacattus.app.gallery.feature.list.ListFragment.Companion.SPACE_COUNT
import com.lunacattus.app.gallery.feature.list.mvi.ListUiEffect
import com.lunacattus.app.gallery.feature.list.mvi.ListUiIntent
import com.lunacattus.app.gallery.feature.list.mvi.ListUiState
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.lunacattus.app.base.R as baseR

@AndroidEntryPoint
class AllMediaListFragment :
    BaseFragment<ItemViewpagerPageBinding, ListUiIntent, ListUiState, ListUiEffect, ListViewModel>(
        ItemViewpagerPageBinding::inflate
    ) {

    private lateinit var listAdapter: GalleryAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private var savedScrollState: SavedScrollState? = null
    private var isUserScroll: Boolean = false

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
        gridLayoutManager = GridLayoutManager(context, SPACE_COUNT, RecyclerView.VERTICAL, false)
        binding.root.apply {
            adapter = listAdapter
            layoutManager = gridLayoutManager
            addItemDecoration(
                ItemSpacingDecoration(
                    5f.dpToPx(this@AllMediaListFragment.requireContext()).toInt(), SPACE_COUNT
                )
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    isUserScroll = newState != RecyclerView.SCROLL_STATE_IDLE
                }

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isUserScroll) return
                    val firstPos = gridLayoutManager.findFirstVisibleItemPosition()
                    if (firstPos != RecyclerView.NO_POSITION) {
                        val vh = recyclerView.findViewHolderForAdapterPosition(firstPos)
                        val offset = vh?.itemView?.top ?: 0

                        val item = listAdapter.peek(firstPos)
                        Logger.d(TAG, "firstPos: $firstPos, offset: $offset, item: $item")
                        if (item != null) {
                            savedScrollState = SavedScrollState(
                                anchorId = item.id,
                                offset = offset
                            )
                        }
                    }
                }
            })
        }
        listAdapter.addLoadStateListener { loadStates ->
            if (loadStates.refresh is LoadState.NotLoading) {
                savedScrollState?.let { state ->
                    val targetPos =
                        listAdapter.snapshot().items.indexOfFirst { it.id == state.anchorId }
                    Logger.d(TAG, "targetPos: $targetPos")
                    if (targetPos != -1) {
                        gridLayoutManager.scrollToPositionWithOffset(targetPos, state.offset)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.galleryFlow.collectLatest {
                    listAdapter.submitData(it)
                }
            }
        }
    }

    companion object {
        const val TAG = "AllMediaListFragment"
    }
}

data class SavedScrollState(
    val anchorId: Long,   // 第一个可见元素的唯一 id
    val offset: Int       // 距离顶部的偏移（像素）
)