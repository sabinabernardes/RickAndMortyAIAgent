# 🧠 Rick and Morty AI - Android Refactor

Enterprise-grade Android project refactored to demonstrate **Clean Architecture**, **Modern Tooling**, and **Multi-module Architecture**. This repository serves as a technical showcase for scalable Android development using Jetpack Compose and state-of-the-art libraries.

---

## 🏗 Architectural Blueprint

The project is structured into independent, highly-decoupled modules to optimize build times (parallel compilation) and enforce strict dependency boundaries.

### 📦 Module Strategy

- **`:app`**: The thin entry point. Orchestrates Koin dependency graphs and handles the main `NavHost`.
- **`:feature:[name]`**: Self-contained business units.
    - `home`: Character discovery with search and infinite pagination (Paging 3).
    - `character_details`: Detailed character profiles and deep-linking support.
    - `chat`: Generative AI chat powered by Gemini 2.5 Flash (see below).
- **`:core:[name]`**: Internal SDKs and shared infrastructure.
    - `network`: Retrofit configuration, OkHttp interceptors, and global error handling.
    - `designsystem`: Single source of truth for UI. Atomic components and **Design Tokens** (Spacing, Typography, Color).
    - `navigation`: Centralized routing logic to prevent circular dependencies between features.
    - `playground`: Isolated environment for UI prototyping and component testing.

### 🧪 Design Patterns & Clean Architecture

Each feature module is strictly divided into layers:
1.  **Data**: API services (Retrofit), Repositories, and Data Sources. Uses **Mappers** to prevent DTO leakage.
2.  **Domain**: Pure Kotlin layer. Contains **UseCases** and Domain Models. 100% agnostic of Android frameworks.
3.  **Presentation**: **MVVM** pattern with Jetpack Compose. State management via `StateFlow` and `Unidirectional Data Flow (UDF)`.

---

## 🛠 Tech Stack & Tooling

| Category | Tools |
| :--- | :--- |
| **UI** | Jetpack Compose, Material 3, Coil (Async Image Loading) |
| **Concurrency** | Kotlin Coroutines & Flow |
| **DI** | Koin (Kotlin-first Dependency Injection) |
| **Networking** | Retrofit, OkHttp, Gson |
| **Pagination** | Paging 3 (RemoteMediator ready) |
| **Navigation** | Jetpack Navigation Compose |
| **Generative AI** | Google Gemini 2.5 Flash via `com.google.ai.client.generativeai` |
| **Testing** | MockK, JUnit 4, Turbine (Flow testing), Compose UI Test |
| **Build** | Gradle Kotlin DSL, Version Catalogs (.toml) |

---

## 🔬 Quality Assurance 

### Testing Strategy (Given-When-Then)
We enforce the **Given-When-Then** pattern to ensure tests are readable and serve as living documentation.
- **Unit Tests**: Coverage focus on `UseCases`, `ViewModels` (State emission), and `Mappers`.
- **UI Tests**: Isolated component testing via Compose Testing library.
- **Turbine**: Leveraged for robust `Flow` and `StateFlow` validation.

### Design System & Tokens
The UI is built upon a **Token-based Design System**. 
- No hardcoded margins or colors in features. 
- Global consistency enforced through `:core:designsystem:tokens`.

### Networking Layer
- **Unified Serialization**: Centrally managed Gson converter to avoid DTO mapping conflicts.
- **Error Handling**: Standardized error states propagated from Data to UI via Sealed Classes.

---

## 📜 Engineering Standards & Governance

To maintain high code quality and architectural consistency, this project follows a set of strict engineering rules. These are modularized and located in the `.gemini/rules/` directory for easy reference by developers and automated tools.

### 🏛 [Architecture](.gemini/rules/ARCHITECTURE.md)
*   **Layer Isolation**: Strict separation of Data, Domain, and Presentation.
*   **Unidirectional Data Flow (UDF)**: UI state is driven by the ViewModel, propagated via StateFlow.
*   **Mandatory Mappers**: Data Transfer Objects (DTOs) never reach the Domain or UI layers.

### 🧪 [Testing Standards](.gemini/rules/TESTING.md)
*   **Given-When-Then (GWT)**: All tests must follow this semantic structure for clarity.
*   **Flow Verification**: Extensive use of **Turbine** for state emission validation.
*   **Mocking Strategy**: Use of **MockK** for behavioral verification in unit tests.

### 🧵 [Coroutines & Threading](.gemini/rules/COROUTINES.md)
*   **Dispatcher Injection**: Avoid hardcoding `Dispatchers.IO` or `Main`.
*   **Lifecycle Awareness**: Use of `viewModelScope` and lifecycle-aware collection in Compose.
*   **Error Handling**: Standardized exception handling for asynchronous operations.

### 🎨 [Design System](.gemini/rules/DESIGN_SYSTEM.md)
*   **Token-First**: All UI dimensions and colors must come from the Design System tokens.
*   **Atomic Components**: Reusable, small-scale components implemented in `:core:designsystem`.

---

## 🤖 Generative AI — Rick Chat

The `:feature:chat` module adds a conversational AI experience to the app. Users can ask anything about the Rick and Morty universe and receive answers in Rick Sanchez's sarcastic, brilliant voice.

### How it works

- Powered by **Google Gemini 2.5 Flash** via the official Android SDK (`com.google.ai.client.generativeai`).
- Responses are **streamed token by token** using `generateContentStream`, so text appears progressively — no waiting for the full response.
- A **Rick persona prompt** is prepended to every message inside `ChatRepositoryImpl`, shaping Gemini's tone and knowledge scope.
- The API key is stored in `local.properties` (never committed) and injected at build time via `BuildConfig`.

### Architecture

Follows the same Clean Architecture pattern as all other features:

```
ChatScreen (Compose)
    └─ ChatViewModel (StateFlow / UDF)
        └─ SendMessageUseCase
            └─ ChatRepository → ChatDataSource → GenerativeModel (Gemini SDK)
```

### Setup

1. Get a free API key at [aistudio.google.com](https://aistudio.google.com).
2. Add to `local.properties`:
   ```properties
   GEMINI_API_KEY=your_key_here
   ```
3. Build and run — the chat is accessible via the FAB on the home screen.

> No login, no download, no setup required for the end user. Works on any Android device with internet access, including foldables (tested on Samsung Galaxy Z Flip 6).

---

## 📸 Screenshots

<table>
  <tr>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img.png" width="200" alt="screenshot 1"/></td>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_1.png" width="200" alt="screenshot 2"/></td>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_2.png" width="200" alt="screenshot 3"/></td>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_3.png" width="200" alt="screenshot 4"/></td>
  </tr>
  <tr>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_4.png" width="200" alt="screenshot 5"/></td>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_5.png" width="200" alt="screenshot 6"/></td>
    <td><img src="https://raw.githubusercontent.com/sabinabernardes/RickAndMorty/feat/chat-gemini-ai/img_6.png" width="200" alt="screenshot 7"/></td>
    <td></td>
  </tr>
</table>

## 🚀 Getting Started

### Requirements
- **JDK 17+**
- **Android Studio Meerkat+**
- **Min SDK 26** / **Target SDK 35**

### Build Commands
```bash
# Run unit tests across all modules
./gradlew test

# Generate Jacoco coverage report
./gradlew jacocoTestReport

# Build debug APK
./gradlew assembleDebug
```

---

## 📊 Roadmap

- [ ] **Offline-first**: Room integration for character caching.
- [ ] **Screenshot Testing**: Implementation of Paparazzi or Roborazzi.
- [ ] **Advanced CI**: Dynamic analysis and Play Store Internal App Sharing integration.

---

---

## 📚 Wiki

A documentação completa do projeto está na [Wiki do repositório](https://github.com/sabinabernardes/RickAndMorty/wiki):

| Página | Conteúdo |
|--------|----------|
| [Setup do Projeto](https://github.com/sabinabernardes/RickAndMorty/wiki/Setup-do-Projeto) | Como rodar, configurar a API Key e dependências |
| [Arquitetura](https://github.com/sabinabernardes/RickAndMorty/wiki/Arquitetura) | Diagrama de módulos e camadas Clean Architecture |
| [Feature: Chat Agêntico](https://github.com/sabinabernardes/RickAndMorty/wiki/Feature-Chat-Agêntico) | Como a IA navega o app com Gemini Function Calling |
| [Core: Design System](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Design-System) | Tokens, componentes e sistema de animações |
| [Core: Navigation](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Navigation) | Rotas centralizadas e fluxo de navegação |
| [Core: Network](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Network) | Cliente HTTP, retry com backoff exponencial |
| [Documentação de Engenharia](https://github.com/sabinabernardes/RickAndMorty/wiki/Documentação-de-Engenharia) | Rules, Specs (SDDs) e ADRs do projeto |

---

**Developed with ❤️ by [Sabina Bernardes](https://github.com/sabinabernardes)**.  
*Architected for scalability, performance, and maintainability.*
