package com.lunacattus.app.base.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDecoration(
    private val space: Int,
    private val spanCount: Int = 1,
    private val orientation: Int = RecyclerView.VERTICAL,
    private val layoutManager: GridLayoutManager? = null // 新增，方便获取 spanSize
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        // 线性布局情况不变
        if (spanCount == 1 || layoutManager == null) {
            if (orientation == RecyclerView.VERTICAL) {
                outRect.top = if (position == 0) 0 else space
            } else {
                outRect.left = if (position == 0) 0 else space
            }
            return
        }

        // -------- 网格布局，结合 spanSizeLookup --------
        val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)   // 当前 item 占几列
        val spanIndex =
            layoutManager.spanSizeLookup.getSpanIndex(position, spanCount) // 当前 item 从哪一列开始

        if (orientation == RecyclerView.VERTICAL) {
            if (spanSize == spanCount) {
                // 独占一行（比如 title），不加左右间距，只控制上下间距
                outRect.left = 0
                outRect.right = 0
                outRect.top = if (position == 0) 0 else space
            } else {
                // 普通 item，多列布局
                outRect.left = spanIndex * space / spanCount
                outRect.right = space - (spanIndex + 1) * space / spanCount
                outRect.top = space
            }
        } else {
            // 横向网格同理
            if (spanSize == spanCount) {
                outRect.top = 0
                outRect.bottom = 0
                outRect.left = if (position == 0) 0 else space
            } else {
                outRect.top = spanIndex * space / spanCount
                outRect.bottom = space - (spanIndex + 1) * space / spanCount
                outRect.left = if (spanIndex == 0) space else 0
            }
        }
    }
}