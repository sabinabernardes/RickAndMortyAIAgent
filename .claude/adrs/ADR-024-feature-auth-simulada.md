# ADR-024 — Feature Auth Simulada para Estudo de Segurança

**Status:** Accepted  
**Data:** 2026-06-04  
**Autor:** Sabina Bernardes  
**Módulos afetados:** `:feature:auth`, `:core:security`

---

## Contexto

O app Rick & Morty AI não possui autenticação real — não há backend, não há dados de usuário sensíveis em produção. Ainda assim, surgiu a necessidade de **estudar e demonstrar padrões de segurança em Android** dentro do próprio projeto: armazenamento seguro de tokens, sanitização de inputs, separação de responsabilidades no fluxo de autenticação, e boas práticas de UI para dados sensíveis.

O objetivo é criar uma implementação didática e realista, sem introduzir dependências de infraestrutura (Firebase, servidores, etc.) desnecessárias.

---

## Decisão

Criar dois módulos:

- **`:core:security`** — módulo reutilizável com `SecureStorage` (interface + impl via `EncryptedSharedPreferences`)
- **`:feature:auth`** — feature de autenticação simulada com validação local de credenciais, token JWT mock, e tela de login em Compose

A autenticação será **local** (sem chamada de rede real): qualquer email válido + senha ≥ 8 caracteres autentica com sucesso. O token é salvo via `EncryptedSharedPreferences`. O login sempre aparece ao abrir o app (sem redirect automático por sessão — o foco é no armazenamento seguro, não em session management).

---

## Alternativas Avaliadas

### Firebase Authentication
- Requer projeto Firebase, google-services.json, backend implícito
- Fora de escopo: o objetivo é estudar segurança local, não integração com BaaS
- Introduziria dep pesada desnecessária para um projeto educacional

### Room + SQLCipher para persistência
- Mais adequado para persistência de múltiplos dados estruturados
- Excessivo para armazenar apenas um token e email
- `EncryptedSharedPreferences` é a solução oficial do Jetpack para key-value seguro

### Credenciais hardcoded (user/pass fixos)
- Mais simples, mas não exercita validação no domínio
- Preferida a validação por regras: email regex + senha ≥ 8 chars
- Cenário mais próximo de uma API real de autenticação

---

## Motivação das Escolhas

**Autenticação local com regras:** exercita o domínio (validação em `LoginUseCase`, não na UI), simula latência de rede com `delay(800)`, e retorna erros tipados que a UI exibe sem revelar qual campo falhou — padrão de segurança que evita enumeração de usuários.

**JWT mock estruturado:** o token gerado segue o formato `Base64(header).Base64(payload).fakeSignature`, onde o payload contém `email`, `iat` (issued at) e `exp` (expiry). Isso permite estudar a estrutura de um JWT real sem precisar de uma lib de criptografia assimétrica.

**`EncryptedSharedPreferences`:** API oficial do Jetpack Security, usa AES256-GCM para valores e AES256-SIV para chaves. A chave mestra é gerada pelo Android Keystore — vinculada ao hardware do dispositivo. Escolha consciente: mais simples que TinkCrypto, adequada para key-value, sem deps externas além do Jetpack.

**Validação no domínio, não na UI:** `LoginUseCase` sanitiza o input (trim + lowercase no email) antes de repassar ao repositório. A UI só exibe o erro retornado, nunca valida diretamente.

---

## Estrutura

```
:core:security
└── storage/
    ├── SecureStorage.kt          (interface)
    └── EncryptedPrefsStorage.kt  (impl)

:feature:auth
├── domain/
│   ├── model/        AuthResult, UserSession
│   ├── repository/   AuthRepository (interface)
│   └── usecase/      LoginUseCase
├── data/
│   └── repository/   AuthRepositoryImpl
└── presentation/
    ├── state/         LoginUiState
    ├── viewmodel/     LoginViewModel
    └── view/          LoginScreen
```

---

## Boas Práticas de Segurança Implementadas

| Camada | Prática |
|--------|---------|
| UI | `PasswordVisualTransformation`, sem log de senha, mensagens de erro genéricas |
| Domínio | Sanitização de input no UseCase, validação tipada (não booleanos) |
| Dados | `delay(800)` simula latência; erro genérico para credenciais inválidas (não revela qual campo) |
| Storage | `EncryptedSharedPreferences` + Android Keystore |
| DI | `SecureStorage` injetado como interface — facilita fake em testes |

---

## Consequências

**Positivas:**
- Demonstra separação de responsabilidades em autenticação dentro do padrão do projeto
- `:core:security` é reutilizável por outros módulos futuros (ex: armazenar preferências sensíveis)
- Testes de `AuthRepositoryImpl` usam fake de `SecureStorage` (em memória) — sem mocks de sistema
- LoginViewModel segue exatamente o mesmo padrão de ChatViewModel (StateFlow, Koin, AppLogger, AnalyticsTracker)

**Negativas:**
- Token JWT mock não é verificável (sem assinatura real) — limitação educacional aceitável
- `EncryptedSharedPreferences 1.1.0-alpha06` ainda em alpha — adequado para estudo, não para produção sem avaliação
- Login sempre aparece (sem session check) — decisão consciente para simplificar e focar no storage

---

## Links

- ADR relacionado: [ADR-015](ADR-015-viewmodel-como-camada-de-observabilidade.md) — padrão de ViewModel com Logger/Analytics
- [Jetpack Security Crypto](https://developer.android.com/reference/kotlin/androidx/security/crypto/EncryptedSharedPreferences)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
