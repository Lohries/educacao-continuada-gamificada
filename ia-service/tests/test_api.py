from fastapi.testclient import TestClient

from app.main import app

cliente = TestClient(app)

CATALOGO = [
    {"id": 1, "titulo": "Fundamentos de Spring Boot", "descricao": "APIs REST com Spring Boot", "xpRecompensa": 100},
    {"id": 2, "titulo": "TDD na Pratica", "descricao": "Test Driven Development com JUnit e Mockito", "xpRecompensa": 150},
    {"id": 3, "titulo": "BDD e ATDD com Cucumber", "descricao": "Criterios de aceite executaveis", "xpRecompensa": 200},
    {"id": 4, "titulo": "Spring Boot Avancado", "descricao": "Seguranca e testes de APIs REST com Spring", "xpRecompensa": 250},
]


def test_health_informa_versoes_dos_modelos():
    corpo = cliente.get("/health").json()
    assert corpo["status"] == "UP"
    assert corpo["modeloRecomendacao"] == "conteudo-jaccard-v1"


def test_recomenda_curso_parecido_com_o_concluido():
    corpo = cliente.post("/recomendacoes", json={"alunoId": 7, "cursosConcluidos": [1], "catalogo": CATALOGO}).json()
    ids = [i["cursoId"] for i in corpo["itens"]]
    assert ids[0] == 4
    assert 1 not in ids
    assert "spring" in corpo["itens"][0]["motivo"]


def test_sem_historico_recomenda_menor_xp_primeiro():
    corpo = cliente.post("/recomendacoes", json={"alunoId": 7, "catalogo": CATALOGO, "limite": 2}).json()
    assert [i["cursoId"] for i in corpo["itens"]] == [1, 2]


def test_aluno_que_concluiu_tudo_nao_recebe_recomendacao():
    corpo = cliente.post("/recomendacoes", json={"alunoId": 7, "cursosConcluidos": [1, 2, 3, 4], "catalogo": CATALOGO}).json()
    assert corpo["itens"] == []


def test_pedido_invalido_e_recusado():
    assert cliente.post("/recomendacoes", json={"alunoId": 7}).status_code == 422


def test_assistente_recupera_a_fonte_certa():
    corpo = cliente.post("/assistente/perguntas", json={"pergunta": "Quantos XP preciso para a badge Avancado?"}).json()
    assert corpo["fontes"][0]["id"] == "faq-badges"
    assert "1000 XP" in corpo["resposta"]


def test_assistente_usa_documentos_enviados_pelo_core():
    corpo = cliente.post("/assistente/perguntas", json={
        "pergunta": "Qual curso fala de Cucumber?",
        "documentos": [{"id": "curso-3", "titulo": "BDD e ATDD com Cucumber", "texto": "Criterios de aceite executaveis"}],
    }).json()
    assert corpo["fontes"][0]["id"] == "curso-3"


def test_assistente_sem_resultado_responde_sem_inventar():
    corpo = cliente.post("/assistente/perguntas", json={"pergunta": "previsao do tempo amanha"}).json()
    assert corpo["fontes"] == []
    assert corpo["resposta"].startswith("Nao encontrei")
