package com.lunacattus.app.connection.ui.routes.main.bluetooth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.lunacattus.ui_design.compose.CustomSwitch
import com.lunacattus.ui_design.compose.SwitchDefaults
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.overScrollVertical
import kotlinx.coroutines.flow.Flow

@Composable
fun BluetoothRoute(
    viewModel: BluetoothViewModel,
    navToBtDiscovery: () -> Unit,
    navToBtBonded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BluetoothScreen(
        uiState,
        viewModel::processUiIntent,
        viewModel.sideEffect,
        navToBtDiscovery,
        navToBtBonded
    )
}

@Composable
fun BluetoothScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    uiEffect: Flow<BluetoothSideEffect>,
    navToBtDiscovery: () -> Unit = {},
    navToBtBonded: () -> Unit = {}
) {

    var btState by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        sendUiIntent.invoke(BluetoothUiIntent.LoadInfo)
    }

    LaunchedEffect(uiState) {
        btState = uiState.btState
    }

    LaunchedEffect(uiEffect) {
        uiEffect.collect {}
    }
    Content(btState, sendUiIntent, navToBtDiscovery, navToBtBonded)
}

@Composable
private fun Content(
    btState: Int,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    navToBtDiscovery: () -> Unit,
    navToBtBonded: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        AppTheme.colors.card,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("蓝牙开关", fontSize = 18.sp, color = AppTheme.colors.primary)
                Spacer(Modifier.weight(1f))
                CustomSwitch(
                    colors = SwitchDefaults.colors()
                        .copy(checkedTrackColor = AppTheme.colors.button),
                    enabled = btState == BluetoothAdapter.STATE_ON || btState == BluetoothAdapter.STATE_OFF,
                    checked = btState == BluetoothAdapter.STATE_ON || btState == BluetoothAdapter.STATE_TURNING_ON,
                    onCheckedChanged = {
                        sendUiIntent(BluetoothUiIntent.SwitchEnable)
                    }
                )
            }
        }
        item {
            Item(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "连接新设备"
            ) {
                navToBtDiscovery()
            }
        }
        item {
            Item(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.BluetoothConnected,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "已绑定的设备"
            ) {
                navToBtBonded()
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
                AppTheme.colors.card,
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