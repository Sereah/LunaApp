package com.lunacattus.app.presentation.compose.routes.enter

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lunacattus.app.presentation.compose.R
import com.lunacattus.app.presentation.compose.common.components.TypewriteText
import com.lunacattus.app.presentation.compose.common.components.quoteTextStyle
import com.lunacattus.app.presentation.compose.common.extensions.debouncedOnClick
import com.lunacattus.app.presentation.compose.theme.AppTheme

@Composable
fun EnterScreen(
    modifier: Modifier = Modifier,
    navToMain: () -> Unit,
) {
    val textDuration = stringResource(R.string.enter_welcome_text).length * 50
    val authorDuration = stringResource(R.string.author).length * 50

    var isVisibleNextButton by remember { mutableStateOf(false) }
    var isFirstEnter by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
    ) {
        TypewriteText(
            text = stringResource(R.string.enter_welcome_text),
            skipToEnd = true,
            style = quoteTextStyle(),
            spec = tween(
                durationMillis = textDuration,
                easing = LinearEasing
            ),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
        TypewriteText(
            text = "-- ${stringResource(R.string.author)}",
            skipToEnd = true,
            style = quoteTextStyle(),
            spec = tween(
                durationMillis = authorDuration,
                delayMillis = textDuration + 200,
                easing = LinearEasing
            ),
            modifier = Modifier
                .padding(end = 20.dp)
                .align(Alignment.End)
        ) {
            isVisibleNextButton = true
            isFirstEnter = false
        }

        if (isVisibleNextButton) {
            Button(
                onClick = debouncedOnClick {
                    navToMain()
                },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = AppTheme.colors.mainText
                ),
                modifier = Modifier
                    .padding(top = 50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Next", color = AppTheme.colors.background)
                    Icon(
                        imageVector = Icons.AutoMirrored.Sharp.ArrowForward,
                        contentDescription = null,
                        tint = AppTheme.colors.background
                    )
                }
            }
        }
    }
}