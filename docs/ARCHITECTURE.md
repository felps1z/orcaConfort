# Arquitetura do OrçaConfort

| Campo | Valor |
| --- | --- |
| Status | **Aceita** |
| Data da decisão | 05/11/2026 |
| Issues | [#12 — Define Project Architecture and Structure](https://github.com/felps1z/orcaConfort/issues/12) |
| Revisão prevista | Ao final do MVP, ou se surgir necessidade de múltiplos módulos Gradle |

## 1. Contexto

O aplicativo precisa de uma estrutura que mantenha as **regras de negócio** (estimativas de materiais e cálculo de custos — a parte mais crítica do projeto) separadas da **interface** e do **armazenamento**, para que possam ser testadas, revisadas e validadas com o cliente sem depender da tela ou do banco de dados.

Alternativas avaliadas:

| Alternativa | Prós | Contras | Decisão |
| --- | --- | --- | --- |
| MVVM puro (tudo no pacote `ui`) | Simples, rápido para MVP | Regras de negócio ficam presas à UI, difíceis de testar | Descartada |
| MVI (unidirecional, reducers) | Fluxo previsível | Curva de aprendizado e burocracia desnecessárias para o escopo | Descartada |
| **MVVM + Clean Architecture** | Testável, organizada, aceita pelo time e alinhada ao `domain` esperado pelo backlog | Um pouco mais de estrutura inicial | **Escolhida** |

## 2. Decisão

O app seguirá **MVVM + Clean Architecture** em um único módulo Gradle (`:app`), com cinco pacotes de topo sob `com.orcaconfort.app`:

```
com.orcaconfort.app
├── ui        # Camada de apresentação (Jetpack Compose + ViewModels)
├── domain    # Camada de negócio (modelos, repositórios como interface, use cases)
├── data      # Camada de dados (implementações de repositório, Room, mappers)
├── di        # Módulos de injeção de dependência (Hilt)
└── core      # Utilitários transversais (formatadores, extensões, constantes)
```

### Regras de dependência

```
        ┌──────────┐
        │    ui    │  (Compose, ViewModels)
        └────┬─────┘
             │ depende de
             ▼
        ┌──────────┐         ┌──────────┐
        │  domain  │ ◄────── │   data   │  (Room, repositórios concretos)
        └──────────┘  implementa └──────────┘
             ▲
             │  injeta tudo
        ┌────┴─────┐
        │    di    │  (Hilt)      core é usado por qualquer camada
        └──────────┘
```

1. `domain` **não** depende de `ui`, `data` ou de bibliotecas Android (apenas Kotlin puro e Coroutines/Flow).
2. `data` depende de `domain` para implementar suas interfaces (`Repository`).
3. `ui` depende de `domain` (nunca de `data`): os ViewModels consomem use cases/interfaces.
4. `di` conhece todas as camadas e é o único lugar que liga interface → implementação.
5. `core` é dependência de qualquer camada, mas **nunca** importa de `ui`/`domain`/`data`.

Isso garante que as regras de cálculo (RF05, RF06, RF09, RF10, RF12) fiquem em `domain`, testáveis com testes unitários puros.

## 3. Estrutura de packages

Raiz: `app/src/main/java/com/orcaconfort/app/`

```
com.orcaconfort.app
├── ui/
│   ├── navigation/        # NavHost, rotas (enum/sealed class)
│   ├── theme/             # Cores, tipografia, tema do Compose
│   ├── components/        # Componentes reutilizáveis (botões, campos, cartões)
│   └── screens/           # Telas agrupadas por funcionalidade
│       ├── orcamento/     #   ex.: OrcamentoViewModel, OrcamentoScreen
│       ├── precos/
│       └── historico/
├── domain/
│   ├── model/             # Entidades de negócio (Orcamento, Material, ModeloSofa…)
│   ├── repository/        # Interfaces (OrcamentoRepository, MaterialRepository…)
│   └── usecase/           # Casos de uso (CalcularOrcamento, SalvarOrcamento…)
├── data/
│   ├── local/             # Room: entities, DAOs, AppDatabase
│   ├── repository/        # Implementações das interfaces do domain
│   └── mapper/            # Conversões entity ↔ model de negócio
├── di/                    # Módulos Hilt (@Module, @InstallIn)
└── core/
    └── util/              # Formatação de moeda (R$), datas, extensões
```

## 4. Onde colocar cada tipo de arquivo

| Tipo de arquivo | Pacote | Exemplo |
| --- | --- | --- |
| Tela Compose (`@Composable`) | `ui/screens/<funcionalidade>` | `TelaOrcamento.kt` |
| ViewModel | `ui/screens/<funcionalidade>` (junto à tela) | `OrcamentoViewModel.kt` |
| Componente reutilizável | `ui/components` | `CampoNumerico.kt` |
| Rotas / navegação | `ui/navigation` | `Rotas.kt` |
| Tema (cores, tipografia) | `ui/theme` | `Tema.kt` |
| Modelo de negócio / entidade de domínio | `domain/model` | `Orcamento.kt` |
| Interface de repositório | `domain/repository` | `OrcamentoRepository.kt` |
| Use case | `domain/usecase` | `CalcularOrcamentoUseCase.kt` |
| Entity / DAO do Room | `data/local` | `OrcamentoDao.kt` |
| Implementação de repositório | `data/repository` | `OrcamentoRepositoryImpl.kt` |
| Mapeador de dados | `data/mapper` | `OrcamentoMappers.kt` |
| Módulo Hilt | `di` | `DatabaseModule.kt` |
| Formatação de R$, datas, extensões | `core/util` | `Formatadores.kt` |
| Teste unitário | `app/src/test/java/com/orcaconfort/app/...` (espelho da estrutura acima) | `CalcularOrcamentoTest.kt` |
| Teste de instrumentação | `app/src/androidTest/java/com/orcaconfort/app/...` | — |

Regras práticas:

- Uma **funcionalidade nova** cria uma subpasta em `ui/screens/` e os use cases correspondentes em `domain/usecase/` — não misture telas de funcionalidades diferentes na mesma pasta.
- Se um arquivo não se encaixar em nenhuma linha da tabela, discuta a inclusão no planning antes de criar uma nova pasta de topo.
- Novas pastas de topo (`ui`, `domain`, `data`, `di`, `core`) **não** podem ser criadas sem revisão da equipe.

## 5. Stack técnica e version catalog

| Ferramenta | Papel | Onde é declarada |
| --- | --- | --- |
| Hilt | Injeção de dependência | `gradle/libs.versions.toml` + módulos em `di/` |
| Room | Persistência local (RNF03, RNF07) | `libs.versions.toml` + `data/local/` |
| Coroutines + Flow | Assincronia e reatividade | `libs.versions.toml` |
| Navigation Compose | Fluxo de telas | `libs.versions.toml` + `ui/navigation/` |
| Compose BOM | Versões da UI unificadas | `libs.versions.toml` |

**Convenção de dependências:** toda dependência e toda versão é adicionada **somente** em [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). Os `build.gradle.kts` só referenciam `libs.*`, sem versão fixa.

**Notas do build (AGP 9):**

- O projeto usa **AGP 9.5 + Gradle 9.6 + JDK 17**. Desde o AGP 9.0 o Kotlin é compilado nativamente (*built-in Kotlin*): por isso **não existe o plugin `kotlin-android`** nos build files — isso é esperado. A versão do Kotlin (2.4.21) é fixada no `build.gradle.kts` raiz, via `buildscript`.
- O Compose usa o plugin `org.jetbrains.kotlin.plugin.compose` (versão acompanhando a do Kotlin).
- `compileSdk = 37` (Android 17): exigência das dependências atuais do AndroidX (ex.: `core-ktx 1.19.1`).

## 6. Testes

- **Testes unitários** (`app/src/test`): cobrem `domain/usecase` e `domain/model` — principalmente os cálculos de materiais e custos. São a base para a validação das regras com o cliente (risco R01/R02).
- **Testes de instrumentação** (`app/src/androidTest`): fluxos críticos da UI.
- Comando: `./gradlew test`

## 7. Alinhamento da equipe

Para considerar a decisão de arquitetura alinhada, a equipe confirma no planning:

- [ ] Ambos os integrantes revisaram este documento.
- [ ] Todos os novos arquivos seguem a tabela da seção 4.
- [ ] Dependências são adicionadas apenas no version catalog.
- [ ] Regras de negócio (cálculos) ficam em `domain/`, nunca em `ui/`.

Essa decisão será revista ao final do MVP — em particular, se o projeto passar a exigir múltiplos módulos Gradle (`:app`, `:core`, `:feature-...`) ou uma abordagem MVI.
