package com.lunacattus.connection.ui.section.bluetooth.detail

import android.annotation.SuppressLint
import android.os.ParcelUuid
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlignVerticalBottom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import com.lunacattus.connection.model.bluetooth.BluetoothDeviceType
import com.lunacattus.connection.model.bluetooth.BondDevice
import com.lunacattus.connection.model.bluetooth.BondDeviceConnectType
import com.lunacattus.connection.model.bluetooth.displayName
import com.lunacattus.connection.model.bluetooth.getPreciseType
import com.lunacattus.connection.model.bluetooth.isCommonUuid
import com.lunacattus.connection.model.bluetooth.isVendorUuid
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.dialog.DialogActions
import com.lunacattus.ui_design.compose.dialog.MessageContent
import com.lunacattus.ui_design.compose.dialog.MessageDialog
import com.lunacattus.ui_design.compose.dialog.MessageDialogDefaults
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun BtDeviceDetailRoute(viewModel: BtDeviceDetailViewModel, onBack: () -> Unit) {
    val uiSate by viewModel.uiState.collectAsStateWithLifecycle()
    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiSate) {
        showLoading = uiSate.selectDevice == null
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(showLoading) {
            LinearProgressIndicator()
        }
        if (uiSate.selectDevice != null) {
            DeviceDetailScreen(
                uiSate.selectDevice!!,
                viewModel::processUiIntent,
                onBack
            )
        }
    }
}

@Composable
fun DeviceDetailScreen(
    selectedDevice: BondDevice,
    sendUiIntent: (BtDeviceDetailUiIntent) -> Unit,
    onBack: () -> Unit
) {
    var showUuidDetail by remember { mutableStateOf<ParcelUuid?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val uuidList = selectedDevice.uuidList.filter {
        it.isVendorUuid() || it.isCommonUuid()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 110.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        //设备图标和名字
        item { DeviceIconAndName(selectedDevice) }
        //连接控制
        item {
            DeviceControl(selectedDevice, sendUiIntent) {
                showDeleteConfirm = true
            }
        }
        item { Text("UUID列表", fontSize = 18.sp, modifier = Modifier.padding(top = 20.dp)) }
        item {
            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 15.dp)
            ) {
                uuidList.forEach { uuid ->
                    UuidItem(
                        uuid = uuid,
                        showDetail = { showUuidDetail = uuid },
                        goTo = {})
                    if (uuid != uuidList.last()) {
                        HorizontalDivider()
                    }
                }
            }
        }
        //设备地址
        item { AddressBottom(selectedDevice) }
    }

    if (showUuidDetail != null) {
        MessageDialog(
            onDismissRequest = {
                showUuidDetail = null
            },
            message = MessageContent.Text(
                value = showUuidDetail.toString()
            )
        )
    }

    if(showDeleteConfirm) {
        MessageDialog(
            onDismissRequest = {
                showDeleteConfirm = false
            },
            message = MessageContent.Text(value = "确认删除 ${selectedDevice.device.name} ？"),
            actions = DialogActions(
                confirmButtonText = "确认",
                cancelButtonText = "取消",
                onConfirm = {
                    sendUiIntent.invoke(BtDeviceDetailUiIntent.ForgetDevice)
                    onBack()
                }
            )
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceIconAndName(selectedDevice: BondDevice) {
    val icon = when (selectedDevice.device.getPreciseType()) {
        BluetoothDeviceType.PHONE -> Icons.Filled.PhoneAndroid
        BluetoothDeviceType.COMPUTER -> Icons.Filled.Computer
        BluetoothDeviceType.HEADSET -> Icons.Filled.Headset
        BluetoothDeviceType.CAR -> Icons.Filled.DirectionsCarFilled
        BluetoothDeviceType.INPUT -> Icons.Filled.Mouse
        else -> Icons.Filled.BluetoothConnected
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
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
    sendUiIntent: (BtDeviceDetailUiIntent) -> Unit,
    onDeleteDevice: (BondDevice) -> Unit,
) {
    val connectState = selectedDevice.connectType
    val isConnected = connectState == BondDeviceConnectType.Connected
    val isDisconnected = connectState == BondDeviceConnectType.Disconnected
    val isDisconnecting = connectState == BondDeviceConnectType.Disconnecting

    val actionIntent = if (isConnected) {
        BtDeviceDetailUiIntent.DisconnectDevice
    } else {
        BtDeviceDetailUiIntent.ConnectDevice
    }

    val actionIcon = if (isConnected || isDisconnecting) Icons.Rounded.Close else Icons.Rounded.Add
    val actionText = when (connectState) {
        BondDeviceConnectType.Connecting -> "连接中"
        BondDeviceConnectType.Connected -> "断开"
        BondDeviceConnectType.Disconnecting -> "断开中"
        BondDeviceConnectType.Disconnected -> "连接"
    }
    val actionColor =
        if (isConnected || isDisconnected) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickableWithDebounce {
                    onDeleteDevice(selectedDevice)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text("取消保存", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
        }
        VerticalDivider(
            modifier = Modifier
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickableWithDebounce(enable = isDisconnected || isConnected) {
                    sendUiIntent.invoke(actionIntent)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = actionColor,
                modifier = Modifier
                    .size(30.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                color = actionColor,
                text = actionText, fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun UuidItem(uuid: ParcelUuid, showDetail: () -> Unit, goTo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickableWithDebounce {
                showDetail()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(uuid.displayName(), fontSize = 15.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.clickableWithDebounce {
                goTo()
            }
        )
    }
}

@Composable
private fun AddressBottom(selectedDevice: BondDevice) {
    Column(modifier = Modifier.padding(top = 40.dp)) {
        Icon(
            imageVector = Icons.Rounded.AlignVerticalBottom,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(Modifier.height(15.dp))
        Text(
            "设备蓝牙地址: ${selectedDevice.device.address}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}