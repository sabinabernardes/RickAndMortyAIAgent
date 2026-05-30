package com.bina.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.bina.designsystem.animation.shimmerEffect
import com.bina.designsystem.tokens.SpacingTokens

private val CardSize = 160.dp

@Composable
fun CardCharacterSkeleton(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.surface
    val highlight = lerp(base, MaterialTheme.colorScheme.primary, 0.22f)

    Card(
        modifier = modifier
            .padding(SpacingTokens.spacing8)
            .width(CardSize),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(CardSize)
                    .shimmerEffect(baseColor = base, highlightColor = highlight)
            )

            Column(modifier = Modifier.padding(SpacingTokens.spacing16)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect(baseColor = base, highlightColor = highlight)
                )
                Spacer(modifier = Modifier.height(SpacingTokens.spacing8))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect(baseColor = base, highlightColor = highlight)
                )
                Spacer(modifier = Modifier.height(SpacingTokens.spacing4))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect(baseColor = base, highlightColor = highlight)
                )
                Spacer(modifier = Modifier.height(SpacingTokens.spacing4))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect(baseColor = base, highlightColor = highlight)
                )
            }
        }
    }
}