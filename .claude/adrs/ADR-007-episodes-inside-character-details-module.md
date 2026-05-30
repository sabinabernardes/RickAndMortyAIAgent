# ADR-007 — Episódios dentro de `:feature:character_details` (sem novo módulo)

**Status:** Aceito  
**Data:** 2026-05-30  
**Feature:** Lista de episódios na tela de detalhes

---

## Contexto

Ao adicionar a funcionalidade de listar episódios na tela de detalhes do personagem, surgiu a questão: criar um módulo separado `:feature:episodes` ou colocar tudo dentro do módulo `:feature:character_details` já existente?

## Opções Avaliadas

### Opção A: Dentro de `:feature:character_details`
- **Prós:** Zero overhead de setup (sem novo `build.gradle.kts`, namespace, DI module separado), episódios só fazem sentido no contexto da tela de detalhes por ora, menos complexidade no grafo de dependências
- **Contras:** O módulo cresce; se episódios forem usados em outra tela no futuro, será necessário extrair

### Opção B: Novo módulo `:feature:episodes`
- **Prós:** Reutilizável por outras features, separação de responsabilidades mais clara
- **Contras:** Overhead considerável de setup (build.gradle, namespace, Koin module, ajuste no app), nenhuma outra tela usa episódios atualmente — seria YAGNI

## Decisão

**Escolhida: Opção A — dentro de `:feature:character_details`**

## Justificativa

Episódios, neste estágio do app, são um detalhe do personagem — não uma entidade independente com tela própria. Criar um módulo separado só para satisfazer uma possível expansão futura viola YAGNI. Se episódios ganharem tela própria no futuro, a extração para um módulo é uma refatoração localizada e mecânica.

## Consequências

- Novo código de episódios vive em subpastes do pacote `com.bina.character_details` (ex: `data/model/EpisodeData.kt`)
- O `characterDetailsModule` Koin absorve as novas bindings
- Se uma tela de listagem de episódios for criada no futuro, extrair para `:feature:episodes` será o caminho