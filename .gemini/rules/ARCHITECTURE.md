# Architecture Rules

- **Pattern**: Strict Clean Architecture with Data, Domain, and Presentation layers.
- **Modularization**: Feature-based modularization (`:feature:home`, `:feature:character_details`, `:feature:chat`, `:feature:auth`).
- **Core Modules**: Shared logic stays in `:core` modules (`:core:network`, `:core:designsystem`, `:core:navigation`, `:core:logging`, `:core:analytics`, `:core:security`).
- **Dependency Flow**: Dependencies must only flow inwards (Presentation -> Domain <- Data).
- **Mappers**: Always use mappers to convert between layers (Data -> Domain, Domain -> UI) to prevent leaking implementation details.
- **Dependency Injection**: Use Koin for all dependencies. Define modules in a `di` package within each feature or core module.
