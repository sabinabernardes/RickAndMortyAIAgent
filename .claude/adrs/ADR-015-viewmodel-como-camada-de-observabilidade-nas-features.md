# ADR-015 — ViewModel como camada de integração de observabilidade nas features

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulos:** `:feature:home`, `:feature:character_details`, `:feature:chat`

---

## Contexto

Os módulos `:core:logging` e `:core:analytics` estão implementados e os eventos de cada feature estão definidos como sealed classes (`HomeEvent`, `CharacterDetailsEvent`, `ChatEvent`). As dependências já estão declaradas nos `build.gradle.kts` de cada feature e os módulos Koin registrados no `:app`.

O que falta é decidir **em qual camada da arquitetura** cada feature deve injetar e chamar `AppLogger`, `AnalyticsTracker` e `PerformanceTracker`.

A arquitetura de cada feature segue:

```
Screen (Composable) → ViewModel → UseCase → Repository → DataSource
```

As opções de integração são:

- **ViewModel**: coordena estado da UI, recebe ações do usuário, observa fluxos de dados
- **Repository/DataSource**: acessa dados de rede ou banco; lida com erros de infraestrutura
- **UseCase**: encapsula regra de negócio; geralmente sem efeitos colaterais

## Opções Avaliadas

### Opção A: Injetar apenas no Repository/DataSource

```kotlin
class HomeRepositoryImpl(
    private val dataSource: CharacterDataSource,
    private val logger: AppLogger
) : HomeRepository {
    override fun getCharacters(query: String): Flow<PagingData<CharacterDomain>> {
        logger.debug(TAG, "Fetching characters query='$query'")
        // ...
    }
}
```

- **Prós:** Logging próximo ao dado; captura erros de rede com contexto técnico
- **Contras:** Analytics de comportamento do usuário (ex: `CharacterClicked`) não faz sentido no Repository — ele não sabe qual ação o usuário tomou; precisa mudar mais arquivos; Repository é unit-testado sem UI state

### Opção B: Injetar apenas no ViewModel

```kotlin
class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel()
```

- **Prós:** Ponto único de integração; ViewModel já coordena estado da UI e ações do usuário; todos os eventos de analytics mapeiam naturalmente para funções do ViewModel; testes de ViewModel cobrem toda a observabilidade com `mockk<AppLogger>()`
- **Contras:** Logging técnico de infra (ex: falha de rede dentro do DataSource) fica invisível, a menos que o erro suba até o ViewModel

### Opção C: Ambas as camadas

- **Prós:** Cobertura completa — erros de infra no Repository + eventos de usuário no ViewModel
- **Contras:** Mais arquivos alterados; dobra o número de injeções a configurar no Koin; escopo grande demais para uma primeira iteração

## Decisão

**Escolhida: Opção B — ViewModel como camada primária de observabilidade nesta iteração**

## Justificativa

Todos os eventos definidos nas sealed classes de cada feature (`CharacterClicked`, `SearchPerformed`, `MessageSent`, etc.) representam **ações do usuário** — e essas ações chegam ao sistema pelas funções públicas dos ViewModels. O ViewModel já é o ponto natural onde o estado da UI muda, onde erros são capturados para exibição, e onde fluxos assíncronos são coordenados.

Centralizar observabilidade no ViewModel significa:

1. Um único arquivo a modificar por feature (o ViewModel)
2. Testes existentes do ViewModel podem ser estendidos com verificações de `logger` e `analytics` usando `mockk(relaxed = true)` sem setup adicional
3. Erros que sobem até o ViewModel — que são os erros relevantes para o usuário — são todos capturados

A cobertura de Repository/DataSource (Opção C) pode ser adicionada em uma iteração futura sem conflito com esta decisão.

## Consequências

- `AppLogger`, `AnalyticsTracker` e `PerformanceTracker` são injetados no construtor dos ViewModels de cada feature
- As funções Koin `viewModel { }` em `homeModule`, `characterDetailsModule` e `chatModule` são atualizadas para incluir `get()` para cada nova dependência
- Nenhuma mudança em Repository, UseCase ou DataSource nesta iteração
- Testes dos ViewModels usam `mockk<AppLogger>(relaxed = true)` e `mockk<AnalyticsTracker>()` como parâmetros de construtor
- Para adicionar logging de infra no futuro: injetar `AppLogger` nos Repositories sem conflito com esta decisão