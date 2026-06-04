# Screenshot Testing com Roborazzi

> Documentação técnica completa da implementação de screenshot testing neste projeto. Use como referência de como integrar Roborazzi em projetos Android com Robolectric.

---

## O que é Screenshot Testing?

Screenshot testing (ou visual regression testing) captura uma imagem de referência de um componente ou tela — o **golden** — e a compara pixel a pixel contra o estado atual em cada execução de CI. Se qualquer pixel mudar fora do esperado, o teste falha.

**Por que isso importa:**
- Um bug de layout em `padding` de 4dp pode passar por todos os testes de comportamento e só ser percebido visualmente em produção
- Refactorings de tema (cores, tipografia) são validados automaticamente
- O histórico de goldens no git é um histórico visual do produto

---

## Por que Roborazzi e não Paparazzi?

| Critério | Roborazzi | Paparazzi |
|----------|-----------|-----------|
| Infra necessária | Roda sobre Robolectric existente | Plugin JVM separado |
| Reutiliza testes | ✅ `captureRoboImage()` em testes existentes | ❌ Testes novos obrigatórios |
| Screenshot mid-flow | ✅ Em qualquer ponto da execução | ❌ Só composables isolados |
| Dark mode | ✅ Via `AppTheme(darkTheme = true)` | ✅ Via configuração |
| Renderização | Skia nativo via Robolectric | LayoutLib puro |
| Overhead de setup | ~5 min (plugin + 1 anotação) | ~30 min por módulo |

**Decisão:** Roborazzi, porque o projeto já tinha Robolectric em todos os módulos. Adicionar screenshot testing foi literalmente uma linha por teste. Ver [ADR-023](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-023-roborazzi-screenshot-testing.md) para detalhes.

---

## Conceitos Fundamentais

### Golden (arquivo de referência)

Um golden é uma imagem PNG que representa "o que este componente deve parecer". É gerado uma vez com `recordRoborazziDebug` e commitado no repositório. A partir daí, `verifyRoborazziDebug` usa essa imagem como baseline.

```
src/test/snapshots/
  com.bina.designsystem.components.StatusBadgeTest.GIVEN alive status WHEN rendered THEN text is displayed.png
  com.bina.features.home.HomeScreenTest.GIVEN loading state WHEN rendered THEN no error dialog is shown.png
```

O nome do arquivo é gerado automaticamente a partir do nome completo do teste (package + classe + método).

### Record vs Verify

| Modo | Gradle task | Quando usar |
|------|-------------|-------------|
| Record | `recordRoborazziDebug` | Quando um golden não existe ainda, ou quando uma mudança visual é **intencional** |
| Verify | `verifyRoborazziDebug` | CI — falha se qualquer pixel diverge do golden |

**Fluxo correto:**
```
Mudança intencional de UI → record localmente → commitar novo golden → CI verifica
Mudança acidental de UI   → CI falha → investigar → corrigir código (não o golden)
```

---

## Setup Completo

### 1. `gradle/libs.versions.toml`

```toml
[versions]
roborazzi = "1.63.0"

[libraries]
roborazzi         = { group = "io.github.takahirom.roborazzi", name = "roborazzi",         version.ref = "roborazzi" }
roborazzi-compose = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose", version.ref = "roborazzi" }

[plugins]
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
```

> **Atenção:** O artifact `roborazzi-junit4` **não existe**. Use apenas `roborazzi` + `roborazzi-compose`.

### 2. `build.gradle.kts` raiz

```kotlin
plugins {
    // ...
    alias(libs.plugins.roborazzi) apply false
}
```

### 3. `build.gradle.kts` de cada módulo

```kotlin
plugins {
    // ...
    alias(libs.plugins.roborazzi)
}

android {
    // ...
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // NÃO use systemProperty para output dir — o plugin Gradle sobrescreve
        }
    }
}

// Configura onde os goldens são salvos (fora do build/ — versionável)
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/snapshots"))
}

dependencies {
    // ...
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
```

> **Armadilha comum:** `systemProperty("roborazzi.output.dir", ...)` em `testOptions.unitTests.all` **não funciona** — o plugin Roborazzi sobrescreve esse valor com o diretório da task Gradle. Use sempre a extensão `roborazzi { outputDir.set(...) }`.

---

## Escrevendo Testes com Screenshot

### Estrutura mínima

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)     // obrigatório para renderização pixel-accurate
class StatusBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN alive status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            AppTheme {
                StatusBadge(status = "Alive")
            }
        }

        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()  // captura o golden
    }
}
```

### O que é `@GraphicsMode(NATIVE)`?

Por padrão, o Robolectric usa um renderizador legado que produz imagens imprecisas (ou em branco). `@GraphicsMode(Mode.NATIVE)` ativa o **Skia** — o mesmo motor gráfico do Android real — via bibliotecas nativas empacotadas no Robolectric. Sem essa anotação, os goldens ficam incorretos ou vazios.

```kotlin
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MeuTesteDeUI { ... }
```

### Dark Mode

Roborazzi não tem configuração especial para dark mode — você passa `darkTheme = true` para o seu tema:

```kotlin
@Test
fun `GIVEN alive status WHEN dark mode THEN badge renders correctly`() {
    composeTestRule.setContent {
        AppTheme(darkTheme = true) {           // tema dark
            StatusBadge(status = "Alive")
        }
    }
    composeTestRule.onRoot().captureRoboImage()
}
```

Convenção de nomes de golden neste projeto:
- Light mode: nome padrão gerado pelo método de teste
- Dark mode: teste separado com sufixo `dark` no nome do método

### Screenshot mid-flow

Uma das vantagens do Roborazzi sobre Paparazzi é capturar screenshots **durante** a execução, não só do estado inicial:

```kotlin
@Test
fun `GIVEN loading state WHEN rendered THEN no error dialog is shown`() {
    composeTestRule.setContent {
        AppTheme {
            HomeContent(
                state = HomeUiState(isLoading = true, characters = emptyList()),
                onSearch = {},
                onCharacterClick = {}
            )
        }
    }

    // Captura o estado de loading
    composeTestRule.onRoot().captureRoboImage()

    // Afirmação comportamental continua normalmente
    composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    composeTestRule.onNodeWithTag("error_dialog").assertDoesNotExist()
}
```

---

## Goldens deste Projeto

Os goldens estão em dois lugares:
- `src/test/snapshots/` de cada módulo — usados pelo CI para verificação (`verifyRoborazziDebug`)
- `docs/screenshots/` na raiz do projeto — cópias para consulta rápida sem navegar nos módulos

### `:core:designsystem`

| Componente | Estado | Golden |
|------------|--------|--------|
| `StatusBadge` | Alive | `src/test/snapshots/com.bina.designsystem.components.StatusBadgeTest.GIVEN alive status WHEN rendered THEN text is displayed.png` |
| `StatusBadge` | Dead | `src/test/snapshots/com.bina.designsystem.components.StatusBadgeTest.GIVEN dead status WHEN rendered THEN text is displayed.png` |
| `StatusBadge` | Unknown | `src/test/snapshots/com.bina.designsystem.components.StatusBadgeTest.GIVEN unknown status WHEN rendered THEN text is displayed.png` |
| `CardCharacter` | Nome exibido | `src/test/snapshots/com.bina.designsystem.components.CardCharacterTest.GIVEN character data WHEN rendered THEN name is displayed.png` |
| `DialogError` | Mensagem exibida | `src/test/snapshots/com.bina.designsystem.components.DialogErrorTest.GIVEN error message WHEN rendered THEN message is displayed.png` |

### `:feature:home`

| Tela | Estado | Golden |
|------|--------|--------|
| `HomeContent` | Loading (sem dialog de erro) | `src/test/snapshots/com.bina.home.presentation.view.HomeScreenTest.GIVEN loading state WHEN rendered THEN no error dialog is shown.png` |
| `HomeContent` | Error state | `src/test/snapshots/com.bina.home.presentation.view.HomeScreenTest.GIVEN error state WHEN rendered THEN error message is displayed.png` |

### `:feature:character_details`

| Tela | Estado | Golden |
|------|--------|--------|
| `CharacterDetailsContent` | Nome do personagem | `src/test/snapshots/com.bina.character_details.presentation.view.CharacterDetailsScreenTest.GIVEN success state WHEN rendered THEN character name is displayed.png` |
| `CharacterDetailsContent` | Episódios | `src/test/snapshots/com.bina.character_details.presentation.view.CharacterDetailsScreenTest.GIVEN success state with episodes WHEN rendered THEN episode name is displayed.png` |

### `:feature:chat`

| Tela | Estado | Golden |
|------|--------|--------|
| `ChatScreen` | Modelo indisponível | `src/test/snapshots/com.bina.chat.chat.presentation.view.ChatScreenTest.GIVEN model unavailable state WHEN rendered THEN error title is displayed.png` |
| `ChatScreen` | Conversa vazia | `src/test/snapshots/com.bina.chat.chat.presentation.view.ChatScreenTest.GIVEN empty conversation WHEN rendered THEN empty state title is displayed.png` |
| `ChatScreen` | Mensagem do usuário | `src/test/snapshots/com.bina.chat.chat.presentation.view.ChatScreenTest.GIVEN conversation with messages WHEN rendered THEN user message is displayed.png` |

---

## Fluxo no CI

O step de screenshot testing está no job `test` do workflow `android-ci.yml`, após os testes unitários:

```yaml
- name: Verify screenshot goldens
  env:
    GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
  run: ./gradlew verifyRoborazziDebug -PGEMINI_API_KEY=$GEMINI_API_KEY

- name: Upload screenshot diffs on failure
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: roborazzi-diffs
    path: '**/build/outputs/roborazzi/**_compare.png'
```

Quando `verifyRoborazziDebug` falha, o Roborazzi gera imagens `_compare.png` com:
- **Esquerda:** golden (esperado)
- **Centro:** diff destacado em vermelho
- **Direita:** atual (o que o teste produziu)

Esses diffs são enviados como artefato de CI para diagnóstico visual.

---

## Workflows Comuns

### Adicionando um novo componente

```bash
# 1. Escreva o teste com captureRoboImage()
# 2. Gere o golden
./gradlew :core:designsystem:recordRoborazziDebug

# 3. Revise a imagem gerada em src/test/snapshots/
# 4. Commit golden + teste juntos
git add core/designsystem/src/test/snapshots/
git add core/designsystem/src/test/
git commit -m "test(designsystem): add screenshot golden for NewComponent"
```

### Mudança intencional de UI (ex.: novo tema)

```bash
# 1. Faça as mudanças no código
# 2. Re-gere os goldens afetados
./gradlew recordRoborazziDebug

# 3. Revise visualmente TODOS os goldens alterados antes de commitar
# 4. Commit goldens + código juntos
git add '**/src/test/snapshots/*.png'
git commit -m "test(goldens): update snapshots for new color theme"
```

### Quando o CI falha por screenshot

```bash
# 1. Baixe o artefato "roborazzi-diffs" do job de CI
# 2. Abra o *_compare.png para ver o diff visual
# 3a. Se a mudança foi acidental → corrija o código, não o golden
# 3b. Se a mudança foi intencional → rode record localmente e commite o novo golden
```

---

## Troubleshooting

### Golden em branco ou preto

**Causa:** `@GraphicsMode(NATIVE)` ausente na classe de teste.  
**Fix:** Adicionar `@GraphicsMode(GraphicsMode.Mode.NATIVE)` na classe.

### Golden salvo em `build/outputs/roborazzi/` em vez de `src/test/snapshots/`

**Causa:** Uso de `systemProperty("roborazzi.output.dir", ...)` em `testOptions.unitTests.all` — o plugin Gradle sobrescreve esse valor.  
**Fix:** Usar a extensão Gradle:
```kotlin
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/snapshots"))
}
```

### `roborazzi-junit4` not found

**Causa:** Esse artifact não existe no Maven Central.  
**Fix:** Use apenas `roborazzi` e `roborazzi-compose`.

### `verify` falha mesmo sem mudança de código

**Causa:** Gradle cache recuperou um golden de build anterior com resolução diferente.  
**Fix:** `./gradlew verifyRoborazziDebug --rerun-tasks`

---

## Referências

- [Roborazzi GitHub](https://github.com/takahirom/roborazzi)
- [ADR-023: Decisão de escolher Roborazzi](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/adrs/ADR-023-roborazzi-screenshot-testing.md)
- [SDD: Paparazzi vs Roborazzi](https://github.com/sabinabernardes/RickAndMorty/blob/master/.claude/specs/screenshot-testing-paparazzi-vs-roborazzi-sdd.md)
- [Wiki: Testes de UI (comportamento)](Testes-de-UI)
---

## Galeria de Goldens

Os goldens abaixo foram gerados pelo `recordRoborazziDebug` e estão commitados em `src/test/snapshots/` de cada módulo. Qualquer pixel diferente desses nas próximas execuções faz o CI falhar.

### `:core:designsystem` — Componentes atômicos

#### `StatusBadge` — light vs dark

| Light | Dark |
|:-----:|:----:|
| <img src="assets/golden_status_alive_light.png" width="180"> | <img src="assets/golden_status_alive_dark.png" width="180"> |

#### `CardCharacter` — light vs dark

| Light | Dark |
|:-----:|:----:|
| <img src="assets/golden_card_light.png" width="280"> | <img src="assets/golden_card_dark.png" width="280"> |

---

### `:feature:home` — Estados de tela

| Loading (sem erro) | Error light | Error dark |
|:------------------:|:-----------:|:----------:|
| <img src="assets/golden_home_loading.png" width="220"> | <img src="assets/golden_home_error_light.png" width="220"> | <img src="assets/golden_home_error_dark.png" width="220"> |

---

### `:feature:chat` — Chat vazio

| Light | Dark |
|:-----:|:----:|
| <img src="assets/golden_chat_empty_light.png" width="220"> | <img src="assets/golden_chat_empty_dark.png" width="220"> |

---

> **Como ler:** cada imagem é exatamente o que o CI espera encontrar. Se uma mudança no tema escurecer um badge 1% a mais, o `verifyRoborazziDebug` falhará com uma imagem `_compare.png` mostrando o diff em vermelho.
