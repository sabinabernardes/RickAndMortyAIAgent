package com.bina.designsystem.animation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.shimmerEffect(
    baseColor: Color,
    highlightColor: Color
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    return drawBehind {
        val shimW = size.width * 1.5f
        val x = -shimW + progress * (size.width + shimW * 2)
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to baseColor,
                    0.4f to highlightColor,
                    0.6f to highlightColor,
                    1.0f to baseColor
                ),
                start = Offset(x, 0f),
                end = Offset(x + shimW, size.height)
            )
        )
    }
}
