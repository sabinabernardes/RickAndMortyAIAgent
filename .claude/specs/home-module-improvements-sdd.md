# SDD — Melhorias do módulo :feature:home

**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-02

---

## 1. Contexto e Objetivo

Quatro problemas identificados na análise de senioridade do projeto, todos no módulo `:feature:home`:

| # | Problema | Impacto |
|---|----------|---------|
| 1 | `homeModule` em `app/Modules.kt` em vez de `:feature:home` | Inconsistência de padrão; DI da feature fora da feature |
| 2 | `open class HomeViewModel` sem justificativa | Código com intenção opaca; `open` implica herança intencional |
| 3 | Typo `pagingSouce` no pacote e diretório | Pacote com nome errado; não corrigível sem renomear arquivos |
| 4 | `SearchPerformed` e chamadas à API disparadas em cada keystroke | Dados de analytics inúteis; desperdício de chamadas de rede |

Nenhum desses problemas afeta o comportamento funcional visível pelo usuário — mas todos afetam a qualidade do código e a confiabilidade dos dados de produto.

---

## 2. Decisões Técnicas

- [ADR-017](../adrs/ADR-017-homemodule-dentro-do-feature-home.md) — `homeModule` dentro de `:feature:home`
- [ADR-018](../adrs/ADR-018-debounce-search-no-viewmodel.md) — Debounce de busca e `SearchPerformed` no ViewModel
- [ADR-019](../adrs/ADR-019-ci-gate-obrigatorio-antes-do-auto-merge.md) — CI gate obrigatório antes do auto-merge

---

## 3. Mudança 1 — Criar `HomeModule.kt` em `:feature:home`

### Arquivo novo

**`feature/home/src/main/java/com/bina/home/di/HomeModule.kt`**

```kotlin
package com.bina.home.di

import com.bina.home.data.datasource.CharacterDataSource
import com.bina.home.data.datasource.CharacterDataSourceImpl
import com.bina.home.data.repository.HomeRepositoryImpl
import com.bina.home.domain.repository.HomeRepository
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory<CharacterDataSource> { CharacterDataSourceImpl(get()) }
    factory<HomeRepository> { HomeRepositoryImpl(get()) }
    factory { GetCharactersUseCase(get()) }
    factory { CharacterUiMapper() }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
}
```

### Arquivo alterado

**`app/src/main/java/com/bina/rickandmorty/di/Modules.kt`**

Remover:
- A definição do `homeModule` (bloco `val homeModule = module { … }`)
- Os imports das classes de home

Adicionar:
```kotlin
import com.bina.home.di.homeModule
```

O `appModules` não muda — `homeModule` continua referenciado lá.

---

## 4. Mudança 2 — Remover `open` de `HomeViewModel`

**`feature/home/src/main/java/com/bina/home/presentation/viewmodel/HomeViewModel.kt`**

```kotlin
// Antes
open class HomeViewModel(…) : ViewModel()

// Depois
class HomeViewModel(…) : ViewModel()
```

Nenhuma subclasse de `HomeViewModel` existe no projeto. `open` sem herança é ruído.

---

## 5. Mudança 3 — Corrigir typo `pagingSouce` → `pagingSource`

Dois arquivos precisam mudar:

**Arquivo 1:** `feature/home/src/main/java/com/bina/home/data/pagingSouce/CharacterPagingSource.kt`

```kotlin
// Antes
package com.bina.home.data.pagingSouce

// Depois
package com.bina.home.data.pagingSource
```

O arquivo também precisa ser movido para o diretório correto:
```
pagingSouce/CharacterPagingSource.kt  →  pagingSource/CharacterPagingSource.kt
```

**Arquivo 2:** `feature/home/src/main/java/com/bina/home/data/repository/HomeRepositoryImpl.kt`

```kotlin
// Antes
import com.bina.home.data.pagingSouce.CharacterPagingSource

// Depois
import com.bina.home.data.pagingSource.CharacterPagingSource
```

**Arquivo 3 (teste):** `feature/home/src/test/java/com/bina/home/data/repository/CharacterPagingSourceTest.kt`

Verificar se o import precisa de atualização.

---

## 6. Mudança 4 — Debounce de busca no ViewModel

Esta é a mudança mais significativa. Afeta `HomeViewModel.kt`, `HomeScreen.kt` e `HomeViewModelTest.kt`.

### 6.1 HomeViewModel

**Antes:**
```kotlin
fun getCharacters(query: String = "") {
    currentQuery = query
    if (query.isNotBlank()) analytics.track(HomeEvent.SearchPerformed(query))
    viewModelScope.launch { /* … */ }
}

fun onQueryChange(newQuery: String) {
    getCharacters(newQuery)
}
```

**Depois:**

```kotlin
private val _searchQuery = MutableStateFlow("")

init {
    logger.debug(TAG, "initialized")
    performance.startTrace(TRACE_SCREEN_LOAD)
    loadCharacters("")  // carga inicial imediata — sem debounce
    viewModelScope.launch {
        _searchQuery
            .drop(1)                    // pula o valor inicial "" já carregado acima
            .debounce(DEBOUNCE_MS)
            .distinctUntilChanged()
            .collect { query ->
                if (query.isNotBlank()) analytics.track(HomeEvent.SearchPerformed(query))
                loadCharacters(query)
            }
    }
}

fun onQueryChange(newQuery: String) {
    currentQuery = newQuery
    _searchQuery.value = newQuery
}

fun onRetry() {
    loadCharacters(currentQuery)  // retry não tem debounce
}

private fun loadCharacters(query: String) {
    viewModelScope.launch {
        getCharactersUseCase(query)
            .map { pagingData -> pagingData.map { uiMapper.map(it) } }
            .cachedIn(viewModelScope)
            .onStart {
                logger.debug(TAG, "loading characters query='$query'")
                _uiState.value = CharactersUiState.Loading
            }
            .catch { e ->
                logger.error(TAG, "characters load failed", e)
                _uiState.value = CharactersUiState.Error(e.message)
            }
            .collect { mappedPagingData ->
                if (!screenLoadTracked) {
                    val duration = performance.stopTrace(TRACE_SCREEN_LOAD)
                    logger.info(TAG, "home_screen_load: ${duration}ms")
                    screenLoadTracked = true
                }
                _uiState.value = CharactersUiState.Success(flowOf(mappedPagingData))
            }
    }
}

companion object {
    private const val TAG = "HomeViewModel"
    private const val TRACE_SCREEN_LOAD = "home_screen_load"
    const val DEBOUNCE_MS = 500L  // interno mas visível nos testes
}
```

**Novos imports necessários:**
```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
```

### 6.2 HomeScreen

`LaunchedEffect(query)` passa a chamar `onQueryChange` em vez de `getCharacters`:

```kotlin
// Antes
LaunchedEffect(query) {
    viewModel.getCharacters(query)
}

// Depois
LaunchedEffect(query) {
    viewModel.onQueryChange(query)
}
```

### 6.3 HomeViewModelTest

Os testes existentes que chamam `getCharacters()` diretamente precisam ser atualizados — a função não é mais pública.

**Setup adicional:**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    // testes de debounce precisam de StandardTestDispatcher para controlar o tempo
    private val standardDispatcher = StandardTestDispatcher()
    // …
}
```

**Padrão para testes de debounce:**

```kotlin
@Test
fun `GIVEN query WHEN onQueryChange THEN SearchPerformed is tracked after debounce`() =
    runTest(standardDispatcher) {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()
        viewModel = createViewModel()

        viewModel.onQueryChange("Rick")
        advanceTimeBy(HomeViewModel.DEBOUNCE_MS + 1)

        verify { analytics.track(HomeEvent.SearchPerformed("Rick")) }
    }

@Test
fun `GIVEN rapid typing WHEN onQueryChange called multiple times THEN only last query is tracked`() =
    runTest(standardDispatcher) {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()
        viewModel = createViewModel()

        viewModel.onQueryChange("R")
        viewModel.onQueryChange("Ri")
        viewModel.onQueryChange("Ric")
        viewModel.onQueryChange("Rick")
        advanceTimeBy(HomeViewModel.DEBOUNCE_MS + 1)

        verify(exactly = 1) { analytics.track(ofType<HomeEvent.SearchPerformed>()) }
        verify { analytics.track(HomeEvent.SearchPerformed("Rick")) }
    }
```

---

## 7. Mudança 5 — CI gate no auto-merge

### `android-ci.yml` — adicionar job `auto-merge`

```yaml
  auto-merge:
    name: Habilitar auto-merge
    runs-on: ubuntu-latest
    needs: [build]
    if: github.event_name == 'pull_request'
    permissions:
      pull-requests: write
      contents: write
    steps:
      - name: Habilitar auto-merge (squash)
        run: gh pr merge --auto --squash "$PR_URL"
        env:
          PR_URL: ${{ github.event.pull_request.html_url }}
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### `auto-merge.yml` — remover arquivo

O arquivo `.github/workflows/auto-merge.yml` é deletado inteiramente.

---

## 8. Resumo dos Arquivos

| Arquivo | Operação |
|---------|----------|
| `feature/home/src/main/java/com/bina/home/di/HomeModule.kt` | Criar |
| `app/src/main/java/com/bina/rickandmorty/di/Modules.kt` | Remover bloco `homeModule`, adicionar import |
| `feature/home/src/main/java/com/bina/home/presentation/viewmodel/HomeViewModel.kt` | Remover `open`, adicionar debounce |
| `feature/home/src/main/java/com/bina/home/presentation/view/HomeScreen.kt` | `getCharacters` → `onQueryChange` |
| `feature/home/src/main/java/com/bina/home/data/pagingSource/CharacterPagingSource.kt` | Corrigir pacote (mover arquivo) |
| `feature/home/src/main/java/com/bina/home/data/repository/HomeRepositoryImpl.kt` | Corrigir import |
| `feature/home/src/test/java/com/bina/home/data/repository/CharacterPagingSourceTest.kt` | Corrigir import se necessário |
| `feature/home/src/test/java/com/bina/home/presentation/viewmodel/HomeViewModelTest.kt` | Atualizar chamadas + testes de debounce |
| `.github/workflows/android-ci.yml` | Adicionar job `auto-merge` |
| `.github/workflows/auto-merge.yml` | Deletar |

---

## 9. Critérios de Aceite

- [ ] `HomeModule.kt` existe em `feature/home/…/di/` e contém todas as injeções do home
- [ ] `app/Modules.kt` não contém mais a definição do `homeModule` — apenas o import e a referência em `appModules`
- [ ] `HomeViewModel` é `class` (não `open`)
- [ ] Pacote `com.bina.home.data.pagingSource` (com `r`) — nenhuma ocorrência de `pagingSouce` no projeto
- [ ] Busca rápida ("Morty" letra a letra) dispara exatamente **1** chamada à API e **1** evento `SearchPerformed`, não 5
- [ ] Carga inicial da Home (query vazia) ainda é imediata — sem delay de 500ms
- [ ] `onRetry()` funciona sem debounce
- [ ] Testes de debounce passam com `advanceTimeBy()`
- [ ] Build compila sem warnings; todos os testes existentes continuam passando
- [ ] Auto-merge só é habilitado após o job `build` do CI passar
- [ ] Arquivo `auto-merge.yml` não existe mais