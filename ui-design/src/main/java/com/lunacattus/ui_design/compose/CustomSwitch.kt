package com.lunacattus.ui_design.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 定义 Switch 组件在不同状态下的颜色。
 *
 * @property checkedTrackColor 开启状态下轨道的颜色。
 * @property uncheckedTrackColor 关闭状态下轨道的颜色。
 * @property thumbColor 滑块的颜色。
 */
@Immutable
data class SwitchColors(
    val checkedTrackColor: Color,
    val uncheckedTrackColor: Color,
    val thumbColor: Color,
)

/**
 * 定义 Switch 组件的尺寸。
 *
 * @property width 组件的总宽度。
 * @property height 组件的总高度。
 * @property thumbSize 滑块的大小。
 */
@Immutable
data class SwitchSizes(
    val width: Dp,
    val height: Dp,
    val thumbSize: Dp,
)

/**
 * 定义点击时的涟漪效果。
 *
 * @property bounded 涟漪效果是否应限制在组件边界内。
 * @property radius 涟漪效果的半径。
 * @property color 涟漪效果的颜色。
 */
@Immutable
data class SwitchRipple(
    val bounded: Boolean,
    val radius: Dp,
    val color: Color
)

/**
 * 包含 `CustomSwitch` 组件的默认值。
 */
object SwitchDefaults {
    /**
     * 创建一个 `SwitchColors` 实例，其中包含默认的颜色配置。
     */
    @Composable
    fun colors(
        checkedTrackColor: Color = Color(0xFF35898F),
        uncheckedTrackColor: Color = Color(0xFFCCCCCC),
        thumbColor: Color = Color.White,
    ): SwitchColors = SwitchColors(
        checkedTrackColor = checkedTrackColor,
        uncheckedTrackColor = uncheckedTrackColor,
        thumbColor = thumbColor
    )

    /**
     * 创建一个 `SwitchSizes` 实例，其中包含默认的尺寸配置。
     */
    fun sizes(
        width: Dp = 52.dp,
        height: Dp = 32.dp,
        thumbSize: Dp = 28.dp,
    ): SwitchSizes = SwitchSizes(width, height, thumbSize)

    /**
     * 创建一个 `SwitchRipple` 实例，其中包含默认的涟漪效果配置。
     */
    fun ripple(
        bounded: Boolean = false,
        radius: Dp = Dp.Unspecified,
        color: Color = Color.LightGray
    ): SwitchRipple = SwitchRipple(bounded, radius, color)
}

/**
 * 一个可自定义的 `Switch` (开关) 组件。
 *
 * @param checked 开关是否处于开启状态。
 * @param onCheckedChanged 当开关状态改变时的回调。
 * @param modifier 应用于此组件的 `Modifier`。
 * @param enabled 此组件是否启用。
 * @param sizes 组件的尺寸配置。
 * @param colors 组件的颜色配置。
 * @param ripple 点击时的涟漪效果配置，如果为 `null` 则禁用涟漪。
 */
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sizes: SwitchSizes = SwitchDefaults.sizes(),
    colors: SwitchColors = SwitchDefaults.colors(),
    ripple: SwitchRipple? = null,
) {
    val disabledCheckedTrackColor = remember(colors.checkedTrackColor) { colors.checkedTrackColor.copy(alpha = 0.5f) }
    val disabledUncheckedTrackColor = remember(colors.uncheckedTrackColor) { colors.uncheckedTrackColor.copy(alpha = 0.5f) }
    val disabledThumbColor = remember(colors.thumbColor) { colors.thumbColor.copy(alpha = 0.5f) }

    val trackColor by animateColorAsState(
        targetValue = if (enabled) {
            if (checked) colors.checkedTrackColor else colors.uncheckedTrackColor
        } else {
            if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
        },
        label = "Track Color"
    )
    val animatedThumbColor by animateColorAsState(
        targetValue = if (enabled) colors.thumbColor else disabledThumbColor,
        label = "Thumb Color"
    )
    val thumbPadding = (sizes.height - sizes.thumbSize) / 2
    val thumbPosition by animateDpAsState(
        targetValue = if (checked) sizes.width - sizes.thumbSize - thumbPadding else thumbPadding,
        label = "Thumb Position"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(sizes.width)
            .height(sizes.height)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple?.let {
                    ripple(
                        bounded = it.bounded,
                        radius = it.radius,
                        color = it.color
                    )
                },
                enabled = enabled
            ) { onCheckedChanged(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(sizes.thumbSize)
                .offset(x = thumbPosition, y = thumbPadding)
                .clip(CircleShape)
                .background(animatedThumbColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomSwitchCheckedPreview() {
    var isChecked by remember { mutableStateOf(true) }
    CustomSwitch(
        checked = isChecked,
        onCheckedChanged = { isChecked = it },
        ripple = SwitchDefaults.ripple()
    )
}

@Preview(showBackground = true)
@Composable
fun CustomSwitchDisabledPreview() {
    var isChecked by remember { mutableStateOf(true) }
    CustomSwitch(
        checked = isChecked,
        onCheckedChanged = { isChecked = it },
        enabled = false,
        ripple = SwitchDefaults.ripple()
    )
}
