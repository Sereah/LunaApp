package com.lunacattus.connection.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Wifi
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.connection.ui.base.LocalNavigator
import com.lunacattus.connection.ui.base.MainGraph
import com.lunacattus.connection.ui.base.NavigationState
import com.lunacattus.connection.ui.base.Navigator
import com.lunacattus.connection.ui.base.rememberNavigationState
import com.lunacattus.connection.ui.base.toEntries
import com.lunacattus.connection.ui.section.bluetooth.BluetoothHomeRoute
import com.lunacattus.connection.ui.section.bluetooth.bluetoothSection
import com.lunacattus.connection.ui.section.root.rootSection
import com.lunacattus.connection.ui.section.wifi.WifiRoute
import com.lunacattus.connection.ui.section.wifi.wifiSection
import com.lunacattus.connection.ui.theme.AppTheme
import com.lunacattus.connection.ui.theme.slideInFromLeft
import com.lunacattus.connection.ui.theme.slideInFromRight
import com.lunacattus.connection.ui.theme.slideOutFromLeft
import com.lunacattus.connection.ui.theme.slideOutFromRight
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.dialog.OverlayToast
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            AppTheme {
                val rootBackStack = rememberNavBackStack(MainGraph)
                val innerNavigationState = rememberNavigationState(
                    startRoute = BluetoothHomeRoute,
                    topLevelRoutesKey = topLevelRoutes.keys
                )
                val navigator = remember(rootBackStack, innerNavigationState) {
                    Navigator(
                        innerState = innerNavigationState,
                        rootBackStack = rootBackStack
                    )
                }

                CompositionLocalProvider(LocalNavigator provides navigator) {
                    NavDisplay(
                        entries = rememberDecoratedNavEntries(
                            backStack = rootBackStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            entryProvider = entryProvider {
                                entry<MainGraph> {
                                    Main(innerNavigationState, navigator)
                                }
                                rootSection()
                            }
                        ),
                        onBack = { navigator.goBack() },
                    )
                }
                ToastView()
            }
        }
    }
}

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
                    bluetoothSection()
                    wifiSection()
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

private data class NavBarItem(
    val icon: ImageVector,
    val title: String
)

private val topLevelRoutes = mapOf(
    BluetoothHomeRoute to NavBarItem(icon = Icons.Rounded.Bluetooth, title = "Bluetooth"),
    WifiRoute to NavBarItem(icon = Icons.Rounded.Wifi, title = "Wi-Fi")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
private fun TopBar(navigator: Navigator, navState: NavigationState, hazeState: HazeState) {
    CenterAlignedTopAppBar(
        title = { Text(navState.currentRoute.name) },
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

@Composable
fun ToastView() {
    var toastEvent by remember { mutableStateOf<Pair<String, Long>?>(null) }

    LaunchedEffect(Unit) {
        ActivityToastEvent.events.collect {
            toastEvent = when (it) {
                is ToastEvent.ShowToast -> {
                    it.message to it.id
                }
            }
        }
    }

    OverlayToast(
        modifier = Modifier.fillMaxSize(),
        toastEvent
    )
}