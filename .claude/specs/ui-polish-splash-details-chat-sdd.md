# SDD — Splash Screen + Detail Redesign + Chat Polish

**Módulos:** `:app` · `:feature:character_details` · `:feature:chat`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-05-30  

---

## 1. Contexto e Objetivo

Três melhorias visuais complementares à spec de Design System Polish (`design-system-polish-sdd.md`):

| Feature | Problema atual | Objetivo |
|---------|----------------|----------|
| Splash Screen | Fundo branco padrão do Android antes do Compose carregar | Portal no fundo escuro via SplashScreen API |
| CharacterDetailsScreen | Layout vertical simples, sem hierarquia visual, 300dp de imagem plana | Hero colapsável, card flutuante, toolbar adaptativa, stagger de entrada |
| ChatScreen | Estado vazio genérico, dots estáticos, itens soltos no input | Estado vazio temático, typing indicator animado, input flutuante |

---

## 2. Feature 1 — Splash Screen

### Abordagem: SplashScreen API (`androidx.core:core-splashscreen`)

**Por quê?**
- O sistema exibe o splash antes do processo do app inicializar — zero custo de performance
- Sem flash branco entre o ícone e o Compose renderizar
- Backport para API 12+ via biblioteca; minSdk do projeto é 28
- O ícone do portal (`@drawable/ic_launcher_foreground`) é reutilizado sem novo ativo

### Configuração visual
- `windowSplashScreenBackground`: `#0A1628` (fundo escuro espacial — mesmo do background do ícone)
- `windowSplashScreenAnimatedIcon`: `@drawable/ic_launcher_foreground` (portal ciano)
- `windowSplashScreenIconBackgroundColor`: `#0A1628`
- `postSplashScreenTheme`: `@style/Theme.RickAndMorty`

### Arquivos

| Arquivo | Mudança |
|---------|---------|
| `gradle/libs.versions.toml` | Adicionar `coreSplashscreen = "1.0.1"` e entrada em `[libraries]` |
| `app/build.gradle.kts` | `implementation(libs.androidx.core.splashscreen)` |
| `app/src/main/res/values/themes.xml` | Novo estilo `Theme.RickAndMorty.Splash` parent `Theme.SplashScreen` |
| `app/src/main/AndroidManifest.xml` | `android:theme="@style/Theme.RickAndMorty.Splash"` na `<activity>` |
| `app/src/main/java/com/bina/rickandmorty/MainActivity.kt` | `installSplashScreen()` antes de `super.onCreate()` |

---

## 3. Feature 2 — CharacterDetailsScreen Redesign

### Layout alvo

```
┌─────────────────────────┐
│  [← Rick Sanchez    ]   │  ← TopAppBar: transparente → opaca ao scrollar
│  ███████████████████    │
│  █   foto hero (full)  █│  ← AsyncImage, ContentScale.Crop
│  █  gradient overlay   █│  ← gradiente 120dp bottom para legibilidade
│  ███████████████ 🟢     │  ← StatusBadge bottom-end
│╔═══════════════════════╗│  ← Surface overlap 24dp (rounded top 24dp)
│║ Rick Sanchez          ║│  ← headlineMedium
│║ Human · Male          ║│  ← bodyMedium, alpha 60%
│║ ────────────────────  ║│
│║ Status      Alive     ║│  ← DetailItem com fadeSlideIn(0)
│║ Origem      Earth     ║│  ← fadeSlideIn(1)
│║ Localização Citadel   ║│  ← fadeSlideIn(2)
│╚═══════════════════════╝│
└─────────────────────────┘
```

### Técnica — card flutuante sem gap de layout

`Modifier.layout {}` reduz a altura reportada do card em 24dp e o posiciona visualmente 24dp acima, sem deixar espaço extra no scroll:

```kotlin
Modifier.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val overlap = 24.dp.roundToPx()
    layout(placeable.width, placeable.height - overlap) {
        placeable.placeRelative(0, -overlap)
    }
}
```

### Técnica — toolbar adaptativa

```kotlin
val scrollState = rememberScrollState()
val threshold = with(LocalDensity.current) {
    (DimensionTokens.DetailImageHeight - 80.dp).toPx()
}
val fraction by animateFloatAsState(
    targetValue = ((scrollState.value - threshold * 0.5f) / (threshold * 0.5f)).coerceIn(0f, 1f)
)
// TopAppBar:
//   containerColor = surface.copy(alpha = fraction)
//   title alpha = fraction (aparece gradualmente)
//   navigationIcon sempre branco (visível sobre a imagem)
```

### Estrutura do Composable

```
Box(fillMaxSize) {
    Column(verticalScroll = scrollState) {
        Box(height = DimensionTokens.DetailImageHeight) {
            AsyncImage(fillMaxSize, Crop)
            Box(120dp bottom, gradient black overlay)
            StatusBadge(align BottomEnd, padding 16dp)
        }
        Surface(
            modifier = layout-overlap-trick,
            shape = RoundedCornerShape(topStart=24, topEnd=24),
            tonalElevation = ElevationTokens.Level2
        ) {
            Column(padding 24dp) {
                Text(name, headlineMedium)
                Text("species · gender", bodyMedium, 60% alpha)
                HorizontalDivider(padding vertical 16dp)
                DetailItem("Status", status, index=0)
                DetailItem("Origem", origin, index=1)
                DetailItem("Localização", location, index=2)
            }
        }
    }
    TopAppBar(fixed, transparent → opaca)
}
```

### DetailItem atualizado
Recebe `index: Int = 0` e aplica `Modifier.fadeSlideIn(index)`.

### Arquivo modificado
`feature/character_details/src/main/java/com/bina/character_details/presentation/view/CharacterDetailsScreen.kt`

---

## 4. Feature 3 — ChatScreen Polish

### 4.1 Estado vazio temático

Atual: `Text("Wubba lubba dub dub!\nPergunte algo...")` centralizado.

Novo:
```
          [portal icon 80dp]      ← Icon(@drawable/ic_launcher_foreground, 80dp, tint = primary)
       Fale com o Rick             ← headlineMedium
  Pergunte sobre o universo        ← bodyMedium, onBackground alpha 50%
  de Rick and Morty.
```

Reutiliza `@drawable/ic_launcher_foreground` via `painterResource` — sem novo ativo.

### 4.2 Typing Indicator animado

Quando `message.isStreaming && message.text.isEmpty()`: substituir "..." por três dots que pulsam em sequência (stagger de 150ms cada).

```kotlin
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    // Dot 1: delay 0ms, Dot 2: delay 150ms, Dot 3: delay 300ms
    // alpha: 0.3f → 1f → 0.3f, ciclo 900ms
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(300, delayMillis = index * 150),
                    RepeatMode.Reverse
                )
            )
            Box(Modifier.size(8.dp).alpha(alpha).background(color, CircleShape))
        }
    }
}
```

### 4.3 Animação de entrada nas mensagens

`ChatMessageItem` aplica `Modifier.fadeSlideIn()` no `Surface` raiz. Sem stagger (mensagens chegam uma por vez).

### 4.4 Input row com Surface container

Envolver `OutlinedTextField` + `IconButton` em `Surface`:
```kotlin
Surface(
    modifier = Modifier.fillMaxWidth().padding(SpacingTokens.spacing8),
    shape = MaterialTheme.shapes.extraLarge,
    tonalElevation = ElevationTokens.Level1
) {
    Row(verticalAlignment = CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
        OutlinedTextField(/* sem borda explícita, transparente */)
        IconButton(...)
    }
}
```

### Arquivo modificado
`feature/chat/src/main/java/com/bina/chat/presentation/view/ChatScreen.kt`

---

## 5. Feature 4 — HomeScreen Skeleton Loading

### Problema atual
`LoadingContent` exibe um único `CircularProgressIndicator` centralizado. Enquanto a API carrega, a tela fica vazia — experiência abrupta.

### Solução: Skeleton Grid com efeito shimmer

Criar `CardCharacterSkeleton` no DS espelhando a estrutura do `CardCharacter`, com retângulos cinzas animados. O efeito shimmer usa `Brush.linearGradient` animado via `InfiniteTransition` — sem biblioteca externa.

#### Shimmer Brush
```kotlin
// core/designsystem/src/main/kotlin/com/bina/designsystem/animation/ShimmerBrush.kt
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition()
    val translateX by transition.animateFloat(
        initialValue = -300f, targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart)
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFE0E0E0)),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f)
    )
}
```

#### CardCharacterSkeleton
Mesma dimensão do `CardCharacter` (160dp), mesma estrutura de `Column`, mas com:
- `Box` 160×160dp com `shimmerBrush()` como background
- Retângulo de nome ~80% width, 16dp height
- Retângulo de label 50% width, 12dp height
- `shape = MaterialTheme.shapes.medium`

#### HomeScreen
Substituir `LoadingContent` por um `LazyVerticalGrid` com 6 itens `CardCharacterSkeleton` — mesma estrutura do `CharacterList` para que a transição seja suave.

### Arquivos
| Arquivo | Mudança |
|---------|---------|
| `core/designsystem/.../animation/ShimmerBrush.kt` | Novo — brush animado reutilizável |
| `core/designsystem/.../components/CardCharacterSkeleton.kt` | Novo — skeleton card |
| `feature/home/.../HomeScreen.kt` | Substituir `LoadingContent` |

---

## 6. Ordem de execução

| # | Arquivo | Mudança |
|---|---------|---------|
| 1 | `libs.versions.toml` | core-splashscreen |
| 2 | `app/build.gradle.kts` | dependency |
| 3 | `themes.xml` | splash theme |
| 4 | `AndroidManifest.xml` | activity theme |
| 5 | `MainActivity.kt` | installSplashScreen() |
| 6 | `CharacterDetailsScreen.kt` | redesign completo |
| 7 | `ChatScreen.kt` | estado vazio + typing indicator + Surface input + fadeSlideIn |
| 8 | `ShimmerBrush.kt` | novo arquivo |
| 9 | `CardCharacterSkeleton.kt` | novo componente DS |
| 10 | `HomeScreen.kt` | substituir LoadingContent por skeleton grid |

---

## 6. Verificação

| Cenário | Esperado |
|---------|---------|
| Cold start do app | Portal ciano sobre fundo `#0A1628` antes da HomeScreen |
| Tap num personagem | Hero foto full-width, badge sobre imagem com gradient, toolbar aparece ao scrollar |
| Abrir chat (vazio) | Portal icon + headline + subtítulo |
| Enviar mensagem | Bubble aparece com fade + slide |
| IA respondendo | Três dots pulsando no bubble, não spinner no botão |
| Modo escuro | Todos os três fluxos com cores corretas |
| Home carregando | Grid 2×3 de cards skeleton com shimmer antes dos personagens aparecerem |

---

## 7. Referências

- Design System base: `design-system-polish-sdd.md`
- Animação: `core/designsystem/src/main/kotlin/com/bina/designsystem/animation/FadeSlideIn.kt`
- Portal icon: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- DoD base: `design-system-polish-dod.md`