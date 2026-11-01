# ADR-001 — Evoluir o Core de Monólito em Camadas para Monólito Modular

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 1 — Drivers e evolução do Core

## Contexto

O MVP da AC1 cresce de cerca de 1.000 para até 100.000 usuários. O Ranking tende a crescer mais que o resto, enquanto Cursos, Conquistas e Recompensas continuam evoluindo. Na AC1, o Core estava organizado por camada técnica (`controller`, `service`, `repository`, `entity`, `dto`). Por isso, cada capacidade ficava espalhada em vários pacotes: o Ranking em 3 e a Conquista em 4. Além disso, o `UsuarioService` misturava cadastro, XP/nível/badges e ranking.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-01 Manutenibilidade** | As capacidades mudam em ritmos diferentes. Sem fronteiras explícitas, uma regra de XP ou de ranking força mudanças em vários pacotes técnicos (estrutura). |
| **ASR-02 Escalabilidade (Ranking)** | O Ranking cresce de forma desigual e pode precisar de uma estratégia própria, como cache, réplica de leitura ou extração. Para isso, ele precisa de uma fronteira isolável (estrutura e implantação). |
| **ASR-03 Complexidade operacional** | 6 desenvolvedores teriam que absorver rede, contratos, deploys e falhas distribuídas se o Core virasse serviços agora (operação). |

Desempenho e testabilidade foram analisados, mas **não** são drivers desta decisão. Ainda não há medição de gargalo, e a testabilidade melhora como consequência, sem determinar a decomposição.

## RPCs

- O Core continua em Java 21 com Spring Boot 3.4.
- A equipe tem 6 desenvolvedores.
- O banco relacional da AC1 (PostgreSQL/H2) é mantido.
- O legado da AC1 evolui de forma incremental, sem reescrita. Os endpoints públicos continuam compatíveis com o front-end Vue.

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — Manter o Monólito em Camadas | Nenhum custo de mudança; operação simples | As capacidades continuam espalhadas e o acoplamento cresce com o produto. O Ranking não tem fronteira para escalar depois |
| **B — Monólito Modular** | Fronteiras por capacidade, um único deploy e fronteiras verificadas por teste. Prepara uma extração seletiva | Ainda compartilha processo, banco e deploy; sem escala independente em tempo de execução |
| C — Extrair o Ranking como serviço | Escala e deploy independentes do Ranking | Rede, contrato, consistência dos dados de XP, observabilidade distribuída e um segundo pipeline, sem evidência de gargalo que justifique |

## Decisão

Adotar o **Monólito Modular**. O Core continua sendo um único container Spring Boot, organizado em módulos de negócio: `usuario`, `curso`, `matricula`, `conquista`, `ranking`, mais o módulo transversal `shared`.
- A API pública de cada módulo fica no pacote raiz dele (serviços, DTOs e entidades).
- Repositórios, seeders e controllers ficam em subpacotes internos (`internal`, `web`).
- As fronteiras são verificadas automaticamente pelo **Spring Modulith** (`ModularidadeTest`): o build quebra se um módulo usar o código interno de outro ou se surgir um ciclo.
- O **Ranking** é tratado como candidato explícito a uma extração futura: ele só lê dados através da API do módulo `usuario`.

## Consequências

**Positivas:**
- Cada capacidade passa a ficar em 1 pacote.
- As dependências entre pacotes de topo caíram de 41 para 16 imports.
- O `UsuarioService` deixou de misturar capacidades.
- As fronteiras deixam de ser só convenção: um teste as garante.
- A operação continua igual: um jar, um banco e um `docker compose`.

**Trade-offs aceitos:**
- O deploy e o processo continuam compartilhados.
- O Ranking ainda escala junto com o Core.
- `Matricula` mantém referências JPA a `Usuario` e `Curso`, ou seja, o acoplamento de dados continua no mesmo banco.

**Gatilho de revisão:** reavaliar a extração do Ranking quando as métricas do Desafio 6 (latência p95 de `GET /api/ranking` e throughput) mostrarem gargalo, ou quando a equipe precisar publicar o Ranking em um ritmo diferente do Core.

## Evidência

- [`evidencias/desafio-1/dependencias-antes-depois.md`](../arquitetura/evidencias/desafio-1/dependencias-antes-depois.md): análise automática das duas versões.
- [`evidencias/desafio-1/falha-simulada-fronteira.log`](../arquitetura/evidencias/desafio-1/falha-simulada-fronteira.log): o Ranking acessando `usuario.internal.UsuarioRepository` faz o build falhar.
- [`evidencias/desafio-1/testes-apos-modularizacao.log`](../arquitetura/evidencias/desafio-1/testes-apos-modularizacao.log): os 23 testes da AC1 continuam passando, mais os 2 testes de modularidade.
