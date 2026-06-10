# SDD — `ObservableUseCase` em `:core:domain`

## Problema

Use cases atuais são classes independentes sem contrato comum. Não há como instrumentar chamadas de rede de forma genérica — medir duração, registrar resultado, rastrear erros — sem duplicar código em cada use case.

---

## Módulo `:core:domain` — contratos e abstracao

### `UseCase.kt`

```kotlin
package com.bina.domain

import kotlin.time.TimeSource

// Contrato para use cases sem parâmetros
object NoParams

// Interface base — não usada diretamente, serve de contrato para DI e testes
fun interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): DomainResult<R>
}
```

### `UseCaseObserver.kt`

```kotlin
package com.bina.domain

// Definida no domínio; implementada fora (analytics, logging, tests)
interface UseCaseObserver {
    fun onStart(useCaseName: String)
    fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long)
}

object NoOpUseCaseObserver : UseCaseObserver {
    override fun onStart(useCaseName: String) = Unit
    override fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long) = Unit
}
```

**Por que `onComplete` recebe `DomainResult<*>` em vez de callbacks separados por estado:**  
A implementação do observador faz seu próprio `when` sobre o outcome — não precisa de 5 métodos. Menos acoplamento de protocolo entre domínio e analytics.

### `ObservableUseCase.kt`

```kotlin
package com.bina.domain

import kotlin.time.TimeSource

abstract class ObservableUseCase<in P, out T>(
    private val observer: UseCaseObserver = NoOpUseCaseObserver
) : UseCase<P, T> {

    // Subclasses implementam apenas a lógica de negócio
    protected abstract suspend fun execute(params: P): DomainResult<T>

    final override suspend operator fun invoke(params: P): DomainResult<T> {
        val name = this::class.simpleName ?: "UnknownUseCase"
        val mark = TimeSource.Monotonic.markNow()
        observer.onStart(name)
        return execute(params).also { result ->
            observer.onComplete(name, result, mark.elapsedNow().inWholeMilliseconds)
        }
    }
}
```

**Por que `invoke` é `final`:** garante que nenhuma subclasse pule o hook de monitoramento ao sobrescrever `invoke`. A subclasse só pode tocar `execute`.

**Por que `TimeSource.Monotonic`:** não é afetado por ajustes de relógio do sistema (NTP, mudança de timezone). Mede duração real de execução.

---

## `:core:analytics` — implementação do observador

```kotlin
// :core:analytics/src/main/java/com/bina/analytics/AnalyticsUseCaseObserver.kt
package com.bina.analytics

import com.bina.analytics.event.UseCaseEvent
import com.bina.domain.DomainResult
import com.bina.domain.UseCaseObserver

class AnalyticsUseCaseObserver(
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : UseCaseObserver {

    override fun onStart(useCaseName: String) {
        performance.startTrace(useCaseName)
    }

    override fun onComplete(useCaseName: String, outcome: DomainResult<*>, durationMs: Long) {
        performance.stopTrace(useCaseName)
        val event = when (outcome) {
            is DomainResult.Success      -> UseCaseEvent(useCaseName, "success", durationMs)
            is DomainResult.Error        -> UseCaseEvent(useCaseName, "error", durationMs, outcome.code?.toString())
            is DomainResult.Unauthorized -> UseCaseEvent(useCaseName, "unauthorized", durationMs)
            is DomainResult.Empty        -> UseCaseEvent(useCaseName, "empty", durationMs)
            is DomainResult.Loading      -> return  // não rastreado — estado transitório
        }
        analytics.track(event)
    }
}
```

```kotlin
// :core:analytics/src/main/java/com/bina/analytics/event/UseCaseEvent.kt
package com.bina.analytics.event

data class UseCaseEvent(
    val useCaseName: String,
    val outcome: String,
    val durationMs: Long,
    val errorCode: String? = null
) : AnalyticsEvent {
    override val name = "use_case_completed"
    override val properties = buildMap {
        put("use_case", useCaseName)
        put("outcome", outcome)
        put("duration_ms", durationMs.toString())
        errorCode?.let { put("error_code", it) }
    }
}
```

`:core:analytics` precisa adicionar `:core:domain` às suas dependências:
```kotlin
// :core:analytics/build.gradle.kts
dependencies {
    implementation(project(":core:domain"))
}
```

---

## Migração dos Use Cases existentes

### Antes

```kotlin
class GetCharacterDetailsUseCase(
    private val repository: CharacterDetailsRepository
) {
    suspend operator fun invoke(id: Int): NetworkResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(id)
}
```

### Depois

```kotlin
class GetCharacterDetailsUseCase(
    private val repository: CharacterDetailsRepository,
    observer: UseCaseObserver = NoOpUseCaseObserver   // injetável, default no-op
) : ObservableUseCase<Int, CharacterDetailsDomain>(observer) {

    override suspend fun execute(params: Int): DomainResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(params)
}
```

Mudanças mecânicas por use case:
1. Adicionar `observer: UseCaseObserver = NoOpUseCaseObserver` no construtor
2. Trocar `class X` por `class X(...) : ObservableUseCase<P, T>(observer)`
3. Renomear `invoke` para `execute` e remover `operator`
4. Trocar tipo de retorno de `NetworkResult<T>` para `DomainResult<T>`

### Use cases em escopo desta migração

| Use Case | Feature | Params | Retorno atual |
|----------|---------|--------|---------------|
| `GetCharacterDetailsUseCase` | `:feature:character_details` | `Int` | `NetworkResult<CharacterDetailsDomain>` |
| `GetEpisodesUseCase` | `:feature:character_details` | `List<Int>` | `NetworkResult<List<EpisodeDomain>>` |
| `LoginUseCase` | `:feature:auth` | `LoginParams` | a verificar |
| `SendMessageUseCase` | `:feature:chat` | `MessageParams` | a verificar |
| `CheckModelAvailabilityUseCase` | `:feature:chat` | `NoParams` | a verificar |

### Fora do escopo (retornam Flow/PagingData)

| Use Case | Motivo |
|----------|--------|
| `GetCharactersUseCase` | Retorna `Flow<PagingData<CharacterDomain>>` — ciclo de vida de Paging |

---

## Convenção de Teste

### Use Case — `NoOpUseCaseObserver` como default

```kotlin
@Test
fun `retorna Success quando repositório retorna dados`() = runTest {
    val domain = CharacterDetailsDomain(id = 1, name = "Rick")
    coEvery { repository.getCharacterDetails(1) } returns DomainResult.Success(domain)

    // Observer não precisa ser passado — default é NoOp
    val useCase = GetCharacterDetailsUseCase(repository)
    val result = useCase(1)

    assertIs<DomainResult.Success<CharacterDetailsDomain>>(result)
    assertEquals(domain, result.data)
}
```

### Use Case — verificar que o observer é chamado

```kotlin
@Test
fun `notifica observer com outcome correto`() = runTest {
    val observer = mockk<UseCaseObserver>(relaxed = true)
    val domain = CharacterDetailsDomain(id = 1, name = "Rick")
    coEvery { repository.getCharacterDetails(1) } returns DomainResult.Success(domain)

    val useCase = GetCharacterDetailsUseCase(repository, observer)
    useCase(1)

    verify { observer.onStart("GetCharacterDetailsUseCase") }
    verify { observer.onComplete("GetCharacterDetailsUseCase", any<DomainResult.Success<*>>(), any()) }
}
```

### `AnalyticsUseCaseObserver` — verificar eventos gerados

```kotlin
@Test
fun `rastreia evento de erro com código`() {
    val analytics = mockk<AnalyticsTracker>(relaxed = true)
    val performance = mockk<PerformanceTracker>(relaxed = true)
    val observer = AnalyticsUseCaseObserver(analytics, performance)

    observer.onComplete("GetCharacterDetailsUseCase", DomainResult.Error("Not found", 404), 120L)

    verify {
        analytics.track(match { event ->
            event is UseCaseEvent &&
            event.outcome == "error" &&
            event.errorCode == "404"
        })
    }
}
```

---

## Diagrama de dependências (pós-implementação)

```
:feature:X:domain
    GetXUseCase extends ObservableUseCase
         ↓ depends on
    :core:domain
         UseCase, ObservableUseCase, UseCaseObserver, NoOpUseCaseObserver, DomainResult
         (sem dependências externas)

:core:analytics
    AnalyticsUseCaseObserver implements UseCaseObserver
         ↓ depends on
    :core:domain  (UseCaseObserver, DomainResult)
    [AnalyticsTracker, PerformanceTracker — próprios]

:app (DI)
    bind UseCaseObserver → AnalyticsUseCaseObserver
    inject em cada GetXUseCase via Hilt/DI
```

---

## Estratégia de Migração

| Fase | Escopo | Branch |
|------|--------|--------|
| 1 | Criar contratos em `:core:domain`: `UseCase`, `UseCaseObserver`, `NoOpUseCaseObserver`, `ObservableUseCase` | `feat/core-domain-observable-use-case` |
| 2 | Criar `AnalyticsUseCaseObserver` e `UseCaseEvent` em `:core:analytics` | mesma branch |
| 3 | Migrar `:feature:character_details` — 2 use cases | `feat/character-details-observable-use-case` |
| 4 | Migrar `:feature:auth` e `:feature:chat` | PRs por feature |
| 5 | Wiring de DI no `:app` — injetar `AnalyticsUseCaseObserver` | junto ao último PR de feature |

As fases 3-4 podem rodar independentes após a fase 2 ser mergeada.

---

## O que NÃO muda

- Use cases de Paging (`GetCharactersUseCase`) — sem alteração
- ViewModels — continuam chamando `useCase(params)` via `invoke`, sem saber de `ObservableUseCase`
- Repositórios — sem alteração
- `DomainResult` — sem alteração