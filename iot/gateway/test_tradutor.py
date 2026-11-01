import json

import pytest

from tradutor import EventoInvalido, JaVistos, laboratorio_do_topico, traduzir

TOPICO = "lab/lab-redes/dispositivo/lab-redes-sensor1/eventos"


def payload(**campos):
    base = {"id": "e-1", "d": "lab-redes-sensor1", "t": "C", "a": 7, "ts": 1_793_000_000}
    base.update(campos)
    return json.dumps(base).encode()


def test_traduz_para_o_contrato_do_core():
    evento = traduzir(TOPICO, payload())
    assert evento == {
        "eventId": "e-1",
        "dispositivoId": "lab-redes-sensor1",
        "laboratorio": "lab-redes",
        "tipo": "CONCLUSAO_ATIVIDADE",
        "alunoId": 7,
        "ocorridoEm": "2026-10-26T07:33:20Z",
    }


@pytest.mark.parametrize("codigo,tipo", [("P", "PRESENCA"), ("I", "INICIO_ATIVIDADE"), ("C", "CONCLUSAO_ATIVIDADE")])
def test_mapeia_os_tipos(codigo, tipo):
    assert traduzir(TOPICO, payload(t=codigo))["tipo"] == tipo


@pytest.mark.parametrize("ruim", [b"nao-json", payload(t="X"), payload(a="sete"), json.dumps({"id": "x"}).encode()])
def test_recusa_payload_fora_do_contrato(ruim):
    with pytest.raises(EventoInvalido):
        traduzir(TOPICO, ruim)


def test_recusa_topico_fora_do_padrao():
    with pytest.raises(EventoInvalido):
        laboratorio_do_topico("lab/x/eventos")


def test_cache_de_vistos_descarta_reentrega_e_respeita_capacidade():
    vistos = JaVistos(capacidade=2)
    vistos.marcar("a")
    vistos.marcar("b")
    assert "a" in vistos
    vistos.marcar("c")
    assert "a" not in vistos and "b" in vistos and "c" in vistos
