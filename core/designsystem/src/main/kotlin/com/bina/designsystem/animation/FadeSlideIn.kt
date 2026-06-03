package com.bina.designsystem.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.bina.designsystem.tokens.AnimationTokens
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive motion: spring slide + tween fade.
 * Spring feels more natural than tween for spatial movement.
 * [index] drives stagger delay in list contexts; default 0 = no stagger.
 */
@Composable
fun Modifier.fadeSlideIn(index: Int = 0): Modifier {
    val delayMs = (index * AnimationTokens.StaggerDelay)
        .coerceAtMost(AnimationTokens.StaggerMaxDelay)
        .toLong()

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMs > 0) delay(delayMs)
        visible = true
    }

    // Alpha uses tween — springs don't feel right for opacity
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = AnimationTokens.DurationEnter),
        label = "fadeAlpha"
    )

    // Slide uses spring — M3 Expressive motion principle
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 48f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "slideY"
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}
