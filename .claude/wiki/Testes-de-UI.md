# Testes de UI

> Estratégia, ferramentas e cobertura de testes de UI por módulo. Todos rodam na JVM — sem emulador, sem CI especial.

---

## Ferramentas

| Ferramenta | Papel |
|-----------|-------|
| **Robolectric** | Executa código Android na JVM — permite `createComposeRule` sem emulador |
| **Compose Test JUnit4** | `assertIsDisplayed`, `assertIsEnabled`, `onNodeWithText`, `onNodeWithTag` |
| **Roborazzi** | Screenshot testing — captura goldens e falha se pixel mudar |

**Por que Robolectric e não emulador?** O CI (`ubuntu-latest`) não tem GPU. Robolectric com `@GraphicsMode(NATIVE)` usa Skia nativo e reproduz renderização suficientemente fiel para testes de comportamento e screenshots. Ver [Screenshot Testing com Roborazzi](Screenshot-Testing-Roborazzi.md).

---

## Padrão de Teste

Sub-composables são declarados `internal` (não `private`) para serem testados diretamente sem ViewModel:

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN idle state WHEN rendered THEN email field is visible`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Idle, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }
}
```

---

## Cobertura por Módulo

### :feature:auth — `LoginScreenTest` ✅

| # | Componente | Cenário | Tipo |
|---|-----------|---------|------|
| 1 | `LoginHeader` | Título "Rick & Morty AI" visível | Comportamento |
| 2 | `LoginHeader` | Subtitle "Entre para continuar" visível | Comportamento |
| 3 | `StudyBanner` | Label de autenticação simulada visível | Comportamento |
| 4 | `StudyBanner` | "email válido" e "8 caracteres" visíveis | Comportamento |
| 5 | `LoginForm(Idle)` | Campo Email visível | Comportamento |
| 6 | `LoginForm(Idle)` | Campo Senha visível | Comportamento |
| 7 | `LoginForm(Idle)` | Botão habilitado | Comportamento |
| 8 | `LoginForm(Loading)` | Botão desabilitado | Comportamento |
| 9 | `LoginForm(Loading)` | Golden light | Screenshot |
| 10 | `LoginForm(Error)` | Mensagem de erro visível | Comportamento |
| 11 | `LoginForm(Error "...8 caracteres...")` | Substring "8 caracteres" visível | Comportamento |
| 12 | `LoginForm(Error)` | Golden light | Screenshot |
| 13 | `LoginForm(Idle)` dark mode | Golden dark | Screenshot |
| 14 | `StudyBanner` dark mode | Golden dark | Screenshot |

> **Nota de implementação:** o estado `Loading` mostra `CircularProgressIndicator` no botão (sem texto "Entrar"). Por isso o botão é identificado por `testTag("login_button")`, não por texto.

### :feature:home — `HomeScreenTest`

Cenários planejados (ver [SDD](../specs/ui-testing-strategy-sdd.md)):
- Loading → skeletons visíveis
- Error → texto de erro exibido
- Error + clique "Tentar novamente" → callback chamado
- SearchToolbar → campo acessível por semântica

### :feature:character_details — `CharacterDetailsScreenTest`

Cenários planejados:
- Loading → spinner visível
- Error → texto de erro visível
- Success → nome, espécie, status presentes
- EpisodesLoading → skeleton de episódios
- EpisodesSuccess → códigos de episódios visíveis
- Clique voltar → callback chamado

### :feature:chat — `ChatScreenTest`

Cenários planejados:
- `Initializing` → typing indicator
- `ModelUnavailable` → mensagem de indisponibilidade
- `Conversation` vazia → empty state icon
- `Conversation` com mensagens → texto visível
- Campo vazio → botão de enviar desabilitado

---

## Screenshots Goldens

Goldens são commitados no repositório e verificados no CI:

| Módulo | Localização | Goldens |
|--------|------------|---------|
| `:feature:auth` | `feature/auth/src/test/snapshots/` | 4 (loading light, error light, idle dark, banner dark) |

```bash
# Regerar todos os goldens
./gradlew :feature:auth:recordRoborazziDebug

# Verificar (CI)
./gradlew :feature:auth:verifyRoborazziDebug

# Rodar todos os testes de UI
./gradlew :feature:auth:testDebugUnitTest
```

---

## Cobertura e CI

Testes de comportamento (Robolectric) rodam via `testDebugUnitTest` — já estão no pipeline de CI sem configuração extra.

A cobertura JaCoCo exclui composables de tela por padrão (`**/view/**`), pois a lógica real está no ViewModel e no UseCase. O que conta para coverage são os testes unitários de ViewModel e UseCase.

---

## Referências

- [Screenshot Testing com Roborazzi](Screenshot-Testing-Roborazzi.md) — goldens, record vs verify, dark mode, troubleshooting
- [Feature: Auth Simulada](Feature-Auth-Simulada.md) — detalhes da `LoginScreenTest`
- [SDD Estratégia de Testes de UI](../specs/ui-testing-strategy-sdd.md) — planejamento completo por feature
