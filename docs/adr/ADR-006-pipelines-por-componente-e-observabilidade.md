# ADR-006 — Pipelines Jenkins por componente e métricas que validam as decisões

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 6 — CI/CD e observabilidade

## Contexto

Depois dos Desafios 1 a 5, a solução tem quatro componentes com ciclos e linguagens diferentes:
- o Core (Java/Spring Boot, Monólito Modular);
- o `ia-service` (Python/FastAPI);
- o gateway IoT (Python);
- o broker MQTT.

Cada decisão anterior deixou uma hipótese que só se sustenta se puder ser **verificada a cada mudança** e **observada em operação**:
- fronteiras de módulo;
- domínio isolado;
- degradação da IA;
- eventos sem perda;
- IoT sem duplicidade;
- o gatilho de extração do Ranking.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-15 Deploy independente com segurança** | O ia-service e o gateway foram separados para mudar sozinhos (ADR-003, ADR-005). Isso exige um pipeline e um artefato por componente, com portões próprios (implantação) |
| **ASR-16 Observabilidade das decisões** | Fallback da IA, circuito, eventos pendentes, p95 do ranking e IoT precisam virar métricas e alertas. Exige instrumentação em cada componente e uma coleta central (operação) |
| **ASR-17 Testabilidade como portão** | As regras de arquitetura (Modulith, ArchUnit) e a cobertura precisam bloquear a entrega, não só existir. Exige estágios que falham o build (implantação) |

Confiabilidade, recuperabilidade e desempenho já foram tratados nos ASRs 02, 06, 09, 10 e 12. Aqui eles são **medidos**, não redecididos.

## RPCs

- O Jenkins é a base da esteira.
- Prometheus e Grafana são as ferramentas do laboratório.
- Artefatos e configurações são versionados: Jenkinsfile, JCasC, `prometheus.yml`, alertas e painel em JSON.

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — Pipeline único para o monorepo | Um arquivo; visão única | Qualquer mudança testa e reconstrói tudo. Uma falha do Python bloqueia o deploy do Core e vice-versa, o que anula o deploy independente que justificou separar a IA |
| **B — Um pipeline por componente (Jenkinsfile na pasta do componente), disparado só quando a pasta muda** | O deploy independente fica real; build mais rápido; quem mexe no componente é dono do pipeline | Três Jenkinsfiles; mudanças de contrato entre componentes precisam de testes de contrato dos dois lados (já existem: `RecomendacaoDegradadaTest` e pytest) |

**Métricas: técnica × evidência arquitetural.**
- **Métrica técnica** descreve o componente, por exemplo a CPU ou a contagem de requisições HTTP.
- **Evidência arquitetural** é a métrica que confirma ou derruba uma hipótese de um ADR.

| ADR | Hipótese | Métrica (evidência) | Alerta |
|---|---|---|---|
| ADR-001 | O Ranking não precisa ser extraído enquanto o p95 ficar baixo | `http_server_requests_seconds_bucket{uri="/api/ranking"}`, p95 | `RankingLento` (p95 > 300 ms por 10 min = gatilho de reavaliação) |
| ADR-003 | Queda da IA vira degradação, não erro | `ia_respostas_total{origem}`, `resilience4j_circuitbreaker_state`, 5xx do Core | `IaEmModoDegradado`, `CircuitoIaAberto` |
| ADR-004 | Nenhum evento se perde; a falha do consumidor não afeta a conclusão | `eventos_pendentes`, `notificacao_envios_total{resultado}`, `gamificacao_conclusoes_total` | `EventosPendentesAcumulando` |
| ADR-005 | Duplicatas são barradas e nenhum evento se perde | `iot_eventos_{recebidos,encaminhados,duplicados}_total`, `iot_falhas_core_total` | — |
| Todos | Componentes no ar | `up` | `ComponenteForaDoAr` |

## Decisão

**Um pipeline Jenkins por componente** (`backend/Jenkinsfile`, `ia-service/Jenkinsfile` e `iot/Jenkinsfile`). Os jobs são criados como código (JCasC + Job DSL, em `ci/jenkins/`), com `includedRegions` na pasta do componente.

**O que bloqueia o deploy:**

| Componente | Portões |
|---|---|
| Core | 1) **Regras de arquitetura** (`ModularidadeTest` e `HexagonalConquistaTest`), rodadas primeiro para falhar rápido. 2) **Suíte + cobertura** (`mvn verify` com `jacoco:check`: ≥ 80% de linhas no total e ≥ 95% em `conquista`); `MetricasTest` garante que as métricas de evidência existem. 3) **Imagem Docker**. 4) **Smoke test**: a imagem sobe, `/actuator/health` responde UP e `/actuator/prometheus` expõe as métricas |
| ia-service | pytest → imagem → smoke (`/health` e `/metrics`) |
| Gateway IoT | pytest → imagem → smoke (importa o gateway e o tradutor) |

**Observabilidade:**
- O Core (Micrometer) expõe `/actuator/prometheus` com histograma de latência e métricas de negócio e de resiliência.
- O ia-service usa `prometheus-fastapi-instrumentator` (`/metrics`).
- O gateway usa `prometheus_client` (`:9101`).
- O Prometheus v2.55.1 coleta a cada 5 s e avalia `alertas.yml`; cada alerta é ligado a um ASR.
- O Grafana 11.3.1 tem o painel provisionado **"B1 — Evidências arquiteturais"**, com um gráfico por hipótese.

## Consequências

**Positivas:**
- Cada componente é entregue no próprio ritmo, e uma falha num não bloqueia o outro.
- As regras arquiteturais viraram *fitness functions* que bloqueiam a entrega.
- Os gatilhos de revisão dos ADRs (extração do Ranking, broker) passaram a ter uma métrica e um alerta observáveis.

**Trade-offs aceitos:**
- Três pipelines para manter.
- O build do Core no Jenkins baixa as dependências de novo dentro do `docker build`; um cache de camadas ou um repositório de artefatos fica para depois.
- O Jenkins do laboratório roda com o socket do Docker e usuário root, o que não serve para produção: lá ele usaria agentes isolados.
- Ainda não há estágio de deploy num ambiente, só construção e verificação da imagem publicável.

## Evidência

- Builds reais no Jenkins 2.580.2 dos três jobs, com o resultado de cada estágio: [`evidencias/desafio-6/jenkins/`](../arquitetura/evidencias/desafio-6/jenkins/).
- Mini-lab de observabilidade ([script](../arquitetura/evidencias/desafio-6/mini-lab.sh), [log](../arquitetura/evidencias/desafio-6/mini-lab.log)): tráfego normal, depois o ia-service derrubado, depois religado. Inclui consultas ao Prometheus, os alertas disparados e as capturas do Grafana.
