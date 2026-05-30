# SDD — Feature de Chat com IA

**Módulo:** `:feature:chat`  
**Status:** Implementado (v2 — Gemini API direta)  
**Autor:** Sabina Bernardes  
**Data:** 2026-05-29  
**Revisão:** Pivot de ML Kit on-device → Gemini API com BuildConfig (ver ADR-006)

---

## 1. Contexto e Objetivo

O app Rick and Morty não possuía nenhuma funcionalidade de IA. O objetivo desta feature é adicionar uma tela de chat onde o usuário pode fazer perguntas sobre o universo do show e receber respostas no tom sarcástico e brilhante do Rick Sanchez.

A solução deve ser:
- **Sem friction para o usuário** — sem login, sem download, sem configuração
- **Compatível com o Z Flip 6** (e qualquer dispositivo Android com internet)
- **Gratuita** — free tier do Google AI Studio

---

## 2. Decisão Técnica

**Gemini API direta** via `com.google.ai.client.generativeai:0.9.0`, modelo `gemini-2.5-flash`, com chave configurada em `local.properties` e exposta como `BuildConfig.GEMINI_API_KEY`.

| Critério | Gemini API direta |
|----------|------------------|
| Modelo | `gemini-2.5-flash` |
| Custo | Grátis (free tier Google AI Studio) |
| Internet | Necessária |
| Credencial visível ao usuário | Nenhuma |
| Compatibilidade | Qualquer Android |
| Configuração do dev | Uma vez: criar chave em aistudio.google.com |

> `gemini-1.5-flash` foi deprecado e removido — migrado para `gemini-2.5-flash` em 2026-05-29.

> Ver ADR-006 para o histórico de decisões e o pivot do ML Kit on-device.

---

## 3. Configuração da API Key

Em `local.properties` (nunca commitado):
```properties
GEMINI_API_KEY=sua_chave_aqui
```

Em `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "GEMINI_API_KEY", "\"${properties["GEMINI_API_KEY"]}\"")
```

---

## 4. Arquitetura

O módulo segue o padrão **Clean Architecture** já estabelecido no projeto.

```
Presentation (Compose + ViewModel + StateFlow)
      ↓
  Domain (UseCases + Repository interface)
      ↓
   Data (DataSource + Repository impl + Gemini SDK)
```

**Dependências do módulo:**
- `:core:designsystem` — tokens de design e componentes
- `com.google.ai.client.generativeai` — SDK Gemini oficial
- Koin — injeção de dependência
- Compose + Material 3 — UI

---

## 5. Estrutura de Arquivos

```
feature/chat/
  build.gradle.kts
  src/main/java/com/bina/chat/
    data/
      datasource/
        ChatDataSource.kt          # interface
        ChatDataSourceImpl.kt      # integração Gemini SDK
      mapper/
        ChatMessageMapper.kt
      model/
        ChatMessageData.kt
      repository/
        ChatRepositoryImpl.kt      # persona do Rick aqui
    di/
      ChatModule.kt
    domain/
      model/
        ChatMessageDomain.kt
        MessageRole.kt
        ModelAvailability.kt       # Available / Unavailable
      repository/
        ChatRepository.kt
      usecase/
        CheckModelAvailabilityUseCase.kt
        SendMessageUseCase.kt
    presentation/
      mapper/
        ChatMessageUiMapper.kt
      model/
        ChatMessageUiModel.kt
      state/
        ChatUiState.kt
      view/
        ChatScreen.kt
      viewmodel/
        ChatViewModel.kt
```

---

## 6. Modelo de Estado (UiState)

```kotlin
sealed class ChatUiState {
    object Initializing : ChatUiState()
    object ModelUnavailable : ChatUiState()   // erro de inicialização / sem internet
    data class Conversation(
        val messages: List<ChatMessageUiModel>,
        val isAiTyping: Boolean,
        val errorMessage: String? = null
    ) : ChatUiState()
}
```

**Transições:**
```
Initializing
    ├─ Available   → Conversation(messages=[], isAiTyping=false)
    └─ Unavailable → ModelUnavailable

Conversation
    ├─ sendMessage() → Conversation(isAiTyping=true) → streaming → Conversation(isAiTyping=false)
    └─ clearChat()   → Conversation(messages=[])
```

> `ModelDownloadable` e `ModelDownloading` existem no código mas não são acionados nesta implementação. Preservados para evolução futura (ex: suporte offline).

---

## 7. Persona do Rick

Definida em `ChatRepositoryImpl`, prefixada em cada prompt:

```kotlin
private const val RICK_PERSONA = """Você é um especialista apaixonado no universo de Rick and Morty.
Responda com o tom sarcástico, brilhante e impaciente do Rick Sanchez.
Use gírias do Rick ocasionalmente (como "Morty", "wubba lubba dub dub", "burp").
Seja direto e um pouco condescendente, mas sempre preciso sobre o universo do show.
Responda a seguinte pergunta como Rick responderia: """
```

> Ver ADR-003 para a decisão de localizar a persona no Repository.

---

## 8. Inicialização do Modelo

- `GenerativeModel(modelName = "gemini-2.5-flash")` criado via `BuildConfig.GEMINI_API_KEY` — registrado como `single{}` no Koin
- `checkAvailability()` verifica apenas se a chave está configurada (não faz chamada de rede)
- `warmup()` é no-op nesta implementação (sem modelo local para pré-carregar)
- `ChatViewModel.onCleared()` chama `repository.close()`

---

## 9. UI/UX

**Entrada:** FAB na HomeScreen (ícone `SmartToy`, cor `ColorTokens.Secondary`).

**ChatScreen:**
- `Toolbar` com back navigation
- Estado `Initializing` → spinner
- Estado `ModelUnavailable` → mensagem de erro de conexão
- Estado `Conversation` vazio → texto de boas-vindas do Rick
- Estado `Conversation` ativo → `LazyColumn` com bubbles + campo de input

**Bubbles:**
- Usuário → alinhado à direita, cor `ColorTokens.Primary`
- AI → alinhado à esquerda, cor `ColorTokens.Secondary`
- Enquanto `isStreaming = true` → exibe `"..."` até o primeiro token chegar

---

## 10. Compatibilidade de Dispositivos

| Dispositivo | Suporte |
|------------|---------|
| Samsung Galaxy Z Flip 6 | Confirmado (requer internet) |
| Qualquer Android com internet | Confirmado |
| Emuladores | Confirmado |
| Dispositivos sem internet | ModelUnavailable |

---

## 11. Testes

| Arquivo | Cobertura |
|---------|-----------|
| `CheckModelAvailabilityUseCaseTest` | Available, Unavailable |
| `SendMessageUseCaseTest` | Delegação ao repository, stream de chunks |
| `ChatViewModelTest` | init por estado, sendMessage com streaming, erro, clearChat, blank message, dismissError |

---

## 12. Como Configurar (Dev)

1. Acessar `aistudio.google.com` → criar chave gratuita
2. Adicionar em `local.properties`: `GEMINI_API_KEY=sua_chave`
3. Buildar: `./gradlew :feature:chat:assembleDebug`
4. Testar: `./gradlew :feature:chat:test`

---

## 13. Possíveis Evoluções

- **Fallback offline** — retomar MediaPipe quando o modelo puder ser distribuído sem auth
- **Contexto do personagem** — passar dados do personagem atual via argumento de navegação
- **Histórico persistido** — salvar conversas com Room