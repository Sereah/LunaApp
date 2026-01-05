package com.lunacattus.mutilscreen

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.lunacattus.logger.Logger
import com.lunacattus.mutilscreen.ui.theme.LunaAppTheme
import com.lunacattus.ui_design.compose.clickableWithDebounce

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LunaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current as MainActivity
    activity::class.java.methods.forEach {
        Logger.d("MainActivity", "method: ${it.name}")
    }
    val intent = Intent(activity, SecondActivity::class.java)
    val options = ActivityOptions.makeBasic().apply { launchDisplayId = 0 }.toBundle()
    Text(
        text = "Hello $name!",
        modifier = modifier.clickableWithDebounce {
            activity.startActivityAsUser(
                intent,
                options,
                UserHandle.getUserHandleForUid(0)
            )
        }
    )
}