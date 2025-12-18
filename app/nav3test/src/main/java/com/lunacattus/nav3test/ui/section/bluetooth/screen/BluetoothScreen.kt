package com.lunacattus.nav3test.ui.section.bluetooth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lunacattus.nav3test.ui.theme.AppTheme
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun BluetoothRoute(
    navToBtDiscovery: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Button(onClick = { navToBtDiscovery() }) {
                Text("navToBtDiscovery")
            }
        }
    }
}