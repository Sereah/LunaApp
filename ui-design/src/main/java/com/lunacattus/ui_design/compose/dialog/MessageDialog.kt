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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.debouncedOnClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface MessageContent {
    data class Text(val value: String) : MessageContent
    data class Lines(val values: List<String>) : MessageContent
}

@Immutable
data class MessageDialogColors(
    val containerColor: Color,
    val titleColor: Color,
    val messageColor: Color
)

@Immutable
data class MessageDialogStyle(
    val cornerRadius: Dp,
    val maxHeight: Dp,
    val dimAmount: Float
)

@Immutable
data class DialogActions(
    val dismissOnClickOutside: Boolean = true,
    val confirmButtonText: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val cancelButtonText: String? = null,
    val onCancel: (() -> Unit)? = null
)

@Immutable
data class MessageDialogTextStyles(
    val titleFontSize: TextUnit,
    val messageFontSize: TextUnit,
    val buttonFontSize: TextUnit
)

object MessageDialogDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        titleColor: Color = MaterialTheme.colorScheme.onSurface,
        messageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): MessageDialogColors = MessageDialogColors(
        containerColor = containerColor,
        titleColor = titleColor,
        messageColor = messageColor
    )

    @Composable
    fun style(
        cornerRadius: Dp = 16.dp,
        maxHeight: Dp = 400.dp,
        dimAmount: Float = 0.5f
    ): MessageDialogStyle = MessageDialogStyle(
        cornerRadius = cornerRadius,
        maxHeight = maxHeight,
        dimAmount = dimAmount
    )

    @Composable
    fun actions(
        dismissOnClickOutside: Boolean = true,
        confirmButtonText: String? = null,
        onConfirm: (() -> Unit)? = null,
        cancelButtonText: String? = null,
        onCancel: (() -> Unit)? = null
    ): DialogActions = DialogActions(
        confirmButtonText = confirmButtonText,
        onConfirm = onConfirm,
        cancelButtonText = cancelButtonText,
        onCancel = onCancel,
        dismissOnClickOutside = dismissOnClickOutside
    )

    @Composable
    fun textStyles(
        titleFontSize: TextUnit = 20.sp,
        messageFontSize: TextUnit = 16.sp,
        buttonFontSize: TextUnit = 16.sp
    ): MessageDialogTextStyles = MessageDialogTextStyles(
        titleFontSize = titleFontSize,
        messageFontSize = messageFontSize,
        buttonFontSize = buttonFontSize
    )
}

/**
 * 一个居中显示的、可定制的消息对话框，带有弹出和消失的动画效果。
 *
 * @param onDismissRequest 当请求关闭对话框时调用的回调（例如，点击外部区域或按返回键）。
 * @param title 对话框的可选标题。
 * @param message
 * 对话框的消息内容，支持单段文本或多行列表，
 * 具体行为由 [MessageContent] 的实现类型决定。
 * @param colors 对话框的颜色配置，包括容器、标题和消息文本的颜色。
 * @param style 对话框的样式配置，包括圆角半径、最大高度和背景昏暗程度。
 * @param actions 对话框的动作配置，包括确认和取消按钮的文本及回调。
 * @param textStyles 对话框文本的样式配置，包括标题、消息和按钮的字体大小。
 */
@Composable
fun MessageDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    message: MessageContent,
    colors: MessageDialogColors = MessageDialogDefaults.colors(),
    style: MessageDialogStyle = MessageDialogDefaults.style(),
    actions: DialogActions = MessageDialogDefaults.actions(),
    textStyles: MessageDialogTextStyles = MessageDialogDefaults.textStyles()
) {
    val animationDuration = 250
    val scope = rememberCoroutineScope()
    val visibleState = remember { MutableTransitionState(false) }
    val showButtons = actions.confirmButtonText != null || actions.cancelButtonText != null

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
            dismissOnClickOutside = actions.dismissOnClickOutside
        )
    ) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(style.dimAmount)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickableWithDebounce { dismissWithAnimation() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(animationSpec = tween(animationDuration)) + scaleIn(
                    initialScale = 0.2f,
                    animationSpec = tween(animationDuration)
                ),
                exit = fadeOut(animationSpec = tween(animationDuration)) + scaleOut(
                    targetScale = 0.2f,
                    animationSpec = tween(animationDuration)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 320.dp) // 限制最大宽度
                        .clickableWithDebounce {} // 阻止点击事件传递到外部
                    ,
                    shape = RoundedCornerShape(style.cornerRadius),
                    color = colors.containerColor
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = if (showButtons) 10.dp else 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                color = colors.titleColor,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = textStyles.titleFontSize),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        CompositionLocalProvider(LocalOverscrollFactory provides null) {
                            Box(
                                modifier = Modifier
                                    .heightIn(max = style.maxHeight)
                            ) {
                                when (message) {
                                    is MessageContent.Text -> {
                                        Text(
                                            text = message.value,
                                            color = colors.messageColor,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = textStyles.messageFontSize),
                                            modifier = Modifier.verticalScroll(rememberScrollState())
                                        )
                                    }

                                    is MessageContent.Lines -> {
                                        Column(
                                            modifier = Modifier.verticalScroll(rememberScrollState())
                                        ) {
                                            message.values.forEach { line ->
                                                Text(
                                                    text = line,
                                                    color = colors.messageColor,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = textStyles.messageFontSize)
                                                )
                                                Spacer(Modifier.height(4.dp)) // 行间距
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showButtons) {
                            Spacer(Modifier.height(24.dp)) // Spacer between message and buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End // Align buttons to the end (right)
                            ) {
                                if (actions.cancelButtonText != null) {
                                    TextButton(
                                        onClick = debouncedOnClick {
                                            dismissWithAnimation()
                                            actions.onCancel?.let { it() }
                                        }
                                    ) {
                                        Text(
                                            text = actions.cancelButtonText,
                                            fontSize = textStyles.buttonFontSize
                                        )
                                    }
                                }
                                if (actions.confirmButtonText != null) {
                                    Spacer(Modifier.width(8.dp)) // Spacer between buttons
                                    TextButton(
                                        onClick = debouncedOnClick {
                                            dismissWithAnimation()
                                            actions.onConfirm?.let { it() }
                                        }
                                    ) {
                                        Text(
                                            text = actions.confirmButtonText,
                                            fontSize = textStyles.buttonFontSize
                                        )
                                    }
                                }
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
            message = MessageContent.Text("This is a short message."),
            actions = MessageDialogDefaults.actions(
                confirmButtonText = "OK",
                onConfirm = {},
                cancelButtonText = "Cancel",
                onCancel = {}
            ),
            textStyles = MessageDialogDefaults.textStyles(
                titleFontSize = 20.sp,
                messageFontSize = 16.sp,
                buttonFontSize = 16.sp
            )
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
            message = MessageContent.Text(longMessage),
            actions = MessageDialogDefaults.actions(
                confirmButtonText = "Confirm",
                onConfirm = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageDialogListTextPreview() {
    val list = List(200) {
        "$it========$it"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        MessageDialog(
            onDismissRequest = {},
            title = "Long Message Title",
            message = MessageContent.Lines(list),
            actions = MessageDialogDefaults.actions(
                cancelButtonText = "Close",
                onCancel = {}
            )
        )
    }
}