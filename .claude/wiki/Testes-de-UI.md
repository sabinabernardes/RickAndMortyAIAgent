# Testes de UI

Os testes de UI do projeto cobrem componentes do `:core:designsystem` e as telas das features. Todos rodam **sem emulador e sem dispositivo físico** — na JVM, via Robolectric.

---

## Por que Robolectric?

Testes instrumentados (`connectedAndroidTest`) exigem um emulador ou dispositivo conectado. Isso torna a execução lenta e frágil em CI. O Robolectric resolve o problema simulando o ambiente Android diretamente na JVM:

| | Robolectric | Instrumentado |
|---|---|---|
| **Execução** | JVM local | Emulador / dispositivo |
| **Velocidade** | Rápido (segundos) | Lento (minutos) |
| **CI** | `./gradlew test` | Precisa de AVD |
| **Uso aqui** | Testes de componente e tela | — |

O Robolectric **não substitui** testes end-to-end, mas cobre o golden path de cada tela com custo quase zero de infraestrutura.

---

## Configuração

### 1. Dependência

`gradle/libs.versions.toml`:
```toml
[versions]
robolectric = "4.13"

[libraries]
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
```

### 2. Build Gradle de cada módulo

```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true  // necessário para strings e recursos
        }
    }
}

dependencies {
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.ui.test.manifest)
}
```

### 3. Anotações obrigatórias em cada classe de teste

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])  // compileSdk 35 não é suportado pelo Robolectric 4.13
class MinhaTelaTest {
    @get:Rule
    val composeTestRule = createComposeRule()
}
```

> **Por que `sdk = [33]`?** O Robolectric 4.13 suporta até SDK 34. Como o projeto usa `compileSdk = 35`, é necessário fixar `sdk = [33]` para evitar o erro `DefaultSdkPicker: No compatible SDK`.

---

## Visibilidade dos Composables

Para que os testes do módulo consigam acessar os composables de tela, a visibilidade deve ser `internal` (não `private`):

```kotlin
// HomeScreen.kt
@Composable
internal fun HomeContent(...) { ... }

// CharacterDetailsScreen.kt
@Composable
internal fun CharacterDetailsContent(...) { ... }

// ChatScreen.kt
@Composable
internal fun ConversationContent(...) { ... }
internal fun ModelUnavailableContent() { ... }
```

---

## Cobertura por módulo

### `:core:designsystem`

| Classe | Testes |
|--------|--------|
| `CardCharacter` | name, species, location exibidos; click dispara callback |
| `DialogError` | mensagem exibida; botões retry e dismiss disparam callbacks |
| `StatusBadge` | textos "Alive", "Dead", "Unknown" exibidos |

### `:feature:home`

| Teste | Verificação |
|-------|-------------|
| Loading state | sem dialog de erro |
| Error state | mensagem de erro visível |
| Retry click | `viewModel.onRetry()` chamado |
| Dismiss click | `viewModel.clearError()` chamado |
| Character click | callback `onCharacterClick` disparado |

### `:feature:character_details`

| Teste | Verificação |
|-------|-------------|
| Success state | nome do personagem visível |
| Success state | "Human · Male" visível |
| With episodes | nome e código do episódio existem na tela |
| Back button | callback `onBackClick` disparado |
| Episodes error | mensagem de erro visível |

### `:feature:chat`

| Teste | Verificação |
|-------|-------------|
| Model unavailable | título de erro visível |
| Model unavailable | mensagem descritiva visível |
| Empty conversation | título "Fale com o Rick" visível |
| User message | texto da mensagem visível |
| AI message | texto da resposta visível |
| Send message | callback `onSendMessage` dispara com o texto correto |

### `:feature:auth`

Testa sub-composables diretamente (`LoginHeader`, `StudyBanner`, `LoginForm`) — sem ViewModel, pois são declarados `internal`:

| Teste | Verificação |
|-------|-------------|
| `LoginHeader` rendered | "Rick & Morty AI" visível |
| `LoginHeader` rendered | "Entre para continuar" visível |
| `StudyBanner` rendered | label de autenticação simulada visível |
| `StudyBanner` rendered | "email válido" e "8 caracteres" visíveis |
| `LoginForm(Idle)` | campo Email visível |
| `LoginForm(Idle)` | campo Senha visível |
| `LoginForm(Idle)` | botão `login_button` habilitado |
| `LoginForm(Loading)` | botão `login_button` **desabilitado** |
| `LoginForm(Loading)` | snapshot golden (light) |
| `LoginForm(Error)` | mensagem de erro visível |
| `LoginForm(Error "...8 caracteres...")` | substring "8 caracteres" visível |
| `LoginForm(Error)` | snapshot golden (light) |
| `LoginForm(Idle)` dark | snapshot golden (dark) |
| `StudyBanner` dark | snapshot golden (dark) |

> **Nota:** estado `Loading` exibe `CircularProgressIndicator` (sem texto "Entrar"). O botão é identificado por `testTag("login_button")`, não por texto.

---

## Padrões e convenções

### Given-When-Then no nome do teste

```kotlin
@Test
fun `GIVEN error state WHEN retry clicked THEN onRetry is called on viewModel`() { ... }
```

### `assertExists()` vs `assertIsDisplayed()`

Use `assertIsDisplayed()` para conteúdo visível na tela. Para conteúdo dentro de um container com scroll que pode estar fora da área visível, use `assertExists()`:

```kotlin
// conteúdo acima do fold — garante que está visível
composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()

// episódios ficam abaixo do fold em scroll — verificar existência é suficiente
composeTestRule.onNodeWithText("Pilot").assertExists()
```

### Nós com texto duplicado

Quando o mesmo texto aparece mais de uma vez na árvore de semântica (ex: nome na header e no toolbar colapsável), use o índice:

```kotlin
composeTestRule.onAllNodesWithText("Rick Sanchez")[0].assertIsDisplayed()
```

### ViewModel com debounce em testes

O `HomeViewModel` recebe `debounceMs` injetado. Testes de comportamento passam `0L` para que o debounce seja transparente:

```kotlin
private fun fakeViewModel() = HomeViewModel(
    useCase = mockk(relaxed = true),
    ...,
    debounceMs = 0L
)
```

---

## Executando os testes

```bash
# Todos os módulos de uma vez
./gradlew :core:designsystem:testDebugUnitTest \
          :feature:home:testDebugUnitTest \
          :feature:character_details:testDebugUnitTest \
          :feature:chat:testDebugUnitTest \
          :feature:auth:testDebugUnitTest

# Módulo individual
./gradlew :feature:home:testDebugUnitTest
```

Nenhum emulador ou dispositivo necessário. Os testes aparecem no resultado de `./gradlew test` junto com os testes unitários normais.