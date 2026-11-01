# ia-service

Serviço Python (FastAPI) de **recomendação de cursos** (RF03) e **assistente educacional com RAG** (RF04). Decisão em [ADR-003](../docs/adr/ADR-003-servico-python-de-ia.md).

| Rota | O que faz |
|---|---|
| `GET /health` | Estado e versões dos modelos |
| `POST /recomendacoes` | Recomendação por conteúdo (`conteudo-jaccard-v1`) a partir do histórico e do catálogo enviados pelo Core |
| `POST /assistente/perguntas` | RAG: recuperação BM25 na base de conhecimento + documentos enviados; gerador extrativo |

O serviço não tem estado e não acessa o banco do Core.

## Rodar

```bash
python -m venv .venv
.venv/Scripts/python -m pip install -r requirements-dev.txt   # Linux/macOS: .venv/bin/python
.venv/Scripts/python -m pytest -q
.venv/Scripts/python -m uvicorn app.main:app --port 8000
```

Docs interativas: http://localhost:8000/docs. No `docker compose`, o Core chega ao serviço em `http://ia-service:8000` (`IA_URL`).
