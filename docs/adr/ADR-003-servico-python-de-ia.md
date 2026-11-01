# ADR-003 — Serviço Python independente para recomendação e assistente (RAG)

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 3 — Serviço Python e IA

## Contexto

A plataforma vai incorporar a recomendação inteligente (RF03) e o assistente educacional (RF04). Os modelos:
- são feitos em Python;
- têm ciclo de atualização próprio (o modelo muda sem mudar regra de negócio);
- têm carga variável, já que o assistente responde a picos de dúvidas;
- podem ficar indisponíveis.

A indisponibilidade da IA **não pode** bloquear as operações essenciais do Core: cadastro, matrícula, conclusão com XP e ranking.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-06 Tolerância a falhas** | A IA cai ou fica lenta sem afetar o Core. Exige fronteira de processo, timeout, circuit breaker e um comportamento degradado definido (comunicação e operação) |
| **ASR-07 Deploy independente** | O modelo de recomendação e o gerador do RAG mudam em ciclo próprio. Exige um artefato e um pipeline separados do Core (implantação) |
| **ASR-08 Escalabilidade elástica da IA** | A carga do assistente varia muito mais que a do Core. A IA precisa escalar réplicas sem escalar o Core (implantação) |

Interoperabilidade e manutenibilidade foram analisadas, mas não são drivers. A interoperabilidade fica resolvida com REST/JSON, sem exigir nada especial. A manutenibilidade melhora como consequência da separação.

## RPCs

- O Core continua em Java/Spring Boot.
- ML e RAG são feitos em Python.
- A indisponibilidade da IA não pode bloquear o fluxo essencial.

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — IA embutida no Core (Java, por exemplo DJL/ONNX) | Sem rede | Viola a RPC do Python. Um modelo pesado ou travado derruba o Core, e não dá para escalar a IA separadamente |
| B — Módulo acoplado (Python chamado por processo/JNI dentro do container do Core) | Sem rede entre containers | Mesmo ciclo de deploy e mesmo container: uma falha da IA afeta o Core, e escalar a IA obriga a escalar o Core |
| **C — Serviço Python independente (FastAPI) + cliente resiliente no Core** | Fronteira de falha, deploy e escala independentes; cumpre a RPC do Python | Rede, contrato HTTP, um container e um pipeline a mais, e o comportamento degradado precisa ser definido |

**ML e RAG: juntos ou separados?** Juntos **no mesmo serviço**, por enquanto, mas em rotas independentes (`/recomendacoes` e `/assistente/perguntas`) e sem estado compartilhado:
- usam a mesma linguagem e a mesma equipe;
- têm a mesma política de degradação;
- separar já criaria dois deploys sem driver.

**Gatilho de separação:** quando o RAG passar a usar um LLM externo, com custo por chamada, latência de segundos ou GPU, a escala e o custo dele deixam de ser os da recomendação. Aí ele vira um serviço próprio.

**Python sozinho justifica um serviço?** Não. O que justifica são os ASRs 06, 07 e 08. O Python é a RPC que torna a alternativa A inviável.

## Decisão

- Criar o **`ia-service`** (Python 3.12, FastAPI), com recomendação por conteúdo (`conteudo-jaccard-v1`) e assistente RAG (recuperação BM25 + gerador extrativo, com interface `Gerador` pronta para um LLM).
- O serviço é **sem estado e sem acesso ao banco do Core**: o Core envia o histórico e o catálogo em cada pedido.
- No Core, o módulo `recomendacao` chama o serviço por **REST/JSON**, com três proteções:
  - **timeout** de 1,5 s (`IA_TIMEOUT_MS`);
  - **circuit breaker** (Resilience4j: abre com 50% de falha em 3+ chamadas e tenta de novo após 15 s);
  - **fallback** local.

**Comportamento degradado aceito:**
- **Recomendação:** sai a regra local (cursos não concluídos do menor para o maior XP), com `origem=fallback, degradado=true`.
- **Assistente:** sai uma mensagem de indisponibilidade, sem resposta inventada.

## Consequências

**Positivas:**
- Uma falha da IA vira degradação, não erro.
- O modelo evolui e escala sozinho.
- O contrato é explícito e testável dos dois lados (pytest no Python, `RecomendacaoDegradadaTest` no Java).

**Trade-offs aceitos:**
- Latência de rede em cada recomendação.
- O payload carrega o catálogo inteiro. Isso serve para o catálogo atual; com milhares de cursos, o ia-service precisaria de um índice próprio, alimentado por eventos (Desafio 4).
- Mais um container e mais um pipeline (Desafio 6).
- O gerador do RAG é extrativo no laboratório (sem LLM) para não depender de chave de API. A interface `Gerador` isola essa troca.

## Evidência

- `RecomendacaoDegradadaTest`, com um stub HTTP no lugar do ia-service: IA no ar, IA com erro, IA lenta (o timeout corta), circuito abrindo e o fluxo essencial funcionando sem a IA.
- O [`mini-lab.sh`](../arquitetura/evidencias/desafio-3/mini-lab.sh) e seu [log](../arquitetura/evidencias/desafio-3/mini-lab.log): o Core consome o FastAPI real, o processo Python é morto, o Core responde com fallback e o circuito abre. O fluxo essencial continua funcionando, e o circuito se recupera quando o Python volta.
