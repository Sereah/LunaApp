package com.lunacattus.app.connection.ui.routes.main.bluetooth.screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothSideEffect
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiState
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothViewModel
import com.lunacattus.app.connection.ui.theme.AppTheme
import com.lunacattus.app.connection.ui.theme.immediatelyIn
import com.lunacattus.app.connection.ui.theme.immediatelyOut
import com.lunacattus.ui_design.compose.Spinner
import com.lunacattus.ui_design.compose.clickableWithDebounce
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BtDiscoveryRoute(viewModel: BluetoothViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BtDiscoveryScreen(
        uiState = uiState,
        sendUiIntent = viewModel::processUiIntent,
        viewModel.sideEffect,
        onBack
    )
}

@Composable
fun BtDiscoveryScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    sideEffect: SharedFlow<BluetoothSideEffect>,
    onBack: () -> Unit,
) {

    var foundDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var localDeviceName by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        sendUiIntent.invoke(BluetoothUiIntent.Discovery(true))
        onDispose {
            sendUiIntent.invoke(BluetoothUiIntent.Discovery(false))
        }
    }

    LaunchedEffect(sideEffect) {
        sideEffect.collect {
            if (it is BluetoothSideEffect.BackDiscoveryScreen) {
                onBack.invoke()
            }
        }
    }

    LaunchedEffect(uiState) {
        foundDevices = uiState.discoveryDeviceList
        localDeviceName = uiState.info.name
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item { DiscoveryTitle(onBack, localDeviceName, uiState, sendUiIntent) }
        items(items = foundDevices, key = { it.address }) { device ->
            DiscoveryDeviceItem(device, sendUiIntent)
        }
    }
}

@Composable
private fun DiscoveryTitle(
    onBack: () -> Unit,
    localDeviceName: String,
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit
) {
    Column {
        Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickableWithDebounce {
                    onBack()
                }
        )
        Spacer(Modifier.height(20.dp))
        Text("扫描新设备", fontSize = 24.sp, color = AppTheme.colors.primary)
        Spacer(Modifier.height(10.dp))
        Text("设备名称", fontSize = 20.sp, color = AppTheme.colors.primary)
        Text(
            text = localDeviceName,
            fontSize = 15.sp,
            color = AppTheme.colors.inversePrimary
        )
        Spacer(Modifier.height(15.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp)
        ) {
            Text("可用设备", fontSize = 16.sp, color = AppTheme.colors.primary)
            AnimatedVisibility(
                uiState.discovery,
                enter = immediatelyIn,
                exit = immediatelyOut
            ) {
                Spinner(Modifier.size(40.dp))
            }
            AnimatedVisibility(
                !uiState.discovery,
                enter = immediatelyIn,
                exit = immediatelyOut
            ) {
                Text(
                    "刷新",
                    fontSize = 14.sp,
                    color = AppTheme.colors.icon,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickableWithDebounce {
                            sendUiIntent.invoke(BluetoothUiIntent.Discovery(true))
                        }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DiscoveryDeviceItem(
    device: BluetoothDevice,
    sendUiIntent: (BluetoothUiIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                AppTheme.colors.card,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp)
            .clickableWithDebounce {
                sendUiIntent.invoke(BluetoothUiIntent.PairNewDevice(device))
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            device.name ?: device.address,
            fontSize = 15.sp,
            color = AppTheme.colors.inversePrimary
        )
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            device.bondState == BluetoothDevice.BOND_BONDING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spinner(Modifier.size(40.dp))
        }
    }
}