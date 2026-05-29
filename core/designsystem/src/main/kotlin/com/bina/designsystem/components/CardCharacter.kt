package com.bina.designsystem.components

import com.bina.designsystem.tokens.SpacingTokens
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.designsystem.tokens.TypographyTokens.DefaultTypography

@Composable
fun CardCharacter(
    painter: Painter,
    name: String,
    status: String,
    lastLocation: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(SpacingTokens.spacing8)
            .width(160.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box {
                Image(
                    painter = painter,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(160.dp),
                    contentScale = ContentScale.Crop
                )
                StatusBadge(
                    status = status,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.spacing8)
                )
            }

            Column(modifier = Modifier.padding(SpacingTokens.spacing16)) {
                Text(
                    text = name,
                    style = DefaultTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(
                    SpacingTokens.spacing4
                ))
                Text(
                    text = "Last Location",
                    style = DefaultTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = lastLocation,
                    style = DefaultTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardCharacterPreview() {
    RickAndMortyTheme {
        CardCharacter(
            painter = object : Painter() {
                override val intrinsicSize = Size.Unspecified
                override fun DrawScope.onDraw() {}
            },
            name = "Rick Sanchez",
            status = "Alive",
            lastLocation = "Earth (C-137)",
        )
    }
}

