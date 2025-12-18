package com.lunacattus.nav3test.ui

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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.nav3test.ui.base.LocalNavigator
import com.lunacattus.nav3test.ui.base.MainGraph
import com.lunacattus.nav3test.ui.base.NavigationState
import com.lunacattus.nav3test.ui.base.Navigator
import com.lunacattus.nav3test.ui.base.rememberNavigationState
import com.lunacattus.nav3test.ui.base.toEntries
import com.lunacattus.nav3test.ui.section.bluetooth.BluetoothRoute
import com.lunacattus.nav3test.ui.section.bluetooth.bluetoothSection
import com.lunacattus.nav3test.ui.section.root.rootSection
import com.lunacattus.nav3test.ui.section.wifi.WifiRoute
import com.lunacattus.nav3test.ui.section.wifi.wifiSection
import com.lunacattus.nav3test.ui.theme.AppTheme
import com.lunacattus.nav3test.ui.theme.slideInFromLeft
import com.lunacattus.nav3test.ui.theme.slideInFromRight
import com.lunacattus.nav3test.ui.theme.slideOutFromLeft
import com.lunacattus.nav3test.ui.theme.slideOutFromRight
import dagger.hilt.android.AndroidEntryPoint

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
                    startRoute = BluetoothRoute,
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
            }
        }
    }
}

@Composable
fun Main(navState: NavigationState, navigator: Navigator) {
    Scaffold(
        bottomBar = {
            bottomBar(
                isSelected = { it == navState.topLevelRoute },
                click = { navigator.navigate(it) }
            )
        }
    ) {
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
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private data class NavBarItem(
    val icon: ImageVector,
    val title: String
)

private val topLevelRoutes = mapOf(
    BluetoothRoute to NavBarItem(icon = Icons.Rounded.Bluetooth, title = "Bluetooth"),
    WifiRoute to NavBarItem(icon = Icons.Rounded.Wifi, title = "Wi-Fi")
)

@Composable
private fun bottomBar(
    isSelected: (NavKey) -> Boolean,
    click: (NavKey) -> Unit
) {
    Column {
        HorizontalDivider(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.outlineVariant)
                .height(0.5.dp)
        )
        NavigationBar(windowInsets = WindowInsets()) {
            topLevelRoutes.forEach { (key, value) ->
                NavigationBarItem(
                    selected = isSelected(key),
                    onClick = { click.invoke(key) },
                    icon = { Icon(imageVector = value.icon, contentDescription = null) },
                    label = { Text(text = value.title) }
                )
            }
        }
    }
}