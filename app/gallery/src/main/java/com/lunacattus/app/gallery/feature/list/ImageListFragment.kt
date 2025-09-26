package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.lunacattus.app.gallery.base.StateCollectorDelegate.CollectConfig
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.gallery.feature.list.mvi.ListUiEffect
import com.lunacattus.app.gallery.feature.list.mvi.ListUiIntent
import com.lunacattus.app.gallery.feature.list.mvi.ListUiState
import com.lunacattus.app.gallery.feature.list.mvi.ListViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageListFragment : BaseListFragment() {

    override val viewModel: ListViewModel by activityViewModels()

    override fun handleSideEffect(effect: ListUiEffect) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dispatchUiIntent(ListUiIntent.LoadMoreImage)
        stateCollector.collectState(
            config = CollectConfig<ListUiState.Success, List<Gallery>>(
                mapFn = { it.imageList }
            )
        ) {
            Logger.d(TAG, "collect image list: ${it.size}")
            val firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition()
            listAdapter.submitList(it) {
                if (firstVisibleItem == 0) {
                    binding.root.scrollToPosition(0)
                }
            }
        }
    }

    override fun loadMore() {
        dispatchUiIntent(ListUiIntent.LoadMoreImage)
    }

    companion object {
        const val TAG = "ImageListFragment"
    }
}