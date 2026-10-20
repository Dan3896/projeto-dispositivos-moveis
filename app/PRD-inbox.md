# PRD — Inbox de tarefas

> **Nota de recebimento (20/10/2026):** texto recebido incompleto, interrompido na seção 8 ("A interface deve…"). As decisões tomadas para fechar as lacunas estão no `app/PRD.md`, seção "Inbox como estado da tarefa". Neste repositório os requisitos deste documento são citados como **INB RF-001** a **INB RF-008**, para não colidir com os RF do PRD principal.

**Produto:** Clarity  
**Funcionalidade:** Inbox  
**Plataforma:** Android  
**Status:** Proposta para o MVP acadêmico

## 1. Visão geral

O Inbox é uma visualização de tarefas ainda não organizadas, permitindo que o usuário registre pendências rapidamente e decida posteriormente como elas serão organizadas.

Seu objetivo é reduzir a fricção na captura de tarefas, evitando exigir que o usuário informe área, data ou outros detalhes no momento em que registra uma ideia ou obrigação.

O Inbox integra o módulo de Tarefas e utiliza o mesmo modelo de dados. Não constitui um módulo independente nem mantém cópias das tarefas.

## 2. Objetivos

- Permitir a captura rápida de tarefas.
- Separar a captura da organização.
- Facilitar a organização posterior por área e data.
- Manter uma única fonte de verdade para as tarefas.
- Integrar-se às visualizações Today, Todas as tarefas e Áreas da vida.

### Fora dos objetivos

- Captura de notas, documentos ou arquivos.
- Processamento de tarefas por inteligência artificial.
- Captura por voz.
- Integração com e-mail, Telegram ou WhatsApp.
- Compartilhamento ou colaboração.
- Priorização automática.
- Sistemas avançados de planejamento.

O MVP considera o Inbox exclusivamente uma caixa de entrada de tarefas.

## 3. Posicionamento na navegação

O Inbox deve ser uma aba ou filtro dentro da tela Tarefas.

A área Tarefas deve oferecer, no mínimo, as seguintes visualizações:

- Inbox: tarefas que ainda precisam ser organizadas.
- Todas: tarefas cadastradas, independentemente do estado de organização.

Today permanece como uma visualização separada, voltada às tarefas previstas para o dia.

Áreas da vida permite consultar tarefas por categoria.

Essas visualizações devem consultar as mesmas entidades e não gerar registros duplicados.

## 4. Conceitos e regras de negócio

### 4.1. Captura

Ao criar uma tarefa pelo Inbox, o usuário deve informar apenas o título.

O sistema registra automaticamente a data de criação e define a tarefa como não organizada.

A área e a data de execução não são obrigatórias durante a captura.

### 4.2. Estado de organização

Cada tarefa deve possuir um estado de organização independente de seu estado de conclusão.

Estados sugeridos:

- Não organizada: tarefa presente no Inbox.
- Organizada: tarefa com destino definido e fora do Inbox.

Uma tarefa pode estar organizada e pendente, organizada e concluída ou não organizada e pendente.

A conclusão de uma tarefa não deve ser confundida com sua organização.

### 4.3. Organização de uma tarefa

O usuário pode abrir uma tarefa do Inbox e escolher:

- Área da vida.
- Data de execução, opcionalmente.

A confirmação da organização altera o estado da tarefa para organizada.

Uma tarefa pode ser organizada sem data de execução, desde que o usuário confirme explicitamente sua organização.

A atribuição de uma área ou de uma data, isoladamente, não deve remover automaticamente a tarefa do Inbox antes da confirmação.

### 4.4. Exibição após organização

Após a organização:

- A tarefa deixa de aparecer no Inbox.
- Continua aparecendo em Todas as tarefas.
- Aparece na área da vida correspondente, quando houver área definida.
- Aparece no Today quando a data de execução corresponder ao dia atual e a tarefa atender aos critérios de exibição dessa tela.

Tarefas organizadas sem data não devem ser incluídas automaticamente no Today.

### 4.5. Reabertura da organização

O MVP pode permitir que o usuário envie uma tarefa organizada de volta ao Inbox.

Essa ação deve limpar ou preservar a área e a data conforme uma decisão explícita de produto. Recomendação: preservar os dados já preenchidos e permitir que o usuário os revise durante a próxima organização.

O estado de conclusão e o XP não devem ser alterados apenas por essa ação.

### 4.6. Exclusão

O usuário pode excluir uma tarefa do Inbox mediante uma ação explícita.

A exclusão remove a tarefa de todas as visualizações, pois todas compartilham o mesmo registro.

Se houver confirmação de exclusão, ela deve identificar claramente a ação. Não deve haver exclusão automática por tempo de permanência no Inbox.

## 5. Requisitos funcionais

### RF-001 — Visualizar Inbox

O sistema deve exibir todas as tarefas não organizadas e não excluídas.

**Critérios de aceitação:**
- Dada uma tarefa não organizada, quando o usuário abrir o Inbox, ela deve aparecer na lista.
- Dada uma tarefa organizada, ela não deve aparecer no Inbox.
- A lista deve atualizar após alterações de estado.

### RF-002 — Capturar tarefa rapidamente

O sistema deve permitir criar uma tarefa informando apenas o título.

**Critérios de aceitação:**
- O título não pode ser vazio ou conter apenas espaços.
- Após a criação, a tarefa deve aparecer no Inbox.
- O sistema deve registrar a data de criação.
- A criação não deve exigir área ou data de execução.
- O usuário deve receber feedback de sucesso ou erro.

### RF-003 — Organizar tarefa

O sistema deve permitir alterar o estado de organização de uma tarefa do Inbox.

**Critérios de aceitação:**
- O usuário pode selecionar uma área existente.
- O usuário pode definir uma data de execução ou deixá-la em branco.
- O sistema deve apresentar uma ação explícita de confirmação.
- Após a confirmação, a tarefa deve deixar o Inbox.
- A tarefa deve continuar acessível em Todas as tarefas.

### RF-004 — Filtrar tarefas organizadas

O sistema deve disponibilizar a visualização Todas as tarefas.

**Critérios de aceitação:**
- As tarefas organizadas e não organizadas devem aparecer nessa visualização, respeitando os filtros aplicados.
- Cada tarefa deve aparecer uma única vez.
- A alteração da organização deve refletir-se sem criar outro registro.

### RF-005 — Integrar tarefas às áreas da vida

O sistema deve disponibilizar tarefas organizadas por área.

**Critérios de aceitação:**
- Uma tarefa com área definida deve aparecer na área correspondente.
- Uma tarefa sem área não deve ser atribuída artificialmente a uma categoria.
- A remoção ou alteração de uma área deve respeitar as regras de integridade do modelo de dados.

### RF-006 — Integrar com Today

O sistema deve disponibilizar no Today as tarefas que atendam aos critérios de planejamento diário.

**Critérios de aceitação:**
- Uma tarefa organizada com data de execução igual ao dia atual deve aparecer no Today, se estiver dentro dos critérios gerais dessa tela.
- Uma tarefa sem data não deve aparecer automaticamente no Today.
- A mudança de data deve atualizar sua exibição nas visualizações temporais.

### RF-007 — Excluir tarefa

O sistema deve permitir excluir uma tarefa do Inbox.

**Critérios de aceitação:**
- A tarefa excluída deixa de aparecer em todas as visualizações.
- A exclusão não pode criar registros órfãos ou inconsistências.
- A ação deve ser explícita.

### RF-008 — Persistir dados localmente

O sistema deve preservar as tarefas e seus estados de organização após o encerramento do aplicativo.

**Critérios de aceitação:**
- Após reiniciar o aplicativo, as tarefas devem manter seus dados.
- Uma tarefa organizada não pode retornar ao Inbox indevidamente.
- Alterações confirmadas devem permanecer após a reinicialização.

## 6. Requisitos de interface

A tela Inbox deve conter:

- Título e identificação da caixa de entrada.
- Campo de captura rápida.
- Botão de adicionar ou ação equivalente.
- Lista de tarefas não organizadas.
- Acesso à ação de organizar.
- Acesso à exclusão.
- Estado vazio com orientação para capturar a primeira tarefa.

O fluxo de organização deve ser curto, preferencialmente em uma tela ou painel.

O formulário de organização deve apresentar a área da vida, a data opcional e uma ação de confirmação.

A interface deve diferenciar visualmente tarefas não organizadas de tarefas organizadas, sem excesso de elementos.

## 7. Modelo de dados

A entidade Task deve conter, no mínimo:

- id: identificador único.
- title: título da tarefa.
- createdAt: data de criação.
- organizationStatus: estado de organização.
- areaId: identificador da área, opcional conforme as regras de domínio.
- dueDate: data de execução, opcional.
- completionStatus: estado de conclusão.
- completedAt: data de conclusão, quando aplicável.

Os campos e tipos definitivos devem seguir o modelo de dados geral do Clarity.

O Inbox não deve possuir uma tabela própria de tarefas. A consulta deve selecionar os registros cujo estado de organização seja não organizada.

O estado de organização deve ser independente do estado de conclusão.

## 8. Requisitos não funcionais

- As operações de captura e organização devem funcionar sem conexão com a internet.
- Os dados devem permanecer disponíveis após reiniciar o aplicativo.
- A interface deve

*(texto interrompido aqui no original recebido)*
