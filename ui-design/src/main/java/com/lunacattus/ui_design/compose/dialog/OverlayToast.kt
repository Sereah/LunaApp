package com.lunacattus.ui_design.compose.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 一个可自动消失的悬浮Toast提示。
 * 当 `event` 的值改变时，会显示一个带有淡入淡出动画的Toast。
 *
 * @param modifier 用于自定义Toast容器的修饰符。
 * @param event 一个包含要显示的消息和唯一ID的Pair。当此事件（特别是ID）发生变化时，会触发Toast。设置为null则不显示。
 * @param duration Toast可见的持续时间（以毫秒为单位）。
 */
@Composable
fun OverlayToast(
    modifier: Modifier = Modifier,
    event: Pair<String, Long>?,
    duration: Long = 2000
) {
    if (event == null) return

    val (message, id) = event
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        visible = true
        delay(duration)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .background(
                        Color.Black.copy(alpha = 0.8f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    message,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
