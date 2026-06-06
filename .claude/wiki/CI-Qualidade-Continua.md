# CI — Qualidade Contínua

O pipeline de CI roda em todo PR e push para `master`/`develop` via GitHub Actions.

---

## Jobs

### 1. Análise Estática (`static-analysis`)

| Step | O que faz |
|------|-----------|
| `./gradlew detekt` | Analisa o código com as regras de `config/detekt/detekt.yml` |
| Reviewdog | Posta os achados do Detekt como comentários **inline no diff do PR** |
| `./gradlew lint` | Executa o Android Lint em todos os módulos |

**Reviewdog** só atua em PRs (`filter-mode=added`) — só comenta em linhas que foram alteradas no PR, sem poluir com issues antigas.

### 2. Testes e Cobertura (`test`)

| Step | O que faz |
|------|-----------|
| `testDebugUnitTest` | Roda todos os testes unitários |
| `jacocoTestReport` + `jacocoFullReport` | Gera relatório de cobertura por módulo e agregado |
| Codecov | Sobe o XML para o [dashboard do Codecov](https://codecov.io/gh/sabinabernardes/RickAndMortyAIAgent) e posta comentário no PR com o link do relatório e diff de cobertura |
| Roborazzi | Verifica screenshot goldens — falha se houver diff visual |

### 3. Coverage Gate (`coverage-gate`)

Só roda em PRs. Usa `madrapps/jacoco-report` para:
- Exigir mínimo de **60% de cobertura geral**
- Exigir mínimo de **60% nos arquivos alterados no PR**
- Postar comentário com resumo de cobertura

---

## Ferramentas

### Codecov

- **Dashboard**: [codecov.io/gh/sabinabernardes/RickAndMortyAIAgent](https://codecov.io/gh/sabinabernardes/RickAndMortyAIAgent)
- **O que mostra**: cobertura linha a linha por arquivo, histórico de cobertura, diff entre branches
- **Secret necessário**: `CODECOV_TOKEN` em _Settings → Secrets → Actions_
- **Badge token**: obter em _Codecov → Settings → Badge_ (diferente do upload token)

### Reviewdog + Detekt

- Não precisa de token extra — usa o `GITHUB_TOKEN` nativo do Actions
- Saída XML habilitada em todos os módulos via `allprojects` no `build.gradle.kts`
- Só comenta em linhas novas/alteradas no PR

---

## Secrets necessários

| Secret | Onde obter | Usado por |
|--------|------------|-----------|
| `CODECOV_TOKEN` | codecov.io → Settings → General | Upload de cobertura |
| `GEMINI_API_KEY` | Google AI Studio | Testes e build |
| `GITHUB_TOKEN` | Automático (Actions) | Reviewdog, coverage gate |

---

## Versões das Actions

Todas as GitHub Actions usam **v5** (Node.js 24):

```
actions/checkout@v5
actions/setup-java@v5
actions/cache@v5
actions/upload-artifact@v5
actions/download-artifact@v5
codecov/codecov-action@v5
reviewdog/action-setup@v1
madrapps/jacoco-report@v1.6.1
```