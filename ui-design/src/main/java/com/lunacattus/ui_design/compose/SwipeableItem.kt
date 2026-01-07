package com.lunacattus.ui_design.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToRevealItem(
    modifier: Modifier = Modifier,
    actionWidth: Dp = 80.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface, // 背景底层颜色
    containerShape: Shape = RoundedCornerShape(12.dp), // 统一的圆角
    foregroundColor: Color = MaterialTheme.colorScheme.primary, // 前景颜色
    revealContent: @Composable (close: () -> Unit) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { actionWidth.toPx() }
    val offsetX = remember { Animatable(0f) }

    val close: () -> Unit = {
        scope.launch {
            offsetX.animateTo(0f, spring())
        }
    }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            val proposed = offsetX.value + delta
            // 限制滑动范围：只能向左滑（负值），且最大不超过 actionWidth
            val coerced = proposed.coerceIn(-maxOffsetPx, 0f)
            offsetX.snapTo(coerced)
        }
    }

    // 外层容器：负责裁剪形状和整体样式
    Box(
        modifier = modifier
            .graphicsLayer {
                shape = containerShape
                clip = true
            }
            .background(containerColor) // 这里的颜色就是滑动露出来的底部背景色
    ) {
        // 底部动作区
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidth)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            revealContent(close)
        }

        // 前景内容区
        Box(
            modifier = Modifier
                .fillMaxWidth() // 确保占满整行
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = draggableState,
                    onDragStopped = {
                        scope.launch {
                            val settleTo = if (offsetX.value <= -maxOffsetPx / 2f) -maxOffsetPx else 0f
                            offsetX.animateTo(settleTo, spring())
                        }
                    }
                )
                .background(foregroundColor) // 前景颜色
        ) {
            content()
        }
    }
}

@Composable
@Preview
fun ExampleListItem() {
    SwipeToRevealItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        actionWidth = 100.dp,
        containerColor = Color(0xFFFF4444), // 滑开后底部的红色（比如删除背景）
        containerShape = RoundedCornerShape(16.dp), // 圆角
        revealContent = { close ->
            TextButton(onClick = { close() }) {
                Text("删除", color = Color.White)
            }
        }
    ) {
        // 这里是你的 Item 内容
        ListItem(
            headlineContent = { Text("向左滑动试试") },
            supportingContent = { Text("底部圆角已同步") },
            modifier = Modifier.fillMaxSize()
        )
    }
}

