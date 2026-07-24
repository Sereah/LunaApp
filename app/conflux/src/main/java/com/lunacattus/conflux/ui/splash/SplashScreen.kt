package com.lunacattus.conflux.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.conflux.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Theme Colors ───
private val CoralPink = Color(0xFFFF6B6B)
private val MintTeal = Color(0xFF26C6B6)
private val LightCoral = Color(0xFFFF8A80)
private val BackgroundWarm = Color(0xFFFFFBFA)

// ─── DynaPuff playful font ───
private val DynaPuffBold = FontFamily(Font(R.font.dynapuff_bold, FontWeight.Bold))

// ─── Pink-dominant gradients per letter (vertical, each subtly different) ───
private val LetterGradients = listOf(
    Brush.verticalGradient(listOf(CoralPink, LightCoral)),
    Brush.verticalGradient(listOf(CoralPink, MintTeal.copy(alpha = 0.55f))),
    Brush.verticalGradient(listOf(LightCoral, CoralPink)),
    Brush.verticalGradient(listOf(CoralPink, Color(0xFFFFAB91))),
    Brush.verticalGradient(listOf(LightCoral, MintTeal.copy(alpha = 0.55f))),
    Brush.verticalGradient(listOf(Color(0xFFFFAB91), CoralPink)),
    Brush.verticalGradient(listOf(CoralPink, LightCoral)),
)

private const val WORD = "conflux"

// Per-letter entry angles & offsets for spin-drop cascade
private val EntryRotations = listOf(-35f, -20f, -30f, -15f, -25f, -18f, -32f)
private val EntryOffsets = listOf(60f, 45f, 70f, 40f, 55f, 48f, 65f)

// ═══════════════════════════════════════════════════════════════
// Main composable
// ═══════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGlow by remember { mutableStateOf(false) }
    var showLogo by remember { mutableStateOf(false) }

    val letterTransforms = remember {
        WORD.indices.map { i ->
            LetterTransform(
                offsetY = androidx.compose.animation.core.Animatable(EntryOffsets[i]),
                rotation = androidx.compose.animation.core.Animatable(EntryRotations[i]),
                alpha = androidx.compose.animation.core.Animatable(0f),
            )
        }
    }

    // Glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowScale",
    )

    LaunchedEffect(Unit) {
        // Phase 1 — Glow + Logo (0–800ms)
        showGlow = true
        delay(200)
        showLogo = true
        delay(600)

        // Phase 2 — Letter wave cascade (800–1800ms)
        WORD.indices.forEach { i ->
            launch {
                letterTransforms[i].offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
            launch {
                letterTransforms[i].rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                )
            }
            launch {
                letterTransforms[i].alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                )
            }
            delay(70)
        }
        delay(800)

        // Phase 3 — Hold → finish
        delay(600)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWarm),
    ) {
        // ── Glow ──
        if (showGlow) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-80).dp)
                    .size(300.dp)
                    .scale(glowPulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CoralPink.copy(alpha = 0.22f),
                                MintTeal.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        // ── Logo ──
        AnimatedVisibility(
            visible = showLogo,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp),
            enter = fadeIn(animationSpec = tween(500)) +
                scaleIn(
                    initialScale = 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(260.dp),
            )
        }

        // ── conflux — gradient letters with custom font ──
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WORD.forEachIndexed { index, char ->
                val tf = letterTransforms[index]
                Text(
                    text = char.toString(),
                    style = TextStyle(
                        brush = LetterGradients[index],
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DynaPuffBold,
                    ),
                    modifier = Modifier
                        .offset(y = tf.offsetY.value.dp)
                        .rotate(tf.rotation.value)
                        .graphicsLayer { alpha = tf.alpha.value },
                )
            }
        }
    }
}

// ─── Per-letter animation state ───
private class LetterTransform(
    val offsetY: androidx.compose.animation.core.Animatable<Float, *>,
    val rotation: androidx.compose.animation.core.Animatable<Float, *>,
    val alpha: androidx.compose.animation.core.Animatable<Float, *>,
)
