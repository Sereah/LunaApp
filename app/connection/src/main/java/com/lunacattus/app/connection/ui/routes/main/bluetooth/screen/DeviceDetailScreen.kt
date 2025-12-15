package com.lunacattus.app.connection.ui.routes.main.bluetooth.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlignVerticalBottom
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothViewModel
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BondDevice
import com.lunacattus.app.connection.ui.theme.AppTheme
import com.lunacattus.ui_design.compose.clickableWithDebounce

@Composable
fun DeviceDetailRoute(viewModel: BluetoothViewModel, onBack: () -> Unit) {
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    if (selectedDevice == null) {
        onBack()
    } else {
        DeviceDetailScreen(
            selectedDevice!!,
            viewModel::processUiIntent,
            onBack
        )
    }
}

@Composable
fun DeviceDetailScreen(
    selectedDevice: BondDevice,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    onBack: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        //返回按钮和页面标题
        item { DetailTitle(onBack) }
        //设备图标和名字
        item { DeviceIconAndName(selectedDevice, onBack) }
        //连接控制
        item { DeviceControl(selectedDevice, sendUiIntent, onBack) }
        //设备地址
        item { AddressBottom(selectedDevice) }
    }
}

@Composable
private fun DetailTitle(onBack: () -> Unit) {
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
    Text("设备详细信息", fontSize = 24.sp, color = AppTheme.colors.primary)
    Spacer(Modifier.height(10.dp))
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceIconAndName(selectedDevice: BondDevice, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.PhoneAndroid,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clickableWithDebounce {
                    onBack()
                }
        )
        Spacer(Modifier.height(10.dp))
        Text(
            selectedDevice.device.name ?: selectedDevice.device.address,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun DeviceControl(
    selectedDevice: BondDevice,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(
                AppTheme.colors.card,
                RoundedCornerShape(12.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickableWithDebounce {
                    sendUiIntent.invoke(BluetoothUiIntent.ForgetDevice(selectedDevice))
                    onBack()
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = AppTheme.colors.warning,
                modifier = Modifier
                    .size(25.dp)
            )
            Text("取消保存", fontSize = 14.sp, color = AppTheme.colors.warning)
        }
        VerticalDivider(
            modifier = Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(AppTheme.colors.divider.copy(alpha = 0.4f))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickableWithDebounce(enable = !selectedDevice.connecting) {
                    if (selectedDevice.isConnected) {
                        sendUiIntent.invoke(
                            BluetoothUiIntent.DisconnectDevice(
                                selectedDevice
                            )
                        )
                    } else {
                        sendUiIntent.invoke(
                            BluetoothUiIntent.ConnectDevice(
                                selectedDevice
                            )
                        )
                    }
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (selectedDevice.isConnected) {
                    Icons.Rounded.Close
                } else {
                    Icons.Rounded.Add
                },
                contentDescription = null,
                tint = if (selectedDevice.connecting) AppTheme.colors.inversePrimary
                else AppTheme.colors.icon,
                modifier = Modifier
                    .size(25.dp)
            )
            Text(
                color = if (selectedDevice.connecting) AppTheme.colors.inversePrimary
                else AppTheme.colors.icon,
                text = if (selectedDevice.isConnected) {
                    "断开"
                } else {
                    "连接"
                }, fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AddressBottom(selectedDevice: BondDevice) {
    Column {
        Icon(
            imageVector = Icons.Rounded.AlignVerticalBottom,
            contentDescription = null,
            tint = AppTheme.colors.inversePrimary,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(Modifier.height(15.dp))
        Text(
            "设备蓝牙地址: ${selectedDevice.device.address}",
            fontSize = 14.sp,
            color = AppTheme.colors.inversePrimary
        )
    }
}