# ADR-010 — Tratamento do quirk da API: objeto único vs array

**Status:** Aceito  
**Data:** 2026-05-30  
**Feature:** Lista de episódios na tela de detalhes

---

## Contexto

A Rick & Morty API tem um comportamento inconsistente no endpoint `GET /episode/{ids}`:

- `GET /episode/1,2,3` → retorna `[{...}, {...}, {...}]` (array JSON)
- `GET /episode/1` → retorna `{...}` (objeto JSON, **não** array)

Se o tipo de retorno do Retrofit for `List<EpisodeData>`, a segunda forma causa um `JsonSyntaxException` porque Gson tenta fazer parse de um objeto como array.

## Opções Avaliadas

### Opção A: Dois métodos no ApiService
```kotlin
@GET("episode/{id}")
suspend fun getEpisodeSingle(@Path("id") id: Int): EpisodeData

@GET("episode/{ids}")
suspend fun getEpisodes(@Path("ids") ids: String): List<EpisodeData>
```
O `EpisodeDataSourceImpl` escolhe qual chamar com base em `ids.size`.

- **Prós:** Simples, sem adaptador customizado, sem reflection, sem biblioteca extra
- **Contras:** Dois métodos no ApiService para o mesmo recurso

### Opção B: Adaptador Gson customizado
Registrar um `TypeAdapterFactory` que detecta se o JSON começa com `[` ou `{` e normaliza para array.

- **Prós:** Um único método no ApiService
- **Contras:** Código de infraestrutura não-óbvio, afeta o Gson global ou precisa de anotação customizada, harder to debug

### Opção C: Buscar sempre por array (passar `"${id}"` com size > 1 sempre)
Não é possível: a API não aceita `GET /episode/1,` (trailing comma) como array.

## Decisão

**Escolhida: Opção A — dois métodos no ApiService, lógica de split no DataSource**

## Justificativa

A inconsistência da API é um detalhe de implementação que deve ser encapsulado na camada de dados. A Opção A é a mais explícita e fácil de debugar. O custo (dois métodos) é trivial. A lógica de decisão fica em `EpisodeDataSourceImpl`:

```kotlin
override suspend fun getEpisodes(ids: List<Int>): List<EpisodeData> {
    return if (ids.size == 1) {
        listOf(apiService.getEpisodeSingle(ids.first()))
    } else {
        apiService.getEpisodes(ids.joinToString(","))
    }
}
```

## Consequências

- `EpisodeApiService` expõe dois endpoints para o mesmo recurso — comentário inline documenta o motivo
- Nenhuma outra camada (repositório, UseCase, ViewModel) precisa saber desse quirk
- Personagens com exatamente 1 episódio são testados explicitamente nos critérios de aceite