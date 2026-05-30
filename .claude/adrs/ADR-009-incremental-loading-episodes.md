# ADR-009 — Carregamento incremental: personagem primeiro, episódios depois

**Status:** Aceito  
**Data:** 2026-05-30  
**Feature:** Lista de episódios na tela de detalhes

---

## Contexto

Para exibir os dados do personagem e seus episódios, o ViewModel precisa fazer duas calls de rede:
1. `GET /character/{id}` — dados do personagem
2. `GET /episode/{ids}` — dados dos episódios

A questão é: emitir `Success` somente quando ambas as calls terminam, ou emitir progressivamente?

## Opções Avaliadas

### Opção A: Carregar tudo antes de mostrar (loading único)
- **Prós:** Estado mais simples — `CharacterDetailsUiState` com apenas `Loading | Success | Error`
- **Contras:** O usuário fica na tela de loading esperando episódios que não impactam os dados principais; em conexões lentas, a experiência é claramente pior

### Opção B: Carregamento incremental (personagem primeiro, episódios depois)
- **Prós:** O usuário vê os dados do personagem imediatamente; a seção de episódios exibe um skeleton enquanto carrega; falha nos episódios não bloqueia a tela inteira
- **Contras:** `CharacterDetailsUiState.Success` precisa de um sub-estado `EpisodesState`, levemente mais complexo

## Decisão

**Escolhida: Opção B — carregamento incremental**

## Justificativa

Os dados do personagem (nome, status, origem, localização) são a informação principal da tela. Os episódios são um complemento. Bloquear a tela inteira esperando uma segunda call de rede degrada a percepção de performance sem necessidade. O custo de complexidade (um sealed class adicional `EpisodesState`) é baixo e localizado.

## Implementação no ViewModel

```kotlin
fun getCharacterDetails(id: Int) {
    viewModelScope.launch {
        _uiState.value = Loading
        try {
            val domain = getCharacterDetailsUseCase(id)
            _uiState.value = Success(uiMapper.map(domain), EpisodesState.Loading)
            // ↑ personagem visível, episódios carregando
            try {
                val ids = domain.episodeUrls.map { it.substringAfterLast("/").toInt() }
                val episodes = getEpisodesUseCase(ids).map(episodeUiMapper::map)
                _uiState.update { s ->
                    if (s is Success) s.copy(episodesState = EpisodesState.Success(episodes)) else s
                }
            } catch (e: Exception) {
                _uiState.update { s ->
                    if (s is Success) s.copy(episodesState = EpisodesState.Error(e.message)) else s
                }
            }
        } catch (e: Exception) {
            _uiState.value = Error(e.message)
        }
    }
}
```

## Consequências

- `CharacterDetailsUiState.Success` deixa de ser `data object` e vira `data class` com `episodesState: EpisodesState`
- A UI precisa tratar `EpisodesState.Loading`, `.Success` e `.Error` dentro do estado `Success` do personagem
- Falha de rede nos episódios é isolada — não afeta a exibição dos dados do personagem