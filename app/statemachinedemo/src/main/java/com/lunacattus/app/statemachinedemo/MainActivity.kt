package com.lunacattus.app.statemachinedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lunacattus.app.statemachinedemo.PageState.Companion.MSG_GO_TO_SLEEP
import com.lunacattus.app.statemachinedemo.ui.theme.LunaAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LunaAppTheme {
                Main()
            }
        }
    }
}

@Composable
fun Main() {
    val viewModel = hiltViewModel<MainViewModel>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            { viewModel.machineStart() }
        ) {
            Text("启动状态机")
        }

        Button({ viewModel.sendMessage(MSG_GO_TO_SLEEP) }) {
            Text("发送消息：MSG_GO_TO_SLEEP")
        }

        Button({ viewModel.machineQuit() }) {
            Text("退出状态机")
        }
    }
}