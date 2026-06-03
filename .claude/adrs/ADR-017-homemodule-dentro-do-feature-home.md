# ADR-017 — homeModule dentro de :feature:home

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulo:** `:feature:home`

---

## Contexto

O `homeModule` do Koin está definido em `app/src/main/java/com/bina/rickandmorty/di/Modules.kt`, junto com o `networkModule` e o `keysModule`. Os outros dois módulos de feature têm seus próprios arquivos de DI:

- `feature/character_details/…/di/CharacterDetailsModule.kt` → `characterDetailsModule`
- `feature/chat/…/di/ChatModule.kt` → `chatModule`
- `app/…/di/Modules.kt` → `homeModule` ← fora do padrão

Isso cria inconsistência: quem precisa entender a injeção do `HomeViewModel` precisa saber procurar fora do módulo `:feature:home`.

## Opções Avaliadas

### Opção A: Manter em `app/Modules.kt`

- **Prós:** Nenhuma mudança necessária agora
- **Contras:** Quebra a convenção estabelecida pelos outros dois features; qualquer novo colaborador vai procurar `HomeModule.kt` dentro de `:feature:home` e não vai encontrar; a lógica de DI da feature vaza para o `:app`

### Opção B: Criar `HomeModule.kt` dentro de `:feature:home` e remover `homeModule` de `app/Modules.kt`

```kotlin
// feature/home/src/main/java/com/bina/home/di/HomeModule.kt
val homeModule = module {
    factory<CharacterDataSource> { CharacterDataSourceImpl(get()) }
    factory<HomeRepository> { HomeRepositoryImpl(get()) }
    factory { GetCharactersUseCase(get()) }
    factory { CharacterUiMapper() }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
}
```

```kotlin
// app/…/Modules.kt — só referencia, não define
val appModules = listOf(loggingModule, analyticsModule, networkModule, homeModule, …)
```

- **Prós:** Consistente com todos os outros features; a DI da feature mora dentro da feature; fácil de encontrar; `:feature:home` fica autocontido
- **Contras:** Requer um arquivo novo e uma remoção — mudança pequena mas necessária

## Decisão

**Escolhida: Opção B — criar `HomeModule.kt` em `:feature:home`**

## Justificativa

O padrão já existe nos outros dois módulos. Mantê-lo uniformemente é mais importante do que evitar a criação de um arquivo. Consistência reduz carga cognitiva: qualquer engenheiro que entra no projeto e abre `feature/home/` vai encontrar o DI do home lá, da mesma forma que encontraria em `feature/chat/` ou `feature/character_details/`.

## Consequências

- Novo arquivo: `feature/home/src/main/java/com/bina/home/di/HomeModule.kt`
- `homeModule` removido de `app/Modules.kt`
- Import de `homeModule` adicionado em `app/Modules.kt` (vindo de `:feature:home`)
- Nenhuma mudança funcional — apenas relocação