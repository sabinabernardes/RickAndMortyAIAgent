# SDD — Integração de Observabilidade nas Features

**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-02  
**Branch:** `feat/core-logging-analytics`

---

## 1. Contexto e Objetivo

Os módulos `:core:logging` e `:core:analytics` estão implementados. Os eventos de cada feature estão definidos como sealed classes. As dependências estão declaradas nos `build.gradle.kts` e os módulos Koin registrados.

**O que falta:** nenhum ViewModel injeta ou chama `AppLogger`, `AnalyticsTracker` ou `PerformanceTracker`. Esta SDD especifica exatamente o que mudar em cada feature para completar a integração.

**Escopo desta iteração:**
- `HomeViewModel`, `CharacterDetailsViewModel`, `ChatViewModel` — novos parâmetros de construtor + chamadas de log/track/perf
- `HomeScreen.kt` — dois callbacks adicionais para rastrear click em personagem e paginação
- `homeModule`, `characterDetailsModule`, `chatModule` — atualizar `viewModel { }` no Koin

**Fora de escopo:** logging em Repositories e DataSources (ver ADR-015).

---

## 2. Decisões Técnicas

- [ADR-011](../adrs/ADR-011-dois-modulos-logging-analytics.md) — Por que dois módulos separados
- [ADR-012](../adrs/ADR-012-interface-first-sem-deps-externas.md) — Interface-first sem dependências externas
- [ADR-013](../adrs/ADR-013-analytics-event-sealed-class.md) — Eventos como sealed classes por domínio
- [ADR-014](../adrs/ADR-014-performance-tracking-systemclock.md) — Performance com SystemClock
- [ADR-015](../adrs/ADR-015-viewmodel-como-camada-de-observabilidade-nas-features.md) — ViewModel como camada primária
- [ADR-016](../adrs/ADR-016-convencao-performance-traces-nas-features.md) — Convenção de naming para traces

---

## 3. Arquitetura

```
HomeScreen ──────────────────────────────────────────────────────────
  │  onCharacterClicked(id) → viewModel.onCharacterClicked(id)       │
  │  onPageLoaded()         → viewModel.onPageLoaded()               │
  ▼                                                                   │
HomeViewModel (AppLogger + AnalyticsTracker + PerformanceTracker) ◄──┘
  │  logger.debug/info/error(TAG, ...)
  │  analytics.track(HomeEvent.*)
  │  performance.startTrace/stopTrace(TRACE_NAME)
  ▼
GetCharactersUseCase → HomeRepository (sem mudanças)
```

Mesmo padrão para `CharacterDetailsViewModel` e `ChatViewModel`.

---

## 4. :feature:home

### 4.1 HomeViewModel

**Arquivo:** `feature/home/src/main/java/com/bina/home/presentation/viewmodel/HomeViewModel.kt`

**Novos parâmetros de construtor:**
```kotlin
open class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel()
```

**`init`:**
```kotlin
init {
    logger.debug(TAG, "initialized")
    performance.startTrace(TRACE_SCREEN_LOAD)
    getCharacters()
}
```

**`getCharacters(query)`:**
```kotlin
fun getCharacters(query: String = "") {
    currentQuery = query
    if (query.isNotBlank()) analytics.track(HomeEvent.SearchPerformed(query))
    viewModelScope.launch {
        getCharactersUseCase(query)
            .map { pagingData -> pagingData.map { uiMapper.map(it) } }
            .cachedIn(viewModelScope)
            .onStart {
                logger.debug(TAG, "loading characters query='$query'")
                _uiState.value = CharactersUiState.Loading
            }
            .catch { e ->
                logger.error(TAG, "characters load failed", e)
                _uiState.value = CharactersUiState.Error(e.message)
            }
            .collect { mappedPagingData ->
                if (!screenLoadTracked) {
                    val duration = performance.stopTrace(TRACE_SCREEN_LOAD)
                    logger.info(TAG, "home_screen_load: ${duration}ms")
                    screenLoadTracked = true
                }
                _uiState.value = CharactersUiState.Success(flowOf(mappedPagingData))
            }
    }
}
```

**Novos campos e funções:**
```kotlin
private var screenLoadTracked = false

fun onCharacterClicked(characterId: Int) {
    analytics.track(HomeEvent.CharacterClicked(characterId.toString()))
}

fun onPageLoaded() {
    analytics.track(HomeEvent.PaginationLoadedNextPage)
}

companion object {
    private const val TAG = "HomeViewModel"
    private const val TRACE_SCREEN_LOAD = "home_screen_load"
}
```

### 4.2 HomeScreen.kt

**Arquivo:** `feature/home/src/main/java/com/bina/home/presentation/view/HomeScreen.kt`

**`HomeContent` — delegar click e rastrear paginação:**
```kotlin
@Composable
private fun HomeContent(
    uiState: CharactersUiState,
    onCharacterClick: (Int) -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    when (uiState) {
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
        // Loading e Error sem mudança
    }
}
```

### 4.3 Koin — homeModule

**Arquivo:** `app/src/main/java/com/bina/rickandmorty/di/Modules.kt`

```kotlin
// Antes
viewModel { HomeViewModel(get(), get()) }

// Depois
viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
```

---

## 5. :feature:character_details

### 5.1 CharacterDetailsViewModel

**Arquivo:** `feature/character_details/src/main/java/com/bina/character_details/presentation/viewmodel/CharacterDetailsViewModel.kt`

**Novos parâmetros:**
```kotlin
class CharacterDetailsViewModel(
    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val uiMapper: CharacterDetailsUiMapper,
    private val episodeUiMapper: EpisodeUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel()
```

**`getCharacterDetails(id)`:**
```kotlin
fun getCharacterDetails(id: Int) {
    viewModelScope.launch {
        _uiState.value = CharacterDetailsUiState.Loading
        analytics.track(CharacterDetailsEvent.ScreenOpened(id.toString()))
        logger.debug(TAG, "loading character id=$id")
        performance.startTrace(TRACE_DETAILS_LOAD)

        try {
            val domain = getCharacterDetailsUseCase(id)
            val duration = performance.stopTrace(TRACE_DETAILS_LOAD)
            logger.info(TAG, "character $id loaded in ${duration}ms")
            _uiState.value = CharacterDetailsUiState.Success(uiMapper.map(domain))

            performance.startTrace(TRACE_EPISODES_FETCH)
            try {
                val ids = domain.episodeUrls.map { it.substringAfterLast("/").toInt() }
                val episodes = getEpisodesUseCase(ids).map(episodeUiMapper::map)
                val episodeDuration = performance.stopTrace(TRACE_EPISODES_FETCH)
                logger.info(TAG, "episodes loaded count=${episodes.size} in ${episodeDuration}ms")
                analytics.track(CharacterDetailsEvent.EpisodesLoaded(episodes.size))
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Success(episodes))
                    } else state
                }
            } catch (e: Exception) {
                performance.stopTrace(TRACE_EPISODES_FETCH)
                logger.warn(TAG, "episodes load failed", e)
                analytics.track(CharacterDetailsEvent.EpisodesLoadFailed)
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Error(e.message))
                    } else state
                }
            }
        } catch (e: Exception) {
            performance.stopTrace(TRACE_DETAILS_LOAD)
            logger.error(TAG, "character $id load failed", e)
            _uiState.value = CharacterDetailsUiState.Error(e.message)
        }
    }
}

companion object {
    private const val TAG = "CharacterDetailsViewModel"
    private const val TRACE_DETAILS_LOAD = "character_details_load"
    private const val TRACE_EPISODES_FETCH = "episodes_fetch"
}
```

### 5.2 Koin — characterDetailsModule

**Arquivo:** `feature/character_details/src/main/java/com/bina/character_details/di/CharacterDetailsModule.kt`

```kotlin
// Antes
viewModel { CharacterDetailsViewModel(get(), get(), get(), get()) }

// Depois
viewModel { CharacterDetailsViewModel(get(), get(), get(), get(), get(), get(), get()) }
```

---

## 6. :feature:chat

### 6.1 ChatViewModel

**Arquivo:** `feature/chat/src/main/java/com/bina/chat/chat/presentation/viewmodel/ChatViewModel.kt`

**Novos parâmetros:**
```kotlin
class ChatViewModel(
    private val checkModelAvailabilityUseCase: CheckModelAvailabilityUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val repository: ChatRepository,
    private val uiMapper: ChatMessageUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel()
```

**`doCheckAvailability()`:**
```kotlin
private suspend fun doCheckAvailability() {
    logger.debug(TAG, "checking model availability")
    when (checkModelAvailabilityUseCase()) {
        is ModelAvailability.Available -> {
            logger.info(TAG, "model available")
            _uiState.value = ChatUiState.Conversation(messages = emptyList(), isAiTyping = false)
        }
        is ModelAvailability.Unavailable -> {
            logger.warn(TAG, "model unavailable")
            analytics.track(ChatEvent.ModelUnavailable)
            _uiState.value = ChatUiState.ModelUnavailable
        }
        is ModelAvailability.Downloadable -> {
            logger.info(TAG, "model downloadable")
            _uiState.value = ChatUiState.ModelDownloadable
        }
    }
}
```

**`sendMessage(userText)`:**
```kotlin
fun sendMessage(userText: String) {
    if (userText.isBlank()) return
    val currentState = _uiState.value as? ChatUiState.Conversation ?: return

    analytics.track(ChatEvent.MessageSent)
    logger.debug(TAG, "sending message length=${userText.length}")

    // ... atualizar _uiState com placeholder (sem mudança) ...

    viewModelScope.launch {
        performance.startTrace(TRACE_RESPONSE_TIME)
        try {
            val result = sendMessageUseCase(userText)
            val duration = performance.stopTrace(TRACE_RESPONSE_TIME)
            logger.info(TAG, "response received in ${duration}ms")

            val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
            val finalMessages = state.messages.toMutableList().also {
                it[it.lastIndex] = placeholder.copy(text = result.text, isStreaming = false)
            }
            _uiState.value = state.copy(messages = finalMessages, isAiTyping = false)

            result.navigationEvent?.let { event ->
                val action = when (event) {
                    is ChatNavigationEvent.OpenCharacter -> "open_character"
                    is ChatNavigationEvent.SearchCharacters -> "search_characters"
                }
                analytics.track(ChatEvent.AgentNavigationTriggered(action))
                _navigationEvent.emit(event)
            }
        } catch (e: Exception) {
            performance.stopTrace(TRACE_RESPONSE_TIME)
            logger.error(TAG, "send message failed", e)
            val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
            _uiState.value = state.copy(
                messages = state.messages.dropLast(1),
                isAiTyping = false,
                errorMessage = "Erro ao gerar resposta. Tente novamente."
            )
        }
    }
}

companion object {
    private const val TAG = "ChatViewModel"
    private const val TRACE_RESPONSE_TIME = "chat_response_time"
}
```

### 6.2 Koin — chatModule

**Arquivo:** `feature/chat/src/main/java/com/bina/chat/di/ChatModule.kt`

```kotlin
// Antes
viewModel { ChatViewModel(get(), get(), get(), get()) }

// Depois
viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get()) }
```

---

## 7. Resumo dos Arquivos Alterados

| Arquivo | Tipo de mudança |
|---|---|
| `feature/home/…/HomeViewModel.kt` | +3 params, +3 funções, +companion |
| `feature/home/…/HomeScreen.kt` | +LaunchedEffect para paginação, wrap do click |
| `feature/character_details/…/CharacterDetailsViewModel.kt` | +3 params, +companion, logging/tracking em `getCharacterDetails` |
| `feature/character_details/…/CharacterDetailsModule.kt` | `viewModel { }` de 4 para 7 args |
| `feature/chat/…/ChatViewModel.kt` | +3 params, +companion, logging/tracking em `doCheckAvailability` e `sendMessage` |
| `feature/chat/…/ChatModule.kt` | `viewModel { }` de 4 para 7 args |
| `app/…/Modules.kt` — `homeModule` | `viewModel { }` de 2 para 5 args |

---

## 8. Testes

Cada ViewModel já tem testes existentes. As mudanças são **adicionais** — não quebram os testes atuais.

**Padrão de setup nos testes:**
```kotlin
private val logger = mockk<AppLogger>(relaxed = true)
private val analytics = mockk<AnalyticsTracker>()
private val performance = mockk<PerformanceTracker>()

// No setup, fazer performance retornar uma duração fake:
every { performance.startTrace(any()) } just Runs
every { performance.stopTrace(any()) } returns 100L
```

**Novos testes a adicionar por ViewModel:**

*HomeViewModelTest:*
- `onCharacterClicked tracks CharacterClicked event`
- `onQueryChange with non-blank query tracks SearchPerformed`
- `onPageLoaded tracks PaginationLoadedNextPage`
- `error emits logger error call`

*CharacterDetailsViewModelTest:*
- `getCharacterDetails tracks ScreenOpened`
- `getCharacterDetails on success tracks nothing more (no crash)`
- `episodes success tracks EpisodesLoaded with correct count`
- `episodes error tracks EpisodesLoadFailed`

*ChatViewModelTest:*
- `model unavailable tracks ModelUnavailable`
- `sendMessage tracks MessageSent`
- `navigation event tracks AgentNavigationTriggered with correct action`
- `sendMessage error does not crash, stops trace`

---

## 9. Critérios de Aceite

- [ ] `HomeViewModel` injeta `AppLogger`, `AnalyticsTracker`, `PerformanceTracker` via Koin
- [ ] Click em personagem na `HomeScreen` chama `viewModel.onCharacterClicked(id)` antes de navegar
- [ ] Nova página na paginação dispara `HomeEvent.PaginationLoadedNextPage`
- [ ] `CharacterDetailsViewModel` rastreia `ScreenOpened` sempre que `getCharacterDetails` é chamado
- [ ] Sucesso de episódios rastreia `EpisodesLoaded(count)` com contagem correta
- [ ] Falha de episódios rastreia `EpisodesLoadFailed` e faz `stopTrace("episodes_fetch")`
- [ ] `ChatViewModel` rastreia `ModelUnavailable` quando modelo indisponível
- [ ] `sendMessage` rastreia `MessageSent` e mede `chat_response_time`
- [ ] Navegação agentica rastreia `AgentNavigationTriggered(action)` com `"open_character"` ou `"search_characters"`
- [ ] Todos os `startTrace` têm `stopTrace` correspondente, inclusive em caminhos de erro
- [ ] Build compila sem warnings; testes existentes continuam passando
- [ ] Novos testes de ViewModel verificam eventos com `verify { analytics.track(...) }`