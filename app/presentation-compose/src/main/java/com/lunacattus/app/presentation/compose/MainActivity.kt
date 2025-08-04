package com.lunacattus.app.presentation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.FormatListNumberedRtl
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lunacattus.app.presentation.compose.common.components.BottomItem
import com.lunacattus.app.presentation.compose.common.components.HazeAppBarBottomScaffold
import com.lunacattus.app.presentation.compose.routes.main.browser.BrowserGraph
import com.lunacattus.app.presentation.compose.routes.main.browser.browserRouter
import com.lunacattus.app.presentation.compose.routes.main.music.MusicGraph
import com.lunacattus.app.presentation.compose.routes.main.music.musicRouter
import com.lunacattus.app.presentation.compose.routes.main.playList.PlayListGraph
import com.lunacattus.app.presentation.compose.routes.main.playList.playListRouter
import com.lunacattus.app.presentation.compose.routes.main.settings.SettingsGraph
import com.lunacattus.app.presentation.compose.routes.main.settings.settingsRouter
import com.lunacattus.app.presentation.compose.routes.main.video.VideoGraph
import com.lunacattus.app.presentation.compose.routes.main.video.videoRouter
import com.lunacattus.app.presentation.compose.routes.player.playerRouter
import com.lunacattus.app.presentation.compose.theme.AppTheme
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
                val rootNavController = rememberNavController()
                NavHost(
                    navController = rootNavController,
                    startDestination = "mainNav"
                ) {
                    composable(route = "mainNav") {
                        Main(rootNavController)
                    }
                    playerRouter(rootNavController)
                }
            }
        }
    }
}

@Composable
fun Main(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    val currentBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDest = currentBackStackEntry?.destination
    val bottomItems = bottomItems()
    val selectedItem = bottomItems.find { item ->
        currentDest?.hierarchy?.any { it.route == item.route } == true
    } ?: bottomItems[0]

    Box(modifier = Modifier.fillMaxSize()) {
        HazeAppBarBottomScaffold(
            appBarMiddleComposable = {
                Text(
                    text = selectedItem.title,
                    fontSize = 22.sp,
                    color = AppTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            },
            appBarBackgroundColor = Color(0XFFEDEDED),
            appBarDividerColor = Color.LightGray,
            bottomIconSize = 25.dp,
            bottomTitleSize = 15.sp,
            bottomItems = bottomItems(),
            bottomSelectItemIndex = bottomItems.indexOf(selectedItem),
            bottomOnSelectItem = {
                mainNavController.navigate(it.route) {
                    popUpTo(mainNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        ) {
            NavHost(
                navController = mainNavController,
                startDestination = VideoGraph.route,
            ) {
                videoRouter(mainNavController, rootNavController)
                musicRouter(mainNavController)
                playListRouter(mainNavController)
                browserRouter(mainNavController)
                settingsRouter(mainNavController)
            }
        }
    }
}

@Composable
fun bottomItems(): List<BottomItem> {
    return listOf(
        BottomItem(
            title = "Video",
            icon = Icons.Rounded.SlowMotionVideo,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = VideoGraph.route
        ),
        BottomItem(
            title = "Music",
            icon = Icons.Rounded.MusicNote,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = MusicGraph.route
        ),
        BottomItem(
            title = "PlayList",
            icon = ImageVector.vectorResource(R.drawable.ic_play_list),
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = PlayListGraph.route
        ),
        BottomItem(
            title = "Browser",
            icon = Icons.Rounded.FileCopy,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = BrowserGraph.route
        ),
        BottomItem(
            title = "Settings",
            icon = Icons.Rounded.Settings,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = SettingsGraph.route
        )
    )
}