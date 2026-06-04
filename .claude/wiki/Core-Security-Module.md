# :core:security — Armazenamento Seguro de Credenciais

> Módulo educacional para estudar boas práticas de armazenamento seguro em Android. Não há autenticação real — o objetivo é entender como proteger dados sensíveis em disco.

---

## Por que este módulo existe?

Apps reais que lidam com tokens, senhas ou dados pessoais precisam garantir que essas informações não fiquem em texto puro no armazenamento do dispositivo. O Android oferece `EncryptedSharedPreferences` (API do Jetpack Security) que usa o **Android Keystore** para guardar a chave de criptografia em hardware separado — inacessível ao restante do sistema, mesmo com root.

Este módulo isola essa responsabilidade em uma interface simples (`SecureStorage`) para que o restante do app nunca dependa diretamente de detalhes de criptografia.

---

## Estrutura

```
core/security/
├── build.gradle.kts
└── src/main/java/com/bina/security/
    ├── storage/
    │   ├── SecureStorage.kt           # contrato público (interface)
    │   └── EncryptedPrefsStorage.kt   # impl com EncryptedSharedPreferences
    └── di/
        └── SecurityModule.kt          # Koin: single<SecureStorage>
```

---

## Interface SecureStorage

```kotlin
interface SecureStorage {
    fun save(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
    fun clear()
}
```

Qualquer código que precise salvar dados sensíveis recebe `SecureStorage` por injeção — sem conhecer `EncryptedSharedPreferences`, sem importar nada de `androidx.security`. Isso permite trocar a implementação (ex: DataStore criptografado, Vault) sem tocar nos callers.

---

## Implementação: EncryptedPrefsStorage

```kotlin
class EncryptedPrefsStorage(context: Context) : SecureStorage {
    private val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    // ...
}
```

**O que acontece por baixo:**

| Camada | Mecanismo |
|--------|-----------|
| Chave mestra | Gerada no Android Keystore (hardware-backed quando disponível) |
| Chaves dos valores | Criptografadas com AES-256-SIV (determinístico — permite busca por chave) |
| Valores | Criptografados com AES-256-GCM (autenticado — detecta adulteração) |
| Arquivo no disco | `.xml` ilegível em texto puro |

> **Por que AES-256-GCM para valores?** GCM (Galois/Counter Mode) é autenticado: qualquer adulteração do arquivo em disco é detectada na leitura, não apenas na descriptografia.

---

## Android Keystore: por que é relevante

O Keystore armazena material criptográfico de forma que a chave **nunca sai do hardware seguro** (TEE ou SE). Mesmo que um atacante extraia o arquivo de preferências, não consegue decriptografar sem a chave — que não é exportável.

Isso é diferente de guardar uma chave gerada por `Random` em outro arquivo SharedPreferences (abordagem insegura que alguns tutoriais antigas ainda mostram).

---

## Testabilidade

`EncryptedPrefsStorage` depende de APIs Android que não funcionam em JVM (unit tests). A solução é um **fake em memória** usado nos testes dos módulos que dependem de `SecureStorage`:

```kotlin
class FakeSecureStorage : SecureStorage {
    private val store = mutableMapOf<String, String>()
    override fun save(key: String, value: String) { store[key] = value }
    override fun get(key: String) = store[key]
    override fun remove(key: String) { store.remove(key) }
    override fun clear() { store.clear() }
}
```

`EncryptedPrefsStorage` é excluída do JaCoCo — não por falta de qualidade, mas porque o Android Keystore genuinamente não é instanciável em JVM. Testes de instrumentação (device/emulator) cobririam isso em um projeto com CI completo.

---

## Injeção de dependência (Koin)

```kotlin
val securityModule = module {
    single<SecureStorage> { EncryptedPrefsStorage(androidContext()) }
}
```

`single` (não `factory`) porque `EncryptedSharedPreferences` abre o arquivo uma vez e mantém o handle — recriar a cada injeção seria desnecessário e mais lento.

---

## Referências

- [ADR-024](../adrs/ADR-024-feature-auth-simulada.md) — decisões arquiteturais da autenticação simulada
- [Android Keystore System](https://developer.android.com/privacy-and-security/keystore)
- [Jetpack Security Crypto](https://developer.android.com/jetpack/androidx/releases/security)
