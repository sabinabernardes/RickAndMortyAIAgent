package com.bina.character_details.presentation.view

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bina.designsystem.R as DesignSystemR
import coil.compose.AsyncImage
import com.bina.character_details.presentation.model.CharacterDetailsUiModel
import com.bina.character_details.presentation.model.EpisodeUiModel
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import com.bina.character_details.presentation.state.EpisodesState
import com.bina.character_details.presentation.viewmodel.CharacterDetailsViewModel
import com.bina.designsystem.animation.fadeSlideIn
import com.bina.designsystem.components.StatusBadge
import com.bina.designsystem.tokens.SpacingTokens
import org.koin.androidx.compose.koinViewModel

private val HeroHeight = 320.dp
private val CardOverlap = 24.dp

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is CharacterDetailsUiState.Loading -> {
                val loadingDesc = stringResource(DesignSystemR.string.loading_content_description)
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { contentDescription = loadingDesc },
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is CharacterDetailsUiState.Success -> {
                CharacterDetailsContent(
                    character = state.character,
                    episodesState = state.episodesState,
                    onBackClick = onBackClick
                )
            }
            is CharacterDetailsUiState.Error -> {
                Text(
                    text = state.message ?: "Erro desconhecido",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(SpacingTokens.spacing24)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (uiState !is CharacterDetailsUiState.Success) {
            PillBackButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(SpacingTokens.spacing8)
            )
        }
    }
}

@Composable
internal fun CharacterDetailsContent(
    character: CharacterDetailsUiModel,
    episodesState: EpisodesState,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val heroHeightPx = remember(density) { with(density) { HeroHeight.toPx() } }
    val threshold = heroHeightPx * 0.6f

    val toolbarFraction by animateFloatAsState(
        targetValue = ((scrollState.value - threshold * 0.4f) / (threshold * 0.6f)).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "toolbarFraction"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroHeight)
            ) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                StatusBadge(
                    status = character.status,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SpacingTokens.spacing16)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val overlapPx = CardOverlap.roundToPx()
                        layout(placeable.width, placeable.height - overlapPx) {
                            placeable.placeRelative(0, -overlapPx)
                        }
                    },
                shape = RoundedCornerShape(topStart = CardOverlap, topEnd = CardOverlap),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.spacing24)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.spacing4))
                    Text(
                        text = "${character.species} · ${character.gender}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = SpacingTokens.spacing16),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    DetailItem(label = "Status", value = character.status, index = 0)
                    DetailItem(label = "Origem", value = character.origin, index = 1)
                    DetailItem(label = "Localização", value = character.location, index = 2)

                    EpisodeListSection(episodesState = episodesState)

                    Spacer(modifier = Modifier.height(SpacingTokens.spacing32))
                }
            }
        }

        // Scroll-aware name bar (fades in, no back button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = toolbarFraction))
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = character.name,
                modifier = Modifier
                    .alpha(toolbarFraction)
                    .padding(horizontal = 80.dp)
                    .semantics { if (toolbarFraction < 0.5f) hideFromAccessibility() },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        // Floating pill back button — always legible over any hero image
        PillBackButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(SpacingTokens.spacing8)
        )
    }
}

@Composable
private fun PillBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.48f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(DesignSystemR.string.toolbar_back_content_description),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(DesignSystemR.string.toolbar_back_content_description),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fadeSlideIn(index)
            .semantics(mergeDescendants = true) {}
            .padding(vertical = SpacingTokens.spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun EpisodeListSection(episodesState: EpisodesState) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = SpacingTokens.spacing16),
        color = MaterialTheme.colorScheme.outlineVariant
    )

    when (episodesState) {
        is EpisodesState.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aparece em",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            repeat(3) { EpisodeSkeletonItem() }
        }
        is EpisodesState.Success -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingTokens.spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aparece em",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${episodesState.episodes.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            episodesState.episodes.forEachIndexed { index, episode ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                EpisodeCard(episode = episode, index = index)
            }
        }
        is EpisodesState.Error -> {
            Text(
                text = "Aparece em",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = SpacingTokens.spacing8)
            )
            Text(
                text = "Não foi possível carregar os episódios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun EpisodeCard(episode: EpisodeUiModel, index: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fadeSlideIn(index)
            .padding(vertical = SpacingTokens.spacing8)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = episode.code,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = episode.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = episode.airDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(top = SpacingTokens.spacing4)
        )
    }
}

@Composable
private fun EpisodeSkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.spacing8),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp, 14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
        )
    }
}