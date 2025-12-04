package com.lunacattus.ui_design.compose.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.lunacattus.ui_design.compose.clickableWithDebounce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 一个可定制的确认对话框，从底部弹出，并带有动画效果。
 *
 * @param confirmText 确认按钮的文本。
 * @param cancelText 取消按钮的文本。
 * @param onConFirm 点击确认按钮时的回调。
 * @param onDismiss 对话框因任何原因（确认、取消、点击外部、按返回键）关闭时的回调。
 * @param confirmTextColor 确认按钮文本的颜色。
 * @param cancelTextColor 取消按钮文本的颜色。
 * @param dialogBackgroundColor 对话框的背景颜色。
 * @param dividerColor 分割线的颜色。
 * @param rippleColor 点击波纹效果的颜色。
 * @param dismissOnClickOutside 是否允许点击对话框外部区域来关闭对话框。
 * @param dimAmount 对话框背景的昏暗程度，范围从 0.0f（完全透明）到 1.0f（完全不透明）。
 */
@Composable
fun BottomConfirmDialog(
    confirmText: String,
    cancelText: String = "cancel",
    onConFirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmTextColor: Color = Color.Red.copy(alpha = 0.5f),
    cancelTextColor: Color = Color(0xC87C7C7D),
    dialogBackgroundColor: Color = Color.White,
    dividerColor: Color = Color(0xF7B8B8BB),
    rippleColor: Color = Color.Black.copy(alpha = 0.8f),
    dismissOnClickOutside: Boolean = true,
    dimAmount: Float = 0.2f
) {

    val scope = rememberCoroutineScope()
    val animationDuration = 300
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    // 封装带动画的关闭逻辑
    val dismissWithAnimation = {
        scope.launch {
            visibleState.targetState = false
            delay(animationDuration.toLong())
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (dismissOnClickOutside) {
                dismissWithAnimation()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(dimAmount)
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 外部点击事件由 Dialog 的 onDismissRequest 处理
                .clickableWithDebounce {
                    if (dismissOnClickOutside) {
                        dismissWithAnimation()
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInVertically(
                    initialOffsetY = { it + 10 },
                    animationSpec = tween(durationMillis = animationDuration)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it + 10 },
                    animationSpec = tween(durationMillis = animationDuration)
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .fillMaxWidth()
                        .background(dialogBackgroundColor, RoundedCornerShape(10.dp))
                        // 阻止点击事件传递到外部 Box
                        .clickableWithDebounce {}
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp))
                            .clickableWithDebounce(
                                indication = ripple(
                                    bounded = true,
                                    color = rippleColor
                                )
                            ) {
                                scope.launch {
                                    visibleState.targetState = false
                                    delay(animationDuration.toLong())
                                    onConFirm()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 18.sp,
                            color = confirmTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                    HorizontalDivider(
                        color = dividerColor,
                        thickness = 0.5.dp,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp))
                            .clickableWithDebounce(
                                indication = ripple(
                                    bounded = true,
                                    color = rippleColor
                                )
                            ) {
                                dismissWithAnimation()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            fontSize = 18.sp,
                            color = cancelTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun ConfirmDialogPre() {
    BottomConfirmDialog(
        confirmText = "confirm",
        onDismiss = {},
        onConFirm = {})
}
