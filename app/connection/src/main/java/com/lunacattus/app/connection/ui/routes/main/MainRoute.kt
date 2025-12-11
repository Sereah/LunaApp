package com.lunacattus.app.connection.ui.routes.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lunacattus.app.connection.ui.routes.main.bluetooth.BluetoothGraph
import com.lunacattus.app.connection.ui.routes.main.bluetooth.bluetoothRouter
import com.lunacattus.app.connection.ui.routes.main.wifi.WifiGraph
import com.lunacattus.app.connection.ui.routes.main.wifi.wifiRouter
import com.lunacattus.app.connection.ui.theme.AppTheme
import com.lunacattus.ui_design.compose.BottomItem
import com.lunacattus.ui_design.compose.HazeAppBarBottomScaffold

@Composable
fun MainRoute(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    val backStack by mainNavController.currentBackStackEntryAsState()
    val bottomItems = bottomItems()
    val selectedItem = bottomItems.find { bottomItem ->
        backStack?.destination?.hierarchy?.any { it.route == bottomItem.route } == true
    } ?: bottomItems[0]

    HazeAppBarBottomScaffold(
        modifier = Modifier.fillMaxSize(),
        appBarMiddleComposable = {
            Text(
                text = selectedItem.title,
                fontSize = 22.sp,
                color = AppTheme.colors.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        },
        appBarBackgroundColor = AppTheme.colors.background,
        bottomBackgroundColor = AppTheme.colors.background,
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
            startDestination = BluetoothGraph.route
        ) {
            bluetoothRouter(mainNavController, rootNavController)
            wifiRouter(mainNavController, rootNavController)
        }
    }
}


@Composable
fun bottomItems(): List<BottomItem> {
    return listOf(
        BottomItem(
            title = "Bluetooth",
            icon = Icons.Rounded.Bluetooth,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = BluetoothGraph.route
        ),
        BottomItem(
            title = "Wifi",
            icon = Icons.Rounded.Wifi,
            selectedColor = AppTheme.colors.primary,
            unSelectColor = AppTheme.colors.inversePrimary,
            route = WifiGraph.route
        )
    )
}