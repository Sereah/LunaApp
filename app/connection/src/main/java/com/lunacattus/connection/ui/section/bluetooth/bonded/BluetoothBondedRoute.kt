package com.lunacattus.connection.ui.section.bluetooth.bonded

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.connection.model.bluetooth.BluetoothDeviceType
import com.lunacattus.connection.model.bluetooth.BondDevice
import com.lunacattus.connection.model.bluetooth.BondDeviceConnectType
import com.lunacattus.connection.model.bluetooth.getPreciseType
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.onClickWithDebounced
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun BluetoothBondedRoute(
    viewModel: BluetoothBondedViewModel,
    navToDeviceDetail: (address: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BtBondedScreen(
        uiState = uiState,
        sendUiIntent = viewModel::processUiIntent,
        navToDeviceDetail
    )
}

@Composable
fun BtBondedScreen(
    uiState: BluetoothBondedUiState,
    sendUiIntent: (BluetoothBondedUiIntent) -> Unit,
    navToDeviceDetail: (address: String) -> Unit,
) {

    LaunchedEffect(Unit) {
        sendUiIntent.invoke(BluetoothBondedUiIntent.LoadBondedDevices)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 110.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(items = uiState.bondedDeviceList, key = { it.device.address }) { device ->
            BondedDeviceItem(device, sendUiIntent, navToDeviceDetail)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BondedDeviceItem(
    device: BondDevice,
    sendUiIntent: (BluetoothBondedUiIntent) -> Unit,
    navToDeviceDetail: (address: String) -> Unit
) {
    val isConnected = device.connectType == BondDeviceConnectType.Connected
    val isDisconnected = device.connectType == BondDeviceConnectType.Disconnected
    val icon = when (device.device.getPreciseType()) {
        BluetoothDeviceType.PHONE -> Icons.Filled.PhoneAndroid
        BluetoothDeviceType.COMPUTER -> Icons.Filled.Computer
        BluetoothDeviceType.HEADSET -> Icons.Filled.Headset
        BluetoothDeviceType.CAR -> Icons.Filled.DirectionsCarFilled
        BluetoothDeviceType.INPUT -> Icons.Filled.Mouse
        else -> Icons.Filled.BluetoothConnected
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp)
            .clickableWithDebounce(isDisconnected || isConnected) {
                if (isConnected) {
                    sendUiIntent.invoke(BluetoothBondedUiIntent.DisconnectDevice(device))
                } else {
                    sendUiIntent.invoke(BluetoothBondedUiIntent.ConnectDevice(device))
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.padding(end = 10.dp))
        Text(
            text = device.device.name ?: device.device.address,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        )
        Text(
            when (device.connectType) {
                BondDeviceConnectType.Connecting -> "连接中"
                BondDeviceConnectType.Connected -> "已连接"
                BondDeviceConnectType.Disconnecting -> "断开中"
                BondDeviceConnectType.Disconnected -> "未连接"
            },
            fontSize = 15.sp,
            color = if (device.connectType == BondDeviceConnectType.Connected)
                MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.5f
            )
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 15.dp)
                .width(0.5.dp)
        )
        IconButton(onClick = onClickWithDebounced { navToDeviceDetail(device.device.address) }) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}