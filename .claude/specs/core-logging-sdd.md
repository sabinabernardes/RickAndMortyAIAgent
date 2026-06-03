# SDD — Módulo :core:logging

**Módulo:** `:core:logging`  
**Status:** Planejado  
**Autor:** Sabina Bernardes  
**Data:** 2026-06-02

---

## 1. Contexto e Objetivo

O projeto não tem logging estruturado. Os únicos logs existentes são o `HttpLoggingInterceptor` (logs de HTTP brutos) no `NetworkClient` e `println`/`Log.d` avulsos que podem existir durante desenvolvimento. Isso dificulta diagnosticar bugs em produção e entender o fluxo de dados entre camadas.

O objetivo do `:core:logging` é fornecer uma interface de logging tipada por nível (DEBUG, INFO, WARN, ERROR) com tag explícita, injetável via Koin, testável com MockK, e substituível por backend remoto no futuro sem mudar nenhuma feature.

---

## 2. Decisões Técnicas

- [ADR-011](../adrs/ADR-011-dois-modulos-logging-analytics.md) — Por que dois módulos separados (logging e analytics)
- [ADR-012](../adrs/ADR-012-interface-first-sem-deps-externas.md) — Interface-first sem dependências externas

---

## 3. Arquitetura

```
:core:logging
├── AppLogger (interface)          ← contrato público
├── LogLevel (enum)                ← DEBUG, INFO, WARN, ERROR
└── impl/
    └── LogcatLogger (class)       ← implementação com android.util.Log
└── di/
    └── LoggingModule.kt           ← Koin: single<AppLogger> { LogcatLogger() }
```

Features injetam `AppLogger` como dependência de construtor. Não há acesso estático/singleton — toda instância vem do Koin.

---

## 4. Contratos das Interfaces

### AppLogger

```kotlin
interface AppLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
```

### LogcatLogger (implementação concreta)

```kotlin
class LogcatLogger : AppLogger {
    override fun debug(tag: String, message: String) = Log.d(tag, message)
    override fun info(tag: String, message: String) = Log.i(tag, message)
    override fun warn(tag: String, message: String, throwable: Throwable?) =
        Log.w(tag, message, throwable)
    override fun error(tag: String, message: String, throwable: Throwable?) =
        Log.e(tag, message, throwable)
}
```

---

## 5. Koin Module

```kotlin
// LoggingModule.kt
val loggingModule = module {
    single<AppLogger> { LogcatLogger() }
}
```

Registrado em `appModules` no `:app`.

---

## 6. Como Features Consomem

O `AppLogger` é injetado via construtor nos pontos onde logging é relevante:

```kotlin
// Exemplo em DataSource
class CharacterDataSourceImpl(
    private val apiService: RickAndMortyApiService,
    private val logger: AppLogger
) : CharacterDataSource {

    override suspend fun getCharacters(page: Int): CharacterResponse {
        logger.debug(TAG, "Fetching page $page")
        return apiService.getCharacters(page)
    }

    companion object {
        private const val TAG = "CharacterDataSource"
    }
}

// Exemplo em ViewModel (erros)
class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val logger: AppLogger
) : ViewModel() {

    private fun loadCharacters() {
        viewModelScope.launch {
            try {
                // ...
            } catch (e: Exception) {
                logger.error(TAG, "Failed to load characters", e)
                _uiState.value = CharactersUiState.Error(e.message)
            }
        }
    }
}
```

---

## 7. Estrutura de Arquivos do Módulo

```
core/logging/
├── build.gradle.kts
└── src/
    ├── main/
    │   └── java/com/bina/logging/
    │       ├── AppLogger.kt
    │       ├── impl/
    │       │   └── LogcatLogger.kt
    │       └── di/
    │           └── LoggingModule.kt
    └── test/
        └── java/com/bina/logging/
            └── LogcatLoggerTest.kt
```

**build.gradle.kts mínimo:**
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.bina.logging"
    compileSdk = 35
    defaultConfig { minSdk = 28 }
}

dependencies {
    implementation(libs.koin.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

---

## 8. Critérios de Aceite

- [ ] `AppLogger` é uma interface com os 4 métodos de nível
- [ ] `LogcatLogger` implementa `AppLogger` usando `android.util.Log` (sem dependências externas)
- [ ] `loggingModule` registra `LogcatLogger` como `single<AppLogger>`
- [ ] Features podem injetar `AppLogger` via Koin normalmente
- [ ] Testes de features mockam `AppLogger` com `mockk<AppLogger>()` sem setup especial
- [ ] `LogcatLoggerTest` verifica que o logger não lança exceção para nenhum nível (smoke test)
- [ ] Substituição por backend remoto requer apenas criar nova classe + trocar o `single<>` no Koin