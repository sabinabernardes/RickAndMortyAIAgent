# :feature:auth — Autenticação Simulada

> Módulo educacional para estudar padrões de segurança em autenticação Android: sanitização de inputs, separação de responsabilidades por camada (Clean Architecture), armazenamento seguro de tokens e UX de login. Nenhuma requisição real é feita — tudo roda localmente.

---

## Objetivo de estudo

| O que estudar | Como está implementado aqui |
|---|---|
| Onde ficam regras de negócio | Use case (domínio), não repositório nem ViewModel |
| Como proteger tokens | `EncryptedSharedPreferences` via `:core:security` |
| JWT — estrutura e verificação | Token mock com header/payload real em Base64, assinatura explicitamente fake |
| Sanitização de input | `email.trim().lowercase()` no use case antes de qualquer validação |
| Mensagens de erro seguras | `InvalidCredentials` não revela se o erro foi no email ou na senha |
| UX de loading | `CircularProgressIndicator` + botão desabilitado durante chamada suspensa |

---

## Arquitetura

```
feature/auth/
└── src/main/java/com/bina/auth/
    ├── domain/           ← regras de negócio, sem deps Android
    │   ├── model/        ← AuthResult, UserSession
    │   ├── repository/   ← interface AuthRepository
    │   └── usecase/      ← LoginUseCase (validação aqui)
    ├── data/
    │   └── repository/   ← AuthRepositoryImpl (só persistência)
    ├── presentation/
    │   ├── state/        ← LoginUiState
    │   ├── viewmodel/    ← LoginViewModel
    │   └── view/         ← LoginScreen (Compose)
    ├── analytics/        ← AuthEvent
    └── di/               ← AuthModule (Koin)
```

---

## Fluxo de dados

```
LoginScreen
    ↓ onLoginClicked(email, password)
LoginViewModel.onLoginClicked()
    ↓ LoginUseCase(email, password)
        ├── sanitiza: trim + lowercase
        ├── valida email (regex)
        ├── valida senha (≥ 8 chars)
        ├── bloqueia DEMO_BLOCKED_EMAIL
        └── AuthResult.Success → repository.login(email, password)
                                      ↓
                                  delay(800ms)      ← simula latência de rede
                                  buildMockJwt()
                                  secureStorage.save(token, email)
                                  → UserSession
    ↓ mapeia AuthResult → LoginUiState
LoginScreen reage ao StateFlow
```

---

## Camadas e responsabilidades

### Domínio

**AuthResult** — o que pode acontecer em uma tentativa de login:

```kotlin
sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    object InvalidEmail : AuthResult()
    object WeakPassword : AuthResult()
    object InvalidCredentials : AuthResult()  // não revela qual campo falhou
}
```

> `InvalidCredentials` existe para estudar a prática de segurança de não revelar se o erro foi no email ou na senha — evita enumeration attacks.

**LoginUseCase** — toda a validação fica aqui, não no repositório:

```kotlin
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        return when {
            !cleanEmail.matches(EMAIL_REGEX) -> AuthResult.InvalidEmail
            password.length < MIN_PASSWORD_LENGTH -> AuthResult.WeakPassword
            cleanEmail == DEMO_BLOCKED_EMAIL -> AuthResult.InvalidCredentials
            else -> AuthResult.Success(repository.login(cleanEmail, password))
        }
    }
}
```

**Por que a validação fica no use case e não no repositório?**

O repositório (camada de dados) deve ser responsável apenas por persistir e recuperar dados. Regras de negócio — como "email deve ser válido" — são domínio. Colocar validação no repositório viola a separação de camadas: um repositório diferente (ex: RemoteAuthRepository) precisaria duplicar as mesmas regras. No use case, a regra existe uma vez e vale para qualquer implementação de repositório.

---

### Dados

**AuthRepositoryImpl** — só persistência, sem lógica de negócio:

```kotlin
class AuthRepositoryImpl(private val secureStorage: SecureStorage) : AuthRepository {
    override suspend fun login(email: String, password: String): UserSession {
        delay(800)                        // simula latência
        val token = buildMockJwt(email)
        secureStorage.save(KEY_TOKEN, token)
        secureStorage.save(KEY_EMAIL, email)
        return UserSession(token, email)
    }
}
```

O repositório recebe `email` e `password` já validados — sua única responsabilidade é gerar o token e salvar.

**JWT mock** — estrutura real, assinatura fake:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ5MDAwMDAwLCJleHAiOjE3NDkwMDM2MDB9
.MOCK_SIGNATURE
```

Header e payload são Base64Url reais (decodificáveis em [jwt.io](https://jwt.io)). A assinatura é um literal `MOCK_SIGNATURE` — intencionalmente óbvio para fins educacionais. Em produção, a assinatura seria gerada com HMAC-SHA256 ou RS256 no servidor.

> **Por que `java.util.Base64` e não `android.util.Base64`?** A classe do Android não está disponível em testes JVM (unit tests rodam na JVM, sem o framework Android). `java.util.Base64` funciona em ambos os contextos.

---

### Apresentação

**LoginUiState**:

```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

**LoginViewModel** — padrão idêntico ao ChatViewModel do projeto:
- `StateFlow<LoginUiState>` para a UI
- `viewModelScope.launch` para a chamada suspensa
- `AppLogger` + `AnalyticsTracker` injetados via Koin
- Analytics deduplicados: um único `if/else` após o `when` (não 3x `track(LoginFailure)`)

**LoginScreen** — boas práticas na UI:
- `PasswordVisualTransformation()` — nunca mostra senha em texto puro
- `KeyboardType.Password` — hint ao sistema para não sugerir autocompletar
- Mensagem de erro genérica para `InvalidCredentials` — não diz qual campo falhou
- `StudyBanner` — banner visível indicando que a autenticação é simulada

---

## Testes

### LoginUseCaseTest (unit)
Cobre toda a lógica de validação:
- Email inválido → `AuthResult.InvalidEmail`
- Senha curta → `AuthResult.WeakPassword`
- Email bloqueado (demo) → `AuthResult.InvalidCredentials`
- Entrada válida → repositório chamado, retorna `AuthResult.Success`
- Sanitização: `" USER@EXAMPLE.COM "` normalizado para `"user@example.com"`

### AuthRepositoryImplTest (unit)
Usa `FakeSecureStorage` (não mock) — testa comportamento de persistência:
- Login bem-sucedido → token e email salvos no storage
- `getStoredSession()` recupera sessão salva
- `logout()` limpa o storage

### LoginViewModelTest (unit)
Usa `mockk` para repositório, `UnconfinedTestDispatcher`:
- Valida sequência de estados: `Idle → Loading → Success`
- Valida mapeamento de cada `AuthResult` para a mensagem de erro correta
- Valida que analytics são disparados (`LoginAttempt`, `LoginSuccess`, `LoginFailure`)

### LoginScreenTest (UI — Roborazzi + Robolectric)
Testa sub-composables diretamente (`LoginHeader`, `StudyBanner`, `LoginForm`) — sem ViewModel:
- Visibilidade de elementos em cada estado
- Botão desabilitado em `Loading`
- Mensagem de erro em `Error`
- 4 snapshots golden (light/dark, idle/error)

---

## Referências

- [ADR-024](../adrs/ADR-024-feature-auth-simulada.md) — decisões arquiteturais
- [:core:security](Core-Security-Module.md) — armazenamento seguro de tokens
- [Screenshot Testing com Roborazzi](Screenshot-Testing-Roborazzi.md) — como os goldens da LoginScreen funcionam
- [OWASP Mobile Top 10](https://owasp.org/www-project-mobile-top-10/) — M1 (Improper Credential Usage), M9 (Insecure Data Storage)
