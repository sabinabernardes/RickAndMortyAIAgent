# ADR-005 — Módulo :feature:chat sem dependência de :core:network

**Status:** Aceito
**Data:** 2026-05-29
**Feature:** `:feature:chat`

---

## Contexto

O projeto possui um módulo `:core:network` que centraliza o cliente Retrofit, interceptors e configuração de OkHttp para chamadas à API do Rick and Morty. Precisávamos decidir se o módulo `:feature:chat` deveria depender deste módulo.

## Decisão

**`:feature:chat` NÃO depende de `:core:network`.**

## Justificativa

O `:core:network` é construído para chamadas HTTP (Retrofit + OkHttp + interceptors de resiliência). O módulo de chat usa ML Kit GenAI — uma API local que se comunica com o serviço AICore do sistema operacional, não com um servidor HTTP.

Adicionar `:core:network` como dependência de `:feature:chat` seria:
1. **Acoplamento desnecessário** — o chat nunca usaria Retrofit, OkHttp ou os interceptors
2. **Violação do princípio de menor privilégio** — um módulo só deve depender do que realmente usa
3. **Confusão arquitetural** — sugeriria que o chat faz chamadas de rede quando não faz

## Consequências

- O grafo de dependências do `:feature:chat` é mais limpo e honesto
- Build times menores (menos código compilado desnecessariamente)
- Se futuramente um fallback cloud for adicionado, `:core:network` será adicionado como dependência naquele momento — não antes

## Diagrama de Dependências do Módulo

```
:feature:chat
    ├── :core:designsystem   (componentes e tokens de UI)
    ├── Koin                 (injeção de dependência)
    ├── Compose + Material3  (UI)
    └── mlkit:genai-prompt   (inferência local)
```