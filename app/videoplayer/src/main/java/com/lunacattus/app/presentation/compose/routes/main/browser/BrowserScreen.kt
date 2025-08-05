package com.lunacattus.app.presentation.compose.routes.main.browser

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.lunacattus.app.domain.model.Video
import com.lunacattus.app.domain.model.VideoType
import com.lunacattus.app.presentation.compose.MainActivity
import com.lunacattus.app.presentation.compose.R
import com.lunacattus.app.presentation.compose.common.components.overScrollVertical
import com.lunacattus.app.presentation.compose.common.extensions.clickableWithDebounce
import com.lunacattus.app.presentation.compose.routes.main.browser.mvi.BrowserUiIntent
import com.lunacattus.app.presentation.compose.routes.player.mvi.PlayerViewModel
import com.lunacattus.app.presentation.compose.theme.AppTheme
import com.lunacattus.logger.Logger

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
    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val video = buildVideoFromUri(context, uri)
                sendUiIntent(BrowserUiIntent.AddStreamToPlayList(video))
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = PaddingValues(vertical = 100.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            BrowserItem(
                icon = {
                    Image(
                        painter = painterResource(R.drawable.ic_web),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "Add Network stream",
                onClick = { showStreamDialog = true }
            )
        }

        item {
            BrowserItem(
                icon = {
                    Image(
                        painter = painterResource(R.drawable.ic_local_file),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                title = "Add From Local File",
                onClick = {
                    mediaLauncher.launch(arrayOf("video/*"))
                }
            )
        }
    }

    if (showStreamDialog) {
        WebStreamDialog(
            onConfirm = { streamUrl, name ->
                sendUiIntent(
                    BrowserUiIntent.AddStreamToPlayList(
                        Video(
                            description = "",
                            uri = streamUrl,
                            subtitle = "",
                            coverPic = "",
                            title = name,
                            type = VideoType.WebStream
                        )
                    )
                )
                showStreamDialog = false
            },
            onDismiss = { showStreamDialog = false }
        )
    }
}

@Composable
fun BrowserItem(
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
        icon.invoke()
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = Color.Black,
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

fun buildVideoFromUri(context: Context, uri: Uri): Video {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        Logger.d(tag = TAG, message = "buildVideoFromUri, duration: $duration, title: $title, artist: $artist, album: $album")
        if (title == null) {
            title = getDisplayName(context, uri)
        }
        Video(
            description = "",
            uri = uri.toString(),
            subtitle = artist ?: "",
            title = title,
            coverPic = album ?: "",
            type = VideoType.LocalVideo
        )

    } catch (e: Exception) {
        Video(
            description = "",
            uri = uri.toString(),
            subtitle = "",
            title = "",
            coverPic = "",
            type = VideoType.LocalVideo
        )
    } finally {
        retriever.release()
    }
}

fun getDisplayName(context: Context, uri: Uri): String {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return "Unknown"
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