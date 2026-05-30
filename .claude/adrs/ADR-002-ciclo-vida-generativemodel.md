# ADR-002 — Ciclo de Vida do GenerativeModel (Koin single)

**Status:** Aceito
**Data:** 2026-05-29
**Feature:** `:feature:chat`

---

## Contexto

O `GenerativeModel` do ML Kit é um objeto caro de criar (inicializa o runtime de IA do AICore). Precisávamos decidir onde instanciá-lo e como gerenciar seu ciclo de vida.

## Opções Avaliadas

### Opção A: Koin `factory{}` — nova instância por ViewModel
- **Prós:** Ciclo de vida simples, sem estado compartilhado
- **Contras:** Custo de inicialização repetido a cada entrada na tela, `warmup()` chamado repetidamente

### Opção B: Koin `single{}` — instância única no processo
- **Prós:** Inicialização feita uma única vez, `warmup()` carrega os recursos uma vez, reutilizado entre navegações
- **Contras:** Requer cuidado com `close()` para liberar recursos

### Opção C: Singleton manual no `Application`
- **Prós:** Controle explícito de inicialização
- **Contras:** Acoplamento ao Application, bypass do sistema de DI

## Decisão

**Escolhida: Opção B — Koin `single{}`**

## Justificativa

A criação do `GenerativeModel` inicializa o runtime do AICore — operação cara que não deve ser repetida. Com `single{}`, a instância é criada uma vez e reutilizada entre navegações.

## Contrato de Ciclo de Vida

```
ViewModel.init
  └─ checkAvailability()
       └─ AVAILABLE → repository.warmup() (via viewModelScope)

ViewModel.onCleared()
  └─ repository.close() → dataSource.close() → generativeModel.close()
```

O `onCleared()` é chamado quando o usuário sai definitivamente da tela. Se o usuário voltar, o Koin entrega a mesma instância e o novo `ChatViewModel` chama `warmup()` novamente — comportamento idempotente conforme docs do ML Kit.

## Consequências

- ML Kit requer que a inferência ocorra apenas com o app em foreground. O ciclo de vida do ViewModel (destruído quando a Activity para de estar visível) garante isso naturalmente.
- O `close()` libera memória do modelo após a saída da tela. O próximo `warmup()` recarrega em ~300ms.
- Não há leak: Koin não mantém referência circular com o ViewModel.