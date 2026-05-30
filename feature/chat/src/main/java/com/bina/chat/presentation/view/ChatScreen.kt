package com.bina.chat.presentation.view

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bina.chat.domain.model.MessageRole
import com.bina.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.presentation.state.ChatUiState
import com.bina.chat.presentation.viewmodel.ChatViewModel
import com.bina.designsystem.components.Toolbar
import com.bina.designsystem.tokens.ColorTokens
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is ChatUiState.Initializing -> InitializingContent()
                is ChatUiState.ModelUnavailable -> ModelUnavailableContent()
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
        CircularProgressIndicator(color = ColorTokens.Secondary)
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
            modifier = Modifier.size(64.dp),
            tint = ColorTokens.Secondary
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing16))
        Text(
            text = "Erro ao carregar modelo",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SpacingTokens.spacing8))
        Text(
            text = "Não foi possível baixar ou inicializar o modelo de IA. Verifique sua conexão e tente novamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wubba lubba dub dub!\nPergunte algo sobre Rick and Morty.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(SpacingTokens.spacing32)
                )
            }
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
                    .padding(horizontal = SpacingTokens.spacing16, vertical = SpacingTokens.spacing4)
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
private fun ChatMessageItem(message: ChatMessageUiModel) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            color = if (isUser) ColorTokens.Primary else ColorTokens.Secondary,
            tonalElevation = 2.dp
        ) {
            Text(
                text = if (message.isStreaming && message.text.isEmpty()) "..." else message.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = if (isUser) ColorTokens.OnPrimary else ColorTokens.OnSecondary,
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.spacing16,
                    vertical = SpacingTokens.spacing8
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.spacing8),
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
            keyboardActions = KeyboardActions(onSend = { if (inputText.isNotBlank()) onSend() })
        )
        IconButton(
            onClick = onSend,
            enabled = isEnabled && inputText.isNotBlank()
        ) {
            if (!isEnabled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = ColorTokens.Secondary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank()) ColorTokens.Secondary
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            }
        }
    }
}
