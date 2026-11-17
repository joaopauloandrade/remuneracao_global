# Especificação Técnica de Desenvolvimento (SPEC.md) — Módulo de Remuneração Global ServiceNow

## 1. Visão Geral e Arquitetura do Sistema

### 1.1 Contexto Institucional e Objetivos do Projeto
O Módulo de Remuneração Global foi concebido para atender às diretrizes do Ministério da Gestão e da Inovação em Serviços Públicos (MGI), especificamente no âmbito da Secretaria de Coordenação e Governança das Empresas Estatais (SEST). O objetivo central da aplicação é automatizar, padronizar e auditabilizar todo o ciclo de vida de recepção, análise, validação e aprovação dos honorários e benefícios praticados pelos dirigentes das empresas estatais federais (dependentes e não dependentes).

Historicamente, o processamento da remuneração global ocorria de forma descentralizada e manual, por meio da troca de planilhas eletrônicas e controles paralelos. Esse modelo expunha a administração pública a elevados riscos operacionais, inconsistências de cálculo, retrabalho e baixa rastreabilidade histórica. A implementação no ServiceNow transforma essa dinâmica ao consolidar uma única base de dados relacional e auditável, provendo interfaces segregadas para as empresas estatais e para a equipe técnica e gestora da SEST.

### 1.2 Arquitetura Scoped Application e Convenções de Nomenclatura
A solução é desenvolvida sob o paradigma de Scoped Application na plataforma ServiceNow, garantindo o isolamento de escopo técnico, integridade de artefatos e facilidade de promoção entre ambientes via Update Sets ou repositórios de aplicação. Para assegurar o alinhamento com a arquitetura de Customer Service Management (CSM), os processos principais estendem as tabelas nativas de Case e Case Task.

Os identificadores de negócio e prefixos de numeração sequencial automática com 7 dígitos seguem rigorosamente a padronização estabelecida nos requisitos técnicos do projeto:

*   **Case Type RMG (`RMG0001001`)**: Destinado ao envio de novas propostas de remuneração global por parte das empresas estatais no fluxo principal (AGO/AGE).
*   **Case Type RBC (`RBC0001001`)**: Destinado ao fluxo de solicitações para cadastramento ou alteração de rubricas no catálogo padrão SEST.
*   **Case Type RMGS (`RMGS0001001`)**: Destinado à prestação e acompanhamento das propostas de remuneração de empresas subsidiárias.
*   **Task Type RMGTASK (`RMGTASK0001001`)**: Tarefas estruturais de análise de linhas de proposta vinculadas a um caso principal RMG.
*   **Task Type RMGSTASK (`RMGSTASK0001001`)**: Tarefas estruturais de análise de linhas de proposta vinculadas a um caso de subsidiária RMGS.

---

## 2. Perfis de Acesso, Grupos e Governança de Segurança

### 2.1 Estrutura de Grupos de Acesso (sys_user_group)
O controle de acesso à aplicação é estruturado em quatro grupos funcionais primários. Essa separação garante a segregação entre usuários externos (representantes das empresas estatais que operam via Portal) e usuários internos (equipe técnica e gestores que operam via Workspace).

A configuração dos grupos de acesso estabelece a base para o roteamento de atribuição de tarefas e controle granular de visibilidade:

| Nome do Grupo | Tipo de Usuário | Descrição e Atribuições Principais |
| :--- | :--- | :--- |
| **RMG Preenchedor** | Externo | Representantes das empresas estatais responsáveis pelo preenchimento, edição em modo rascunho e submissão oficial das propostas pelo Portal. Não possuem acesso ao Workspace interno. |
| **RMG Área Técnica** | Interno | Analistas técnicos da SEST responsáveis pela triagem, conferência, edição de valores de propostas submetidas, devolução para ajustes e emissão de pareceres no Workspace. |
| **RMG Gestor** | Interno | Gestores da área técnica da SEST com prerrogativa de homologação final de propostas, edição da base de rubricas padrão, alteração de casos aprovados durante a vigência e desativação de cadastros. |
| **RMG Administrador** | Interno | Administradores do módulo com privilégios integrais de parametrização, gestão de permissões, manutenção estrutural e suporte operacional ao sistema. |

### 2.2 Segregação de Dados por Empresa e Perfis
A segurança da informação é regida pelo princípio do privilégio mínimo e pelo isolamento estrito de dados institucionais:

*   **Visão de Empresa Estatal**: Cada usuário associado ao perfil de preenchedor visualiza e edita exclusivamente os registros e propostas pertencentes à sua própria organização. O acesso a dados de outras entidades estatais ou a propostas históricas de exercícios encerrados é estritamente vedado.
*   **Visão Interna SEST**: Os analistas e gestores da SEST possuem acesso amplo para consultar a base consolidada de todas as empresas estatais, permitindo análises comparativas e auditorias globais.

---

## 3. Modelo de Dados e Estrutura de Tabelas Customizadas

### 3.1 Tabelas de Apoio e Cadastros Base

#### Tabela Customizada de Cargos (`u_cargos`)
Armazena a lista padronizada de cargos autorizados para os órgãos estatutários, garantindo unicidade e impedindo duplicações.

| Nome do Campo | Elemento no Banco | Tipo de Dado | Obrigatoriedade e Regras de Negócio |
| :--- | :--- | :--- | :--- |
| Código | `u_codigo` | String | Obrigatório. Chave de identificação única (Índice Único). |
| Nome | `u_nome` | String | Obrigatório. Nome oficial do cargo (Índice Único, Case Sensitive). |
| Descrição | `u_descricao` | String (Multilinhas) | Opcional. Detalhamento das atribuições do cargo. |
| Ativo | `u_ativo` | Boolean | Obrigatório. Padrão `True`. Utilizado para desativação lógica (soft delete). |
| Data de Desativação | `u_data_desativacao` | Date/Time | Preenchimento automático na desativação com a data/hora corrente. |
| Motivo da Desativação | `u_motivo_desativacao` | String (Multilinhas) | Obrigatório ao desativar o registro. |

#### Tabela Customizada de Instâncias de Gestão (`u_instancias_gestao`)
Gerencia a estrutura oficial dos órgãos estatutários e comitês das empresas estatais (ex.: Diretoria, Conselho de Administração, Conselho Fiscal e Comitês).

| Nome do Campo | Elemento no Banco | Tipo de Dado | Obrigatoriedade e Regras de Negócio |
| :--- | :--- | :--- | :--- |
| Código | `u_codigo` | String | Obrigatório. Chave de identificação única (Índice Único). |
| Nome | `u_nome` | String | Obrigatório. Denominação oficial do órgão (Índice Único). |
| Descrição | `u_descricao` | String (Multilinhas) | Opcional. Descrição da instância estatutária. |
| Ativo | `u_ativo` | Boolean | Obrigatório. Padrão `True`. Preserva o histórico sem exclusão física. |
| Data de Desativação | `u_data_desativacao` | Date/Time | Preenchimento automático na inativação do registro. |
| Motivo da Desativação | `u_motivo_desativacao` | String (Multilinhas) | Justificativa obrigatória para desativação. |

### 3.2 Tabelas de Dados Operacionais e Propostas de Remuneração
O armazenamento e processamento detalhado das rubricas são distribuídos em tabelas customizadas especializadas, garantindo a rastreabilidade entre o rascunho em preenchimento, a proposta enviada imutável e a base histórica de valores vigentes.

As tabelas operacionais (`u_case_rmg`, `u_case_rmgs`, `u_proposta_enviada`, `u_historico_remuneracao`, `RMGTASK` e `RMGSTASK`) compartilham uma estrutura padronizada de 33 campos agrupados por domínio funcional:

| Agrupamento | Nome do Campo | Tipo de Dado | Regras de Preenchimento e Automação |
| :--- | :--- | :--- | :--- |
| **Controle** | `u_status` | Choice | Definido automaticamente com base no estado do caso pai. |
| **Controle** | `u_case_number` | Reference | Vínculo obrigatório ao caso pai (`sn_customerservice_case`). |
| **Usuário** | `u_user_name` | String | Automático: nome do usuário logado (Somente Leitura). |
| **Usuário** | `u_user_email` | String | Automático: e-mail do usuário logado (Somente Leitura). |
| **Usuário** | `u_user_phone` | String | Automático: telefone do usuário logado (Somente Leitura). |
| **Empresa** | `u_empresa_id` | String | Automático: ID único extraído do cadastro da empresa. |
| **Empresa** | `u_empresa_nome` | Reference | Automático no fluxo RMG; Lista filtrada no fluxo RMGS. |
| **Empresa** | `u_codigo_siest` | String | Automático: código SIEST da empresa (Somente Leitura). |
| **Empresa** | `u_empresa_sigla` | String | Automático: sigla institucional (Somente Leitura). |
| **Empresa** | `u_dependencia` | Choice | Automático: Dependente / Não Dependente (Somente Leitura). |
| **Empresa** | `u_capital` | String | Automático: estrutura de capital da empresa. |
| **Empresa** | `u_setor_atuacao` | String | Automático: setor econômico de atuação. |
| **Empresa** | `u_porte` | String | Automático: enquadramento de porte da empresa. |
| **Empresa** | `u_controle` | String | Automático: tipo de controle acionário. |
| **Assembleia** | `u_assembleia` | Choice | Escolha obrigatória: `AGO` ou `AGE`. |
| **Assembleia** | `u_data_assembleia` | Date | Data de realização da assembleia de acionistas/sócios. |
| **Assembleia** | `u_inicio_abrangencia`| Date | Data inicial da vigência da proposta aprovada. |
| **Assembleia** | `u_fim_abrangencia` | Date | Data final da vigência da proposta aprovada. |
| **Assembleia** | `u_exercicio` | Integer | Automático: ano de criação da proposta (Somente Leitura). |
| **Classificação**| `u_instancia_sest` | Reference | Referência à tabela `u_instancias_gestao`. |
| **Classificação**| `u_orgao_empresa` | String | Descrição livre do órgão utilizada pela empresa. |
| **Classificação**| `u_rubrica_sest` | Reference | Referência ao cadastro de Rubricas Padrão SEST. |
| **Classificação**| `u_rubrica_empresa` | String | Descrição livre da rubrica utilizada pela empresa. |
| **Classificação**| `u_cargo_sest` | Reference | Referência à tabela `u_cargos`. |
| **Classificação**| `u_cargo_empresa` | String | Descrição livre do cargo utilizada pela empresa. |
| **Valores** | `u_vl_rubrica` | Decimal (R$) | Valor mensal unitário da rubrica proposto. |
| **Valores** | `u_qtde` | Integer | Quantidade de ocupantes no cargo. |
| **Valores** | `u_pgto` | Integer | Número de pagamentos anuais previstos. |
| **Valores** | `u_valor_total` | Decimal (R$) | Calculado: `u_vl_rubrica * u_qtde * u_pgto` (Somente Leitura). |
| **Valores** | `u_var_mensal_perc` | Decimal (%) | Calculado automaticamente em relação ao valor vigente. |
| **Valores** | `u_var_total_perc` | Decimal (%) | Calculado automaticamente em relação ao total vigente. |
| **Justificativa**| `u_justificativa` | String (Large) | Texto obrigatório em caso de inclusão ou alteração. |
| **Observação** | `u_obs_sest` | String (Large) | Anotações e pareceres técnicos da equipe SEST. |
| **Anexos** | `u_anexos` | Attachment | Anexo 1 (Obrigatório), Anexo 2 (Obrigatório), Anexo 3 (Opcional). |

---

## 4. Ciclo de Vida e Máquinas de Estado

### 4.1 Máquina de Estados do Case RMG (Envio de Nova Proposta)
O fluxo principal do processo de Remuneração Global percorre seis estágios formais. A transição de estados coordena as permissões de edição e garante a imutabilidade dos dados após a submissão e homologação final.

As regras operacionais dos estados do Case RMG são definidas a seguir:

1.  **Em preenchimento (Default Inicial)**: A proposta encontra-se sob responsabilidade da empresa estatal em modo de rascunho. Permite edições livres, exclusões e salvamentos parciais.
2.  **Proposta enviada**: A empresa realiza a submissão oficial. O sistema gera uma foto congelada dos dados na tabela `Proposta Enviada`, bloqueia a edição para a empresa e disponibiliza o caso na fila da SEST.
3.  **Em análise**: O analista da SEST assume a condução do caso. São geradas as tarefas técnicas e ativadas as colunas de variação percentual e observações técnicas.
4.  **Devolvida para ajuste**: A SEST identifica inconsistências e devolve a proposta. As linhas de rubrica com status "Em análise" são removidas e as linhas "Proposta" são reabilitadas para edição da empresa estatal.
5.  **Aprovada**: Homologação final realizada pela SEST. O registro gera a nova base de valores vigentes na tabela `Histórico`. O caso torna-se somente leitura para todos os usuários, com exceção de alterações autorizadas efetuadas pelos perfis `RMG Gestor` e `RMG Administrador` durante a janela de vigência.
6.  **Cancelada**: Encerrado sem aprovação. Registro torna-se estritamente somente leitura para todos os perfis.

### 4.2 Máquina de Estados do Case RMGS (Remuneração de Subsidiárias)
O acompanhamento de subsidiárias utiliza uma máquina de estados simplificada contendo quatro estágios: **Em preenchimento**, **Proposta enviada**, **Aprovada** e **Cancelada**. As travas de segurança aplicadas aos estados homologados e cancelados seguem exatamente as mesmas regras descritas para o caso RMG.

### 4.3 Máquina de Estados das Tarefas Estruturais (RMGTASK e RMGSTASK)
As tarefas de trabalho associadas aos casos navegam por três estados operacionais:

*   **Proposta (Default Inicial)**: Tarefa gerada automaticamente e aguardando início da análise pela SEST.
*   **Editada**: Registra que houve modificação técnica em valores ou quantidades promovida pela equipe da SEST.
*   **Excluída**: Indica que a linha correspondente foi descontinuada durante a análise. A tarefa é bloqueada para qualquer edição adicional.

*Trava por Estado do Caso Pai*: Caso o registro principal (Case) esteja no estado **Aprovada**, todas as tarefas associadas são automaticamente convertidas para o modo somente leitura, impedindo qualquer modificação em seus campos.

---

## 5. Regras de Negócio, Validações e Automações Financeiras

### 5.1 Fórmulas e Cálculos Automáticos do Sistema
Para assegurar a confiabilidade e eliminar inconsistências humanas, a plataforma executa os cálculos matemáticos diretamente no servidor no momento do salvamento do registro:

*   **Subtotal por Rubrica**: $\text{Subtotal} = \text{Valor Mensal Proposto} \times \text{Número de Pagamentos}$
*   **Valor Total da Rubrica**: $\text{Valor Total} = \text{Valor Mensal Proposto} \times \text{Número de Pagamentos} \times \text{Quantidade de Cargos}$
*   **Variação Mensal Percentual**:
    $$\text{Variação Mensal (\%)} = \left( \frac{\text{Valor Mensal Proposto}}{\text{Valor Mensal Vigente}} - 1 \right) \times 100$$
*   **Variação Total Percentual**:
    $$\text{Variação Total (\%)} = \left( \frac{\text{Valor Total Proposto}}{\text{Valor Total Vigente}} - 1 \right) \times 100$$

*Tratamento de Exceções de Cálculo*: Quando o valor vigente de referência for nulo, não cadastrado ou igual a zero (caso típico de inclusão de nova rubrica), o sistema deve registrar a variação sem emitir erros de execução na interface.

### 5.2 Validações de Combinação de Órgãos e Cargos
O sistema valida a aderência estrutural entre a instância de gestão selecionada e os cargos permitidos. Tentativas de cadastramento fora da matriz abaixo devem ser bloqueadas pelo sistema:

| Órgão Estatutário (Instância de Gestão) | Cargos Permitidos (Padrão SEST) |
| :--- | :--- |
| **Diretoria** | Presidente, Vice-Presidente, Diretor, Liquidante |
| **Conselho de Administração** | Membro |
| **Conselho Fiscal** | Membro |
| **Comitê de Auditoria** | Presidente, Membro |
| **Demais Comitês** | Presidente, Membro |

### 5.3 Regras de Consistência Financeira de Rubricas
O motor de regras de negócio analisa a coerência dos valores informados com base em preceitos normativos estabelecidos:

*   **Gratificação Natalina (13º)**: O valor mensal cadastrado deve ser rigorosamente igual ao honorário mensal fixado para o cargo.
*   **Gratificação de Férias**: O valor mensal deve corresponder exatamente a $1/3$ (um terço) do honorário mensal do cargo.
*   **Quarentena**: O valor mensal deve corresponder a $100\%$ do honorário mensal fixado.
*   **Honorários dos Conselhos (Administração e Fiscal)**: O honorário fixado não pode exceder $10\%$ da remuneração mensal média dos diretores executivos da estatal. A fórmula de referência para o Honorário Fixo do Conselho de Administração é dada por:
    $$\text{Honorário Fixo CA} = \text{Média Ponderada dos Honorários Fixos} \times 0{,}1 \times \left(\frac{13}{12}\right)$$
*   **Comitê de Auditoria**: O valor fixado para o honorário do Comitê de Auditoria não pode ser inferior ao honorário estabelecido para o Conselho de Administração.

*Comportamento do Sistema*: Divergências identificadas nessas regras geram alertas visuais na interface para orientação do usuário, contudo não impedem a submissão formal da proposta para análise técnica.

---

## 6. Experiência do Usuário (Workspace, Landing Page e Portais)

### 6.1 Workspace Customizado Remuneração Global
Desenvolvido no padrão Next Experience e acessível via URL dedicada (`/remuneracao-global/home`), o Workspace é o ambiente de trabalho unificado dos analistas e gestores da SEST.

A estrutura de navegação do Workspace contempla:

*   **Cabeçalho e App Shell**: Identidade visual do projeto, busca global e notificações.
*   **Menu Lateral de Navegação**:
    *   *Início*: Landing Page operacional.
    *   *Listas*: Acesso rápido aos cases (RMG, RBC, RMGS) e tarefas (RMGTASK, RMGSTASK).
    *   *Dashboards*: Relatórios analíticos e consolidados.
    *   *Cadastros*: Consultas de Empresas, Instâncias de Gestão e Rubricas.
*   **Record Page Unificada**: Formulário de análise em abas de contexto com visualização de dados institucionais, histórico de atividades (*Activity Stream*) e painel de anexos.

### 6.2 Landing Page Operacional
A página inicial do Workspace disponibiliza contadores dinâmicos e filtros rápidos baseados exclusivamente no exercício/ano civil corrente:

*   **Indicadores AGO (Ordinária)**: Cartões com volumetria de propostas *Submetidas*, *Em análise* e *Aprovadas* no ano atual.
*   **Indicadores AGE (Extraordinária)**: Cartões com volumetria de propostas *Submetidas*, *Em análise* e *Aprovadas* no ano atual.
*   **Widget "Minhas Propostas"**: Lista personalizada exibindo os casos abertos (situação submetida ou em análise no ano atual) atribuídos diretamente ao usuário logado.

### 6.3 Consulta de Empresas (Modelo Comunica - Read-Only)
O módulo disponibiliza a lista de Empresas estatais cadastradas para consulta da equipe técnica. Para preservar a integridade da base cadastral nativa, as visões e formulários da lista de empresas são configurados em modo estrito de leitura (*Read-Only*). Botões de inserção (`Novo`), exclusão ou edição inline são desativados via interface e bloqueados no servidor por regras de segurança (ACLs).

---

## 7. Integração, Entregáveis, Auditoria e Requisitos Não-Funcionais

### 7.1 Auditoria e Rastreabilidade Nativa (`sys_audit`)
Em conformidade com as diretrizes de governança pública, a auditoria nativa da plataforma ServiceNow (`sys_audit`) é ativada em todas as tabelas customizadas do módulo (`u_cargos`, `u_instancias_gestao`, `u_case_rmg`, `u_case_rmgs`, `u_proposta_enviada`, `u_historico_remuneracao`, `RMGTASK` e `RMGSTASK`).

Cada operação de criação, alteração ou exclusão registra automaticamente:

*   Identificação do usuário responsável pela ação.
*   Data e hora exata da ocorrência (timestamp).
*   Nome do campo modificado.
*   Valor anterior e novo valor assumido pelo registro.

### 7.2 Entregáveis de Extração de Dados e Relatórios
A aplicação fornece mecanismos automáticos para geração e exportação de relatórios institucionais:

*   **Relatórios em Excel (.xlsx)**: Exportação tabular das propostas cadastradas, relatórios institucionais e base de rubricas aprovadas.
*   **Anexo da Nota Técnica em PDF Pesquisável**: O sistema gera automaticamente o anexo formatado da Nota Técnica em PDF com suporte a seleção de texto e pesquisa por OCR, impedindo a geração de arquivos convertidos exclusivamente em imagem.

### 7.3 Matriz de Requisitos Não-Funcionais (RNF)

| Código | Requisito Não-Funcional | Diretriz Técnica de Implementação |
| :--- | :--- | :--- |
| **RNF-01** | Segurança da Informação | Controle rigoroso por perfis (ACLs) garantindo o isolamento total de dados entre empresas estatais concorrentes. |
| **RNF-02** | Integridade dos Dados | Imutabilidade dos registros de exercícios anteriores. Apenas o perfil `RMG Gestor` pode ajustar a base histórica vigente. |
| **RNF-03** | Rastreabilidade | Registro cronológico completo de todas as alterações via mecanismo nativo `sys_audit`. |
| **RNF-04** | Usabilidade | Interface em layout tabular amigável no padrão Next Experience, com mensagens e alertas orientativos de preenchimento. |
| **RNF-05** | Confiabilidade | Execução exclusiva dos cálculos no servidor (Server-Side Business Rules / Script Includes) garantindo unicidade da informação. |
| **RNF-06** | Desempenho | Otimização de índices de banco de dados para suportar acessos simultâneos durante os picos de submissão de assembleias. |
| **RNF-07** | Disponibilidade | Operação em alta disponibilidade garantindo a continuidade do serviço no período de submissão. |
| **RNF-08** | Escalabilidade | Arquitetura modular em Scoped App permitindo a inclusão futura de novas validações e relatórios sem refatoração estrutural. |
