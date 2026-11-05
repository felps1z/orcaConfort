# Contribuindo com o OrçaConfort

Este documento define como a equipe trabalha no repositório: nomes de branches, mensagens de commit, fluxo de Pull Request e onde colocar cada tipo de arquivo. Ele complementa o [documento inicial do projeto](https://github.com/felps1z/orcaConfort) e o [quadro Kanban](https://github.com/users/felps1z/projects/8).

## Estrutura de branches

| Branch | Papel | Proteção |
| --- | --- | --- |
| `main` | Estado estável do projeto. Não recebe commits diretos; só por Pull Request. | Protegida |
| `develop` | Branch de integração. Todo o trabalho parte daqui e para aqui. | — |
| `feature/*` | Novas funcionalidades ou melhorias. | — |
| `bugfix/*` | Correção de defeitos. | — |
| `docs/*` | Alterações apenas de documentação. | — |
| `chore/*` | Tarefas de manutenção (build, dependências, CI, configuração). | — |
| `release/*` | Preparação de uma entrega/MVP. | — |

Regras:

- `main` é protegida: não faça `git push` direto em `main`.
- Toda branch de trabalho é criada a partir de `develop` e devolvida a `develop` por Pull Request.
- Após o merge, a branch de trabalho é excluída.

## Convenção de nomes de branches

Formato: `<tipo>/issue-<número>-<descrição-curta>`

- tudo em **minúsculas**;
- palavras separadas por **hífens**;
- o número da issue é sempre incluído, para rastreabilidade.

Exemplos:

```
feature/issue-12-arquitetura-inicial
feature/issue-13-historico-de-orcamentos
bugfix/issue-21-selecao-de-modelo
docs/issue-9-prototipo-figma
chore/issue-4-estrutura-de-branches
```

## Mensagens de commit

Utilizamos [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>: <descrição curta em minúsculas>

tipos: feat | fix | docs | refactor | test | chore | style | build
```

Exemplos:

```
feat: adicionar seleção de modelo do sofá
fix: corrigir cálculo de tecido para sofá retrátil
docs: registrar decisão arquitetural em ARCHITECTURE.md
chore: configurar version catalog do Gradle
```

Quando o commit estiver relacionado a uma issue, referencie o número no corpo do commit ou no PR (`Refs #12`).

## Fluxo de Pull Request

1. Atualize `develop`: `git checkout develop && git pull`
2. Crie a branch de trabalho: `git checkout -b feature/issue-<n>-<descrição>`
3. Faça as alterações em commits convencionais.
4. Abra o PR para `develop` usando o [template](.github/PULL_REQUEST_TEMPLATE.md) e vincule a issue (ex.: `Closes #12`).
5. Peça a revisão do outro integrante; verifique os critérios de aceitação da issue.
6. Após a aprovação, faça o merge e exclua a branch.
7. Alterações consideradas prontas para entrega são integradas de `develop` para `main` por PR.

## Onde colocar os arquivos

A estrutura de packages (MVVM + Clean Architecture) está documentada em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), incluindo a tabela **"onde colocar cada tipo de arquivo"**. Antes de criar um arquivo novo, confira essa tabela.

## Dependências

- Toda dependência nova é adicionada **apenas** em [`gradle/libs.versions.toml`](gradle/libs.versions.toml) — nunca com versão fixa nos `build.gradle.kts`.
- As versões usadas no projeto são as registradas no version catalog; atualizações são feitas por PR (`chore`).
