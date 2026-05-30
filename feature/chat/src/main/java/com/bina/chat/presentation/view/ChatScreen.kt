package com.bina.chat.presentation.view

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bina.chat.domain.model.MessageRole
import com.bina.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.presentation.state.ChatUiState
import com.bina.chat.presentation.viewmodel.ChatViewModel
import com.bina.designsystem.animation.fadeSlideIn
import com.bina.designsystem.components.Toolbar
import com.bina.designsystem.tokens.ColorTokens
import com.bina.designsystem.tokens.ElevationTokens
import com.bina.designsystem.tokens.SpacingTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { Toolbar(title = "Rick AI", onBackClick = onBackClick) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (val state = uiState) {
                is ChatUiState.Initializing -> InitializingContent()
                is ChatUiState.ModelUnavailable -> ModelUnavailableContent()
                is ChatUiState.ModelDownloadable -> ModelUnavailableContent()
                is ChatUiState.Conversation -> ConversationContent(
                    state = state,
                    onSendMessage = viewModel::sendMessage,
                    onDismissError = viewModel::dismissError
                )
            }
        }
    }
}

@Composable
private fun InitializingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TypingIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ModelUnavailableContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(SpacingTokens.spacing64),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing16))
        Text(
            text = "Erro ao carregar modelo",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing8))
        Text(
            text = "Não foi possível inicializar o Rick AI. Verifique sua conexão e tente novamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConversationContent(
    state: ChatUiState.Conversation,
    onSendMessage: (String) -> Unit,
    onDismissError: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.messages.isEmpty()) {
            EmptyConversationContent(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.spacing8),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.spacing8)
            ) {
                item { Spacer(modifier = Modifier.height(SpacingTokens.spacing8)) }
                items(state.messages) { message ->
                    ChatMessageItem(message = message)
                }
                item { Spacer(modifier = Modifier.height(SpacingTokens.spacing8)) }
            }
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.Error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SpacingTokens.spacing16,
                        vertical = SpacingTokens.spacing4
                    )
            )
        }

        ChatInputRow(
            inputText = inputText,
            isEnabled = !state.isAiTyping,
            onInputChange = { inputText = it },
            onSend = {
                onSendMessage(inputText.trim())
                inputText = ""
            }
        )
    }
}

@Composable
private fun EmptyConversationContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing24))
        Text(
            text = "Fale com o Rick",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing8))
        Text(
            text = "Pergunte sobre o universo de Rick and Morty. Wubba lubba dub dub!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessageUiModel) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fadeSlideIn(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = if (isUser) 280.dp else 340.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = ElevationTokens.Level1
        ) {
            if (message.isStreaming && message.text.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = SpacingTokens.spacing16, vertical = SpacingTokens.spacing16)) {
                    TypingIndicator(color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            } else {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.spacing16,
                        vertical = SpacingTokens.spacing8
                    )
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator(color: Color = MaterialTheme.colorScheme.primary) {
    val transition = rememberInfiniteTransition(label = "typing")
    val alphas = List(3) { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(durationMillis = 300, delayMillis = index * 150),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$index"
        ).value
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.spacing4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        alphas.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun ChatInputRow(
    inputText: String,
    isEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.spacing8),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = ElevationTokens.Level1
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingTokens.spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pergunte ao Rick...") },
                enabled = isEnabled,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (inputText.isNotBlank()) onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )
            IconButton(
                onClick = onSend,
                enabled = isEnabled && inputText.isNotBlank()
            ) {
                if (!isEnabled) {
                    TypingIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
