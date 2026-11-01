"""Gateway IoT: consome os eventos dos laboratorios no broker MQTT, traduz e
encaminha ao Core por REST (ADR-005).

- Sessao persistente (clean_session=False) + assinatura QoS 1: se o gateway
  cair, o broker guarda as mensagens e entrega quando ele voltar.
- Ack manual: a mensagem so e confirmada ao broker depois que o Core aceitou
  (201 novo ou 200 reentrega). Se o Core nao responder, a mensagem fica sem
  ack e o broker a reentrega na proxima conexao.
- Duplicatas: cache local de eventIds + unique no Core.
"""
import json
import logging
import os
import time
import urllib.error
import urllib.request

import paho.mqtt.client as mqtt
from prometheus_client import Counter, start_http_server

from tradutor import EventoInvalido, JaVistos, traduzir

MQTT_HOST = os.getenv("MQTT_HOST", "localhost")
MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))
CORE_URL = os.getenv("CORE_URL", "http://localhost:8080")
TOPICO = "lab/+/dispositivo/+/eventos"
PORTA_METRICAS = int(os.getenv("PORTA_METRICAS", "9101"))

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s gateway: %(message)s")
log = logging.getLogger("gateway")

RECEBIDOS = Counter("iot_eventos_recebidos", "Mensagens MQTT recebidas")
ENCAMINHADOS = Counter("iot_eventos_encaminhados", "Eventos novos aceitos pelo Core")
DUPLICADOS = Counter("iot_eventos_duplicados", "Reentregas descartadas (cache do gateway ou Core)")
INVALIDOS = Counter("iot_eventos_invalidos", "Mensagens fora do contrato, descartadas")
FALHAS_CORE = Counter("iot_falhas_core", "Tentativas de entrega ao Core que falharam")

vistos = JaVistos()


def enviar_ao_core(evento: dict, tentativas: int = 4) -> int | None:
    corpo = json.dumps(evento).encode()
    for n in range(tentativas):
        pedido = urllib.request.Request(f"{CORE_URL}/api/eventos-aprendizagem", data=corpo,
                                        headers={"Content-Type": "application/json"}, method="POST")
        try:
            with urllib.request.urlopen(pedido, timeout=3) as resp:
                return resp.status
        except urllib.error.HTTPError as e:
            if 400 <= e.code < 500:
                return e.code
            FALHAS_CORE.inc()
        except (urllib.error.URLError, TimeoutError):
            FALHAS_CORE.inc()
        time.sleep(0.5 * 2 ** n)
    return None


def ao_receber(cliente: mqtt.Client, _userdata, msg: mqtt.MQTTMessage) -> None:
    RECEBIDOS.inc()
    try:
        evento = traduzir(msg.topic, msg.payload)
    except EventoInvalido as e:
        INVALIDOS.inc()
        log.warning("descartado (invalido): %s", e)
        cliente.ack(msg.mid, msg.qos)
        return

    if evento["eventId"] in vistos:
        DUPLICADOS.inc()
        log.info("duplicado descartado no gateway: %s", evento["eventId"])
        cliente.ack(msg.mid, msg.qos)
        return

    status = enviar_ao_core(evento)
    if status is None:
        log.error("Core indisponivel; %s sem ack, sera reentregue pelo broker", evento["eventId"])
        return
    if status == 201:
        ENCAMINHADOS.inc()
        log.info("encaminhado: %s %s aluno=%s lab=%s", evento["eventId"], evento["tipo"], evento["alunoId"], evento["laboratorio"])
    elif status == 200:
        DUPLICADOS.inc()
        log.info("duplicado descartado no Core: %s", evento["eventId"])
    else:
        INVALIDOS.inc()
        log.warning("Core recusou %s com HTTP %s", evento["eventId"], status)
    vistos.marcar(evento["eventId"])
    cliente.ack(msg.mid, msg.qos)


def ao_conectar(cliente: mqtt.Client, _userdata, _flags, reason_code, _props) -> None:
    log.info("conectado ao broker %s:%s (%s); assinando %s", MQTT_HOST, MQTT_PORT, reason_code, TOPICO)
    cliente.subscribe(TOPICO, qos=1)


def main() -> None:
    start_http_server(PORTA_METRICAS)
    cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="gateway-iot",
                          clean_session=False, manual_ack=True)
    cliente.on_connect = ao_conectar
    cliente.on_message = ao_receber
    cliente.reconnect_delay_set(min_delay=1, max_delay=30)
    cliente.connect(MQTT_HOST, MQTT_PORT, keepalive=30)
    cliente.loop_forever(retry_first_connection=True)


if __name__ == "__main__":
    main()
