package com.lunacattus.conflux.ui.sections.setting

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.ui.base.BaseRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.setting.homepage.SettingRoute
import com.lunacattus.conflux.ui.sections.setting.homepage.SettingViewModel
import kotlinx.serialization.Serializable

@Serializable
data object SettingRoute : BaseRoute {
    override val name: String
        get() = "设置"
}

fun EntryProviderScope<NavKey>.settingSection() {
    entryWithNavAndVm<SettingRoute, SettingViewModel>(animated = false) { _, navigator, model ->
        SettingRoute(model)
    }
}