# SDD — Design System Polish & Animações de Entrada

**Módulo:** `:core:designsystem` + telas de features  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-05-30  

---

## 1. Contexto e Objetivo

O Design System existe em `core/designsystem` mas acumulou inconsistências:

- **Sistema de cores duplicado:** `ColorTokens` (cinza escuro) conflita com `ColorScheme` (azul ciano), e o `app/ui/theme/` ainda tem um terceiro sistema com cores Material default roxas.
- **Tipografia incompleta:** apenas 4 dos 14 estilos Material 3 estão definidos. `RickAndMortyTheme` referencia `bodySmall` e `titleLarge` que não existem, causando fallback silencioso.
- **Hardcodes espalhados:** `StatusBadge`, `ChatScreen`, `CardCharacter` e `CharacterDetailsScreen` usam valores literais de `dp`, `sp` e `Color()` em vez dos tokens do DS.
- **Ausência de animações:** `AnimationTokens` existe mas não é usado. Nenhuma tela tem animação de entrada.
- **Sem fonte customizada:** `FontFamily.Default` em toda a tipografia.

O objetivo é consolidar o DS como fonte única de verdade, eliminar todos os hardcodes, adicionar a fonte **Nunito Sans** e implementar animação **Fade + Slide** escalonada nas listas.

---

## 2. Escopo

| Item | Incluído |
|------|----------|
| Consolidar ColorTokens com ColorScheme | Sim |
| Adicionar tokens de cor semânticos (status alive/dead) | Sim |
| Adicionar DimensionTokens (card, bubble, imagem) | Sim |
| Completar TypographyTokens com Nunito Sans | Sim |
| Corrigir RickAndMortyTheme (typography completo) | Sim |
| Refatorar StatusBadge (remover hardcodes) | Sim |
| Refatorar ChatScreen (remover hardcodes) | Sim |
| Refatorar CardCharacter (usar DimensionTokens) | Sim |
| Refatorar CharacterDetailsScreen (usar tokens) | Sim |
| Animação Fade + Slide nas listas e mensagens | Sim |
| Remover app/ui/theme/ duplicado | Sim |
| Dark mode mantido | Sim |
| Testes unitários de UI | Não (sem alteração de lógica) |

---

## 3. Decisões de Design

### 3.1 Cores
`ColorTokens` vira um espelho semântico da `ColorScheme` (fonte de verdade). Quem precisar de cor raw usa `MaterialTheme.colorScheme`. Quem precisar de cor específica de status usa `ColorTokens.StatusAlive/Dead/Unknown`.

### 3.2 Tipografia
Google Fonts via `androidx.compose.ui:ui-text-google-fonts` — sem arquivo local, carregado em runtime. Fonte: **Nunito Sans**. Fallback automático para `FontFamily.Default` se offline.

### 3.3 Animações
`Modifier.fadeSlideIn(index)` reutilizável: `alpha 0→1` + `translationY +16dp→0`, duração 300ms, easing `EaseOut`. Stagger de `30ms × index` com cap em `150ms` para grids. Mensagens do Chat não têm stagger (delay = 0).

### 3.4 Shapes no Chat
Bubbles usam `RoundedCornerShape` com corners assimétricos compostos a partir de `MaterialTheme.shapes.large` (16dp) e `MaterialTheme.shapes.small` (4dp) — sem criar token novo de shape, pois esses valores já são semânticos no M3.

---

## 4. Arquitetura e Arquivos

### 4.1 Novos arquivos

```
core/designsystem/src/main/kotlin/com/bina/designsystem/
  tokens/
    DimensionTokens.kt          # CardCharacterSize, DetailImageHeight, ChatBubble*
  animation/
    FadeSlideIn.kt              # Modifier extension
```

### 4.2 Arquivos modificados

```
core/designsystem/
  tokens/
    ColorTokens.kt              # alinhar com ColorScheme + adicionar StatusAlive/Dead/Unknown
    TypographyTokens.kt         # Nunito Sans + 13 estilos M3 completos
    AnimationTokens.kt          # DurationEnter, EasingEnter, StaggerDelay, StaggerMaxDelay
  theme/
    RickAndMortyTheme.kt        # passar TypographyTokens.DefaultTypography completo
  components/
    StatusBadge.kt              # usar ColorTokens.Status*, ElevationTokens, SpacingTokens
    CardCharacter.kt            # usar DimensionTokens.CardCharacterSize + receber index

feature/chat/
  presentation/view/
    ChatScreen.kt               # remover hardcodes dp/sp + aplicar fadeSlideIn

feature/character_details/
  presentation/view/
    CharacterDetailsScreen.kt   # usar DimensionTokens + MaterialTheme.typography

feature/home/
  presentation/view/
    HomeScreen.kt               # passar index para CardCharacter

gradle/
  libs.versions.toml            # adicionar ui-text-google-fonts

app/src/main/java/com/bina/rickandmorty/ui/theme/
  Color.kt                      # remover conteúdo (manter arquivo vazio ou deletar)
  Type.kt                       # deletar
```

---

## 5. Tokens Novos

### ColorTokens (adições)
```kotlin
val StatusAlive   = Color(0xFF2ECC71)
val StatusDead    = Color(0xFFE74C3C)
val StatusUnknown = Color(0xFF9E9E9E)
```

### DimensionTokens (arquivo novo)
```kotlin
object DimensionTokens {
    val CardCharacterSize       = 160.dp
    val DetailImageHeight       = 300.dp
    val ChatBubbleUserMaxWidth  = 280.dp
    val ChatBubbleAiMaxWidth    = 340.dp
    val ChatIconSize            = 64.dp
}
```

### AnimationTokens (adições)
```kotlin
const val DurationEnter   = 300       // ms
val EasingEnter: Easing   = EaseOut
const val StaggerDelay    = 30        // ms por item
const val StaggerMaxDelay = 150       // ms
```

---

## 6. Tipografia — Nunito Sans

**Dependência:**
```toml
# libs.versions.toml
ui-text-google-fonts = { module = "androidx.compose.ui:ui-text-google-fonts", version.ref = "compose-bom" }
```

**Configuração no TypographyTokens:**
```kotlin
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
private val nunitoSans = GoogleFont("Nunito Sans")
private fun nunitoFontFamily(weight: FontWeight) = FontFamily(
    Font(googleFont = nunitoSans, fontProvider = provider, weight = weight)
)
```

Os 13 estilos cobrem: `displayLarge`, `headlineLarge/Medium/Small`, `titleLarge/Medium/Small`, `bodyLarge/Medium/Small`, `labelLarge/Medium/Small`.

---

## 7. Animação — FadeSlideIn

```kotlin
// core/designsystem/src/main/kotlin/com/bina/designsystem/animation/FadeSlideIn.kt

@Composable
fun Modifier.fadeSlideIn(index: Int = 0): Modifier {
    val delay = (index * AnimationTokens.StaggerDelay)
        .coerceAtMost(AnimationTokens.StaggerMaxDelay)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(AnimationTokens.DurationEnter, easing = AnimationTokens.EasingEnter)
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 16f,
        animationSpec = tween(AnimationTokens.DurationEnter, easing = AnimationTokens.EasingEnter)
    )
    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY.dp.toPx()
    }
}
```

**Pontos de aplicação:**
- `CardCharacter` — `Card(modifier = modifier.fadeSlideIn(index))`
- `HomeScreen` — `CardCharacter(..., index = index)`
- `CharacterDetailsScreen` — `DetailItem(modifier = Modifier.fadeSlideIn(index))` com índice crescente
- `ChatMessageItem` — `Surface(modifier = Modifier.fadeSlideIn())` sem índice

---

## 8. Ordem de Execução

| Passo | O que muda | Risco de quebra |
|-------|-----------|-----------------|
| 1 | `ColorTokens` + status colors | Baixo |
| 2 | `DimensionTokens` (novo) | Nenhum |
| 3 | `AnimationTokens` (adições) | Nenhum |
| 4 | `TypographyTokens` + Google Fonts dep | Baixo |
| 5 | `RickAndMortyTheme` — typography completo | Baixo |
| 6 | `StatusBadge` refactor | Baixo |
| 7 | `CardCharacter` — DimensionTokens + index param | Baixo |
| 8 | `ChatScreen` refactor hardcodes | Baixo |
| 9 | `CharacterDetailsScreen` refactor | Baixo |
| 10 | `FadeSlideIn.kt` + aplicar nas telas | Baixo |
| 11 | `HomeScreen` — passar index | Baixo |
| 12 | Remover `app/ui/theme/Color.kt`, `Type.kt` | Médio (verificar imports) |

---

## 9. Referências

- `core/designsystem/src/main/kotlin/com/bina/designsystem/` — tokens, theme, components
- `feature/chat/src/main/java/com/bina/chat/presentation/view/ChatScreen.kt`
- `feature/home/src/main/java/com/bina/home/presentation/view/HomeScreen.kt`
- `feature/character_details/src/main/java/com/bina/character_details/presentation/view/CharacterDetailsScreen.kt`
- DoR: `design-system-polish-dor.md`
- DoD: `design-system-polish-dod.md`