package com.lunacattus.app.connection.ui.routes.main.bluetooth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.lunacattus.ui_design.compose.clickableWithDebounce
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BtBondedRoute(viewModel: BluetoothViewModel, onBack: () -> Boolean) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BtBondedScreen(
        uiState = uiState,
        sendUiIntent = viewModel::processUiIntent,
        viewModel.sideEffect,
        onBack
    )
}

@Composable
fun BtBondedScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    sideEffect: SharedFlow<BluetoothSideEffect>,
    onBack: () -> Boolean
) {

    LaunchedEffect(Unit) {
        sendUiIntent.invoke(BluetoothUiIntent.LoadBondedDevices)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(items = uiState.bondedDeviceList, key = { it.address }) { device ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        AppTheme.colors.card,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp)
                    .clickableWithDebounce {
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    device.name ?: device.address,
                    fontSize = 18.sp,
                    color = AppTheme.colors.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (device.isConnected()) "已连接" else "未连接",
                    fontSize = 15.sp,
                    color = AppTheme.colors.inversePrimary
                )
            }
        }
    }

}