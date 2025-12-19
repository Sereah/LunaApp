package com.lunacattus.connection.ui.section.bluetooth.discovery

import android.annotation.SuppressLint
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
import androidx.compose.material3.MaterialTheme
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
import com.lunacattus.connection.model.bluetooth.DiscoveryDevice
import com.lunacattus.connection.ui.theme.immediatelyIn
import com.lunacattus.connection.ui.theme.immediatelyOut
import com.lunacattus.ui_design.compose.Spinner
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun BluetoothDiscoveryRoute(
    localDeviceName: String,
    viewModel: BluetoothDiscoveryViewModel,
    onSuccessBonded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BtDiscoveryScreen(
        localDeviceName = localDeviceName,
        uiState = uiState,
        sendUiIntent = viewModel::processUiIntent,
        onSuccessBonded
    )
}

@Composable
fun BtDiscoveryScreen(
    localDeviceName: String,
    uiState: BluetoothDiscoveryUiState,
    sendUiIntent: (BluetoothDiscoveryUiIntent) -> Unit,
    onSuccessBonded: () -> Unit
) {

    var foundDevices by remember { mutableStateOf(emptyList<DiscoveryDevice>()) }

    DisposableEffect(Unit) {
        sendUiIntent.invoke(BluetoothDiscoveryUiIntent.Discovery(true))
        onDispose {
            sendUiIntent.invoke(BluetoothDiscoveryUiIntent.Discovery(false))
        }
    }

    LaunchedEffect(uiState) {
        foundDevices = uiState.discoveryDeviceList
        if (uiState.successBonded) {
            onSuccessBonded()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 110.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item { DiscoveryTitle(localDeviceName, uiState, sendUiIntent) }
        items(items = foundDevices, key = { it.device.address }) { device ->
            DiscoveryDeviceItem(device, sendUiIntent)
        }
    }
}

@Composable
private fun DiscoveryTitle(
    localDeviceName: String,
    uiState: BluetoothDiscoveryUiState,
    sendUiIntent: (BluetoothDiscoveryUiIntent) -> Unit
) {
    Column {
        Spacer(Modifier.height(10.dp))
        Text("设备名称", fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = localDeviceName,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(15.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp)
        ) {
            Text("可用设备", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
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
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickableWithDebounce {
                            sendUiIntent.invoke(BluetoothDiscoveryUiIntent.Discovery(true))
                        }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DiscoveryDeviceItem(
    device: DiscoveryDevice,
    sendUiIntent: (BluetoothDiscoveryUiIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp)
            .clickableWithDebounce {
                sendUiIntent.invoke(BluetoothDiscoveryUiIntent.PairNewDevice(device))
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            device.device.name ?: device.device.address,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            device.isBonding,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spinner(Modifier.size(40.dp))
        }
    }
}