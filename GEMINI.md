# 📚 Rick and Morty AI - Project Instructions

This file serves as the root index for all project-wide rules and engineering standards. All developers (and AI agents) must adhere to the modularized rules linked below.

## 🛠 Core Rules

- [Architecture Guidelines](.gemini/rules/ARCHITECTURE.md)
- [Testing Standards (Given-When-Then)](.gemini/rules/TESTING.md)
- [Coroutines & Flow Best Practices](.gemini/rules/COROUTINES.md)
- [Design System & UI Tokens](.gemini/rules/DESIGN_SYSTEM.md)

---

## 🚀 General Workflow

1.  **Modular Development**: Always develop features in isolated modules.
2.  **DI with Koin**: Ensure all new services are registered in the corresponding Koin module.
3.  **UI Consistency**: Consult the `:core:designsystem` before creating any new UI elements.
4.  **Verification**: A task is only considered complete after unit tests pass and the build is successful.
