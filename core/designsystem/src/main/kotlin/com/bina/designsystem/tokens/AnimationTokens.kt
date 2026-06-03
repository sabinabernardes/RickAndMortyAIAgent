package com.bina.designsystem.tokens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing

object AnimationTokens {
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 600
    const val DurationEnter = 300

    const val StaggerDelay = 30
    const val StaggerMaxDelay = 150

    val DefaultEasing: Easing = EaseInOut
    val EasingEnter: Easing = EaseOut
}
