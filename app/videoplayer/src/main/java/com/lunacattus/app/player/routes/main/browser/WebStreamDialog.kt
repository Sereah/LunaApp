package com.lunacattus.app.player.routes.main.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lunacattus.app.player.theme.AppTheme

@Composable
fun WebStreamDialog(
    onDismiss: () -> Unit,
    onConfirm: (streamUrl: String, name: String) -> Unit,
) {

    var streamUrl by rememberSaveable { mutableStateOf("") }
    var isStreamUrlInValid by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        }
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = "Please enter any HTTP, RTSP, RTMP, MMS, FTP or UDP/RTP address.",
                    fontSize = 15.sp,
                    color = AppTheme.colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
                )
                TextField(
                    value = streamUrl,
                    onValueChange = {
                        streamUrl = it
                        if (it == "") {
                            isStreamUrlInValid = false
                        }
                    },
                    placeholder = {
                        Text(
                            text = "http://myserver.com/file.mp4",
                            color = Color.Black.copy(alpha = 0.2f)
                        )
                    },
                    colors = TextFieldDefaults.colors().copy(
                        unfocusedIndicatorColor = AppTheme.colors.primary.copy(alpha = .2f),
                        focusedIndicatorColor = AppTheme.colors.primary.copy(alpha = .5f),
                        cursorColor = AppTheme.colors.primary.copy(alpha = .5f),
                        errorIndicatorColor = Color.Red.copy(alpha = 0.5f),
                        errorCursorColor = Color.Red.copy(alpha = 0.5f),
                    ),
                    supportingText = {
                        if (isStreamUrlInValid) {
                            Text(text = "Invalid Url", fontSize = 14.sp, color = Color.Red)
                        }
                    },
                    isError = isStreamUrlInValid,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (isSupportedVideoUrl(streamUrl)) {
                            isStreamUrlInValid = false
                            val name = getFileNameFromUrl(streamUrl)
                            onConfirm.invoke(streamUrl, name ?: "")
                        } else {
                            isStreamUrlInValid = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = AppTheme.colors.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Add To Play List",
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}