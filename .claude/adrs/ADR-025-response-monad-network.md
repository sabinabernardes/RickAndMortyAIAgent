# ADR-025 — Response Monad em `:core:network`

**Status:** Proposed  
**Data:** 2026-06-06  
**Autor:** Sabina Bernardes  
**Módulos afetados:** `:core:network`, `:feature:character_details`, `:feature:home`, `:feature:chat`

---

## Contexto

O `NetworkResult<T>` foi introduzido no SDD anterior ([network-result-error-mapping-sdd.md](../specs/network-result-error-mapping-sdd.md)) para eliminar `try/catch` genérico nos ViewModels e tipar os casos de erro de rede. A decisão funcionou bem para o fluxo principal.

Dois gaps surgiram com a maturidade do módulo:

1. **Headers inacessíveis:** o Retrofit é configurado para retornar `T` diretamente via `ConverterFactory`, descartando o envelope `retrofit2.Response<T>`. Informações de protocolo úteis — `X-Request-Id` para logging, `X-RateLimit-Remaining` para backoff, `Link` para paginação por header — são silenciosamente descartadas antes de chegar ao repositório.

2. **Erros 4xx sem semântica de negócio:** `HttpException` com código 404, 409, ou 422 são todos mapeados para `NetworkResult.Error(exception)`. A UI não consegue distinguir "recurso não encontrado" de "conflito de dados" sem inspecionar `HttpException.code()` — HTTP vazando para a camada de apresentação.

---

## Decisão

Evoluir `NetworkResult<T>` para o padrão **Response Monad**, com três mudanças coordenadas:

### 1. Introduzir `ResponseEnvelope<T>`

```kotlin
data class ResponseEnvelope<T>(val data: T, val headers: okhttp3.Headers)
```

`Success` passa a carregar `ResponseEnvelope<T>` em vez de `T`. Uma extension property `val Success<T>.data` garante compatibilidade com call sites existentes.

### 2. Separar `Error` em `BusinessError` e `NetworkError`

```kotlin
data class BusinessError(val code: Int, val message: String) : NetworkResult<Nothing>()
data class NetworkError(val exception: Throwable) : NetworkResult<Nothing>()
```

- **`BusinessError`** — respostas HTTP 4xx com body. Previstas, tratáveis por cada feature.
- **`NetworkError`** — 5xx, timeout, `IOException`. Imprevistos, tratados de forma genérica.
- **`Unauthorized`** — mantido separado por ser transversal (pode disparar logout global).

### 3. `safeApiCall` passa a receber `Response<T>` do Retrofit

DataSources atualizam suas interfaces para `suspend fun foo(): Response<T>`. O `safeApiCall` extrai headers e decide o tipo de resultado com base no código HTTP. Repositórios não mudam sua assinatura pública.

---

## Alternativas Avaliadas

### Manter `Error(Throwable)` e inspecionar `HttpException` no call site
- Funciona, mas vaza conceitos de HTTP (códigos de status) para a camada de apresentação
- Cada ViewModel que quiser distinguir 404 de 409 precisa importar `retrofit2.HttpException`
- Inconsistente: cada feature pode fazer a distinção de forma diferente

### Usar `Result<T>` da stdlib do Kotlin
- Não suporta estados semânticos como `Unauthorized`, `Empty`, `BusinessError`
- `Result.failure(e)` é tão genérico quanto o `Error(Throwable)` atual
- Incompatível com a convenção já estabelecida no projeto

### Arrow `Either<Error, T>`
- Tipagem mais expressiva para composição funcional
- Dependência externa pesada para o benefício obtido
- Curva de aprendizado alta; o projeto usa padrão imperativo nos ViewModels
- Descartado: o benefício não justifica introduzir uma dep de 3MB para este caso

### Interceptor OkHttp para capturar headers
- Alternativa para expor headers sem mudar `NetworkResult`
- Só funciona para logar ou armazenar em memória compartilhada — não resolve o acesso tipado por chamada
- Cria acoplamento temporal (o interceptor executa antes do repositório processar a resposta)

---

## Motivação das Escolhas

**`ResponseEnvelope` em vez de campos soltos em `Success`:** manter `data` e `headers` em um tipo nomeado permite adicionar campos futuros (ex: `statusCode`, `cacheControl`) sem alterar a assinatura de `Success`.

**Extension `.data` para compatibilidade:** a migração de 4 módulos de feature em paralelo geraria conflitos de merge desnecessários. A extension permite que cada feature migre no próprio PR, sem urgência.

**`mapSuccess` como operador de transformação:** o mapeamento de DTO → Domain que antes ficava dentro de `safeApiCall { mapper.toDomain(call()) }` precisa sair — o mapper não deve receber `Response<T>`. `mapSuccess` é o lugar natural para isso.

---

## Consequências

**Positivas:**
- Headers disponíveis por chamada, sem estado compartilhado
- Erros de negócio tipados — features podem reagir a 404/409/422 sem conhecer HTTP
- Migração incremental via extension `.data` — sem big bang
- `safeApiCall` continua sendo o único lugar com `try/catch` no módulo de dados

**Negativas:**
- DataSources precisam trocar `T` por `Response<T>` — mudança mecânica mas volumosa
- Testes de repositório precisam construir `Response.success(body, headers)` em vez de retornar `T` diretamente — levemente mais verbosos
- `mapSuccess` é um operador novo que a equipe precisa aprender a usar

---

## Estrutura de Arquivos

```
:core:network
├── NetworkResult.kt        ← adiciona BusinessError, NetworkError, ResponseEnvelope
├── NetworkCall.kt          ← safeApiCall recebe Response<T>
└── NetworkResultExt.kt     ← mapSuccess, extension .data (novo arquivo)
```

---

## Links

- Spec: [network-response-monad-sdd.md](../specs/network-response-monad-sdd.md)
- ADR anterior: [ADR-005](ADR-005-sem-core-network.md) — decisão de criar `:core:network`
- SDD relacionado: [network-result-error-mapping-sdd.md](../specs/network-result-error-mapping-sdd.md) — padrão atual de `NetworkResult`