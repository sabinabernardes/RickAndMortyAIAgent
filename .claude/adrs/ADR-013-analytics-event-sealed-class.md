# ADR-013 — AnalyticsEvent como sealed class por domínio

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulo:** `:core:analytics`

---

## Contexto

Ao rastrear eventos de analytics, é preciso decidir como representar cada evento no código. Um evento carrega:
- Um **nome** identificador (ex: `"character_clicked"`)
- **Propriedades** opcionais (ex: `characterId = "123"`)

A questão é como modelar isso de forma que seja type-safe, extensível por feature e fácil de serializar para qualquer backend.

## Opções Avaliadas

### Opção A: Strings hardcoded nos call sites
```kotlin
analyticsTracker.track("character_clicked", mapOf("id" to characterId))
analyticsTracker.track("search_performed", mapOf("query" to query))
```

- **Prós:** Zero boilerplate, imediato
- **Contras:** Typos são erros silenciosos em runtime; refactoring não atualiza todos os usos automaticamente; impossível saber quais eventos existem sem busca textual no código; propriedades são `Any` — sem garantia de tipo

### Opção B: Constantes de string em um objeto global
```kotlin
object AnalyticsEvents {
    const val CHARACTER_CLICKED = "character_clicked"
    const val SEARCH_PERFORMED = "search_performed"
}
analyticsTracker.track(AnalyticsEvents.CHARACTER_CLICKED, mapOf("id" to characterId))
```

- **Prós:** Sem typos nos nomes; centralizado
- **Contras:** Propriedades ainda são `Map<String, Any>` sem type-safety; crescimento do objeto global conforme o app cresce; sem agrupamento por domínio/feature

### Opção C: Sealed classes por domínio com interface marker base
```kotlin
// Em :core:analytics
interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String> get() = emptyMap()
}

// Em :feature:home
sealed class HomeEvent : AnalyticsEvent {
    data class CharacterClicked(val characterId: String) : HomeEvent() {
        override val name = "home_character_clicked"
        override val properties = mapOf("character_id" to characterId)
    }
    data class SearchPerformed(val query: String) : HomeEvent() {
        override val name = "home_search_performed"
        override val properties = mapOf("query" to query)
    }
}
```

- **Prós:** Type-safe — propriedades são campos Kotlin tipados; o compilador garante que nenhum evento fique sem `name`; `when` exhaustivo em testes; agrupado por domínio; refactoring seguro; fácil de serializar para qualquer backend via `name` + `properties`
- **Contras:** Mais boilerplate por evento

## Decisão

**Escolhida: Opção C — sealed classes por domínio com interface marker `AnalyticsEvent`**

## Justificativa

Analytics é uma camada que cresce ao longo da vida do produto. Erros de nome de evento ou propriedade ausente são difíceis de detectar — chegam como dados incorretos no dashboard, muitas vezes após semanas. O compilador Kotlin é o melhor "teste" para isso.

A interface marker `AnalyticsEvent` vive em `:core:analytics`. Cada feature define seus próprios eventos como sealed classes no seu próprio módulo — sem dependência circular.

**Eventos planejados por feature:**

| Feature | Evento | Propriedades |
|---|---|---|
| Home | `CharacterClicked` | `characterId` |
| Home | `SearchPerformed` | `query` |
| Home | `PaginationLoadedNextPage` | `page` |
| CharacterDetails | `ScreenOpened` | `characterId` |
| CharacterDetails | `EpisodesLoaded` | `episodeCount` |
| Chat | `MessageSent` | — |
| Chat | `ModelUnavailable` | — |
| Chat | `AgentNavigationTriggered` | `action` |

## Consequências

- `AnalyticsEvent` (interface) e `AnalyticsTracker` ficam em `:core:analytics`
- Features declaram seus eventos como sealed classes no próprio módulo (sem dependência entre features)
- `AnalyticsTracker.track(event: AnalyticsEvent)` recebe qualquer evento; implementação converte via `event.name` e `event.properties`
- Adicionar novo evento = criar nova data class na sealed class da feature — compilador avisa se algo está faltando