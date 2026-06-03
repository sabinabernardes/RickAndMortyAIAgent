package com.bina.home.presentation.view

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.bina.designsystem.animation.fadeSlideIn
import com.bina.designsystem.components.CardCharacter
import com.bina.designsystem.components.CardCharacterSkeleton
import com.bina.designsystem.components.DialogError
import com.bina.designsystem.components.SearchToolbar
import com.bina.designsystem.tokens.ElevationTokens
import com.bina.designsystem.tokens.SpacingTokens
import com.bina.home.presentation.model.CharacterUiModel
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    initialQuery: String = "",
    viewModel: HomeViewModel = koinViewModel(),
    onCharacterClick: (Int) -> Unit,
    onChatClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf(initialQuery) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(query) {
        viewModel.getCharacters(query)
    }

    Scaffold(
        topBar = {
            SearchToolbar(
                title = "Personagens",
                query = query,
                onQueryChange = { query = it }
            )
        },
        bottomBar = { ChatPortalBar(onClick = onChatClick) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            onCharacterClick = onCharacterClick,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun ChatPortalBar(onClick: () -> Unit) {
    val glowTransition = rememberInfiniteTransition(label = "portalGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = ElevationTokens.Level2
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = SpacingTokens.spacing16, vertical = SpacingTokens.spacing16)
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(SpacingTokens.spacing8))
                    Text(
                        text = "Chat com Rick AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: CharactersUiState,
    onCharacterClick: (Int) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is CharactersUiState.Loading -> LoadingContent(modifier = modifier)
        is CharactersUiState.Success -> {
            val characters = uiState.data.collectAsLazyPagingItems()

            val previousAppendState = remember { mutableStateOf<LoadState>(LoadState.NotLoading(false)) }
            LaunchedEffect(characters.loadState.append) {
                val current = characters.loadState.append
                if (previousAppendState.value is LoadState.Loading
                    && current is LoadState.NotLoading
                    && !current.endOfPaginationReached
                ) {
                    viewModel.onPageLoaded()
                }
                previousAppendState.value = current
            }

            CharacterList(
                characters = characters,
                onCharacterClick = { id ->
                    viewModel.onCharacterClicked(id)
                    onCharacterClick(id)
                },
                modifier = modifier
            )
        }
        is CharactersUiState.Error -> {
            Box(modifier = modifier) {
                DialogError(
                    message = uiState.message ?: "Erro ao carregar dados",
                    onDismiss = { viewModel.clearError() },
                    onRetry = { viewModel.onRetry() }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.spacing8)
    ) {
        items(6) {
            CardCharacterSkeleton()
        }
    }
}

@Composable
fun CharacterList(
    characters: LazyPagingItems<CharacterUiModel>,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.spacing8)
    ) {
        items(characters.itemCount) { index ->
            characters[index]?.let { character ->
                CardCharacter(
                    painter = rememberAsyncImagePainter(model = character.imageUrl),
                    name = character.name,
                    status = character.status,
                    species = character.species,
                    lastLocation = character.location,
                    onClick = { onCharacterClick(character.id) },
                    modifier = Modifier.fadeSlideIn(index = index.coerceAtMost(5))
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