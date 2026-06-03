# ADR-021 — Coverage gate obrigatório via JaCoCo

**Status:** Aceito  
**Data:** 2026-06-03  
**Escopo:** CI/CD — cobertura de testes

---

## Contexto

O projeto já gera relatórios JaCoCo (`jacocoTestReport` por módulo e `jacocoFullReport` agregado), mas a cobertura não é verificada no CI — PRs com cobertura baixa ou regressão de cobertura são mergeados sem aviso. O relatório existe mas não tem dentes.

Em times profissionais, coverage gate é o mínimo esperado: não necessariamente um threshold alto, mas que qualquer regressão significativa bloqueie o merge.

## Opções Avaliadas

### Opção A: Verificação manual via script shell

Ler o XML do JaCoCo e calcular cobertura com `awk` ou `python`:
```bash
coverage=$(python3 -c "import xml.etree.ElementTree as ET; ...")
if [ "$coverage" -lt 60 ]; then exit 1; fi
```

- **Prós:** Zero dependência externa
- **Contras:** Script frágil; não comenta no PR; difícil de manter; não mostra breakdown por módulo

### Opção B: `madrapps/jacoco-report` GitHub Action

Action do marketplace que lê o XML do JaCoCo, comenta no PR com breakdown de cobertura por pacote e falha o job se threshold não for atingido.

```yaml
- uses: madrapps/jacoco-report@v1.6.0
  with:
    paths: build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml
    token: ${{ secrets.GITHUB_TOKEN }}
    min-coverage-overall: 60
    min-coverage-changed-files: 60
```

- **Prós:** Comentário visual no PR; breakdown por módulo; threshold por arquivo modificado (evita que PRs pequenos passem por código não testado); fácil de manter
- **Contras:** Dependência externa de marketplace action (fixar versão `@v1.6.0` mitiga risco de breaking change)

### Opção C: Nenhum gate (manter estado atual)

- **Prós:** Zero esforço
- **Contras:** Coverage existe mas não é enforced — sinal negativo; regressões silenciosas

## Decisão

**Escolhida: Opção B — `madrapps/jacoco-report@v1.6.0`**

## Justificativa

O projeto já tem toda a infraestrutura de cobertura montada (`jacocoFullReport` com XML). A action adiciona a camada de enforcement e visibilidade com mínimo de configuração. Versão fixada em `@v1.6.0` para evitar breaking changes silenciosos.

**Threshold escolhido: 60%**

| Critério | Valor | Motivo |
|---|---|---|
| `min-coverage-overall` | 60% | Realista para o estado atual; excluindo DI, gerado e Composables sem lógica |
| `min-coverage-changed-files` | 60% | Garante que código novo também seja coberto |

60% é o piso, não o teto. O objetivo é subir gradualmente à medida que testes de integração forem adicionados.

**Exclusões de cobertura (já configuradas nos módulos):**
- `**/R.class`, `**/BuildConfig.*`, `**/Manifest*.*` — gerado
- `**/di/**` — módulos Koin (estrutura, não lógica)
- `**/*Test*.*` — os próprios testes

## Consequências

- Job `coverage-gate` é adicionado ao workflow com `needs: [test]`
- O job lê `build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml`
- PRs recebem comentário automático com percentual de cobertura global e por arquivo modificado
- PRs com cobertura < 60% têm o merge bloqueado
- Para subir o threshold no futuro: editar `min-coverage-overall` no workflow