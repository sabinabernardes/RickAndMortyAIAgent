# ADR-019 — CI gate obrigatório antes do auto-merge

**Status:** Aceito  
**Data:** 2026-06-02  
**Escopo:** CI/CD — `.github/workflows/auto-merge.yml`

---

## Contexto

O projeto tem dois workflows do GitHub Actions:

- `android-ci.yml` — executa build, testes unitários, lint e cobertura JaCoCo em cada PR
- `auto-merge.yml` — habilita auto-merge com squash assim que o PR é aberto

O problema: `auto-merge.yml` tem `needs: []` — não depende de nenhum job. Isso significa que o `gh pr merge --auto --squash` é habilitado **antes** de o CI terminar. Se o CI falhar depois, o PR já foi mergeado.

Na prática, o auto-merge com squash só acontece quando todos os checks obrigatórios passam — mas isso depende da configuração de **branch protection rules** no GitHub. Sem branch protection configurada, o `--auto` faz o merge imediatamente.

## Opções Avaliadas

### Opção A: Adicionar `needs: [build]` no `auto-merge.yml`

```yaml
jobs:
  auto-merge:
    needs: [build]  # aguarda o job 'build' do android-ci.yml terminar
```

- **Problema:** `needs` só funciona entre jobs do mesmo workflow. `auto-merge.yml` e `android-ci.yml` são workflows separados — `needs` cross-workflow não existe no GitHub Actions.

### Opção B: Unificar os dois workflows — auto-merge como job final do `android-ci.yml`

```yaml
# android-ci.yml
jobs:
  build:
    # ... build, test, lint, coverage ...
  
  auto-merge:
    needs: [build]
    if: github.event_name == 'pull_request'
    steps:
      - run: gh pr merge --auto --squash "$PR_URL"
```

- **Prós:** O auto-merge só acontece se build+testes passaram; um único arquivo de workflow; clara relação de dependência
- **Contras:** Arquivo de CI fica mais longo; mistura lógica de build com lógica de merge

### Opção C: Manter workflows separados + configurar Branch Protection Rules no GitHub

Configurar no repositório: `Settings → Branches → Branch protection rules → master`:
- ✅ Require status checks to pass before merging
- ✅ Status checks obrigatórios: `Build e Teste Android` (nome do job em `android-ci.yml`)
- ✅ Require branches to be up to date before merging

Com branch protection ativa, o `gh pr merge --auto` do `auto-merge.yml` registra a intenção de auto-merge, mas o GitHub só executa o merge quando todos os checks obrigatórios passam.

- **Prós:** Workflows com responsabilidades separadas; o GitHub garante a segurança no nível do repositório, não da automação; funciona para merges manuais também
- **Contras:** Configuração fora do código (não é "infrastructure as code"); requer acesso de admin ao repositório

### Opção D: Mover auto-merge para job final do CI (Opção B) + Branch Protection como camada extra

- **Prós:** Defense in depth — CI gate no workflow E proteção no repositório
- **Contras:** Mais complexo; pode ser overkill para um projeto solo/pequeno time

## Decisão

**Escolhida: Opção B — mover auto-merge como job final do `android-ci.yml` com `needs: [build]`**

## Justificativa

A Opção B resolve o problema no código, sem depender de configuração manual do repositório que pode ser revertida ou esquecida. A relação `auto-merge needs build` fica explícita e auditável no histórico do repositório.

A Opção C (branch protection) é complementar e recomendada como camada extra, mas não substitui o gate no workflow — proteção do repositório pode ser desativada por um admin; o workflow não.

## Consequências

- `auto-merge.yml` é **removido**
- Um novo job `auto-merge` é adicionado ao final de `android-ci.yml` com `needs: [build]`
- O job só roda em eventos de `pull_request` (`if: github.event_name == 'pull_request'`)
- PRs com build/teste falhando não recebem auto-merge habilitado
- Recomendação: configurar branch protection rules no GitHub como camada adicional