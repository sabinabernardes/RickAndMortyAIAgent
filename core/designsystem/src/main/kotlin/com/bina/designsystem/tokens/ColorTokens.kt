package com.bina.designsystem.tokens

import androidx.compose.ui.graphics.Color

object ColorTokens {
    val Primary = Color(0xFF1B1B1F)
    val OnPrimary = Color(0xFFFFFFFF)
    val Secondary = Color(0xFF03DAC6)
    val OnSecondary = Color(0xFF000000)
    val Background = Color(0xFFF6F6F6)
    val OnBackground = Color(0xFF1B1B1F)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1B1B1F)
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)

    // StatusAlive escurecido para contraste AA (branco sobre #1E8449 ≈ 4.6:1)
    val StatusAlive = Color(0xFF1E8449)

    // StatusDead escurecido para contraste AA (branco sobre #A93226 ≈ 6.1:1)
    val StatusDead = Color(0xFFA93226)

    // StatusUnknown escurecido para contraste AA (branco sobre #616161 ≈ 4.6:1)
    val StatusUnknown = Color(0xFF616161)

    val OnStatusAlive = Color(0xFFFFFFFF)
    val OnStatusDead = Color(0xFFFFFFFF)
    val OnStatusUnknown = Color(0xFFFFFFFF)
}
