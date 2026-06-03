# DoR — Definition of Ready
## Design System Polish & Animações de Entrada

**Spec:** `design-system-polish-sdd.md`  
**Data:** 2026-05-30

---

Uma task desta spec está **pronta para ser implementada** quando todos os itens abaixo estiverem marcados.

---

## Critérios Gerais

- [ ] A SDD (`design-system-polish-sdd.md`) foi lida e está compreendida
- [ ] O branch `feat/design-system-polish` foi criado a partir de `master`
- [ ] O projeto builda limpo no estado atual (`./gradlew assembleDebug` sem erros)
- [ ] Os previews do Android Studio estão funcionando para `StatusBadge` e `CardCharacter`

---

## Critérios por Fase

### Fase 1 — Consolidação de Cores
- [ ] Está claro que `ColorScheme.kt` é a fonte de verdade de cores raw
- [ ] Está claro que `ColorTokens.kt` deve ser um espelho semântico, não definir valores próprios
- [ ] Os três arquivos de tema (`ColorTokens`, `ColorScheme`, `app/ui/theme/Color.kt`) foram lidos e comparados

### Fase 2 — Tipografia + Nunito Sans
- [ ] A dependência `ui-text-google-fonts` está disponível na BOM `2025.01.00` (confirmado)
- [ ] O arquivo `res/values/font_certs.xml` (ou equivalente para GMS fonts) existe ou será criado junto
- [ ] Está definido que os 13 estilos M3 serão todos preenchidos com Nunito Sans

### Fase 3–5 — Refactor de Componentes
- [ ] Cada arquivo a ser refatorado foi lido integralmente antes de começar
- [ ] Está claro que **nenhuma lógica de negócio** será alterada — só tokens e imports

### Fase 6 — Animações
- [ ] O arquivo `FadeSlideIn.kt` será criado em `core/designsystem/.../animation/`
- [ ] `CardCharacter` receberá `index: Int = 0` como parâmetro com default 0 (sem quebrar callers existentes)
- [ ] `HomeScreen` passará o índice da `LazyVerticalGrid` para cada `CardCharacter`

### Fase 7 — Limpeza
- [ ] Todos os `import` de `app/ui/theme/Color.kt` e `Type.kt` foram identificados antes de deletar os arquivos

---

## O que NÃO está no escopo (não implementar)
- Testes unitários de UI (sem mudança de lógica)
- Novo componente de loading customizado
- Dark mode toggle manual
- Mudança de arquitetura ou camada de dados