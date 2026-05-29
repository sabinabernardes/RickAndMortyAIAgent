package com.bina.playground.ui.theme.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.designsystem.tokens.ColorTokens
import com.bina.designsystem.tokens.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemPlaygroundScreen() {
    var isDarkTheme by rememberSaveable { mutableStateOf(false) }
    var selectedComponent by rememberSaveable { mutableStateOf("Toolbar") }

    val components: List<Pair<String, @Composable () -> Unit>> = listOf(
        "Character Card" to { CardCharacterContent() },
        "Dialog" to { DialogPreviewContent() },
        "Search Toolbar" to {
            SearchToolbarPreviewContent(
                search = "Rick and Morty",
                onSearchChange = {}
            )
        },
    )

    RickAndMortyTheme(useDarkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Design Playground") },
                    actions = {
                        IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar tema"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(SpacingTokens.spacing16)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.spacing16)
            ) {
                Text(
                    text = "Selecione um componente para visualizar:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = SpacingTokens.spacing16)
                )
                components.forEach { (label, _) ->
                    Button(
                        onClick = { selectedComponent = label },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedComponent == label)
                                ColorTokens.Primary else ColorTokens.Surface
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (selectedComponent == label)
                                ColorTokens.OnPrimary else ColorTokens.OnBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(SpacingTokens.spacing16))

                components.firstOrNull { it.first == selectedComponent }?.second?.invoke()
            }
        }
    }
}

@Preview(showBackground = true, name = "🎮 Playground Preview")
@Composable
fun DesignSystemPlaygroundScreenPreview() {
    RickAndMortyTheme {
        DesignSystemPlaygroundScreen()
    }
}
