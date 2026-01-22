package com.lunacattus.conflux.ui.sections.setting.homepage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalActivityViewModel
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun SettingRoute(viewModel: SettingViewModel) {
    val activityViewModel = LocalActivityViewModel.current
    SettingScreen(
        enableDynamicColor = activityViewModel.dynamicColor,
        changeDynamicColor = { activityViewModel.changeDynamicColor(it) },
        enableNightMode = activityViewModel.nightMode,
        changeNightMode = { activityViewModel.changeNightMode(it) }
    )
}

@Composable
private fun SettingScreen(
    enableDynamicColor: Boolean,
    changeDynamicColor: (Boolean) -> Unit,
    enableNightMode: Boolean,
    changeNightMode: (Boolean) -> Unit
) {

    val firstItems = listOf(
        SwitchItem(
            title = stringResource(R.string.dynamic_color),
            icon = Icons.Rounded.ColorLens,
            checked = enableDynamicColor,
            onCheckedChange = changeDynamicColor
        ),
        SwitchItem(
            title = stringResource(R.string.night_mode),
            summary = stringResource(R.string.night_mode_summary),
            icon = Icons.Rounded.Nightlight,
            checked = enableNightMode,
            onCheckedChange = changeNightMode
        ),
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current
    ) {
        item {
            ItemCard(firstItems)
        }
    }
}
