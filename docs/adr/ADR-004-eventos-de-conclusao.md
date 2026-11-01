# ADR-004 — Conclusão síncrona e consumidores por eventos persistidos (sem broker por enquanto)

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 4 — Comunicação e eventos

## Contexto

Ao concluir um curso, o sistema:
1. registra a conclusão;
2. calcula a recompensa (XP, nível e badges);
3. atualiza o progresso;
4. pode disparar uma notificação e analytics.

Uma falha da notificação **não pode** impedir a conclusão. Até o Desafio 3, tudo acontecia numa única chamada síncrona dentro de `MatriculaService.concluir`.

## O que precisa ser imediato e o que pode ser eventual

| Passo | Imediato ou eventual | Por quê |
|---|---|---|
| Registrar a conclusão | **Imediato** | É a operação essencial; o aluno precisa da confirmação |
| XP, nível e badges | **Imediato** | O aluno vê o XP na resposta, e o ranking depende disso. A consistência forte entre a conclusão e o XP evita "concluí e não ganhei XP" |
| Notificação | **Eventual** | Pode chegar segundos depois; depende de um provedor externo que falha |
| Analytics, índice da IA e outros futuros | **Eventual** | Não fazem parte da resposta ao aluno |

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-09 Tolerância a falhas do fluxo de conclusão** | Uma falha de um consumidor não pode desfazer a conclusão. Exige comunicação assíncrona depois do commit (comunicação) |
| **ASR-10 Recuperabilidade (sem perda de eventos)** | Se o consumidor estiver fora, o evento não pode se perder. Exige que o evento seja persistido na mesma transação da conclusão e reentregue depois (dados e operação) |
| **ASR-11 Desacoplamento entre produtor e consumidores** | Novos consumidores (analytics, IA) não podem exigir mudança em `matricula`. Exige um contrato de evento (estrutura e comunicação) |

Latência e disponibilidade foram analisadas. A latência da conclusão melhora como consequência, porque a notificação sai da resposta. A disponibilidade fica coberta pelo ASR-09. A consistência foi resolvida por passo: forte para o XP e eventual para o resto.

## RPCs

- As operações essenciais do Core precisam concluir mesmo com um consumidor externo indisponível.
- A adoção de broker tem custo operacional.
- Equipe de 6 desenvolvedores; deploy único (ADR-001).

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — Tudo REST/síncrono na conclusão | Simples; resultado imediato de tudo | Falha da notificação **desfaz a conclusão** (demonstrado por teste), e a latência do provedor entra na resposta |
| B — Evento em memória (`@EventListener` assíncrono comum) | Desacopla e não bloqueia | O evento **se perde** se o consumidor falhar ou o processo cair |
| **C — Evento persistido na transação (outbox) com o Event Publication Registry do Spring Modulith + listener assíncrono** | Conclusão não depende do consumidor; nenhum evento se perde; reentrega automática; sem nenhum componente operacional novo | Consistência eventual da notificação; o consumidor precisa ser idempotente; tudo ainda dentro do mesmo processo |
| D — Broker (RabbitMQ ou Kafka) | Consumidores em outros processos; escala e retenção | Novo componente para operar, monitorar e manter seguro. Hoje todos os consumidores estão no Core, então não há consumidor remoto que o justifique |

**RabbitMQ × Kafka, pelo problema do case:**
- O fluxo é **comando/notificação** com poucos consumidores, entrega pelo menos uma vez e sem necessidade de reprocessar o histórico. Isso é o perfil do **RabbitMQ**.
- O **Kafka** se justificaria por um log de eventos retido para reprocessamento, alto throughput ou vários consumidores lendo em ritmos diferentes, como o analytics do *learning record*.

Nenhum dos dois é necessário **agora**. O registro de publicações já dá a garantia de entrega.

## Decisão

1. A **conclusão e o XP continuam síncronos**, na mesma transação.
2. `matricula` publica o evento **`CursoConcluido`**. O Spring Modulith (`spring-modulith-starter-jpa`) grava a publicação na tabela `event_publication` **na mesma transação** da conclusão (outbox).
3. O novo módulo **`notificacao`** consome o evento com `@ApplicationModuleListener`, ou seja, de forma assíncrona, depois do commit e em transação própria.
4. Se o consumidor falhar, a publicação fica **incompleta**, e o `ReprocessadorDeEventos` a reentrega a cada 15 s.
5. O consumidor é **idempotente**: a notificação tem `matricula_id` único, então a reentrega não duplica.
6. **Broker adiado.** O gatilho é surgir um consumidor **fora do processo do Core**, por exemplo o ia-service indexando eventos de aprendizagem ou um serviço de notificação separado. Nesse ponto, o evento passa a ser externalizado: o Spring Modulith publica as mesmas classes de evento em RabbitMQ ou Kafka sem mudar o produtor.

## Consequências

**Positivas:**
- A queda da notificação não afeta a conclusão.
- Nenhum evento se perde, porque fica persistido junto com a conclusão.
- A recuperação é automática.
- Novos consumidores só precisam escutar `CursoConcluido`.
- Zero componentes novos de infraestrutura.

**Trade-offs aceitos:**
- A notificação é eventual: chega segundos depois, ou depois que o provedor volta.
- A entrega é **pelo menos uma vez**, o que exige consumidores idempotentes.
- A tabela `event_publication` cresce, então é preciso limpar as publicações completas.
- Os consumidores continuam no mesmo processo; isso muda quando houver broker.

## Evidência

- `EventosConclusaoTest` cobre três cenários:
  - consumidor no ar;
  - consumidor fora, com o evento pendente e reprocessado;
  - contraste síncrono, em que a mesma falha desfaz a conclusão, o XP e o evento.
- O [`mini-lab.sh`](../arquitetura/evidencias/desafio-4/mini-lab.sh) e seu [log](../arquitetura/evidencias/desafio-4/mini-lab.log) mostram o mesmo pela API real.
