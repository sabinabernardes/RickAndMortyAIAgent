# ADR-001 — Escolha do Provedor de IA para o Chat

**Status:** Aceito
**Data:** 2026-05-29
**Feature:** `:feature:chat`

---

## Contexto

Precisávamos de um provedor de IA para implementar uma tela de chat no app Rick and Morty. Os requisitos eram: gratuito, sem custo recorrente por uso, e preferencialmente sem depender de internet em runtime.

## Opções Avaliadas

### Opção A: Firebase AI Logic — Gemini Developer API (Cloud)
- **Prós:** Funciona em qualquer dispositivo, SDK oficial Firebase, 1.500 req/dia grátis, fácil integração
- **Contras:** Depende de internet, tem cota (1.500 req/dia), requer conta Firebase e API key

### Opção B: ML Kit GenAI Prompt API — On-Device (Gemini Nano via AICore)
- **Prós:** Completamente gratuito sem cota, sem internet, sem API key, privacidade total
- **Contras:** Só funciona em dispositivos físicos compatíveis (Pixel 9+, Galaxy S24+), não funciona em emuladores

### Opção C: MediaPipe LLM Inference — Gemma-3 On-Device
- **Prós:** Completamente gratuito, sem cota, modelo Gemma-3 1B local
- **Contras:** Exige dispositivos de topo (Pixel 8+, Galaxy S23+, mín. 8GB RAM), download de ~1GB do modelo

### Opção D: OpenAI (GPT)
- **Prós:** Alta qualidade de resposta, ampla documentação
- **Contras:** Sem SDK Android oficial, custo por token, não tem tier 100% gratuito sustentável

## Decisão

**Escolhida: Opção B — ML Kit GenAI Prompt API (on-device)**

## Justificativa

- Alinhado ao objetivo de custo zero sem limite de requisições
- Privacidade: dados do usuário nunca saem do dispositivo
- Integração simples com um único artefato Gradle
- A limitação de dispositivos é aceitável para um app de portfólio/MVP

## Consequências

- A tela deve tratar o estado `ModelUnavailable` explicitamente
- Emuladores não podem ser usados para testar esta feature
- O `ChatDataSourceImpl` encapsula o ML Kit — substituição futura por cloud é localizada nesta classe

## Caminho de Evolução

Se futuramente quisermos suporte a todos os dispositivos, o padrão sugerido é:

```
checkAvailability()
  ├─ AVAILABLE → ML Kit on-device
  └─ UNAVAILABLE → Firebase AI Logic (Gemini 2.5 Flash) como fallback
```

Isso pode ser implementado em `ChatRepositoryImpl` sem alterar nenhuma outra camada.