# Clarity

App Android nativo e offline para registrar tarefas por área da vida, ver o que vence hoje e acompanhar o que foi feito. MVP acadêmico; requisitos em [`app/PRD.md`](app/PRD.md) e, para o Inbox, em [`app/PRD-inbox.md`](app/PRD-inbox.md).

- **Núcleo:** Boas-vindas, Hoje, Todas as tarefas, Nova/Editar tarefa.
- **Extensão:** Inbox (aba da tela Tarefas: tarefas ainda não organizadas) e Histórico. Ativos; ver `EXTENSION_ENABLED` em `ui/ClarityApp.kt`.

## Requisitos

- Android Studio recente (AGP 9.3, Gradle 9.5, Kotlin 2.4 embutido no AGP).
- JDK 17 ou superior (o Gradle usa o toolchain configurado em `gradle/gradle-daemon-jvm.properties`).
- Android SDK com a plataforma 37. O app roda do Android 8.0 (API 26) em diante.

## Compilar e rodar

```bash
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala no emulador ou aparelho conectado
```

No Android Studio: abrir a pasta do projeto e rodar a configuração `app`.

## Testar

```bash
./gradlew testDebugUnitTest            # testes unitários (JVM, sem Android)
./gradlew connectedDebugAndroidTest    # testes de banco (Room em memória) e de interface; exige emulador ou aparelho
```

| Nível | Onde | O que cobre |
| --- | --- | --- |
| Unitário | `app/src/test` | Regras de domínio (ordem, contadores, atraso, validação, Histórico, virada do dia com relógio falso) e ViewModels com repositórios falsos (padrões do formulário, toque duplo, falhas simuladas, XP) |
| Banco | `app/src/androidTest/.../data` | Transação de conclusão + XP, unicidade do XP, concorrência, ids sem reuso, capturar, organizar e mover para o Inbox, FK, migração 1 → 2 com dados (`MigrationTest`) |
| Interface | `app/src/androidTest/.../ui` | F1 (boas-vindas), F3 e F4 (criar e concluir com +10 XP), erro de título, F9 (capturar no Inbox e organizar até aparecer em Hoje) |

A planilha de rastreabilidade (critério × teste) está em [`docs/rastreabilidade.md`](docs/rastreabilidade.md), junto com o roteiro dos testes manuais.

## Arquitetura

Módulo único, três camadas (PRD, seção 12):

```
app/src/main/java/com/example/upcampusplus/
├── domain/   Kotlin puro: modelos, regras (TaskRules, History, Validation), Clock, casos de uso, contratos dos repositórios
├── data/     Room (entidades, DAOs, banco), repositórios e DataStore (só o indicador de boas-vindas)
├── ui/       Compose: tema, componentes, uma tela + ViewModel por destino, navegação
├── AppContainer.kt         injeção de dependências manual
└── ClarityApplication.kt   cria o container; StrictMode em debug
```

- O banco está na versão 2, com os esquemas exportados em `app/schemas/` (versionar junto com o código). Mudança de esquema exige migração explícita, como `MIGRATION_1_2` em `data/db/Migrations.kt`; não há `fallbackToDestructiveMigration`.
- O Inbox não tem tabela: é a coluna `task.organized` (0 = no Inbox). Inbox, Todas, filtros de área e Hoje leem o mesmo registro.
- O XP é um registro por tarefa (`xp_entry.task_id` único), gravado na mesma transação da conclusão.
- O manifesto não pede permissões e desliga o backup em nuvem: os dados ficam só no aparelho.
