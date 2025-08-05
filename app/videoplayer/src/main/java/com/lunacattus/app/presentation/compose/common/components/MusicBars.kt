package com.lunacattus.app.presentation.compose.common.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MusicBars(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barColor: Color = Color.Green,
    barSpacing: Dp = 4.dp,
    minHeightFraction: Float = 0.3f, // 0..1，相对于父高度
    cycleDurationMillis: Int = 1200
) {
    require(barCount >= 1) { "barCount must be >=1" }
    val safeMinFraction = minHeightFraction.coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition()
    val phaseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cycleDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    BoxWithConstraints(modifier = modifier) {
        val maxW = maxWidth
        val maxH = maxHeight

        // 每根柱子的宽度（减去间隔）
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = if (barCount > 0) {
            ((maxW - totalSpacing) / barCount).coerceAtLeast(0.dp)
        } else 0.dp

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(barSpacing),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(barCount) { index ->
                val phaseOffset = (2 * PI / barCount * index).toFloat()
                val normalized = (sin(phaseProgress + phaseOffset) * 0.5f + 0.5f).coerceIn(0f, 1f)
                val heightFraction = lerpFloat(safeMinFraction, 1f, normalized)
                val barHeight = maxH * heightFraction

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight)
                        .background(barColor, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}


@Preview
@Composable
fun PreviewMusicBarsSized() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MusicBars(
            modifier = Modifier.size(30.dp),
            barCount = 4,
            barColor = Color.Black,
            barSpacing = 2.dp,
            minHeightFraction = 0.2f,
            cycleDurationMillis = 1000
        )
    }
}


