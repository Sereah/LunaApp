package com.lunacattus.browser

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import androidx.core.content.withStyledAttributes
import androidx.core.view.isEmpty

/**
 * TabSliderView —— 替代 TabLayout 的自定义滑动选项卡控件
 *
 * 交互规则：
 *  - 点击 track 任意位置 → 弹簧动画切换到对应 tab
 *  - 按住滑块拖动       → 跟手移动，松手弹簧吸附到最近 tab
 *  - 在 track 空白处滑动 → 不移动滑块，松手视为点击
 *
 * XML 属性（tsv_ 前缀避免与系统/三方库冲突）：
 *  - tsv_trackColor        : track 背景色，默认 #E0E0E0
 *  - tsv_thumbColor        : 滑块背景色，默认 #FFFFFF
 *  - tsv_trackCornerRadius : track 圆角，默认 height/2（胶囊）
 *  - tsv_thumbCornerRadius : 滑块圆角，默认同 trackCornerRadius
 *  - tsv_thumbPadding      : 滑块内边距，默认 4dp
 *  - tsv_animDuration      : 保留字段，弹簧动画不使用此值
 *  - tsv_thumbElevation    : 滑块软阴影半径，默认 4dp
 *
 * 依赖（build.gradle）：
 *  implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
 */
class TabSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // ── 监听接口 ─────────────────────────────────────────────────
    interface OnTabSelectedListener {
        fun onTabSelected(index: Int, view: View)
        fun onTabReselected(index: Int, view: View) {}
    }

    // ── 公开属性 ─────────────────────────────────────────────────
    var tsvTrackColor: Int = "#E0E0E0".toColorInt()
        set(value) { field = value; invalidate() }

    var tsvThumbColor: Int = Color.WHITE
        set(value) { field = value; invalidate() }

    var tsvTrackCornerRadius: Float = -1f
        set(value) { field = value; invalidate() }

    var tsvThumbCornerRadius: Float = -1f
        set(value) { field = value; invalidate() }

    var tsvThumbPadding: Float = dp(4f)
        set(value) { field = value; requestLayout() }

    var tsvAnimDuration: Long = 300L

    var tsvThumbElevation: Float = dp(4f)
        set(value) { field = value; invalidate() }

    var selectedIndex: Int = 0
        private set

    var listener: OnTabSelectedListener? = null

    // ── Paint ────────────────────────────────────────────────────
    private val trackPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── 滑块位置 ─────────────────────────────────────────────────
    private var thumbLeft: Float = 0f

    private val thumbHolder = FloatValueHolder(0f)
    private val springAnim = SpringAnimation(thumbHolder).apply {
        spring = SpringForce().apply {
            stiffness    = SpringForce.STIFFNESS_MEDIUM
            dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        }
        addUpdateListener { _, value, _ ->
            thumbLeft = value
            invalidate()
        }
    }

    // ── 触摸状态机 ───────────────────────────────────────────────
    /**
     * IDLE       — 按下但尚未判定，或落点在 track 空白处
     * THUMB_DRAG — 手指落在滑块上，正在跟手拖动
     */
    private enum class TouchMode { IDLE, THUMB_DRAG }
    private var touchMode = TouchMode.IDLE
    private var downX = 0f
    private var downY = 0f
    private var dragStartThumbLeft = 0f
    private val gestureThreshold = dp(6f)

    // ── 初始化 ───────────────────────────────────────────────────
    init {
        setWillNotDraw(false)
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.TabSliderView) {
                tsvTrackColor = getColor(R.styleable.TabSliderView_tsv_trackColor, tsvTrackColor)
                tsvThumbColor = getColor(R.styleable.TabSliderView_tsv_thumbColor, tsvThumbColor)
                tsvTrackCornerRadius =
                    getDimension(R.styleable.TabSliderView_tsv_trackCornerRadius, -1f)
                tsvThumbCornerRadius =
                    getDimension(R.styleable.TabSliderView_tsv_thumbCornerRadius, -1f)
                tsvThumbPadding =
                    getDimension(R.styleable.TabSliderView_tsv_thumbPadding, tsvThumbPadding)
                tsvAnimDuration = getInt(R.styleable.TabSliderView_tsv_animDuration, 300).toLong()
                tsvThumbElevation =
                    getDimension(R.styleable.TabSliderView_tsv_thumbElevation, tsvThumbElevation)
            }
        }
    }

    // ── 公开 API ─────────────────────────────────────────────────

    fun setTabViews(views: List<View>) {
        removeAllViews()
        views.forEach { it.isClickable = false; addView(it) }
        selectedIndex = 0
        setThumbLeftImmediate(0f)
        invalidate()
    }

    fun setTabViews(vararg views: View) = setTabViews(views.toList())

    fun selectTab(index: Int, animate: Boolean = true) {
        require(index in 0 until childCount) { "index $index out of bounds" }
        val targetLeft = index * tabWidth()
        if (animate) springTo(targetLeft) else setThumbLeftImmediate(targetLeft)
        if (index != selectedIndex) {
            selectedIndex = index
            listener?.onTabSelected(index, getChildAt(index))
        } else {
            listener?.onTabReselected(index, getChildAt(index))
        }
    }

    // ── 布局 ─────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isEmpty()) return
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val childW = w / childCount
        val childH = (h - tsvThumbPadding * 2).toInt()
        val ws = MeasureSpec.makeMeasureSpec(childW, MeasureSpec.EXACTLY)
        val hs = MeasureSpec.makeMeasureSpec(childH, MeasureSpec.EXACTLY)
        for (i in 0 until childCount) getChildAt(i).measure(ws, hs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isEmpty()) return
        val childW = tabWidth().toInt()
        val childH = (height - tsvThumbPadding * 2).toInt()
        val top = tsvThumbPadding.toInt()
        for (i in 0 until childCount) {
            val left = i * childW
            getChildAt(i).layout(left, top, left + childW, top + childH)
        }
        if (changed) setThumbLeftImmediate(selectedIndex * tabWidth())
    }

    // ── 绘制 ─────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        val trackR = if (tsvTrackCornerRadius < 0) height / 2f else tsvTrackCornerRadius
        val thumbR = if (tsvThumbCornerRadius < 0) trackR else tsvThumbCornerRadius
        val tw = tabWidth()

        trackPaint.color = tsvTrackColor
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), trackR, trackR, trackPaint)

        if (tsvThumbElevation > 0) {
            shadowPaint.color = Color.argb(50, 0, 0, 0)
            shadowPaint.maskFilter = BlurMaskFilter(tsvThumbElevation, BlurMaskFilter.Blur.NORMAL)
            canvas.drawRoundRect(
                thumbLeft + tsvThumbPadding,
                tsvThumbPadding + tsvThumbElevation * 0.4f,
                thumbLeft + tw - tsvThumbPadding,
                height - tsvThumbPadding + tsvThumbElevation * 0.4f,
                thumbR, thumbR, shadowPaint
            )
        }

        thumbPaint.color = tsvThumbColor
        thumbPaint.maskFilter = null
        canvas.drawRoundRect(
            thumbLeft + tsvThumbPadding,
            tsvThumbPadding,
            thumbLeft + tw - tsvThumbPadding,
            height - tsvThumbPadding,
            thumbR, thumbR, thumbPaint
        )
    }

    // ── 触摸 ─────────────────────────────────────────────────────

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                dragStartThumbLeft = thumbLeft
                // 只有落在滑块上才可能拦截
                touchMode = if (isTouchOnThumb(ev.x)) TouchMode.THUMB_DRAG else TouchMode.IDLE
            }
            MotionEvent.ACTION_MOVE -> {
                // 仅在 THUMB_DRAG 模式下，水平移动超过阈值才拦截
                if (touchMode == TouchMode.THUMB_DRAG) {
                    val dx = abs(ev.x - downX)
                    val dy = abs(ev.y - downY)
                    if (dx > gestureThreshold && dx > dy) {
                        springAnim.cancel()
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {

            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                dragStartThumbLeft = thumbLeft
                touchMode = if (isTouchOnThumb(ev.x)) TouchMode.THUMB_DRAG else TouchMode.IDLE
                if (touchMode == TouchMode.THUMB_DRAG) springAnim.cancel()
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchMode == TouchMode.THUMB_DRAG) {
                    val dx = ev.x - downX
                    val maxLeft = width - tabWidth()
                    setThumbLeftImmediate((dragStartThumbLeft + dx).coerceIn(0f, maxLeft))
                    // 经过某个 tab 时实时回调
                    val draggingIndex = nearestIndex()
                    if (draggingIndex != selectedIndex) {
                        selectedIndex = draggingIndex
                        listener?.onTabSelected(draggingIndex, getChildAt(draggingIndex))
                    }
                }
                // IDLE（空白处）：什么都不做，滑块静止不动
            }

            MotionEvent.ACTION_UP -> {
                when (touchMode) {
                    TouchMode.THUMB_DRAG -> {
                        // 松手：弹簧吸附到最近 tab
                        snapToNearest()
                    }
                    TouchMode.IDLE -> {
                        // 没有发生拖动 → 视为点击，切换到点击位置对应的 tab
                        if (abs(ev.x - downX) < gestureThreshold && abs(ev.y - downY) < gestureThreshold) {
                            val clickedIndex = (ev.x / tabWidth()).toInt().coerceIn(0, childCount - 1)
                            selectTab(clickedIndex)
                        }
                    }
                }
                touchMode = TouchMode.IDLE
            }

            MotionEvent.ACTION_CANCEL -> {
                if (touchMode == TouchMode.THUMB_DRAG) {
                    // 拖动被外部打断，弹回原位
                    springTo(selectedIndex * tabWidth())
                }
                touchMode = TouchMode.IDLE
            }
        }
        return true
    }

    // ── 动画 ─────────────────────────────────────────────────────

    private fun springTo(targetLeft: Float) {
        springAnim.cancel()
        thumbHolder.value = thumbLeft
        springAnim.setStartValue(thumbLeft)
        springAnim.animateToFinalPosition(targetLeft)
    }

    private fun setThumbLeftImmediate(value: Float) {
        springAnim.cancel()
        thumbLeft = value
        thumbHolder.value = value
        invalidate()
    }

    // ── 工具 ─────────────────────────────────────────────────────

    private fun isTouchOnThumb(x: Float): Boolean {
        val slop = dp(4f)
        return x >= thumbLeft - slop && x <= thumbLeft + tabWidth() + slop
    }

    private fun nearestIndex(): Int {
        val tw = tabWidth()
        return if (tw == 0f) 0 else (thumbLeft / tw).roundToInt().coerceIn(0, childCount - 1)
    }

    private fun snapToNearest() {
        val nearest = nearestIndex()
        springTo(nearest * tabWidth())
        if (nearest != selectedIndex) {
            selectedIndex = nearest
            listener?.onTabSelected(nearest, getChildAt(nearest))
        } else {
            listener?.onTabReselected(nearest, getChildAt(nearest))
        }
    }

    private fun tabWidth(): Float = if (childCount == 0) 0f else width.toFloat() / childCount

    private fun dp(value: Float) = value * resources.displayMetrics.density
}