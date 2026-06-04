# Core: Observabilidade

> Como logging, analytics e rastreamento de performance estão integrados em cada feature do app. Todos os módulos seguem o mesmo padrão — o ViewModel é a camada de observabilidade.

---

## Módulos

| Módulo | Interface principal | Responsabilidade |
|--------|--------------------|-----------------| 
| `:core:logging` | `AppLogger` | Logging estruturado por nível: debug / info / warn / error |
| `:core:analytics` | `AnalyticsTracker` | Rastreamento de eventos de negócio |
| `:core:analytics` | `PerformanceTracker` | Mede duração de operações com `startTrace` / `stopTrace` |

**Regra:** todas as interfaces são sem dependências externas — swappable sem mudar as features. Ver [ADR-012](../adrs/ADR-012-interface-first-sem-deps-externas.md).

---

## Padrão de Injeção

Todo ViewModel que rastreia eventos recebe `AppLogger` e `AnalyticsTracker` como parâmetros de construtor via Koin:

```kotlin
class XViewModel(
    private val useCase: XUseCase,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    // PerformanceTracker opcional — apenas quando há operações longas mensuráveis
) : ViewModel()
```

---

## Eventos por Feature

Cada feature define seus eventos como `sealed class` em `analytics/`:

### :feature:home — `HomeEvent`

| Evento | Quando |
|--------|--------|
| `CharacterClicked(id)` | Clique em personagem na grid |
| `SearchPerformed(query)` | Busca com query não-vazia |
| `PaginationLoadedNextPage` | Nova página carregada pelo Pager |

### :feature:character_details — `CharacterDetailsEvent`

| Evento | Quando |
|--------|--------|
| `ScreenOpened(characterId)` | `getCharacterDetails` chamado |
| `EpisodesLoaded(count)` | Episódios carregados com sucesso |
| `EpisodesLoadFailed` | Falha ao carregar episódios |

### :feature:chat — `ChatEvent`

| Evento | Quando |
|--------|--------|
| `ModelUnavailable` | Modelo Gemini indisponível |
| `MessageSent` | Usuário envia mensagem |
| `AgentNavigationTriggered(action)` | IA dispara navegação (`"open_character"` ou `"search_characters"`) |

### :feature:auth — `AuthEvent`

| Evento | Quando |
|--------|--------|
| `LoginAttempt` | `onLoginClicked` chamado (antes da validação) |
| `LoginSuccess` | `AuthResult.Success` |
| `LoginFailure` | Qualquer resultado de erro (InvalidEmail, WeakPassword, InvalidCredentials) |
| `LogoutRequested` | Logout solicitado |

> `LoginFailure` é um evento único independente do tipo de erro — intencionalmente genérico para não vazar informação de qual campo falhou.

---

## Performance Tracking

`PerformanceTracker` é usado apenas em operações com latência mensurável. Auth usa `delay(800)` fixo (simulado), então não rastreia performance.

| Feature | Trace | Quando mede |
|---------|-------|------------|
| `home` | `home_screen_load` | Desde `init` até primeiro dado carregado |
| `character_details` | `character_details_load` | Requisição de detalhes |
| `character_details` | `episodes_fetch` | Requisição de episódios |
| `chat` | `chat_response_time` | Desde envio até resposta da IA |

---

## Cobertura de Testes

Analytics são mockados nos testes de ViewModel:

```kotlin
private val logger = mockk<AppLogger>(relaxed = true)
private val analytics = mockk<AnalyticsTracker>()

// Verificação de evento:
verify { analytics.track(AuthEvent.LoginSuccess) }
```

`:feature:auth` tem cobertura de `AuthEvent` via `AuthEventTest` — garante que todos os `name` e `properties` de cada evento estão corretos mesmo sem o analytics real.

---

## Referências

- [ADR-011](../adrs/ADR-011-dois-modulos-logging-analytics.md) — por que dois módulos separados
- [ADR-012](../adrs/ADR-012-interface-first-sem-deps-externas.md) — interface-first sem deps externas
- [ADR-013](../adrs/ADR-013-analytics-event-sealed-class.md) — eventos como sealed classes
- [ADR-015](../adrs/ADR-015-viewmodel-como-camada-de-observabilidade-nas-features.md) — ViewModel como camada primária
- [ADR-016](../adrs/ADR-016-convencao-performance-traces-nas-features.md) — convenção de naming para traces
- [Feature: Auth Simulada](Feature-Auth-Simulada.md) — AuthEvent em contexto
