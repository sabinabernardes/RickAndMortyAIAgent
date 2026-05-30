package com.bina.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.designsystem.tokens.SpacingTokens
import com.bina.designsystem.tokens.TypographyTokens.DefaultTypography

@Composable
fun CardCharacter(
    painter: Painter,
    name: String,
    status: String,
    species: String,
    lastLocation: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(SpacingTokens.spacing8)
            .width(160.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(SpacingTokens.spacing4))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = species,
                        style = DefaultTypography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(SpacingTokens.spacing4))
                Text(
                    text = "Localização",
                    style = DefaultTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Text(
                    text = lastLocation,
                    style = DefaultTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
            species = "Human",
            lastLocation = "Earth (C-137)",
        )
    }
}