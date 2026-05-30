# SDD — Chat Agêntico: IA que age no app

**Módulo:** `:feature:chat`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-05-30  
**Depende de:** SDD Chat com IA (chat-feature-sdd.md)

---

## 1. Contexto e Objetivo

O chat atual responde perguntas sobre o universo de Rick and Morty com a persona do Rick Sanchez, mas é puramente textual — não tem conexão com os dados reais da API e não consegue agir dentro do app.

O objetivo desta feature é transformar o chat num **agente**: além de responder com texto, a IA pode executar ações no app com base na intenção do usuário.

**Exemplo de uso:**
> Usuário: "mostre o Morty que mais aparece"
> → Rick responde com texto sarcástico sobre Morty
> → App navega automaticamente para a tela de detalhes de Morty Smith (ID=2)

**Outros comandos suportados:**
- "abra o Rick Sanchez" → detalhe do personagem
- "buscar personagens chamados Beth" → home com filtro aplicado
- "me mostra a Summer" → detalhe do personagem

---

## 2. Decisão Técnica — Gemini Function Calling

O SDK `com.google.ai.client.generativeai:0.9.0` já suporta **Function Calling** (Tool Use): o modelo pode invocar funções declaradas pelo app ao invés de — ou além de — responder com texto.

| Aspecto | Decisão |
|---------|---------|
| Mecanismo | Gemini Function Calling via `Tool` + `FunctionDeclaration` |
| Chamada à API | `generateContent` (não streaming) para mensagens com tools |
| Resolução de personagem | `GET /character?name=X` — primeiro resultado da API |
| Módulo | Tudo dentro de `:feature:chat` (sem novo módulo) |
| Dependência cross-feature | Nenhuma — nova `CharacterSearchApiService` em `:feature:chat` |

**Por que não streaming?**  
Function calls chegam como uma part completa na resposta — não em tokens. Usar `generateContent` simplifica o parsing. O indicador de "digitando..." já existe no `ChatUiState` e cobre o período de espera.

> Ver ADR-003 para a localização da persona no Repository. O mesmo princípio se aplica: toda a lógica agêntica fica no Repository, não no ViewModel.

---

## 3. Funções Declaradas para o Gemini

```kotlin
Tool(
    functionDeclarations = listOf(
        FunctionDeclaration(
            name = "show_character",
            description = "Abre a tela de detalhes de um personagem de Rick and Morty pelo nome",
            parameters = Schema.obj(
                properties = mapOf("name" to Schema.str("Nome do personagem"))
            )
        ),
        FunctionDeclaration(
            name = "search_characters",
            description = "Abre a tela inicial com uma busca aplicada por nome de personagem",
            parameters = Schema.obj(
                properties = mapOf("query" to Schema.str("Nome ou fragmento para buscar"))
            )
        )
    )
)
```

---

## 4. Fluxo Completo

```
Usuário digita mensagem
        ↓
ChatViewModel.sendMessage(userText)
        ↓
SendMessageUseCase.invoke(userText)
        ↓
ChatRepositoryImpl.streamResponse(userText)
  → buildPrompt(RICK_PERSONA + userText)
  → ChatDataSource.sendMessageWithTools(prompt)
     → GenerativeModel.generateContent(prompt, tools)
        ↓
Gemini responde com um dos cenários:
  ┌─ Só texto → exibe como mensagem AI normal
  ├─ Só FunctionCall → executa ação, sem texto na bolha
  └─ Texto + FunctionCall → exibe texto + executa ação
        ↓
ChatRepositoryImpl detecta FunctionCallPart:
  show_character(name) →
    CharacterSearchDataSource.searchByName(name)
    → GET /character?name=X → primeiro resultado → ID
    → emite ChatNavigationEvent.OpenCharacter(id)

  search_characters(query) →
    emite ChatNavigationEvent.SearchCharacters(query)
        ↓
ChatViewModel coleta ChatNavigationEvent
  → _navigationEvent.emit(event)
        ↓
ChatScreen LaunchedEffect coleta navigationEvent
  → onNavigateToCharacter(id) ou onSearchCharacters(query)
        ↓
MainActivity executa navegação:
  OpenCharacter → navController.navigate(Detail.createRoute(id))
  SearchCharacters → navController.navigate(Home) + passa query
```

---

## 5. Arquitetura — Novos Arquivos

```
feature/chat/src/main/java/com/bina/chat/
  data/
    remote/
      CharacterSearchApiService.kt     # GET /character?name=X (nova interface Retrofit)
    datasource/
      CharacterSearchDataSource.kt     # interface
      CharacterSearchDataSourceImpl.kt # impl — busca e retorna List<CharacterSearchResult>
    model/
      CharacterSearchResult.kt         # id: Int, name: String, episodeCount: Int
  domain/
    model/
      ChatNavigationEvent.kt           # sealed class OpenCharacter(id) | SearchCharacters(query)
```

---

## 6. Arquitetura — Arquivos Modificados

| Arquivo | O que muda |
|---------|-----------|
| `ChatDataSource.kt` | Adiciona `suspend fun sendMessageWithTools(prompt: String): ChatToolResult` |
| `ChatDataSourceImpl.kt` | Implementa `sendMessageWithTools` com `generateContent` + `Tool` definitions |
| `ChatRepository.kt` | Interface mantida — `streamResponse` passa a checar por function calls internamente |
| `ChatRepositoryImpl.kt` | Processa `FunctionCallPart`, chama `CharacterSearchDataSource`, emite texto + action |
| `ChatViewModel.kt` | Adiciona `_navigationEvent: MutableSharedFlow<ChatNavigationEvent>` + `val navigationEvent` |
| `ChatScreen.kt` | Adiciona `onNavigateToCharacter: (Int) -> Unit` + `onSearchCharacters: (String) -> Unit`; coleta `navigationEvent` via `LaunchedEffect` |
| `ChatModule.kt` | Registra `CharacterSearchApiService`, `CharacterSearchDataSource`; adiciona `Tool` ao `GenerativeModel` |
| `MainActivity.kt` | Passa lambdas de navegação para `ChatScreen` |

---

## 7. Modelo de Dados — ChatToolResult

Encapsula a resposta do Gemini quando tools estão ativas:

```kotlin
data class ChatToolResult(
    val text: String?,
    val action: ChatAction? = null
)

sealed class ChatAction {
    data class OpenCharacter(val characterName: String) : ChatAction()
    data class SearchCharacters(val query: String) : ChatAction()
}
```

---

## 8. CharacterSearchApiService

Endpoint simples, sem paginação — só precisa do primeiro resultado:

```kotlin
interface CharacterSearchApiService {
    @GET("character")
    suspend fun searchByName(
        @Query("name") name: String,
        @Query("page") page: Int = 1
    ): CharacterListResponse
}

data class CharacterListResponse(
    val results: List<CharacterSearchResult>
)

data class CharacterSearchResult(
    val id: Int,
    val name: String,
    val episode: List<String>  // conta o tamanho para ranking
)
```

**Resolução de qual personagem mostrar:** primeiro resultado da API para o nome dado. A API do Rick and Morty já retorna os personagens mais relevantes primeiro (personagem principal > secundário > one-off).

---

## 9. ChatNavigationEvent

```kotlin
sealed class ChatNavigationEvent {
    data class OpenCharacter(val characterId: Int) : ChatNavigationEvent()
    data class SearchCharacters(val query: String) : ChatNavigationEvent()
}
```

Emitido via `SharedFlow` no `ChatViewModel`:
```kotlin
private val _navigationEvent = MutableSharedFlow<ChatNavigationEvent>()
val navigationEvent: SharedFlow<ChatNavigationEvent> = _navigationEvent.asSharedFlow()
```

---

## 10. Mudanças na ChatScreen

```kotlin
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onNavigateToCharacter: (Int) -> Unit,   // novo
    onSearchCharacters: (String) -> Unit,   // novo
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ChatNavigationEvent.OpenCharacter -> onNavigateToCharacter(event.characterId)
                is ChatNavigationEvent.SearchCharacters -> onSearchCharacters(event.query)
            }
        }
    }
    // ... resto da UI inalterado
}
```

---

## 11. Mudanças no MainActivity

```kotlin
composable(NavDestination.Chat.route) {
    ChatScreen(
        onBackClick = { navController.popBackStack() },
        onNavigateToCharacter = { id ->
            navController.navigate(NavDestination.Detail.createRoute(id.toString()))
        },
        onSearchCharacters = { query ->
            navController.navigate(NavDestination.Home.route)
            // HomeViewModel recebe o query via SavedStateHandle ou SharedFlow (a definir na impl)
        }
    )
}
```

---

## 12. Prompt do Rick com Function Calling

O `RICK_PERSONA` recebe uma instrução adicional sobre quando usar as funções:

```kotlin
private const val RICK_PERSONA = """Você é um especialista apaixonado no universo de Rick and Morty.
Responda com o tom sarcástico, brilhante e impaciente do Rick Sanchez.
Use gírias do Rick ocasionalmente (como "Morty", "wubba lubba dub dub", "burp").
Seja direto e um pouco condescendente, mas sempre preciso sobre o universo do show.
Quando o usuário pedir para VER ou MOSTRAR um personagem específico, use a função show_character.
Quando o usuário pedir para BUSCAR ou LISTAR personagens por nome, use a função search_characters.
Responda a seguinte pergunta como Rick responderia: """
```

---

## 13. Testes

| Teste | O que verifica |
|-------|---------------|
| `SendMessageUseCaseTest` | Atualização para cobrir retorno com `ChatToolResult` |
| `ChatViewModelTest` | `navigationEvent` emitido quando repository retorna action |
| `CharacterSearchDataSourceTest` | Chama endpoint correto, retorna primeiro resultado |
| `ChatRepositoryAgentTest` | FunctionCall → action correta; só texto → sem action |

---

## 14. Limitações Conhecidas

- **Back navigation após ação:** ao navegar para detalhes via chat agêntico, o botão voltar retorna ao chat (comportamento correto do back stack).
- **Personagem não encontrado:** se a API não retornar resultados para o nome dado, o chat exibe mensagem de erro do Rick ("Nem eu conheço esse personagem, Morty").
- **search_characters via chat:** a implementação de passar o query para o `HomeViewModel` ao navegar de volta requer uma solução de estado compartilhado (ex: `SavedStateHandle` ou um `SharedFlow` no `:core:navigation`) — a ser detalhada na implementação.
- **Sem streaming para mensagens agênticas:** o usuário vê o typing indicator por mais tempo comparado ao streaming. Aceitável para v1.

---

## 15. Possíveis Evoluções

- **show_episode(code)** — navegar para detalhes de um episódio quando existir tela de episódio
- **Streaming + function calling** — usar `generateContentStream` com parsing de parts acumuladas
- **Contexto do personagem atual** — passar o personagem aberto como contexto inicial do chat (Opção A do plano de features)
- **Ações compostas** — "compare Rick e Morty" → abre ambos os detalhes em split screen