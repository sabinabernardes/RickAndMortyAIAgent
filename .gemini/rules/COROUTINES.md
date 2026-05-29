# Coroutines Rules

- **Dispatchers**: Never hardcode dispatchers. Inject them or use default ones provided by the system.
- **ViewModel Scope**: Use `viewModelScope.launch` for starting coroutines in ViewModels.
- **Flows**: Prefer `StateFlow` for UI state representation.
- **Collection**: Use `collectAsStateWithLifecycle` or `collectAsState` in Compose.
- **Safety**: Always use `try-catch` blocks or `CoroutineExceptionHandler` for network calls within coroutines.
