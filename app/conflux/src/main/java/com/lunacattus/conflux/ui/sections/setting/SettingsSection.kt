package com.lunacattus.conflux.ui.sections.setting

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.setting.homepage.SettingRoute
import com.lunacattus.conflux.ui.sections.setting.homepage.SettingViewModel
import kotlinx.serialization.Serializable

@Serializable
data object SettingRoute : NavKey

fun EntryProviderScope<NavKey>.settingSection() {
    entryWithNavAndVm<SettingRoute, SettingViewModel> { _, navigator, model ->
        SettingRoute(model)
    }
}