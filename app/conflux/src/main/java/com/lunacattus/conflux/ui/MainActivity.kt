package com.lunacattus.conflux.ui

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.lunacattus.conflux.permission.PermissionHost
import com.lunacattus.conflux.ui.base.LocalNavigator
import com.lunacattus.conflux.ui.base.MainGraph
import com.lunacattus.conflux.ui.base.Navigator
import com.lunacattus.conflux.ui.base.rememberNavigationState
import com.lunacattus.conflux.ui.sections.connection.ConnectionRoute
import com.lunacattus.conflux.ui.sections.home.HomeRoute
import com.lunacattus.conflux.ui.sections.media.MediaRoute
import com.lunacattus.conflux.ui.sections.setting.SettingRoute
import com.lunacattus.conflux.ui.theme.LunaAppTheme
import com.lunacattus.ui_design.compose.dialog.OverlayToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            viewModel.changeNightMode(isSystemInDarkTheme())
            PermissionHost(
                permissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                ),
                onPermissionDenied = {
                    finish()
                }
            ) {
                LunaAppTheme(
                    dynamicColor = viewModel.dynamicColor,
                    darkTheme = viewModel.nightMode
                ) {
                    SystemBarAppearance(viewModel.nightMode)
                    val rootBackStack = rememberNavBackStack(MainGraph)
                    val innerNavigationState = rememberNavigationState(
                        startRoute = HomeRoute,
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
                                }
                            ),
                            onBack = { navigator.goBack() },
                        )
                    }
                }
            }
            ToastView()
        }
    }
}

@Composable
fun SystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
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

val topLevelRoutes = mapOf(
    HomeRoute to NavBarItem(icon = Icons.Rounded.Home, title = "首页"),
    ConnectionRoute to NavBarItem(icon = Icons.Rounded.Link, title = "连接"),
    MediaRoute to NavBarItem(icon = Icons.Rounded.MusicVideo, title = "多媒体"),
    SettingRoute to NavBarItem(icon = Icons.Rounded.Settings, title = "设置"),
)

data class NavBarItem(
    val icon: ImageVector,
    val title: String
)
