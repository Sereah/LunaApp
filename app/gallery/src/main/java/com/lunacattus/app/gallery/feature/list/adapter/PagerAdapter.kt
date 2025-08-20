package com.lunacattus.app.gallery.feature.list.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lunacattus.app.gallery.feature.list.MediaListFragment
import com.lunacattus.app.gallery.feature.list.ImageListFragment
import com.lunacattus.app.gallery.feature.list.VideoListFragment

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MediaListFragment()
            1 -> ImageListFragment()
            2 -> VideoListFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int = 3

}