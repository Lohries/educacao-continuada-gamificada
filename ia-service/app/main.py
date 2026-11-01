"""ia-service: recomendacao (ML) e assistente (RAG) da plataforma (ADR-003).

Os dois ficam no mesmo processo, mas em rotas independentes e sem estado
compartilhado, para poderem ser separados quando tiverem escala ou custo
diferentes.
"""
from fastapi import FastAPI

from .assistente import MODELO_GERADOR, Pergunta, Resposta, responder
from .recomendacao import MODELO, PedidoRecomendacao, RespostaRecomendacao, recomendar

app = FastAPI(title="ia-service", version="1.0.0",
              description="Recomendacao de cursos e assistente educacional (RAG)")


@app.get("/health")
def health():
    return {"status": "UP", "modeloRecomendacao": MODELO, "geradorAssistente": MODELO_GERADOR}


@app.post("/recomendacoes", response_model=RespostaRecomendacao)
def post_recomendacoes(pedido: PedidoRecomendacao):
    return recomendar(pedido)


@app.post("/assistente/perguntas", response_model=Resposta)
def post_pergunta(pergunta: Pergunta):
    return responder(pergunta)
