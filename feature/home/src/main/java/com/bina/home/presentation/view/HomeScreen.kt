package com.bina.home.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.bina.designsystem.components.SearchToolbar
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import coil.compose.rememberImagePainter
import com.bina.designsystem.components.CardCharacter
import com.bina.designsystem.components.DialogError
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.model.CharacterUiModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navController: NavHostController
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(query) {
        viewModel.getCharacters(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchToolbar(
            title = "Personagens",
            query = query,
            onQueryChange = { query = it }
        )
        HomeContent(
            uiState = uiState,
            navController = navController,
            viewModel = viewModel
        )
    }
}

@Composable
private fun HomeContent(
    uiState: CharactersUiState,
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    when (uiState) {
        is CharactersUiState.Loading -> LoadingContent()
        is CharactersUiState.Success -> {
            val characters = uiState.data.collectAsLazyPagingItems()
            CharacterList(
                characters = characters,
                onRetry = { characters.retry() },
                navController = navController
            )
        }
        is CharactersUiState.Error -> {
            DialogError(
                message = uiState.message ?: "Erro desconhecido",
                onDismiss = { viewModel.clearError() },
                onRetry = { viewModel.onRetry() }
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CharacterList(
    characters: LazyPagingItems<CharacterUiModel>,
    onRetry: () -> Unit,
    navController: NavHostController
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        items(characters.itemCount) { index ->
            characters[index]?.let { character ->
                CardCharacter(
                    painter = rememberImagePainter(data = character.imageUrl ?: ""),
                    name = character.name ?: "Desconhecido",
                    status = character.status ?: "Desconhecido",
                    lastLocation =  "Desconhecido",
                    onClick = {
                        navController.navigate(
                            "details/${character.id}"
                        )
                    }
                )
            }
        }
        when (val appendState = characters.loadState.append) {
            is LoadState.Loading -> {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item(span = { GridItemSpan(2) }) {
                    DialogError (
                        message = "Erro ao carregar mais personagens",
                        onDismiss = {},
                        onRetry = { onRetry() }
                    )
                }
            }
            else -> Unit
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = NavHostController(LocalContext.current) // Mock NavController for preview
    )
}