# ADR-006 — Pivot: Gemini API Direta com BuildConfig (substitui ML Kit on-device)

**Status:** Aceito  
**Data:** 2026-05-29  
**Feature:** `:feature:chat`  
**Substitui:** ADR-001 (decisão original pelo ML Kit GenAI)

---

## Contexto

A implementação original com **ML Kit GenAI Prompt API** (ADR-001) foi testada em dispositivo real e falhou no **Samsung Galaxy Z Flip 6** — o dispositivo não é suportado pelo AICore (infraestrutura do Gemini Nano), confirmando a incerteza já registrada no SDD.

A decisão de migrar para **MediaPipe LLM Inference** (Gemma 3 1B on-device) foi iniciada mas bloqueada antes de chegar a um build funcional por um problema não mapeado antes da implementação: o modelo requer autenticação no Hugging Face para download, o que contradiz o requisito de "sem credenciais" e adiciona complexidade de UX (tela de download de ~700MB, gerenciamento de token HF).

## Restrições Revisadas

Após o bloqueio, os requisitos foram refinados:

- Sem credenciais visíveis ao usuário em nenhuma tela
- Sem download de modelo in-app
- Funcionar no Z Flip 6
- Gratuito para portfólio/demo
- Simples de manter

## Opções Avaliadas nesta Revisão

### Opção A: MediaPipe LLM Inference (on-device, Gemma 3 1B)
- **Prós:** 100% offline após setup, sem custo por requisição
- **Contras:** Download de ~700MB exige URL autenticada (Hugging Face), ou sideload manual via ADB. Complexidade de UX incompatível com portfólio. Bloqueado.

### Opção B: Sideload via ADB (on-device, sem download in-app)
- **Prós:** Zero credencial, zero download no app, funciona offline
- **Contras:** Requer passo manual do desenvolvedor antes de cada demo. Não funciona para quem baixa o app sem preparação prévia.

### Opção C: Gemini API direta com chave no `BuildConfig` ✅
- **Prós:** Funciona em qualquer dispositivo (incluindo Z Flip 6), zero friction para o usuário, chave configurada uma vez no `local.properties` (fora do git), SDK oficial Google, free tier generoso (1M tokens/dia, 15 req/min)
- **Contras:** Requer internet, chave embutida no APK (aceitável para portfólio)

### Opção D: Firebase AI Logic
- **Contras:** Exige projeto Firebase, autenticação OAuth, configuração de `google-services.json`. Descartada — viola o requisito de simplicidade.

## Decisão

**Escolhida: Opção C — Gemini API direta com `BuildConfig.GEMINI_API_KEY`**

A chave é obtida em `aistudio.google.com` (gratuito, sem cartão), armazenada em `local.properties` e exposta via `buildConfigField` no `build.gradle.kts`. Nenhuma tela de login, nenhum download, nenhuma dependência do Firebase.

## Consequências

- `ChatDataSourceImpl` passa a usar `com.google.ai.client.generativeai:generativeai`
- O método `downloadModel()` é removido de `ChatDataSource`, `ChatRepository` e `ChatViewModel`
- `ModelAvailability.Downloadable` e `ModelAvailability.Downloading` deixam de ser usados nesta feature (podem ser removidos ou mantidos para evolução futura)
- `ChatUiState.ModelDownloadable` e `ChatUiState.ModelDownloading` idem
- O módulo `:feature:chat` passa a depender de internet — isso deve ser documentado no README
- A chave **nunca deve ser commitada** — garantido pelo `.gitignore` padrão do Android (que já exclui `local.properties`)
- `ADR-005` (sem core:network) permanece válido: a dependência de rede é encapsulada no SDK do Gemini, não exposta via `core:network`

## Lição Aprendida

> Antes de iniciar qualquer implementação de IA, mapear explicitamente: (1) como o modelo/chave chega ao dispositivo, (2) se há etapa de autenticação, (3) se essa etapa é aceitável para o público-alvo. Essas perguntas devem constar no checklist pré-implementação da feature.