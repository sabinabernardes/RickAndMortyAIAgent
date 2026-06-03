package com.bina.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bina.designsystem.R
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.designsystem.tokens.ElevationTokens
import com.bina.designsystem.tokens.SpacingTokens

@Composable
fun SearchToolbar(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isPreview = LocalInspectionMode.current

    if (!isPreview) {
        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = ElevationTokens.Level2
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    horizontal = SpacingTokens.spacing16,
                    vertical = SpacingTokens.spacing16
                )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(SpacingTokens.spacing8))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = {
                    Text(text = stringResource(R.string.search_toolbar_label))
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_toolbar_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SpacingTokens.spacing8),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchToolbarPreview() {
    var search by remember { mutableStateOf("") }
    RickAndMortyTheme {
        SearchToolbar(
            title = "Personagens",
            query = search,
            onQueryChange = { search = it }
        )
    }
}