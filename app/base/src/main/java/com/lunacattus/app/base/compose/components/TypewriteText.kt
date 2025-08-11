package com.lunacattus.app.base.compose.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TypewriteText(
    text: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    skipToEnd: Boolean = false,
    spec: AnimationSpec<Int> = tween(durationMillis = text.length * 100, easing = LinearEasing),
    style: TextStyle = LocalTextStyle.current,
    preoccupySpace: Boolean = true,
    onComplement: () -> Unit = {}
) {
    var textToAnimate by remember { mutableStateOf("") }

    val index = remember {
        Animatable(initialValue = 0, typeConverter = Int.VectorConverter)
    }

    LaunchedEffect(isVisible, text, skipToEnd) {
        if (!isVisible) {
            index.snapTo(0)
            textToAnimate = ""
            return@LaunchedEffect
        }

        textToAnimate = text

        if (skipToEnd) {
            // 立即跳到完成状态
            index.snapTo(text.length)
            onComplement()
        } else {
            // 正常执行动画
            index.animateTo(text.length, spec)
            onComplement()
        }
    }

    // 包含动画文本和静态文本的Box
    Box(modifier = modifier) {
        if (preoccupySpace && index.isRunning) {
            // 预占空间的透明文本
            Text(
                text = text,
                style = style,
                modifier = Modifier.alpha(0f)
            )
        }

        // 根据当前index值显示动画文本
        Text(
            text = textToAnimate.substring(0, index.value),
            style = style
        )
    }
}

@Composable
fun quoteTextStyle() = TextStyle(
    fontSize = 16.sp,
    lineHeight = 18.sp,
    fontFamily = FontFamily.Serif,
    fontStyle = FontStyle.Italic,
    color = Color(0xFF9D506E)
)

@Preview
@Composable
fun TypewriteTextPre() {
    Column(
        modifier = Modifier
            .padding(50.dp)
            .width(300.dp)
    ) {
        val text = "Haha, Isn't the animation of this typewriter great?"
        val author = "Glacien"
        TypewriteText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = quoteTextStyle(),
            spec = tween(
                durationMillis = text.length * 100,
                easing = LinearEasing
            )
        )
        TypewriteText(
            text = "-- $author",
            modifier = Modifier.align(Alignment.End),
            style = quoteTextStyle(),
            spec = tween(
                durationMillis = author.length * 100,
                delayMillis = text.length * 100 + 300,
                easing = LinearEasing
            )
        )
    }
}