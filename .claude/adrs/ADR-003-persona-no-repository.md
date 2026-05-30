# ADR-003 — Localização da Persona do Rick no Repository

**Status:** Aceito
**Data:** 2026-05-29
**Feature:** `:feature:chat`

---

## Contexto

A IA precisa responder no tom do Rick Sanchez. Isso requer um "system prompt" (instrução de persona) prefixado em cada mensagem. O ML Kit GenAI Prompt API não possui um campo `systemPrompt` dedicado — a instrução deve ser concatenada ao prompt do usuário. Precisávamos decidir onde montar esse prompt completo.

## Opções Avaliadas

### Opção A: No ViewModel (Presentation layer)
- **Prós:** Visível para quem lê o ViewModel
- **Contras:** Viola separação de camadas — a Presentation não deve conhecer detalhes de prompt engineering. Dificulta troca de provedor de IA.

### Opção B: No UseCase (Domain layer)
- **Prós:** Centralizado na lógica de negócio
- **Contras:** O Domain deve ser puro e independente de implementação. Embutir um prompt de IA no Domain acopla a lógica à escolha do provedor.

### Opção C: No Repository (Data layer)
- **Prós:** Isolado na camada de dados; troca de provedor não afeta Domain nem Presentation; fácil de testar e iterar
- **Contras:** Nenhum relevante

### Opção D: No DataSource
- **Prós:** Mais próximo do provedor
- **Contras:** O DataSource deve ser genérico (poderia ser reutilizado por outros casos de uso). A persona é específica do caso de uso "chat Rick and Morty".

## Decisão

**Escolhida: Opção C — Repository (`ChatRepositoryImpl`)**

## Justificativa

O Repository é o ponto onde a intenção de negócio (responder sobre Rick and Morty) encontra a implementação técnica (ML Kit). É o lugar correto para traduzir a mensagem do usuário em um prompt completo para o modelo.

## Implementação

```kotlin
private const val RICK_PERSONA = """Você é um especialista apaixonado no universo de Rick and Morty.
Responda com o tom sarcástico, brilhante e impaciente do Rick Sanchez..."""

override fun streamResponse(userMessage: String): Flow<String> {
    val fullPrompt = RICK_PERSONA + userMessage
    return dataSource.sendMessageStream(fullPrompt)
}
```

## Consequências

- Para mudar a persona, edita-se apenas `ChatRepositoryImpl` — zero impacto em Domain e Presentation.
- Para adicionar um provedor cloud com `systemPrompt` nativo (ex: Firebase AI Logic), basta criar um `ChatRepositoryCloudImpl` diferente sem alterar a interface.
- O `ChatDataSource` recebe apenas o prompt final, sem saber que há uma persona embutida.