# SDD — CI/CD com Quality Gates

## Contexto

O pipeline de CI existente (`android-ci.yml`) cobre build, testes unitários e lint num único job sequencial, mas não tem análise estática Kotlin, enforcement de cobertura de testes, nem paralelismo entre etapas independentes. Este documento descreve o estado alvo do pipeline após a adição dessas capacidades.

**ADRs relacionadas:** ADR-019 (CI gate antes do auto-merge), ADR-020 (Detekt), ADR-021 (coverage gate), ADR-022 (paralelismo de jobs)

---

## Estado Atual vs Estado Alvo

| Dimensão | Antes | Depois |
|---|---|---|
| Análise estática Kotlin | Apenas Android Lint | Detekt + Android Lint |
| Cobertura | Relatório gerado, sem gate | Gate obrigatório ≥ 60% |
| Estrutura de jobs | 1 job monolítico (build) | 4 jobs paralelos |
| Feedback em falha de lint | Após ~5 min (esperando testes) | ~2 min (job paralelo) |
| Visibilidade de cobertura | Artefato HTML para download | Comentário no PR |
| Badge no README | Ausente | Presente |

---

## Arquitetura do Pipeline

```
Trigger: pull_request → master
         push → master

┌─────────────────────────────┐   ┌──────────────────────────────────┐
│     static-analysis         │   │             test                 │
│                             │   │                                  │
│  ./gradlew detekt           │   │  ./gradlew testDebugUnitTest     │
│  ./gradlew lint             │   │  ./gradlew jacocoTestReport      │
│                             │   │  ./gradlew jacocoFullReport      │
│  Upload:                    │   │                                  │
│  - detekt-results/          │   │  Upload:                         │
│  - lint-results/            │   │  - test-results/                 │
│                             │   │  - jacoco-xml/                   │
└──────────────┬──────────────┘   └──────────────────┬─────────────┘
               │                                      │
               │                  ┌───────────────────▼─────────────┐
               │                  │         coverage-gate            │
               │                  │                                  │
               │                  │  madrapps/jacoco-report@v1.6.0  │
               │                  │  min-coverage-overall: 60        │
               │                  │  min-coverage-changed-files: 60  │
               │                  │                                  │
               │                  │  → Comenta no PR                 │
               │                  │  → Falha se < 60%                │
               │                  └──────────────────┬──────────────┘
               │                                      │
               └───────────────────┬──────────────────┘
                                   ▼
                    ┌──────────────────────────────┐
                    │          auto-merge           │
                    │  (só em pull_request)         │
                    │                              │
                    │  gh pr merge --auto --squash  │
                    └──────────────────────────────┘
```

---

## Ferramentas

### Detekt

Análise estática para código Kotlin. Detecta code smells, complexidade excessiva, violações de nomenclatura e problemas de formatação.

**Config:** `config/detekt/detekt.yml`  
**Versão:** 1.23.7  
**Plugin adicional:** `detekt-formatting` (regras de formatação equivalentes ao KtLint)

**Ajustes para Compose:**

| Regra | Configuração | Motivo |
|---|---|---|
| `complexity.LongParameterList` | `ignoreAnnotated: ['Composable']` | Composables têm muitos parâmetros por design |
| `naming.FunctionNaming` | aceita PascalCase | Convenção obrigatória para Composables |
| `style.MagicNumber` | `ignoreAnnotated: ['Preview', 'Composable']` | Dimensões numéricas são normais em UI |

**Rodar localmente:**
```bash
./gradlew detekt                          # todos os módulos
./gradlew :feature:home:detekt            # módulo específico
open build/reports/detekt/detekt.html     # relatório visual
```

---

### Android Lint

Verifica uso correto de APIs Android, recursos, acessibilidade e possíveis crashes.

**Rodar localmente:**
```bash
./gradlew lint
open app/build/reports/lint-results-debug.html
```

---

### JaCoCo + Coverage Gate

JaCoCo instrumenta os testes e gera relatório de cobertura por linha e instrução. O `jacocoFullReport` agrega os 5 módulos cobertos em um único XML.

**Módulos cobertos:** `:feature:home`, `:feature:chat`, `:feature:character_details`, `:feature:auth`, `:core:network`, `:core:navigation`, `:core:security`

**Threshold:** 60% de instruction coverage (global e por arquivo modificado no PR)

**Exclusões de cobertura (configuradas nos módulos):**
- Código gerado: `R.class`, `BuildConfig.*`, `Manifest*.*`
- DI: `**/di/**` (módulos Koin — estrutura, sem lógica testável)
- Os próprios testes: `**/*Test*.*`
- Infraestrutura Android não testável em JVM: `**/EncryptedPrefsStorage*` (depende de Android Keystore — coberta apenas por testes instrumentados)

**Rodar localmente:**
```bash
./gradlew testDebugUnitTest jacocoTestReport jacocoFullReport
open build/reports/jacoco/jacocoFullReport/html/index.html
```

---

## Gerenciamento de Secrets

| Secret | Usado em | Motivo |
|---|---|---|
| `GEMINI_API_KEY` | Job `test` | `BuildConfig.GEMINI_API_KEY` é necessário para compilar o módulo `:feature:chat` |
| `GITHUB_TOKEN` | Job `coverage-gate`, `auto-merge` | Criado automaticamente pelo GitHub Actions; sem configuração manual |

O job `static-analysis` **não precisa** de `GEMINI_API_KEY` — Detekt e Lint operam no código-fonte, não executam o app.

**Configurar o secret:**  
`github.com/sabinabernardes/RickAndMorty → Settings → Secrets and variables → Actions → New repository secret`  
Nome: `GEMINI_API_KEY`

---

## Artefatos Gerados

| Artefato | Job | Conteúdo |
|---|---|---|
| `detekt-results` | `static-analysis` | Relatório HTML com violações por arquivo |
| `lint-results` | `static-analysis` | Relatório HTML do Android Lint |
| `test-results` | `test` | Relatório JUnit por módulo |
| `jacoco-xml` | `test` | XML do `jacocoFullReport` (entrada para o coverage gate) |

Artefatos ficam disponíveis por 7 dias no GitHub Actions (padrão).

---

## Rodando o Pipeline Completo Localmente

```bash
# Equivalente ao que o CI executa (exceto o coverage gate, que depende do GitHub)
./gradlew detekt lint testDebugUnitTest jacocoTestReport jacocoFullReport \
  -PGEMINI_API_KEY=sua_chave_aqui

# Abrir os relatórios
open app/build/reports/detekt/detekt.html
open app/build/reports/lint-results-debug.html
open build/reports/jacoco/jacocoFullReport/html/index.html
```

---

## Arquivos Modificados

| Arquivo | Mudança |
|---|---|
| `gradle/libs.versions.toml` | Versão Detekt, plugin e library `detekt-formatting` |
| `build.gradle.kts` (raiz) | Plugin Detekt em `subprojects {}` |
| `config/detekt/detekt.yml` | Config Detekt com ajustes Compose |
| `.github/workflows/android-ci.yml` | 4 jobs paralelos + coverage gate |
| `README.md` | Badge de CI |