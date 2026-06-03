# SDD — Módulo :core:analytics

**Módulo:** `:core:analytics`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-02

---

## 1. Contexto e Objetivo

O projeto não tem rastreamento de eventos de usuário nem monitoramento de performance. Não é possível saber quais personagens são mais clicados, com que frequência o chat é usado, se episódios carregam rápido ou se o Gemini demora demais para responder.

O objetivo do `:core:analytics` é fornecer:
1. **AnalyticsTracker** — rastreamento de eventos de negócio (ações do usuário)
2. **PerformanceTracker** — medição de tempo de operações críticas

Ambos são interfaces injetáveis via Koin, sem dependências externas, substituíveis por Firebase Analytics/Performance no futuro.

---

## 2. Decisões Técnicas

- [ADR-011](../adrs/ADR-011-dois-modulos-logging-analytics.md) — Por que dois módulos separados (logging e analytics)
- [ADR-012](../adrs/ADR-012-interface-first-sem-deps-externas.md) — Interface-first sem dependências externas
- [ADR-013](../adrs/ADR-013-analytics-event-sealed-class.md) — AnalyticsEvent como sealed class por domínio
- [ADR-014](../adrs/ADR-014-performance-tracking-systemclock.md) — Performance tracking com SystemClock

---

## 3. Arquitetura

```
:core:analytics
├── event/
│   └── AnalyticsEvent.kt          ← interface marker base
├── AnalyticsTracker.kt            ← interface de rastreamento de eventos
├── PerformanceTracker.kt          ← interface de medição de tempo
└── impl/
    ├── LogcatAnalyticsTracker.kt  ← loga eventos no Logcat
    └── LogcatPerformanceTracker.kt ← mede tempo com SystemClock
└── di/
    └── AnalyticsModule.kt         ← Koin bindings

:feature:home
└── analytics/
    └── HomeEvent.kt               ← sealed class com eventos da feature Home

:feature:character_details
└── analytics/
    └── CharacterDetailsEvent.kt   ← sealed class com eventos da feature

:feature:chat
└── analytics/
    └── ChatEvent.kt               ← sealed class com eventos do chat
```

Features declaram seus eventos no próprio módulo — sem dependência entre features.

---

## 4. Contratos das Interfaces

### AnalyticsEvent (interface marker)

```kotlin
// Em :core:analytics
interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String> get() = emptyMap()
}
```

### AnalyticsTracker

```kotlin
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
```

### PerformanceTracker

```kotlin
interface PerformanceTracker {
    fun startTrace(name: String)
    fun stopTrace(name: String): Long  // retorna duração em ms
}
```

---

## 5. Implementações Concretas

### LogcatAnalyticsTracker

```kotlin
class LogcatAnalyticsTracker(private val logger: AppLogger) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        val props = if (event.properties.isEmpty()) ""
                    else " | ${event.properties.entries.joinToString { "${it.key}=${it.value}" }}"
        logger.info(TAG, "[EVENT] ${event.name}$props")
    }

    companion object {
        private const val TAG = "Analytics"
    }
}
```

Exemplo de saída no Logcat:
```
I/Analytics: [EVENT] home_character_clicked | character_id=123
I/Analytics: [EVENT] home_search_performed | query=Rick
```

### LogcatPerformanceTracker

```kotlin
class LogcatPerformanceTracker(private val logger: AppLogger) : PerformanceTracker {

    private val activeTraces = mutableMapOf<String, Long>()

    override fun startTrace(name: String) {
        activeTraces[name] = SystemClock.elapsedRealtime()
    }

    override fun stopTrace(name: String): Long {
        val start = activeTraces.remove(name)
        if (start == null) {
            logger.warn(TAG, "[PERF] stopTrace called without startTrace: $name")
            return -1L
        }
        val duration = SystemClock.elapsedRealtime() - start
        logger.info(TAG, "[PERF] $name: ${duration}ms")
        return duration
    }

    companion object {
        private const val TAG = "Performance"
    }
}
```

---

## 6. Eventos por Feature

### HomeEvent (em :feature:home)

```kotlin
sealed class HomeEvent : AnalyticsEvent {

    data class CharacterClicked(val characterId: String) : HomeEvent() {
        override val name = "home_character_clicked"
        override val properties = mapOf("character_id" to characterId)
    }

    data class SearchPerformed(val query: String) : HomeEvent() {
        override val name = "home_search_performed"
        override val properties = mapOf("query" to query)
    }

    object PaginationLoadedNextPage : HomeEvent() {
        override val name = "home_pagination_next_page"
    }
}
```

### CharacterDetailsEvent (em :feature:character_details)

```kotlin
sealed class CharacterDetailsEvent : AnalyticsEvent {

    data class ScreenOpened(val characterId: String) : CharacterDetailsEvent() {
        override val name = "character_details_screen_opened"
        override val properties = mapOf("character_id" to characterId)
    }

    data class EpisodesLoaded(val episodeCount: Int) : CharacterDetailsEvent() {
        override val name = "character_details_episodes_loaded"
        override val properties = mapOf("episode_count" to episodeCount.toString())
    }

    object EpisodesLoadFailed : CharacterDetailsEvent() {
        override val name = "character_details_episodes_load_failed"
    }
}
```

### ChatEvent (em :feature:chat)

```kotlin
sealed class ChatEvent : AnalyticsEvent {

    object MessageSent : ChatEvent() {
        override val name = "chat_message_sent"
    }

    object ModelUnavailable : ChatEvent() {
        override val name = "chat_model_unavailable"
    }

    data class AgentNavigationTriggered(val action: String) : ChatEvent() {
        override val name = "chat_agent_navigation"
        override val properties = mapOf("action" to action)
    }
}
```

---

## 7. Koin Module

```kotlin
// AnalyticsModule.kt
val analyticsModule = module {
    single<AnalyticsTracker> { LogcatAnalyticsTracker(get()) }
    single<PerformanceTracker> { LogcatPerformanceTracker(get()) }
}
```

`get()` resolve `AppLogger` do `loggingModule`. A ordem de registro em `appModules` deve garantir que `loggingModule` vem antes de `analyticsModule`.

---

## 8. Como Features Consomem

### Analytics de evento no ViewModel

```kotlin
class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    fun onCharacterClicked(characterId: String) {
        analyticsTracker.track(HomeEvent.CharacterClicked(characterId))
        // navegar...
    }
}
```

### Performance tracking em DataSource

```kotlin
class CharacterDetailsDataSourceImpl(
    private val apiService: CharacterDetailsApiService,
    private val performanceTracker: PerformanceTracker
) : CharacterDetailsDataSource {

    override suspend fun getCharacterDetails(id: String): CharacterDetailsData {
        performanceTracker.startTrace("character_details_load")
        val result = apiService.getCharacterDetails(id)
        performanceTracker.stopTrace("character_details_load")
        return result
    }
}
```

---

## 9. Estrutura de Arquivos do Módulo

```
core/analytics/
├── build.gradle.kts
└── src/
    ├── main/
    │   └── java/com/bina/analytics/
    │       ├── event/
    │       │   └── AnalyticsEvent.kt
    │       ├── AnalyticsTracker.kt
    │       ├── PerformanceTracker.kt
    │       ├── impl/
    │       │   ├── LogcatAnalyticsTracker.kt
    │       │   └── LogcatPerformanceTracker.kt
    │       └── di/
    │           └── AnalyticsModule.kt
    └── test/
        └── java/com/bina/analytics/
            ├── LogcatAnalyticsTrackerTest.kt
            └── LogcatPerformanceTrackerTest.kt
```

**build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.bina.analytics"
    compileSdk = 35
    defaultConfig { minSdk = 28 }
}

dependencies {
    implementation(project(":core:logging"))
    implementation(libs.koin.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

---

## 10. Critérios de Aceite

- [ ] `AnalyticsEvent` é uma interface com `name: String` e `properties: Map<String, String>`
- [ ] `AnalyticsTracker` e `PerformanceTracker` são interfaces puras sem dependências externas
- [ ] `LogcatAnalyticsTracker` loga eventos no formato `[EVENT] <name> | <props>` via `AppLogger`
- [ ] `LogcatPerformanceTracker` usa `SystemClock.elapsedRealtime()` e loga `[PERF] <name>: <ms>ms`
- [ ] `stopTrace` sem `startTrace` correspondente loga warning e retorna `-1L`
- [ ] Sealed classes de eventos declaradas em cada feature (HomeEvent, CharacterDetailsEvent, ChatEvent)
- [ ] `analyticsModule` Koin registra ambos os trackers como singletons
- [ ] Features injetam `AnalyticsTracker` e `PerformanceTracker` via construtor
- [ ] Testes mockam as interfaces com `mockk<AnalyticsTracker>()` sem setup especial
- [ ] Substituição por Firebase requer apenas nova classe + troca no Koin — zero mudança nas features