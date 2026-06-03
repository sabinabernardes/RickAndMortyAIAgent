# ADR-022 — Paralelismo de jobs no CI

**Status:** Aceito  
**Data:** 2026-06-03  
**Escopo:** CI/CD — estrutura do workflow

---

## Contexto

O workflow atual tem um único job `build` que executa sequencialmente: build → testes → lint → JaCoCo → upload de artefatos. Isso significa que qualquer etapa falha só após todas as anteriores completarem. Um erro de lint só é descoberto depois de esperar os testes. Com a adição do Detekt (ADR-020), o job ficaria ainda mais longo.

Jobs independentes podem rodar em paralelo no GitHub Actions sem custo adicional de configuração — cada job recebe seu próprio runner.

## Opções Avaliadas

### Opção A: Manter job monolítico

Adicionar Detekt como mais um passo no job `build` existente.

- **Prós:** Arquivo mais simples; uma única lista de steps
- **Contras:** Feedback mais lento (Detekt espera os testes terminarem); uma falha de análise estática não é distinguível de uma falha de teste nos logs; tempo total de CI = soma de todos os steps

### Opção B: Jobs paralelos com dependência explícita

Dividir em jobs com responsabilidades claras:

```
static-analysis (detekt + lint) ──────────────────────────────┐
                                                               ↓
test (testDebugUnitTest + jacocoTestReport) → coverage-gate → auto-merge
```

- `static-analysis` e `test` rodam em paralelo — sem dependência entre eles
- `coverage-gate` depende de `test` (precisa do XML JaCoCo gerado)
- `auto-merge` depende de `static-analysis` e `coverage-gate` — só habilita merge quando ambos passam

- **Prós:** Feedback mais rápido em cada dimensão; falhas isoladas por responsabilidade; wall time ≈ `max(static-analysis, test + coverage-gate)` em vez de soma
- **Contras:** Arquivo de workflow mais longo; dois runners em vez de um (custo marginal em repositório público = zero)

## Decisão

**Escolhida: Opção B — 4 jobs com paralelismo explícito**

## Justificativa

A separação de responsabilidades em jobs distintos tem dois benefícios práticos:

1. **Feedback mais rápido:** se o Detekt falhar, o desenvolvedor sabe em ~2 minutos, sem esperar os testes
2. **Sinal mais claro:** no painel do GitHub Actions, "Análise Estática ❌" é mais informativo que "Build e Teste ❌" quando o problema é um code smell

A remoção do `assembleDebug` separado elimina overhead real: `testDebugUnitTest` já compila o código como pré-requisito — rodar `assembleDebug` antes era compilar duas vezes.

**Mapeamento de jobs:**

| Job | Depende de | O que faz |
|---|---|---|
| `static-analysis` | — | `detekt`, `lint`, upload de relatórios |
| `test` | — | `testDebugUnitTest`, `jacocoTestReport`, `jacocoFullReport`, upload XML |
| `coverage-gate` | `test` | `madrapps/jacoco-report`, comenta cobertura no PR, falha se < 60% |
| `auto-merge` | `static-analysis`, `coverage-gate` | `gh pr merge --auto --squash` |

## Consequências

- `android-ci.yml` passa de 2 jobs para 4 jobs
- O antigo job `build` (monolítico) é removido
- Tempo estimado de CI em PR: ~4-5 min (paralelo) vs ~7-8 min (sequencial com Detekt)
- `auto-merge` só é habilitado quando análise estática E cobertura passam — gate mais rigoroso que o atual