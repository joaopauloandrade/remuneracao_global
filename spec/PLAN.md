# PLAN.md — Plano de Implementação e Desenvolvimento (Remuneração Global ServiceNow)

## 1. Visão Geral do Plano e Estratégia SDD

Este documento estabelece o **Plano de Implementação (PLAN.md)** para o desenvolvimento do Módulo de Remuneração Global na plataforma ServiceNow, seguindo a metodologia **Spec Driven Development (SDD)**. 

A partir das especificações consolidadas no `SPEC.md` e nos requisitos funcionais e não funcionais mapeados no documento de negócio (MVP MGI/SEST) juntamente com as 18 histórias de desenvolvimento (User Stories), este plano detalha a ordem cronológica de execução, dependências técnicas, estratégia de empacotamento via Update Sets, plano de testes e governança de implantação.

### 1.1 Premissas de Desenvolvimento SDD
* **Construção incremental e declarativa**: Desenvolvimento em *Scoped Application* dedicada (`x_mgi_rmg`), isolando artefatos globais e garantindo integridade de atualização.
* **Validação por fases de dependência**: Tabelas de suporte e tipos de casos são construídos antes das tabelas de itens (tasks) e regras de cálculo automatizado.
* **Governança estrita de Update Sets**: Nomenclatura padronizada por história de usuário com commits frequentes e testes isolados em ambiente de Desenvolvimento (DEV) antes da migração para Teste (TST/HOMOL).

---

## 2. Matriz de Rastreabilidade e Mapeamento de Entregáveis

A tabela a seguir mapeia os requisitos do negócio aos artefatos técnicos especificados e suas respetivas histórias de desenvolvimento:

| Requisito do Negócio (MVP) | Descrição Resumida | História Técnica (User Story) | Entregável Técnico Principal |
| :--- | :--- | :--- | :--- |
| **RF-01, RF-02, RNF-02** | Estruturação de grupos de acesso e perfis de segurança segregados | `STRY0011811` | Grupos de Acesso (Submitters, Technical Staff, Managers, Admins) |
| **RNF-04** | Definição da Scoped App e Workspace customizado | `STRY0011809` | Application Scope `x_mgi_rmg` e Configuração de Workspace |
| **RF-03, RF-04** | Criação de tipos de casos para requisições e propostas | `STRY0011810` | Case Types com prefixos RMG, RBC e RMGS estendendo `sn_customerservice_case` |
| **RF-05, RF-06** | Cadastro padronizado de Órgãos Estatutários (Instâncias) | `STRY0011852` | Tabela custom `u_rmg_instancia_gestao` |
| **RF-07, RF-08** | Cadastro padronizado de Cargos de Executivos | `STRY0011853` | Tabela custom `u_rmg_cargo` |
| **RF-09 a RF-15** | Estrutura de cabeçalho das propostas das empresas estatais | `STRY0011858`, `STRY0011859` | Tabelas `u_rmg_case` e `u_rmgs_case` |
| **RF-16 a RF-25** | Armazenamento de propostas enviadas e histórico aprovado | `STRY0011860`, `STRY0011861` | Tabelas `u_rmg_proposta_enviada` e `u_rmg_historico` |
| **RF-26 a RF-35** | Detalhamento das linhas de rubrica e cargos da proposta | `STRY0011850`, `STRY0011862`, `STRY0011863` | Task Types e tabelas de itens `u_rmgtask` e `u_rmgstask` |
| **RF-36 a RF-40** | Governança do ciclo de vida e transição de estados dos casos | `STRY0011816`, `STRY0011817` | Life Cycle States para RMG e RMGS |
| **RF-41, RF-42** | Edição e transição de linhas da proposta em elaboração | `STRY0011851` | Task States para RMGTASK e RMGSTASK (Proposta, Editada, Excluída) |
| **RNF-01** | Rastreabilidade total e auditoria de alterações | `STRY0011814` | Ativação de Audit Log nas tabelas custom |
| **RF-43, RF-44** | Monitoramento via Landing Page do Workspace | `STRY0011815` | Landing Page, Data Visualization Components e Single Scorecard Widgets |
| **RNF-03** | Consulta de empresas com restrição de edição | `STRY0011812` | Módulo de consulta de empresas e ACLs read-only |

---

## 3. Fases de Desenvolvimento e Cronograma Técnico

O plano de implementação é estruturado em **6 Fases de Desenvolvimento (Sprints)** organizadas por ordem estrita de dependência arquitetural.

```
+-----------------------------------------------------------------------------------+
| FASE 1: Fundação da Aplicação, Grupos de Acesso e Cadastros Base                 |
| (STRY0011809, STRY0011810, STRY0011811, STRY0011852, STRY0011853)                |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| FASE 2: Arquitetura de Dados Principal e Auditoria Nativa                         |
| (STRY0011858, STRY0011859, STRY0011860, STRY0011861, STRY0011814)                |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| FASE 3: Estrutura de Tarefas (Case Tasks) e Linhas de Rubrica                      |
| (STRY0011850, STRY0011862, STRY0011863)                                           |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| FASE 4: Ciclo de Vida e Máquinas de Estado (Life Cycle States)                    |
| (STRY0011816, STRY0011817, STRY0011851)                                           |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| FASE 5: Interface de Usuário, Workspace & Landing Page                            |
| (STRY0011812, STRY0011815)                                                        |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| FASE 6: Automações Financeiras, Business Rules e Emissão de Relatórios            |
| (Regras de Cálculo, Formatação de Nota Técnica PDF/Excel e Validações Matemáticas) |
+-----------------------------------------------------------------------------------+
```

### 3.1 Fase 1: Fundação da Aplicação, Grupos de Acesso e Cadastros Base
* **Objetivo**: Criar o escopo da aplicação, tabelas mestre de suporte e estrutura de acessos.
* **Tarefas de Desenvolvimento**:
  1. Criar o Scoped Application `x_mgi_rmg` e definir privilégios de acesso entre escopos (`STRY0011809`).
  2. Criar a estrutura inicial do Workspace `Remuneração Global Workspace` com rotas e menu lateral (`STRY0011809`).
  3. Criar a hierarquia de Case Types estendendo `sn_customerservice_case`: `RMG`, `RBC` e `RMGS` (`STRY0011810`).
  4. Configurar os 4 Grupos de Acesso no ServiceNow (`STRY0011811`):
     * `RMG_Portal_Submitters` (Usuários Externos de Empresas Estatais)
     * `RMG_Technical_Staff` (Analistas Técnicos SEST)
     * `RMG_Managers` (Gestores Técnicos SEST)
     * `RMG_Admins` (Administradores do Sistema)
  5. Criar a tabela custom `u_rmg_instancia_gestao` (Instâncias de Gestão / Órgãos Estatutários) com validação de unicidade no campo `u_codigo` (`STRY0011852`).
  6. Criar a tabela custom `u_rmg_cargo` (Cargos de Executivos) com validação de unicidade no campo `u_codigo` (`STRY0011853`).
* **Critério de Aceite da Fase**: Tabelas base funcionais em formulário, grupos criados e Case Types reconhecidos pelo dicionário de dados.

### 3.2 Fase 2: Arquitetura de Dados Principal e Auditoria Nativa
* **Objetivo**: Implementar as tabelas principais de cabeçalho dos casos e registros históricos.
* **Tarefas de Desenvolvimento**:
  1. Criar a tabela custom de casos de Remuneração Global `u_rmg_case` estendendo `sn_customerservice_case`, adicionando campos corporativos, de assembleia e financeiros (`STRY0011858`).
  2. Criar a tabela custom de submissão de subsidiárias `u_rmgs_case` estendendo `sn_customerservice_case` (`STRY0011859`).
  3. Criar a tabela custom de propostas submetidas `u_rmg_proposta_enviada` (`STRY0011860`).
  4. Criar a tabela custom de histórico de propostas aprovadas e carga inicial `u_rmg_historico` (`STRY0011861`).
  5. Habilitar a flag nativa de auditoria (`audit = true`) no Dicionário de Dados para as tabelas `u_rmg_case`, `u_rmgs_case`, `u_rmg_proposta_enviada`, `u_rmg_historico`, `u_rmg_instancia_gestao` e `u_rmg_cargo` (`STRY0011814`).
* **Critério de Aceite da Fase**: Formulários principais configurados com campos de leitura/escrita e histórico de auditoria (`sys_audit`) registrando alterações.

### 3.3 Fase 3: Estrutura de Tarefas (Case Tasks) e Linhas de Rubrica
* **Objetivo**: Criar a estrutura granular de itens da proposta financeira.
* **Tarefas de Desenvolvimento**:
  1. Criar os Task Types estendendo `sn_customerservice_task`: `RMGTASK` e `RMGSTASK` (`STRY0011850`).
  2. Criar a tabela custom `u_rmgtask` para linhas de proposta de empresas controladas (`STRY0011863`).
  3. Criar a tabela custom `u_rmgstask` para linhas de proposta de subsidiárias (`STRY0011862`).
  4. Configurar relacionamentos parent-child entre `u_rmg_case` -> `u_rmgtask` e `u_rmgs_case` -> `u_rmgstask`.
  5. Implementar Client Scripts para auto-preenchimento de dados da empresa e usuário logado na criação da tarefa.
* **Critério de Aceite da Fase**: Criação de tarefas associadas ao caso pai funcional, com auto-preenchimento de dados de contexto.

### 3.4 Fase 4: Ciclo de Vida e Máquinas de Estado
* **Objetivo**: Configurar o fluxo de estados e regras de transição de formulários.
* **Tarefas de Desenvolvimento**:
  1. Configurar os estados do Life Cycle para o case `RMG` (`STRY0011816`):
     * `Rascunho` (Draft)
     * `Proposta Enviada` (Submitted)
     * `Em Análise Técnica` (Under Review)
     * `Aprovado` (Approved)
     * `Devolvido para Ajustes` (Returned)
     * `Cancelado` (Canceled)
  2. Configurar os estados do Life Cycle para o case `RMGS` (`STRY0011817`):
     * `Rascunho` (Draft)
     * `Proposta Enviada` (Submitted)
     * `Aprovado` (Approved)
     * `Cancelado` (Canceled)
  3. Configurar os estados funcionais para as tarefas `RMGTASK` e `RMGSTASK` (`STRY0011851`):
     * `Proposta`
     * `Editada`
     * `Excluída`
  4. Configurar Data Policies e UI Policies para bloqueio de edição em modo Read-Only nos estados finais (`Aprovado`, `Cancelado`, `Proposta Enviada`).
* **Critério de Aceite da Fase**: Impedimento estrito de edição de casos em estado final e alternância correta de visibilidade de campos conforme o estado ativo.

### 3.5 Fase 5: Interface de Usuário, Workspace & Landing Page
* **Objetivo**: Construir a experiência visual do analista e gestor no Configurable Workspace.
* **Tarefas de Desenvolvimento**:
  1. Configurar o módulo de consulta de empresas `u_rmg_empresa` com permissão estrita de leitura (`STRY0011812`).
  2. Remover botões `New`, `Delete` e edição inline na lista do Workspace de empresas (`STRY0011812`).
  3. Configurar a Landing Page do Workspace com widgets e filtros temporais (`STRY0011815`):
     * Métricas de propostas por assembleia (AGO vs. AGE) no ano vigente.
     * Lista "Minhas Propostas" filtrada pelo analista atribuído (`assigned_to = javascript:gs.getUserID()`).
     * Indicadores por estado da proposta.
* **Critério de Aceite da Fase**: Landing page exibindo dados em tempo real e lista de empresas protegida contra alterações inadvertidas.

### 3.6 Fase 6: Automações Financeiras, Business Rules e Relatórios
* **Objetivo**: Implementar lógica de cálculo financeiro, regras de validação e relatórios exportáveis.
* **Tarefas de Desenvolvimento**:
  1. Implementar Script Include `RMGCabalanceCalculator` para automatizar cálculos financeiros de totalização e variações percentuais.
  2. Criar Business Rules `before insert/update` para validação matemática:
     $$\text{Var \%} = \left( \frac{\text{Valor Proposto} - \text{Valor Vigente}}{\text{Valor Vigente}} \right) \times 100$$
  3. Configurar validação obrigatória de justificativa quando a variação for superior ao limite configurado.
  4. Configurar geração e exportação de relatórios em Excel/PDF e o anexo da Nota Técnica em PDF pesquisável.
* **Critério de Aceite da Fase**: Recálculo automático instantâneo ao alterar valores e exportação do anexo da Nota Técnica em conformidade com os requisitos do MGI/SEST.

---

## 4. Arquitetura de Update Sets e Governança no ServiceNow

Para garantir o rastreamento adequado e evitar conflitos de mesclagem (collisions), o desenvolvimento no ambiente de DEV será dividido em **Update Sets por História de Usuário**:

### 4.1 Nomenclatura Padrão de Update Sets
Formato obrigatório: `RMG_v1.0_<STORY_NUMBER>_<DESC_CURTA>`

* `RMG_v1.0_STRY0011809_Workspace_ScopedApp`
* `RMG_v1.0_STRY0011810_Case_Types`
* `RMG_v1.0_STRY0011811_Access_Groups`
* `RMG_v1.0_STRY0011852_Instancias_Gestao_Table`
* `RMG_v1.0_STRY0011853_Cargos_Table`
* `RMG_v1.0_STRY0011858_Case_RMG_Table`
* `RMG_v1.0_STRY0011859_Case_RMGS_Table`
* `RMG_v1.0_STRY0011860_Proposta_Enviada_Table`
* `RMG_v1.0_STRY0011861_Historico_Table`
* `RMG_v1.0_STRY0011814_Enable_Audit_Logs`
* `RMG_v1.0_STRY0011850_Task_Types`
* `RMG_v1.0_STRY0011862_RMGS_Task_Table`
* `RMG_v1.0_STRY0011863_RMG_Task_Table`
* `RMG_v1.0_STRY0011816_RMG_Case_States`
* `RMG_v1.0_STRY0011817_RMGS_Case_States`
* `RMG_v1.0_STRY0011851_Task_States`
* `RMG_v1.0_STRY0011812_Empresas_Read_Only_List`
* `RMG_v1.0_STRY0011815_Workspace_Landing_Page`
* `RMG_v1.0_FINANCE_BR_Calculos_Relatorios` (Update Set complementar de automações)

### 4.2 Ordem de Migração (Commit & Batching Order)
Na migração de Desenvolvimento (DEV) para Homologação (TST) e Produção (PRD), os Update Sets deverão ser consolidados em um **Batch Update Set Parent**: `RMG_v1.0_RELEASE_BATCH_PARENT`.

A ordem estrita de importação e preview será:
1. `STRY0011809` (Scope e Workspace)
2. `STRY0011810` e `STRY0011811` (Case Types e Grupos)
3. `STRY0011852` e `STRY0011853` (Cadastros Base)
4. `STRY0011858`, `STRY0011859`, `STRY0011860`, `STRY0011861` e `STRY0011814` (Tabelas de Caso e Auditoria)
5. `STRY0011850`, `STRY0011862`, `STRY0011863` (Tabelas de Tarefa e Linhas)
6. `STRY0011816`, `STRY0011817`, `STRY0011851` (Estados e Lifecycle)
7. `STRY0011812` e `STRY0011815` (Interfaces e Landing Page)
8. `FINANCE_BR_Calculos_Relatorios` (Business Rules e Script Includes)

---

## 5. Estratégia de Testes, Validação Financeira e Homologação

A garantia de qualidade será executada através de três camadas de teste:

```
+-----------------------------------------------------------------------------------+
| 1. TESTES UNITÁRIOS E DE SCRIPTS (Automated Test Framework - ATF & Fix Scripts)   |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 2. TESTES DE SEGURANÇA E ACL (Impersonation por Perfil de Acesso)                 |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 3. TESTES DE ACEITAÇÃO DO USUÁRIO (UAT / Homologação MGI/SEST)                    |
+-----------------------------------------------------------------------------------+
```

### 5.1 Testes Unitários de Cálculos Financeiros
Cenários obrigatórios a serem validados no Script Include de cálculo financeiro:

* **Cenário T1 — Variação Zero**: Valor Proposto igual ao Valor Vigente. Esperado: Variação % = 0,00%, Justificativa não obrigatória.
* **Cenário T2 — Aumento Salarial**: Valor Proposto R$ 25.000,00 vs. Vigente R$ 20.000,00. Esperado: Variação % = +25,00%, exigir Justificativa se o limite for ultrapassado.
* **Cenário T3 — Nova Rubrica (Vigente Zero)**: Valor Vigente R$ 0,00 e Proposto R$ 5.000,00. Esperado: Tratamento para divisão por zero (exibir N/A ou 100% com indicador de nova rubrica).
* **Cenário T4 — Totalização do Órgão Estatutário**: Soma dos honorários de 5 membros do Conselho de Administração deve bater com a linha de totalização do `u_rmgtask`.

### 5.2 Testes de ACL e Segurança por Perfil
* **Teste de Impersonação — Empresa Estatal**: Confirmar que o usuário só visualiza os casos criados para a sua própria empresa (`u_empresa = gs.getUser().getCompanyID()`).
* **Teste de Impersonação — Analista SEST**: Confirmar acesso de leitura e escrita em todos os casos em estado `Proposta Enviada` ou `Em Análise Técnica`.
* **Teste de Bloqueio em Estado Final**: Garantir que após mudar o caso para `Aprovado`, o formulário torne todos os campos Read-Only para qualquer usuário que não seja Administrador.

---

## 6. Matriz de Gestão de Riscos e Mitigação Técnica

| Risco Técnico / Operacional | Impacto | Probabilidade | Estratégia de Mitigação |
| :--- | :---: | :---: | :--- |
| **Divisão por zero em cálculo de variação %** (Rubricas sem valor vigente) | Alto | Média | Tratar no Script Include checando se o denominador é 0 antes de efetuar a divisão. |
| **Bloqueio indevido de formulário para Administrador** | Médio | Baixa | Configurar exceção explícita nas ACLs e Data Policies permitindo override do perfil `RMG_Admins`. |
| **Conflito de Update Sets entre desenvolvedores** | Alto | Média | Utilizar Update Sets estritamente vinculados às histórias de usuário e realizar Preview & Commit ordenado. |
| **Lentidão em consultas históricas grandes** | Médio | Baixa | Criar índices de banco de dados (`sys_index`) nos campos `u_empresa`, `u_exercicio` e `u_estado`. |
| **Falha na geração do PDF pesquisável da Nota Técnica** | Alto | Baixa | Utilizar gerador nativo de documentos da plataforma testando a renderização de caracteres acentuados e tabelas. |

---

## 7. Checklist de Prontidão para Implantação (Go-Live Checklist)

- [ ] Scoped Application `x_mgi_rmg` criada e configurada com acessos cross-scope liberados.
- [ ] Todas as 10 tabelas custom criadas e verificadas no Dicionário de Dados.
- [ ] Ativação de auditoria confirmada nas tabelas no ambiente de Produção.
- [ ] Mapeamento dos 4 Grupos de Acesso efetuado e vinculação às ACLs concluída.
- [ ] Life Cycle States e Task States configurados e validados por fluxo.
- [ ] Script Includes de automação financeira testados via ATF.
- [ ] Workspace Landing Page validada com dados reais de teste.
- [ ] Batch Update Set exportado, validado sem erros de Preview e com commit efetuado com sucesso em Produção.
- [ ] Teste de fumaça (*Smoke Test*) executado no ambiente produtivo com usuário simulado da Empresa Estatal.
