package com.lunacattus.app.player.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset

val slideInFromRight = slideIn(
    animationSpec = tween(500),
    initialOffset = { fullSize ->
        IntOffset(fullSize.width, 0)
    }
)

val slideOutFromRight = slideOut(
    animationSpec = tween(500),
    targetOffset = { fullSize ->
        IntOffset(fullSize.width, 0)
    }
)

val slideInFromLeft = slideIn(
    animationSpec = tween(500),
    initialOffset = { fullSize ->
        IntOffset(-fullSize.width, 0)
    }
)

val slideOutFromLeft = slideOut(
    animationSpec = tween(500),
    targetOffset = { fullSize ->
        IntOffset(-fullSize.width, 0)
    }
)

val stayStillIn = slideIn(
    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    initialOffset = { fullSize ->
        IntOffset(0, 0)
    }
)

val stayStillOut = slideOut(
    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    targetOffset = { fullSize ->
        IntOffset(0, 0)
    }
)