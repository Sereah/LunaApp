package com.lunacattus.app.base.compose.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StrokeStyle(
    val width: Dp = 4.dp,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val glowRadius: Dp? = 4.dp
)

@Composable
fun CircleLoader(
    modifier: Modifier,
    isVisible: Boolean,
    color: Color,
    secondColor: Color? = color,
    tailLength: Float = 140f,
    smoothTransition: Boolean = true,
    strokeStyle: StrokeStyle = StrokeStyle(),
    cycleDuration: Int = 1400
) {

    val transition = rememberInfiniteTransition()
    val spinAngel by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val tailToDisplay = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        val targetTail = if (isVisible) tailLength else 0f
        if (smoothTransition) {
            tailToDisplay.animateTo(targetTail, tween(cycleDuration, easing = LinearEasing))
        } else {
            tailToDisplay.snapTo(targetTail)
        }
    }

    Canvas(
        modifier
            .rotate(spinAngel)
            .aspectRatio(1f)
    ) {
        listOfNotNull(color, secondColor).forEachIndexed { index, color ->
            rotate(if (index == 0) 0f else 180f) {
                val brush = Brush.sweepGradient(
                    0f to Color.Transparent,
                    tailToDisplay.value / 360f to color,
                    1f to Color.Transparent
                )
                val paint = setupPaint(strokeStyle, brush)
                drawIntoCanvas { canvas ->
                    canvas.drawArc(
                        rect = size.toRect(),
                        startAngle = 0f,
                        sweepAngle = tailToDisplay.value,
                        useCenter = false,
                        paint = paint
                    )
                }
            }
        }
    }

}

fun DrawScope.setupPaint(strokeStyle: StrokeStyle, brush: Brush): Paint {
    val paint = Paint().apply {
        isAntiAlias = true
        style = PaintingStyle.Stroke
        strokeWidth = strokeStyle.width.toPx()
        strokeCap = strokeStyle.strokeCap
        brush.applyTo(size, this, 1f)
    }
    strokeStyle.glowRadius?.let { radius ->
        paint.asFrameworkPaint().setShadowLayer(
            radius.toPx(), 0f, 0f, android.graphics.Color.WHITE
        )
    }
    return paint
}

@Preview
@Composable
fun CircleLoaderPre() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Black)
    ) {

        var isVisible by remember { mutableStateOf(false) }

        CircleLoader(
            color = Color(0xFFE91E63),
            secondColor = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center),
            isVisible = isVisible,
            tailLength = 300f,
            cycleDuration = 1000
        )
        Button(
            onClick = { isVisible = !isVisible },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = if (isVisible) "stop" else "start",
                color = Color.White
            )
        }
    }
}