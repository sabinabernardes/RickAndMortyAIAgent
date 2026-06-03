package com.bina.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bina.designsystem.theme.RickAndMortyTheme
import androidx.compose.ui.res.stringResource
import com.bina.designsystem.R
import com.bina.designsystem.tokens.TypographyTokens.DefaultTypography

@Composable
fun DialogError(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val isInPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isInPreview) "Erro" else stringResource(id = R.string.dialog_error_title),
                style = DefaultTypography.headlineSmall,
                color = DefaultTypography.headlineSmall.color,
            )
        },
        text = {
            Text(
                text = message,
                style = DefaultTypography.bodySmall
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(
                    text = if (isInPreview) "Tentar novamente" else stringResource(id = R.string.dialog_error_retry)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isInPreview) "Fechar" else stringResource(id = R.string.dialog_error_close)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorDialogPreview() {
    RickAndMortyTheme {
        DialogError(
            message = "Não foi possível carregar os personagens. Verifique sua conexão.",
            onDismiss = {},
            onRetry = {}
        )
    }
}
