package com.lunacattus.conflux.ui.sections.llm.tts

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.ui_design.compose.MusicBars
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.overScrollVertical
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TtsRoute(
    viewModel: TtsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sendIntent = viewModel::handleIntent
    val context = LocalContext.current

    val textFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    val text = BufferedReader(InputStreamReader(input)).readText()
                    val filename = uri.lastPathSegment ?: "file.txt"
                    sendIntent(TtsIntent.SetLongTextFromFile(text, filename))
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect {}
    }

    TtsScreen(
        state = state,
        sendIntent = sendIntent,
        onPickTextFile = { textFileLauncher.launch(arrayOf("text/*")) },
        modifier = modifier,
    )
}

@Composable
private fun TtsScreen(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    onPickTextFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var configExpanded by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(state.messageGroups.size) {
        if (state.messageGroups.isNotEmpty()) {
            listState.animateScrollToItem(state.messageGroups.size + 7)
        }
    }

    Box(
        modifier = modifier
            .padding(LocalInnerPadding.current)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical(),
        ) {
            item(key = "config") {
                ConfigSection(
                    state,
                    sendIntent,
                    configExpanded
                ) { configExpanded = !configExpanded }
            }
            item(key = "status") { StatusBar(state, sendIntent) }
            item(key = "divider1") {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.06f
                    )
                )
            }
            item(key = "speaker") { SpeakerSection(state, sendIntent) }
            item(key = "playback") { PlaybackControls(state, sendIntent) }
            item(key = "divider2") {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.06f
                    )
                )
            }
            if (state.error != null) {
                item(key = "error") { ErrorBanner(state.error) { sendIntent(TtsIntent.DismissError) } }
            }
            if (state.messageGroups.isEmpty()) {
                item(key = "empty") { EmptyPlaceholder(Modifier.fillMaxWidth()) }
            } else {
                item(key = "clear_all") {
                    TextButton(
                        onClick = { sendIntent(TtsIntent.ClearAll) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DeleteForever,
                            null,
                            Modifier.size(16.dp),
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.tts_clear_all),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
                items(state.messageGroups, key = { it.id }) { group ->
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        MessageCard(
                            group = group, isPlaying = state.playingGroupId == group.id,
                            onPlayGroup = { sendIntent(TtsIntent.PlayGroup(group.id)) },
                            onPlayChunk = { idx ->
                                sendIntent(
                                    TtsIntent.PlaySingleChunk(
                                        group.id,
                                        idx
                                    )
                                )
                            },
                            onStop = { sendIntent(TtsIntent.StopAudio) },
                            onDelete = { sendIntent(TtsIntent.DeleteGroup(group.id)) })
                    }
                }
            }
            item(key = "input_spacer") { Spacer(Modifier.height(120.dp)) }
        }
        InputSection(
            state = state, sendIntent = sendIntent, onPickTextFile = onPickTextFile,
            inputMode = inputMode, onToggleInputMode = { inputMode = !inputMode },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ConfigSection(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    expanded: Boolean,
    toggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickableWithDebounce { toggle() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Hub,
                null,
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${state.host}:${state.port}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (expanded) stringResource(R.string.tts_collapse_host) else stringResource(R.string.tts_edit_host),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(visible = expanded) {
            Row(
                Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    state.host,
                    { sendIntent(TtsIntent.UpdateHost(it)) },
                    Modifier.weight(1f),
                    placeholder = { Text("host", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )
                OutlinedTextField(
                    state.port,
                    { sendIntent(TtsIntent.UpdatePort(it)) },
                    Modifier.width(80.dp),
                    placeholder = { Text("port", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusBar(state: TtsState, sendIntent: (TtsIntent) -> Unit) {
    val wsColor = when {
        state.wsConnected -> Color(0xFF4CAF50); state.wsConnecting -> Color(0xFFFFC107); else -> MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.3f
        )
    }
    val wsColorAnim by animateColorAsState(wsColor, tween(300))
    val httpColor =
        if (state.httpRequesting) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.3f
        )
    val httpColorAnim by animateColorAsState(httpColor, tween(300))

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(wsColorAnim))
            Spacer(Modifier.width(4.dp))
            Text(
                if (state.wsConnected) stringResource(R.string.tts_ws_connected) else if (state.wsConnecting) stringResource(
                    R.string.tts_ws_connecting
                ) else stringResource(R.string.tts_ws_disconnected),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(httpColorAnim))
            Spacer(Modifier.width(4.dp))
            Text(
                if (state.httpRequesting) stringResource(R.string.tts_http_requesting) else stringResource(
                    R.string.tts_http_idle
                ),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.weight(1f))
        FilterChip(
            state.requestMode == RequestMode.WebSocket,
            { sendIntent(TtsIntent.SetRequestMode(RequestMode.WebSocket)) },
            label = { Text("WS", fontSize = 11.sp) },
            modifier = Modifier.height(24.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.15f
                ), selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        FilterChip(
            state.requestMode == RequestMode.HTTP,
            { sendIntent(TtsIntent.SetRequestMode(RequestMode.HTTP)) },
            label = { Text("HTTP", fontSize = 11.sp) },
            modifier = Modifier.height(24.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.15f
                ), selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        SmallIconButton(
            if (state.wsConnected) Icons.Rounded.CloudOff else Icons.Rounded.Cloud,
            stringResource(R.string.tts_ws_connect_cd)
        ) {
            if (state.wsConnected) sendIntent(TtsIntent.DisconnectWs) else sendIntent(TtsIntent.ConnectWs)
        }
        SmallIconButton(
            Icons.Rounded.HealthAndSafety,
            stringResource(R.string.tts_health_check_cd)
        ) { sendIntent(TtsIntent.HealthCheck) }
    }
}

@Composable
private fun SpeakerSection(state: TtsState, sendIntent: (TtsIntent) -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeakerDropdown(state.speaker, sendIntent, Modifier.weight(1f))
            LangDropdown(state.language, sendIntent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            state.instruct,
            { sendIntent(TtsIntent.UpdateInstruct(it)) },
            Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.tts_instruct_hint), fontSize = 12.sp) },
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.3f
                ), unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun SpeakerDropdown(
    speaker: Speaker,
    sendIntent: (TtsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Box(modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .clickableWithDebounce { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Person,
                null,
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(speaker.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${speaker.description} · ${speaker.nativeLanguage}",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Rounded.ArrowDropDown,
                null,
                Modifier.size(20.dp),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            Speaker.ALL.forEach { spk ->
                val selected = spk == speaker
                DropdownMenuItem(text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                spk.name,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) Icon(
                                Icons.Rounded.Check,
                                null,
                                Modifier.size(18.dp),
                                MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "${spk.description} · ${spk.nativeLanguage}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }, onClick = { sendIntent(TtsIntent.SelectSpeaker(spk)); expanded = false })
            }
        }
    }
}

@Composable
private fun LangDropdown(
    lang: String,
    sendIntent: (TtsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Box(modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .clickableWithDebounce { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Language,
                null,
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                lang,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Rounded.ArrowDropDown,
                null,
                Modifier.size(20.dp),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            LANGUAGES.forEach { l ->
                val selected = l == lang
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            l,
                            Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (selected) Icon(
                            Icons.Rounded.Check,
                            null,
                            Modifier.size(18.dp),
                            MaterialTheme.colorScheme.primary
                        )
                    }
                }, onClick = { sendIntent(TtsIntent.SelectLanguage(l)); expanded = false })
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            null,
            Modifier.size(18.dp),
            MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(8.dp))
        Text(
            message,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Rounded.Close,
                stringResource(R.string.tts_close),
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.SettingsVoice,
                null,
                Modifier.size(56.dp),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.tts_placeholder_empty),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun MessageCard(
    group: TtsMessageGroup,
    isPlaying: Boolean,
    onPlayGroup: () -> Unit,
    onPlayChunk: (Int) -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.6f
            )
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val modeColor =
                    if (group.mode == RequestMode.WebSocket) Color(0xFF4CAF50) else Color(0xFFFFC107)
                Box(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(modeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(group.mode.name, fontSize = 9.sp, color = modeColor)
                }
                if (group.isLongText) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            stringResource(R.string.tts_long_text),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "${group.speaker} · ${group.language}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Text(
                    group.id.takeLast(8),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                    Icon(
                        Icons.Rounded.Delete,
                        stringResource(R.string.tts_delete_cd),
                        Modifier.size(14.dp),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                group.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val hasAudio = group.chunks.isNotEmpty()
                val bgColor by animateColorAsState(
                    if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.1f
                    ), tween(200)
                )
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickableWithDebounce { if (isPlaying) onStop() else onPlayGroup() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) MusicBars(
                        Modifier.size(18.dp),
                        barCount = 4,
                        barColor = MaterialTheme.colorScheme.onPrimary,
                        barSpacing = 1.dp,
                        cycleDurationMillis = 800
                    )
                    else Icon(
                        Icons.Rounded.PlayArrow,
                        stringResource(R.string.tts_play_all_cd),
                        Modifier.size(20.dp),
                        if (hasAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.2f
                        )
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            !group.isCompleted && group.totalChunks > 0 -> stringResource(
                                R.string.tts_synthesizing,
                                group.chunks.size,
                                group.totalChunks
                            )

                            !group.isCompleted -> stringResource(R.string.tts_waiting)
                            group.chunks.isEmpty() -> stringResource(R.string.tts_no_audio)
                            else -> stringResource(
                                R.string.tts_total_chunks,
                                group.chunks.size,
                                group.chunks.sumOf { it.durationMs })
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                if (group.chunks.size > 1 && group.isCompleted) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.tts_play_all),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (group.chunks.size > 1 && group.isCompleted) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.chunks.forEachIndexed { index, chunk ->
                        ChunkItem(
                            chunk,
                            index,
                            { onPlayChunk(index) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChunkItem(chunk: TtsAudioChunk, index: Int, onPlay: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .clickableWithDebounce { onPlay() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Headphones,
            null,
            Modifier.size(14.dp),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(R.string.tts_segment, index + 1),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${chunk.durationMs}ms",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Rounded.PlayArrow,
            stringResource(R.string.tts_play_cd),
            Modifier.size(16.dp),
            MaterialTheme.colorScheme.primary
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun PlaybackControls(state: TtsState, sendIntent: (TtsIntent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = "${String.format("%.1f", state.playbackSpeed)}x · ${(state.playbackVolume * 100).toInt()}%"
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickableWithDebounce { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Tune, null, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(if (expanded) stringResource(R.string.tts_collapse_host) else stringResource(R.string.tts_edit_host),
                fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Speed, null, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text("${String.format("%.1f", state.playbackSpeed)}x", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(52.dp))
                    Slider(state.playbackSpeed, { sendIntent(TtsIntent.SetPlaybackSpeed(it)) }, valueRange = 0.5f..3.0f, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VolumeUp, null, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text("${(state.playbackVolume * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(52.dp))
                    Slider(state.playbackVolume, { sendIntent(TtsIntent.SetPlaybackVolume(it)) }, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
private fun InputSection(state: TtsState, sendIntent: (TtsIntent) -> Unit, onPickTextFile: () -> Unit, inputMode: Boolean, onToggleInputMode: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)) {
        if (state.requestMode == RequestMode.HTTP && state.httpRequesting) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleInputMode) {
                Text(
                    if (inputMode) stringResource(R.string.tts_short_text) else stringResource(R.string.tts_long_text_file),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (inputMode) {
                TextButton(onClick = onPickTextFile) {
                    Icon(Icons.Rounded.OpenInNew, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.tts_pick_file), fontSize = 12.sp)
                }
                state.longTextFilename?.let { name ->
                    Text(
                        name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )
                }
            }
            Spacer(Modifier.weight(1f))
        }

        if (inputMode) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    state.longText,
                    { sendIntent(TtsIntent.UpdateLongText(it)) },
                    Modifier.weight(1f),
                    placeholder = {
                        Text(
                            stringResource(R.string.tts_long_text_hint),
                            fontSize = 12.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.bodySmall,
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.3f
                        ),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    )
                )
                Spacer(Modifier.width(8.dp))
                val canSend = state.longText.isNotBlank()
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickableWithDebounce { if (canSend) sendIntent(TtsIntent.SendText) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Send,
                        stringResource(R.string.tts_send_cd),
                        Modifier.size(22.dp),
                        if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        )
                    )
                }
            }
        } else {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    state.inputText,
                    { sendIntent(TtsIntent.UpdateText(it)) },
                    Modifier.weight(1f),
                    placeholder = {
                        Text(
                            stringResource(R.string.tts_input_hint),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.5f
                        ),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendIntent(TtsIntent.SendText) })
                )
                Spacer(Modifier.width(8.dp))
                val canSend = state.inputText.isNotBlank()
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickableWithDebounce { if (canSend) sendIntent(TtsIntent.SendText) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Send,
                        stringResource(R.string.tts_send_cd),
                        Modifier.size(22.dp),
                        if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.3f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(icon, description, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
