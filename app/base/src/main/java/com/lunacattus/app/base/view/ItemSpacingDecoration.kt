package com.lunacattus.app.base.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDecoration(
    private val space: Int,         // 间距 px
    private val spanCount: Int = 1, // 列数，线性布局=1
    private val orientation: Int = RecyclerView.VERTICAL // RecyclerView方向
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        if (spanCount == 1) {
            // 线性布局
            if (orientation == RecyclerView.VERTICAL) {
                // 纵向，只在 item 上方添加间距，第一项不加
                outRect.top = if (position == 0) 0 else space
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = 0
            } else {
                // 横向，只在 item 左边添加间距，第一项不加
                outRect.left = if (position == 0) 0 else space
                outRect.top = 0
                outRect.right = 0
                outRect.bottom = 0
            }
        } else {
            // 网格布局
            val column = position % spanCount
            val row = position / spanCount

            if (orientation == RecyclerView.VERTICAL) {
                outRect.left = column * space / spanCount
                outRect.right = space - (column + 1) * space / spanCount
                outRect.top = if (row == 0) 0 else space
                outRect.bottom = 0
            } else {
                // 横向网格
                outRect.top = row * space / spanCount
                outRect.bottom = space - (row + 1) * space / spanCount
                outRect.left = if (column == 0) 0 else space
                outRect.right = 0
            }
        }
    }
}