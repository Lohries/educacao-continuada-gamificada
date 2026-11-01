# ADR-005 — Eventos IoT por MQTT, com gateway traduzindo para o Core

- **Status:** aceito
- **Data:** 2026-11-01
- **Desafio:** B1 / Desafio 5 — IoT e MQTT

## Contexto

Os laboratórios vão gerar eventos de presença, início e conclusão de atividade a partir de dispositivos:
- com pouco poder computacional;
- com conexão instável.

Esses eventos precisam entrar na plataforma **sem acoplar o Core a cada dispositivo**: o Core não pode conhecer protocolo, firmware ou formato de cada sensor.

## ASRs

| ASR | Como altera a arquitetura |
|---|---|
| **ASR-12 Tolerância à conectividade intermitente** | Dispositivo e consumidor caem; os eventos não podem se perder. Exige um broker com sessão persistente, QoS 1 e *store-and-forward* no dispositivo (comunicação e implantação) |
| **ASR-13 Eficiência de comunicação** | O dispositivo tem pouca CPU, memória e banda. Exige um protocolo leve, com conexão mantida e payload compacto (comunicação) |
| **ASR-14 Interoperabilidade / desacoplamento do Core** | Dispositivos heterogêneos não podem chegar diretamente ao Core. Exige um gateway que traduza para o contrato do Core (estrutura) |

Escalabilidade, disponibilidade e segurança foram analisadas:
- **Escalabilidade:** o broker absorve o *fan-in* dos dispositivos.
- **Disponibilidade:** fica coberta pelo ASR-12.
- **Segurança de integração:** é relevante, mas não muda a estrutura escolhida. Muda a configuração do broker (TLS e ACL), registrada abaixo como pendência para produção.

## RPCs

- Os dispositivos podem ter pouco poder computacional.
- A conectividade pode ser intermitente.
- O protocolo e o broker adicionam componentes operacionais.
- O Core continua em Java/Spring Boot, com deploy único (ADR-001).

## Alternativas

| Alternativa | Benefícios | Custos / riscos |
|---|---|---|
| A — REST direto do dispositivo ao Core | Nenhum componente novo | HTTP/TLS por evento pesa para o dispositivo. Sem buffer no meio: Core fora significa evento perdido ou retry no dispositivo. O Core passa a conhecer cada formato de dispositivo |
| **B — MQTT (Mosquitto) + gateway consumidor que traduz e chama o Core por REST** | Protocolo leve; QoS 1 e sessão persistente guardam mensagens com o consumidor fora; o Core recebe um contrato único | Broker e gateway a mais para operar; entrega pelo menos uma vez, o que exige deduplicação |
| C — MQTT + ponte para uma mensageria interna (RabbitMQ/Kafka) | Fan-out para vários consumidores internos e retenção | Um segundo broker sem ASR que o justifique: só há um consumidor (o Core), e o ADR-004 já cobre a entrega interna |

**O que o MQTT resolve especificamente:**
- conexão mantida com *keepalive*, sem *handshake* por evento;
- payload binário pequeno: aqui são cerca de 99 bytes por evento;
- **QoS 1** (pelo menos uma vez);
- **sessão persistente**: o broker guarda as mensagens do assinante desconectado;
- tópicos hierárquicos (`lab/<lab>/dispositivo/<id>/eventos`) para roteamento e ACL.

**Outro broker depois do MQTT?** Não. Só se aparecerem vários consumidores internos independentes dos eventos de laboratório, ou necessidade de retenção e *replay*. Esse é o mesmo gatilho do ADR-004.

## Decisão

- **Broker Eclipse Mosquitto 2.0.20**, com persistência ligada.
- **Dispositivos:** publicam payload compacto (`id`, `d`, `t`, `a`, `ts`) com **QoS 1** e guardam na fila local o que geram offline (*store-and-forward*).
- **Gateway IoT** (Python, paho-mqtt 2.1.0):
  - **sessão persistente** (`clean_session=False`) com assinatura QoS 1;
  - **ack manual**: só confirma ao broker depois que o Core aceitou o evento;
  - valida e traduz para o contrato REST do Core;
  - descarta duplicatas com um cache LRU de `eventId`.
- **Core** (módulo `aprendizagem`): `POST /api/eventos-aprendizagem`, **idempotente** pelo `eventId` único (201 novo, 200 reentrega). É a garantia final contra duplicidade.

## Consequências

**Positivas:**
- Dispositivo, gateway ou Core podem cair sem perder eventos: cada salto guarda até o próximo confirmar.
- O Core não conhece MQTT nem dispositivos.
- Um novo tipo de dispositivo exige mudança só no gateway.

**Trade-offs aceitos:**
- Dois componentes novos (broker e gateway) para operar.
- A entrega é pelo menos uma vez; duplicatas são esperadas e tratadas em duas camadas.
- O cache do gateway é volátil: depois de um restart, a deduplicação fica só com a unique do Core.
- **Segurança:** o laboratório usa o broker sem autenticação. **Antes de produção** é preciso TLS na porta 8883, uma credencial por dispositivo e ACL restringindo cada dispositivo ao próprio tópico.

## Evidência

- `test_tradutor.py` (10 testes): contrato, tipos, payload inválido, tópico e cache de deduplicação.
- `EventoAprendizagemTest`: 201/200 por `eventId`, tipo inválido (400) e aluno inexistente (404).
- O [`mini-lab.sh`](../arquitetura/evidencias/desafio-5/mini-lab.sh) e seu [log](../arquitetura/evidencias/desafio-5/mini-lab.log): Mosquitto real, dois dispositivos simulados com queda e duplicatas, e o gateway parado e religado.
