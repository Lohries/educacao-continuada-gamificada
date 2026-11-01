"""Traducao do evento MQTT compacto do dispositivo para o contrato REST do Core (ADR-005).

Payload do dispositivo (curto de proposito: dispositivos com pouca memoria e banda):
    {"id": "<uuid>", "d": "<dispositivo>", "t": "P|I|C", "a": <alunoId>, "ts": <epoch s>}
Topico: lab/<laboratorio>/dispositivo/<dispositivo>/eventos
"""
import json
from collections import OrderedDict
from datetime import datetime, timezone

TIPOS = {"P": "PRESENCA", "I": "INICIO_ATIVIDADE", "C": "CONCLUSAO_ATIVIDADE"}


class EventoInvalido(ValueError):
    pass


def laboratorio_do_topico(topico: str) -> str:
    partes = topico.split("/")
    if len(partes) != 5 or partes[0] != "lab" or partes[2] != "dispositivo" or partes[4] != "eventos":
        raise EventoInvalido(f"topico fora do padrao: {topico}")
    return partes[1]


def traduzir(topico: str, payload: bytes) -> dict:
    laboratorio = laboratorio_do_topico(topico)
    try:
        bruto = json.loads(payload)
    except (ValueError, UnicodeDecodeError) as e:
        raise EventoInvalido(f"payload nao e JSON: {e}") from e
    faltando = [c for c in ("id", "d", "t", "a", "ts") if c not in bruto]
    if faltando:
        raise EventoInvalido(f"campos ausentes: {faltando}")
    if bruto["t"] not in TIPOS:
        raise EventoInvalido(f"tipo desconhecido: {bruto['t']}")
    if not isinstance(bruto["a"], int) or not isinstance(bruto["ts"], (int, float)):
        raise EventoInvalido("aluno e timestamp devem ser numericos")
    return {
        "eventId": str(bruto["id"]),
        "dispositivoId": str(bruto["d"]),
        "laboratorio": laboratorio,
        "tipo": TIPOS[bruto["t"]],
        "alunoId": bruto["a"],
        "ocorridoEm": datetime.fromtimestamp(bruto["ts"], tz=timezone.utc).isoformat().replace("+00:00", "Z"),
    }


class JaVistos:
    """Cache LRU dos eventIds ja encaminhados: corta a maioria das reentregas
    QoS 1 antes de chegar ao Core. A garantia final e a unique do Core."""

    def __init__(self, capacidade: int = 10_000):
        self.capacidade = capacidade
        self._ids: OrderedDict[str, None] = OrderedDict()

    def __contains__(self, event_id: str) -> bool:
        return event_id in self._ids

    def marcar(self, event_id: str) -> None:
        self._ids[event_id] = None
        self._ids.move_to_end(event_id)
        if len(self._ids) > self.capacidade:
            self._ids.popitem(last=False)
