package com.lunacattus.conflux.ui.sections.llm.polish

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import com.lunacattus.ui_design.compose.clickableWithDebounce
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

@Composable
fun PolishRoute(
    viewModel: PolishViewModel,
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
                    sendIntent(PolishIntent.UpdateText(text))
                }
            } catch (_: Exception) {
            }
        }
    }

    PolishScreen(
        state = state,
        sendIntent = sendIntent,
        effects = viewModel.effects,
        onPickTextFile = { textFileLauncher.launch(arrayOf("text/*")) },
        modifier = modifier,
    )
}

@Composable
private fun PolishScreen(
    state: PolishState,
    sendIntent: (PolishIntent) -> Unit,
    effects: kotlinx.coroutines.flow.Flow<PolishEffect>,
    onPickTextFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var settingsExpanded by remember { mutableStateOf(false) }
    var presetsExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            if (effect is PolishEffect.ScrollToBottom && state.results.isNotEmpty()) {
                listState.animateScrollToItem(state.results.size + 2)
            }
        }
    }

    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
            MaterialTheme.colorScheme.background,
        )
    )

    Column(
        modifier = modifier
            .padding(LocalInnerPadding.current)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerGradient)
        ) {
            PolishHeader(
                state = state,
                sendIntent = sendIntent,
                onSettingsClick = { settingsExpanded = true },
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (state.results.isEmpty()) {
                item(key = "empty") {
                    EmptyPlaceholder(isRequesting = state.isRequesting)
                }
            } else {
                item(key = "clear_all") {
                    TextButton(
                        onClick = { sendIntent(PolishIntent.ClearAll) },
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
                            stringResource(R.string.polish_clear_all),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
                items(state.results, key = { it.id }) { item ->
                    Column(
                        Modifier
                            .animateItem()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        SwipeableDeleteItem(
                            onDelete = { sendIntent(PolishIntent.DeleteResult(item.id)) },
                        ) {
                            ResultCard(item = item)
                        }
                    }
                }
            }
            item(key = "input_spacer") { Spacer(Modifier.height(160.dp)) }
        }

        InputSection(
            state = state,
            sendIntent = sendIntent,
            presetsExpanded = presetsExpanded,
            onTogglePresets = { presetsExpanded = !presetsExpanded },
            onDismissPresets = { presetsExpanded = false },
            onDismissKeyboard = { focusManager.clearFocus() },
            onPickTextFile = onPickTextFile,
        )
    }

    if (settingsExpanded) {
        SettingsDialog(
            state = state,
            sendIntent = sendIntent,
            onDismiss = { settingsExpanded = false },
        )
    }
}

@Composable
private fun PolishHeader(
    state: PolishState,
    sendIntent: (PolishIntent) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
        ),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                null,
                Modifier
                    .size(18.dp)
                    .scale(if (state.isRequesting) glowScale else 1f),
                MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.llm_polish_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))

        val statusColor by animateColorAsState(
            if (state.isRequesting) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            animationSpec = tween(400),
        )
        val statusText = if (state.isRequesting)
            stringResource(R.string.polish_requesting)
        else
            stringResource(R.string.polish_idle)

        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            statusText,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        if (state.isRequesting) {
            Spacer(Modifier.width(6.dp))
            CircularProgressIndicator(
                Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.Rounded.Settings,
                null,
                Modifier.size(18.dp),
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyPlaceholder(isRequesting: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(1500)),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isRequesting) {
            CircularProgressIndicator(
                Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.polish_requesting),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Icon(
                Icons.Rounded.AutoAwesome,
                null,
                Modifier
                    .size(56.dp)
                    .scale(glowScale),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.polish_no_results),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun InputSection(
    state: PolishState,
    sendIntent: (PolishIntent) -> Unit,
    presetsExpanded: Boolean,
    onTogglePresets: () -> Unit,
    onDismissPresets: () -> Unit,
    onDismissKeyboard: () -> Unit,
    onPickTextFile: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background.copy(alpha = 0f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.background,
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        AnimatedVisibility(
            visible = presetsExpanded,
            enter = expandVertically(tween(250)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(150)),
        ) {
            PresetsPanel(
                selectedText = state.inputText,
                onSelect = { text ->
                    sendIntent(PolishIntent.UpdateText(text))
                    onDismissPresets()
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                TextButton(onClick = onTogglePresets) {
                    Icon(
                        if (presetsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        null,
                        Modifier.size(16.dp),
                        MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        stringResource(R.string.polish_presets),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                IconButton(
                    onClick = onPickTextFile,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Rounded.OpenInNew,
                        null,
                        Modifier.size(16.dp),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { sendIntent(PolishIntent.UpdateText(it)) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        stringResource(R.string.polish_input_hint),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onDismissKeyboard()
                        sendIntent(PolishIntent.SendPolish)
                    }
                ),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium,
                enabled = !state.isRequesting,
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    onDismissKeyboard()
                    sendIntent(PolishIntent.SendPolish)
                },
                enabled = state.inputText.isNotBlank() && !state.isRequesting,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.inputText.isNotBlank() && !state.isRequesting)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            Color.Transparent
                    ),
            ) {
                Icon(
                    Icons.Rounded.Send,
                    null,
                    Modifier.size(22.dp),
                    if (state.inputText.isNotBlank() && !state.isRequesting)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Thermostat,
                null,
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.polish_temperature, state.temperature),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(Modifier.width(8.dp))
            Slider(
                value = state.temperature.toFloat(),
                onValueChange = { sendIntent(PolishIntent.UpdateTemperature(it.toDouble())) },
                modifier = Modifier.weight(1f),
                valueRange = 0f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                ),
            )
        }
    }
}

@Composable
private fun PresetsPanel(
    selectedText: String,
    onSelect: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            POLISH_PRESETS.forEach { preset ->
                val isSelected = preset == selectedText
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isSelected)
                                Modifier.background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                                        )
                                    )
                                )
                            else Modifier
                        )
                        .clickableWithDebounce { onSelect(preset) }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        preset,
                        fontSize = 13.sp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Rounded.Check,
                            null,
                            Modifier.size(16.dp),
                            MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    item: PolishResultItem,
) {
    val clipboardManager = LocalClipboardManager.current
    var originalExpanded by remember { mutableStateOf(false) }
    var originalDidOverflow by remember { mutableStateOf(false) }
    var polishedExpanded by remember { mutableStateOf(false) }
    var polishedDidOverflow by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        Icons.Rounded.Thermostat,
                        null,
                        Modifier.size(14.dp),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        stringResource(R.string.polish_temperature, item.temperature),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        stringResource(R.string.polish_result_request_id, item.requestId),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(item.polishedText))
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        null,
                        Modifier.size(16.dp),
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
            )

            Column {
                Text(
                    stringResource(R.string.polish_result_original),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.originalText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = if (originalExpanded) 5 else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!originalExpanded) {
                            originalDidOverflow = layoutResult.hasVisualOverflow
                        }
                    },
                )
                if (originalDidOverflow || originalExpanded) {
                    Text(
                        if (originalExpanded) stringResource(R.string.tts_collapse) else stringResource(R.string.tts_expand),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickableWithDebounce { originalExpanded = !originalExpanded }
                            .padding(vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Column {
                Text(
                    stringResource(R.string.polish_result_polished),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.polishedText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (polishedExpanded) 5 else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        if (!polishedExpanded) {
                            polishedDidOverflow = layoutResult.hasVisualOverflow
                        }
                    },
                )
                if (polishedDidOverflow || polishedExpanded) {
                    Text(
                        if (polishedExpanded) stringResource(R.string.tts_collapse) else stringResource(R.string.tts_expand),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickableWithDebounce { polishedExpanded = !polishedExpanded }
                            .padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val deleteButtonWidth = 80.dp
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidth.toPx() }
    val maxOffset = -deleteButtonWidthPx

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "swipeOffset",
    )

    Box {
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .width(deleteButtonWidth)
                .fillMaxHeight()
                .clickableWithDebounce {
                    offsetX = 0f
                    onDelete()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Delete,
                null,
                Modifier.size(26.dp),
                Color(0xFFE53935),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < maxOffset * 0.35f) maxOffset else 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(maxOffset * 1.05f, 0f)
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsDialog(
    state: PolishState,
    sendIntent: (PolishIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    var editHost by remember { mutableStateOf(state.host) }
    var editPort by remember { mutableStateOf(state.port) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Tune,
                        null,
                        Modifier.size(20.dp),
                        MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.polish_settings),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.Close,
                            null,
                            Modifier.size(18.dp),
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = editHost,
                    onValueChange = { editHost = it },
                    label = { Text("Host", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    ),
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editPort,
                    onValueChange = { editPort = it },
                    label = { Text("Port", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    ),
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = {
                        sendIntent(PolishIntent.UpdateHost(editHost))
                        sendIntent(PolishIntent.UpdatePort(editPort))
                        onDismiss()
                    }) {
                        Text(
                            stringResource(R.string.polish_save),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
