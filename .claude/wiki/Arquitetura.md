# Arquitetura

> Visão geral dos módulos, camadas e fluxo de dependências do app Rick & Morty AI.

---

## Grafo de Módulos

```
                        ┌─────────────┐
                        │    :app     │
                        └──────┬──────┘
                               │ depende de tudo
          ┌────────────────────┼────────────────────┐
          │                    │                    │
    ┌─────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
    │:feature:   │      │:feature:    │     │:feature:    │
    │home        │      │chat         │     │character_   │
    └─────┬──────┘      └──────┬──────┘     │details      │
          │                    │             └──────┬──────┘
          │             ┌──────▼──────┐             │
          │             │:feature:    │             │
          │             │auth         │             │
          │             └──────┬──────┘             │
          │                    │                    │
          └────────────────────┼────────────────────┘
                               │
          ┌────────────────────┼────────────────────────────┐
          │                    │                            │
    ┌─────▼──────┐      ┌──────▼──────┐     ┌─────────────▼──────┐
    │:core:      │      │:core:       │     │:core:               │
    │network     │      │navigation   │     │designsystem         │
    └────────────┘      └─────────────┘     └────────────────────┘
    ┌────────────┐      ┌─────────────┐     ┌────────────────────┐
    │:core:      │      │:core:       │     │:core:              │
    │logging     │      │analytics    │     │security            │
    └────────────┘      └─────────────┘     └────────────────────┘
```

**Regra:** dependências só fluem para dentro. Features dependem de core, nunca entre si. `:app` é o único que conhece todos os módulos.

---

## Módulos

### :app

Ponto de entrada do app. Responsável por:
- Inicializar o Koin com todos os módulos (`appModules`)
- Declarar o `NavHost` com todas as rotas
- Nenhuma lógica de negócio

**Módulos Koin registrados:**
```kotlin
val appModules = listOf(
    loggingModule, analyticsModule, networkModule,
    homeModule, characterDetailsModule, chatModule,
    keysModule, securityModule, authModule
)
```

---

### Features

| Módulo | Tela(s) | Depende de |
|--------|---------|------------|
| `:feature:home` | Grid de personagens, busca | `:core:network`, `:core:designsystem`, `:core:logging`, `:core:analytics`, `:core:navigation` |
| `:feature:character_details` | Detalhes do personagem, episódios | `:core:network`, `:core:designsystem`, `:core:logging`, `:core:analytics`, `:core:navigation` |
| `:feature:chat` | Chat com IA (Gemini) | `:core:designsystem`, `:core:logging`, `:core:analytics`, `:core:navigation` |
| `:feature:auth` | Tela de login simulado | `:core:security`, `:core:designsystem`, `:core:logging`, `:core:analytics`, `:core:navigation` |

---

### Core

| Módulo | Responsabilidade |
|--------|-----------------|
| `:core:network` | Cliente HTTP (Retrofit + OkHttp), retry com backoff exponencial |
| `:core:designsystem` | Tokens de design (cores, tipografia, espaçamento), componentes compartilhados, tema Material 3 |
| `:core:navigation` | `NavDestination` sealed class com todas as rotas, interface `Navigator` |
| `:core:logging` | `AppLogger` — interface de logging estruturado por nível (debug/info/warn/error) |
| `:core:analytics` | `AnalyticsTracker` + `PerformanceTracker` — interfaces para eventos e rastreamento de performance |
| `:core:security` | `SecureStorage` — interface + impl via `EncryptedSharedPreferences` e Android Keystore |

---

## Navegação

Todas as rotas são definidas em `NavDestination` (`:core:navigation`):

```kotlin
sealed class NavDestination(val route: String) {
    object Login  : NavDestination("login")
    object Home   : NavDestination("home")          // home?query={query}
    object Chat   : NavDestination("chat")
    data class Detail(val itemId: String) : NavDestination("detail/{itemId}")
}
```

**Fluxo de navegação:**

```
Login ──onLoginSuccess──▶ Home ──onCharacterClick──▶ Detail
                          Home ──onChatClick────────▶ Chat
                          Chat ──openCharacter───────▶ Detail
                          Chat ──searchCharacters─────▶ Home (com query)
```

`Login → Home` usa `popUpTo(Login) { inclusive = true }` — o botão Voltar em Home não retorna ao Login.

---

## Padrão de Camadas (Clean Architecture)

Cada feature segue a mesma estrutura:

```
feature/X/
└── src/main/java/com/bina/X/
    ├── domain/           ← sem deps Android
    │   ├── model/        ← entidades de negócio
    │   ├── repository/   ← interfaces (contratos)
    │   └── usecase/      ← regras de negócio
    ├── data/
    │   └── repository/   ← implementações (Retrofit, EncryptedPrefs, etc.)
    ├── presentation/
    │   ├── state/        ← sealed class UiState
    │   ├── viewmodel/    ← StateFlow + Koin + Logger + Analytics
    │   └── view/         ← Composables (sem lógica)
    ├── analytics/        ← sealed class XEvent : AnalyticsEvent
    └── di/               ← módulo Koin da feature
```

**Regra de validação:** regras de negócio ficam no `UseCase`, nunca no repositório ou na UI. Ver [ADR-024](../adrs/ADR-024-feature-auth-simulada.md) para o exemplo do `LoginUseCase`.

---

## Injeção de Dependência (Koin)

Cada módulo declara seu próprio módulo Koin em `di/`:

| Escopo | Quando usar |
|--------|------------|
| `single` | Uma instância por app — conexões, storage (`SecureStorage`), clientes HTTP |
| `factory` | Nova instância a cada injeção — UseCases, Repositories |
| `viewModel` | Gerenciado pelo ciclo de vida do ViewModel |

---

## Referências

- [Core: Security](Core-Security-Module.md) — EncryptedSharedPreferences e Android Keystore
- [Feature: Auth Simulada](Feature-Auth-Simulada.md) — fluxo de autenticação local
- [Core: Observabilidade](Core-Observabilidade.md) — logging, analytics e performance por módulo
- [ADR-024](../adrs/ADR-024-feature-auth-simulada.md) — decisões arquiteturais de auth e security
