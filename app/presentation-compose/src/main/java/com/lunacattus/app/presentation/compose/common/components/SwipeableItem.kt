package com.lunacattus.app.presentation.compose.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeToRevealItem(
    modifier: Modifier = Modifier,
    actionWidth: Dp = 80.dp,
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
            val coerced = proposed.coerceIn(-maxOffsetPx, 0f)
            offsetX.snapTo(coerced)
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                revealContent.invoke(close)
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = draggableState,
                    onDragStopped = {
                        scope.launch {
                            val settleTo = if (offsetX.value <= -maxOffsetPx / 2f) {
                                -maxOffsetPx
                            } else {
                                0f
                            }
                            offsetX.animateTo(settleTo, spring())
                        }
                    }
                )
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
            .height(60.dp),
        actionWidth = 100.dp,
        revealContent = {
            Text(text = "添加", modifier = Modifier.clickable {
                it.invoke()
            })
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("这是一个可左滑的项", modifier = Modifier.padding(start = 16.dp))
        }
    }
}

