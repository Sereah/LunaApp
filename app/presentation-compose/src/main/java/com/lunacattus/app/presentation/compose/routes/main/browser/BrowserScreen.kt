package com.lunacattus.app.presentation.compose.routes.main.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.lunacattus.app.domain.model.Video
import com.lunacattus.app.domain.model.VideoType
import com.lunacattus.app.presentation.compose.MainActivity
import com.lunacattus.app.presentation.compose.common.components.overScrollVertical
import com.lunacattus.app.presentation.compose.common.extensions.clickableWithDebounce
import com.lunacattus.app.presentation.compose.routes.main.browser.mvi.BrowserUiIntent
import com.lunacattus.app.presentation.compose.routes.player.mvi.PlayerViewModel
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier,
    sendUiIntent: (BrowserUiIntent) -> Unit,
    navToPlayer: () -> Unit,
) {

    val context = LocalContext.current
    val activity = remember { context as MainActivity }
    val playerViewModel = hiltViewModel<PlayerViewModel>(activity)
    var showStreamDialog by remember { mutableStateOf(false) }
    var streamUrl by rememberSaveable { mutableStateOf("") }
    var isStreamUrlInValid by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp)
    ) {
        item {
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
                        showStreamDialog = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Network stream",
                    color = AppTheme.colors.primary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }

    if (showStreamDialog) {
        Dialog(
            onDismissRequest = {
                showStreamDialog = false
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
                                sendUiIntent(
                                    BrowserUiIntent.AddStreamToPlayList(
                                        Video(
                                            description = "",
                                            uri = streamUrl,
                                            subtitle = "",
                                            coverPic = "",
                                            title = name ?: "",
                                            type = VideoType.WebStream
                                        )
                                    )
                                )
                                showStreamDialog = false
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
}

fun isSupportedVideoUrl(url: String): Boolean {
    return try {
        val uri = url.toUri()
        val scheme = uri.scheme?.lowercase() ?: return false
        scheme in listOf("http", "https", "rtsp", "rtmp", "mms", "ftp", "udp")
                && !uri.host.isNullOrEmpty()
                && uri.pathSegments.isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

fun getFileNameFromUrl(url: String): String? {
    return try {
        val uri = url.toUri()
        val path = uri.path ?: return null
        val segments = path.split("/")
        segments.lastOrNull { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}