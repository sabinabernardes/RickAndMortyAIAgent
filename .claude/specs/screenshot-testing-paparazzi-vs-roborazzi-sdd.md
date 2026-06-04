# SDD — Screenshot Testing: Paparazzi vs Roborazzi

**Módulos:** `:core:designsystem`, `:feature:home`, `:feature:character_details`, `:feature:chat`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-03  
**ADRs relacionadas:** ADR-020 (Detekt), ci-cd-quality-gates-sdd, ui-testing-strategy-sdd

---

## 1. Contexto e Problema

O projeto já tem duas camadas de testes rodando na JVM sem emulador:

| Camada | Ferramenta | O que cobre |
|--------|-----------|------------|
| Unitária | JUnit 4 + MockK + Turbine | ViewModels, UseCases, Repositories, Mappers |
| Comportamento UI | Robolectric + `createComposeRule()` | Estados de tela, ações do usuário, semântica |

O que **não existe ainda** é uma camada de **regressão visual** — testes que capturam um bitmap do componente ou tela e falham automaticamente se a aparência mudar. Sem isso:

- Uma mudança em `StatusBadge` que inverte as cores (verde ↔ vermelho) passa em todos os testes
- Ajuste de `SpacingTokens` que quebra o layout de `CardCharacter` não é detectado
- Refactor de `HomeScreen` que muda o visual do estado de erro não produz nenhum sinal de falha

Este documento avalia **Paparazzi** e **Roborazzi**, decide qual adotar e define o escopo de implementação.

---

## 2. Estado Atual dos Testes de UI

### Testes de comportamento existentes (Robolectric + `createComposeRule`)

```
core/designsystem/src/test/
├── StatusBadgeTest.kt       — assertIsDisplayed para Alive/Dead/Unknown
├── CardCharacterTest.kt     — assertIsDisplayed para nome, localização
└── DialogErrorTest.kt       — assertIsDisplayed para mensagem, botões

feature/home/src/test/
└── HomeScreenTest.kt        — Loading, Error, retry/dismiss callbacks

feature/character_details/src/test/
└── CharacterDetailsScreenTest.kt — Loading, Success, Error, voltar

feature/chat/src/test/
└── ChatScreenTest.kt        — Initializing, ModelUnavailable, Conversation, envio
```

**O padrão atual:**

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatusBadgeTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN alive status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
    }
}
```

Esses testes verificam **presença semântica** (o texto existe, o botão existe), mas não a **aparência visual** (cor, layout, espaçamento, ícone correto).

---

## 3. Os Candidatos

### 3.1 Paparazzi (Cash App)

Renderiza composables para bitmap na JVM usando **LayoutLib** — o mesmo motor do Android Studio Preview. Não depende de Robolectric.

**Como funciona:**

```kotlin
class StatusBadgeScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun statusBadge_alive() {
        paparazzi.snapshot {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
    }
}
```

Gera/compara golden em `src/test/snapshots/images/`. Para atualizar:

```bash
./gradlew :core:designsystem:recordPaparazziDebug
./gradlew :core:designsystem:verifyPaparazziDebug   # no CI
```

**Setup necessário por módulo:**

```kotlin
// build.gradle.kts
plugins { alias(libs.plugins.paparazzi) }
```

```toml
# libs.versions.toml
[plugins]
paparazzi = { id = "app.cash.paparazzi", version = "1.3.4" }
```

---

### 3.2 Roborazzi (takahirom)

Adiciona captura de screenshot ao Robolectric existente. Usa **Skia** como motor de renderização. Integra com `createComposeRule()` — o mesmo setup que já existe no projeto.

**Como funciona:**

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatusBadgeTest {                   // mesmo arquivo que já existe

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN alive status WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
        composeTestRule.onRoot().captureRoboImage()   // ← única linha adicionada
    }
}
```

Gera/compara golden em `src/test/screenshots/`. Para atualizar:

```bash
./gradlew :core:designsystem:recordRoborazziDebug
./gradlew :core:designsystem:verifyRoborazziDebug    # no CI
```

**Setup necessário por módulo:**

```toml
# libs.versions.toml
[versions]
roborazzi = "1.42.0"

[libraries]
roborazzi             = { module = "io.github.takahirom.roborazzi:roborazzi",              version.ref = "roborazzi" }
roborazzi-compose     = { module = "io.github.takahirom.roborazzi:roborazzi-compose",      version.ref = "roborazzi" }
roborazzi-junit4      = { module = "io.github.takahirom.roborazzi:roborazzi-junit4",       version.ref = "roborazzi" }

[plugins]
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
```

```kotlin
// build.gradle.kts
plugins { alias(libs.plugins.roborazzi) }

dependencies {
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit4)
}
```

---

## 4. Comparação Direta

| Dimensão | Paparazzi | Roborazzi |
|----------|-----------|-----------|
| **Motor de renderização** | LayoutLib (Android Studio Preview) | Skia via Robolectric |
| **Fidelidade visual** | Alta — mesmo motor do preview | Média-alta — pequenas diferenças em sombras/blur |
| **Integração com testes existentes** | Nenhuma — runner diferente, regra diferente | Total — adiciona 1 linha aos testes que já existem |
| **Setup por módulo** | Plugin Gradle separado | Plugin Gradle + 3 deps |
| **Infraestrutura adicional** | Nova (paralela ao Robolectric) | Zero — estende o que já existe |
| **Suporte a flow (screenshot mid-test)** | Não — apenas composables estáticos | Sim — captura em qualquer ponto do fluxo de comportamento |
| **Dark mode** | Via `deviceConfig` | Via `RickAndMortyTheme(useDarkTheme = true)` |
| **Velocidade de execução** | Rápido (JVM puro) | Levemente mais lento (Robolectric overhead) |
| **Maturidade** | Estável, Cash App em produção | Estável, Google recomenda em roadmap |
| **Arquivos de golden** | `src/test/snapshots/` | `src/test/screenshots/` |
| **Tamanho dos goldens** | Menores (componentes isolados) | Podem ser maiores (telas completas) |

---

## 5. Decisão: Roborazzi

### Por que não Paparazzi

O projeto já tem Robolectric + `createComposeRule()` em **todos os 4 módulos** com cobertura de UI. Adotar Paparazzi significa:

1. Criar uma **segunda infraestrutura de testes paralela** — duas runners, duas regras, dois plugins por módulo
2. Os testes existentes de comportamento (`HomeScreenTest`) **não podem ser reutilizados** — precisam ser reescritos com `PaparazziRule`
3. Paparazzi não consegue capturar screenshots **mid-flow** (após clicar, após animação, após mudar estado) — só composables estáticos
4. Dois workflows de golden: `recordPaparazzi` E `recordRoborazzi` se precisar dos dois

### Por que Roborazzi

1. **Custo zero de adoção nos testes existentes** — `captureRoboImage()` é a única linha nova
2. **Screenshots mid-flow** — captura o estado *depois* de `performClick()`, depois de uma transição de estado
3. Um único `recordRoborazziDebug` atualiza todos os goldens de todos os módulos
4. O CI já roda `testDebugUnitTest` que cobre todos os testes Robolectric — `verifyRoborazziDebug` é adicionado ao mesmo job

### Quando Paparazzi seria a escolha certa

Se o projeto não tivesse nenhum teste Robolectric e quisesse apenas snapshots de componentes isolados, Paparazzi seria preferível pela maior fidelidade visual. Para este projeto, a fidelidade extra não justifica a infraestrutura paralela.

---

## 6. O Que Testar

### 6.1 `:core:designsystem` — Componentes

Adicionar `captureRoboImage()` aos testes existentes. Cada `@Test` vira um snapshot.

| Componente | Testes existentes | Snapshots a adicionar |
|------------|------------------|----------------------|
| `StatusBadge` | Alive, Dead, Unknown | 3 light + 3 dark = 6 |
| `CardCharacter` | Nome + localização | 2 light + 2 dark = 4 |
| `CardCharacterSkeleton` | 1 | 1 light + 1 dark = 2 |
| `DialogError` | Mensagem + botões | 2 light + 2 dark = 4 |
| `SearchToolbar` | Vazio + com query | — (criar novos) |

**Total estimado:** ~18 goldens

### 6.2 `:feature:home` — Estados de tela

| Estado | Snapshot após... |
|--------|-----------------|
| Loading | `setContent` com `CharactersUiState.Loading` |
| Error | `setContent` com `CharactersUiState.Error("Sem conexão")` |

**Total estimado:** 2 light + 2 dark = 4 goldens

### 6.3 `:feature:character_details`

| Estado | Snapshot |
|--------|---------|
| Loading | Spinner visível |
| Success | Nome, status badge, imagem placeholder |
| Error | Mensagem de erro |

**Total estimado:** 3 light + 3 dark = 6 goldens

### 6.4 `:feature:chat`

| Estado | Snapshot |
|--------|---------|
| Initializing | Typing indicator |
| ModelUnavailable | Ícone de aviso + mensagem |
| Conversation vazia | Empty state icon |
| Conversation com mensagens | Bubbles user + AI |

**Total estimado:** 4 light + 4 dark = 8 goldens

---

## 7. Padrão de Implementação

### 7.1 Componente isolado (designsystem)

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatusBadgeTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN alive status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()          // ← adicionar
    }

    @Test
    fun `GIVEN alive status dark WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                StatusBadge(status = "Alive")
            }
        }
        composeTestRule.onRoot().captureRoboImage("StatusBadge_alive_dark")   // ← novo teste
    }
}
```

### 7.2 Tela de feature (mid-flow)

```kotlin
@Test
fun `GIVEN error state WHEN rendered THEN matches snapshot and error message displayed`() {
    composeTestRule.setContent {
        RickAndMortyTheme {
            HomeContent(
                uiState = CharactersUiState.Error("Sem conexão"),
                onCharacterClick = {},
                viewModel = fakeViewModel()
            )
        }
    }
    composeTestRule.onNodeWithText("Sem conexão").assertIsDisplayed()
    composeTestRule.onRoot().captureRoboImage()    // ← adicionar ao final
}
```

### 7.3 Nomeação de goldens

Roborazzi usa o nome do método de teste por padrão. Para dark mode, passar nome explícito:

```kotlin
composeTestRule.onRoot().captureRoboImage("StatusBadge_alive_dark")
```

Goldens ficam em `src/test/screenshots/` e são **commitados no repositório**.

---

## 8. Integração com CI

Adicionar ao job `static-analysis` no `android-ci.yml`:

```yaml
- name: Verify Roborazzi screenshots
  run: ./gradlew verifyRoborazziDebug
```

> **Não** é necessário `record` no CI — os goldens já estão commitados. O CI só verifica se houve regressão.

**Workflow para atualizar goldens localmente:**

```bash
# Regenera todos os goldens
./gradlew recordRoborazziDebug

# Commita os goldens atualizados
git add **/src/test/screenshots/
git commit -m "chore(screenshots): atualiza goldens após mudança visual intencional"
```

---

## 9. O Que NÃO entra nesta spec

| Fora do escopo | Motivo |
|----------------|--------|
| Testes de animação | Roborazzi captura estado estático — animações requerem emulador |
| Testes de scroll | Requer interação com lista real de paginação |
| Paparazzi em `core:designsystem` | Infraestrutura paralela — não justificada se Roborazzi cobre |
| Screenshot de tela inteira com Toolbar | Depende de `AndroidComposeTestRule` com Activity — escopo maior |

---

## 10. Ordem de Implementação

| Passo | O que fazer | Esforço |
|-------|------------|---------|
| 1 | Adicionar `roborazzi`, `roborazzi-compose`, `roborazzi-junit4` ao `libs.versions.toml` | Baixo |
| 2 | Aplicar plugin + deps em `:core:designsystem` | Baixo |
| 3 | Adicionar `captureRoboImage()` aos 3 testes existentes + dark mode | Baixo |
| 4 | Aplicar plugin + deps em `:feature:home`, `:feature:character_details`, `:feature:chat` | Baixo |
| 5 | Adicionar `captureRoboImage()` aos testes de tela existentes | Baixo |
| 6 | Rodar `./gradlew recordRoborazziDebug` — gerar todos os goldens | Baixo |
| 7 | Commitar goldens em `src/test/screenshots/` | Baixo |
| 8 | Adicionar `verifyRoborazziDebug` ao CI | Baixo |

**Esforço total estimado:** 1 dia de trabalho.

---

## 11. Referências

- Roborazzi: https://github.com/takahirom/roborazzi
- Roborazzi + Compose: https://github.com/takahirom/roborazzi#compose
- Paparazzi: https://github.com/cashapp/paparazzi
- Testes existentes: `core/designsystem/src/test/`, `feature/*/src/test/`
- Estratégia de testes UI anterior: `.claude/specs/ui-testing-strategy-sdd.md`