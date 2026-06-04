# Documentação de Engenharia

Este projeto mantém três camadas de documentação técnica versionadas junto com o código, dentro da pasta `.claude/` e `.gemini/rules/`. Cada camada tem um propósito diferente.

```mermaid
graph TD
    RULES["📋 Rules\n.gemini/rules/\nRegras permanentes do projeto"]
    SPECS["📄 Specs SDDs\n.claude/specs/\nO que construir e por quê"]
    ADRS["🏛 ADRs\n.claude/adrs/\nDecisões arquiteturais tomadas"]

    RULES -->|guiam| SPECS
    SPECS -->|geram| ADRS
    ADRS -->|retroalimentam| RULES
```

---

## 📋 Rules — Regras permanentes

As rules ficam em `.gemini/rules/` e definem os padrões que **todo o código deve seguir sempre**, independente da feature. São lidas por ferramentas de IA (Gemini, Claude) para garantir consistência automática nas sugestões de código.

| Arquivo | O que define |
|---------|-------------|
| `ARCHITECTURE.md` | Clean Architecture obrigatória, separação de camadas, módulos por feature |
| `TESTING.md` | JUnit 4 + MockK, padrão Given-When-Then, `runTest` para coroutines |
| `COROUTINES.md` | Nunca hardcodar dispatchers, usar `viewModelScope`, preferir `StateFlow` |
| `DESIGN_SYSTEM.md` | Apenas tokens do design system — zero valores hardcodados em features |

**Exemplo prático:** a rule `DESIGN_SYSTEM.md` impede que um dev escreva `padding(16.dp)` diretamente — o correto é `padding(SpacingTokens.spacing16)`. Isso garante que uma mudança no token propague para todo o app.

---

## 📄 Specs — O que construir

As specs ficam em `.claude/specs/` e documentam features **antes e durante** a implementação. Cada feature tem pelo menos um **SDD (Software Design Document)**, e features maiores têm também **DoR** e **DoD**.

| Tipo | Significado | Quando é criado |
|------|-------------|-----------------|
| **SDD** | Software Design Document — o quê, como e por quê implementar | Antes de começar a feature |
| **DoR** | Definition of Ready — critérios para a feature estar pronta para implementar | Junto com o SDD |
| **DoD** | Definition of Done — critérios para considerar a feature concluída | Junto com o SDD |

### Specs existentes

| Spec | Feature | Status |
|------|---------|--------|
| `chat-feature-sdd.md` | Chat com IA — Gemini API direto | Implementado |
| `agentic-chat-sdd.md` | Chat Agêntico — IA abre telas do app | Implementado |
| `core-logging-sdd.md` | Módulo de logging estruturado | Implementado |
| `core-analytics-sdd.md` | Módulo de analytics e performance | Implementado |
| `feature-observability-integration-sdd.md` | Integração de observabilidade nas features | Implementado |
| `ci-cd-quality-gates-sdd.md` | Pipeline CI/CD com Detekt, JaCoCo e auto-merge | Implementado |
| `ui-testing-strategy-sdd.md` | Estratégia de testes de UI com Robolectric e Roborazzi | Implementado |
| `screenshot-testing-paparazzi-vs-roborazzi-sdd.md` | Análise Paparazzi vs Roborazzi | Implementado |
| `episode-list-details-sdd.md` | Lista de episódios na tela de detalhes | Planejado |
| `design-system-polish-sdd.md` | Polish do design system e animações de entrada | Planejado |

---

## 🏛 ADRs — Decisões arquiteturais

ADRs (Architecture Decision Records) documentam **decisões técnicas importantes que foram tomadas**, incluindo o contexto, as alternativas consideradas e o motivo da escolha. São imutáveis — uma decisão revista não apaga a anterior, gera um novo ADR.

```mermaid
flowchart LR
    CONTEXTO["Contexto\n(por que precisamos decidir)"]
    ALTERNATIVAS["Alternativas\n(o que consideramos)"]
    DECISAO["Decisão\n(o que escolhemos)"]
    CONSEQUENCIAS["Consequências\n(impactos e trade-offs)"]

    CONTEXTO --> ALTERNATIVAS --> DECISAO --> CONSEQUENCIAS
```

### ADRs do projeto

| ADR | Decisão | Módulo |
|-----|---------|--------|
| [ADR-001](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-001-escolha-provedor-ia.md) | Escolha do provedor de IA: ML Kit Gemini Nano (on-device) | `:feature:chat` |
| [ADR-002](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-002-ciclo-vida-generativemodel.md) | `GenerativeModel` como `single` no Koin (não `factory`) | `:feature:chat` |
| [ADR-003](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-003-persona-no-repository.md) | Persona do Rick concatenada no `ChatRepositoryImpl` | `:feature:chat` |
| [ADR-004](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-004-design-chatuistate.md) | `ChatUiState` unificado com `Conversation` como estado principal | `:feature:chat` |
| [ADR-005](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-005-sem-core-network.md) | `:feature:chat` não depende de `:core:network` | `:feature:chat` |
| [ADR-006](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-006-pivot-gemini-api-direto.md) | **Pivot:** substituição do ML Kit pelo Gemini API direto com `BuildConfig` | `:feature:chat` |
| [ADR-007](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-007-episodes-inside-character-details-module.md) | Episódios dentro de `:feature:character_details`, sem novo módulo | `:feature:character_details` |
| [ADR-008](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-008-multi-id-episode-request.md) | Busca de múltiplos episódios em uma única request | `:feature:character_details` |
| [ADR-009](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-009-incremental-loading-episodes.md) | Carregamento incremental: personagem primeiro, episódios depois | `:feature:character_details` |
| [ADR-010](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-010-api-single-object-vs-array.md) | Tratamento do quirk da API: resposta como objeto único ou array | `:feature:character_details` |
| [ADR-011](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-011-dois-modulos-logging-analytics.md) | Dois módulos separados: `:core:logging` e `:core:analytics` | `:core` |
| [ADR-012](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-012-interface-first-sem-deps-externas.md) | Interface-first sem dependências externas nos módulos core | `:core` |
| [ADR-013](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-013-analytics-event-sealed-class.md) | `AnalyticsEvent` como sealed class por domínio de feature | `:core:analytics` |
| [ADR-014](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-014-performance-tracking-systemclock.md) | Performance tracking com `SystemClock.elapsedRealtime()` | `:core:analytics` |
| [ADR-015](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-015-viewmodel-como-camada-de-observabilidade-nas-features.md) | ViewModel como camada primária de observabilidade nas features | Features |
| [ADR-016](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-016-convencao-performance-traces-nas-features.md) | Convenção `feature_operação` para nomes de performance trace | Features |
| [ADR-017](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-017-homemodule-dentro-do-feature-home.md) | `homeModule` Koin declarado dentro de `:feature:home` | `:feature:home` |
| [ADR-018](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-018-debounce-search-no-viewmodel.md) | Debounce de busca e analytics de `SearchPerformed` no ViewModel | `:feature:home` |
| [ADR-019](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-019-ci-gate-obrigatorio-antes-do-auto-merge.md) | CI gate obrigatório antes do auto-merge | CI/CD |
| [ADR-020](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-020-detekt-analise-estatica-kotlin.md) | Detekt para análise estática Kotlin em todos os módulos | CI/CD |
| [ADR-021](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-021-coverage-gate-jacoco.md) | Coverage gate obrigatório ≥ 60% via JaCoCo | CI/CD |
| [ADR-022](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-022-paralelismo-jobs-ci.md) | Jobs paralelos no CI: static-analysis e test independentes | CI/CD |
| [ADR-023](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-023-roborazzi-screenshot-testing.md) | Roborazzi para screenshot testing (vs Paparazzi) | Testes |
| [ADR-024](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-024-feature-auth-simulada.md) | Autenticação simulada com JWT mock e EncryptedSharedPreferences | `:feature:auth` |

### ADR em destaque: ADR-006 — O pivot do ML Kit para Gemini API

O ADR-006 é o mais relevante para entender a arquitetura atual do chat. O plano original usava **Gemini Nano on-device** (ML Kit) — sem internet, processamento local no dispositivo. Após testes no Samsung Galaxy Z Flip 6, o AICore não era suportado pelo aparelho.

A decisão foi pivotar para **Gemini 2.5 Flash via API direta**, com a chave armazenada em `local.properties` e injetada via `BuildConfig` — mais simples, compatível com qualquer dispositivo Android com internet, e sem custo de latência de download de modelo.

---

## Esta arquitetura em um app de produção com milhões de usuários

O projeto é educacional, mas as decisões arquiteturais refletem exatamente o que o Google recomenda para apps Android em produção — com argumentos que ficam mais fortes, não mais fracos, conforme a escala cresce.

### Clean Architecture: regras de negócio que não quebram com mudança de infraestrutura

A separação em Domain → Data → Presentation significa que as regras de negócio (UseCases) não conhecem Retrofit, Room, Firebase ou qualquer SDK externo. Quando um app chega a milhões de usuários, a infraestrutura muda com frequência: troca de CDN, migração de banco, novo backend de analytics. Clean Architecture garante que essas mudanças não quebrem a lógica de negócio.

**No projeto:** `LoginUseCase` valida credenciais sem saber que o repositório usa `delay()` hoje — amanhã poderia usar uma chamada real de rede sem tocar no UseCase.

> "Separation of concerns is the most important principle to follow when designing your app architecture."
> — [Guide to app architecture · Google](https://developer.android.com/topic/architecture)

### Modularização: o que o Gradle não compila não atrasa o build

Em um app com dezenas de features e um time de 50+ engenheiros, o tempo de build é um KPI de produtividade. Módulos independentes são compilados em paralelo pelo Gradle e, mais importante, **não são recompilados quando não mudaram**.

A Google usa exatamente essa estratégia no Now in Android — o app de referência deles — que tem mais de 20 módulos, compilados com Gradle Configuration Cache e Build Cache habilitados.

**No projeto:** mudar `:feature:auth` não retoca `:feature:chat`, `:core:designsystem` nem `:core:network`.

Além disso, modularização habilita o **Play Feature Delivery**: features podem ser entregues sob demanda pelo Play Store, reduzindo o tamanho do APK inicial — crítico para usuários com dispositivos de entrada ou conexões lentas.

> "Modularization is a practice of organizing a codebase into loosely coupled and self contained parts. Each part is a module. Each module is independent and serves a clear purpose."
> — [Guide to Android app modularization · Google](https://developer.android.com/topic/modularization)

### Interface-first: troca de infraestrutura sem tocar nas features

`AppLogger`, `AnalyticsTracker`, `PerformanceTracker` e `SecureStorage` são interfaces. As features nunca importam `Log.d()`, `FirebaseAnalytics` ou `EncryptedSharedPreferences` diretamente.

Em produção, esse padrão vale muito:

| Interface | Hoje (estudo) | Em produção |
|-----------|--------------|-------------|
| `AnalyticsTracker` | `LogcatAnalyticsTracker` | `FirebaseAnalyticsTracker` |
| `PerformanceTracker` | `LogcatPerformanceTracker` | `FirebasePerformanceTracker` |
| `AppLogger` | `LogcatLogger` | `CrashlyticsLogger` |
| `SecureStorage` | `EncryptedPrefsStorage` | `EncryptedDataStoreStorage` |

Cada troca é **uma nova classe + uma linha no Koin**. As 4 features não mudam.

> "We recommend that you code to interfaces for the repositories, data sources and other classes in your app. This way you can swap implementations for testing and in future features."
> — [Architecture recommendations · Google](https://developer.android.com/topic/architecture/recommendations)

### StateFlow + sealed UiState: sem estado ambíguo em produção

`Loading`, `Success`, `Error` são estados mutuamente exclusivos representados por tipos — não por combinações de booleans (`isLoading = true`, `hasError = false`, `data = null`). Isso elimina estados impossíveis como `isLoading = true && hasError = true`.

Em escala, isso importa para rastreamento de bugs: um evento de analytics disparado em `LoginUiState.Error` sempre carrega a mensagem de erro. Não há campo nullable que esqueceu de ser preenchido.

> "We recommend modeling UI state as a sealed class when there are a small number of different states."
> — [UI layer · Google](https://developer.android.com/topic/architecture/ui-layer/stateholders)

### Paging 3: paginação em escala sem carregar a lista inteira

`:feature:home` usa Paging 3 para carregar personagens. Com milhões de registros no backend, a única abordagem viável é paginação — carregar a lista completa em memória causaria OOM (out of memory) em dispositivos de entrada.

O Paging 3 integra com `LazyColumn`/`LazyVerticalGrid` do Compose, gerencia cache, retry e estado de loading de cada página de forma independente.

> "The Paging library helps you load and display pages of data from a larger dataset from local storage or over network."
> — [Paging 3 overview · Google](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)

### Segurança com Android Keystore: o padrão para tokens em produção

`:core:security` usa `EncryptedSharedPreferences` com chave mestra gerada pelo **Android Keystore**. A chave nunca sai do hardware seguro (TEE/SE) — mesmo que o arquivo de preferências seja extraído, os dados são ilegíveis sem a chave de hardware.

Essa é a recomendação explícita do Google para armazenar tokens de acesso, credenciais e dados sensíveis em apps Android. Em produção, o mesmo padrão seria usado para tokens OAuth e refresh tokens.

> "Use the Android Keystore system to store cryptographic keys in a container to make it more difficult to extract from the device."
> — [Android Keystore System · Google](https://developer.android.com/privacy-and-security/keystore)

### O que precisaria evoluir para produção real

Esta arquitetura está pronta para as evoluções críticas de escala sem reescrita:

| Necessidade em produção | Caminho de evolução | Impacto no código atual |
|------------------------|--------------------|-----------------------|
| Offline-first | Room + repositório offline-first (mesmo contrato de interface) | Só a implementação do repositório |
| Analytics de produção | `FirebaseAnalyticsTracker` implementando `AnalyticsTracker` | Uma classe nova, uma linha no Koin |
| Auth real (OAuth) | `AuthRepositoryImpl` chamando API real | O `LoginUseCase` não muda |
| Performance de startup | Baseline Profiles por módulo | Configuração de build, sem toque em código |
| Features sob demanda | Play Feature Delivery com módulos dinâmicos | Configuração de módulo, sem refatoração |
| Background sync | WorkManager chamando os mesmos UseCases | Nova camada de agendamento, domínio intacto |

---

## Referências oficiais do Google

| Referência | O que cobre |
|-----------|------------|
| [Guide to app architecture](https://developer.android.com/topic/architecture) | Princípios de Clean Architecture para Android — camadas, UDF, recomendações |
| [Modularization guide](https://developer.android.com/topic/modularization) | Estratégias de modularização, tipos de módulo, when to modularize |
| [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations) | Lista prescritiva de boas práticas por camada |
| [Now in Android](https://github.com/android/nowinandroid) | App de referência do Google — multi-módulo, Compose, MVI em produção |
| [UI layer](https://developer.android.com/topic/architecture/ui-layer) | StateFlow, sealed UiState, UDF (Unidirectional Data Flow) |
| [Data layer](https://developer.android.com/topic/architecture/data-layer) | Repositórios, fontes de dados, interface-first |
| [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) | Paginação em escala integrada com Compose |
| [Android Keystore System](https://developer.android.com/privacy-and-security/keystore) | Armazenamento seguro de chaves criptográficas |
| [Jetpack Security](https://developer.android.com/jetpack/androidx/releases/security) | EncryptedSharedPreferences e EncryptedFile |
| [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) | Otimização de startup e performance por módulo |
| [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery) | Entrega de features sob demanda para reduzir APK inicial |
| [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) | Background sync garantido, compatível com Doze e App Standby |
