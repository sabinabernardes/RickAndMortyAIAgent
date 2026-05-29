package com.bina.character_details.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bina.character_details.presentation.model.CharacterDetailsUiModel
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import com.bina.character_details.presentation.viewmodel.CharacterDetailsViewModel
import com.bina.designsystem.components.Toolbar
import com.bina.designsystem.tokens.SpacingTokens
import com.bina.designsystem.tokens.TypographyTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun CharacterDetailsScreen(
    characterId: Int,
    onBackClick: () -> Unit,
    viewModel: CharacterDetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(characterId) {
        viewModel.getCharacterDetails(characterId)
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = "Detalhes",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is CharacterDetailsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is CharacterDetailsUiState.Success -> {
                    CharacterDetailsContent(character = state.character)
                }
                is CharacterDetailsUiState.Error -> {
                    Text(
                        text = state.message ?: "Erro desconhecido",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterDetailsContent(character: CharacterDetailsUiModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = character.imageUrl,
            contentDescription = character.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(SpacingTokens.spacing16)
        ) {
            Text(
                text = character.name,
                style = TypographyTokens.DefaultTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(SpacingTokens.spacing8))

            DetailItem(label = "Status", value = character.status)
            DetailItem(label = "Espécie", value = character.species)
            DetailItem(label = "Gênero", value = character.gender)
            DetailItem(label = "Origem", value = character.origin)
            DetailItem(label = "Localização", value = character.location)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = SpacingTokens.spacing4)) {
        Text(
            text = label,
            style = TypographyTokens.DefaultTypography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = TypographyTokens.DefaultTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
