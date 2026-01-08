package com.lunacattus.mutilscreen

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lunacattus.logger.Logger
import com.lunacattus.mutilscreen.ui.theme.LunaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LunaAppTheme {

                var displayId by remember { mutableStateOf("0") }
                var userId by remember { mutableStateOf("0") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Text("displayId: ")
                            Spacer(Modifier.weight(1f))
                            TextField(
                                value = displayId,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        displayId = newValue
                                    }
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Text("userId: ")
                            Spacer(Modifier.weight(1f))
                            TextField(
                                value = userId,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        userId = newValue
                                    }
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            launch(displayId.toInt(), userId.toInt())
                        }) {
                            Text("启动副驾屏界面")
                        }
                    }
                }
            }
        }
    }

    private fun launch(displayId: Int, userId: Int) {
        Logger.d("", "displayId: $displayId, userId: $userId")
        try {
            val intent = Intent(this, SecondActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val options = ActivityOptions.makeBasic().apply { launchDisplayId = displayId }
            val pend: PendingIntent? = PendingIntent.getActivityAsUser(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE, options.toBundle(), UserHandle.of(userId)
            )
            pend?.send()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }
}