package com.lunacattus.connection.ui.section.bluetooth.homepage

import android.bluetooth.BluetoothAdapter
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
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.ui_design.compose.CustomSwitch
import com.lunacattus.ui_design.compose.SwitchDefaults
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.dialog.MessageContent
import com.lunacattus.ui_design.compose.dialog.MessageDialog
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun BluetoothHomeRoute(
    viewModel: BluetoothHomeViewModel,
    navToBtDiscovery: (localDeviceName: String) -> Unit,
    navToBtBonded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BluetoothScreen(
        uiState,
        viewModel::processUiIntent,
        navToBtDiscovery,
        navToBtBonded
    )
}

@Composable
fun BluetoothScreen(
    uiState: BluetoothHomeUiState,
    sendUiIntent: (BluetoothHomeUiIntent) -> Unit,
    navToBtDiscovery: (localDeviceName: String) -> Unit = {},
    navToBtBonded: () -> Unit = {}
) {

    var showProfileDialog by remember { mutableStateOf(false) }
    var showUuidsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 110.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { BtSwitchItem(uiState.btState, sendUiIntent) }
        items(items = functionList(), key = { it.type }) { function ->
            FunctionItem(function.icon, function.title) {
                when (function.type) {
                    Type.ADD_NEW -> navToBtDiscovery(uiState.info.name)
                    Type.BONDED -> navToBtBonded()
                    Type.LOCAL_PROFILE -> showProfileDialog = true
                    Type.LOCAL_UUIDS -> showUuidsDialog = true
                }
            }
        }
    }

    if (showProfileDialog) {
        MessageDialog(
            onDismissRequest = {
                showProfileDialog = false
            },
            message = MessageContent.Lines(uiState.info.profiles)
        )
    }

    if (showUuidsDialog) {
        MessageDialog(
            onDismissRequest = {
                showUuidsDialog = false
            },
            message = MessageContent.Lines(uiState.info.uuidList.map { it.toString() })
        )
    }
}

private enum class Type {
    ADD_NEW, BONDED, LOCAL_PROFILE, LOCAL_UUIDS
}

private data class BtFunction(
    val type: Type,
    val title: String,
    val icon: ImageVector
)

private fun functionList(): List<BtFunction> {
    return listOf(
        BtFunction(
            type = Type.ADD_NEW,
            title = "连接新设备",
            icon = Icons.Rounded.Add
        ),
        BtFunction(
            type = Type.BONDED,
            title = "保存的设备",
            icon = Icons.Rounded.BluetoothConnected
        ),
        BtFunction(
            type = Type.LOCAL_PROFILE,
            title = "本机支持的协议",
            icon = Icons.Rounded.LocalOffer
        ),
        BtFunction(
            type = Type.LOCAL_UUIDS,
            title = "本机UUID",
            icon = Icons.Rounded.CastConnected
        ),
    )
}

@Composable
private fun FunctionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .clickableWithDebounce {
                onClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
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

@Composable
private fun BtSwitchItem(btState: Int, sendUiIntent: (BluetoothHomeUiIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("蓝牙开关", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        CustomSwitch(
            colors = SwitchDefaults.colors()
                .copy(checkedTrackColor = MaterialTheme.colorScheme.primary),
            enabled = btState == BluetoothAdapter.STATE_ON || btState == BluetoothAdapter.STATE_OFF,
            checked = btState == BluetoothAdapter.STATE_ON || btState == BluetoothAdapter.STATE_TURNING_ON,
            onCheckedChanged = {
                sendUiIntent(BluetoothHomeUiIntent.SwitchEnable)
            }
        )
    }
}