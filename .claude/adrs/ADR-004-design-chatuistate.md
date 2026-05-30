# ADR-004 — Design do ChatUiState (estado unificado Conversation)

**Status:** Aceito
**Data:** 2026-05-29
**Feature:** `:feature:chat`

---

## Contexto

A tela de chat tem dois eixos de estado: (1) disponibilidade do modelo e (2) estado da conversa ativa. Precisávamos decidir como modelar esses estados em uma sealed class.

## Opções Avaliadas

### Opção A: Estados separados para "pronto" e "conversando"

```kotlin
sealed class ChatUiState {
    object Initializing
    object ModelDownloadable
    object ModelDownloading
    object ModelUnavailable
    object Ready                      // modelo pronto, sem mensagens
    data class Conversation(...)      // chat ativo com mensagens
}
```

- **Prós:** Explícito
- **Contras:** Transição `Ready → Conversation` requer lógica extra; o usuário envia a primeira mensagem e há um "salto" de estado. `Ready` e `Conversation(messages=[])` são semanticamente idênticos.

### Opção B: Estado `Conversation` unificado (idle + ativo)

```kotlin
data class Conversation(
    val messages: List<ChatMessageUiModel>,   // vazio = tela de boas-vindas
    val isAiTyping: Boolean,
    val errorMessage: String? = null
) : ChatUiState()
```

- **Prós:** `messages.isEmpty()` é o estado "pronto sem mensagens"; sem estado intermediário; menos transições no ViewModel
- **Contras:** A UI precisa checar `messages.isEmpty()` para renderizar a tela de boas-vindas

## Decisão

**Escolhida: Opção B — `Conversation` como estado unificado**

## Justificativa

Quando o modelo está disponível, a UI passa imediatamente para `Conversation(messages=[])`. A lista vazia é a representação natural do "pronto para conversar". Adicionar um estado `Ready` seria redundante — ele nunca teria comportamento diferente de `Conversation([], false)`.

## Implementação da UI

```kotlin
// Na ConversationContent:
if (state.messages.isEmpty()) {
    // tela de boas-vindas
} else {
    LazyColumn { /* mensagens */ }
}
```

## Consequências

- O ViewModel tem 5 estados no total (Initializing, Downloadable, Downloading, Unavailable, Conversation) em vez de 6.
- `clearChat()` simplesmente emite `Conversation(messages=[], isAiTyping=false)` — sem mudança de tipo de estado.
- A UI trata a lista vazia como welcome screen, sem lógica adicional no ViewModel.