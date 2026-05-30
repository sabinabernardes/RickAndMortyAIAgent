# SDD — Lista de Episódios na Tela de Detalhes

**Módulo:** `:feature:character_details`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-05-30

---

## 1. Contexto e Objetivo

A API Rick & Morty já retorna, no endpoint `GET /character/{id}`, o campo `episode: List<String>` contendo as URLs dos episódios em que o personagem aparece (ex: `https://rickandmortyapi.com/api/episode/1`). Atualmente esse dado é descartado no `CharacterDetailsMapper` e nenhuma informação de episódio é exibida na tela.

O objetivo é aproveitar esse dado para buscar os episódios via `GET /episode/{ids}` e exibi-los como uma nova seção scrollável na tela de detalhes, após as informações existentes (Status, Origem, Localização).

---

## 2. Decisões Técnicas

Ver ADRs relacionados:
- [ADR-007](../adrs/ADR-007-episodes-inside-character-details-module.md) — Episódios dentro de `:feature:character_details` (sem novo módulo)
- [ADR-008](../adrs/ADR-008-multi-id-episode-request.md) — Busca com múltiplos IDs em uma única request
- [ADR-009](../adrs/ADR-009-incremental-loading-episodes.md) — Carregamento incremental (personagem primeiro, episódios depois)
- [ADR-010](../adrs/ADR-010-api-single-object-vs-array.md) — Tratamento do quirk da API (objeto único vs array)

---

## 3. Arquitetura

O módulo segue o padrão **Clean Architecture** já estabelecido no projeto.

```
Presentation (Compose + ViewModel + StateFlow)
      ↓
Domain (EpisodeDomain, GetEpisodesUseCase, EpisodeRepository interface)
      ↓
Data (EpisodeApiService, EpisodeDataSource, EpisodeRepositoryImpl, EpisodeMapper)
```

Tudo coexiste dentro de `:feature:character_details`, sem novo módulo.

---

## 4. Fluxo de Dados

```
LaunchedEffect(characterId)
  ↓
CharacterDetailsViewModel.getCharacterDetails(id)
  ↓
GetCharacterDetailsUseCase(id)
  → CharacterDetailsData.episode: List<String>  (URLs dos episódios)
  → _uiState = Success(character, episodesState = Loading)  ← personagem já aparece na tela
  ↓
Extrai IDs das URLs: url.substringAfterLast("/").toInt()
  ↓
GetEpisodesUseCase(ids: List<Int>)
  → EpisodeApiService.getEpisodes("1,2,3")  GET /episode/1,2,3
  → EpisodeMapper.toDomain(data)
  → EpisodeUiMapper.map(domain)
  ↓
_uiState.update { Success(character, episodesState = Success(episodes)) }
```

Em caso de falha na busca de episódios:
```
_uiState.update { Success(character, episodesState = Error(message)) }
```
O personagem continua visível — apenas a seção de episódios exibe erro.

---

## 5. Modelos por Camada

### Data
```kotlin
data class EpisodeData(
    @SerializedName("id")       val id: Int,
    @SerializedName("name")     val name: String,
    @SerializedName("episode")  val episode: String,   // "S01E01"
    @SerializedName("air_date") val airDate: String
)
```

### Domain
```kotlin
data class EpisodeDomain(
    val id: Int,
    val name: String,
    val code: String,      // "S01E01"
    val airDate: String
)

// CharacterDetailsDomain recebe campo adicional:
val episodeUrls: List<String>
```

### Presentation
```kotlin
data class EpisodeUiModel(
    val id: Int,
    val name: String,
    val code: String,
    val airDate: String
)

// CharacterDetailsUiState.Success recebe sub-estado:
data class Success(
    val character: CharacterDetailsUiModel,
    val episodesState: EpisodesState = EpisodesState.Loading
) : CharacterDetailsUiState()

sealed class EpisodesState {
    object Loading : EpisodesState()
    data class Success(val episodes: List<EpisodeUiModel>) : EpisodesState()
    data class Error(val message: String?) : EpisodesState()
}
```

---

## 6. Layout na Tela de Detalhes

A tela inteira usa `verticalScroll` (já existente). A hero image mantém 320dp; os episódios aparecem abaixo do conteúdo atual e o usuário faz scroll para ver.

```
╔════════════════════════════════════╗  ← topo da tela
║  ← Voltar                          ║  ← PillBackButton (flutuante, já existe)
║                                    ║
║         HERO IMAGE 320dp           ║  ← sem compressão
║                          [Alive ●] ║
╠════════════════════════════════════╣  ← Surface card arredondado (já existe)
║ Rick Sanchez                       ║
║ Human · Male                       ║
║ ─────────────────────────────────  ║
║ Status         Alive               ║  ← DetailItems (já existem)
║ Origem         Earth (C-137)       ║
║ Localização    Citadel of Ricks    ║
║ ─────────────────────────────────  ║  ← novo divider
║ Aparece em                   51    ║  ← label + contagem de episódios
║                                    ║
║  S01E01  Pilot                     ║  ← EpisodeCard
║          December 2, 2013          ║
║ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ║
║  S01E02  Lawnmower Dog             ║
║          December 9, 2013          ║
║  ...                               ║  ↑ scroll continua
╚════════════════════════════════════╝
```

**Estado de loading** (3 placeholders animados):
```
║ Aparece em                         ║
║  ░░░░░░  ░░░░░░░░░░░░░░░░░░        ║
║  ░░░░░░  ░░░░░░░░░░░░░░░░░░        ║
║  ░░░░░░  ░░░░░░░░░░░░░░░░░░        ║
```

**Estado de erro:**
```
║ Aparece em                         ║
║  Não foi possível carregar         ║
║  os episódios.                     ║
```

### EpisodeCard
- Linha 1: código `S01E01` em `primary` (bold) + nome do episódio em `onSurface`
- Linha 2: data de exibição em `onSurface.copy(alpha = 0.55f)`
- `fadeSlideIn(index)` para entrada animada (mesmo padrão dos `DetailItem`)
- Separados por `HorizontalDivider` com `outlineVariant`
- Sem clique (sem tela de detalhe de episódio neste escopo)

---

## 7. Arquivos

### Criar (11 arquivos)

| Camada | Arquivo |
|--------|---------|
| `data/model` | `EpisodeData.kt` |
| `data/remote` | `EpisodeApiService.kt` |
| `data/datasource` | `EpisodeDataSource.kt`, `EpisodeDataSourceImpl.kt` |
| `data/mapper` | `EpisodeMapper.kt` |
| `data/repository` | `EpisodeRepositoryImpl.kt` |
| `domain/model` | `EpisodeDomain.kt` |
| `domain/repository` | `EpisodeRepository.kt` |
| `domain/usecase` | `GetEpisodesUseCase.kt` |
| `presentation/model` | `EpisodeUiModel.kt` |
| `presentation/mapper` | `EpisodeUiMapper.kt` |
| `presentation/state` | `EpisodesState.kt` |

### Modificar (7 arquivos)

| Arquivo | O que muda |
|---------|------------|
| `CharacterDetailsDomain.kt` | + `episodeUrls: List<String>` |
| `CharacterDetailsMapper.kt` | mapear `data.episode` para `episodeUrls` |
| `CharacterDetailsUiState.kt` | `Success` ganha `episodesState: EpisodesState` |
| `CharacterDetailsUiMapper.kt` | assinatura compatível com novo domínio |
| `CharacterDetailsViewModel.kt` | 2 novos injects + lógica de carregamento incremental |
| `CharacterDetailsScreen.kt` | + `EpisodeListSection` + `EpisodeCard` composables |
| `CharacterDetailsModule.kt` | + 5 novas bindings Koin |

---

## 8. Critérios de Aceite

- [ ] Tela de detalhes exibe dados do personagem imediatamente
- [ ] Seção "Aparece em" aparece com loading skeleton enquanto episódios carregam
- [ ] Episódios são listados com código (`S01E01`), nome e data
- [ ] Personagem com 1 episódio renderiza corretamente (sem quebrar por quirk da API)
- [ ] Personagem com muitos episódios (ex: Rick, 51 ep.) é scrollável
- [ ] Erro de rede na busca de episódios exibe mensagem sem derrubar a tela inteira
- [ ] Build sem erros: `./gradlew :feature:character_details:assembleDebug`