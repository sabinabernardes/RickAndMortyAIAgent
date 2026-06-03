# ADR-012 — Interface-first sem dependências externas

**Status:** Aceito  
**Data:** 2026-06-02  
**Módulos:** `:core:logging`, `:core:analytics`

---

## Contexto

É a primeira implementação de observabilidade no projeto. qual biblioteca ou padrão usar para logging e analytics?

Opções no mercado:
- **Timber** — biblioteca de logging para Android, wrapper sobre `android.util.Log`
- **Firebase Analytics + Crashlytics** — plataforma Google, requer `google-services.json` e conta Firebase
- **DataDog** — plataforma paga de observabilidade enterprise
- **Interfaces puras Kotlin + implementações nativas** — zero dependências externas, substituível

## Opções Avaliadas

### Opção A: Timber
```kotlin
// build.gradle.kts
implementation("com.jakewharton.timber:timber:5.0.1")

// Uso
Timber.d("Character loaded: %s", characterId)
```

- **Prós:** API simples, suporte a tags automáticas, fácil de configurar `DebugTree`/`ReleaseTree`
- **Contras:** Dependência externa adicional; Timber é global (singleton estático), dificulta testes; a API é baseada em strings formatadas, sem estrutura de metadados; trocar de Timber para DataDog requer mudar todos os call sites

### Opção B: Firebase Analytics + Crashlytics
```kotlin
Firebase.analytics.logEvent("character_clicked") { param("id", characterId) }
```

- **Prós:** Ecossistema Google (alinhado com Gemini já em uso), dashboard gratuito
- **Contras:** Requer `google-services.json` e conta Firebase; adiciona `com.google.firebase:firebase-analytics` (~200KB); acoplamento forte — impossível testar sem SDK real ou mock manual; overkill para exploração inicial

### Opção C: Interfaces puras Kotlin + implementações nativas
```kotlin
interface AppLogger {
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

class LogcatLogger : AppLogger {
    override fun debug(tag: String, message: String) = Log.d(tag, message)
    override fun error(tag: String, message: String, throwable: Throwable?) =
        Log.e(tag, message, throwable)
}
```

- **Prós:** Zero deps externas; testável com MockK sem setup especial; substituível — trocar para Firebase = criar `FirebaseLogger : AppLogger`, mudar binding no Koin; sem acoplamento em call sites; design explícito do contrato
- **Contras:** Mais código inicial a escrever; sem dashboard de produção até integrar backend real

## Decisão

**Escolhida: Opção C — interfaces puras Kotlin + implementações nativas Android**

## Justificativa

O objetivo desta fase é explorar e aprender o design de observabilidade, não entregar dados a um dashboard de produção. Interfaces puras permitem:
1. Escrever e testar o código de observabilidade sem nenhuma plataforma externa
2. Trocar o backend no futuro mudando **apenas** a implementação no módulo, sem tocar em nenhuma feature
3. Usar MockK normalmente nos testes — `mockk<AppLogger>()` funciona igual a qualquer outra interface

As implementações concretas usam exclusivamente:
- `android.util.Log` para logging → nativo do Android SDK
- `android.os.SystemClock` para performance → nativo do Android SDK

## Consequências

- `AppLogger`, `AnalyticsTracker` e `PerformanceTracker` são interfaces no módulo respective
- `LogcatLogger`, `LogcatAnalyticsTracker` e `LogcatPerformanceTracker` são as implementações concretas desta fase
- Koin registra as implementações concretas como singletons — features injetam pelas interfaces
- Para adicionar Firebase no futuro: criar `FirebaseAnalyticsTracker : AnalyticsTracker` e trocar o `single<AnalyticsTracker>` no Koin module — zero mudança nas features
