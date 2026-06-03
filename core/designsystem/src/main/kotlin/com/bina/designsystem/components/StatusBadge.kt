package com.bina.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bina.designsystem.tokens.ColorTokens
import com.bina.designsystem.tokens.ElevationTokens
import com.bina.designsystem.tokens.SpacingTokens

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "alive" -> ColorTokens.StatusAlive to ColorTokens.OnStatusAlive
        "dead" -> ColorTokens.StatusDead to ColorTokens.OnStatusDead
        else -> ColorTokens.StatusUnknown to ColorTokens.OnStatusUnknown
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        tonalElevation = ElevationTokens.Level1
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = SpacingTokens.spacing8, vertical = SpacingTokens.spacing4),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBadgePreview() {
    StatusBadge(status = "Alive")
}