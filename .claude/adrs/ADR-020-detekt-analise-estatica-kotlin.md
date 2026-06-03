# ADR-020 — Detekt para análise estática Kotlin

**Status:** Aceito  
**Data:** 2026-06-03  
**Escopo:** CI/CD — qualidade de código em todos os módulos

---

## Contexto

O projeto não tem nenhuma ferramenta de análise estática além do Android Lint (que cobre recursos Android e uso de APIs, mas não estilo de código Kotlin, complexidade ciclomática ou code smells). Em times profissionais Android, análise estática Kotlin é esperada por padrão — a ausência é um sinal negativo em code reviews e entrevistas técnicas.

As opções disponíveis são KtLint (formatação), Detekt (análise + formatação) ou nenhuma ferramenta.

## Opções Avaliadas

### Opção A: KtLint standalone

KtLint é um linter de formatação Kotlin sem configuração. Aplica as regras do guia de estilo oficial Kotlin.

- **Prós:** Zero configuração, fácil de adicionar
- **Contras:** Cobre apenas formatação. Não detecta: funções muito longas, complexidade ciclomática alta, nomes mal escolhidos, imports não usados além do que o compilador já detecta, retornos de função desnecessariamente complexos

### Opção B: Detekt

Detekt é uma ferramenta de análise estática para Kotlin que cobre:
- Code smells (funções muito longas, complexidade ciclomática, nomes ruins)
- Formatação via plugin `detekt-formatting` (inclui as mesmas regras do KtLint)
- Regras configuráveis por projeto (`detekt.yml`)
- Suporte nativo a código Compose (regras `ignoreAnnotated` para `@Composable`, `@Preview`)

- **Prós:** Uma ferramenta substitui as duas; configurável para o contexto do projeto; relatório HTML com lista de violações
- **Contras:** Mais configuração inicial que KtLint puro; `detekt-formatting` pode conflitar com formatação do IDE se `autoCorrect = true` (evitado mantendo `autoCorrect = false` no CI)

### Opção C: Nenhuma ferramenta

- **Prós:** Zero overhead
- **Contras:** Sinal negativo em qualquer repositório profissional; code smells acumulam sem visibilidade

## Decisão

**Escolhida: Opção B — Detekt com `detekt-formatting`**

## Justificativa

Detekt resolve os dois problemas em uma ferramenta: formatação (via `detekt-formatting`, que usa as regras do KtLint internamente) e análise de qualidade. Adicionar KtLint + Detekt em separado seria redundância. A config `detekt.yml` fica versionada no repositório, tornando as decisões de estilo explícitas e auditáveis.

Ajustes necessários para o contexto Compose do projeto:

| Regra | Ajuste | Motivo |
|---|---|---|
| `complexity.LongParameterList` | `ignoreAnnotated: ['Composable']` | Composables recebem muitos parâmetros por design (slots, modificadores, callbacks) |
| `naming.FunctionNaming` | `functionPattern: '[a-zA-Z][a-zA-Z0-9]*'` | Composables usam PascalCase por convenção, diferente das funções normais |
| `style.MagicNumber` | `ignoreAnnotated: ['Preview', 'Composable']` | Previews e Composables usam valores numéricos para dimensões de layout |

`autoCorrect = false` no CI — o CI só reporta, não modifica arquivos. Correção automática é responsabilidade do desenvolvedor localmente.

## Consequências

- `gradle/libs.versions.toml` recebe `detekt = "1.23.7"`, plugin e library `detekt-formatting`
- `build.gradle.kts` (raiz) aplica o plugin em todos os subprojetos via bloco `subprojects {}`
- `config/detekt/detekt.yml` é criado com as regras ajustadas para Compose
- CI roda `./gradlew detekt` no job `static-analysis`
- Violações de Detekt bloqueiam o merge (via `build.maxIssues: 0`)
- Rodar localmente: `./gradlew detekt` ou `./gradlew :feature:home:detekt` por módulo