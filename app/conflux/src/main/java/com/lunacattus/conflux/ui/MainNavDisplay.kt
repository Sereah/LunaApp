package com.lunacattus.conflux.ui

import android.annotation.SuppressLint
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.conflux.ui.base.NavigationState
import com.lunacattus.conflux.ui.base.Navigator
import com.lunacattus.conflux.ui.base.toEntries
import com.lunacattus.conflux.ui.sections.connection.connectSection
import com.lunacattus.conflux.ui.sections.home.homeSection
import com.lunacattus.conflux.ui.sections.media.mediaSection
import com.lunacattus.conflux.ui.theme.slideInFromLeft
import com.lunacattus.conflux.ui.theme.slideInFromRight
import com.lunacattus.conflux.ui.theme.slideOutFromLeft
import com.lunacattus.conflux.ui.theme.slideOutFromRight
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
    val hazeState = rememberHazeState()
    Scaffold(
        topBar = {
            TopBar(navigator, navState, hazeState)
        },
        bottomBar = {
            BottomBar(navigator, navState, hazeState)
        }
    ) { _ ->
        NavDisplay(
            entries = navState.toEntries(
                entryProvider {
                    homeSection()
                    connectSection()
                    mediaSection()
                }
            ),
            onBack = { navigator.goBack() },
            transitionSpec = {
                slideInFromRight togetherWith slideOutFromLeft
            },
            popTransitionSpec = {
                slideInFromLeft togetherWith slideOutFromRight
            },
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
private fun TopBar(navigator: Navigator, navState: NavigationState, hazeState: HazeState) {
    CenterAlignedTopAppBar(
        title = { Text(topLevelRoutes[navState.currentRoute]?.title ?: "") },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        navigationIcon = {
            if (navState.currentRoute != navState.topLevelRoute) {
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun BottomBar(
    navigator: Navigator,
    navState: NavigationState,
    hazeState: HazeState
) {
    Column {
        HorizontalDivider(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.outlineVariant)
                .height(0.5.dp)
        )
        NavigationBar(
            windowInsets = WindowInsets(),
            modifier = Modifier.hazeEffect(state = hazeState, style = HazeMaterials.ultraThick())
        ) {
            topLevelRoutes.forEach { (key, value) ->
                NavigationBarItem(
                    selected = navState.topLevelRoute == key,
                    onClick = { navigator.navigate(key) },
                    icon = { Icon(imageVector = value.icon, contentDescription = null) },
                    label = { Text(text = value.title) }
                )
            }
        }
    }
}