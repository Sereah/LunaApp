package com.lunacattus.browser

import android.R.attr.label
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.jessyan.autosize.AutoSize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AutoSizeUtil.fixDensity(this, 1920f, true)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tabSlider = findViewById<TabSliderView>(R.id.tabSlider)
        val labels = listOf("首页", "发现", "消息", "我的")
        val tabViews = labels.map { label ->
            TextView(this).apply {
                text = label
                textSize = 28f
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.CENTER
                // 可以在这里设置任意复杂的自定义 View
            }
        }
        tabSlider.setTabViews(tabViews)
        tabSlider.listener = object : TabSliderView.OnTabSelectedListener {
            override fun onTabSelected(index: Int, view: android.view.View) {
                // 切换到新 tab，更新 UI（比如切换 Fragment、更新文字颜色等）
                tabViews.forEachIndexed { i, tv ->
                    (tv as TextView).setTextColor(
                        if (i == index) "#1976D2".toColorInt() else Color.BLACK
                    )
                }
                // 联动 ViewPager2 示例：
                // viewPager.currentItem = index
                println("选中第 $index 个 tab: $label")
            }

            override fun onTabReselected(index: Int, view: android.view.View) {
                // 重复点击同一 tab（可选处理，例如回到顶部）
                println("重复选中第 $index 个 tab")
            }
        }
//        val tabSlider = TabSliderView(this).apply {
//            trackColor = "#EEEEEE".toColorInt()
//            thumbColor = Color.WHITE
//            trackCornerRadius = 24f   // px，-1 表示胶囊形
//            thumbCornerRadius = -1f   // 跟随 trackCornerRadius
//            thumbPadding = 6f    // px
//            thumbElevation = 8f    // px
//            animDuration = 300L
//        }
//        val params = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            96 // 例如 48dp
//        )
//        findViewById<ConstraintLayout>(R.id.main).addView(tabSlider, params)
//        val labels = listOf("首页", "发现", "消息", "我的")
//        val tabViews = labels.map { label ->
//            TextView(this).apply {
//                text = label
//                textSize = 14f
//                setTextColor(Color.BLACK)
//                gravity = android.view.Gravity.CENTER
//                // 可以在这里设置任意复杂的自定义 View
//            }
//        }
//        tabSlider.setTabViews(tabViews)
    }
}