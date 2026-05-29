# Design System Rules

- **Tokens**: Strictly use `SpacingTokens`, `TypographyTokens`, and `ColorTokens`. Never use hardcoded DP or Color values in feature modules.
- **Components**: Reuse components from `:core:designsystem`. If a new component is needed, implement it in the design system module first.
- **Theme**: Ensure all components support both Light and Dark modes using `MaterialTheme.colorScheme`.
- **Atomic UI**: Keep components small and focused (atoms/molecules).
- **Toolbars**: Use the standard `Toolbar` or `SearchToolbar` for screen headers.
