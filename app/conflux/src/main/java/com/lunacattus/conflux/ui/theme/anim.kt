package com.lunacattus.conflux.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

val slideInFromRight = slideIn(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    initialOffset = { fullSize ->
        IntOffset(fullSize.width, 0)
    }
)

val slideOutFromRight = slideOut(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    targetOffset = { fullSize ->
        IntOffset(fullSize.width, 0)
    }
)

val slideInFromLeft = slideIn(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    initialOffset = { fullSize ->
        IntOffset(-fullSize.width, 0)
    }
)

val slideOutFromLeft = slideOut(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    targetOffset = { fullSize ->
        IntOffset(-fullSize.width, 0)
    }
)

val slideInFromBottom = slideIn(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    initialOffset = { fullSize ->
        IntOffset(0, fullSize.height)
    }
)

val slideOutFromBottom = slideOut(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    targetOffset = { fullSize ->
        IntOffset(0, fullSize.height)
    }
)

val slideInFromTop = slideIn(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    initialOffset = { fullSize ->
        IntOffset(0, -fullSize.height)
    }
)

val slideOutFromTop = slideOut(
    animationSpec = tween(
        durationMillis = 400,
        easing = CubicBezierEasing(0.42f, 0.00f, 0.58f, 1.00f)
    ),
    targetOffset = { fullSize ->
        IntOffset(0, -fullSize.height)
    }
)

val stayStillIn = slideIn(
    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    initialOffset = { _ ->
        IntOffset(0, 0)
    }
)

val stayStillOut = slideOut(
    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    targetOffset = { _ ->
        IntOffset(0, 0)
    }
)

val immediatelyIn = fadeIn(tween(0))

val immediatelyOut = fadeOut(tween(0))

val enterAndExit = scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(durationMillis = 700)
) + fadeIn(animationSpec = tween(durationMillis = 700)) togetherWith
        stayStillOut

val popEnterAndExit = stayStillIn togetherWith
        scaleOut(
            targetScale = 0.8f, // 当前页向中心缩小消失
            animationSpec = tween(durationMillis = 700)
        ) + fadeOut(animationSpec = tween(durationMillis = 700))