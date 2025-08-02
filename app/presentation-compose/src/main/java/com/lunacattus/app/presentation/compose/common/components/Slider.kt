package com.lunacattus.app.presentation.compose.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSlider(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    playFraction: Float,
    onPlayFractionChange: (Float) -> Unit,
    onPlayFractionChangeFinish: () -> Unit,
    bufferFraction: Float = playFraction,
    onBufferFractionChange: (Float) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val sliderColors = SliderDefaults.colors().copy(
        thumbColor = Color.White,
        activeTrackColor = Color.Transparent,
        inactiveTrackColor = Color.Transparent,
        disabledActiveTrackColor = Color.Transparent,
        disabledInactiveTrackColor = Color.Transparent
    )
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(sliderColors.thumbColor.copy(alpha = 0.2f))
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = bufferFraction)
                .height(1.dp)
                .background(sliderColors.thumbColor.copy(alpha = 0.4f))
                .align(Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = playFraction)
                .height(1.dp)
                .background(sliderColors.thumbColor)
                .align(Alignment.CenterStart)
        )
        Slider(
            value = playFraction,
            enabled = enable,
            onValueChange = onPlayFractionChange,
            onValueChangeFinished = onPlayFractionChangeFinish,
            colors = sliderColors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(8.dp, 8.dp),
                    colors = sliderColors,
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    colors = sliderColors,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp,
                    modifier = Modifier
                        .height(1.dp)
                        .align(Alignment.Center)
                )
            },
        )
    }
}

@Preview
@Composable
fun VideoSliderSample() {
    var position by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoSlider(
            playFraction = position,
            onPlayFractionChange = { position = it },
            onPlayFractionChangeFinish = {},
            bufferFraction = position + 0.2f,
            modifier = Modifier
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}