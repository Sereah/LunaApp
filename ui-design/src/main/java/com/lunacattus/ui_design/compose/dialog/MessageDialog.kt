package com.lunacattus.ui_design.compose.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.lunacattus.ui_design.compose.clickableWithDebounce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 一个居中显示的、可定制的消息对话框，带有弹出和消失的动画效果。
 *
 * @param onDismissRequest 当请求关闭对话框时调用的回调（例如，点击外部区域或按返回键）。
 * @param title 对话框的可选标题。
 * @param message 要在对话框中显示的多行文本消息。
 * @param containerColor 对话框容器的背景颜色。
 * @param titleColor 标题文本的颜色。
 * @param messageColor 消息文本的颜色。
 * @param cornerRadius 对话框的圆角半径。
 * @param maxHeight 对话框内容区域的最大高度。如果内容超过此高度，将变得可滚动。
 * @param dimAmount 对话框背景的昏暗程度，范围从 0.0f（完全透明）到 1.0f（完全不透明）。
 */
@Composable
fun MessageDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    message: String,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    cornerRadius: Dp = 16.dp,
    maxHeight: Dp = 400.dp,
    dimAmount: Float = 0.2f
) {
    val animationDuration = 250
    val scope = rememberCoroutineScope()
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    // 封装带动画的关闭逻辑
    val dismissWithAnimation = {
        scope.launch {
            visibleState.targetState = false
            delay(animationDuration.toLong())
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = { dismissWithAnimation() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(dimAmount)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickableWithDebounce { dismissWithAnimation() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = tween(animationDuration)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(animationDuration)
                ),
                exit = fadeOut(animationSpec = tween(animationDuration)) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(animationDuration)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 320.dp) // 限制最大宽度
                        .clickableWithDebounce {} // 阻止点击事件传递到外部
                    ,
                    shape = RoundedCornerShape(cornerRadius),
                    color = containerColor
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                color = titleColor,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        CompositionLocalProvider(LocalOverscrollFactory provides null) {
                            Box(modifier = Modifier.heightIn(max = maxHeight)) {
                                Text(
                                    text = message,
                                    color = messageColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageDialogShortPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        MessageDialog(
            onDismissRequest = {},
            title = "Short Message",
            message = "This is a short message that fits perfectly within the dialog."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageDialogLongPreview() {
    val longMessage =
        "This is a very long message designed to test the scrolling behavior of the dialog. ".repeat(
            20
        )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        MessageDialog(
            onDismissRequest = {},
            title = "Long Message Title",
            message = longMessage
        )
    }
}
