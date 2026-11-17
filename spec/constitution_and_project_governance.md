# `constitution.md` — Governança, Arquitetura e Compliance (Global)

> **Status:** Ativo / Imutável
>
> **Escopo:** Global / Arquitetura de Software de Ponta a Ponta
>
> **Tecnologias Core:**
>
> * **Backend:** Java 25, Spring Boot 3.x/4.x, Maven/Gradle, Spring Boot Actuator
>
> * **Frontend:** Next.js (App Router), TypeScript (Strict Mode), Tailwind CSS, shadcn/ui, TanStack Query

## I. Preâmbulo e Princípios Fundamentais (As Leis Imutáveis)

1. **Simplicidade Acima de Complexidade Desnecessária (KISS):** O código deve ser legível por humanos antes de ser executado por máquinas. Evite *over-engineering* e frameworks pesados quando soluções nativas do ecossistema atenderem ao requisito.

2. **Imutabilidade e Segurança por Padrão:** Objetos de domínio, DTOs e tipos de estado devem ser imutáveis sempre que possível (`record` no Java e `readonly` / tipos estritos no TypeScript). Nenhuma credencial ou dado sensível pode ser versionado no código-fonte (Lei de Compliance/LGPD).

3. **Observabilidade Nativa:** Nenhuma funcionalidade de backend entra em produção sem logs estruturados (JSON), rastreabilidade distribuída (*Distributed Tracing* via Micrometer/OpenTelemetry), métricas operacionais e endpoints de saúde devidamente expostos via **Spring Boot Actuator**.

4. **Resiliência e Tolerância a Falhas:** Integrações externas e chamadas de banco de dados devem ser protegidas por mecanismos de *Circuit Breaker*, *Retry* e *Timeout* padronizados.

## II. Diretrizes Arquiteturais do Backend (Java 25 & Spring Boot)

1. **Estilo Arquitetural (Arquitetura Hexagonal / Modular Monolith):**

   * **Domínio Isolado:** O núcleo da aplicação (`domain`) não pode ter dependências diretas de frameworks (Spring, Hibernate, etc.). O Spring Boot atua exclusivamente na camada de infraestrutura e aplicação (`adapters`).

   * **Divisão de Pacotes:** `domain` (regras puras), `application` (casos de uso) e `infrastructure` (controladores REST, persistência).

2. **Uso Adequado do Ecossistema Java 25 & Spring Boot:**

   * Priorize o uso intensivo de **Records** para DTOs e Value Objects, **Pattern Matching** e **Sequenced Collections**.

   * **Injeção de Dependências:** Obrigatoriamente via construtor (gerada por `@RequiredArgsConstructor` ou nativa). O uso de `@Autowired` em campos (*field injection*) é terminantemente **proibido**.

   * **Gerenciamento de Transações:** `@Transactional` deve ser aplicado exclusivamente na camada de Serviço (`application`). Utilize `@Transactional(readOnly = true)` em consultas.

3. **Observabilidade e Gestão com Spring Boot Actuator:**
   * **Inclusão Obrigatória:** A dependência `spring-boot-starter-actuator` é mandatória em todos os microsserviços ou aplicações Spring Boot.
   * **Endpoints de Saúde e Métricas:** Os endpoints essenciais (`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`) devem estar configurados para alimentar ferramentas de monitoramento (Prometheus, Grafana, Kubernetes Liveness/Readiness Probes).
   * **Segurança de Endpoints (Hardening):** 
     * Endpoints sensíveis do Actuator (como `/env`, `/beans`, `/heapdump`, `/logfile`) **nunca** devem ser expostos publicamente na porta principal da aplicação.
     * Deve-se isolar a porta de gerenciamento (`management.server.port`) ou restringir o acesso aos endpoints via Spring Security apenas para perfis autorizados (ex: ROLE_ADMIN ou redes internas/Sidecars).
     * O detalhamento de saúde (`management.endpoint.health.show-details`) deve ser restrito a usuários autenticados ou liberado apenas como `when-authorized` em ambientes de produção.

4. **Tratamento de Erros e Respostas API:**

   * **Padronização RFC 7807 (Problem Details):** Todos os erros da API devem retornar JSON padronizado (`type`, `title`, `status`, `detail`). Um `@ControllerAdvice` global é obrigatório.

## III. Diretrizes Arquiteturais do Frontend (Next.js & TypeScript)

1. **Stack Tecnológica & Padrões Core:**

   * **Framework:** Next.js com **App Router** (utilizando SSR, SSG/ISR e Client Components de forma inteligente).

   * **Linguagem:** TypeScript em **Strict Mode** ativado, garantindo contratos rígidos com os DTOs do backend.

   * **Gerenciamento de Server State:** **TanStack Query (React Query)** obrigatório para gerenciar requisições, cache, paginação e sincronização de dados da API.

   * **Estilização e Componentes:** **Tailwind CSS** combinado com **shadcn/ui** para consistência de design e acessibilidade.

   * **Validação:** **Zod + React Hook Form** para formulários tipados de ponta a ponta.

2. **Organização Modular (Feature-Driven):**

   * A estrutura de pastas do frontend deve ser organizada por domínios/features (agrupando componentes, hooks e serviços de uma mesma regra de negócio), evitando pastas globais gigantescas de arquivos técnicos.

## IV. Qualidade de Código, Testes e Compliance Técnico

1. **Cobertura de Testes Obrigatória:**

   * **Backend:** Testes unitários (`JUnit 5`, `Mockito`) para regras de negócio e testes de integração com **Testcontainers**.

   * **Frontend:** Testes de componentes críticos e validação de fluxos de usuário.

2. **Análise Estática de Código:**

   * O código deve passar sem alertas críticos em ferramentas de análise estática integradas ao pipeline CI/CD (SonarQube, Checkstyle, ESLint).

   * Complexidade ciclomática por método deve ser mantida baixa (limite recomendável: $\le 10$).

## V. Segurança, Privacidade e Compliance

1. **Proteção de Dados (LGPD):** Nenhum dado pessoal identificável (PII) pode aparecer em logs de auditoria ou console. Mascaramento automático deve ser aplicado a CPFs, e-mails e chaves sensíveis.

2. **Autenticação e Autorização:**

   * Padrão obrigatório **OAuth2 / OpenID Connect** via JWT.

   * Validação de acesso tanto na borda (Middleware/Filtros) quanto nas regras de negócio.

3. **Auditoria:** Operações críticas de escrita (Criação, Atualização, Exclusão de registros sensíveis) devem gerar rastros de auditoria imutáveis.

## VI. Versionamento e Mudanças na Constituição

* Esta constituição é um documento vivo, porém **suas regras só podem ser alteradas mediante aprovação em Code Review coletivo** (Pull Request com ao menos dois revisores seniores).

* Qualquer código gerado por IA ou por desenvolvedores humanos que viole estas diretrizes terá o Pull Request rejeitado automaticamente.