# SDD — Mapeamento de Erros com NetworkResult

## Contexto

Antes deste SDD, o tratamento de erros de rede no módulo `:feature:character_details` era feito com `try/catch` genérico no ViewModel (`@Suppress("TooGenericExceptionCaught")`). Erros de rede, HTTP 401, e erros de parsing eram capturados de forma indiferenciada, e o `ResilienceInterceptor` bloqueava threads OkHttp com `Thread.sleep`.

Este documento especifica como **todos os módulos de feature** devem tratar erros de rede usando `NetworkResult`.

---

## Estado Anterior vs Estado Alvo

### Antes (padrão lançar/capturar)
```
DataSource → throws Exception (qualquer)
Repository → propagates throws
UseCase    → propagates throws
ViewModel  → try/catch(Exception) → UiState.Error(e.message)
```

Problemas:
- `@Suppress("TooGenericExceptionCaught")` — suprime aviso Detekt sem resolver o problema
- HTTP 401 tratado igual a IOException
- `ResilienceInterceptor` bloqueia thread pool OkHttp com `Thread.sleep`

### Depois (padrão NetworkResult)
```
DataSource → throws Exception (Retrofit)
Repository → safeApiCall { ... } → NetworkResult<T>
UseCase    → propagates NetworkResult<T>
ViewModel  → when (result) { is Success, is Error, is Unauthorized }
```

---

## `NetworkResult<T>` — Contrato de Dados

Definido em `:core:network`:

```kotlin
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()          // gerenciado pelo ViewModel, não retornado por suspend
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable) : NetworkResult<Nothing>()
    object Empty : NetworkResult<Nothing>()            // uso futuro: listas vazias semânticas
    object Unauthorized : NetworkResult<Nothing>()     // HTTP 401
}
```

**Nota:** `Loading` não deve ser retornado por funções `suspend` — é um estado de UI gerenciado pelo ViewModel antes de chamar o use case.

---

## `safeApiCall` — Wrapper Coroutines

Definido em `:core:network/NetworkCall.kt`:

```kotlin
suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(call())
} catch (e: HttpException) {
    if (e.code() == 401) NetworkResult.Unauthorized else NetworkResult.Error(e)
} catch (e: Exception) {
    NetworkResult.Error(e)
}
```

**Por que substituir `ResilienceInterceptor`:**
- `ResilienceInterceptor` usava `Thread.sleep` que bloqueia threads do pool OkHttp, reduzindo throughput em cenários de retry
- `safeApiCall` roda em coroutines — `delay()` pode ser adicionado sem bloquear threads
- Retry é uma preocupação separada que pode ser adicionada como decorator de `safeApiCall` quando necessário

---

## Padrão de Implementação por Camada

### Repositório (data)

```kotlin
class CharacterDetailsRepositoryImpl(
    private val dataSource: CharacterDetailsDataSource
) : CharacterDetailsRepository {
    override suspend fun getCharacterDetails(id: Int): NetworkResult<CharacterDetailsDomain> =
        safeApiCall { CharacterDetailsMapper.toDomain(dataSource.getCharacterDetails(id)) }
}
```

**Regra:** O repositório é o único lugar onde `safeApiCall` é chamado. DataSources continuam sendo passthrough simples que lançam exceções — o wrapper fica na borda da camada de dados.

### Use Case (domain)

```kotlin
class GetCharacterDetailsUseCase(private val repository: CharacterDetailsRepository) {
    suspend operator fun invoke(id: Int): NetworkResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(id)
}
```

**Regra:** Use cases propagam `NetworkResult<T>` sem modificar. Lógica de negócio que depende do resultado (transformações) deve ser aplicada dentro de `is NetworkResult.Success`.

### ViewModel (presentation)

```kotlin
fun getCharacterDetails(id: Int) {
    viewModelScope.launch {
        _uiState.value = CharacterDetailsUiState.Loading   // loading gerenciado aqui
        when (val result = getCharacterDetailsUseCase(id)) {
            is NetworkResult.Success -> {
                _uiState.value = CharacterDetailsUiState.Success(uiMapper.map(result.data))
            }
            is NetworkResult.Error -> {
                _uiState.value = CharacterDetailsUiState.Error(result.exception.message)
            }
            is NetworkResult.Unauthorized -> {
                _uiState.value = CharacterDetailsUiState.Error("Acesso não autorizado")
            }
            else -> _uiState.value = CharacterDetailsUiState.Error(null)
        }
    }
}
```

**Regra:** `when` deve ser exaustivo. Usar `else` para `Loading` e `Empty` que não são retornados por suspend functions mas precisam satisfazer o compilador.

---

## Aplicação por Módulo

| Módulo | Status | Observação |
|---|---|---|
| `:feature:character_details` | ✅ Implementado | Repository + UseCase + ViewModel |
| `:feature:home` | ⏳ Futuro | Usa Paging3 — integração com `NetworkResult` requer adaptador para `PagingSource` |
| `:feature:chat` | ⏳ Futuro | Usa Flow de streaming — padrão diferente |

**Por que não aplicar ao `:feature:home` neste PR:**
`PagingSource` tem seu próprio mecanismo de tratamento de erros (`LoadResult.Error`) que é incompatível com `NetworkResult`. Envolver `PagingSource` em `NetworkResult` adicionaria complexidade sem benefício, pois o estado de erro já é exposto pelo `LoadState` do Paging3.

---

## Convenção de Teste

### Repositório
```kotlin
// Sucesso
coEvery { dataSource.getCharacterDetails(1) } returns characterData
val result = repository.getCharacterDetails(1)
assertTrue(result is NetworkResult.Success)

// Erro
coEvery { dataSource.getCharacterDetails(any()) } throws RuntimeException("Not found")
val result = repository.getCharacterDetails(99)
assertTrue(result is NetworkResult.Error)
assertEquals("Not found", (result as NetworkResult.Error).exception.message)
```

### ViewModel
```kotlin
// Sucesso
coEvery { useCase(id) } returns NetworkResult.Success(domain)

// Erro
coEvery { useCase(id) } returns NetworkResult.Error(Exception(errorMessage))
```

**Não usar `throws` no mock de use cases** — o use case nunca lança exceção, sempre retorna `NetworkResult`.

---

## Adicionando Retry (Futuro)

Quando retry for necessário, adicionar como decorador de `safeApiCall`:

```kotlin
suspend fun <T> safeApiCallWithRetry(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000L,
    call: suspend () -> T
): NetworkResult<T> {
    repeat(maxRetries) { attempt ->
        val result = safeApiCall(call)
        if (result !is NetworkResult.Error || !isRetryable(result.exception)) return result
        delay(initialDelayMs * (2.0.pow(attempt).toLong()))
    }
    return safeApiCall(call)
}
```

Isso usa `delay()` (non-blocking) em vez de `Thread.sleep` (blocking), resolvendo o problema original do `ResilienceInterceptor`.