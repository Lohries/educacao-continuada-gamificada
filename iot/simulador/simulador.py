"""Simulador de dispositivos de laboratorio (ADR-005).

Cada dispositivo publica eventos compactos com QoS 1 em
lab/<laboratorio>/dispositivo/<id>/eventos. Opcionalmente:
  --duplicar N   reenvia N eventos com o mesmo id (reentrega do dispositivo)
  --queda        derruba a conexao de um dispositivo no meio da rajada; os
                 eventos gerados offline vao para a fila local do dispositivo
                 (store-and-forward) e saem quando ele reconecta

Uso: python simulador.py --alunos 1 2 3 --eventos 4 --duplicar 2 --queda
"""
import argparse
import json
import random
import time
import uuid
from collections import deque

import paho.mqtt.client as mqtt

TIPOS = ["P", "I", "C"]


class Dispositivo:
    def __init__(self, host: str, porta: int, laboratorio: str, n: int):
        self.nome = f"{laboratorio}-sensor{n}"
        self.topico = f"lab/{laboratorio}/dispositivo/{self.nome}/eventos"
        self.fila_local: deque[dict] = deque()
        self.infos: list[mqtt.MQTTMessageInfo] = []
        self.bytes = 0
        self.cliente = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=self.nome, clean_session=False)
        self.cliente.connect(host, porta, keepalive=30)
        self.cliente.loop_start()

    def publicar(self, evento: dict) -> None:
        if not self.cliente.is_connected():
            self.fila_local.append(evento)
            return
        corpo = json.dumps(evento, separators=(",", ":"))
        self.bytes += len(corpo)
        self.infos.append(self.cliente.publish(self.topico, corpo, qos=1))

    def cair(self) -> None:
        self.cliente.loop_stop()
        self.cliente.disconnect()

    def voltar(self) -> int:
        self.cliente.reconnect()
        self.cliente.loop_start()
        for _ in range(50):
            if self.cliente.is_connected():
                break
            time.sleep(0.1)
        pendentes = len(self.fila_local)
        while self.fila_local:
            self.publicar(self.fila_local.popleft())
        return pendentes

    def encerrar(self) -> int:
        for info in self.infos:
            info.wait_for_publish(timeout=10)
        confirmados = sum(1 for info in self.infos if info.is_published())
        self.cliente.loop_stop()
        self.cliente.disconnect()
        return confirmados


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--host", default="localhost")
    p.add_argument("--porta", type=int, default=1883)
    p.add_argument("--laboratorio", default="lab-redes")
    p.add_argument("--dispositivos", type=int, default=2)
    p.add_argument("--alunos", type=int, nargs="+", required=True)
    p.add_argument("--eventos", type=int, default=3, help="eventos por dispositivo")
    p.add_argument("--duplicar", type=int, default=0)
    p.add_argument("--queda", action="store_true")
    p.add_argument("--semente", type=int, default=42)
    a = p.parse_args()
    random.seed(a.semente)

    dispositivos = [Dispositivo(a.host, a.porta, a.laboratorio, n + 1) for n in range(a.dispositivos)]
    time.sleep(0.5)
    gerados = []

    for i in range(a.eventos):
        for disp in dispositivos:
            if a.queda and i == a.eventos // 2 and disp is dispositivos[0]:
                disp.cair()
                print(f"[queda] {disp.nome} sem conexao; eventos seguintes vao para a fila local")
            evento = {"id": str(uuid.uuid4()), "d": disp.nome, "t": TIPOS[i % 3],
                      "a": random.choice(a.alunos), "ts": int(time.time())}
            disp.publicar(evento)
            gerados.append((disp, evento))

    if a.queda:
        time.sleep(1)
        enviados = dispositivos[0].voltar()
        print(f"[queda] {dispositivos[0].nome} reconectado; {enviados} eventos da fila local publicados")

    for disp, evento in random.sample(gerados, a.duplicar):
        disp.publicar(evento)
        print(f"[duplicado] reenviando {evento['id']} de {disp.nome}")

    publicados = sum(len(d.infos) for d in dispositivos)
    bytes_total = sum(d.bytes for d in dispositivos)
    confirmados = sum(d.encerrar() for d in dispositivos)
    print(f"gerados={len(gerados)} publicados={publicados} (com {a.duplicar} duplicados) "
          f"confirmados_pelo_broker={confirmados} bytes_medios_por_mensagem={bytes_total // max(publicados, 1)}")


if __name__ == "__main__":
    main()
