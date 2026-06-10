# ADR-026 — Separação de `DomainResult` do `:core:network`

**Status:** Proposed  
**Data:** 2026-06-10  
**Autor:** Sabina Bernardes  
**Módulos afetados:** `:core:network`, `:core:domain` (novo), `:feature:character_details`, `:feature:home`, `:feature:chat`

---

## Contexto

O `NetworkResult<T>` foi introduzido para tipar resultados de chamadas de rede ([ADR-025](ADR-025-response-monad-network.md)). A implementação atual funciona bem na camada de dados, mas a classe vive em `:core:network`, que depende de `okhttp3.Headers`.

O problema: interfaces de repositório e use cases — que pertencem ao **domínio** — importam `NetworkResult` de `:core:network`:

```
:feature:character_details:domain → :core:network → okhttp3
```

Isso viola a regra de dependência da Clean Architecture: **domínio não pode depender de infraestrutura**. O domínio deveria ser o núcleo estável do sistema, sem conhecer detalhes de protocolo HTTP ou biblioteca de rede.

Efeitos práticos do problema atual:
- Substituir OkHttp/Retrofit forçaria mudanças nas interfaces de domínio
- Testes de domínio precisam do artefato `okhttp3` no classpath
- `ResponseEnvelope<T>` (que carrega `okhttp3.Headers`) vaza para o domínio mesmo que o domínio nunca use headers

---

## Decisão

Criar um módulo `:core:domain` contendo `DomainResult<T>` — um tipo de resultado puro, sem dependências de infraestrutura.

### Separação de responsabilidades

| Tipo | Módulo | Quem usa |
|------|--------|----------|
| `NetworkResult<T>` | `:core:network` | Camada de dados (repositórios impl, datasources) |
| `DomainResult<T>` | `:core:domain` | Domínio (repositórios interface, use cases, ViewModels) |

### `DomainResult<T>` — contrato do domínio

```kotlin
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val code: Int? = null) : DomainResult<Nothing>()
    object Loading : DomainResult<Nothing>()
    object Empty : DomainResult<Nothing>()
    object Unauthorized : DomainResult<Nothing>()
}
```

`Unauthorized` permanece em `DomainResult` porque é um conceito de domínio (sessão inválida), não de protocolo. O código HTTP `401` que o origina é um detalhe de implementação da camada de dados.

`Error` unifica `BusinessError` e `NetworkError` do `NetworkResult`. O domínio não precisa distinguir origem do erro (HTTP vs. rede) — só precisa de uma mensagem e, opcionalmente, um código para exibição.

### Mapeamento na camada de dados

O `RepositoryImpl` é o único lugar que conhece ambos os tipos:

```kotlin
// Extensão em :core:network ou :feature:X:data
fun <T> NetworkResult<T>.toDomain(): DomainResult<T> = when (this) {
    is NetworkResult.Success    -> DomainResult.Success(data)
    is NetworkResult.BusinessError -> DomainResult.Error(message, code)
    is NetworkResult.NetworkError  -> DomainResult.Error(exception.message ?: "Erro de rede")
    is NetworkResult.Unauthorized  -> DomainResult.Unauthorized
    is NetworkResult.Empty         -> DomainResult.Empty
    is NetworkResult.Loading       -> DomainResult.Loading
}
```

### Fluxo resultante

```
DataSource        → Response<DTO>
safeApiCall       → NetworkResult<DTO>    (interno ao :core:network)
RepositoryImpl    → NetworkResult<DTO>.mapSuccess(mapper::toDomain).toDomain()
                  → DomainResult<Domain>
Repository (iface)→ DomainResult<Domain>  (contrato público do domínio)
UseCase           → DomainResult<Domain>  (propaga sem transformar)
ViewModel         → DomainResult<Domain>  (faz o when para UiState)
```

---

## Alternativas Avaliadas

### Mover `NetworkResult` para um módulo neutro (ex: `:core:result`)
- Elimina a dependência de `okhttp3` do domínio se `ResponseEnvelope` for separado
- Mas os estados `BusinessError`/`NetworkError` continuam sendo conceitos HTTP no domínio
- Não resolve a separação semântica — domínio ainda conheceria erros de rede por nome

### Usar `kotlin.Result<T>` no domínio
- Nativo da stdlib, sem dependências
- Não suporta `Empty`, `Loading`, `Unauthorized` como estados de primeiro nível
- Forçaria `when (result.isSuccess)` + `result.exceptionOrNull()` — menos expressivo
- Descartado: perda de expressividade sem ganho real

### Manter o estado atual (NetworkResult no domínio)
- Sem custo de migração imediato
- Torna a troca de biblioteca de rede uma mudança de domínio — custo crescente
- Aceito como dívida técnica, não como decisão arquitetural

### Usar Arrow `Either<DomainError, T>` no domínio
- Mesmas razões do ADR-025: dependência pesada, curva de aprendizado
- Descartado

---

## Consequências

**Positivas:**
- Domínio compilável sem nenhuma dependência de HTTP/OkHttp/Retrofit
- Testes de use cases e repositórios interface são puramente unitários — sem mocks de biblioteca de rede
- Substituição de Retrofit/OkHttp não toca interfaces de domínio
- `DomainResult` é um contrato estável; `NetworkResult` pode evoluir livremente

**Negativas:**
- Novo módulo `:core:domain` para criar e manter
- Função `toDomain()` de mapeamento em cada feature (ou em `:core:network` como extensão)
- ViewModels precisam migrar de `NetworkResult` para `DomainResult` — mudança mecânica

---

## Links

- Spec: [domain-result-separation-sdd.md](../specs/domain-result-separation-sdd.md)
- ADR relacionado: [ADR-025](ADR-025-response-monad-network.md) — Response Monad em `:core:network`