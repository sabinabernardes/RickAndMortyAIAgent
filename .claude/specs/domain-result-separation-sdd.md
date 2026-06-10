# SDD — Separação de `DomainResult` do `:core:network`

## Problema

A camada de domínio (interfaces de repositório, use cases) importa `NetworkResult<T>` de `:core:network`, que por sua vez depende de `okhttp3`. Domínio não pode depender de infraestrutura.

```
// ATUAL — viola Clean Architecture
import com.bina.network.NetworkResult  // ← infraestrutura no domínio

interface CharacterDetailsRepository {
    suspend fun getCharacterDetails(id: Int): NetworkResult<CharacterDetailsDomain>
}
```

---

## Estado Atual vs Estado Alvo

```
ATUAL
:feature:X:domain  ──depends──▶  :core:network  ──depends──▶  okhttp3
                                                               retrofit2

ALVO
:feature:X:domain  ──depends──▶  :core:domain   (Kotlin puro, sem deps externas)
:feature:X:data    ──depends──▶  :core:network  (impl detalhe — não vaza pro domínio)
:core:network      ──depends──▶  :core:domain   (usa DomainResult no mapeamento)
```

---

## Módulo novo: `:core:domain`

### `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.bina.domain"
    // mesma config dos outros módulos core
}

dependencies {
    // intencionalmente vazio — sem dependências externas
}
```

### `DomainResult.kt`

```kotlin
package com.bina.domain

sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val code: Int? = null) : DomainResult<Nothing>()
    object Loading : DomainResult<Nothing>()
    object Empty : DomainResult<Nothing>()
    object Unauthorized : DomainResult<Nothing>()
}
```

**Por que `Error` unifica `BusinessError` e `NetworkError` do `NetworkResult`:**  
O domínio não precisa saber a *origem* do erro — só precisa reagir a ele. A distinção "HTTP 4xx vs. timeout" é um detalhe de implementação da camada de dados.

**Por que `Unauthorized` é um conceito de domínio:**  
Representa sessão inválida — uma condição de negócio que pode disparar logout global. O código HTTP `401` que o origina é um detalhe de implementação.

---

## Mapeamento `NetworkResult` → `DomainResult`

Fica em `:core:network` como extensão interna, disponível para as camadas de dados das features:

```kotlin
// :core:network/src/main/java/com/bina/network/NetworkResultExt.kt
fun <T> NetworkResult<T>.toDomain(): DomainResult<T> = when (this) {
    is NetworkResult.Success       -> DomainResult.Success(data)
    is NetworkResult.BusinessError -> DomainResult.Error(message, code)
    is NetworkResult.NetworkError  -> DomainResult.Error(exception.message ?: "Erro de rede")
    is NetworkResult.Unauthorized  -> DomainResult.Unauthorized
    is NetworkResult.Empty         -> DomainResult.Empty
    is NetworkResult.Loading       -> DomainResult.Loading
}
```

`:core:network` precisa adicionar `:core:domain` como dependência para usar `DomainResult`.

---

## Padrão de Implementação por Camada

### Repositório — interface (domínio)

```kotlin
// ANTES
import com.bina.network.NetworkResult

interface CharacterDetailsRepository {
    suspend fun getCharacterDetails(id: Int): NetworkResult<CharacterDetailsDomain>
}

// DEPOIS
import com.bina.domain.DomainResult

interface CharacterDetailsRepository {
    suspend fun getCharacterDetails(id: Int): DomainResult<CharacterDetailsDomain>
}
```

### Repositório — implementação (dados)

```kotlin
// ANTES
override suspend fun getCharacterDetails(id: Int): NetworkResult<CharacterDetailsDomain> =
    safeApiCall { dataSource.getCharacterDetails(id) }
        .mapSuccess { CharacterDetailsMapper.toDomain(it) }

// DEPOIS
override suspend fun getCharacterDetails(id: Int): DomainResult<CharacterDetailsDomain> =
    safeApiCall { dataSource.getCharacterDetails(id) }
        .mapSuccess { CharacterDetailsMapper.toDomain(it) }
        .toDomain()
```

A cadeia `safeApiCall → mapSuccess → toDomain` mantém a separação clara:
1. `safeApiCall` cuida do protocolo HTTP
2. `mapSuccess` converte DTO → Domain
3. `toDomain` converte o wrapper de rede para o wrapper de domínio

### Use Case

```kotlin
// Não muda — propaga DomainResult sem transformar
class GetCharacterDetailsUseCase(private val repository: CharacterDetailsRepository) {
    suspend operator fun invoke(id: Int): DomainResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(id)
}
```

### ViewModel

```kotlin
// ANTES
when (val result = useCase(id)) {
    is NetworkResult.Success       -> ...
    is NetworkResult.BusinessError -> ...
    is NetworkResult.NetworkError  -> ...
    ...
}

// DEPOIS
when (val result = useCase(id)) {
    is DomainResult.Success       -> _uiState.value = UiState.Success(uiMapper.map(result.data))
    is DomainResult.Error         -> _uiState.value = UiState.Error(result.message)
    is DomainResult.Unauthorized  -> { /* dispara evento global de logout */ }
    is DomainResult.Empty         -> _uiState.value = UiState.Empty
    is DomainResult.Loading       -> _uiState.value = UiState.Loading
}
```

---

## Convenção de Teste

### Use Case — sem nenhuma dependência de rede

```kotlin
// Antes: precisava de ResponseEnvelope e okhttp3.Headers no teste
coEvery { repository.getCharacterDetails(1) } returns NetworkResult.Success(
    ResponseEnvelope(domain, okhttp3.Headers.headersOf())
)

// Depois: puro domínio
coEvery { repository.getCharacterDetails(1) } returns DomainResult.Success(domain)
coEvery { repository.getCharacterDetails(99) } returns DomainResult.Error("Não encontrado", 404)
coEvery { repository.getCharacterDetails(-1) } returns DomainResult.Unauthorized
```

### Repositório impl — o mapeamento é testado aqui

```kotlin
coEvery { dataSource.getCharacterDetails(1) } returns Response.success(
    characterDto,
    okhttp3.Headers.headersOf("X-Request-Id", "abc123")
)
val result = repository.getCharacterDetails(1)
assertIs<DomainResult.Success<CharacterDetailsDomain>>(result)
assertEquals(expectedDomain, result.data)
```

---

## Estratégia de Migração

| Fase | Escopo | Branch |
|------|--------|--------|
| 1 | Criar `:core:domain` com `DomainResult`; adicionar `toDomain()` em `:core:network` | `feat/core-domain-result` |
| 2 | Migrar `:feature:character_details` — repositórios, use cases, ViewModel, testes | `feat/character-details-domain-result` |
| 3 | Migrar `:feature:home` | `feat/home-domain-result` |
| 4 | Migrar `:feature:chat` | `feat/chat-domain-result` |
| 5 | Remover import de `NetworkResult` das features — lint rule opcional | junto ao PR da última feature |

Cada fase é um PR independente. A compilação não quebra entre fases porque `RepositoryImpl` pode retornar `NetworkResult` (fase 1→2) enquanto a interface ainda usa `NetworkResult`.

---

## O que NÃO muda

- `NetworkResult` — permanece em `:core:network`, sem alteração
- `safeApiCall` — sem mudança
- `ResponseEnvelope` e `mapSuccess` — sem mudança
- `NetworkResultExt` — recebe apenas a extensão `toDomain()` adicional
- DataSources — interface Retrofit não muda
- Lógica de negócio nos ViewModels — só troca o tipo do `when`

---

## Diagrama de Dependências (pós-migração)

```
:app
 └── :feature:character_details
      ├── domain/  ──▶  :core:domain     (DomainResult, sem deps externas)
      ├── data/    ──▶  :core:network    (NetworkResult, okhttp3, retrofit)
      └── data/    ──▶  :core:domain     (para converter NetworkResult → DomainResult)

:core:network  ──▶  :core:domain        (extensão toDomain)
:core:domain   ──▶  (nenhuma dep)
```