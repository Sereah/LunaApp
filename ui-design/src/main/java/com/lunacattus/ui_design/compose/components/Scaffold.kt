package com.lunacattus.ui_design.compose.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.distinctUntilChanged

@Preview
@Composable
fun Sample() {
    val itemList = listOf(
        BottomItem(
            title = "home",
            icon = Icons.Rounded.Home,
            selectedColor = Color.Black,
            unSelectColor = Color.LightGray,
            route = "home"
        ),
        BottomItem(
            title = "person",
            icon = Icons.Rounded.Person,
            selectedColor = Color.Black,
            unSelectColor = Color.LightGray,
            route = "home"
        ),
        BottomItem(
            title = "setting",
            icon = Icons.Rounded.Settings,
            selectedColor = Color.Black,
            unSelectColor = Color.LightGray,
            route = "home"
        )
    )

    var selectItem by remember { mutableStateOf(itemList.first()) }

    HazeAppBarBottomScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        appBarMiddleComposable = {
            Text(text = "Wechat", fontSize = 25.sp, modifier = Modifier.padding(bottom = 10.dp))
        },
        appBarLeftComposable = {
            Image(
                imageVector = Icons.Rounded.StarBorder,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 10.dp, bottom = 10.dp)
            )
        },
        appBarRightComposable = {
            Image(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 10.dp, bottom = 10.dp)
            )
        },
        appBarBackgroundColor = Color.White,
        appBarDividerColor = Color(0X90B8B8BB),
        appBarLinearGradient = true,
        bottomItems = itemList,
        bottomIconSize = 30.dp,
        bottomTitleSize = 18.sp,
        bottomSelectItemIndex = itemList.indexOf(selectItem),
        bottomOnSelectItem = {
            selectItem = it
        },
    ) {
        val list = List(20) {
            "https://bing.biturl.top/?resolution=1920&format=image&index=$it"
        }
        LazyColumn(
            contentPadding = PaddingValues(top = 100.dp, start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.overScrollVertical()
        ) {
            items(list) { item ->
                AsyncImage(
                    model = item,
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

/**
 * 一个带有毛玻璃（haze）效果的脚手架 Composable，提供了顶部应用栏和底部导航栏。
 *
 * 该组件设计用于在半透明、模糊的应用栏和底部栏后面显示内容。
 * 模糊效果由 `haze` 库提供。为了使效果可见，内容区域应该是可滚动的。
 *
 * @param modifier 应用于脚手架的修饰符。
 * @param appBarLeftComposable 显示在应用栏左侧的 Composable。
 * @param appBarMiddleComposable 显示在应用栏中间的 Composable。
 * @param appBarRightComposable 显示在应用栏右侧的 Composable。
 * @param appBarBackgroundColor 应用栏的背景颜色。此颜色也用作毛玻璃效果的着色。
 * @param appBarDividerColor 应用栏底部分割线的颜色。
 * @param appBarHeight 应用栏的高度。
 * @param appBarLinearGradient 是否使用线性渐变来实现毛玻璃效果，如果为true，则[appBarBackgroundColor]和[appBarDividerColor]将被忽略。
 * @param bottomBackgroundColor 底部导航栏的背景颜色。默认为 [appBarBackgroundColor]。
 * @param bottomDividerColor 底部导航栏顶部分割线的颜色。默认为 [appBarDividerColor]。
 * @param bottomHeight 底部导航栏的高度。默认为 [appBarHeight]。
 * @param bottomIconSize 底部导航栏中图标的大小。
 * @param bottomTitleSize 底部导航栏中标题的字体大小。
 * @param bottomItems 要在底部导航栏中显示的 [BottomItem] 列表。
 * @param bottomSelectItemIndex 底部导航栏中当前选定项的索引。
 * @param bottomOnSelectItem 当选择底部导航栏中的项目时调用的回调。
 * @param onListOffsetVerticalChange 提供内容列表的垂直滚动偏移量的回调。
 * @param onListOffSetIndexChange 提供内容列表中第一个可见项的索引的回调。
 * @param content 要在脚手架中显示的主要内容，通常是可滚动的列表。
 */
@Composable
fun HazeAppBarBottomScaffold(
    modifier: Modifier = Modifier,
    appBarLeftComposable: @Composable (() -> Unit)? = null,
    appBarMiddleComposable: @Composable (() -> Unit)? = null,
    appBarRightComposable: @Composable (() -> Unit)? = null,
    appBarBackgroundColor: Color = Color.Unspecified,
    appBarDividerColor: Color = Color.LightGray,
    appBarHeight: Dp = 90.dp,
    appBarLinearGradient: Boolean = false,
    bottomBackgroundColor: Color = appBarBackgroundColor,
    bottomDividerColor: Color = appBarDividerColor,
    bottomHeight: Dp = appBarHeight,
    bottomIconSize: Dp = 25.dp,
    bottomTitleSize: TextUnit = 15.sp,
    bottomItems: List<BottomItem>,
    bottomSelectItemIndex: Int,
    bottomOnSelectItem: (BottomItem) -> Unit,
    onListOffsetVerticalChange: (Int) -> Unit = {},
    onListOffSetIndexChange: (Int) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val hazeState = rememberHazeState()
    Box(modifier = modifier.fillMaxSize()) {
        val listState = rememberLazyListState()

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemScrollOffset to listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { (offset, index) ->
                    onListOffsetVerticalChange(offset)
                    onListOffSetIndexChange(index)
                }
        }

        AppBar(
            modifier = Modifier
                .height(appBarHeight)
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    if (appBarLinearGradient) {
                        progressive =
                            HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
                    }
                    style = HazeStyle(
                        backgroundColor = appBarBackgroundColor,
                        tint = HazeTint(color = appBarBackgroundColor.copy(alpha = 0.8f)),
                        blurRadius = 24.dp
                    )
                    blurEnabled = true
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, _ -> }
                }
                .zIndex(99f),
            leftComposable = appBarLeftComposable,
            middleComposable = appBarMiddleComposable,
            rightComposable = appBarRightComposable,
            backgroundColor = if (appBarLinearGradient) Color.Unspecified else appBarBackgroundColor,
            dividerColor = if (appBarLinearGradient) Color.Unspecified else appBarDividerColor
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .zIndex(1f)
        ) {
            content()
        }

        AppBottom(
            modifier = Modifier
                .height(bottomHeight)
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = bottomBackgroundColor,
                        tint = HazeTint(color = bottomBackgroundColor.copy(alpha = 0.85f)),
                        blurRadius = 24.dp
                    )
                    blurEnabled = true
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, _ -> }
                }
                .zIndex(99f)
                .align(Alignment.BottomCenter),
            backgroundColor = bottomBackgroundColor,
            dividerColor = bottomDividerColor,
            iconSize = bottomIconSize,
            titleSize = bottomTitleSize,
            items = bottomItems,
            selectItemIndex = bottomSelectItemIndex,
            onSelectItem = bottomOnSelectItem
        )
    }

}

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    leftComposable: @Composable (() -> Unit)? = null,
    middleComposable: @Composable (() -> Unit)? = null,
    rightComposable: @Composable (() -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    dividerColor: Color = Color.Unspecified,
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.BottomStart
            ) {
                leftComposable?.invoke()
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                middleComposable?.invoke()
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                rightComposable?.invoke()
            }
        }

        HorizontalDivider(
            color = dividerColor,
            thickness = 0.5.dp
        )
    }
}

@Composable
fun AppBottom(
    modifier: Modifier,
    backgroundColor: Color,
    dividerColor: Color,
    iconSize: Dp,
    titleSize: TextUnit,
    items: List<BottomItem>,
    selectItemIndex: Int,
    onSelectItem: (BottomItem) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HorizontalDivider(
            color = dividerColor,
            thickness = 0.5.dp
        )

        Row(
            modifier = modifier.fillMaxWidth(),
        ) {

            items.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onSelectItem(item)
                            }
                        }
                ) {
                    val isSelected = item == items[selectItemIndex]
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.9f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    )
                    CompositionLocalProvider(
                        LocalContentColor provides if (isSelected) {
                            item.selectedColor
                        } else {
                            item.unSelectColor
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(iconSize)
                                .scale(scale)
                        )
                        Text(
                            text = item.title,
                            fontSize = titleSize,
                            modifier = Modifier.scale(scale)
                        )
                    }
                }
            }
        }
    }
}

data class BottomItem(
    val title: String,
    val icon: ImageVector,
    val selectedColor: Color,
    val unSelectColor: Color,
    val route: String,
)