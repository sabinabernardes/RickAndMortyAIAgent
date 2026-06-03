# ADR-016 — Convenção de naming para performance traces nas features

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulos:** `:feature:home`, `:feature:character_details`, `:feature:chat`

---

## Contexto

`PerformanceTracker.startTrace(name)` e `stopTrace(name)` usam strings como identificadores. Sem convenção, nomes como `"loadChars"`, `"HomeViewModel.getCharacters"`, `"home_load"` e `"SCREEN_LOAD_HOME"` poderiam coexistir, tornando impossível comparar traces entre features ou automatizar alertas futuros.

Também é preciso decidir **quais operações** merecem um trace — rastrear tudo é ruído; rastrear pouco perde visibilidade.

## Opções de Naming

### Opção A: `ClassName.methodName` (estilo Java)
```
HomeViewModel.getCharacters
CharacterDetailsViewModel.loadEpisodes
ChatViewModel.sendMessage
```
- **Prós:** Rastreável no código por grep
- **Contras:** Acopla o nome do trace ao nome da classe; renomear um ViewModel quebra a consistência histórica dos traces

### Opção B: `feature_operação` (snake_case)
```
home_screen_load
character_details_load
episodes_fetch
chat_response_time
```
- **Prós:** Independente de nomes de classe; legível em dashboards sem contexto de código; consistente com o naming dos `AnalyticsEvent.name` (ex: `"home_character_clicked"`)
- **Contras:** Requer convenção explícita documentada

### Opção C: Livre, sem convenção
- **Contras:** Impossível automatizar alertas ou comparar features sem padronização

## Decisão

**Escolhida: Opção B — `feature_operação` em snake_case**

## Justificativa

Os nomes dos `AnalyticsEvent` já seguem o padrão `feature_ação` (ex: `"home_character_clicked"`, `"chat_message_sent"`). Manter a mesma convenção para performance traces torna o sistema de observabilidade coerente — ao olhar logs, um nome como `[PERF] home_screen_load: 312ms` se encaixa naturalmente junto de `[EVENT] home_character_clicked`.

## Catálogo de traces desta iteração

| Trace | Feature | Início | Fim | O que mede |
|---|---|---|---|---|
| `home_screen_load` | `:feature:home` | `HomeViewModel.init` | primeiro `collect` com sucesso em `getCharacters()` | Tempo até exibir primeiro grid de personagens |
| `character_details_load` | `:feature:character_details` | `getCharacterDetails()` chamado | `CharacterDetailsUiState.Success` emitido | Tempo até exibir dados do personagem |
| `episodes_fetch` | `:feature:character_details` | Chamada a `getEpisodesUseCase()` | Sucesso ou erro dos episódios | Tempo de busca de episódios (operação secundária) |
| `chat_response_time` | `:feature:chat` | `sendMessageUseCase()` chamado | Resultado recebido (sucesso ou erro) | Tempo de resposta do modelo Gemini |

## Regras de uso

1. **`startTrace` sempre antes de `stopTrace`** — usar um `try/finally` ou capturar erros para garantir que o `stopTrace` seja chamado mesmo em caso de falha
2. **Nome deve ser idêntico** em `startTrace` e `stopTrace` — considerar extrair como constante `companion object`
3. **Um trace por operação** — não aninhar traces com o mesmo nome; para operações paralelas, usar nomes distintos
4. **`stopTrace` retorna a duração em ms** — pode ser passada ao logger para linha de log combinada: `logger.info(TAG, "episodes_fetch: ${duration}ms")`

## Consequências

- Cada ViewModel que usa `PerformanceTracker` define as constantes de nome em um `companion object`
- Nomes de trace são strings fixas — não interpolam variáveis (ex: não `"character_${id}_load"`)
- A duração retornada por `stopTrace` pode ser incluída no log `info` para diagnóstico sem necessidade de dashboard