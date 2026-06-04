# 🧠 Rick and Morty AI - Study Project

[![Android CI](https://github.com/sabinabernardes/RickAndMorty/actions/workflows/android-ci.yml/badge.svg)](https://github.com/sabinabernardes/RickAndMorty/actions/workflows/android-ci.yml)

Enterprise-grade Android project demonstrating **Clean Architecture**, **Multi-module**, and **Generative AI** integration. Built as a technical showcase for scalable Android development with Jetpack Compose.

---

## 📸 Screenshots

<table>
  <tr>
    <th align="center">Home — Dark</th>
    <th align="center">Home — Light</th>
    <th align="center">Detalhes — Dark</th>
    <th align="center">Detalhes — Light</th>
    <th align="center">Chat — Dark</th>
    <th align="center">Chat — Light</th>
  </tr>
  <tr>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/home_grid.png" width="160"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/home_light.png" width="160"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/detail_dark.png" width="160"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/detail_light.png" width="160"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/chat_empty.png" width="160"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/chat_light.png" width="160"/></td>
  </tr>
</table>

### 🤖 Chat Agêntico — em ação

<table>
  <tr>
    <th align="center">Abrir personagem pelo chat</th>
    <th align="center">Buscar personagens pelo chat</th>
  </tr>
  <tr>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/chat_show_character.gif" width="240"/></td>
    <td><img src="https://raw.githubusercontent.com/wiki/sabinabernardes/RickAndMorty/assets/chat_search_characters.gif" width="240"/></td>
  </tr>
</table>

---

## 🛠 Tech Stack

| Category | Tools |
| :--- | :--- |
| **UI** | Jetpack Compose, Material 3, Coil |
| **Architecture** | MVVM, Clean Architecture, Multi-module |
| **Concurrency** | Kotlin Coroutines & Flow |
| **DI** | Koin |
| **Networking** | Retrofit, OkHttp |
| **Pagination** | Paging 3 |
| **Generative AI** | Google Gemini 2.5 Flash |
| **Testing** | MockK, JUnit 4, Turbine, Roborazzi |
| **Build** | Gradle Kotlin DSL, Version Catalogs |

---

## 🚀 Quick Start

```bash
# 1. Add your Gemini API key to local.properties
GEMINI_API_KEY=your_key_here

# 2. Install git hooks (one-time)
./gradlew installGitHooks

# 3. Build and run
./gradlew assembleDebug
```

> Requires JDK 17+, Android Studio Meerkat+, Min SDK 26.

---

## 📊 Roadmap

- [x] **Screenshot Testing**: Roborazzi com 12 goldens, verificados em CI.
- [ ] **Baseline Profile**: Startup time optimization com Jetpack Baseline Profiles.
- [ ] **Offline-first**: Room integration para cache de personagens.

---

## 📚 Wiki

Documentação completa na [Wiki do repositório](https://github.com/sabinabernardes/RickAndMorty/wiki):

| Página | Conteúdo |
|--------|----------|
| [Setup do Projeto](https://github.com/sabinabernardes/RickAndMorty/wiki/Setup-do-Projeto) | Como rodar, configurar a API Key e dependências |
| [Arquitetura](https://github.com/sabinabernardes/RickAndMorty/wiki/Arquitetura) | Diagrama de módulos e camadas Clean Architecture |
| [Feature: Chat Agêntico](https://github.com/sabinabernardes/RickAndMorty/wiki/Feature-Chat-Agêntico) | Como a IA navega o app com Gemini Function Calling |
| [Core: Design System](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Design-System) | Tokens, componentes e sistema de animações |
| [Core: Navigation](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Navigation) | Rotas centralizadas e fluxo de navegação |
| [Core: Network](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Network) | Cliente HTTP, retry com backoff exponencial |
| [Core: Observabilidade](https://github.com/sabinabernardes/RickAndMorty/wiki/Core-Observabilidade) | Logging estruturado, analytics e performance monitoring |
| [Documentação de Engenharia](https://github.com/sabinabernardes/RickAndMorty/wiki/Documentação-de-Engenharia) | Rules, Specs (SDDs) e ADRs do projeto |
| [Testes de UI](https://github.com/sabinabernardes/RickAndMorty/wiki/Testes-de-UI) | Robolectric, createComposeRule e cobertura por módulo |
| [Screenshot Testing com Roborazzi](.claude/wiki/Screenshot-Testing-Roborazzi.md) | Goldens, record vs verify, dark mode, troubleshooting |
| [Acessibilidade WCAG 2.1 AA](https://github.com/sabinabernardes/RickAndMorty/wiki/Acessibilidade-WCAG-2.1-AA) | TalkBack, contraste AA, semântica e live regions |

---

**Developed with ❤️ by [Sabina Bernardes](https://github.com/sabinabernardes).**  
*Architected for scalability, performance, and maintainability.*
