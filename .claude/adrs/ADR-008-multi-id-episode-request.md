# ADR-008 — Busca de episódios com múltiplos IDs em uma única request

**Status:** Aceito  
**Data:** 2026-05-30  
**Feature:** Lista de episódios na tela de detalhes

---

## Contexto

O `CharacterDetailsData.episode` retorna uma lista de URLs como `https://rickandmortyapi.com/api/episode/1`. Para exibir os episódios, precisamos buscar seus dados. A API Rick & Morty suporta duas abordagens:

1. `GET /episode/1` — um episódio por vez
2. `GET /episode/1,2,3` — múltiplos episódios em uma request, retorna array

## Opções Avaliadas

### Opção A: Uma request com múltiplos IDs (`GET /episode/1,2,3`)
- **Prós:** Apenas 1 chamada de rede independente da quantidade de episódios; Rick Sanchez, por exemplo, aparece em 51 episódios — seriam 51 requests na opção B
- **Contras:** Quirk da API: quando há apenas 1 ID, retorna objeto JSON `{...}` em vez de array `[{...}]`, requerendo tratamento especial no DataSource

### Opção B: Requests paralelas por ID (`async/awaitAll`)
- **Prós:** Implementação mais simples (sem quirk de tipo de resposta), cada request é independente
- **Contras:** N chamadas de rede simultâneas; para personagens com muitos episódios gera pressão desnecessária na rede e no servidor

## Decisão

**Escolhida: Opção A — request única com múltiplos IDs**

## Justificativa

1 request vs N requests é uma diferença relevante para personagens principais (Rick: 51 ep., Morty: 51 ep.). O quirk de retorno é tratado localmente no `EpisodeDataSourceImpl` com um check simples:

```kotlin
if (ids.size == 1) {
    listOf(apiService.getEpisodeSingle(ids.first()))
} else {
    apiService.getEpisodes(ids.joinToString(","))
}
```

Isso encapsula o comportamento irregular da API sem vazar para outras camadas.

## Consequências

- `EpisodeApiService` precisa de dois métodos: `getEpisodeSingle(id: Int): EpisodeData` e `getEpisodes(ids: String): List<EpisodeData>`
- A lógica de split está em `EpisodeDataSourceImpl`, transparente para o repositório e UseCase
- IDs são extraídos das URLs com `url.substringAfterLast("/").toInt()` no ViewModel antes de chamar o UseCase