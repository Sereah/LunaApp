package com.lunacattus.conflux.ui

import android.annotation.SuppressLint
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.NavigationState
import com.lunacattus.conflux.ui.base.Navigator
import com.lunacattus.conflux.ui.base.toEntries
import com.lunacattus.conflux.ui.sections.connection.connectSection
import com.lunacattus.conflux.ui.sections.home.homeSection
import com.lunacattus.conflux.ui.sections.media.mediaSection
import com.lunacattus.conflux.ui.sections.setting.settingSection
import com.lunacattus.conflux.ui.theme.slideInFromBottom
import com.lunacattus.conflux.ui.theme.slideInFromLeft
import com.lunacattus.conflux.ui.theme.slideInFromRight
import com.lunacattus.conflux.ui.theme.slideInFromTop
import com.lunacattus.conflux.ui.theme.slideOutFromBottom
import com.lunacattus.conflux.ui.theme.slideOutFromLeft
import com.lunacattus.conflux.ui.theme.slideOutFromRight
import com.lunacattus.conflux.ui.theme.slideOutFromTop
import com.lunacattus.ui_design.compose.clickableWithDebounce
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main(navState: NavigationState, navigator: Navigator) {
    val defaultTitle = stringResource(R.string.app_name)
    var topBarTitle by remember {
        mutableStateOf(
            TopBarTitle(
                title = defaultTitle,
                showBackIcon = false
            )
        )
    }
    val hazeState = rememberHazeState()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    val isBottomBar = layoutType == NavigationSuiteType.NavigationBar

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            topLevelRoutes.forEach { (key, value) ->
                item(
                    selected = navState.topLevelRoute == key,
                    onClick = { navigator.navigate(key) },
                    icon = { Icon(imageVector = value.icon, contentDescription = null) },
                    label = { Text(text = value.title) },
                    modifier = Modifier.padding(
                        vertical = if (isBottomBar) {
                            0.dp
                        } else {
                            16.dp
                        }
                    )
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopBar(navigator, hazeState, topBarTitle)
            },
        ) { padding ->
            CompositionLocalProvider(
                LocalInnerPadding provides padding,
                LocalSetTopBarTitle provides { title ->
                    topBarTitle = title
                }
            ) {
                NavDisplay(
                    entries = navState.toEntries(
                        entryProvider {
                            homeSection()
                            connectSection()
                            mediaSection()
                            settingSection()
                        }
                    ),
                    onBack = { navigator.goBack() },
                    transitionSpec = {

                        val topLevels = topLevelRoutes.keys.map { it.toString() }
                        val lastRoute = navState.lastRoute
                        val currentRoute = navState.currentRoute
                        val transform: ContentTransform = when {
                            // 顶级 ↔ 顶级
                            isTopLevelToTopLevel(lastRoute, currentRoute, topLevels) -> {
                                topLevelTransform(isBottomBar, lastRoute, currentRoute, topLevels)
                            }

                            // 子路由：同一顶级栈
                            isSameTopLevelStack(lastRoute, currentRoute, navState.backStacks) -> {
                                slideInFromRight togetherWith slideOutFromLeft
                            }

                            // 子路由：跨顶级栈
                            else -> {
                                val lastTop = lastRoute?.let {
                                    findTopLevelOfRoute(it, navState.backStacks)
                                }
                                val currentTop = findTopLevelOfRoute(
                                    currentRoute,
                                    navState.backStacks
                                )
                                topLevelTransform(isBottomBar, lastTop, currentTop, topLevels)
                            }
                        }

                        transform
                    },
                    popTransitionSpec = {
                        val lastRoute = navState.lastRoute
                        val currentRoute = navState.currentRoute

                        val useHorizontal =
                            isBottomBar || isSameTopLevelStack(lastRoute, currentRoute, navState.backStacks)

                        if (useHorizontal) {
                            slideInFromLeft togetherWith slideOutFromRight
                        } else {
                            slideInFromTop togetherWith slideOutFromBottom
                        }

                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
private fun TopBar(
    navigator: Navigator,
    hazeState: HazeState,
    topBarTitle: TopBarTitle
) {
    TopAppBar(
        title = { Text(topBarTitle.title) },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        navigationIcon = {
            if (topBarTitle.showBackIcon) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "",
                    modifier = Modifier.clickableWithDebounce {
                        navigator.goBack()
                    }
                )
            }
        },
        modifier = Modifier
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.regular(MaterialTheme.colorScheme.surface)
            )
    )
}

val LocalInnerPadding = staticCompositionLocalOf<PaddingValues> {
    error("PaddingValues not provided")
}

val LocalSetTopBarTitle = compositionLocalOf<(TopBarTitle) -> Unit> {
    error("LocalSetTopBarTitle not provided")
}

data class TopBarTitle(
    val title: String,
    val showBackIcon: Boolean = false,
)

private fun topLevelTransform(
    isBottomBar: Boolean,
    from: BaseRoute?,
    to: BaseRoute?,
    topLevels: List<String>
): ContentTransform {
    val forward = isMoveToRight(from.toString(), to.toString(), topLevels)
    return when {
        isBottomBar && forward ->
            slideInFromRight togetherWith slideOutFromLeft

        isBottomBar && !forward ->
            slideInFromLeft togetherWith slideOutFromRight

        !isBottomBar && forward ->
            slideInFromBottom togetherWith slideOutFromTop

        else ->
            slideInFromTop togetherWith slideOutFromBottom
    }
}

private fun isSameTopLevelStack(
    lastRoute: BaseRoute?,
    currentRoute: BaseRoute,
    backStacks: Map<BaseRoute, NavBackStack<NavKey>>
): Boolean {
    if (lastRoute == null) return false

    val lastTop = findTopLevelOfRoute(lastRoute, backStacks)
    val currentTop = findTopLevelOfRoute(currentRoute, backStacks)

    return lastTop != null && lastTop == currentTop
}

private fun isTopLevelToTopLevel(
    lastRoute: BaseRoute?,
    currentRoute: BaseRoute,
    topLevels: List<String>
): Boolean {
    return lastRoute != null &&
            lastRoute.toString() in topLevels &&
            currentRoute.toString() in topLevels
}

private fun isMoveToRight(
    fromRoute: String,
    toRoute: String,
    topLevelRoutes: List<String>
): Boolean {
    val fromIndex = topLevelRoutes.indexOf(fromRoute)
    val toIndex = topLevelRoutes.indexOf(toRoute)
    return toIndex > fromIndex
}

private fun findTopLevelOfRoute(
    route: BaseRoute,
    backStacks: Map<BaseRoute, NavBackStack<NavKey>>
): BaseRoute? {
    return backStacks.entries
        .firstOrNull { (_, stack) -> stack.contains(route) }
        ?.key
}