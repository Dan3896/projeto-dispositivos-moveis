# PRD Clarity (MVP acadêmico)

Oct 19, 2026 · @Daniel · Versão 0.4 (0.2 alinhada ao protótipo UP Campus+; 0.3 registra as decisões adotadas na implementação; 0.4 adota o PRD do Inbox, `app/PRD-inbox.md`, 20/10/2026)

## 1. Resumo executivo

O Clarity é um app Android nativo e offline para registrar tarefas por área da vida, ver o que vence hoje e acompanhar o que foi feito. A versão 0.2 alinha telas e funcionalidades ao protótipo UP Campus+ (PDF com 6 telas).

O protótipo marca as telas com dois selos, lidos aqui como níveis de escopo:

| Nível | Telas | Prioridade no PRD |
| --- | --- | --- |
| Núcleo (selo Plano C) | Boas-vindas, Hoje, Todas as tarefas, Nova e editar tarefa | Must: obrigatório para a entrega |
| Extensão (selo Plano B) | Inbox (aba de Tarefas), Histórico | Should: entra depois do núcleo pronto e testado |

Decisões centrais:

- **Só o título é obrigatório.** Área, prioridade e prazo já vêm preenchidos (Faculdade, Média, hoje), como no protótipo.
- **Hoje** mostra as pendentes com prazo até hoje, ordenadas por prioridade, e três contadores: Para hoje, Atrasadas e Concluídas.
- **XP:** 10 por tarefa, concedido uma única vez e mantido ao reabrir ou remover, com registro próprio e unicidade no banco. O protótipo não mostra XP; o PRD mantém a regra com exibição discreta (D-03).
- **Áreas fixas** (Faculdade, Trabalho, Projetos, Pessoal), usadas como etiqueta e filtro, sem tela própria.
- **Inbox** é uma visão das tarefas ainda não organizadas, dentro de Tarefas, e não uma tabela própria: capturar cria uma tarefa só com o título; organizar define a área (e, se quiser, a data) e a tira do Inbox (PRD do Inbox, versão 0.4).
- **Histórico** é derivado das conclusões de tarefas; não existe tabela de eventos.

A leitura dos selos (D-01) e a exibição de XP (D-03) foram implementadas conforme a recomendação, mas ainda precisam de confirmação da equipe; ver seção 17.

## 2. Contexto e problema

Quem concilia trabalho, estudos e projetos pessoais tende a espalhar pendências em lugares diferentes, perder de vista o que precisa ser feito hoje e não perceber o que já avançou.

Ferramentas completas de gestão de tarefas resolvem isso com muitos campos, estruturas e configurações. Para uma parte das pessoas, esse custo de organização vira o próprio obstáculo: organizar passa a competir com executar.

O Clarity parte de uma aposta, ainda não validada: um espaço único, com o mínimo de campos e uma recompensa visível ao concluir, reduz essa fricção o suficiente para a pessoa registrar e fechar tarefas com regularidade.

Contexto do projeto: MVP acadêmico, desenvolvido por equipe pequena e com prazo de disciplina. Isso limita escopo e infraestrutura de forma deliberada.

## 3. Visão e proposta de valor

**Proposta de valor:** menos sobrecarga na organização, mais clareza na execução.

**Visão do MVP:** ao abrir o app, a pessoa sabe em poucos segundos o que tem para hoje, o que já fez e quanto falta. Registrar uma tarefa leva poucos toques. Concluir gera uma recompensa pequena e imediata.

### Princípios de produto

| # | Princípio | Como se aplica no MVP |
| --- | --- | --- |
| P1 | Simplicidade antes de quantidade | Tarefa com 4 campos (título, área, prioridade, prazo), só o título obrigatório |
| P2 | Baixa fricção para cadastrar e concluir | Campos com valor padrão; conclusão com um toque; captura de uma linha no Inbox |
| P3 | Interface clara e consistente | Mesmo cartão de tarefa em Hoje e Tarefas; mesmo cabeçalho em todas as telas |
| P4 | Progresso perceptível sem excesso | Contadores do dia, Histórico e total de XP; nada além disso |
| P5 | Organização por áreas da vida | Quatro áreas fixas, como etiqueta e filtro, sem hierarquia |
| P6 | Funcionamento local | Banco local, sem conta, sem rede ("Tudo salvo no próprio aparelho") |
| P7 | Escopo controlado | Núcleo antes da extensão; o que não aparece no protótipo fica fora (seção 5) |

Toda funcionalidade da seção 7 aponta para pelo menos um princípio na sua justificativa.

## 4. Público-alvo e necessidades

Público inicial: pessoas que conciliam pelo menos duas frentes de responsabilidade (trabalho, estudos, projetos pessoais, vida pessoal) e usam o celular como ferramenta principal do dia. Exemplos: estudante que trabalha, profissional com projeto paralelo.

Não houve pesquisa com usuários. Tudo abaixo é descrição de intenção ou hipótese.

| Dimensão | Descrição |
| --- | --- |
| Necessidades | Um lugar único para as pendências; saber o que fazer hoje; ver que está avançando |
| Dificuldades | Pendências espalhadas; ferramentas que exigem configuração demais; sensação de não progredir |
| Contextos de uso | Início do dia para planejar; momentos curtos entre atividades para registrar; fim do dia para revisar |
| Objetivos | Não esquecer o que importa; fechar o dia sabendo o que foi feito |

### Hipóteses a validar

- H1: registrar uma tarefa só com o título, aceitando os padrões, atende à maioria dos usos diários.
- H2: separar por área da vida ajuda a perceber o equilíbrio entre frentes, sem exigir mais estrutura.
- H3: XP fixo por tarefa concluída é percebido como recompensa, e não como ruído.
- H4: a tela Hoje, ordenada por prioridade, basta como ponto de partida, sem priorização automática.
- H5: a ausência de lembretes não impede o uso regular no período de teste.
- H6: separar capturar (Inbox) de organizar reduz a perda de ideias e pendências que surgem no meio de outra atividade.

O MVP acadêmico só consegue testar H1 e parte de H4 em sessões de usabilidade (seção 14). As demais exigem uso real por semanas.

## 5. Objetivos e não objetivos

### Objetivos do MVP

1. Permitir criar, editar, concluir, reabrir e remover tarefas localmente, sem perda de dados ao reiniciar o app.
2. Mostrar na tela Hoje o que vence até hoje, por prioridade, com os contadores do dia.
3. Aplicar a regra de 10 XP por tarefa de forma consistente e à prova de cliques duplicados.
4. Organizar tarefas em quatro áreas e filtrar a lista por área.
5. Na extensão, capturar tarefas só com o título no Inbox, organizá-las depois e consultar o Histórico por dia, área e busca.
6. Entregar um app navegável, testado nos fluxos principais e documentado o bastante para avaliação acadêmica.

### Não objetivos (fora do escopo)

Estes itens não entram no MVP e não devem influenciar a arquitetura sem decisão formal de mudança de escopo:

| Item | Motivo da exclusão |
| --- | --- |
| Inteligência artificial | Sem requisito comprovado; aumenta complexidade e dependência externa |
| Integração com Telegram ou WhatsApp | Exige backend e APIs externas |
| Pomodoro ou timers de foco | Outra categoria de produto; ausente do protótipo |
| Backend, conta e sincronização em nuvem | Contraria P6 ("Sem cadastro") |
| Colaboração entre usuários | Ausente do protótipo |
| Gamificação avançada (níveis, moedas, ranking, streaks, conquistas, desafios) | Contraria P4 |
| Assinaturas e monetização | Fora do contexto acadêmico |
| Notificações e lembretes | Exigem agendamento e permissões; ver D-10 |
| Widget na tela inicial do Android | Aparece no protótipo só como exemplo de ideia capturada, não como funcionalidade |
| Automação e recorrência de tarefas | Ausentes do protótipo |
| Priorização inteligente ou delegação automática | Contraria P1; a prioridade é sempre escolhida pela pessoa |
| Subtarefas, dependências, etiquetas livres, anexos, horário, duração e descrição | Contraria P1; nenhuma tela do protótipo usa |
| Tela de gestão de áreas | O protótipo usa áreas só como chips; áreas são fixas (RN-10) |
| Notas, aprendizados e ideias no Inbox | Fora dos objetivos do PRD do Inbox: o Inbox recebe só tarefas (versão 0.4) |

## 6. Escopo do MVP

O MVP tem 5 telas e uma visão: 4 telas no núcleo, o Histórico na extensão e o Inbox como aba da tela Tarefas (PRD do Inbox, seção 3). A barra inferior tem 3 destinos (Hoje, Tarefas, Histórico); Boas-vindas e o formulário de tarefa ficam fora dela.

| Tela | Nível | Responde à pergunta | Conteúdo | Requisitos |
| --- | --- | --- | --- | --- |
| Boas-vindas | Núcleo | O que é o app? | Nome, proposta, "Começar" e três benefícios; só no primeiro acesso | RF-001 |
| Hoje (inicial) | Núcleo | O que vence até hoje e quanto já fiz? | Data e total do dia, contadores Para hoje, Atrasadas e Concluídas, lista por prioridade, XP, botões Nova tarefa e Todas as tarefas | RF-011 a RF-013, RF-016, RF-020 |
| Tarefas, aba Todas | Núcleo | Quais são todas as minhas tarefas? | Chips de área (Todas as áreas + 4), seções Pendentes e Concluídas com contagem, botão Nova tarefa; tarefas do Inbox com o selo INBOX | RF-014, RF-015, RF-023 |
| Nova, editar e organizar tarefa | Núcleo | Como registro, ajusto ou organizo uma tarefa? | Título, área, prioridade, prazo (opcional), Cancelar e Salvar (ou Organizar), Mover para o Inbox, dica | RF-003 a RF-006, RF-022, RF-028 |
| Tarefas, aba Inbox | Extensão | Onde anoto algo rápido sem organizar agora? | Campo de captura e +, lista Para organizar com Organizar e Remover, estado vazio | RF-021 a RF-024 |
| Histórico | Extensão | O que eu concluí, e em quais áreas? | Contadores por área, busca, linha do tempo agrupada por dia | RF-025 a RF-027 |

### Campos da tarefa

| Campo | Obrigatório | Regra | Padrão |
| --- | --- | --- | --- |
| Título | Sim | 1 a 100 caracteres após remover espaços nas pontas | Vazio |
| Área | Sim para tarefa organizada; vazia no Inbox (RN-21) | Faculdade, Trabalho, Projetos ou Pessoal | Nova tarefa: área do filtro ativo em Tarefas, senão Faculdade. Organizar: a que já estiver na tarefa, senão nenhuma |
| Prioridade | Não (sempre preenchida) | Alta, Média ou Baixa | Média |
| Prazo | Não | Data de calendário, sem horário; passado e futuro permitidos; pode ficar Sem prazo | Nova tarefa: hoje. Captura: sem prazo |
| Organização | Não editável no formulário | Não organizada (no Inbox) ou Organizada; muda ao organizar e ao Mover para o Inbox | Nova tarefa: Organizada. Captura: Não organizada |
| Estado | Não editável no formulário | Pendente ou Concluída; muda só ao concluir e reabrir. Só tarefa organizada conclui (RN-20) | Pendente |

"Só o título é obrigatório" significa que a pessoa pode salvar só digitando o título: em Nova tarefa a área vem preenchida, e no Inbox a tarefa nasce sem área nem prazo.

## 7. Requisitos funcionais

São 28 requisitos: 19 Must (núcleo) e 9 Should (1 do núcleo e 8 da extensão). Na versão 0.4, RF-021 a RF-024 foram reescritos e RF-028 entrou para seguir o PRD do Inbox (`app/PRD-inbox.md`, citado como INB). Os identificadores foram reorganizados na versão 0.2 e passam a ser estáveis; um requisito removido mantém o número e passa a constar como Removido.

| ID | Nome | Tela | Prioridade | Regras |
| --- | --- | --- | --- | --- |
| RF-001 | Boas-vindas no primeiro acesso | Boas-vindas | Must | Nenhuma |
| RF-002 | Áreas iniciais | Todas | Must | RN-10 |
| RF-003 | Criar tarefa | Nova tarefa | Must | RN-01, RN-02, RN-14 |
| RF-004 | Validar formulário | Nova tarefa | Must | RN-01 |
| RF-005 | Editar tarefa | Editar tarefa | Must | RN-01, RN-09 |
| RF-006 | Cancelar criação ou edição | Nova tarefa | Must | Nenhuma |
| RF-007 | Concluir tarefa | Hoje, Tarefas | Must | RN-03, RN-13 |
| RF-008 | Conceder XP | Hoje, Tarefas | Must | RN-03 a RN-06 |
| RF-009 | Reabrir tarefa | Tarefas | Must | RN-04 |
| RF-010 | Remover tarefa com toque longo | Hoje, Tarefas | Must | RN-06, RN-13 |
| RF-011 | Lista do dia por prioridade | Hoje | Must | RN-07, RN-12 |
| RF-012 | Contadores do dia | Hoje | Must | RN-08 |
| RF-013 | Exibir XP | Hoje | Must | RN-05 |
| RF-014 | Pendentes e Concluídas | Tarefas | Must | RN-12 |
| RF-015 | Filtrar por área | Tarefas | Must | RN-10 |
| RF-016 | Desfazer conclusão | Hoje, Tarefas | Should | RN-04 |
| RF-017 | Estados vazios | Todas | Must | Nenhuma |
| RF-018 | Feedback e tratamento de erros | Todas | Must | RN-13 |
| RF-019 | Navegação principal | Todas | Must | Nenhuma |
| RF-020 | Virada do dia | Hoje | Must | RN-11 |
| RF-021 | Capturar no Inbox | Tarefas, aba Inbox | Should | RN-01, RN-02, RN-15 |
| RF-022 | Organizar tarefa do Inbox | Organizar tarefa | Should | RN-16, RN-21 |
| RF-023 | Visões Inbox e Todas | Tarefas | Should | RN-20, RN-21 |
| RF-024 | Remover do Inbox | Tarefas, aba Inbox | Should | RN-06, RN-13 |
| RF-025 | Linha do tempo do Histórico | Histórico | Should | RN-17 |
| RF-026 | Contadores por área | Histórico | Should | RN-18 |
| RF-027 | Buscar no Histórico | Histórico | Should | RN-19 |
| RF-028 | Mover para o Inbox | Editar tarefa | Should | RN-22 |

Abaixo, o detalhe de cada requisito. Os critérios usam Dado / Quando / Então. Os exemplos tratam 15/10/2026 como hoje, como no protótipo.

### RF-001 Boas-vindas no primeiro acesso (Must)

- **Descrição:** no primeiro acesso, o app mostra a tela de boas-vindas com nome, proposta, benefícios e o botão Começar.
- **Justificativa:** apresenta a proposta sem tutorial nem cadastro.
- **Critérios:**
    - Dado um app recém-instalado, quando é aberto, então a tela de boas-vindas aparece.
    - Dado que toquei em Começar, então vou para Hoje e o botão voltar do sistema fecha o app, sem retornar às boas-vindas.
    - Dado que já toquei em Começar antes, quando reabro o app, então ele abre direto em Hoje.

### RF-002 Áreas iniciais (Must)

- **Descrição:** no primeiro acesso, o app cria as áreas Faculdade, Trabalho, Projetos e Pessoal, nessa ordem.
- **Critérios:**
    - Dado o primeiro acesso, então os chips de área do formulário e de Tarefas mostram exatamente as quatro áreas.
    - Dado que o app já foi aberto antes, quando é reaberto, então nenhuma área é duplicada.

### RF-003 Criar tarefa (Must)

- **Descrição:** a pessoa cria uma tarefa digitando o título; área, prioridade e prazo vêm com os padrões da RN-14 e podem ser alterados.
- **Justificativa:** registrar é a ação base do produto; os padrões permitem salvar só com o título.
- **Pré-condição:** formulário aberto por Nova tarefa em Hoje ou em Tarefas.
- **Critérios:**
    - Dado o formulário recém-aberto, então Faculdade, Média e o prazo de hoje já estão selecionados.
    - Dado o título Estudar ViewModel e os padrões, quando toco em Salvar, então volto à tela anterior, a tarefa aparece em Hoje e a mensagem Tarefa criada é exibida.
    - Dado que abri Nova tarefa com o filtro Trabalho ativo em Tarefas, então a área Trabalho já vem selecionada.
    - Dado que toquei em Salvar duas vezes rapidamente, então existe uma única tarefa nova (RN-02).
    - Dado que reiniciei o app, então a tarefa criada continua lá.

### RF-004 Validar formulário (Must)

- **Descrição:** o formulário impede salvar sem título válido e mostra o erro no campo.
- **Critérios:**
    - Dado título vazio ou só com espaços, quando toco em Salvar, então nada é salvo e o campo mostra Informe um título.
    - Dado título com 101 caracteres, então o campo não aceita além de 100 e mostra o contador perto do limite.
    - Dado título com espaços nas pontas, quando salvo, então o título é gravado sem eles.

### RF-005 Editar tarefa (Must)

- **Descrição:** tocar numa tarefa em Hoje ou Tarefas abre o formulário preenchido, com o título Editar tarefa.
- **Justificativa:** planos mudam; refazer a tarefa custaria mais que editar. O protótipo não mostra a edição, que reaproveita o formulário de criação.
- **Critérios:**
    - Dado que mudei o prazo de hoje para 18/10 e salvei, então a tarefa sai de Hoje e aparece em Tarefas com 18/10.
    - Dado que mudei a prioridade de Média para Alta, então a tarefa sobe na lista de Hoje.
    - Dado uma tarefa concluída que edito e salvo, então ela continua Concluída e o XP não muda (RN-09).

### RF-006 Cancelar criação ou edição (Must)

- **Descrição:** Cancelar ou voltar descarta as mudanças.
- **Critérios:**
    - Dado um formulário sem alterações, quando toco em Cancelar, então ele fecha sem perguntar nada.
    - Dado alterações não salvas, quando toco em Cancelar ou voltar, então aparece a pergunta Descartar alterações, com as opções Descartar e Continuar editando.
    - Dado que escolhi Descartar, então nada é gravado.

### RF-007 Concluir tarefa (Must)

- **Descrição:** marcar a caixa de uma tarefa pendente a conclui (Marque para concluir).
- **Critérios:**
    - Dado uma tarefa pendente em Hoje, quando marco a caixa, então ela sai da lista, o contador Concluídas sobe 1 e Para hoje desce 1.
    - Dado uma pendente em Tarefas, quando marco a caixa, então ela vai para Concluídas com o título riscado.
    - Dado que fechei e reabri o app, então a tarefa continua Concluída.

### RF-008 Conceder XP (Must)

- **Descrição:** a primeira conclusão de cada tarefa gera 10 XP.
- **Critérios:**
    - Dado XP total 30 e uma tarefa nunca concluída, quando a concluo, então o XP passa a 40 e a mensagem de conclusão mostra +10 XP.
    - Dado dois toques seguidos na caixa, então o XP aumentou só 10.
    - Dado concluir, reabrir e concluir de novo, então o XP aumentou só 10 no total e a segunda conclusão não mostra +10 XP.

### RF-009 Reabrir tarefa (Must)

- **Descrição:** desmarcar uma tarefa em Concluídas (tela Tarefas) volta a tarefa para Pendente.
- **Critérios:**
    - Dado uma concluída em Tarefas, quando desmarco a caixa, então ela volta para Pendentes e, se o prazo for até hoje, reaparece em Hoje.
    - Dado a reabertura, então o XP total não muda (RN-04).

### RF-010 Remover tarefa com toque longo (Must)

- **Descrição:** segurar uma tarefa em Hoje ou Tarefas abre a confirmação Remover esta tarefa (segure para remover).
- **Justificativa:** sem remoção, tarefas criadas por engano poluem as listas; a confirmação evita perda por toque acidental.
- **Critérios:**
    - Dado que confirmei, então a tarefa some de todas as listas, aparece Tarefa removida e o XP total não muda (RN-06).
    - Dado que cancelei, então nada muda.
    - Dado o leitor de tela ativo, então a remoção está disponível como ação acessível do item, sem depender do gesto.

### RF-011 Lista do dia por prioridade (Must)

- **Descrição:** a seção Para hoje lista as tarefas pendentes com prazo até hoje, na ordem da RN-12. Atrasadas levam o selo ATRASADA. Cada item mostra área, prioridade (cor na borda e texto) e prazo.
- **Critérios:**
    - Dado pendentes com prazo 14/10 (Alta), 15/10 (Alta), 15/10 (Média) e 18/10, então a lista mostra as três primeiras nessa ordem, a de 14/10 com ATRASADA, e a de 18/10 não aparece.
    - Dado uma tarefa concluída hoje, então ela não aparece na lista.
    - Dado o cabeçalho, então ele mostra dia da semana, data e N tarefas para hoje, com N igual ao contador Para hoje.

### RF-012 Contadores do dia (Must)

- **Descrição:** três cartões em Hoje (Para hoje, Atrasadas, Concluídas), calculados pela RN-08. São o indicador de progresso diário.
- **Critérios:**
    - Dado o exemplo de RF-011 e 2 tarefas concluídas hoje, então os cartões mostram 3, 1 e 2.
    - Dado que concluo uma tarefa, então os cartões se atualizam sem recarregar a tela.
    - Dado o valor 1, então o rótulo fica no singular (Atrasada, Concluída).

### RF-013 Exibir XP (Must)

- **Descrição:** o cabeçalho de Hoje mostra o XP total; a mensagem de conclusão mostra +10 XP quando houver ganho. Forma de exibição em D-03.
- **Critérios:**
    - Dado 5 registros de XP, então Hoje mostra 50 XP.
    - Dado que reiniciei o app, então o valor é o mesmo.

### RF-014 Pendentes e Concluídas (Must)

- **Descrição:** a tela Todas as tarefas mostra duas seções, Pendentes e Concluídas, cada uma com a contagem, na ordem da RN-12. Concluídas aparecem com caixa marcada e título riscado.
- **Critérios:**
    - Dado 4 pendentes e 2 concluídas, então as seções mostram 4 tarefas e 2 tarefas.
    - Dado uma pendente atrasada, então ela leva o selo ATRASADA também nesta tela.
    - Dado uma seção sem itens, então ela mostra uma linha de estado vazio (RF-017) em vez de sumir.

### RF-015 Filtrar por área (Must)

- **Descrição:** chips Todas, Faculdade, Trabalho, Projetos e Pessoal no topo de Tarefas; um selecionado por vez; padrão Todas. O filtro vale para as duas seções e para as contagens.
- **Justificativa:** é o único filtro do protótipo e substitui uma tela de áreas.
- **Critérios:**
    - Dado o chip Faculdade, então só aparecem tarefas de Faculdade, e as contagens refletem o filtro.
    - Dado que saio de Tarefas e volto na mesma sessão, então o filtro escolhido continua ativo.

### RF-016 Desfazer conclusão (Should)

- **Descrição:** a mensagem de conclusão tem a ação Desfazer por alguns segundos, que reabre a tarefa.
- **Justificativa:** em Hoje a tarefa concluída some da lista; sem Desfazer, corrigir um toque errado exige ir até Tarefas.
- **Critérios:**
    - Dado que concluí por engano e toquei em Desfazer, então a tarefa volta para a lista de Hoje como Pendente e o XP segue a RN-04.

### RF-017 Estados vazios (Must)

- **Descrição:** cada lista sem itens mostra uma mensagem curta e, quando faz sentido, uma ação.
- **Critérios:**
    - Dado nenhuma tarefa com prazo até hoje, então Hoje mostra Nada para hoje e o botão Nova tarefa continua visível.
    - Dado que concluí todas as tarefas do dia, então Hoje mostra Tudo feito por hoje.
    - Dado um filtro de área sem resultados, então Tarefas mostra Nenhuma tarefa nesta área.
    - Dado o Inbox ou o Histórico vazios, então mostram uma frase sobre como os itens aparecem ali (Inbox vazio: anotar acima e tocar em +).

### RF-018 Feedback e tratamento de erros (Must)

- **Descrição:** criar, salvar, concluir, remover e capturar geram confirmação curta em snackbar. Falhas de gravação geram mensagem de erro e nada muda na tela.
- **Critérios:**
    - Dado uma falha simulada ao salvar, então o formulário continua aberto e preenchido e aparece Não foi possível salvar. Tente de novo.
    - Dado uma falha ao concluir, então a tarefa continua Pendente e o XP não muda (RN-13).

### RF-019 Navegação principal (Must)

- **Descrição:** barra inferior com Hoje, Tarefas e Histórico; item ativo destacado. Tarefas tem as abas Inbox e Todas e lembra a última aberta na sessão. Hoje tem o atalho Todas as tarefas, que abre a aba Todas. Enquanto a extensão não estiver pronta, a barra mostra só Hoje e Tarefas, e Tarefas mostra só Todas.
- **Critérios:**
    - Dado qualquer tela principal, quando toco num item da barra, então vou para a tela correspondente e o item fica destacado.
    - Dado o botão voltar do sistema numa tela principal diferente de Hoje, então volto para Hoje.

### RF-020 Virada do dia (Must)

- **Descrição:** Hoje recalcula a data quando o app volta ao primeiro plano e quando a data muda com o app aberto.
- **Critérios:**
    - Dado o app aberto às 23:59 com uma pendente de hoje, quando o relógio passa da meia-noite, então em até 1 minuto ela recebe o selo ATRASADA e Concluídas volta a 0.
    - Dado o app em segundo plano desde ontem, quando o abro, então o cabeçalho mostra a data de hoje.

### RF-021 Capturar no Inbox (Should)

- **Descrição:** campo de uma linha no topo da aba Inbox e botão +. A tarefa nasce não organizada, só com o título, sem área e sem prazo, com a data de criação (RN-15). INB RF-002.
- **Justificativa:** registrar algo no meio de outra atividade sem decidir área, prioridade ou prazo (Capture agora, organize depois).
- **Critérios:**
    - Dado o texto Perguntar ao professor sobre a entrega, quando toco em +, então a tarefa aparece no topo de Para organizar com a hora da captura, o campo é limpo e continua com o foco, e aparece Tarefa adicionada ao Inbox.
    - Dado o campo vazio ou só com espaços, então o botão + fica desabilitado.
    - Dado um toque duplo em +, então só uma tarefa é criada.
    - Dado uma falha de gravação, então o texto continua no campo e aparece Não foi possível adicionar. Tente de novo.

### RF-022 Organizar tarefa do Inbox (Should)

- **Descrição:** Organizar (ou tocar no cartão) abre o formulário em modo Organizar tarefa, com o que já estiver preenchido e sem padrões: a pessoa escolhe a área, opcionalmente o prazo, e confirma em Organizar (RN-16). INB RF-003.
- **Critérios:**
    - Dado que toquei em Organizar sem escolher área, então aparece Escolha uma área e a tarefa continua no Inbox.
    - Dado que escolhi Pessoal e deixei Sem prazo, quando confirmo, então a tarefa sai do Inbox, aparece em Todas com Pessoal · Média · Sem prazo e não entra em Hoje.
    - Dado que escolhi a data de hoje, quando confirmo, então a tarefa aparece em Hoje.
    - Dado que escolhi área ou data e cancelei, então nada é gravado e a tarefa continua no Inbox.

### RF-023 Visões Inbox e Todas (Should)

- **Descrição:** a tela Tarefas tem as abas Inbox (com a contagem, ex.: Inbox (3)) e Todas. Todas mostra organizadas e não organizadas, cada uma uma vez; as do Inbox têm o selo INBOX, borda neutra e nenhuma caixa de conclusão (RN-20). O filtro de área não inclui tarefa sem área (RN-21). INB RF-001, RF-004 e RF-005.
- **Critérios:**
    - Dado 2 tarefas no Inbox e 1 organizada, então Inbox lista as 2, da mais recente para a mais antiga, e Todas lista as 3.
    - Dado o filtro Trabalho, então tarefas sem área não aparecem.
    - Dado que organizei uma tarefa, então ela sai da aba Inbox e a contagem da aba diminui, sem criar outro registro.

### RF-024 Remover do Inbox (Should)

- **Descrição:** o ícone de lixeira no cartão do Inbox abre a confirmação Remover esta tarefa? · Ela sai do Inbox e de todas as listas. Não há remoção automática por tempo. INB RF-007.
- **Critérios:**
    - Dado que confirmei, então a tarefa some do Inbox e de Todas e aparece Tarefa removida.
    - Dado que cancelei, então nada muda.

### RF-025 Linha do tempo do Histórico (Should)

- **Descrição:** lista de registros (RN-17) agrupada por dia, do mais recente ao mais antigo, com rótulos Hoje, Ontem ou a data. Cada item mostra Concluiu e o título da tarefa, a área e a hora.
- **Critérios:**
    - Dado que concluí Ler capítulo de Room hoje às 10:20, então o grupo HOJE mostra Concluiu Ler capítulo de Room, Faculdade, 10:20.
    - Dado que reabri essa tarefa, então o registro de conclusão some do Histórico.
    - Dado uma tarefa pendente ou no Inbox, então ela não aparece no Histórico.

### RF-026 Contadores por área (Should)

- **Descrição:** quatro cartões no topo do Histórico com o número de registros por área, em todo o período (RN-18).
- **Critérios:**
    - Dado 12 registros de Faculdade, 7 de Trabalho, 5 de Projetos e 4 de Pessoal, então os cartões mostram esses números.
    - Dado uma busca ativa, então os contadores não mudam.

### RF-027 Buscar no Histórico (Should)

- **Descrição:** campo Buscar no histórico que filtra a linha do tempo pelo texto (RN-19).
- **Critérios:**
    - Dado o termo room, então aparecem os registros com Room no texto, sem diferenciar maiúsculas.
    - Dado um termo sem resultados, então aparece Nada encontrado para o termo.
    - Dado que limpei o campo, então a linha do tempo completa volta.

### RF-028 Mover para o Inbox (Should)

- **Descrição:** o formulário de uma tarefa organizada e pendente tem a ação Mover para o Inbox. Ela grava o que está no formulário e devolve a tarefa ao Inbox com área e prazo preservados, para revisão na próxima organização (RN-22). INB seção 4.5.
- **Critérios:**
    - Dado Ler · Projetos · prazo em 2 dias, quando toco em Mover para o Inbox, então a tarefa aparece no Inbox com Projetos e a data, e aparece Tarefa movida para o Inbox.
    - Dado uma tarefa concluída, então a ação não aparece.
    - Dado que movi, então o XP total não muda.

## 8. Regras de negócio

Cada tarefa gera no máximo 10 XP em toda a sua vida, e esse XP nunca é retirado. As demais regras definem quando algo conta como "hoje", "atrasado" e "concluído".

| ID | Regra |
| --- | --- |
| RN-01 | Título obrigatório, 1 a 100 caracteres após remover espaços nas pontas. Títulos repetidos são permitidos. |
| RN-02 | Uma ação de salvar, organizar ou capturar gera no máximo uma gravação. O botão fica desabilitado do toque até o fim da operação. |
| RN-03 | O XP é concedido no momento em que a conclusão é gravada, na mesma transação, e só se ainda não existe registro de XP para aquela tarefa. Valor fixo: 10 XP. |
| RN-04 | Reabrir (inclusive por Desfazer) volta a tarefa para Pendente e limpa a data de conclusão. O XP já ganho é mantido. Concluir de novo não gera XP. |
| RN-05 | XP total = soma de todos os registros de XP. Não é gravado como contador. |
| RN-06 | Remover uma tarefa não remove o XP que ela gerou. |
| RN-07 | Tarefa atrasada = organizada, pendente e com prazo anterior a hoje. Concluída, sem prazo ou no Inbox nunca é atrasada. |
| RN-08 | Hoje considera só tarefas organizadas e com prazo (INB RF-006). Contadores do dia: Para hoje = pendentes com prazo até hoje (inclui atrasadas); Atrasadas = pendentes com prazo anterior a hoje; Concluídas = tarefas cuja conclusão ocorreu hoje, qualquer que seja o prazo. |
| RN-09 | Editar título, área, prioridade ou prazo não altera estado, data de conclusão nem XP. |
| RN-10 | Áreas fixas no MVP: Faculdade, Trabalho, Projetos e Pessoal, criadas no primeiro acesso, sem criação, renomeação ou exclusão. |
| RN-11 | Hoje é a data de calendário no fuso atual do aparelho. Prazo é data de calendário sem horário e não muda se o fuso mudar. |
| RN-12 | Ordem em Hoje: prioridade (Alta, Média, Baixa), depois prazo crescente, depois criação. Em Todas: pendentes por prazo crescente (sem prazo por último), depois prioridade; concluídas da conclusão mais recente para a mais antiga. No Inbox: da captura mais recente para a mais antiga. |
| RN-13 | Concluir, reabrir, salvar, remover, capturar, organizar e mover para o Inbox são atômicos: em caso de falha, nenhum dado muda. |
| RN-14 | Padrões de Nova tarefa: área do filtro ativo em Tarefas, senão Faculdade; prioridade Média; prazo hoje (pode ser removido). Organizar não usa padrões: mostra o que a tarefa já tem. |
| RN-15 | Captura no Inbox: só o título (RN-01). A tarefa nasce não organizada, sem área, sem prazo, com prioridade Média e a data de criação. |
| RN-16 | Organizar exige área e aceita ficar sem prazo; só a confirmação explícita (Organizar) tira a tarefa do Inbox. Escolher área ou data sem confirmar não grava nada. |
| RN-17 | Registros do Histórico = conclusões de tarefas existentes, no momento da conclusão. Reabrir ou remover a tarefa tira seu registro. |
| RN-18 | Contadores do Histórico = número de registros da RN-17 por área, em todo o período. |
| RN-19 | Busca do Histórico: o termo, sem espaços nas pontas, aparece no título, sem diferenciar maiúsculas. Termo vazio mostra tudo. |
| RN-20 | Organização e conclusão são independentes, mas só tarefa organizada pode ser concluída: não existe tarefa não organizada e concluída (INB 4.2). |
| RN-21 | Tarefa organizada sempre tem área; o prazo é opcional. Tarefa no Inbox pode não ter área nem prazo e, sem área, não entra em nenhum filtro de área. |
| RN-22 | Mover para o Inbox: só tarefa organizada e pendente; preserva área e prazo; não muda o estado de conclusão nem o XP (INB 4.5). |

### Decisão: o que acontece com o XP ao reabrir

| Opção | Comportamento | Problema |
| --- | --- | --- |
| A. Remover XP ao reabrir | Total cai 10; reconcluir devolve 10 | Feedback negativo por corrigir um toque; mais estados para testar |
| **B. Manter XP, conceder uma única vez (escolhida)** | Total não muda ao reabrir; reconcluir não soma | Um toque acidental em Concluir rende 10 XP permanentes |
| C. Manter XP e conceder a cada conclusão | Cada ciclo soma 10 | Concluir e reabrir em loop infla o XP |

A opção B foi escolhida porque é a única em que o XP nunca diminui e nunca pode ser inflado, com uma regra de uma linha. O custo (10 XP por toque acidental) é irrelevante num MVP sem ranking nem recompensas.

&#91;embedded content: estados da tarefa e efeito no XP\]

O XP só se move numa transição: a primeira passagem de Pendente para Concluída. Todas as outras deixam o total como está.

## 9. Fluxos de usuário

Dez fluxos cobrem o MVP: F1 a F8 no núcleo, F9 e F10 na extensão.

### F1. Primeiro acesso

- **Entrada:** app aberto pela primeira vez.
- **Passos:** o sistema cria as quatro áreas (RF-002) e mostra Boas-vindas; a pessoa toca em Começar e chega em Hoje vazia (RF-001, RF-017).
- **Erros:** falha ao criar áreas mostra Não foi possível iniciar o app, com Tentar de novo.
- **Resultado:** Hoje pronta para a primeira tarefa; as boas-vindas não voltam a aparecer.

### F2. Ver o dia e o progresso

- **Entrada:** abrir o app ou tocar em Hoje.
- **Passos:** o sistema calcula hoje (RN-11) e mostra cabeçalho, XP, contadores e a lista por prioridade (RF-011 a RF-013).
- **Regras:** RN-07, RN-08, RN-12.
- **Erros:** falha de leitura mostra estado de erro com Tentar de novo.
- **Resultado:** a pessoa vê a próxima tarefa no topo e quanto do dia já fez.

### F3. Criar uma tarefa

- **Entrada:** Nova tarefa em Hoje ou Tarefas.
- **Passos:** formulário abre com foco no título e padrões preenchidos; a pessoa digita o título, opcionalmente muda área, prioridade e prazo, e toca em Salvar.
- **Regras:** RN-01, RN-02, RN-14.
- **Erros:** título inválido mostra erro no campo; falha de gravação mantém o formulário aberto.
- **Resultado:** volta à tela de origem com Tarefa criada; se o prazo for até hoje, a tarefa aparece em Hoje.

### F4. Concluir uma tarefa e ganhar XP

- **Entrada:** caixa de uma pendente em Hoje ou Tarefas.
- **Passos:** a pessoa marca a caixa; o sistema grava a conclusão e, se for a primeira, o XP, na mesma transação; a mensagem mostra +10 XP e Desfazer.
- **Regras:** RN-03, RN-13.
- **Erros:** em falha, a tarefa continua Pendente e aparece Não foi possível concluir.
- **Resultado:** contadores e XP atualizados; em Hoje a tarefa sai da lista.

### F5. Editar uma tarefa

- **Entrada:** toque no corpo de uma tarefa em Hoje ou Tarefas.
- **Passos:** formulário Editar tarefa abre preenchido; a pessoa altera e salva, ou cancela (RF-006).
- **Regras:** RN-01, RN-09.
- **Resultado:** listas e contadores refletem a mudança; aparece Alterações salvas.

### F6. Reabrir uma tarefa concluída

- **Entrada:** caixa de uma tarefa em Concluídas (Tarefas) ou Desfazer na mensagem de conclusão.
- **Passos:** a pessoa desmarca a caixa ou toca em Desfazer; a tarefa volta a Pendente.
- **Regras:** RN-04. Sem confirmação: a ação é reversível e não mexe no XP.
- **Resultado:** tarefa em Pendentes e, com prazo até hoje, de volta em Hoje; XP igual.

### F7. Remover uma tarefa

- **Entrada:** toque longo numa tarefa em Hoje ou Tarefas.
- **Passos:** confirmação Remover esta tarefa; ao confirmar, a tarefa some.
- **Regras:** RN-06, RN-13, RN-17.
- **Resultado:** tarefa fora de todas as listas e do Histórico; XP igual.

### F8. Ver todas as tarefas por área

- **Entrada:** Tarefas na barra ou Todas as tarefas em Hoje.
- **Passos:** a pessoa vê Pendentes e Concluídas e toca num chip de área para filtrar.
- **Regras:** RN-10, RN-12.
- **Resultado:** lista e contagens da área escolhida; Nova tarefa já usa essa área.

### F9. Capturar e organizar (extensão)

- **Entrada:** Tarefas, aba Inbox.
- **Passos:**
    1. A pessoa digita no campo e toca em +; a tarefa aparece em Para organizar.
    2. Depois, toca em Organizar, escolhe a área e, se quiser, o prazo, e confirma em Organizar.
- **Regras:** RN-15, RN-16, RN-20, RN-21.
- **Erros:** sem área, aparece Escolha uma área; cancelar não grava nada; falha de gravação mantém o texto no campo ou o formulário aberto.
- **Resultado:** tarefa organizada, em Todas e na área escolhida; em Hoje se o prazo for até hoje.

### F10. Consultar o Histórico (extensão)

- **Entrada:** Histórico.
- **Passos:** a pessoa vê os contadores por área e a linha do tempo por dia; opcionalmente digita na busca.
- **Regras:** RN-17 a RN-19.
- **Resultado:** a pessoa encontra o que concluiu e em qual área.

## 10. Requisitos não funcionais

Os requisitos abaixo são verificáveis por inspeção ou teste manual. Metas numéricas de desempenho e a versão mínima do Android ficam como decisões pendentes (D-04, D-05).

| ID | Categoria | Requisito | Como verificar |
| --- | --- | --- | --- |
| RNF-01 | Usabilidade | Criar uma tarefa só com o título exige no máximo 3 interações a partir de Hoje (abrir, digitar, salvar) | Contagem no teste de F3; capturar no Inbox exige 2 (digitar, +) |
| RNF-02 | Usabilidade | Concluir e reabrir exigem 1 toque, sem diálogo | Teste de F4 e F6 |
| RNF-03 | Acessibilidade | Alvos de toque com pelo menos 48 × 48 dp | Inspeção com Layout Inspector |
| RNF-04 | Acessibilidade | Todo ícone sem texto tem descrição para leitor de tela; o controle de conclusão anuncia o estado ("Concluída" ou "Pendente") | Teste com TalkBack |
| RNF-05 | Acessibilidade | Texto com contraste mínimo de 4,5:1 (WCAG AA); cor nunca é o único indicador de estado ou área | Verificador de contraste; área sempre com nome |
| RNF-06 | Acessibilidade | Layout não quebra com fonte do sistema em 200% | Teste manual com fonte máxima |
| RNF-07 | Consistência visual | Cores, tipografia e espaçamentos vêm de um único tema; nenhum valor de cor escrito direto na tela | Revisão de código |
| RNF-08 | Desempenho | Listas e progresso refletem uma ação sem recarregar a tela nem travar a interface; acesso a banco fora da thread principal | Teste manual; StrictMode em debug |
| RNF-09 | Integridade | Nenhuma operação deixa estado parcial (RN-13); XP é único por tarefa por restrição do banco | Testes de RN-03 e RN-13 |
| RNF-10 | Persistência | Dados sobrevivem a fechar o app, reiniciar o aparelho e atualizar o app de versão | Testes T-P1 a T-P3 |
| RNF-11 | Compatibilidade | Funciona em retrato, em telas de celular de 360 dp de largura para cima | Teste em emulador de 360 dp e um maior |
| RNF-12 | Privacidade | O app não pede permissões, não acessa a rede e não coleta dados; tudo fica no aparelho | Manifesto sem permissão INTERNET |
| RNF-13 | Tratamento de erros | Toda falha de leitura ou gravação vira mensagem compreensível; nenhum erro técnico aparece para a pessoa e o app não fecha | Testes de erro simulado |
| RNF-14 | Manutenibilidade | Camadas separadas (seção 12); regras de XP, progresso e datas testáveis sem Android, com testes unitários | Revisão de código e execução dos testes |

## 11. UX/UI e navegação

O protótipo UP Campus+ é a referência visual do MVP: cabeçalho escuro com título e subtítulo de uma linha, conteúdo em cartões brancos sobre fundo claro, cor primária lilás e a prioridade marcada por cor na borda do cartão. Obrigatório é reproduzir estrutura, hierarquia e componentes; tons exatos e tipografia são recomendação.

### Navegação

- Barra inferior com 3 destinos: Hoje (inicial), Tarefas (abas Inbox e Todas) e Histórico. Até a extensão estar pronta, só Hoje e Tarefas, sem a aba Inbox (RF-019).
- Boas-vindas só no primeiro acesso, sem barra.
- Nova, Editar e Organizar tarefa como tela própria, sem barra, com Cancelar e Salvar (ou Organizar) no corpo.
- Hoje tem dois botões no corpo: Nova tarefa (primário) e Todas as tarefas (secundário). Tarefas tem Nova tarefa flutuante.

### Estrutura de cada tela

| Tela | Cabeçalho (título · subtítulo) | Corpo, de cima para baixo |
| --- | --- | --- |
| Boas-vindas | Organize com leveza · Gaste menos energia organizando o que precisa fazer | Cartão Planner Pessoal com ilustração e chips Área, Prioridade, Prazo, Status; botão Começar; cartão com 3 benefícios |
| Hoje | Hoje · dia da semana, data e N tarefas para hoje; XP total (D-03) | 3 contadores; Para hoje (por prioridade); botões; dica Marque para concluir · segure para remover |
| Tarefas | Tarefas · Capture no Inbox, organize por área | Abas Inbox e Todas. Todas: chips de área; Pendentes; Concluídas; botão flutuante. Inbox: campo de captura e +; Para organizar |
| Nova, editar e organizar tarefa | Nova tarefa (ou Editar tarefa) · Anote rápido e volte ao que importa; Organizar tarefa · Escolha a área e, se quiser, uma data | Cartão com título, área, prioridade, prazo, botões e Mover para o Inbox; cartão Dica |
| Histórico | Histórico · O que você concluiu, por área | 4 contadores por área; busca; linha do tempo por dia |

### Componentes reutilizáveis

| Componente | Conteúdo | Estados |
| --- | --- | --- |
| Cabeçalho de tela | Título, subtítulo, área para ação ou XP | Único estilo em todas as telas |
| Cartão de tarefa | Borda na cor da prioridade, caixa, título, área · prioridade · prazo (ou Sem prazo), selo | Pendente; Atrasada (selo ATRASADA); Concluída (caixa marcada, título riscado); No Inbox (selo INBOX, borda neutra, ícone no lugar da caixa) |
| Contador | Número e rótulo | Cor por significado (Atrasadas em vermelho, Concluídas em verde) |
| Chip de filtro | Rótulo e contagem opcional | Normal; Selecionado |
| Seletor segmentado | Opções lado a lado (área, prioridade) | Normal; Selecionado; Desabilitado |
| Campo de texto | Ícone, rótulo, valor, erro | Normal; Foco; Erro; Desabilitado |
| Campo de data | Data formatada (ou Sem prazo), Alterar ou Definir e Remover prazo | Normal; Abre o seletor de data; Sem prazo |
| Botão primário e secundário | Rótulo | Habilitado; Pressionado; Carregando; Desabilitado |
| Cartão do Inbox | Título; Capturada: hora (e área e prazo preservados, se houver); Organizar e lixeira | Único |
| Abas | Inbox (com contagem) e Todas | Normal; Selecionada |
| Item da linha do tempo | Marcador, Concluiu e título, área · hora | Conclusão |
| Snackbar, diálogo, estado vazio | Mensagem e ação | Sucesso; Erro; Confirmação de remoção e descarte |

### Diretrizes

| Tema | Obrigatório | Recomendado |
| --- | --- | --- |
| Base | Material 3 via Compose; tema único com tokens de cor, tipo e forma | Tons do protótipo: azul-marinho no cabeçalho, lilás como primária |
| Prioridade | Alta, Média e Baixa em cores distintas e sempre com o texto | Vermelho, âmbar e verde, como no protótipo |
| Espaçamento | Escala única (múltiplos de 4 dp) | Margem lateral de 16 dp; 12 dp entre cartões |
| Tipografia | Escala do Material 3, respeitando a fonte do sistema | Títulos de cabeçalho em Headline; cartões em Title e Body |
| Ícones | Um conjunto vetorial (Material Symbols), não emoji | Contorno na barra, preenchido no item ativo |
| Feedback | Snackbar para sucesso e erro; erro de campo no próprio campo | +10 XP e Desfazer na mesma mensagem |
| Tamanhos de tela | Uma coluna, rolagem vertical; contadores quebram em 2 linhas se faltar largura | Testar em 360 dp e em um aparelho grande |
| Tema escuro | Não obrigatório | Tokens do Material 3 para habilitar sem retrabalho |

O selo PLANO B ou PLANO C do protótipo é marcação de escopo e não aparece no app.

## 12. Arquitetura e restrições técnicas

App Android nativo em Kotlin e Jetpack Compose, módulo único, três camadas (interface, domínio, dados) e Room como banco local. Sem backend, autenticação, analytics ou serviços pagos.

### Camadas

| Camada | Responsabilidade | Conteúdo |
| --- | --- | --- |
| Interface | Desenhar estados e repassar ações; nenhuma regra de negócio | Telas Compose, um ViewModel por tela expondo `StateFlow` de estado, Navigation Compose |
| Domínio | Regras de XP, contadores, atraso, ordenação, organização e Histórico, em Kotlin puro e testável sem Android | Casos de uso só onde há regra: `CompleteTask`, `ReopenTask`, `GetTodaySummary`, `GetHistory`; um `Clock` injetável |
| Dados | Persistir e consultar; garantir atomicidade | Room (entidades, DAOs, transações), repositórios de áreas e de tarefas (com XP e organização); DataStore Preferences só para o indicador de boas-vindas vistas |

Injeção de dependências manual (um `AppContainer` na `Application`). Hilt é aceitável, mas não necessário para o tamanho do projeto (D-06).

### Room ou DataStore

| Critério | Room | DataStore |
| --- | --- | --- |
| Consultas por data, estado e área | Sim, com SQL e índices | Não; tudo em memória |
| Transação entre tarefa e XP (RN-03, RN-13) | Sim | Não entre dois arquivos |
| Restrição de unicidade do XP | Sim, índice único | Não |
| Observação reativa | `Flow` nativo nos DAOs | `Flow` do arquivo inteiro |

**Escolha: Room.** As regras centrais dependem de transação e unicidade, que o DataStore não oferece. O DataStore Preferences entra só para guardar se as boas-vindas já foram vistas, um valor isolado que não participa de transações nem consultas.

### Atualização dos dados

Os DAOs expõem `Flow`. Os ViewModels combinam esses fluxos com o fluxo de "hoje" e publicam o estado da tela. Qualquer gravação atualiza todas as telas abertas sem recarga manual.

### Datas e horários

- Data da tarefa: `LocalDate`, gravada como texto ISO `yyyy-MM-dd`. Comparações por texto ISO preservam a ordem cronológica.
- Momentos (criação, conclusão, ganho de XP): `Instant`, gravado em milissegundos UTC. A data local da conclusão também é gravada (`completed_on`) para o contador Concluídas não mudar se o fuso mudar.
- "Hoje" vem de um `Clock` injetável, lido ao voltar ao primeiro plano (ciclo de vida) e na virada do dia com o app aberto (RF-020). Testes trocam o relógio para simular datas.
- `java.time` nativo exige API 26; abaixo disso é preciso desugaring (D-05).

### Dependências essenciais

Jetpack Compose (BOM) com Material 3, Navigation Compose, Lifecycle ViewModel Compose, DataStore Preferences, Room (runtime, ktx, compilador via KSP), Kotlin Coroutines. Testes: JUnit, kotlinx-coroutines-test, Room em memória e Compose UI Test.

Restrições: nenhuma permissão no manifesto, nenhuma biblioteca de rede, nenhum SDK de terceiros com coleta de dados.

## 13. Modelo de dados

Três tabelas bastam: `area`, `task` e `xp_entry`. O Inbox não tem tabela própria: é a consulta de `task` com `organized = 0` (INB seção 7). Estado da tarefa, contadores e Histórico são derivados; o XP é um registro por tarefa protegido por índice único.

### Entidades

| Tabela | Campo | Tipo | Regra |
| --- | --- | --- | --- |
| area | id | INTEGER, PK | Fixo: 1 a 4 |
| area | name | TEXT, não nulo | Faculdade, Trabalho, Projetos, Pessoal |
| area | sort\_order | INTEGER, não nulo | Ordem dos chips |
| task | id | INTEGER, PK, autoincremento | Room com `autoGenerate = true` (ids nunca reutilizados) |
| task | title | TEXT, não nulo | 1 a 100 caracteres, sem espaços nas pontas |
| task | area\_id | INTEGER, FK para area.id, nulo | Nulo só no Inbox (RN-21); ON DELETE RESTRICT; índice |
| task | priority | INTEGER, não nulo | 0 Alta, 1 Média, 2 Baixa; padrão 1 |
| task | due\_date | TEXT, nulo | `yyyy-MM-dd`; nulo = sem prazo; índice |
| task | organized | INTEGER, não nulo | Estado de organização: 0 = no Inbox, 1 = organizada; padrão 1 |
| task | completed\_at | INTEGER, nulo | Milissegundos UTC; nulo = Pendente |
| task | completed\_on | TEXT, nulo | Data local da conclusão, para o contador Concluídas; nulo junto com `completed_at` |
| task | created\_at, updated\_at | INTEGER, não nulo | Milissegundos UTC |
| xp\_entry | id | INTEGER, PK, autoincremento |  |
| xp\_entry | task\_id | INTEGER, não nulo, **único** | Sem FK, para sobreviver à remoção da tarefa |
| xp\_entry | amount | INTEGER, não nulo | 10 |
| xp\_entry | earned\_at | INTEGER, não nulo | Milissegundos UTC |
Relacionamentos: área 1:N tarefa; tarefa 1:0..1 registro de XP.

Fora do Room: `onboarding_done` (booleano) no DataStore Preferences.

### Persistidos e derivados

| Dado | Como se obtém |
| --- | --- |
| Estado Pendente ou Concluída | `completed_at` nulo ou não |
| Atrasada | Organizada, pendente e `due_date` anterior a hoje |
| Para hoje, Atrasadas, Concluídas | Contagens da RN-08 sobre `due_date` e `completed_on` |
| XP total | `SUM(amount)` de `xp_entry` |
| Inbox e sua contagem | Tarefas com `organized = 0` |
| Registros do Histórico | Tarefas com `completed_at`, ordenadas por momento (RN-17) |
| Contadores por área do Histórico | Contagem desses registros agrupada por área |

Nada derivado é gravado, então nada derivado fica dessincronizado. O preço é que reabrir ou remover uma tarefa apaga o registro de conclusão do Histórico; uma tabela de eventos evitaria isso, mas não é necessária para o MVP (D-07).

### Como o modelo evita inconsistências

- **XP duplicado:** concluir roda numa transação com `UPDATE task SET completed_at = ?, completed_on = ? WHERE id = ? AND completed_at IS NULL AND organized = 1` seguido de `INSERT OR IGNORE` em `xp_entry`. O índice único em `task_id` impede um segundo registro.
- **Reuso de id:** com autoincremento, uma tarefa nova nunca recebe o id de uma removida nem herda seu XP.
- **Tarefa duplicada:** títulos repetidos são legítimos; a proteção é o botão desabilitado durante a gravação (RN-02).
- **Organização pela metade:** organizar e mover para o Inbox mudam `organized` e os campos na mesma transação, com a condição no próprio `UPDATE` (`organized = 0` para organizar; `organized = 1 AND completed_at IS NULL` para mover); se o formulário for cancelado, nada é gravado.
- **Cópias:** Inbox, Todas, áreas e Hoje leem o mesmo registro de `task`; não há cópia para dessincronizar.
- **Área órfã:** áreas não são excluídas, e a FK com RESTRICT bloqueia isso no banco.

### Histórico de XP ou contador simples

| Opção | Prós | Contras |
| --- | --- | --- |
| Contador único (ex.: `total_xp` no DataStore) | Uma linha | Sem idempotência; exigiria flag na tarefa e transação entre DataStore e Room |
| **Registro por tarefa (escolhido)** | Unicidade garantida pelo banco; sobrevive à remoção | Uma tabela a mais |

### Migrações

Versão 2 do banco, com `exportSchema = true` e os esquemas 1 e 2 versionados no repositório; `fallbackToDestructiveMigration` não é permitido (RNF-10). A migração 1 → 2 (`data/db/Migrations.kt`, testada em `MigrationTest`):

- recria `task` com `organized` e com área e prazo aceitando nulo; tarefas existentes ficam organizadas, com os mesmos ids;
- transforma capturas Sem categoria, Aprendizado e Ideia em tarefas não organizadas (texto como título, até 100 caracteres; área preservada; data da captura), para nada se perder; capturas classificadas como Tarefa já tinham a tarefa e não geram outra;
- remove `inbox_item`;
- mantém o contador do autoincremento acima do maior id já usado e do maior `xp_entry.task_id`, para um id antigo nunca voltar (T-X5).

## 14. Métricas de sucesso

O MVP é avaliado por sessões de teste moderadas e testes automatizados, sem telemetria. As metas abaixo são propostas iniciais para a equipe confirmar (D-07); não vêm de referência externa.

### Avaliáveis no projeto acadêmico

| Métrica | Como medir | Meta proposta |
| --- | --- | --- |
| Conclusão dos fluxos principais | Participantes executam F3 a F8 (e F9 e F10, se a extensão estiver pronta) sem ajuda; observador registra sucesso ou falha | Cada fluxo concluído por pelo menos 4 de 5 participantes |
| Tempo para cadastrar uma tarefa | Cronômetro do toque em criar até a confirmação | Mediana até 20 s |
| Taxa de sucesso nas tarefas de teste | Tarefas concluídas sem ajuda ÷ tarefas propostas | Pelo menos 80% |
| Erros de persistência | Execução dos testes T-P1 a T-P3 e T-X1 a T-X5 | Zero falhas |
| Compreensão da interface | Após a sessão, o participante explica com as próprias palavras o que é o progresso do dia e como ganha XP | Pelo menos 4 de 5 explicam corretamente |

Sugestão de amostra: 5 participantes do público-alvo (seção 4), o que costuma bastar para revelar os problemas de usabilidade mais graves.

### Exigiriam uso real e coleta contínua (fora do MVP)

- Retenção: pessoas que continuam usando após 1 e 4 semanas.
- Frequência: dias por semana com pelo menos uma tarefa concluída.
- Efeito do XP: se a conclusão muda com e sem a recompensa (testa H3).
- Equilíbrio entre áreas: se a distribuição por área muda ao longo do tempo (testa H2).

Essas métricas dependeriam de diário de uso ou telemetria com consentimento, e não são requisito do MVP.

## 15. Testes e validação

A estratégia tem três níveis: testes unitários das regras de domínio, testes do banco com Room em memória e testes manuais dos fluxos e da usabilidade. Testes de UI automatizados com Compose são recomendados para F3 e F4, não obrigatórios.

| Nível | O que cobre | Ferramenta |
| --- | --- | --- |
| Unitário | XP, progresso, atraso, validação, "hoje" com relógio falso | JUnit + coroutines-test |
| Banco | Transações, unicidade do XP, ordenação, consultas por data | Room em memória (teste instrumentado) |
| Manual | Fluxos F1 a F9, persistência real, virada do dia, acessibilidade | Roteiro abaixo, em emulador e aparelho físico |
| Usabilidade | Métricas da seção 14 | Sessões moderadas com 5 participantes |

### Cenários críticos

| ID | Cenário | Resultado esperado | Ref. |
| --- | --- | --- | --- |
| T-O1 | Instalar, abrir, tocar em Começar, fechar e reabrir | Boas-vindas só na primeira vez; reabre em Hoje | RF-001 |
| T-X1 | Concluir tarefa nunca concluída | XP +10; um registro em `xp_entry` | RN-03 |
| T-X2 | Disparar concluir duas vezes em paralelo na mesma tarefa | Um único registro; XP +10 | RN-03 |
| T-X3 | Concluir, Desfazer, concluir | XP +10 no total; sem +10 XP na segunda vez | RN-04 |
| T-X4 | Remover tarefa concluída | XP inalterado | RN-06 |
| T-X5 | Remover a tarefa de maior id e criar outra; concluir a nova | Novo id; XP +10 | Seção 13 |
| T-H1 | Pendentes com prioridades e prazos variados, uma atrasada | Ordem e selo conforme RN-12 e RF-011 | RN-12 |
| T-H2 | Conferir contadores com o exemplo de RF-012 | 3, 1 e 2 | RN-08 |
| T-P1 | Criar, concluir, editar, capturar e organizar; forçar parada do app; reabrir | Tudo igual, inclusive XP e Inbox | RNF-10, INB RF-008 |
| T-P2 | Mesmo cenário com reinício do aparelho | Tudo igual | RNF-10 |
| T-P3 | Instalar versão nova sobre a anterior com dados | Dados preservados | RNF-10 |
| T-D1 | Relógio falso às 23:59 com pendente de hoje; avançar 2 min | Selo ATRASADA; Concluídas volta a 0 | RF-020 |
| T-D2 | Tarefa de ontem concluída hoje | Sai de Para hoje e Atrasadas; Concluídas +1 | RN-08 |
| T-D3 | Mudar o fuso do aparelho | Prazos inalterados; hoje segue o novo fuso | RN-11 |
| T-D4 | Mudar prazo de pendente de hoje para amanhã | Sai de Hoje; contadores recalculados | RF-005 |
| T-F1 | Salvar com título vazio ou só espaços | Não salva; erro no campo | RF-004 |
| T-F2 | Colar 150 caracteres no título | Campo fica com 100 | RF-004 |
| T-F3 | Tocar Salvar 5 vezes rapidamente | Uma tarefa criada | RN-02 |
| T-F4 | Abrir Nova tarefa com filtro Projetos ativo | Projetos, Média e hoje pré-selecionados | RN-14 |
| T-I1 | Capturar só com o título e organizar com área e prazo de hoje | Sai do Inbox; aparece em Todas e em Hoje; um único registro | RN-15, RN-16 |
| T-I2 | Abrir Organizar, escolher área e data e cancelar | Continua no Inbox, sem área nem prazo gravados | RN-16 |
| T-I3 | Remover tarefa do Inbox | Some de todas as visões | RF-024 |
| T-I4 | Organizar sem prazo | Fica em Todas, fora de Hoje | RN-08, RN-21 |
| T-I5 | Mover tarefa organizada para o Inbox; tentar com uma concluída | Volta com área e prazo; concluída não volta; XP igual | RN-20, RN-22 |
| T-I6 | Instalar a versão 2 sobre a 1 com tarefas, XP e capturas | Tarefas iguais e organizadas; capturas no Inbox; id novo acima dos antigos | Seção 13 |
| T-R1 | Concluir, depois reabrir uma tarefa | Registro aparece e depois some do Histórico; contadores por área acompanham | RN-17, RN-18 |
| T-R2 | Buscar room no Histórico | Só registros com Room no texto | RN-19 |
| T-E1 | Telas sem dados e filtros sem resultado | Mensagens de RF-017 | RF-017 |
| T-E2 | Falha de gravação simulada ao salvar, organizar, concluir, remover e capturar | Mensagem de erro; nada muda | RF-018, RN-13 |
| T-U1 | TalkBack em F3, F4 e F7 | Controles anunciados com nome e estado; remoção acessível sem toque longo | RNF-04, RF-010 |
| T-U2 | Fonte do sistema em 200% | Sem texto cortado; contadores quebram linha | RNF-06 |

Todo critério de aceitação da seção 7 deve ter pelo menos um teste correspondente, manual ou automatizado, registrado numa planilha de rastreabilidade RF × teste.

## 16. Riscos e premissas

### Riscos

| ID | Risco | Impacto | Mitigação |
| --- | --- | --- | --- |
| R-01 | Inbox e Histórico consumirem o tempo do núcleo | Núcleo entregue incompleto | Extensão só começa com todos os Must aprovados; barra mostra só Hoje e Tarefas, sem a aba Inbox, até lá |
| R-02 | Equipe com pouca experiência em Compose ou Room | Prazo e qualidade | Começar pelo banco e pela tela Hoje; spike de 1 a 2 dias |
| R-03 | Bugs de data (fuso, virada do dia, `LocalDate` e `Instant`) | Contadores e atrasadas errados | `Clock` injetável e testes T-D desde o início |
| R-04 | Mudança de esquema apagar dados entre rodadas de teste | Perda de dados | Esquema exportado; migração explícita (seção 13) |
| R-05 | XP, ausente do protótipo, parecer deslocado ou ser cortado | Requisito original não atendido | Decidir a exibição em D-03 antes de desenhar Hoje |
| R-06 | Toque longo para remover não ser descoberto | Tarefas erradas acumuladas | Dica fixa em Hoje, como no protótipo; ação acessível no item |
| R-07 | Histórico derivado confundir quem reabre tarefas (registro some) | Percepção de perda | Registrar nas sessões; tabela de eventos se necessário (D-07) |
| R-08 | Acessibilidade deixada para o fim | Retrabalho em componentes | Componentes reutilizáveis já com descrições e alvos de 48 dp |

### Premissas

- PR-01: um único usuário por aparelho, sem conta.
- PR-02: português brasileiro como único idioma; textos em `strings.xml`.
- PR-03: só orientação retrato em celulares; tablets não são alvo.
- PR-04: a equipe tem Android Studio e pelo menos um aparelho físico.
- PR-05: dados de teste podem ser apagados entre entregas, não dentro de uma rodada de testes com usuários.
- PR-06: o protótipo UP Campus+ define telas, estrutura e componentes; os selos PLANO C e PLANO B marcam núcleo e extensão (D-01).
- PR-07: volume de dados pequeno (centenas de tarefas), sem paginação.
- PR-08: os números e textos de exemplo do protótipo são ilustrativos, não dados iniciais do app.

## 17. Decisões pendentes

São 11 decisões. O protótipo resolveu três da versão 0.1: prazo sempre preenchido (padrão hoje), áreas fixas e atrasadas visíveis em Hoje. A ordem de prioridade das que faltam está no fim da seção 18.

| ID | Questão | Recomendação | Justificativa | Impacto se adiada | Quando decidir |
| --- | --- | --- | --- | --- | --- |
| D-01 | O que significam os selos PLANO C e PLANO B | PLANO C = núcleo (Must); PLANO B = extensão (Should), feita depois do núcleo | As 4 telas com PLANO C formam um app completo sozinhas; Inbox e Histórico dependem delas | Escopo e cronograma indefinidos; risco de atrasar o núcleo | Antes de começar |
| D-02 | Nome do produto: Clarity ou UP Campus+ | Um único nome em app, ícone e documentação; este PRD usa Clarity como nome do projeto | Dois nomes confundem avaliação e testes | Retrabalho em textos e recursos | Antes de começar |
| D-03 | Como mostrar o XP, ausente do protótipo | Total discreto no cabeçalho de Hoje e +10 XP na mensagem de conclusão | Cumpre o requisito com o mínimo de interface nova | Hoje teria de ser redesenhada depois | Antes de desenhar Hoje |
| D-04 | Versão mínima do Android (minSdk) | API 26 (Android 8.0) | `java.time` nativo sem desugaring; Compose suporta a partir da API 21 | Pode exigir desugaring ou retestar | Antes de começar |
| D-05 | Injeção de dependências | Manual, com `AppContainer` | Poucas dependências; menos configuração | Troca por Hilt é refatoração localizada | Antes de começar |
| D-06 | Metas das métricas de usabilidade | Aceitar as propostas da seção 14 | Precisam existir antes das sessões | Teste sem critério de aprovação | Antes das sessões |
| D-07 | Histórico derivado ou tabela de eventos | Derivado (RN-17) | Uma tabela e uma regra a menos | Reabrir ou remover apaga o registro de conclusão | Antes da extensão |
| D-08 | Editar e reclassificar capturas | Superada na 0.4: a captura é uma tarefa e se edita no formulário Organizar | — | — | — |
| D-09 | Metas numéricas de desempenho | Não fixar; usar RNF-08 | Volume pequeno (PR-07); números sem medição seriam inventados | Nenhum no MVP | Se surgir lentidão |
| D-10 | Lembretes ou notificações | Fora do MVP | Exigem permissão (Android 13+), agendamento e testes extras | Nenhum no MVP | Pós-MVP |
| D-11 | Arquivar concluídas antigas | Não arquivar; Concluídas fica abaixo de Pendentes | Volume pequeno | Lista longa após meses de uso | Pós-MVP |

### Situação na implementação (20/10/2026)

Todas as recomendações acima foram adotadas no código. As marcadas "a confirmar" seguem a recomendação, mas a decisão é da equipe.

| ID | Situação | Onde aparece no código |
| --- | --- | --- |
| D-01 | Adotada, a confirmar. Núcleo e extensão implementados; a barra mostra Hoje, Tarefas e Histórico, e Tarefas tem a aba Inbox | `EXTENSION_ENABLED` em `ui/ClarityApp.kt` |
| D-02 | Adotada, a confirmar. O app se chama Clarity (`app_name`); o pacote continua `com.example.upcampusplus` para não invalidar instalações de teste | `res/values/strings.xml` |
| D-03 | Adotada, a confirmar. Estrela e "N XP" no cabeçalho de Hoje; "Tarefa concluída · +10 XP" com Desfazer | `ui/today/TodayScreen.kt`, `ui/common/TaskActions.kt` |
| D-04 | Adotada: minSdk 26 | `app/build.gradle.kts` |
| D-05 | Adotada: `AppContainer` manual | `AppContainer.kt` |
| D-06 | Em aberto: precisa ser fechada antes das sessões | — |
| D-07 | Adotada: Histórico derivado, sem tabela de eventos | `domain/History.kt` |
| D-08 | Superada pelo PRD do Inbox | — |
| D-10, D-11 | Adotadas: fora do MVP, nada implementado | — |
| D-09 | Adotada: sem metas numéricas; acesso a banco fora da thread principal e StrictMode em debug | `ClarityApplication.kt` |

Outras escolhas feitas na implementação, sem decisão pendente:

- Backup em nuvem do Android desligado (`allowBackup="false"` e regras de extração), coerente com RNF-12 ("tudo fica no aparelho").
- A data de hoje é relida a cada 30 s enquanto uma tela está visível e ao voltar ao primeiro plano (RF-020).
- Com fonte em 200%, o rótulo da barra inferior cresce no máximo 1,3x, para "Histórico" não quebrar no meio da palavra; o restante da interface acompanha a fonte do sistema.

### Inbox como estado da tarefa (versão 0.4)

O PRD do Inbox (`app/PRD-inbox.md`) substituiu o Inbox de capturas com categorias (Tarefa, Aprendizado, Ideia). O texto foi recebido interrompido na seção 8. O que ele não define, ou define de forma ambígua, foi decidido assim; a equipe pode rever:

| Lacuna ou conflito | Decisão | Por quê |
| --- | --- | --- |
| Organizar exige área? (INB 4.3 lista área e data opcional; INB 4.4 diz "quando houver área") | Sim: organizar exige área; o prazo é opcional (RN-16, RN-21) | Histórico, filtro e contadores por área não precisam de um caso "organizada sem área" |
| Tarefa no Inbox pode ser concluída? | Não (RN-20) | INB 4.2 lista só organizada e pendente, organizada e concluída, não organizada e pendente |
| Mover para o Inbox uma tarefa concluída | Não permitido; a ação só aparece para organizada e pendente (RN-22) | Mover não pode mudar a conclusão (INB 4.5) nem criar "não organizada e concluída" |
| Mover para o Inbox: limpar ou preservar área e data | Preservar (recomendação de INB 4.5); o formulário grava o que estiver nele | A pessoa revisa na próxima organização |
| Data no Inbox conta para Hoje ou atraso? | Não: Hoje e ATRASADA só para organizadas | A data só vale depois de confirmada |
| Tarefa no Inbox com área preservada aparece no filtro da área? | Sim, com o selo INBOX; sem área, só em Todas as áreas | INB RF-005: com área, aparece na área; sem área, não é atribuída artificialmente |
| Excluir ou Remover | Remover, a mesma palavra do resto do app, com a confirmação "Ela sai do Inbox e de todas as listas" | Um nome por ação; a confirmação identifica a ação (INB 4.6) |
| Limite do título capturado | 100 caracteres, como qualquer título (RN-01); antes eram 280 | A captura agora é uma tarefa |
| Aprendizados e ideias já gravados | Viram tarefas no Inbox pela migração 1 → 2; a pessoa organiza ou remove | Notas estão fora dos objetivos do Inbox, mas apagar dados sem aviso violaria RNF-10 |
| "Áreas da vida" como visão | Atendida pelos chips de área da aba Todas | Áreas são fixas e sem tela própria (RN-10) |
| Aba aberta ao entrar em Tarefas | A última usada na sessão (começa em Todas); o botão Todas as tarefas de Hoje abre Todas | Mantém o atalho de Hoje coerente |
| Campo de captura durante a gravação | Fica só leitura, sem perder o foco | Permite capturar várias tarefas em sequência sem tocar no campo de novo |

## 18. Critérios de conclusão

O Clarity está pronto para entrega quando todas as condições abaixo forem verdadeiras.

- [x] Os 19 requisitos Must (seção 7) implementados; as 4 telas do núcleo navegáveis.
- [ ] Todos os critérios de aceitação dos requisitos Must verificados e registrados na planilha de rastreabilidade. *Registrados em `docs/rastreabilidade.md`; faltam o teste com TalkBack (RF-010, T-U1) e a volta ao primeiro plano depois da meia-noite (RF-020).*
- [ ] Fluxos F1 a F8 executados ponta a ponta em emulador e em pelo menos um aparelho físico. *Emulador: feito. Aparelho físico: pendente.*
- [ ] Testes T-P1 a T-P3 aprovados: nenhum dado perdido após fechar, reiniciar ou atualizar o app. *T-P1 aprovado; T-P3 coberto pelo teste automatizado da migração 1 → 2 (`MigrationTest`), falta repetir instalando por cima num aparelho com dados; T-P2 pendente.*
- [x] Testes T-X1 a T-X5 aprovados: XP consistente em todos os cenários.
- [x] Testes T-O1, T-H1, T-H2 e T-D1 a T-D4 aprovados.
- [x] Nenhum erro crítico conhecido aberto (fechamento do app, perda de dados, XP incorreto, tela inacessível).
- [x] Testes unitários das regras de domínio passando no build.
- [ ] Sessões de usabilidade realizadas e resultados comparados com as metas da seção 14, mesmo que alguma não seja atingida. *Pendente; depende de D-06.*
- [x] Este PRD atualizado com as decisões tomadas e um README explicando como compilar, rodar e testar.

Requisitos Should, incluindo Inbox e Histórico, não bloqueiam a entrega, mas a ausência deve ser registrada. Se a extensão for entregue, seus critérios de aceitação e os testes T-I e T-R também precisam passar. *Situação: os 9 requisitos Should foram implementados; T-I1 a T-I6 e T-R1 e T-R2 passam.*

### Decisões a confirmar antes do desenvolvimento, por prioridade

1. **D-01** Leitura dos selos: define escopo, cronograma e o que é obrigatório.
2. **D-03** Exibição do XP: define o cabeçalho de Hoje, a primeira tela a construir.
3. **D-02** Nome do produto: define textos, ícone e documentação.
4. **D-04** minSdk: define dependências e o tratamento de datas.
5. **D-05** Injeção de dependências: define a estrutura inicial do projeto.
6. **D-06** Metas de usabilidade: precisa estar fechada antes das sessões de teste.
7. **D-07** Histórico derivado: precisa estar fechada antes de começar a extensão.

## 19. Glossário

| Termo | Definição |
| --- | --- |
| Núcleo | Telas e requisitos marcados PLANO C no protótipo: Boas-vindas, Hoje, Tarefas, Nova e editar tarefa |
| Extensão | Telas e requisitos marcados PLANO B: Inbox e Histórico |
| Área (da vida) | Categoria fixa: Faculdade, Trabalho, Projetos ou Pessoal |
| Tarefa | Item com título, área, prioridade e prazo, Pendente ou Concluída |
| Prioridade | Alta, Média ou Baixa, escolhida pela pessoa; ordena a tela Hoje |
| Prazo | Data de calendário até a qual a tarefa deve ser feita |
| Atrasada | Pendente com prazo anterior a hoje |
| Hoje (data) | Data de calendário no fuso atual do aparelho |
| Hoje (tela) | Tela inicial com contadores e tarefas com prazo até hoje |
| Reabrir | Voltar uma tarefa concluída para Pendente |
| Remover | Apagar uma tarefa com toque longo e confirmação |
| XP | Pontos de experiência; 10 por tarefa, concedidos uma única vez |
| Registro de XP | Linha em `xp_entry` com o XP ganho por uma tarefa |
| Captura | Tarefa registrada no Inbox só com o título, para organizar depois |
| Organizar | Confirmar a área (e, se quiser, o prazo) de uma tarefa do Inbox, que passa a organizada |
| Registro do Histórico | Conclusão de tarefa, exibida na linha do tempo |
| Must / Should | Prioridade MoSCoW: obrigatório para a entrega / importante, não bloqueia |
| Snackbar | Mensagem curta e temporária na parte de baixo da tela |
| Room | Biblioteca do Android para banco SQLite com consultas verificadas em compilação |
| minSdk | Versão mínima do Android em que o app pode ser instalado |
| dp | Unidade de medida independente de densidade de tela no Android |
