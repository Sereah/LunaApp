package com.lunacattus.app.connection.routes.main.bluetooth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiEffect
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiState
import com.lunacattus.app.connection.theme.AppTheme
import com.lunacattus.ui_design.compose.components.CircleLoader
import com.lunacattus.ui_design.compose.components.dialog.MessageDialog
import com.lunacattus.ui_design.compose.components.overScrollVertical
import com.lunacattus.ui_design.compose.extensions.clickableWithDebounce

@Composable
fun BluetoothScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    uiEffect: BluetoothUiEffect
) {

    val itemList = listOf(ItemData.Profile, ItemData.Address, ItemData.Name)
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<ItemData?>(null) }

    LaunchedEffect(uiEffect) {
        when (uiEffect) {
            is BluetoothUiEffect.Error -> {
                Toast.makeText(context, uiEffect.error, Toast.LENGTH_LONG).show()
            }

            BluetoothUiEffect.Idle -> {}
        }
    }

    if (dialog != null) {
        MessageDialog(
            onDismissRequest = {
                dialog = null
            },
            message = when (dialog) {
                ItemData.Profile -> uiState.profiles
                ItemData.Address -> uiState.address
                ItemData.Name -> uiState.name
                else -> ""
            },
            title = dialog!!.title,

        )
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircleLoader(
                color = Color(0xFF8BC34A),
                secondColor = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                isVisible = true,
                tailLength = 300f,
                cycleDuration = 1000
            )
        }
    } else {
        Content(itemList, sendUiIntent) {
            dialog = it
        }
    }
}

@Composable
private fun Content(
    itemList: List<ItemData>,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    onItemClick: (ItemData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(items = itemList) {
            Item(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.FilterNone,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "获取${it}"
            ) {
                when (it) {
                    ItemData.Profile -> sendUiIntent.invoke(BluetoothUiIntent.GetBluetoothProfile)
                    ItemData.Address -> sendUiIntent(BluetoothUiIntent.GetAddress)
                    ItemData.Name -> sendUiIntent(BluetoothUiIntent.GetName)
                }
                onItemClick.invoke(it)
            }
        }
    }
}

@Composable
fun Item(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                AppTheme.colors.primary.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .clickableWithDebounce {
                onClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides AppTheme.colors.primary
        ) {
            icon.invoke()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

enum class ItemData(val title: String) {
    Profile("Profile"),
    Address("Address"),
    Name("Name")
}