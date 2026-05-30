package com.bina.home.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.bina.designsystem.components.CardCharacter
import com.bina.designsystem.components.DialogError
import com.bina.designsystem.components.SearchToolbar
import com.bina.designsystem.tokens.ColorTokens
import com.bina.designsystem.tokens.SpacingTokens
import com.bina.home.presentation.model.CharacterUiModel
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onCharacterClick: (Int) -> Unit,
    onChatClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(query) {
        viewModel.getCharacters(query)
    }

    Scaffold(
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onChatClick,
                containerColor = ColorTokens.Secondary,
                contentColor = ColorTokens.OnSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Chat com Rick AI",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchToolbar(
                title = "Personagens",
                query = query,
                onQueryChange = { query = it }
            )

            HomeContent(
                uiState = uiState,
                onCharacterClick = onCharacterClick,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: CharactersUiState,
    onCharacterClick: (Int) -> Unit,
    viewModel: HomeViewModel
) {
    when (uiState) {
        is CharactersUiState.Loading -> LoadingContent()
        is CharactersUiState.Success -> {
            val characters = uiState.data.collectAsLazyPagingItems()
            CharacterList(
                characters = characters,
                onCharacterClick = onCharacterClick
            )
        }
        is CharactersUiState.Error -> {
            DialogError(
                message = uiState.message ?: "Erro ao carregar dados",
                onDismiss = { viewModel.clearError() },
                onRetry = { viewModel.onRetry() }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun CharacterList(
    characters: LazyPagingItems<CharacterUiModel>,
    onCharacterClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.spacing8)
    ) {
        items(characters.itemCount) { index ->
            characters[index]?.let { character ->
                CardCharacter(
                    painter = rememberAsyncImagePainter(model = character.imageUrl),
                    name = character.name.orEmpty(),
                    status = character.status.orEmpty(),
                    lastLocation = character.location.orEmpty(),
                    onClick = { onCharacterClick(character.id) }
                )
            }
        }

        if (characters.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.spacing16),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(SpacingTokens.spacing32),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
