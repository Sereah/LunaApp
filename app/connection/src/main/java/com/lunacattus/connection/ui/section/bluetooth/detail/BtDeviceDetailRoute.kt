package com.lunacattus.connection.ui.section.bluetooth.detail

import android.annotation.SuppressLint
import android.bluetooth.BluetoothProfile
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlignVerticalBottom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.connection.domain.bluetooth.isCommonUuid
import com.lunacattus.connection.domain.bluetooth.isVendorUuid
import com.lunacattus.connection.ui.ActivityToastEvent
import com.lunacattus.connection.ui.ToastEvent
import com.lunacattus.connection.ui.section.bluetooth.BondDevice
import com.lunacattus.connection.ui.section.bluetooth.BondDeviceConnectType
import com.lunacattus.connection.ui.section.bluetooth.DeviceUUID
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.dialog.MessageContent
import com.lunacattus.ui_design.compose.dialog.MessageDialog
import com.lunacattus.ui_design.compose.overScrollVertical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BtDeviceDetailRoute(address: String, viewModel: BtDeviceDetailViewModel, onBack: () -> Unit) {
    val uiSate by viewModel.uiState.collectAsStateWithLifecycle()
    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(address) {
        viewModel.processUiIntent(BtDeviceDetailUiIntent.GetDevice(address))
    }

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

    val scope = rememberCoroutineScope()
    var showMessageDialog by remember { mutableStateOf(false) }
    val vendorUuidList = selectedDevice.uuidList.filter {
        it.uuid.isVendorUuid()
    }
    val profileUuidList = selectedDevice.uuidList.filter {
        it.uuid.isCommonUuid()
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
        item { DeviceControl(selectedDevice, sendUiIntent, onBack) }
        //获取UUID列表
        item {
            UUIDItem(scope, selectedDevice.uuidList) {
                showMessageDialog = true
            }
        }
        items(items = profileUuidList, key = { it.uuid }) { uuid ->
            ProfileItem(uuid, sendUiIntent)
        }
        items(items = vendorUuidList, key = { it.uuid }) { uuid ->
            VendorUuidItem(uuid, sendUiIntent)
        }
        //设备地址
        item { AddressBottom(selectedDevice) }
    }

    if (showMessageDialog && selectedDevice.uuidList.isNotEmpty()) {
        MessageDialog(
            onDismissRequest = {
                showMessageDialog = false
            },
            title = "UUID列表",
            message = MessageContent.Lines(selectedDevice.uuidList.map { it.toString() })
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeviceIconAndName(selectedDevice: BondDevice) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.PhoneAndroid,
            contentDescription = null,
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
    onBack: () -> Unit,
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
        if (isConnected || isDisconnected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickableWithDebounce {
                    sendUiIntent.invoke(BtDeviceDetailUiIntent.ForgetDevice)
                    onBack()
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(25.dp)
            )
            Text("取消保存", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
        }
        VerticalDivider(
            modifier = Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
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
                    .size(25.dp)
            )
            Text(
                color = actionColor,
                text = actionText, fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun UUIDItem(
    scope: CoroutineScope,
    deviceUuids: List<DeviceUUID>,
    showDialogShow: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .height(50.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 15.dp)
            .clickableWithDebounce {
                if (deviceUuids.isEmpty()) {
                    scope.launch {
                        ActivityToastEvent.send(ToastEvent.ShowToast("UUID为空"))
                    }
                } else {
                    showDialogShow.invoke()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text("查看UUID列表", fontSize = 17.sp)
    }
}

@Composable
private fun ProfileItem(uuid: DeviceUUID, sendUiIntent: (BtDeviceDetailUiIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val isChecked = uuid.connectState == BluetoothProfile.STATE_CONNECTED ||
                uuid.connectState == BluetoothProfile.STATE_CONNECTING
        val isEnable = uuid.connectState == BluetoothProfile.STATE_CONNECTED ||
                uuid.connectState == BluetoothProfile.STATE_DISCONNECTED
        Text(uuid.name, fontSize = 17.sp)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            enabled = isEnable,
            onCheckedChange = {
                sendUiIntent(BtDeviceDetailUiIntent.ChangeProfileConnectState(uuid.uuid, !isChecked))
            }
        )
    }
}

@Composable
private fun VendorUuidItem(uuid: DeviceUUID, sendUiIntent: (BtDeviceDetailUiIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 15.dp)
            .clickableWithDebounce {
                sendUiIntent.invoke(
                    BtDeviceDetailUiIntent.ConnectUuid(uuid)
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(uuid.name, fontSize = 15.sp)
    }
}

@Composable
private fun AddressBottom(selectedDevice: BondDevice) {
    Column {
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