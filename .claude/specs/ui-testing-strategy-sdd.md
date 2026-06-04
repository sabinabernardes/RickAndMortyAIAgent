# SDD — Estratégia de Testes de UI

**Módulos:** `:core:designsystem`, `:feature:home`, `:feature:character_details`, `:feature:chat`, `:feature:auth`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-02

---

## 1. Contexto e Problema

O projeto tem cobertura sólida de testes unitários (ViewModels, UseCases, Repositories, Mappers), mas nenhum teste valida o comportamento visual das telas. Hoje é possível que:

- Um `StatusBadge` exiba verde para "dead" sem nenhum teste falhar
- `HomeScreen` mostre um spinner eterno no estado de erro sem que nada detecte
- Uma mudança no `CardCharacter` quebre o layout visualmente sem sinal de falha no CI

O objetivo desta spec é definir **o que, onde e com qual ferramenta** testar a camada de UI, respeitando a restrição de que o CI (GitHub Actions, `ubuntu-latest`) não tem emulador disponível.

---

## 2. Decisão de Ferramentas

### 2.1 Restrição de CI

O CI atual (`android-ci.yml`) roda `testDebugUnitTest` — testes de JVM. Não há `connectedAndroidTest` (precisa de emulador). Adicionar emulador ao CI aumenta o custo e o tempo de execução em ~10 minutos.

Por isso, **todos os testes desta spec rodam na JVM**, sem emulador.

### 2.2 Duas camadas

| Camada | Ferramenta | Módulo alvo | O que valida |
|--------|-----------|-------------|-------------|
| **Comportamento** | `ui-test-junit4` + Robolectric | `:feature:*` | Estados de tela (Loading, Error, Success), ações do usuário, acessibilidade semântica |
| **Visual** | Paparazzi | `:core:designsystem` | Aparência dos componentes — detecta regressões de cor, tamanho, layout |

### 2.3 Por que não instrumented tests?

Já existe `PlaygroundUiTest` como prova de conceito de teste instrumentado. Ele é valioso para testes de integração, mas **não entra no CI atual** — fica como suite de validação manual ou futura. A spec não cobre essa camada agora.

---

## 3. Camada 1 — Testes de Comportamento (Robolectric + Compose)

### 3.1 Dependências a adicionar

```toml
# libs.versions.toml
robolectric = "4.13"
robolectric-lib = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
```

Em cada `build.gradle.kts` de feature:
```kotlin
testImplementation(libs.androidx.ui.test.junit4)       // já no catalog
testImplementation(libs.robolectric.lib)
testImplementation(libs.androidx.ui.test.manifest)     // já no catalog
```

E no bloco `android {}` de cada feature:
```kotlin
testOptions {
    unitTests {
        isIncludeAndroidResources = true
    }
}
```

### 3.2 Padrão de teste

Os composables de conteúdo (`HomeContent`, `CharacterDetailsContent`, etc.) recebem estado como parâmetro — isso é o que permite testá-los sem ViewModel. O padrão é:

```kotlin
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN loading state WHEN rendered THEN shows skeleton cards`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Loading,
                    onCharacterClick = {},
                    viewModel = fakeViewModel()
                )
            }
        }

        composeTestRule.onAllNodesWithTag("CardCharacterSkeleton")
            .fetchSemanticsNodes()
            .isNotEmpty()
            .let { assertTrue(it) }
    }
}
```

> **Nota:** os composables privados precisam ser `internal` para serem acessados no módulo de teste.  
> Alternativa sem mudar visibilidade: testar via `HomeScreen` com um ViewModel fake injetado.

### 3.3 Cenários por feature

#### `:feature:home` → `HomeScreenTest`

| # | Estado / Ação | O que verificar |
|---|--------------|-----------------|
| 1 | `CharactersUiState.Loading` | Skeletons visíveis |
| 2 | `CharactersUiState.Error("mensagem")` | Texto de erro exibido |
| 3 | `CharactersUiState.Error` + clique em "Tentar novamente" | Callback de retry chamado |
| 4 | `CharactersUiState.Error` + clique em "OK" | Callback de clearError chamado |
| 5 | SearchToolbar presente | Campo de busca acessível por semântica |

#### `:feature:character_details` → `CharacterDetailsScreenTest`

| # | Estado / Ação | O que verificar |
|---|--------------|-----------------|
| 1 | `CharacterDetailsUiState.Loading` | Spinner visível |
| 2 | `CharacterDetailsUiState.Error("msg")` | Texto de erro visível |
| 3 | `CharacterDetailsUiState.Success(character)` | Nome, espécie, status presentes |
| 4 | `EpisodesState.Loading` | Skeleton de episódios visível |
| 5 | `EpisodesState.Success(episodes)` | Códigos dos episódios visíveis |
| 6 | Clique no botão voltar | Callback `onBackClick` chamado |

#### `:feature:chat` → `ChatScreenTest`

| # | Estado / Ação | O que verificar |
|---|--------------|-----------------|
| 1 | `ChatUiState.Initializing` | Typing indicator visível |
| 2 | `ChatUiState.ModelUnavailable` | Mensagem de indisponibilidade visível |
| 3 | `ChatUiState.Conversation` com mensagens vazias | Empty state icon visível |
| 4 | `ChatUiState.Conversation` com mensagens | Texto das mensagens visível |
| 5 | Campo de texto vazio | Botão de enviar desabilitado |
| 6 | Campo de texto preenchido + clique em enviar | Callback de envio chamado, campo limpo |

#### `:feature:auth` → `LoginScreenTest` ✅ implementado

Testa sub-composables diretamente — sem ViewModel (composables são `internal`):

| # | Componente / Estado | O que verificar |
|---|--------------------|-----------------| 
| 1 | `LoginHeader` | Texto "Rick & Morty AI" visível |
| 2 | `LoginHeader` | Subtitle "Entre para continuar" visível |
| 3 | `StudyBanner` | Label de autenticação simulada visível |
| 4 | `StudyBanner` | Instrução "email válido" e "8 caracteres" visíveis |
| 5 | `LoginForm(Idle)` | Campo Email visível |
| 6 | `LoginForm(Idle)` | Campo Senha visível |
| 7 | `LoginForm(Idle)` | Botão `login_button` habilitado |
| 8 | `LoginForm(Loading)` | Botão `login_button` desabilitado |
| 9 | `LoginForm(Loading)` | Snapshot golden (light) |
| 10 | `LoginForm(Error)` | Mensagem de erro visível |
| 11 | `LoginForm(Error("...8 caracteres..."))` | Texto "8 caracteres" visível |
| 12 | `LoginForm(Error)` | Snapshot golden (light) |
| 13 | `LoginForm(Idle)` dark mode | Snapshot golden (dark) |
| 14 | `StudyBanner` dark mode | Snapshot golden (dark) |

> **Implementação:** Roborazzi + Robolectric. 4 goldens em `feature/auth/src/test/snapshots/`. Ver [Screenshot Testing com Roborazzi](../wiki/Screenshot-Testing-Roborazzi.md).

---

## 4. Camada 2 — Testes Visuais (Paparazzi)

### 4.1 O que é Paparazzi

Paparazzi (Cash App) renderiza composables para bitmap na JVM — sem emulador. Gera screenshots de referência (`record`) e as compara nas próximas execuções (`verify`). Falha o build se houver diferença visual.

### 4.2 Dependência

```toml
# libs.versions.toml
paparazzi = "1.3.4"
```

```kotlin
// core/designsystem/build.gradle.kts (plugin)
alias(libs.plugins.paparazzi)
```

### 4.3 Localização

Testes Paparazzi ficam em `:core:designsystem` — o módulo que contém os componentes. Isso mantém os testes de visual junto ao código que eles protegem.

### 4.4 Padrão de teste

```kotlin
class StatusBadgeScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.NoActionBar"
    )

    @Test
    fun statusBadge_alive() {
        paparazzi.snapshot {
            RickAndMortyTheme {
                StatusBadge(status = "Alive")
            }
        }
    }

    @Test
    fun statusBadge_dead() {
        paparazzi.snapshot {
            RickAndMortyTheme {
                StatusBadge(status = "Dead")
            }
        }
    }

    @Test
    fun statusBadge_unknown() {
        paparazzi.snapshot {
            RickAndMortyTheme {
                StatusBadge(status = "Unknown")
            }
        }
    }
}
```

### 4.5 Componentes a cobrir

| Componente | Variações |
|------------|-----------|
| `StatusBadge` | Alive, Dead, Unknown |
| `CardCharacter` | Com imagem placeholder, nome longo, localização longa |
| `CardCharacterSkeleton` | Estado único |
| `SearchToolbar` | Vazio, com query |
| `DialogError` | Com mensagem curta, mensagem longa |

### 4.6 Dark mode

Cada componente deve ter um snapshot light e um dark:

```kotlin
@Test
fun statusBadge_alive_dark() {
    paparazzi.snapshot {
        RickAndMortyTheme(useDarkTheme = true) {
            StatusBadge(status = "Alive")
        }
    }
}
```

### 4.7 Fluxo de trabalho

```bash
# Gerar/atualizar screenshots de referência
./gradlew :core:designsystem:recordPaparazziDebug

# Verificar (no CI)
./gradlew :core:designsystem:verifyPaparazziDebug
```

Screenshots de referência ficam em `core/designsystem/src/test/snapshots/` e são **commitadas no repositório**.

---

## 5. O que NÃO entra nesta spec

| Fora do escopo | Motivo |
|----------------|--------|
| Testes de navegação (Home → Details) | Requer `NavController` real — futuro módulo de e2e |
| Testes de paginação com scroll | Depende de emulador para interação real |
| Testes de acessibilidade (TalkBack) | Requer análise dedicada — futuro |
| `PlaygroundUiTest` (instrumented) | Já existe, mas não entra no CI ainda |

---

## 6. Integração com CI

Após implementar, adicionar ao `android-ci.yml`:

```yaml
- name: Run Paparazzi screenshot verification
  run: ./gradlew :core:designsystem:verifyPaparazziDebug
```

Os testes Robolectric já rodam via `testDebugUnitTest` — nenhuma mudança no CI necessária para eles.

---

## 7. Ordem de Implementação

| Passo | O que fazer | Esforço |
|-------|------------|---------|
| 1 | Adicionar Paparazzi ao `:core:designsystem` | Baixo |
| 2 | Escrever snapshots de `StatusBadge` (light + dark) | Baixo |
| 3 | Escrever snapshots dos demais componentes DS | Baixo |
| 4 | Adicionar Robolectric às features | Médio |
| 5 | Tornar composables de conteúdo `internal` | Baixo |
| 6 | Escrever `HomeScreenTest` | Médio |
| 7 | Escrever `CharacterDetailsScreenTest` | Médio |
| 8 | Escrever `ChatScreenTest` | Médio |
| 9 | Adicionar `verifyPaparazzi` ao CI | Baixo |

---

## 8. Referências

- Paparazzi: https://github.com/cashapp/paparazzi
- `createComposeRule` + Robolectric: https://developer.android.com/develop/ui/compose/testing
- Componentes DS: `core/designsystem/src/main/kotlin/com/bina/designsystem/components/`
- Temas: `core/designsystem/src/main/kotlin/com/bina/designsystem/theme/RickAndMortyTheme.kt`