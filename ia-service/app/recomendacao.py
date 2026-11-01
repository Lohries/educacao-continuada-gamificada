"""Recomendacao de cursos por conteudo (RF03).

Modelo simples e explicavel: o perfil do aluno e o conjunto de termos dos
cursos que ele ja concluiu; cada curso nao concluido recebe a similaridade
de Jaccard entre seus termos e o perfil. Sem historico, recomenda primeiro
os cursos de menor XP (trilha de entrada).

O modelo tem versao propria (MODELO) porque evolui em ciclo separado do
Core (ADR-003).
"""
from pydantic import BaseModel, Field

from .texto import tokens

MODELO = "conteudo-jaccard-v1"


class Curso(BaseModel):
    id: int
    titulo: str
    descricao: str | None = ""
    xpRecompensa: int = 0


class PedidoRecomendacao(BaseModel):
    alunoId: int
    cursosConcluidos: list[int] = Field(default_factory=list)
    catalogo: list[Curso]
    limite: int = Field(default=3, ge=1, le=20)


class ItemRecomendado(BaseModel):
    cursoId: int
    titulo: str
    pontuacao: float
    motivo: str


class RespostaRecomendacao(BaseModel):
    modelo: str
    itens: list[ItemRecomendado]


def _termos(curso: Curso) -> set[str]:
    return set(tokens(f"{curso.titulo} {curso.descricao or ''}"))


def recomendar(pedido: PedidoRecomendacao) -> RespostaRecomendacao:
    concluidos = [c for c in pedido.catalogo if c.id in set(pedido.cursosConcluidos)]
    candidatos = [c for c in pedido.catalogo if c.id not in set(pedido.cursosConcluidos)]

    perfil: set[str] = set()
    for curso in concluidos:
        perfil |= _termos(curso)

    itens = []
    for curso in candidatos:
        termos = _termos(curso)
        if perfil:
            comuns = perfil & termos
            pontuacao = len(comuns) / len(perfil | termos) if termos else 0.0
            motivo = ("parecido com o que voce concluiu: " + ", ".join(sorted(comuns)[:4])) if comuns \
                else "amplia sua trilha com um tema novo"
        else:
            pontuacao = 1.0 / (1 + curso.xpRecompensa)
            motivo = "bom ponto de partida (menor XP da trilha)"
        itens.append(ItemRecomendado(cursoId=curso.id, titulo=curso.titulo,
                                     pontuacao=round(pontuacao, 4), motivo=motivo))

    itens.sort(key=lambda i: (-i.pontuacao, i.cursoId))
    return RespostaRecomendacao(modelo=MODELO, itens=itens[:pedido.limite])
