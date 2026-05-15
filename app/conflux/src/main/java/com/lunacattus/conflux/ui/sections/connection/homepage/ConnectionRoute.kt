package com.lunacattus.conflux.ui.sections.connection.homepage

import android.content.Intent
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
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.conflux.ui.base.GradientHeader
import com.lunacattus.conflux.ui.base.IconSource
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
            title = stringResource(R.string.androidAuto),
            icon = IconSource.Vector(Icons.Rounded.DirectionsCar),
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = navToAndroidAuto,
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
                title = stringResource(R.string.connection_title),
                subtitle = stringResource(R.string.connection_subtitle),
                icon = Icons.Rounded.Link,
                iconTint = MaterialTheme.colorScheme.secondary,
                gradientColors = listOf(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                ),
                glowTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item {
            AnimatedCard(
                index = 0,
                items = connectionItems,
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AnimatedCard(
    index: Int,
    items: List<com.lunacattus.conflux.ui.base.Item>,
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
        ItemCard(items = items)
    }
}
