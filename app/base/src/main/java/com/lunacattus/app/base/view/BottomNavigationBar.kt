package com.lunacattus.app.base.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.lunacattus.app.base.R
import com.lunacattus.common.util.dpToPx

class BottomNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var items: List<BottomNavItem> = emptyList()
    private var selectedItemPosition: Int = -1
    private var itemOnClickListener: ((position: Int, item: BottomNavItem) -> Unit)? = null
    private val animationDuration = 150L // Animation duration in milliseconds

    init {
        orientation = HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.rounded_corners_background)
        elevation = 6f.dpToPx(context)
    }

    fun setItems(items: List<BottomNavItem>, defaultPosition: Int = 0) {
        if (items.isEmpty() || defaultPosition < 0 || defaultPosition >= items.size) {
            throw IllegalArgumentException("Invalid setItems")
        }
        this.items = items
        this.selectedItemPosition = defaultPosition
        removeAllViews()
        items.forEachIndexed { index, item ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_botton_item, this, false)
            val itemTitle = itemView.findViewById<TextView>(R.id.title)
            val itemIcon = itemView.findViewById<ImageView>(R.id.icon)
            itemTitle.apply {
                text = item.title
                setTextColor(ContextCompat.getColor(context, R.color.bottomItemUnSelected))
                textSize = 13f
            }
            itemIcon.setImageResource(item.iconResId)
            itemView.setOnClickListenerWithDebounce {
                onItemClick(index)
            }

            addView(itemView)
        }
        selectItem(defaultPosition, false)
    }

    fun selectItem(position: Int, notifyListener: Boolean = true) {
        if (selectedItemPosition != -1 && selectedItemPosition < childCount) {
            val preItemView = getChildAt(selectedItemPosition)
            val preItemTitle = preItemView.findViewById<TextView>(R.id.title)
            animateTextSize(preItemTitle, 15f, 13f)
            preItemTitle.setTextColor(ContextCompat.getColor(context, R.color.bottomItemUnSelected))
            preItemView.findViewById<ImageView>(R.id.icon).apply {
                setImageResource(items[selectedItemPosition].iconResId)
            }
        }

        if (position < 0 || position >= childCount) {
            // Handle invalid position, perhaps log an error or do nothing
            return
        }

        val currentItemView = getChildAt(position)
        val currentItemTitle = currentItemView.findViewById<TextView>(R.id.title)
        animateTextSize(currentItemTitle, 13f, 15f)
        currentItemTitle.setTextColor(ContextCompat.getColor(context, R.color.bottomItemSelected))
        currentItemView.findViewById<ImageView>(R.id.icon).apply {
            setImageResource(items[position].selectedIconResId)
        }
        selectedItemPosition = position
        if (notifyListener) {
            itemOnClickListener?.invoke(position, items[position])
        }
    }

    private fun animateTextSize(textView: TextView, startSize: Float, endSize: Float) {
        ValueAnimator.ofFloat(startSize, endSize).apply {
            duration = animationDuration
            addUpdateListener { animator ->
                textView.textSize = animator.animatedValue as Float
            }
            start()
        }
    }

    fun setOnItemClickListener(listener: (position: Int, item: BottomNavItem) -> Unit) {
        itemOnClickListener = listener
    }

    private fun onItemClick(position: Int) {
        if (position == selectedItemPosition) {
            return
        }
        selectItem(position)
    }

}

data class BottomNavItem(
    val id: String,
    val title: String,
    val iconResId: Int,
    val selectedIconResId: Int,
    val data: Any? = null
)