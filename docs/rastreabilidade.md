# Rastreabilidade: requisitos × testes

Atende ao critério da seção 15 do PRD: todo critério de aceitação tem ao menos um teste, automatizado ou manual. Os requisitos do PRD do Inbox (`app/PRD-inbox.md`) aparecem como **INB RF-00x**; no PRD principal eles correspondem a RF-021 a RF-024 e RF-028 (versão 0.4).

**Legenda**

- **U:** teste unitário (`app/src/test`, roda com `./gradlew testDebugUnitTest`).
- **B:** teste de banco, Room em memória (`app/src/androidTest/.../data/ClarityDatabaseTest`) e migração 1 → 2 (`MigrationTest`).
- **I:** teste de interface Compose (`app/src/androidTest/.../ui/MainFlowsTest`).
- **M:** verificação manual no emulador (Pixel 6 Pro, API 37), feita em 20/10/2026.
- **Pendente:** ainda precisa ser executado pela equipe (aparelho físico, TalkBack, sessões).

Última execução: 50 testes unitários e 20 instrumentados, todos passando (20/10/2026, emulador Pixel 6 Pro com Android 12).

## Requisitos funcionais

| Requisito | Critério | Teste | Status |
| --- | --- | --- | --- |
| RF-001 | Primeiro acesso mostra Boas-vindas | U `AppViewModelTest` T-O1; I `f1_boasVindasLevamAHojeVazia` | OK |
| RF-001 | Começar leva a Hoje; voltar fecha o app | I `f1_…`; M (voltar em Hoje abre a tela inicial do sistema) | OK |
| RF-001 | Reabrir vai direto para Hoje | U T-O1; M (forçar parada e reabrir) | OK |
| RF-002 | Quatro áreas, sem duplicar | B `rf002_areasIniciaisSemDuplicar` | OK |
| RF-003 | Padrões Faculdade, Média, hoje | U `RF-003 formulario abre…` | OK |
| RF-003 | Salvar só com título → volta, aparece em Hoje, "Tarefa criada" | I `f3_criarSoComTitulo…`; M | OK |
| RF-003 | Filtro Trabalho preseleciona Trabalho | U T-F4 (com Projetos) | OK |
| RF-003 | Salvar duas vezes → uma tarefa | U T-F3 | OK |
| RF-003 | Persistência após reiniciar | M (forçar parada); T-P2 pendente | Parcial |
| RF-004 | Vazio ou só espaços → "Informe um título" | U T-F1; I `rf004_tituloVazioMostraErro` | OK |
| RF-004 | Máximo de 100 e contador perto do limite | U T-F2; M (com 85 caracteres o campo mostra 85/100) | OK |
| RF-004 | Espaços nas pontas removidos | U `RF-004 titulo e gravado…`, T-F3 | OK |
| RF-005 | Mudar prazo tira de Hoje | U T-D4 | OK |
| RF-005 | Prioridade Alta sobe na lista | U T-H1 (ordem) | OK |
| RF-005 | Concluída editada continua Concluída, mesmo XP | U `RF-005 e RN-09…`; B `rn09_editarNaoMudaEstadoNemXp` | OK |
| RF-006 | Sem alterações fecha sem perguntar | U `RF-006 so pergunta…` | OK |
| RF-006 | Com alterações pergunta Descartar / Continuar editando | M (Cancelar e voltar) | OK |
| RF-006 | Descartar não grava | M (prioridade continuou Média) | OK |
| RF-007 | Sai de Hoje; contadores ajustam | U T-H2, T-D2; I `f3_…` ("Tudo feito por hoje"); M | OK |
| RF-007 | Em Tarefas vai para Concluídas riscada | M | OK |
| RF-007 | Continua Concluída após reabrir o app | M | OK |
| RF-008 | 30 → 40 XP e "+10 XP" | U `RF-008 concluir mostra +10 XP…`; B T-X1; I `f3_…` | OK |
| RF-008 | Dois toques → +10 só | U T-X2; B T-X2 (8 conclusões em paralelo) | OK |
| RF-008 | Concluir, reabrir, concluir → +10 no total, sem "+10 XP" na 2ª | U T-X3; B T-X3 | OK |
| RF-009 | Desmarcar volta a Pendentes e a Hoje | U `RF-009 reabrir mantem o XP`; B T-X3 | OK |
| RF-009 | XP não muda | U, B (idem) | OK |
| RF-010 | Confirmar remove, "Tarefa removida", XP igual | U T-X4; B T-X4 | OK |
| RF-010 | Toque longo abre a confirmação; cancelar não muda nada | M | OK |
| RF-010 | Remoção como ação acessível (TalkBack) | Ação personalizada no cartão; T-U1 | Pendente |
| RF-011 | Ordem e selo ATRASADA; futuras fora | U T-H1 | OK |
| RF-011 | Concluída hoje não aparece | U T-H1 | OK |
| RF-011 | Cabeçalho com dia, data e N tarefas | I `f3_…` ("1 tarefa para hoje"); M | OK |
| RF-012 | Exemplo mostra 3, 1 e 2 | U T-H2 | OK |
| RF-012 | Atualiza sem recarregar | I `f3_…`; M | OK |
| RF-012 | Singular com valor 1 | M ("1 Concluída") | OK |
| RF-013 | XP total = soma dos registros | B T-X1, T-X5 | OK |
| RF-013 | Mesmo valor após reiniciar | M (10 XP após forçar parada) | OK |
| RF-014 | Contagens das seções | M ("1 tarefa" / "2 tarefas") | OK |
| RF-014 | ATRASADA também em Tarefas | Mesmo componente `TaskCard`; U `RN-07` | OK |
| RF-014 | Seção vazia mostra linha de estado vazio | M | OK |
| RF-015 | Filtro vale para seções e contagens | U `RF-015 filtro por area…` | OK |
| RF-015 | Filtro mantido ao sair e voltar | Filtro em `SessionState`; M | OK |
| RF-016 | Desfazer reabre a tarefa | U T-X3 (ação da mensagem) | OK |
| RF-017 | Nada para hoje / Tudo feito por hoje | I `f1_…`, `f3_…` | OK |
| RF-017 | Nenhuma tarefa nesta área; Inbox vazio; Histórico vazio | I `inbox_…` ("Inbox vazio"); M | OK |
| RF-018 | Falha ao salvar mantém o formulário | U T-E2 | OK |
| RF-018 | Falha ao concluir mantém Pendente e XP | U `RF-018 falha ao concluir…` | OK |
| RF-019 | Barra (Hoje, Tarefas, Histórico) leva às telas e destaca o item; abas Inbox e Todas | I `inbox_…`; M | OK |
| RF-019 | Voltar numa tela principal leva a Hoje | `navigateToTab` (popUpTo Hoje); M | OK |
| RF-020 | 23:59 → meia-noite: ATRASADA e Concluídas 0 em menos de 1 min | U T-D1 (relógio falso), `RF-020…` | OK |
| RF-020 | Voltar ao app mostra a data de hoje | Fluxo de data reiniciado ao voltar ao primeiro plano (`WhileSubscribed`); T-D1 manual | Pendente |
| RF-021 | Captura no topo de Para organizar, campo limpo e com foco, mensagem | U `INB RF-002 captura so com titulo…`; I `inbox_…`; M | OK |
| RF-021 | Campo vazio ou só espaços desabilita + | U `INB RF-002 titulo vazio…`; I `inbox_…` | OK |
| RF-021 | Toque duplo → uma tarefa | U `INB RF-002 captura so com titulo…` | OK |
| RF-021 | Falha mantém o texto e avisa | U `INB RF-002 falha de gravacao…` | OK |
| RF-022 | Sem área → "Escolha uma área"; continua no Inbox | U `INB RF-003 organizar…`; I `inbox_…`; M | OK |
| RF-022 | Pessoal e Sem prazo → sai do Inbox, fica em Todas, fora de Hoje | U `INB RF-003…`, `INB RF-006…`; B `inb003_…`; M | OK |
| RF-022 | Prazo de hoje → aparece em Hoje | I `inbox_…` | OK |
| RF-022 | Escolher e cancelar não grava | U `INB 4-3 escolher area ou data sem confirmar…` | OK |
| RF-023 | Inbox só com não organizadas, mais recentes primeiro; Todas com todas | U `INB RF-001…`, `INB RF-004…`; B `inboxComCapturasRecentesPrimeiro`; M | OK |
| RF-023 | Filtro de área não inclui tarefa sem área | U `INB RF-005…` | OK |
| RF-023 | Organizar tira do Inbox sem outro registro; contagem da aba | U `INB RF-001 … atualiza apos organizar`; B `inb003_…`; I `inbox_…` (1 registro) | OK |
| RF-024 | Confirmar remove de todas as visões | U `INB RF-007…`; B `inb007_…`; M (diálogo "Ela sai do Inbox e de todas as listas") | OK |
| RF-024 | Cancelar não muda nada | Mesmo `ConfirmDialog` de RF-010; M | OK |
| RF-025 | Grupo HOJE com "Concluiu …", área e hora | U `RF-025…`; M | OK |
| RF-025 | Reabrir tira o registro | U T-R1 | OK |
| RF-025 | Pendente ou no Inbox não aparece | U `RF-025 tarefa pendente ou no Inbox…` | OK |
| RF-026 | Contadores por área | U `RN-18…`; M | OK |
| RF-026 | Busca não muda contadores | U T-R2 | OK |
| RF-027 | "room" encontra Room sem diferenciar maiúsculas | U T-R2; M | OK |
| RF-027 | Sem resultados; limpar volta tudo | U `RF-027…` | OK |
| RF-028 | Mover preserva área e prazo; mensagem | U `INB 4-5 mover para o Inbox…`; B `rn22_…`; M | OK |
| RF-028 | Concluída não mostra a ação | U `RF-005 e RN-09…` (`canMoveToInbox` falso); B `rn22_…`; M | OK |
| RF-028 | XP não muda | U `INB 4-5…`; B `rn22_…` | OK |
| RN-20 | Tarefa no Inbox não conclui nem ganha XP | B `rn20_…`; sem caixa no cartão (M) | OK |
| RN-21 | Nova tarefa pode ficar sem prazo | U `RN-21 nova tarefa pode ficar sem prazo` | OK |

## PRD do Inbox

| Requisito | Onde está coberto | Status |
| --- | --- | --- |
| INB RF-001 Visualizar Inbox | RF-023 | OK |
| INB RF-002 Capturar | RF-021 | OK |
| INB RF-003 Organizar | RF-022 | OK |
| INB RF-004 Todas as tarefas | RF-023 | OK |
| INB RF-005 Áreas da vida | RF-023 (filtro de área) | OK |
| INB RF-006 Today | U `INB RF-006…`; I `inbox_…` | OK |
| INB RF-007 Excluir | RF-024 | OK |
| INB RF-008 Persistir | B `inb002_…`, `inb003_…`; M (forçar parada com tarefas no Inbox e organizadas); migração em `MigrationTest` | OK |
| INB 4.5 Voltar ao Inbox | RF-028 | OK |
| INB seção 8 (incompleta) | Offline e persistência atendidos (RNF-10, RNF-12); o restante do texto não foi recebido | Parcial |

## Cenários críticos da seção 15

| ID | Teste | Status |
| --- | --- | --- |
| T-O1 | U `AppViewModelTest`; I `f1_…`; M | OK |
| T-X1 a T-X5 | B `ClarityDatabaseTest` (`tx1` a `tx5`); U `TaskActionsTest` | OK |
| T-H1, T-H2 | U `TaskRulesTest` | OK |
| T-P1 | M (forçar parada com tarefas, XP e capturas) | OK |
| T-P2 | Reiniciar o aparelho | Pendente |
| T-P3 | Instalar versão nova sobre a anterior com dados | Parcial: migração 1 → 2 testada com dados (`MigrationTest`); falta instalar por cima num aparelho |
| T-D1 | U `DayChangeTest` | OK |
| T-D2, T-D4 | U `TaskRulesTest` | OK |
| T-D3 | U `T-D3 prazo e data de calendario…` | OK |
| T-F1 a T-F4 | U `ValidationTest`, `TaskFormViewModelTest`; I `rf004_…` | OK |
| T-I1 | U `INB RF-003…`; I `inbox_…` | OK |
| T-I2 | U `INB 4-3…` | OK |
| T-I3 | U `INB RF-007…`; B `inb007_…` | OK |
| T-I4 | U `INB RF-006…`; B `inb003_…` | OK |
| T-I5 | U `INB 4-5…`; B `rn22_…` | OK |
| T-I6 | B `MigrationTest` | OK |
| T-R1, T-R2 | U `HistoryTest` | OK |
| T-E1 | M (estados vazios) | OK |
| T-E2 | U (salvar, concluir, capturar com falha simulada) | OK |
| T-U1 | TalkBack em F3, F4 e F7 | Pendente |
| T-U2 | M (fonte em 200%: sem corte; contadores em 2 linhas) | OK |

## Requisitos não funcionais

| ID | Como foi verificado | Status |
| --- | --- | --- |
| RNF-01 | F3 = abrir, digitar, salvar; captura no Inbox = digitar, + (o foco fica no campo para a próxima) | OK |
| RNF-02 | Concluir e reabrir com 1 toque, sem diálogo | OK |
| RNF-03 | Caixas, chips e botões do Material 3 (48 dp); linhas do seletor de área com mínimo de 48 dp | OK (Layout Inspector pendente) |
| RNF-04 | Ícones decorativos sem descrição; botões só com ícone descritos; caixa anuncia "Concluída"/"Pendente" | Pendente (TalkBack) |
| RNF-05 | Pares de cor do tema medidos (mínimo 5,1:1); prioridade e área sempre com texto | OK |
| RNF-06 | T-U2 | OK |
| RNF-07 | Cores só em `ui/theme/Theme.kt` | OK |
| RNF-08 | Room fora da thread principal; StrictMode em debug | OK |
| RNF-09 | Índice único em `xp_entry.task_id`; transações nos repositórios | OK |
| RNF-10 | T-P1 OK; T-P3 coberto pelo `MigrationTest`; T-P2 pendente | Parcial |
| RNF-11 | Emulador de 411 dp; layout de uma coluna; 360 dp pendente | Parcial |
| RNF-12 | Manifesto sem permissões; backup em nuvem desligado. O manifesto final só tem a permissão interna de assinatura do androidx.core | OK |
| RNF-13 | Erros viram mensagens (`UiText`); falhas simuladas nos testes U | OK |
| RNF-14 | Domínio em Kotlin puro, testado na JVM | OK |
