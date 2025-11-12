package com.lunacattus.app.connection.routes.main.bluetooth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothSideEffect
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiIntent
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothUiState
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.BluetoothViewModel
import com.lunacattus.app.connection.routes.main.bluetooth.mvi.ItemData
import com.lunacattus.app.connection.theme.AppTheme
import com.lunacattus.ui_design.compose.components.CircleLoader
import com.lunacattus.ui_design.compose.components.dialog.MessageDialog
import com.lunacattus.ui_design.compose.components.overScrollVertical
import com.lunacattus.ui_design.compose.extensions.clickableWithDebounce
import kotlinx.coroutines.flow.Flow

@Composable
fun BluetoothRoute(viewModel: BluetoothViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BluetoothScreen(
        uiState,
        viewModel::processUiIntent,
        viewModel.sideEffect
    )
}

@Composable
fun BluetoothScreen(
    uiState: BluetoothUiState,
    sendUiIntent: (BluetoothUiIntent) -> Unit,
    uiEffect: Flow<BluetoothSideEffect>
) {

    val itemList = ItemData.entries
    val context = LocalContext.current

    LaunchedEffect(uiEffect) {
        uiEffect.collect {
            when (it) {
                is BluetoothSideEffect.ShowToast -> {
                    Toast.makeText(context, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (uiState.dialogItem != null) {
        MessageDialog(
            onDismissRequest = {
                sendUiIntent(BluetoothUiIntent.DismissDialog)
            },
            message = when (uiState.dialogItem) {
                ItemData.Profile -> uiState.info.profiles
                ItemData.Address -> uiState.info.address
                ItemData.Name -> uiState.info.name
            },
            title = uiState.dialogItem.title
        )
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircleLoader(
                color = Color(0xFF8BC34A),
                secondColor = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                isVisible = true,
                tailLength = 300f,
                cycleDuration = 1000
            )
        }
    }
    Content(itemList, sendUiIntent)
}

@Composable
private fun Content(
    itemList: List<ItemData>,
    sendUiIntent: (BluetoothUiIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(items = itemList) {
            Item(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.FilterNone,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "获取${it.title}"
            ) {
                sendUiIntent(BluetoothUiIntent.LoadItem(it))
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
                AppTheme.colors.primary.copy(alpha = 0.2f),
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