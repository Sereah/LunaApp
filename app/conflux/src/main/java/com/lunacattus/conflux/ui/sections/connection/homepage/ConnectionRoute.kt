package com.lunacattus.conflux.ui.sections.connection.homepage

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.conflux.ui.base.ItemCard
import com.lunacattus.conflux.ui.base.NavigationItem
import com.lunacattus.ui_design.compose.overScrollVertical
import kotlinx.coroutines.launch

@Composable
fun ConnectionRoute(model: ConnectionViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ConnectionScreen(
        navToAndroidAuto = {
            Intent().apply {
                action = "com.google.android.projection.gearhead.SETTINGS"
            }.let {
                runCatching {
                    context.startActivity(it)
                }.onFailure {
                    scope.launch { ActivityToastEvent.send(ToastEvent.ShowToast("打开失败")) }
                }
            }
        }
    )
}

@Composable
fun ConnectionScreen(
    navToAndroidAuto: () -> Unit
) {
    val connectionItems = listOf(
        NavigationItem(
            title = "AndroidAuto",
            icon = Icons.Rounded.DirectionsCar,
            onClick = navToAndroidAuto
        ),
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        item {
            ItemCard(connectionItems)
        }
    }
}