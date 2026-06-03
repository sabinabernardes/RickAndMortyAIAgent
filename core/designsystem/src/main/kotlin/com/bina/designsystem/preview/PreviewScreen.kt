package com.bina.designsystem.preview

import com.bina.designsystem.tokens.SpacingTokens
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.bina.designsystem.theme.RickAndMortyTheme

@Preview(
    name = "Preview- Claro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewTokensLight() {
    RickAndMortyTheme(useDarkTheme = false) {
        TokensContent()
    }
}

@Preview(
    name = "Preview- Escuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF121212
)
@Composable
fun PreviewTokensDark() {
    RickAndMortyTheme(useDarkTheme = true) {
        TokensContent()
    }
}

@Composable
private fun TokensContent() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(SpacingTokens.spacing16)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.spacing16),
            modifier = Modifier.fillMaxSize()
        ) {
            TextTokenSample("Display Large", MaterialTheme.typography.displayLarge)
            TextTokenSample("Headline Medium", MaterialTheme.typography.headlineMedium)
            TextTokenSample("Title Small", MaterialTheme.typography.titleSmall)
            TextTokenSample("Body Medium", MaterialTheme.typography.bodyMedium)
            TextTokenSample("Label Small", MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(SpacingTokens.spacing24))

            SpacingTokenSample("spacing2", SpacingTokens.spacing2)
            SpacingTokenSample("spacing4", SpacingTokens.spacing4)
            SpacingTokenSample("spacing8", SpacingTokens.spacing8)
            SpacingTokenSample("spacing16", SpacingTokens.spacing16)
            SpacingTokenSample("spacing24", SpacingTokens.spacing24)
            SpacingTokenSample("spacing32", SpacingTokens.spacing32)
            SpacingTokenSample("spacing64", SpacingTokens.spacing64)
            SpacingTokenSample("spacing128", SpacingTokens.spacing128)
        }
    }
}

@Composable
private fun TextTokenSample(label: String, style: TextStyle) {
    Column {
        Text(text = label, style = style)
    }
}

@Composable
private fun SpacingTokenSample(label: String, size: Dp) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Spacer(
            modifier = Modifier
                .height(size)
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.3f))
        )
    }
}
