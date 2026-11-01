"""Assistente educacional com RAG (RF04).

Recuperacao: BM25 sobre a base de conhecimento do servico (base_conhecimento.json)
mais os documentos enviados na pergunta (ex.: catalogo de cursos).
Geracao: o gerador padrao e extrativo (monta a resposta com os trechos
recuperados e cita as fontes). Um gerador com LLM implementa a mesma
interface Gerador; ele fica fora do laboratorio para nao depender de chave
de API (ADR-003).
"""
import json
import math
from collections import Counter
from pathlib import Path
from typing import Protocol

from pydantic import BaseModel, Field

from .texto import tokens

BASE = json.loads((Path(__file__).parent / "base_conhecimento.json").read_text(encoding="utf-8"))


class Documento(BaseModel):
    id: str
    titulo: str
    texto: str


class Pergunta(BaseModel):
    pergunta: str = Field(min_length=3)
    documentos: list[Documento] = Field(default_factory=list)
    k: int = Field(default=2, ge=1, le=5)


class Fonte(BaseModel):
    id: str
    titulo: str
    pontuacao: float


class Resposta(BaseModel):
    resposta: str
    fontes: list[Fonte]
    gerador: str


class Gerador(Protocol):
    nome: str

    def gerar(self, pergunta: str, trechos: list[Documento]) -> str: ...


class GeradorExtrativo:
    nome = "extrativo-v1"

    def gerar(self, pergunta: str, trechos: list[Documento]) -> str:
        if not trechos:
            return "Nao encontrei nada sobre isso na base da plataforma. Tente perguntar sobre XP, niveis, badges, ranking ou cursos."
        return " ".join(t.texto for t in trechos)


MODELO_GERADOR = GeradorExtrativo.nome


def bm25(consulta: list[str], docs: list[Documento], k1: float = 1.5, b: float = 0.75) -> list[float]:
    corpus = [tokens(f"{d.titulo} {d.texto}") for d in docs]
    n = len(corpus)
    media = sum(len(c) for c in corpus) / n if n else 0
    df = Counter(t for c in corpus for t in set(c))
    notas = []
    for termos in corpus:
        freq = Counter(termos)
        nota = 0.0
        for t in consulta:
            if t not in freq:
                continue
            idf = math.log(1 + (n - df[t] + 0.5) / (df[t] + 0.5))
            tf = freq[t]
            nota += idf * tf * (k1 + 1) / (tf + k1 * (1 - b + b * len(termos) / (media or 1)))
        notas.append(nota)
    return notas


def responder(pergunta: Pergunta, gerador: Gerador | None = None) -> Resposta:
    gerador = gerador or GeradorExtrativo()
    docs = [Documento(**d) for d in BASE] + pergunta.documentos
    notas = bm25(tokens(pergunta.pergunta), docs)
    ordem = sorted(range(len(docs)), key=lambda i: -notas[i])
    escolhidos = [i for i in ordem[:pergunta.k] if notas[i] > 0]
    trechos = [docs[i] for i in escolhidos]
    return Resposta(
        resposta=gerador.gerar(pergunta.pergunta, trechos),
        fontes=[Fonte(id=docs[i].id, titulo=docs[i].titulo, pontuacao=round(notas[i], 3)) for i in escolhidos],
        gerador=gerador.nome,
    )
