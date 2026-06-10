# ADR-027 — `ObservableUseCase` em `:core:domain`

**Status:** Proposed  
**Data:** 2026-06-10  
**Autor:** Sabina Bernardes  
**Módulos afetados:** `:core:domain` (novo), `:core:analytics`, `:feature:character_details`, `:feature:home`, `:feature:auth`, `:feature:chat`

---

## Contexto

Com a introdução de `:core:domain` ([ADR-026](ADR-026-domain-result-separation.md)), o módulo define `DomainResult<T>` como contrato de retorno dos use cases. O próximo passo é padronizar a estrutura dos use cases em si.

Dois problemas existem no estado atual:

1. **Sem contrato comum:** cada use case é uma classe independente sem interface ou herança. Não é possível instrumentar, interceptar ou decorar chamadas de forma genérica.

2. **Sem observabilidade por padrão:** `:core:analytics` tem `PerformanceTracker` e `AnalyticsTracker`, mas nenhum use case os usa. Medir duração e resultado de chamadas exigiria boilerplate em cada classe.

---

## Decisão

Introduzir em `:core:domain`:

- `UseCase<P, R>` — interface contrato para use cases com resultado `DomainResult`
- `UseCaseObserver` — interface de monitoramento definida no domínio, **sem implementação**
- `NoOpUseCaseObserver` — implementação vazia para uso em testes e como default
- `ObservableUseCase<P, T>` — classe abstrata que instrumenta automaticamente qualquer use case que a estenda

`:core:analytics` fornece `AnalyticsUseCaseObserver`, que implementa `UseCaseObserver` usando os trackers existentes.

### Por que a interface do observador vive em `:core:domain`

`:core:domain` define o contrato e não conhece nenhuma implementação. Isso preserva a inversão de dependência:

```
:feature:X:domain  →  :core:domain   (UseCase, ObservableUseCase, UseCaseObserver)
:core:analytics    →  :core:domain   (implementa UseCaseObserver)
:app               →  :core:analytics  (provê AnalyticsUseCaseObserver via DI)
```

`:core:domain` não depende de `:core:analytics`. O domínio não sabe quem o observa.

### Escopo: apenas use cases com `DomainResult`

Use cases que retornam `Flow<PagingData<T>>` (ex: `GetCharactersUseCase`) **não entram neste padrão**. Paging tem ciclo de vida próprio — a chamada real ocorre de forma lazy ao coletar o Flow. Instrumentar esse padrão requer abordagem diferente (interceptor no `PagingSource`), não coberta aqui.

---

## Alternativas Avaliadas

### Decorator externo (sem herança)
- Envolver use cases em um `MonitoredUseCase(delegate, observer)` na camada de DI
- Não requer herança; mais composável
- Mas exige que cada binding no DI lembre de aplicar o decorator — fácil de esquecer
- Descartado por falta de garantia estrutural

### Coroutines `CoroutineContext` element para propagação
- Observer como elemento de contexto de coroutine, propagado automaticamente
- Elegante para tracing distribuído
- Complexidade desnecessária para o escopo atual
- Pode ser adicionado depois sem quebrar o contrato

### Kotlin Multiplatform ready (sem `System.currentTimeMillis`)
- Usar `TimeSource.Monotonic` da stdlib em vez de `System.currentTimeMillis`
- Mais preciso para medição de duração, sem drift de relógio
- Adotado na spec — `TimeSource.Monotonic.markNow()` é puro Kotlin

---

## Consequências

**Positivas:**
- Qualquer use case que estende `ObservableUseCase` é monitorado automaticamente — sem boilerplate
- Testes de use cases não precisam de `UseCaseObserver` real — `NoOpUseCaseObserver` é o default
- `:core:domain` permanece sem dependências externas
- Monitoring pode ser adicionado/removido via DI sem tocar nos use cases

**Negativas:**
- Use cases existentes precisam refatorar de `class` para `extends ObservableUseCase` — mudança mecânica
- `execute()` como método protegido é um padrão de Template Method — menos idiomático que lambdas em Kotlin, mas testável e simples
- Use cases de Paging ficam fora do padrão — dois contratos diferentes para use cases

---

## Links

- Spec: [observable-use-case-sdd.md](../specs/observable-use-case-sdd.md)
- ADR anterior: [ADR-026](ADR-026-domain-result-separation.md) — módulo `:core:domain` e `DomainResult`
- Módulos de observabilidade: [ADRs 011-014](ADR-011-core-logging.md) — `:core:logging` e `:core:analytics`