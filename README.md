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
| **Testing** | MockK, JUnit 4, Turbine (Flow testing), Compose UI Test |
| **Build** | Gradle Kotlin DSL, Version Catalogs (.toml) |

---

## 🔬 Quality Assurance (Senior Focus)

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

**Developed with ❤️ by [Sabina Bernardes](https://github.com/sabinabernardes)**.  
*Architected for scalability, performance, and maintainability.*
