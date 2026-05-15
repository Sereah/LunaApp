package com.lunacattus.conflux.ui.sections.llm.tts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Headphones
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.ui_design.compose.MusicBars
import com.lunacattus.ui_design.compose.clickableWithDebounce
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

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

    TtsScreen(
        state = state,
        sendIntent = sendIntent,
        onPickTextFile = { textFileLauncher.launch(arrayOf("text/*")) },
        effects = viewModel.effects,
        modifier = modifier,
    )
}

@Composable
private fun TtsScreen(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    onPickTextFile: () -> Unit,
    effects: kotlinx.coroutines.flow.Flow<TtsEffect>,
    modifier: Modifier = Modifier,
) {
    var settingsExpanded by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            if (effect is TtsEffect.ScrollToBottom && state.messageGroups.isNotEmpty()) {
                listState.animateScrollToItem(state.messageGroups.size + 3)
            }
        }
    }

    Column(
        modifier = modifier
            .padding(LocalInnerPadding.current)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TtsHeader(
            state = state,
            sendIntent = sendIntent,
            onSettingsClick = { settingsExpanded = true }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (state.error != null) {
                item(key = "error") { ErrorBanner(state.error) { sendIntent(TtsIntent.DismissError) } }
            }
            if (state.messageGroups.isEmpty()) {
                item(key = "empty") { EmptyPlaceholder() }
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
                    Column(
                        Modifier
                            .animateItem()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        SwipeableDeleteItem(
                            onDelete = { sendIntent(TtsIntent.DeleteGroup(group.id)) }
                        ) {
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
                                onStop = { sendIntent(TtsIntent.StopAudio) })
                        }
                    }
                }
            }
            item(key = "input_spacer") { Spacer(Modifier.height(120.dp)) }
        }
        InputSection(
            state = state, sendIntent = sendIntent, onPickTextFile = onPickTextFile,
            inputMode = inputMode, onToggleInputMode = { inputMode = !inputMode },
        )
    }

    if (settingsExpanded) {
        SettingsDialog(
            state = state,
            sendIntent = sendIntent,
            onDismiss = { settingsExpanded = false }
        )
    }
}

@Composable
private fun EmptyPlaceholder() {
    val emptyInfiniteTransition = rememberInfiniteTransition()
    val emptyGlowScale by emptyInfiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(emptyGlowScale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            )
                        )
                )
                Icon(
                    Icons.Rounded.SettingsVoice,
                    null,
                    Modifier.size(56.dp),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.tts_placeholder_empty),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun TtsHeader(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    onSettingsClick: () -> Unit
) {
    val wsColor = when {
        state.wsConnected -> Color(0xFF4CAF50)
        state.wsConnecting -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    val wsColorAnim by animateColorAsState(wsColor, tween(300))
    val httpColor = when {
        state.httpRequesting -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    val httpColorAnim by animateColorAsState(httpColor, tween(300))

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )


    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = glowAlpha),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = glowAlpha * 0.5f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = glowAlpha * 0.3f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            state.requestMode == RequestMode.WebSocket,
            {
                sendIntent(TtsIntent.SetRequestMode(RequestMode.WebSocket))
                if (!state.wsConnected && !state.wsConnecting) {
                    sendIntent(TtsIntent.ConnectWs)
                }
            },
            label = { Text("WS", fontSize = 11.sp) },
            modifier = Modifier.height(26.dp),
            enabled = !state.httpRequesting,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(wsColorAnim)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            if (state.wsConnected) "已连接"
            else if (state.wsConnecting) "连接中"
            else "未连接",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(Modifier.width(12.dp))

        FilterChip(
            state.requestMode == RequestMode.HTTP,
            { sendIntent(TtsIntent.SetRequestMode(RequestMode.HTTP)) },
            label = { Text("HTTP", fontSize = 11.sp) },
            modifier = Modifier.height(26.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(httpColorAnim)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            if (state.httpRequesting) "请求中" else "空闲",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickableWithDebounce {
                onSettingsClick()
            }) {
            Icon(
                Icons.Rounded.Tune,
                null,
                Modifier.size(14.dp),
                MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(2.dp))
            Text(
                stringResource(R.string.tts_settings_title),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Tune,
                            null,
                            Modifier.size(18.dp),
                            MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.tts_settings_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            stringResource(R.string.tts_close),
                            Modifier.size(18.dp),
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "服务器地址",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
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
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    )
                    OutlinedTextField(
                        state.port,
                        { sendIntent(TtsIntent.UpdatePort(it)) },
                        Modifier.width(80.dp),
                        placeholder = { Text("port", fontSize = 12.sp) },
                        textStyle = MaterialTheme.typography.bodySmall,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(Modifier.height(12.dp))

                SpeakerAndLangSection(state, sendIntent)

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(Modifier.height(12.dp))

                PlaybackControlsContent(state, sendIntent)

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SpeakerAndLangSection(state: TtsState, sendIntent: (TtsIntent) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeakerDropdown(state.speaker, sendIntent, Modifier.weight(1f))
            LangDropdown(state.language, sendIntent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            state.instruct,
            { sendIntent(TtsIntent.UpdateInstruct(it)) },
            Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    stringResource(R.string.tts_instruct_hint),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun PlaybackControlsContent(state: TtsState, sendIntent: (TtsIntent) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Speed,
                null,
                Modifier.size(14.dp),
                MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${String.format("%.1f", state.playbackSpeed)}x",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(52.dp)
            )
            Slider(
                state.playbackSpeed,
                { sendIntent(TtsIntent.SetPlaybackSpeed(it)) },
                valueRange = 0.5f..3.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.VolumeUp,
                null,
                Modifier.size(14.dp),
                MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${(state.playbackVolume * 100).toInt()}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(52.dp)
            )
            Slider(
                state.playbackVolume,
                { sendIntent(TtsIntent.SetPlaybackVolume(it)) },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
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
                    "${stringResource(speaker.descriptionRes)} · ${stringResource(speaker.nativeLanguageRes)}",
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
                            "${stringResource(spk.descriptionRes)} · ${stringResource(spk.nativeLanguageRes)}",
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
private fun SwipeableDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwipedOpen by remember { mutableStateOf(false) }
    val deleteButtonWidth = 100.dp
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidth.toPx() }
    val maxOffset = -deleteButtonWidthPx
    val deleteColor = Color(0xFFE53935)

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 250),
        label = "swipeOffset"
    )

    Box {
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(deleteButtonWidth)
                .fillMaxHeight()
                .clickableWithDebounce { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Delete,
                null,
                Modifier.size(28.dp),
                deleteColor
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < maxOffset / 2) {
                                offsetX = maxOffset
                                isSwipedOpen = true
                            } else {
                                offsetX = 0f
                                isSwipedOpen = false
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (offsetX + dragAmount).coerceIn(maxOffset, 0f)
                            offsetX = newOffset
                            isSwipedOpen = newOffset == maxOffset
                        }
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
private fun MessageCard(
    group: TtsMessageGroup,
    isPlaying: Boolean,
    onPlayGroup: () -> Unit,
    onPlayChunk: (Int) -> Unit,
    onStop: () -> Unit
) {
    var textExpanded by remember { mutableStateOf(false) }
    var textDidOverflow by remember { mutableStateOf(false) }
    var chunksExpanded by remember { mutableStateOf(false) }

    val rawText = group.text
    val displayText = if (textExpanded) {
        if (rawText.length > 500) rawText.take(500) + "…" else rawText
    } else {
        rawText
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modeColor =
                        if (group.mode == RequestMode.WebSocket) Color(0xFF4CAF50) else Color(
                            0xFFFFC107
                        )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(modeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(group.mode.name, fontSize = 9.sp, color = modeColor)
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
                        "request_id: " + group.requestId,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    displayText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    maxLines = if (textExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!textExpanded) {
                            textDidOverflow = layoutResult.hasVisualOverflow
                        }
                    }
                )
                if (textDidOverflow || textExpanded) {
                    Text(
                        if (textExpanded) "收起" else "展开",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickableWithDebounce { textExpanded = !textExpanded }
                            .padding(vertical = 2.dp)
                    )
                }
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
                if (group.chunks.size > 1) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                            .clickableWithDebounce { chunksExpanded = !chunksExpanded }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Headphones,
                            null,
                            Modifier.size(14.dp),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when {
                                !group.isCompleted && group.totalChunks > 0 -> "分段音频 (${group.chunks.size}/${group.totalChunks})"
                                else -> "分段音频 (${group.chunks.size}段)"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            null,
                            Modifier.size(18.dp),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                    if (chunksExpanded) {
                        Spacer(Modifier.height(4.dp))
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

@Composable
private fun InputSection(
    state: TtsState,
    sendIntent: (TtsIntent) -> Unit,
    onPickTextFile: () -> Unit,
    inputMode: Boolean,
    onToggleInputMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
