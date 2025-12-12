package com.lunacattus.app.connection.ui.routes.main.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi.BluetoothViewModel
import com.lunacattus.app.connection.ui.theme.AppTheme

@Composable
fun DeviceDetailRoute(viewModel: BluetoothViewModel, onBack: () -> Boolean) {
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    DeviceDetailScreen(
        selectedDevice,
        viewModel::processUiIntent,
        onBack
    )
}

@Composable
fun DeviceDetailScreen(
    selectedDevice: BluetoothDevice?,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    onBack: () -> Boolean
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedDevice?.name ?: selectedDevice?.address ?: "", fontSize = 20.sp)
            }
        }
    }
}