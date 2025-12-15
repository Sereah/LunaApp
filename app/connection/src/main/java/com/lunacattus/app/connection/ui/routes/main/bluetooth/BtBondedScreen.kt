package com.lunacattus.app.connection.ui.routes.main.bluetooth

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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiState
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothViewModel
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BondDevice
import com.lunacattus.app.connection.ui.theme.AppTheme
import com.lunacattus.ui_design.compose.clickableWithDebounce

@Composable
fun BtBondedRoute(
    viewModel: BluetoothViewModel,
    navToDeviceDetail: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BtBondedScreen(
        uiState = uiState,
        sendUiIntent = viewModel::processUiIntent,
        navToDeviceDetail,
        onBack
    )
}

@Composable
fun BtBondedScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    navToDeviceDetail: () -> Unit,
    onBack: () -> Unit
) {

    LaunchedEffect(Unit) {
        sendUiIntent.invoke(BluetoothUiIntent.LoadBondedDevices)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item { BondedTitle(onBack) }
        items(items = uiState.bondedDeviceList, key = { it.device.address }) { device ->
            BondedDeviceItem(device, sendUiIntent, navToDeviceDetail)
        }
    }
}

@Composable
private fun BondedTitle(onBack: () -> Unit) {
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
    Text("已连接的设备", fontSize = 24.sp, color = AppTheme.colors.primary)
    Spacer(Modifier.height(10.dp))
}

@SuppressLint("MissingPermission")
@Composable
private fun BondedDeviceItem(
    device: BondDevice,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    navToDeviceDetail: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                AppTheme.colors.card,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp)
            .clickableWithDebounce(!device.connecting && !device.disconnecting) {
                if (device.isConnected) {
                    sendUiIntent.invoke(BluetoothUiIntent.DisconnectDevice(device))
                } else {
                    sendUiIntent.invoke(BluetoothUiIntent.ConnectDevice(device))
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            device.device.name ?: device.device.address,
            fontSize = 18.sp,
            color = AppTheme.colors.primary
        )
        Spacer(Modifier.weight(1f))
        Text(
            when {
                device.connecting -> "连接中"
                device.disconnecting -> "断开中"
                device.isConnected -> "已连接"
                else -> "未连接"
            },
            fontSize = 15.sp,
            color = if (device.isConnected) AppTheme.colors.button else AppTheme.colors.inversePrimary
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 15.dp)
                .width(0.5.dp)
                .background(AppTheme.colors.divider)
        )
        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = null,
            tint = AppTheme.colors.inversePrimary,
            modifier = Modifier
                .size(24.dp)
                .clickableWithDebounce {
                    sendUiIntent.invoke(BluetoothUiIntent.OnClickDeviceSetting(device))
                    navToDeviceDetail()
                }
        )
    }
}