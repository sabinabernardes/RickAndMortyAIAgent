# ADR-011 — Dois módulos core separados: :core:logging e :core:analytics

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulos:** `:core:logging`, `:core:analytics`

---

## Contexto

O projeto não tem nenhuma solução de logging estruturado, rastreamento de eventos ou monitoramento de performance. Ao adicionar essa capacidade, surgiu a questão de como organizá-la na estrutura de módulos existente:

```
core/
├── designsystem/
├── navigation/
└── network/
```

Há três naturezas de observabilidade a cobrir:
1. **Logging técnico** — logs de debug/info/warn/error para diagnóstico de desenvolvimento e produção
2. **Analytics de eventos** — rastreamento de ações do usuário para entender uso do produto
3. **Performance monitoring** — medição de tempo de operações críticas (carregamento de tela, latência de API, resposta da IA)

## Opções Avaliadas

### Opção A: Um único módulo `:core:observability`
Agrupa logging, analytics e performance em um só módulo.

- **Prós:** Menos módulos no grafo, um único `build.gradle.kts` para manter
- **Contras:** Mistura duas audiences completamente diferentes — logging serve ao engenheiro (diagnóstico técnico), analytics serve ao produto (comportamento do usuário). Essa mistura dificulta trocar backends independentemente no futuro (ex: DataDog para logs + Firebase Analytics para eventos)

### Opção B: Dois módulos separados — `:core:logging` e `:core:analytics`
- `:core:logging` → diagnóstico técnico: logs por nível, stack traces, contexto de erro
- `:core:analytics` → eventos de negócio: ações do usuário, navegação, uso de features, performance de produto

- **Prós:** Separação de responsabilidades alinhada com quem consome cada dado; backends podem evoluir independentemente; features que só precisam de logging não carregam código de analytics
- **Contras:** Dois módulos, dois `build.gradle.kts`, dois Koin modules

### Opção C: Adicionar ao `:core:network`
Logging já existe parcialmente no `NetworkClient` via `HttpLoggingInterceptor`.

- **Prós:** Zero novo módulo
- **Contras:** `HttpLoggingInterceptor` é logging de transporte HTTP, não logging de aplicação. Misturar logging de feature e analytics ao módulo de rede viola o princípio de responsabilidade única. Features que não usam rede (ex: futuras features offline) precisariam depender de `:core:network` sem motivo.

## Decisão

**Escolhida: Opção B — dois módulos separados `:core:logging` e `:core:analytics`**

## Justificativa

Logging e analytics têm audiences, ciclos de vida e backends diferentes:

| Dimensão | :core:logging | :core:analytics |
|---|---|---|
| Audience | Engenheiro/Ops | Produto/PM |
| Quando usar | Debug, erros, diagnóstico | Ações do usuário, fluxos |
| Backend futuro | DataDog, Logcat, Sentry | Firebase Analytics, Mixpanel |
| Nível de dado | Técnico (stack trace, tag) | Negócio (event name, properties) |

A separação não é overhead — é a documentação da arquitetura expressa em módulos.

## Consequências

- `settings.gradle.kts` recebe `include(":core:logging")` e `include(":core:analytics")`
- Features que precisam de logging importam só `:core:logging`
- Features que emitem eventos importam `:core:analytics` (que pode depender de `:core:logging` internamente)
- Trocar o backend de cada módulo é independente — sem impacto cross-cutting