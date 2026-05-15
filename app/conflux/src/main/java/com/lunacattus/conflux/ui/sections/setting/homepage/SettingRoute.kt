package com.lunacattus.conflux.ui.sections.setting.homepage

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lunacattus.conflux.R
import com.lunacattus.conflux.domain.settings.ConfluxAccessibilityService
import com.lunacattus.conflux.ui.LocalActivityViewModel
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.GradientHeader
import com.lunacattus.conflux.ui.base.IconSource
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
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
        },
        openLanguageSettings = {
            context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
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
    changeAccessibility: (Boolean) -> Unit,
    openLanguageSettings: () -> Unit,
) {
    val appearanceItems = listOf(
        SwitchItem(
            title = stringResource(R.string.dynamic_color),
            icon = IconSource.Vector(Icons.Rounded.ColorLens),
            iconTint = MaterialTheme.colorScheme.primary,
            checked = enableDynamicColor,
            onCheckedChange = changeDynamicColor,
        ),
        SwitchItem(
            title = stringResource(R.string.night_mode),
            summary = stringResource(R.string.night_mode_summary),
            icon = IconSource.Vector(Icons.Rounded.Nightlight),
            iconTint = MaterialTheme.colorScheme.tertiary,
            checked = enableNightMode,
            onCheckedChange = changeNightMode,
        ),
    )

    val systemItems = listOf(
        NavigationItem(
            title = stringResource(R.string.language),
            summary = stringResource(R.string.language_summary),
            icon = IconSource.Vector(Icons.Rounded.Language),
            iconTint = MaterialTheme.colorScheme.secondary,
            onClick = openLanguageSettings,
        ),
        SwitchItem(
            title = stringResource(R.string.accessibility_service),
            icon = IconSource.Vector(Icons.Rounded.Accessibility),
            iconTint = MaterialTheme.colorScheme.primary,
            checked = enableAccessibility,
            onCheckedChange = changeAccessibility,
        ),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GradientHeader(
                title = stringResource(R.string.setting_title),
                subtitle = stringResource(R.string.app_name),
                icon = Icons.Rounded.Settings,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item {
            AnimatedCard(
                index = 0,
                items = appearanceItems,
                categoryText = stringResource(R.string.settings_appearance),
            )
        }

        item {
            AnimatedCard(
                index = 1,
                items = systemItems,
                categoryText = stringResource(R.string.settings_system),
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AnimatedCard(
    index: Int,
    items: List<com.lunacattus.conflux.ui.base.Item>,
    categoryText: String,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 150 + index * 120,
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 150 + index * 120,
            ),
            initialOffsetY = { it / 4 },
        ),
    ) {
        ItemCard(items = items, categoryText = categoryText)
    }
}

fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val expectedId = "${context.packageName}/${serviceClass.canonicalName}"
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

    for (service in enabledServices) {
        if (service.id == expectedId) return true
    }
    return false
}
