# ADR-014 — Performance tracking com SystemClock.elapsedRealtime()

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulo:** `:core:analytics`

---

## Contexto

Para monitorar a performance do app (tempo de carregamento de personagens, latência da API Rick & Morty, tempo de resposta do Gemini no chat), é preciso medir durações de operações de forma confiável. A questão é qual API de clock usar e como estruturar a interface de medição.

Operações a medir:
- Carregamento da lista de personagens (Home)
- Carregamento de detalhes + episódios (CharacterDetails)
- Tempo de resposta do Gemini (Chat)
- Chamadas de rede via `ResilienceInterceptor`

## Opções Avaliadas

### Opção A: System.currentTimeMillis()
```kotlin
val start = System.currentTimeMillis()
// ... operação
val duration = System.currentTimeMillis() - start
```

- **Prós:** Familiar, sem imports extras
- **Contras:** **Não é monotônico** — pode regredir se o relógio do sistema for ajustado (NTP sync, mudança de fuso, modo avião). Uma medição negativa ou inflada por ajuste de relógio corromperia métricas de performance

### Opção B: System.nanoTime()
```kotlin
val start = System.nanoTime()
val durationMs = (System.nanoTime() - start) / 1_000_000
```

- **Prós:** Monotônico, alta resolução (nanosegundos), independente de fuso
- **Contras:** Não é comparável entre processos ou devices; em Android, `SystemClock.elapsedRealtime()` é a API recomendada pelo framework para medições de duração

### Opção C: SystemClock.elapsedRealtime()
```kotlin
val start = SystemClock.elapsedRealtime()
val durationMs = SystemClock.elapsedRealtime() - start
```

- **Prós:** **Monotônico** — nunca regride, não é afetado por ajustes de NTP ou fuso; retorna milissegundos diretamente (sem conversão); é a API recomendada pelo Android SDK para medir intervalos de tempo; alinhado com o que `Trace`, `Choreographer` e outras APIs do framework usam internamente
- **Contras:** Específico para Android (não testável em JVM puro sem mock) — mitigado pela interface que abstrai a chamada

### Opção D: Firebase Performance Monitoring
```kotlin
val trace = Firebase.performance.newTrace("character_load")
trace.start()
// ... operação
trace.stop()
```

- **Prós:** Dashboard automático no Firebase Console, percentis, alertas
- **Contras:** Dependência externa (contraria ADR-012); requer `google-services.json`; impossível na fase de exploração atual

## Decisão

**Escolhida: Opção C — `SystemClock.elapsedRealtime()` com interface `PerformanceTracker`**

## Justificativa

`SystemClock.elapsedRealtime()` é a escolha correta para medir durações em Android:
- Monotônico: medições nunca são negativas nem infladas por ajuste de clock
- Resolução em ms: suficiente para operações de UI e rede (não precisamos de nanosegundos)
- API oficial do framework Android para este uso

A interface `PerformanceTracker` abstrai a chamada nativa, permitindo substituição em testes (mock) e futura migração para Firebase Performance sem mudar os call sites.

**API definida:**

```kotlin
interface PerformanceTracker {
    fun startTrace(name: String)
    fun stopTrace(name: String): Long  // retorna duração em ms
}
```

Implementação concreta usa um `HashMap<String, Long>` interno para armazenar os `startTime` por nome de trace.

## Consequências

- `PerformanceTracker` vive em `:core:analytics` (junto com `AnalyticsTracker`, pois timing de produto é analytics)
- `LogcatPerformanceTracker` armazena start times em memória — sem persistência, só para debugging local
- `stopTrace` loga automaticamente via `AppLogger` (de `:core:logging`) com formato: `[PERF] <name>: <durationMs>ms`
- Para migrar para Firebase Performance: criar `FirebasePerformanceTracker : PerformanceTracker` que chama `Firebase.performance.newTrace(name)` — zero mudança nas features
- Traces não encerrados (chamou `startTrace` sem `stopTrace`) logam um warning — proteção contra vazamento de estado