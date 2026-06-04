# ADR-023 — Roborazzi para Screenshot Testing

**Status:** Accepted  
**Data:** 2026-06-03  
**Autor:** Sabina Bernardes

---

## Contexto

O projeto já tinha testes de comportamento com Compose Testing + Robolectric em todos os 4 módulos testáveis (`:core:designsystem`, `:feature:home`, `:feature:character_details`, `:feature:chat`). A próxima camada de qualidade é garantir **regressão visual** — que mudanças de layout, cores ou tamanhos não passem desapercebidas no CI.

Duas opções foram avaliadas: **Paparazzi** e **Roborazzi**. A spec completa de comparação está em `.claude/specs/screenshot-testing-paparazzi-vs-roborazzi-sdd.md`.

---

## Decisão

**Usar Roborazzi 1.63.0**, integrado sobre os testes Robolectric existentes.

---

## Alternativas Avaliadas

### Paparazzi (Cash App)
- Renderização JVM pura via LayoutLib (sem Robolectric)
- Exige setup separado por módulo com plugin próprio
- Não consegue capturar estados mid-flow (só composables isolados)
- Não reutiliza os testes existentes — exige novos testes só para screenshot

### Roborazzi (takahirom)
- Roda sobre Robolectric, que o projeto já usa em 4 módulos
- `captureRoboImage()` é uma chamada extra nos testes existentes — sem novos arquivos
- Captura mid-flow: screenshots em qualquer ponto da execução do teste
- Suporte a modo dark/light via `darkTheme = true/false`
- Renderização via Skia nativo (`@GraphicsMode(NATIVE)`) — pixel-accurate

---

## Motivação da Escolha

O fator decisivo foi **zero overhead de infraestrutura**. Paparazzi exigiria:
1. Plugin separado por módulo
2. Novos testes escritos do zero (não reusa os existentes)
3. JVM diferente do Robolectric → dois setups de teste para o mesmo módulo

Com Roborazzi, a migração foi:
1. Adicionar `roborazzi` e `roborazzi-compose` como deps de test
2. Adicionar `alias(libs.plugins.roborazzi)` ao build.gradle.kts
3. Adicionar `@GraphicsMode(NATIVE)` na classe de teste
4. Chamar `captureRoboImage()` no teste existente

---

## Implementação

### Módulos cobertos

| Módulo | Testes com screenshot | Goldens |
|--------|----------------------|---------|
| `:core:designsystem` | `StatusBadgeTest`, `CardCharacterTest`, `DialogErrorTest` | `src/test/snapshots/` |
| `:feature:home` | `HomeScreenTest` | `src/test/snapshots/` |
| `:feature:character_details` | `CharacterDetailsScreenTest` | `src/test/snapshots/` |
| `:feature:chat` | `ChatScreenTest` | `src/test/snapshots/` |

### Configuração de output

O Roborazzi Gradle plugin usa `build/outputs/roborazzi/` por padrão. Para manter os goldens versionados em `src/`:

```kotlin
// build.gradle.kts de cada módulo
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/snapshots"))
}
```

### Anotação obrigatória

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)  // renderização Skia — obrigatório para pixel accuracy
class MyScreenTest {
```

### Padrão de captura

```kotlin
// captura no teste existente — 1 linha
composeTestRule.onRoot().captureRoboImage()

// captura dark mode
composeTestRule.setContent {
    AppTheme(darkTheme = true) { MyComponent() }
}
composeTestRule.onRoot().captureRoboImage()
```

---

## Comandos

```bash
# Gerar/atualizar goldens
./gradlew recordRoborazziDebug

# Verificar regressão (CI)
./gradlew verifyRoborazziDebug

# Módulo específico
./gradlew :feature:home:recordRoborazziDebug
./gradlew :feature:home:verifyRoborazziDebug
```

---

## Consequências

**Positivas:**
- Regressão visual detectada automaticamente no CI antes do merge
- Goldens versionados no repositório — histórico visual do produto
- Zero custo de setup: reutiliza infraestrutura Robolectric existente
- Mid-flow screenshots: testa estados reais, não apenas composables isolados

**Negativas:**
- Goldens mudam com qualquer alteração visual intencional — exige re-record manual
- Diff de PNG em code review não é visual (precisa abrir o artefato de CI)
- `@GraphicsMode(NATIVE)` aumenta o tempo de execução dos testes (~10-20% por suite)

---

## Links

- [SDD Screenshot Testing](./../specs/screenshot-testing-paparazzi-vs-roborazzi-sdd.md)
- [Wiki: Screenshot Testing com Roborazzi](./../wiki/Screenshot-Testing-Roborazzi.md)
- [Roborazzi GitHub](https://github.com/takahirom/roborazzi)
- ADR relacionado: [ADR-019](ADR-019-ci-gate-obrigatorio-antes-do-auto-merge.md)