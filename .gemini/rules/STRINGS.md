# String Resources Rules

- **Module-scoped strings**: Every feature/module must have its own `res/values/strings.xml`. Never declare strings from one module in another module's resources.
- **No hardcoded strings in UI**: All user-facing strings displayed in Composables must use `stringResource()`. Never pass raw string literals to `Text()`, `contentDescription`, `placeholder`, or any other composable that renders text to the user.
- **Naming convention**: Use the module name as a prefix. Example: for `:feature:chat`, all string keys start with `chat_`. For `:feature:home`, start with `home_`.
- **AI prompts and tool names are exempt**: Strings used as AI prompts (e.g., persona definitions), API function names, and internal constants are NOT UI strings and do not belong in `strings.xml`.
- **ViewModel error strings**: Error messages set in ViewModels must either use string resource IDs (and be resolved in the UI layer) or be moved to the Composable that renders them.