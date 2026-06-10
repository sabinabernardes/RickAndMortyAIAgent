# SDD — Response Monad em `:core:network`

## Contexto

O `NetworkResult<T>` atual carrega apenas `data: T` no caso de sucesso e um `Throwable` genérico no caso de erro. Isso é insuficiente para dois cenários comuns em APIs REST:

1. **Headers de resposta** — informações como `X-Request-Id`, `X-RateLimit-Remaining`, `Link` (paginação via header) ficam inacessíveis porque o Retrofit é configurado para retornar `T` diretamente, descartando o envelope `Response<T>`.
2. **Erros de negócio** — respostas HTTP 4xx com body (ex: `422 Unprocessable Entity`, `409 Conflict`) são todas mapeadas para `NetworkResult.Error(HttpException)`, sem distinção semântica entre "servidor caiu" e "email já cadastrado".

Este SDD especifica a evolução do `NetworkResult` e do `safeApiCall` para suportar esses dois casos, mantendo compatibilidade com os call sites existentes.

---

## Estado Atual vs Estado Alvo

### Antes

```
Retrofit → T (body diretamente)
safeApiCall → NetworkResult.Success(data: T)
              NetworkResult.Error(exception: Throwable)  ← cobre 4xx e 5xx e IOExceptions
```

### Depois

```
Retrofit → Response<T> (envelope com headers + status)
safeApiCall → NetworkResult.Success(data: T, headers: Headers)
              NetworkResult.BusinessError(code: Int, message: String)  ← 4xx com body
              NetworkResult.NetworkError(exception: Throwable)          ← 5xx, timeout, IO
```

---

## Contrato de Dados

### `ResponseEnvelope<T>`

```kotlin
data class ResponseEnvelope<T>(
    val data: T,
    val headers: okhttp3.Headers
)
```

Carregado dentro de `Success`. Expõe os headers sem poluir o tipo de dado de domínio (`T` permanece limpo).

### `NetworkResult<T>` atualizado

```kotlin
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val envelope: ResponseEnvelope<T>) : NetworkResult<T>()
    data class BusinessError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val exception: Throwable) : NetworkResult<Nothing>()
    object Empty : NetworkResult<Nothing>()
    object Unauthorized : NetworkResult<Nothing>()
}
```

**Por que `BusinessError` separado de `NetworkError`:**
- Erros de negócio (4xx) são previstos e tratáveis pelo domínio (mostrar mensagem ao usuário)
- Erros de infraestrutura (5xx, timeout) são imprevistos e tratados de forma genérica (retry, fallback)
- Misturá-los força o call site a inspecionar `HttpException.code()` — lógica de protocolo HTTP vazando para a UI

**Por que manter `Unauthorized` separado de `BusinessError`:**
- `401` é transversal a todos os módulos — pode disparar logout automático
- Não deve passar pelo `when` de cada feature como um caso de negócio comum

### Extension property de compatibilidade

Para não quebrar todos os call sites de uma vez:

```kotlin
val <T> NetworkResult.Success<T>.data: T get() = envelope.data
```

Call sites existentes que acessam `result.data` continuam compilando sem alteração. A migração para `result.envelope.headers` fica opcional e incremental.

---

## `safeApiCall` atualizado

Passa a receber `Response<T>` do Retrofit em vez de `T`:

```kotlin
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> = try {
    val response = call()
    when {
        response.isSuccessful -> {
            val body = response.body()
            if (body != null)
                NetworkResult.Success(ResponseEnvelope(body, response.headers()))
            else
                NetworkResult.Empty
        }
        response.code() == HTTP_UNAUTHORIZED -> NetworkResult.Unauthorized
        response.code() in HTTP_CLIENT_ERROR_RANGE ->
            NetworkResult.BusinessError(response.code(), response.errorBody()?.string() ?: response.message())
        else -> NetworkResult.NetworkError(HttpException(response))
    }
} catch (e: CancellationException) {
    throw e
} catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
    NetworkResult.NetworkError(e)
}

private const val HTTP_UNAUTHORIZED = 401
private val HTTP_CLIENT_ERROR_RANGE = 400..499
```

**Mudança nos DataSources:** as interfaces do Retrofit precisam retornar `Response<T>` em vez de `T`:

```kotlin
// Antes
@GET("character/{id}")
suspend fun getCharacter(@Path("id") id: Int): CharacterDto

// Depois
@GET("character/{id}")
suspend fun getCharacter(@Path("id") id: Int): Response<CharacterDto>
```

Isso é feito na camada de `DataSource` — a interface do `Repository` não muda.

---

## Padrão de Implementação por Camada

### Repositório

```kotlin
override suspend fun getCharacterDetails(id: Int): NetworkResult<CharacterDetailsDomain> =
    safeApiCall { dataSource.getCharacterDetails(id) }
        .mapSuccess { CharacterDetailsMapper.toDomain(it) }
```

O mapeamento de DTO → Domain sai do `safeApiCall` e vai para um operador `mapSuccess`:

```kotlin
fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(ResponseEnvelope(transform(envelope.data), envelope.headers))
    else -> @Suppress("UNCHECKED_CAST") (this as NetworkResult<R>)
}
```

### ViewModel

```kotlin
when (val result = useCase(id)) {
    is NetworkResult.Success -> {
        val character = result.data   // extension property — compatível
        val requestId = result.envelope.headers["X-Request-Id"]  // novo acesso opcional
        _uiState.value = UiState.Success(uiMapper.map(character))
    }
    is NetworkResult.BusinessError -> {
        _uiState.value = UiState.Error("Erro ${result.code}: ${result.message}")
    }
    is NetworkResult.NetworkError -> {
        _uiState.value = UiState.Error("Sem conexão")
    }
    is NetworkResult.Unauthorized -> {
        // dispara evento global de logout — tratado pelo NavGraph
    }
    else -> _uiState.value = UiState.Error(null)
}
```

---

## Convenção de Teste

### Repositório — construir `ResponseEnvelope` nos mocks

```kotlin
// Sucesso com headers
coEvery { dataSource.getCharacterDetails(1) } returns Response.success(
    characterDto,
    okhttp3.Headers.headersOf("X-Request-Id", "abc123")
)
val result = repository.getCharacterDetails(1)
assertTrue(result is NetworkResult.Success)

// Erro de negócio
coEvery { dataSource.getCharacterDetails(99) } returns Response.error(
    404,
    "{}".toResponseBody()
)
val result = repository.getCharacterDetails(99)
assertTrue(result is NetworkResult.BusinessError)
assertEquals(404, (result as NetworkResult.BusinessError).code)
```

### ViewModel — mocks continuam iguais

```kotlin
coEvery { useCase(id) } returns NetworkResult.Success(
    ResponseEnvelope(domain, okhttp3.Headers.headersOf())
)
```

---

## Estratégia de Migração

| Fase | Escopo | Branch sugerida |
|------|--------|-----------------|
| 1 | Adicionar `ResponseEnvelope`, `mapSuccess`, extension `.data`, atualizar `safeApiCall` | `feat/network-response-monad` |
| 2 | Migrar DataSources para `Response<T>` | mesma branch |
| 3 | Migrar ViewModels para tratar `BusinessError` por feature | PRs separados por feature |

A extension `.data` permite que os PRs de feature sejam mergeados independentemente, sem urgência de migrar todos de uma vez.

---

## O que não muda

- `NetworkResult.Loading` — continua gerenciado pelo ViewModel, nunca retornado por `suspend`
- `NetworkResult.Empty` — semântica preservada para listas vazias
- A interface dos `Repository` — continua retornando `NetworkResult<DomainType>`
- `Use Cases` — continuam propagando `NetworkResult<T>` sem modificação