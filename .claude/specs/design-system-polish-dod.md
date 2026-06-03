# DoD — Definition of Done
## Design System Polish & Animações de Entrada

**Spec:** `design-system-polish-sdd.md`  
**Data:** 2026-05-30

---

A implementação está **concluída** quando todos os itens abaixo estiverem satisfeitos.

---

## 1. Build & Qualidade de Código

- [ ] `./gradlew assembleDebug` passa sem erros e sem warnings novos
- [ ] `./gradlew :core:designsystem:test` passa
- [ ] Nenhum arquivo fora de `DimensionTokens.kt` contém valores literais de `dp` ou `sp` hardcoded nos componentes do DS (`StatusBadge`, `CardCharacter`, `Toolbar`, `SearchToolbar`, `DialogError`)
- [ ] Nenhum arquivo de feature (`ChatScreen`, `HomeScreen`, `CharacterDetailsScreen`) contém `Color(0xFF...)` literals

---

## 2. Design Tokens

- [ ] `ColorTokens.kt` não define cores que divergem de `ColorScheme.kt`
- [ ] `ColorTokens.StatusAlive`, `StatusDead` e `StatusUnknown` existem e são usados em `StatusBadge`
- [ ] `DimensionTokens.kt` existe com: `CardCharacterSize`, `DetailImageHeight`, `ChatBubbleUserMaxWidth`, `ChatBubbleAiMaxWidth`, `ChatIconSize`
- [ ] `AnimationTokens.kt` contém: `DurationEnter`, `EasingEnter`, `StaggerDelay`, `StaggerMaxDelay`
- [ ] `TypographyTokens.DefaultTypography` define todos os 13 estilos M3 com Nunito Sans
- [ ] `RickAndMortyTheme` passa `TypographyTokens.DefaultTypography` completo (não seleção parcial)

---

## 3. Componentes Refatorados

### StatusBadge
- [ ] Usa `ColorTokens.Status*` em vez de `Color(0xFF...)`
- [ ] Usa `MaterialTheme.shapes.extraLarge` em vez de `RoundedCornerShape(50)`
- [ ] Usa `ElevationTokens.Level2` em vez de `4.dp` literal
- [ ] Usa `SpacingTokens.spacing8` / `SpacingTokens.spacing4` no padding

### CardCharacter
- [ ] Usa `DimensionTokens.CardCharacterSize` para width e size
- [ ] Aceita parâmetro `index: Int = 0`
- [ ] Aplica `Modifier.fadeSlideIn(index)` no Card raiz

### ChatScreen
- [ ] `Modifier.size(64.dp)` substituído por `DimensionTokens.ChatIconSize`
- [ ] `widthIn(max = 280.dp)` substituído por `DimensionTokens.ChatBubbleUserMaxWidth`
- [ ] `widthIn(max = 340.dp)` substituído por `DimensionTokens.ChatBubbleAiMaxWidth`
- [ ] `tonalElevation = 2.dp` substituído por `ElevationTokens.Level1`
- [ ] `.copy(fontSize = 17.sp)` removido — usa `MaterialTheme.typography.bodyLarge` direto
- [ ] `strokeWidth = 2.dp` substituído por `SpacingTokens.spacing2`
- [ ] `ChatMessageItem` aplica `Modifier.fadeSlideIn()`

### CharacterDetailsScreen
- [ ] `height(300.dp)` substituído por `DimensionTokens.DetailImageHeight`
- [ ] Referências diretas a `TypographyTokens.DefaultTypography.*` substituídas por `MaterialTheme.typography.*`
- [ ] `DetailItem` aplica `Modifier.fadeSlideIn(index)` com índice crescente

---

## 4. Animações

- [ ] `FadeSlideIn.kt` existe em `core/designsystem/.../animation/`
- [ ] Animação visível na `HomeScreen`: cards entram com fade + slide escalonado
- [ ] Animação visível na `ChatScreen`: cada nova mensagem entra com fade + slide
- [ ] Animação visível na `CharacterDetailsScreen`: itens de detalhe entram escalonados
- [ ] Stagger máximo não ultrapassa 150ms (último item não demora mais que isso)

---

## 5. Tipografia

- [ ] Fonte Nunito Sans visível no app rodando no dispositivo
- [ ] Fallback para `FontFamily.Default` não quebra build offline
- [ ] Dependência `ui-text-google-fonts` adicionada em `libs.versions.toml` e `core/designsystem/build.gradle.kts`

---

## 6. Limpeza

- [ ] `app/src/main/java/com/bina/rickandmorty/ui/theme/Color.kt` sem definições de cor (ou arquivo deletado)
- [ ] `app/src/main/java/com/bina/rickandmorty/ui/theme/Type.kt` deletado
- [ ] Nenhum import quebrado após a limpeza

---

## 7. Validação Visual no Dispositivo

Testado no **Z Flip 6** (ou emulador) nos cenários:

- [ ] **HomeScreen — modo claro:** grid de cards com stagger de entrada visível
- [ ] **HomeScreen — modo escuro:** cores corretas, sem divergência de tokens
- [ ] **CharacterDetailsScreen:** imagem hero ocupa `DetailImageHeight`, itens de detalhe entram animados
- [ ] **ChatScreen — mensagem do usuário:** bubble à direita, cor `Primary`, fade ao aparecer
- [ ] **ChatScreen — mensagem da IA:** bubble à esquerda, cor `Secondary`, fade ao aparecer
- [ ] **StatusBadge Alive:** verde `#2ECC71`, pill shape
- [ ] **StatusBadge Dead:** vermelho `#E74C3C`, pill shape
- [ ] **Fonte Nunito Sans:** visível nos títulos e corpo de texto

---

## 8. Git

- [ ] Commits organizados por fase (uma mudança por commit, mensagem descritiva)
- [ ] Nenhum `local.properties` ou arquivo sensível commitado
- [ ] Branch `feat/design-system-polish` com PR aberto apontando para `master`