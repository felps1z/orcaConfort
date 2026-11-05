# OrçaConfort

Aplicativo Android para auxiliar profissionais de estofamento (tapeceiros) na elaboração de **orçamentos para reforma de móveis estofados**: estimar quantidades de materiais, calcular custos, registrar orçamentos, organizar entregas e gerar PDFs para os clientes.

> Projeto da disciplina de Gerência de Projetos de Software — IFPE Campus Belo Jardim.
> Integrantes: Thiago Leal e Felipe dos Santos.

## Tecnologias

| Área | Escolha |
| --- | --- |
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM + Clean Architecture (`ui` / `domain` / `data`) |
| Injeção de dependência | Hilt |
| Persistência | Room (armazenamento local) |
| Assincronia | Coroutines + Flow |
| Navegação | Navigation Compose |
| Build | Gradle (Kotlin DSL) + Version Catalog (`gradle/libs.versions.toml`) |

## Estrutura do repositório

```
orcaConfort/
├── app/                      # Módulo Android (código, resources, manifest)
│   └── src/main/java/com/orcaconfort/app/
│       ├── ui/               # Telas, ViewModels, tema e componentes Compose
│       ├── domain/           # Modelos, repositórios (interfaces) e use cases
│       ├── data/             # Implementações de repositório, Room e mappers
│       ├── di/               # Módulos do Hilt
│       └── core/             # Utilitários transversais
├── docs/                     # Documentação do projeto
│   └── ARCHITECTURE.md       # Decisão arquitetural e onde colocar arquivos
├── gradle/                   # Version catalog e wrapper do Gradle
├── .github/                  # Template de Pull Request
├── CONTRIBUTING.md           # Fluxo de branches, commits e revisão
└── README.md
```

## Como executar

1. Instale o [Android Studio](https://developer.android.com/studio) (versão Rabbit 2 | 2026.2.2 ou superior) com JDK 17.
2. Clone o repositório: `git clone https://github.com/felps1z/orcaConfort.git`
3. Abra a pasta no Android Studio e aguarde a sincronização do Gradle.
4. Execute em um emulador ou dispositivo Android com `./gradlew assembleDebug` ou pelo botão *Run* do IDE.

## Documentação

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — padrão arquitetural, estrutura de packages e onde colocar cada tipo de arquivo.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — convenções de branches, commits e fluxo de Pull Request.
- [Product Backlog (GitHub Projects)](https://github.com/users/felps1z/projects/8) — quadro Kanban do projeto.
- [Issues](https://github.com/felps1z/orcaConfort/issues) — tarefas e histórias de usuário.

## Fluxo de trabalho

O time trabalha com Kanban e ciclos semanais (planning, dailys, review e retrospective), conforme descrito no documento inicial do projeto. As alterações são feitas em branches `feature/*` ou `bugfix/*` a partir de `develop` e integradas por Pull Request.
