# TASKS.md — Plano de Execução Operacional por Agente de IA (Remuneração Global ServiceNow)

## 1. Visão Geral e Protocolo de Execução por Agente de IA

Este documento define o **Plano de Execução Operacional (TASKS.md)** para a implementação do Módulo de Remuneração Global na plataforma ServiceNow, sob a metodologia **Spec Driven Development (SDD)**.

Cada tarefa listada neste documento foi desenhada para ser **atômica, sequencial e objetivamente verificável**, permitindo que um Agente de IA ou desenvolvedor execute a implementação com alta taxa de sucesso em modo *single-shot* ou *few-shot*, sem estourar o limite da janela de contexto ou introduzir ambiguidades.

### 1.1 Estrutura Padrão de Especificação da Tarefa
Todas as tarefas seguem rigorosamente a estrutura abaixo:
* **ID da Tarefa**: Identificador único sequencial (`TASK-XXX`).
* **História / Referência**: Código da User Story (`STRY0011XXX`) e Requisitos do MVP associados.
* **Update Set Alvo**: Nome exato do Update Set onde as alterações devem ser capturadas.
* **Objetivo**: Descrição sucinta do resultado final esperado.
* **Pré-condições & Dependências**: Tarefas que devem ser concluídas previamente.
* **Passos de Execução**: Instruções técnicas exatas (tabelas, campos, tipos, scripts e ACLs).
* **Critério de Aceite / Verificação (Definition of Done)**: Comando, script ou condição de teste para validar a conclusão da tarefa.

---

## 2. Fase 1: Fundação da Aplicação, Grupos de Acesso e Cadastros Base

### TASK-001: Criar Scoped Application `x_mgi_rmg` e Workspace Base
* **História / Referência**: `STRY0011809` | RNF-04
* **Update Set Alvo**: `RMG_v1.0_STRY0011809_Workspace_ScopedApp`
* **Objetivo**: Criar o escopo da aplicação no ServiceNow e inicializar a estrutura do Workspace customizado.
* **Pré-condições & Dependências**: Nenhuma (Primeira tarefa).
* **Passos de Execução**:
  1. No ServiceNow App Engine Studio / Custom Applications, criar a Scoped Application com o nome `Remuneração Global` e Scope `x_mgi_rmg`.
  2. Configurar a permissão de acesso Cross-Scope para leitura e escrita na tabela `sys_user` e `core_company`.
  3. Criar a especificação do Configurable Workspace com nome `Remuneração Global Workspace` e rota `/now/rmg/workspace`.
  4. Configurar a experiência de navegação lateral (Chrome Toolbar) habilitando o menu principal de solicitações.
* **Critério de Aceite / Verificação**:
  * O escopo `x_mgi_rmg` existe e está ativo na tabela `sys_app`.
  * A rota `/now/rmg/workspace` responde com HTTP 200 para usuários autorizados.

---

### TASK-002: Criar Grupos de Acesso e Funções do Módulo RMG
* **História / Referência**: `STRY0011811` | RF-01, RF-02, RNF-02
* **Update Set Alvo**: `RMG_v1.0_STRY0011811_Access_Groups`
* **Objetivo**: Criar os 4 grupos de acesso e suas respectivas roles na tabela `sys_user_group`.
* **Pré-condições & Dependências**: `TASK-001` concluída.
* **Passos de Execução**:
  1. Criar as seguintes Roles no escopo `x_mgi_rmg`: `x_mgi_rmg.submitter`, `x_mgi_rmg.technical_analyst`, `x_mgi_rmg.manager`, `x_mgi_rmg.admin`.
  2. Criar os grupos de acesso na tabela `sys_user_group`:
     * `RMG_Portal_Submitters` -> Vincular a role `x_mgi_rmg.submitter`.
     * `RMG_Technical_Staff` -> Vincular a role `x_mgi_rmg.technical_analyst`.
     * `RMG_Managers` -> Vincular a role `x_mgi_rmg.manager`.
     * `RMG_Admins` -> Vincular a role `x_mgi_rmg.admin`.
  3. Configurar a descrição funcional de cada grupo registrando os níveis de visibilidade corporativa e aprovação.
* **Critério de Aceite / Verificação**:
  * Executar a consulta: `sys_user_group.select(name STARTSWITH RMG_)` e retornar exatamente 4 registros ativos com roles associadas.

---

### TASK-003: Criar Tipos de Casos Estruturais (Case Types)
* **História / Referência**: `STRY0011810` | RF-03, RF-04
* **Update Set Alvo**: `RMG_v1.0_STRY0011810_Case_Types`
* **Objetivo**: Criar a hierarquia de Case Types estendendo a tabela `sn_customerservice_case`.
* **Pré-condições & Dependências**: `TASK-001` concluída.
* **Passos de Execução**:
  1. Na tabela `sys_db_object`, criar a extensão da tabela `sn_customerservice_case` com os seguintes prefixos e nomes:
     * `sn_customerservice_case_rmg` (Prefix: `RMG`, Label: `Caso Remuneração Global Controlada`).
     * `sn_customerservice_case_rbc` (Prefix: `RBC`, Label: `Caso Remuneração Benefício`).
     * `sn_customerservice_case_rmgs` (Prefix: `RMGS`, Label: `Caso Remuneração Global Subsidiária`).
  2. Definir a numeração sequencial automática (Auto-numbering) no campo `number` para cada prefixo (ex: `RMG0010001`, `RBC0010001`, `RMGS0010001`).
  3. Garantir a herança de campos padrão da tabela pai (`short_description`, `assignment_group`, `assigned_to`, `state`).
* **Critério de Aceite / Verificação**:
  * Registro de teste criado para cada Case Type gera o número sequencial correto conforme o prefixo configurado.

---

### TASK-004: Criar Tabela Custom de Instâncias de Gestão (Órgãos Estatutários)
* **História / Referência**: `STRY0011852` | RF-05, RF-06
* **Update Set Alvo**: `RMG_v1.0_STRY0011852_Instancias_Gestao_Table`
* **Objetivo**: Criar a tabela `u_rmg_instancia_gestao` para cadastro dos órgãos estatutários (Conselhos e Diretoria).
* **Pré-condições & Dependências**: `TASK-001` concluída.
* **Passos de Execução**:
  1. Criar a tabela `u_rmg_instancia_gestao` no escopo `x_mgi_rmg`.
  2. Adicionar os campos:
     * `u_codigo` (String 20, Mandatory, Unique).
     * `u_nome` (String 100, Mandatory).
     * `u_descricao` (String 255, Optional).
     * `u_ativo` (True/False, Default: true).
  3. Adicionar Índice de Banco de Dados (`sys_index`) no campo `u_codigo`.
  4. Configurar Business Rule `before insert/update` para verificar unicidade do campo `u_codigo` antes de salvar.
* **Critério de Aceite / Verificação**:
  * Tentar inserir dois registros com o mesmo `u_codigo` resulta em mensagem de erro e bloqueio da operação.

---

### TASK-005: Criar Tabela Custom de Cargos de Executivos
* **História / Referência**: `STRY0011853` | RF-07, RF-08
* **Update Set Alvo**: `RMG_v1.0_STRY0011853_Cargos_Table`
* **Objetivo**: Criar a tabela `u_rmg_cargo` para padronização dos cargos de executivos e conselheiros.
* **Pré-condições & Dependências**: `TASK-001` e `TASK-004` concluídas.
* **Passos de Execução**:
  1. Criar a tabela `u_rmg_cargo` no escopo `x_mgi_rmg`.
  2. Adicionar os campos:
     * `u_codigo` (String 20, Mandatory, Unique).
     * `u_nome` (String 100, Mandatory).
     * `u_instancia_gestao` (Reference -> `u_rmg_instancia_gestao`, Mandatory).
     * `u_ativo` (True/False, Default: true).
  3. Configurar Business Rule impedindo a exclusão física de registros (`current.setAbortAction(true)` se operação for `delete`), orientando o uso do campo `u_ativo = false`.
* **Critério de Aceite / Verificação**:
  * Operação de exclusão direta via formulário ou script lança exceção e mantém o registro no banco.

---

## 3. Fase 2: Arquitetura de Dados Principal e Auditoria Nativa

### TASK-006: Criar Tabela Custom de Casos de Remuneração Global `u_rmg_case`
* **História / Referência**: `STRY0011858` | RF-09 a RF-12
* **Update Set Alvo**: `RMG_v1.0_STRY0011858_Case_RMG_Table`
* **Objetivo**: Criar a tabela principal de formulário de proposta de empresas controladas.
* **Pré-condições & Dependências**: `TASK-003` concluída.
* **Passos de Execução**:
  1. Criar a tabela `u_rmg_case` estendendo `sn_customerservice_case_rmg`.
  2. Adicionar os campos:
     * `u_empresa` (Reference -> `core_company`, Mandatory, Read-only na submissão).
     * `u_exercicio` (Integer, Mandatory, Default: ano corrente).
     * `u_tipo_assembleia` (Choice: `AGO`, `AGE`, Mandatory).
     * `u_data_assembleia` (Date, Mandatory).
     * `u_valor_total_vigente` (Currency, Read-only, Calculado).
     * `u_valor_total_proposto` (Currency, Read-only, Calculado).
     * `u_variacao_percentual_total` (Decimal, Read-only, Calculado).
     * `u_justificativa` (String 1000, Conditional Mandatory).
  3. Configurar Form Layout organizando os campos em seções: "Dados Gerais", "Informações da Assembleia" e "Resumo Financeiro Totalizador".
* **Critério de Aceite / Verificação**:
  * O formulário da tabela `u_rmg_case` renderiza corretamente com todos os campos e seções no Workspace.

---

### TASK-007: Criar Tabela Custom de Casos de Subsidiárias `u_rmgs_case`
* **História / Referência**: `STRY0011859` | RF-13 a RF-15
* **Update Set Alvo**: `RMG_v1.0_STRY0011859_Case_RMGS_Table`
* **Objetivo**: Criar a tabela de formulário de proposta de empresas subsidiárias.
* **Pré-condições & Dependências**: `TASK-003` e `TASK-006` concluídas.
* **Passos de Execução**:
  1. Criar a tabela `u_rmgs_case` estendendo `sn_customerservice_case_rmgs`.
  2. Adicionar os campos:
     * `u_empresa` (Reference -> `core_company`, Mandatory).
     * `u_empresa_controladora` (Reference -> `core_company`, Read-only).
     * `u_exercicio` (Integer, Mandatory).
     * `u_tipo_assembleia` (Choice: `AGO`, `AGE`).
     * `u_valor_total_proposto` (Currency, Read-only, Calculado).
  3. Configurar Client Script `onLoad` para auto-preencher `u_empresa_controladora` com base no cadastro da empresa subsidiária.
* **Critério de Aceite / Verificação**:
  * Ao selecionar uma empresa subsidiária, o campo `u_empresa_controladora` é preenchido automaticamente.

---

### TASK-008: Criar Tabela Custom de Proposta Enviada `u_rmg_proposta_enviada`
* **História / Referência**: `STRY0011860` | RF-16 a RF-20
* **Update Set Alvo**: `RMG_v1.0_STRY0011860_Proposta_Enviada_Table`
* **Objetivo**: Criar a tabela custom para armazenamento estático das propostas submetidas pelas empresas.
* **Pré-condições & Dependências**: `TASK-006` e `TASK-007` concluídas.
* **Passos de Execução**:
  1. Criar a tabela `u_rmg_proposta_enviada` no escopo `x_mgi_rmg`.
  2. Adicionar os campos:
     * `u_case_rmg` (Reference -> `u_rmg_case`).
     * `u_empresa` (Reference -> `core_company`).
     * `u_instancia_gestao` (Reference -> `u_rmg_instancia_gestao`).
     * `u_cargo` (Reference -> `u_rmg_cargo`).
     * `u_quantidade_membros` (Integer).
     * `u_valor_vigente` (Currency).
     * `u_valor_proposto` (Currency).
     * `u_variacao_percentual` (Decimal).
     * `u_data_envio` (GlideDateTime, Default: javascript:gs.nowDateTime()).
  3. Definir todas as ACLs de escrita para `false` após a criação, tornando a tabela um repositório imutável para a empresa.
* **Critério de Aceite / Verificação**:
  * Registros gravados nesta tabela não aceitam alterações via formulário por nenhum perfil de empresa.

---

### TASK-009: Criar Tabela Custom de Histórico `u_rmg_historico`
* **História / Referência**: `STRY0011861` | RF-21 a RF-25
* **Update Set Alvo**: `RMG_v1.0_STRY0011861_Historico_Table`
* **Objetivo**: Criar a tabela de histórico de aprovações e carga inicial de exercícios anteriores.
* **Pré-condições & Dependências**: `TASK-008` concluídas.
* **Passos de Execução**:
  1. Criar a tabela `u_rmg_historico` no escopo `x_mgi_rmg`.
  2. Adicionar os campos:
     * `u_empresa` (Reference -> `core_company`, Indexado).
     * `u_exercicio` (Integer, Indexado).
     * `u_instancia_gestao` (Reference -> `u_rmg_instancia_gestao`).
     * `u_cargo` (Reference -> `u_rmg_cargo`).
     * `u_valor_aprovado` (Currency).
     * `u_numero_portaria` (String 50).
     * `u_data_publicacao_dou` (Date).
  3. Adicionar Índice Composto (`sys_index`) nos campos `u_empresa` + `u_exercicio`.
* **Critério de Aceite / Verificação**:
  * Consultas combinando empresa e exercício executam utilizando o índice composto sem realizar table scan.

---

### TASK-010: Ativar Auditoria Nativa (`sys_audit`) nas Tabelas Custom
* **História / Referência**: `STRY0011814` | RNF-01
* **Update Set Alvo**: `RMG_v1.0_STRY0011814_Enable_Audit_Logs`
* **Objetivo**: Habilitar a auditoria nativa do ServiceNow em todas as tabelas do escopo.
* **Pré-condições & Dependências**: `TASK-004` a `TASK-009` concluídas.
* **Passos de Execução**:
  1. No Dicionário de Dados (`sys_dictionary`), alterar a propriedade `audit = true` para o registro raiz das tabelas:
     * `u_rmg_case`
     * `u_rmgs_case`
     * `u_rmg_proposta_enviada`
     * `u_rmg_historico`
     * `u_rmg_instancia_gestao`
     * `u_rmg_cargo`
  2. Adicionar o Related List `Audit History` (Histórico de Alterações) nos formulários de administração de cada tabela.
* **Critério de Aceite / Verificação**:
  * Qualquer alteração de valor em um campo das tabelas auditadas gera uma entrada correspondente na tabela `sys_audit`.

---

## 4. Fase 3: Estrutura de Tarefas (Case Tasks) e Linhas de Rubrica

### TASK-011: Criar Task Types Estruturais (`RMGTASK` e `RMGSTASK`)
* **História / Referência**: `STRY0011850` | RF-26 a RF-28
* **Update Set Alvo**: `RMG_v1.0_STRY0011850_Task_Types`
* **Objetivo**: Criar os dois tipos de tarefas derivadas de `sn_customerservice_task`.
* **Pré-condições & Dependências**: `TASK-003` concluída.
* **Passos de Execução**:
  1. Na tabela `sys_db_object`, criar as extensões de `sn_customerservice_task`:
     * `sn_customerservice_task_rmgtask` (Prefix: `RMGTASK`, Label: `Tarefa Remuneração Global Controlada`).
     * `sn_customerservice_task_rmgstask` (Prefix: `RMGSTASK`, Label: `Tarefa Remuneração Global Subsidiária`).
  2. Configurar a numeração sequencial para cada prefixo (ex: `RMGTASK0010001` e `RMGSTASK0010001`).
* **Critério de Aceite / Verificação**:
  * As tarefas criadas herdam os campos padrão de `sn_customerservice_task` e geram números com os prefixos corretos.

---

### TASK-012: Criar Tabela Custom de Linhas de Proposta `u_rmgtask`
* **História / Referência**: `STRY0011863` | RF-29 a RF-32
* **Update Set Alvo**: `RMG_v1.0_STRY0011863_RMG_Task_Table`
* **Objetivo**: Criar a tabela `u_rmgtask` para armazenar as linhas de rubrica e cargos das empresas controladas.
* **Pré-condições & Dependências**: `TASK-011` concluída.
* **Passos de Execução**:
  1. Criar a tabela `u_rmgtask` estendendo `sn_customerservice_task_rmgtask`.
  2. Adicionar os campos:
     * `parent` (Reference -> `u_rmg_case`, Mandatory).
     * `u_empresa` (Reference -> `core_company`, Mandatory).
     * `u_instancia_gestao` (Reference -> `u_rmg_instancia_gestao`, Mandatory).
     * `u_cargo` (Reference -> `u_rmg_cargo`, Mandatory).
     * `u_quantidade_membros` (Integer, Default: 1).
     * `u_honorario_mensal_vigente` (Currency).
     * `u_honorario_mensal_proposto` (Currency).
     * `u_beneficio_mensal_vigente` (Currency).
     * `u_beneficio_mensal_proposto` (Currency).
     * `u_total_mensal_vigente` (Currency, Calculado).
     * `u_total_mensal_proposto` (Currency, Calculado).
     * `u_variacao_percentual` (Decimal, Calculado).
* **Critério de Aceite / Verificação**:
  * A tabela `u_rmgtask` é exibida como Related List no formulário do caso pai `u_rmg_case`.

---

### TASK-013: Criar Tabela Custom de Linhas de Proposta Subsidiária `u_rmgstask`
* **História / Referência**: `STRY0011862` | RF-33 a RF-35
* **Update Set Alvo**: `RMG_v1.0_STRY0011862_RMGS_Task_Table`
* **Objetivo**: Criar a tabela `u_rmgstask` para armazenar as linhas de proposta das empresas subsidiárias.
* **Pré-condições & Dependências**: `TASK-011` e `TASK-012` concluídas.
* **Passos de Execução**:
  1. Criar a tabela `u_rmgstask` estendendo `sn_customerservice_task_rmgstask`.
  2. Adicionar os campos correspondentes à estrutura da `u_rmgtask`, mantendo a referência do pai para `u_rmgs_case`.
  3. Adicionar o campo `u_empresa_controladora` (Reference -> `core_company`).
* **Critério de Aceite / Verificação**:
  * A tabela `u_rmgstask` é vinculada corretamente como Related List no formulário do caso pai `u_rmgs_case`.

---

### TASK-014: Criar Client Scripts para Auto-preenchimento Contextual
* **História / Referência**: `STRY0011858`, `STRY0011862`, `STRY0011863`
* **Update Set Alvo**: `RMG_v1.0_STRY0011863_RMG_Task_Table`
* **Objetivo**: Automatizar o preenchimento da empresa e usuário em novos registros.
* **Pré-condições & Dependências**: `TASK-012` e `TASK-013` concluídas.
* **Passos de Execução**:
  1. Criar Client Script `onLoad` em `u_rmg_case` e `u_rmgs_case`:
     * Obter a empresa do usuário logado via `g_user.company` e atribuir ao campo `u_empresa` caso esteja em rascunho.
  2. Criar Client Script `onLoad` nas tabelas de tarefas `u_rmgtask` e `u_rmgstask`:
     * Copiar automaticamente a empresa do caso pai (`parent.u_empresa`) para o campo `u_empresa` da tarefa.
* **Critério de Aceite / Verificação**:
  * Ao criar uma nova tarefa a partir de um caso, o campo `u_empresa` já vem preenchido sem intervenção manual.

---

## 5. Fase 4: Ciclo de Vida, Máquinas de Estado e ACLs

### TASK-015: Configurar Máquina de Estados do Case `RMG`
* **História / Referência**: `STRY0011816` | RF-36 a RF-38
* **Update Set Alvo**: `RMG_v1.0_STRY0011816_RMG_Case_States`
* **Objetivo**: Configurar os estados e regras de transição do fluxo do caso principal `u_rmg_case`.
* **Pré-condições & Dependências**: `TASK-006` concluída.
* **Passos de Execução**:
  1. No Dicionário de Dados do campo `state` na tabela `u_rmg_case`, definir as seguintes opções de Choice:
     * `1` -> `Rascunho` (Draft)
     * `2` -> `Proposta Enviada` (Submitted)
     * `3` -> `Em Análise Técnica` (Under Review)
     * `4` -> `Devolvido para Ajustes` (Returned)
     * `5` -> `Aprovado` (Approved)
     * `7` -> `Cancelado` (Canceled)
  2. Configurar a transição padrão inicial para `1` (Rascunho).
  3. Criar Script Include Flow Handler validando as permissões de transição por perfil (ex: apenas `RMG_Portal_Submitters` transiciona de `1` para `2`).
* **Critério de Aceite / Verificação**:
  * O campo `state` exibe os 6 estados configurados e impede a transição direta de `Rascunho` para `Aprovado`.

---

### TASK-016: Configurar Máquina de Estados do Case `RMGS`
* **História / Referência**: `STRY0011817` | RF-39, RF-40
* **Update Set Alvo**: `RMG_v1.0_STRY0011817_RMGS_Case_States`
* **Objetivo**: Configurar o fluxo de estados simplificado do caso de subsidiária `u_rmgs_case`.
* **Pré-condições & Dependências**: `TASK-007` concluída.
* **Passos de Execução**:
  1. No Dicionário de Dados do campo `state` na tabela `u_rmgs_case`, definir os Choices:
     * `1` -> `Rascunho`
     * `2` -> `Proposta Enviada`
     * `5` -> `Aprovado`
     * `7` -> `Cancelado`
  2. Configurar valor padrão como `1` (Rascunho).
* **Critério de Aceite / Verificação**:
  * O formulário `u_rmgs_case` restringe os estados aos 4 valores configurados.

---

### TASK-017: Configurar Estados das Tarefas `RMGTASK` e `RMGSTASK`
* **História / Referência**: `STRY0011851` | RF-41, RF-42
* **Update Set Alvo**: `RMG_v1.0_STRY0011851_Task_States`
* **Objetivo**: Definir os estados funcionais para as linhas de proposta.
* **Pré-condições & Dependências**: `TASK-012` e `TASK-013` concluídas.
* **Passos de Execução**:
  1. Configurar as escolhas do campo `state` em `u_rmgtask` e `u_rmgstask`:
     * `1` -> `Proposta`
     * `2` -> `Editada`
     * `3` -> `Excluída`
  2. Definir o estado inicial padrão como `1` (Proposta).
* **Critério de Aceite / Verificação**:
  * Ao modificar o valor de uma tarefa existente, o sistema altera o estado de `Proposta` para `Editada`.

---

### TASK-018: Configurar ACLs e Data Policies de Bloqueio em Estados Finais
* **História / Referência**: `STRY0011816`, `STRY0011817` | RNF-02
* **Update Set Alvo**: `RMG_v1.0_STRY0011816_RMG_Case_States`
* **Objetivo**: Garantir que casos aprovados ou cancelados se tornem estritamente Read-Only.
* **Pré-condições & Dependências**: `TASK-015` e `TASK-016` concluídas.
* **Passos de Execução**:
  1. Criar Data Policy em `u_rmg_case` e `u_rmgs_case`:
     * Condição: `state IN (2, 5, 7)` (Proposta Enviada, Aprovado, Cancelado).
     * Ação: Tornar todos os campos Read-Only para todos os perfis, exceto `RMG_Admins`.
  2. Criar ACL de escrita (`write`) para as tabelas `u_rmgtask` e `u_rmgstask`:
     * Script: `answer = (parent.state == 1 || parent.state == 4);` (Permite edição apenas se o caso pai estiver em Rascunho ou Devolvido).
* **Critério de Aceite / Verificação**:
  * Usuário com perfil `RMG_Portal_Submitters` abre um caso em estado `Aprovado` e todos os botões de edição e campos estão bloqueados.

---

## 6. Fase 5: Interface de Usuário, Workspace & Landing Page

### TASK-019: Configurar Módulo Read-Only de Consulta de Empresas
* **História / Referência**: `STRY0011812` | RNF-03
* **Update Set Alvo**: `RMG_v1.0_STRY0011812_Empresas_Read_Only_List`
* **Objetivo**: Configurar a lista de empresas no Workspace bloqueando ações de criação e edição.
* **Pré-condições & Dependências**: `TASK-001` concluída.
* **Passos de Execução**:
  1. No Workspace Builder, adicionar o módulo de lista apontando para a tabela `core_company`.
  2. Aplicar List Control omitindo os botões `New`, `Edit` e desabilitando a edição inline (`Omit inline edit = true`).
  3. Criar ACL `create` e `write` na tabela `core_company` no escopo `x_mgi_rmg` avaliando como `false` para o grupo `RMG_Technical_Staff`.
* **Critério de Aceite / Verificação**:
  * Analista técnico visualiza a lista de empresas mas não possui opção de adicionar ou editar registros.

---

### TASK-020: Configurar Estrutura Navegacional do Workspace RMG
* **História / Referência**: `STRY0011809` | RNF-04
* **Update Set Alvo**: `RMG_v1.0_STRY0011809_Workspace_ScopedApp`
* **Objetivo**: Montar o menu lateral de navegação e filtros salvos para os analistas.
* **Pré-condições & Dependências**: `TASK-001` e `TASK-019` concluídas.
* **Passos de Execução**:
  1. Configurar as categorias no menu lateral do Workspace:
     * "Minhas Tarefas Pendentes"
     * "Propostas em Análise (AGO)"
     * "Propostas em Análise (AGE)"
     * "Histórico de Portarias Aprovadas"
     * "Cadastro de Empresas (Consulta)"
  2. Vincular as roles `x_mgi_rmg.technical_analyst` e `x_mgi_rmg.manager` como pré-requisitos para visualização do menu.
* **Critério de Aceite / Verificação**:
  * Ao fazer login com usuário analista, a barra lateral do Workspace exibe exatamente as 5 categorias configuradas.

---

### TASK-021: Criar Landing Page do Workspace com Widgets de Monitoramento
* **História / Referência**: `STRY0011815` | RF-43, RF-44
* **Update Set Alvo**: `RMG_v1.0_STRY0011815_Workspace_Landing_Page`
* **Objetivo**: Construir o painel inicial de métricas em tempo real para os analistas.
* **Pré-condições & Dependências**: `TASK-020` concluída.
* **Passos de Execução**:
  1. No UI Builder, abrir a Landing Page do `Remuneração Global Workspace`.
  2. Adicionar componente **Data Visualization (Single Scorecard)**:
     * Métrica: Total de Propostas Recebidas no Exercício Vigente (`u_exercicio = javascript:new GlideDateTime().getYearLocalTime()`).
  3. Adicionar componente **Donut Chart**:
     * Agrupamento de propostas por estado (`state`) e tipo de assembleia (`u_tipo_assembleia`).
  4. Adicionar componente **Simple List**:
     * Título: "Minhas Propostas Atribuidas".
     * Filtro: `assigned_to = javascript:gs.getUserID()^state NOT IN (5,7)`.
* **Critério de Aceite / Verificação**:
  * A Landing Page carrega sem erros de console e atualiza as contagens dinamicamente ao atribuir um novo caso ao analista logado.

---

## 7. Fase 6: Automações Financeiras, Business Rules e Relatórios

### TASK-022: Criar Script Include `RMGCabalanceCalculator`
* **História / Referência**: Automação Financeira MVP | RF-31, RF-34
* **Update Set Alvo**: `RMG_v1.0_FINANCE_BR_Calculos_Relatorios`
* **Objetivo**: Centralizar as regras de matemática financeira e cálculo de variações em um Script Include.
* **Pré-condições & Dependências**: `TASK-012` e `TASK-013` concluídas.
* **Passos de Execução**:
  1. No escopo `x_mgi_rmg`, criar o Script Include `RMGCabalanceCalculator` (Client callable = false).
  2. Implementar a função `calculateLineTotals(honorario, beneficio, membros)`:
     * Retornar `(honorario + beneficio) * membros`.
  3. Implementar a função `calculatePercentageVariation(valorVigente, valorProposto)`:
     * Se `valorVigente == 0`, retornar `100.00` se `valorProposto > 0` senão `0.00`.
     * Retornar `((valorProposto - valorVigente) / valorVigente) * 100`.
  4. Implementar a função `recalculateCaseHeader(caseSysId)`:
     * Somar todos os `u_total_mensal_vigente` e `u_total_mensal_proposto` das tarefas filhas ativas e atualizar os totais do caso pai.
* **Critério de Aceite / Verificação**:
  * Teste via Background Script valida os cálculos com cenários de variação positiva, negativa e divisão por zero sem exceção.

---

### TASK-023: Criar Business Rules de Recálculo Automático de Valores
* **História / Referência**: Automação Financeira MVP | RF-31, RF-34
* **Update Set Alvo**: `RMG_v1.0_FINANCE_BR_Calculos_Relatorios`
* **Objetivo**: Disparar o recálculo dos totais da linha e do cabeçalho nas operações de inserção/alteração.
* **Pré-condições & Dependências**: `TASK-022` concluída.
* **Passos de Execução**:
  1. Criar Business Rule `before insert/update` na tabela `u_rmgtask` e `u_rmgstask`:
     * Executar `RMGCabalanceCalculator.calculateLineTotals` e atualizar `u_total_mensal_vigente` e `u_total_mensal_proposto`.
     * Executar `RMGCabalanceCalculator.calculatePercentageVariation` e atualizar `u_variacao_percentual`.
  2. Criar Business Rule `after insert/update/delete` na tabela `u_rmgtask` e `u_rmgstask`:
     * Disparar `RMGCabalanceCalculator.recalculateCaseHeader(current.parent)`.
* **Critério de Aceite / Verificação**:
  * Ao alterar o honorário mensal em uma tarefa, o total da linha e a variação % são calculados e o caso pai é atualizado instantaneamente.

---

### TASK-024: Implementar Regra de Justificativa Obrigatória para Variações Limite
* **História / Referência**: MVP Requisitos Funcionais | RF-12
* **Update Set Alvo**: `RMG_v1.0_FINANCE_BR_Calculos_Relatorios`
* **Objetivo**: Exigir justificativa no caso quando a variação percentual total ultrapassar o limite configurado (ex: > 0%).
* **Pré-condições & Dependências**: `TASK-023` concluída.
* **Passos de Execução**:
  1. Criar Business Rule `before update` na tabela `u_rmg_case`:
     * Condição: `current.u_variacao_percentual_total > 0 && current.state == 2`.
     * Script: Se `JSUtil.nil(current.u_justificativa)`, executar `current.setAbortAction(true)` e disparar mensagem de erro na tela: `"Justificativa obrigatória para propostas com variação percentual positiva."`.
* **Critério de Aceite / Verificação**:
  * Tentar submeter uma proposta com aumento sem preencher o campo `u_justificativa` bloqueia o salvamento e exibe o alerta.

---

### TASK-025: Implementar Geração do Anexo da Nota Técnica em PDF Pesquisável
* **História / Referência**: MVP Entregáveis | Relatórios SEST
* **Update Set Alvo**: `RMG_v1.0_FINANCE_BR_Calculos_Relatorios`
* **Objetivo**: Automatizar a geração e anexação do PDF com o resumo da proposta no formato pesquisável da Nota Técnica.
* **Pré-condições & Dependências**: `TASK-024` concluída.
* **Passos de Execução**:
  1. Criar Script Include `RMGTechnicalNoteGenerator` utilizando o PDF Generation API nativo do ServiceNow.
  2. Desenhar o template HTML compilando:
     * Dados da Empresa, Exercício e Tipo de Assembleia.
     * Tabela comparativa por Órgão Estatutário / Cargo (Vigente vs. Proposto vs. Variação %).
     * Bloco de Justificativa da Empresa.
  3. Criar UI Action no caso `u_rmg_case` chamada `"Gerar Anexo Nota Técnica"` que executa o script e anexa o PDF pesquisável ao registro.
* **Critério de Aceite / Verificação**:
  * O PDF gerado é anexado ao caso, possui texto selecionável/pesquisável e bate 100% com os totais exibidos no formulário.

---

## 8. Fase 7: Governança, Batch Update Sets e Testes Automatizados

### TASK-026: Criar Batch Parent e Consolidar Update Sets de Desenvolvedores
* **História / Referência**: Governança SDD
* **Update Set Alvo**: `RMG_v1.0_RELEASE_BATCH_PARENT`
* **Objetivo**: Agrupar todos os 19 Update Sets individuais em uma estrutura de release única para migração segura.
* **Pré-condições & Dependências**: `TASK-001` a `TASK-025` concluídas.
* **Passos de Execução**:
  1. Criar o Update Set `RMG_v1.0_RELEASE_BATCH_PARENT` em `sys_update_set`.
  2. Para cada um dos 19 Update Sets das tarefas anteriores, definir o campo `Parent` apontando para `RMG_v1.0_RELEASE_BATCH_PARENT`.
  3. Alterar o estado dos Update Sets filhos para `Complete`.
* **Critério de Aceite / Verificação**:
  * A execução de Preview no Update Set Parent valida a árvore completa sem erros ou conflitos não resolvidos.

---

### TASK-027: Criar Suíte de Testes Automatizados no ATF (Automated Test Framework)
* **História / Referência**: Governança de Qualidade SDD
* **Update Set Alvo**: `RMG_v1.0_RELEASE_BATCH_PARENT`
* **Objetivo**: Garantir regressão zero automatizada para os fluxos principais.
* **Pré-condições & Dependências**: `TASK-026` concluída.
* **Passos de Execução**:
  1. No módulo Automated Test Framework (ATF), criar a Suíte `RMG_Regression_Suite`.
  2. Adicionar Test Steps:
     * Step 1: Open New Form (`u_rmg_case`).
     * Step 2: Set Field Values (Empresa, Exercício, Assembleia).
     * Step 3: Add Related List Record (`u_rmgtask` com valores).
     * Step 4: Submit Record.
     * Step 5: Impersonate (`RMG_Technical_Staff`).
     * Step 6: Validate Field State (Campos Read-Only).
  3. Executar a Suíte e armazenar o relatório de execução.
* **Critério de Aceite / Verificação**:
  * Todos os passos do ATF terminam com status `SUCCESS` em menos de 120 segundos.

---

## 9. Matriz de Resolução de Dependências e Checklist de Prontidão (DoD Global)

```
TASK-001 (Scoped App)
 ├──> TASK-002 (Grupos)
 ├──> TASK-003 (Case Types)
 │     ├──> TASK-006 (Case RMG) ──> TASK-007 (Case RMGS)
 │     │     └──> TASK-008 (Proposta Enviada) ──> TASK-009 (Histórico)
 │     └──> TASK-011 (Task Types)
 │           ├──> TASK-012 (RMGTASK) ──> TASK-013 (RMGSTASK)
 │           │     └──> TASK-014 (Client Scripts)
 │           │     └──> TASK-022 (Calculator Script) ──> TASK-023 (BR Totais)
 │           │                                            └──> TASK-024 (BR Justificativa)
 │           │                                                  └──> TASK-025 (PDF Nota Técnica)
 │           └──> TASK-017 (Task States)
 ├──> TASK-004 (Instâncias) ──> TASK-005 (Cargos)
 ├──> TASK-010 (Auditoria - Após tabelas criadas)
 ├──> TASK-015 (RMG States) ──> TASK-016 (RMGS States) ──> TASK-018 (ACLs Bloqueio)
 └──> TASK-019 (Empresas List) ──> TASK-020 (Workspace Menu) ──> TASK-021 (Landing Page)
                                                                       │
                                                                       v
                                                             TASK-026 (Batch Parent)
                                                                       │
                                                                       v
                                                             TASK-027 (ATF Suite)
```

### Checklist Final de Definição de Pronto (Definition of Done - DoD Global)
- [ ] Todas as 27 tarefas executadas e marcadas como concluídas.
- [ ] Batch Update Set `RMG_v1.0_RELEASE_BATCH_PARENT` sem erros de Preview.
- [ ] 100% dos testes unitários de matemática financeira validados.
- [ ] Suíte de testes ATF executada com sucesso total.
- [ ] Aplicação pronta para migração e Go-Live em Produção.
