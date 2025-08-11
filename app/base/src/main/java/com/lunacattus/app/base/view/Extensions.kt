package com.lunacattus.app.base.view

import android.view.View

inline fun View.setOnClickListenerWithDebounce(
    debounceTime: Long = 500,
    crossinline action: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            action(it)
        }
    }
}