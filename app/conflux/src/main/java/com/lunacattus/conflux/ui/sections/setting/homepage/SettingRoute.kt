package com.lunacattus.conflux.ui.sections.setting.homepage

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lunacattus.conflux.R
import com.lunacattus.conflux.domain.settings.ConfluxAccessibilityService
import com.lunacattus.conflux.ui.LocalActivityViewModel
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.IconSource
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.SwitchItem
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun SettingRoute(viewModel: SettingViewModel) {
    val activityViewModel = LocalActivityViewModel.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val checkAccessibility = {
        val isEnable = isAccessibilityServiceEnabled(context, ConfluxAccessibilityService::class.java)
        viewModel.changeAccessibility(isEnable)
    }

    LaunchedEffect(Unit) {
        checkAccessibility()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkAccessibility()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val toast = stringResource(R.string.toast_close_accessibility)
    SettingScreen(
        enableDynamicColor = activityViewModel.dynamicColor,
        changeDynamicColor = { activityViewModel.changeDynamicColor(it) },
        enableNightMode = activityViewModel.nightMode,
        changeNightMode = { activityViewModel.changeNightMode(it) },
        enableAccessibility = viewModel.accessibilityEnable,
        changeAccessibility = { isChecked ->
            if (isChecked) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    val serviceId = "${context.packageName}/${ConfluxAccessibilityService::class.java.canonicalName}"
                    putExtra(":settings:show_fragment_args", serviceId)
                    putExtra(":settings:fragment_args_key", serviceId)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        }
    )
}

@Composable
private fun SettingScreen(
    enableDynamicColor: Boolean,
    changeDynamicColor: (Boolean) -> Unit,
    enableNightMode: Boolean,
    changeNightMode: (Boolean) -> Unit,
    enableAccessibility: Boolean,
    changeAccessibility: (Boolean) -> Unit
) {

    val firstItems = listOf(
        SwitchItem(
            title = stringResource(R.string.dynamic_color),
            icon = IconSource.Vector(Icons.Rounded.ColorLens),
            iconTint = LocalContentColor.current,
            checked = enableDynamicColor,
            onCheckedChange = changeDynamicColor
        ),
        SwitchItem(
            title = stringResource(R.string.night_mode),
            summary = stringResource(R.string.night_mode_summary),
            icon = IconSource.Vector(Icons.Rounded.Nightlight),
            iconTint = LocalContentColor.current,
            checked = enableNightMode,
            onCheckedChange = changeNightMode
        ),
        SwitchItem(
            title = stringResource(R.string.accessibility_service),
            icon = IconSource.Vector(Icons.Rounded.Accessibility),
            iconTint = LocalContentColor.current,
            checked = enableAccessibility,
            onCheckedChange = changeAccessibility
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

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val expectedId = "${context.packageName}/${serviceClass.canonicalName}"
    // 获取当前系统中所有已启用的无障碍服务
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

    for (service in enabledServices) {
        if (service.id == expectedId) return true
    }
    return false
}
