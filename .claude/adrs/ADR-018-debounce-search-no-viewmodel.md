# ADR-018 — Debounce de busca e analytics de SearchPerformed no ViewModel

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulo:** `:feature:home`

---

## Contexto

A busca de personagens dispara `getCharacters(query)` a cada caractere digitado, porque a `HomeScreen` usa `LaunchedEffect(query)` que reage a cada mudança do estado local `query`. Isso causa dois problemas:

1. **API chamada em cada keystroke** — para a query "Morty" são 5 chamadas de rede em vez de 1
2. **`SearchPerformed` rastreado em cada keystroke** — o evento de analytics dispara para "M", "Mo", "Mor", "Mort" e "Morty" separadamente, tornando os dados de busca inutilizáveis

O problema está no acoplamento entre o estado da UI (`query` no Composable) e a lógica de busca (chamada ao use case). Atualmente o Composable controla quando a busca acontece; deveria ser o ViewModel.

## Opções Avaliadas

### Opção A: Debounce no Composable com `LaunchedEffect` e `delay`

```kotlin
LaunchedEffect(query) {
    delay(500)
    viewModel.getCharacters(query)
}
```

- **Prós:** Uma linha de mudança; sem alterar o ViewModel
- **Contras:** Lógica de negócio (quando disparar a busca) no Composable — viola a separação de responsabilidades; não testável sem Compose test framework; qualquer outro entry point da busca (ex: deeplink) precisa replicar o debounce

### Opção B: Debounce no ViewModel com `Flow.debounce()` + `distinctUntilChanged()`

```kotlin
// ViewModel
private val _searchQuery = MutableStateFlow("")

init {
    loadCharacters("")  // carga inicial imediata
    viewModelScope.launch {
        _searchQuery
            .drop(1)                    // pula o valor inicial já carregado
            .debounce(DEBOUNCE_MS)
            .distinctUntilChanged()
            .collect { query -> loadCharacters(query) }
    }
}

fun onQueryChange(newQuery: String) {
    currentQuery = newQuery
    _searchQuery.value = newQuery
}
```

- **Prós:** O ViewModel é responsável por decidir quando buscar; o Composable só repassa a query; testável com `advanceTimeBy()`; o debounce se aplica a qualquer chamada de `onQueryChange`, independente da origem
- **Contras:** Requer refatoração do ViewModel e atualização dos testes

### Opção C: Debounce mínimo por tamanho de query (só busca com N+ caracteres)

```kotlin
if (query.length >= 3 || query.isEmpty()) loadCharacters(query)
```

- **Prós:** Simples
- **Contras:** Não resolve o problema de frequência — "Mor", "Mort", "Morty" ainda disparam 3 chamadas; regra de N caracteres é arbitrária e não corresponde à experiência do usuário

## Decisão

**Escolhida: Opção B — debounce no ViewModel com `MutableStateFlow` + `Flow.debounce(500)`**

## Justificativa

A decisão de *quando* buscar é lógica de apresentação que pertence ao ViewModel, não ao Composable. O Composable deve apenas refletir estado e repassar intenções — `onQueryChange("Morty")` é a intenção; o ViewModel decide se e quando agir sobre ela.

`Flow.debounce()` é nativo do `kotlinx.coroutines` (já dependência do projeto) e testável com `advanceTimeBy()` do `kotlinx-coroutines-test` (também já presente). Não adiciona nenhuma nova dependência.

O `drop(1)` no flow garante que a carga inicial (query vazia) aconteça imediatamente sem esperar 500ms — o `init` chama `loadCharacters("")` diretamente antes de iniciar o fluxo de debounce.

**Constante:** `DEBOUNCE_MS = 500L` — 300ms é perceptível como lento em conexões rápidas; 800ms é tempo demais de espera após parar de digitar. 500ms é o padrão estabelecido pela maioria dos apps de busca em tempo real.

## Consequências

- `HomeScreen` passa a chamar `viewModel.onQueryChange(query)` via `LaunchedEffect(query)` em vez de `viewModel.getCharacters(query)` diretamente
- `getCharacters(query: String)` passa a ser `private fun loadCharacters(query: String)` — a função pública de busca é agora `onQueryChange`
- `onRetry()` chama `loadCharacters(currentQuery)` diretamente — retry não tem debounce
- `SearchPerformed` só é rastreado quando o flow debounced emite — ou seja, após 500ms sem novos caracteres
- Testes do ViewModel precisam usar `advanceTimeBy(500)` para verificar chamadas após debounce
- A API de rede recebe no máximo uma chamada por pausa de 500ms de digitação