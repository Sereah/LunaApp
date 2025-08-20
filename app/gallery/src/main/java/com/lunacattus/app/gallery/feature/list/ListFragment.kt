package com.lunacattus.app.gallery.feature.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.lunacattus.app.base.view.BottomNavItem
import com.lunacattus.app.base.view.base.BaseFragment
import com.lunacattus.app.gallery.R
import com.lunacattus.app.gallery.databinding.FragmentListBinding
import com.lunacattus.app.gallery.feature.list.adapter.PagerAdapter
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

    override val viewModel: ListViewModel by activityViewModels()

    override fun handleSideEffect(effect: ListUiEffect) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomItem()
        setupViewPager()
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(pagerCallback)
        super.onDestroyView()
    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = PagerAdapter(this@ListFragment)
            registerOnPageChangeCallback(pagerCallback)
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    private fun setBottomItem() {
        binding.bottomNavigationBar.setItems(
            listOf(
                BottomNavItem(
                    id = "Gallery",
                    title = "Gallery",
                    iconResId = R.drawable.ic_pic_video_unselected,
                    selectedIconResId = R.drawable.ic_pic_video_selected
                ),
                BottomNavItem(
                    id = "Pictures",
                    title = "Pictures",
                    iconResId = R.drawable.ic_pic_unselected,
                    selectedIconResId = R.drawable.ic_pic_selected
                ),
                BottomNavItem(
                    id = "Videos",
                    title = "Videos",
                    iconResId = R.drawable.ic_video_unselected,
                    selectedIconResId = R.drawable.ic_video_selected
                )
            )
        )
        binding.bottomNavigationBar.setOnItemClickListener { position, item ->
            binding.viewPager.setCurrentItem(position, true)
        }
    }

    private val pagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.bottomNavigationBar.selectItem(position)
        }
    }

    companion object {
        const val TAG = "ListFragment"
        const val SPACE_COUNT = 3
    }
}