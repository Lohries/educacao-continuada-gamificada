package br.edu.gamificacao.bdd;

import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Passos comuns de preparacao de cenario (criacao de aluno e de curso),
 * reaproveitados pelas tres User Stories do grupo.
 */
public class CommonSteps {

    @Autowired
    private ApiTestHelper apiTestHelper;

    @Autowired
    private CenarioContexto contexto;

    @Dado("que existe um aluno cadastrado chamado {string}")
    public void queExisteUmAlunoCadastradoChamado(String nome) {
        contexto.setUsuarioAtualId(apiTestHelper.criarAluno(nome, 0));
    }

    @Dado("que existe um aluno cadastrado chamado {string} com {int} XP")
    public void queExisteUmAlunoCadastradoChamadoComXp(String nome, int xp) {
        contexto.setUsuarioAtualId(apiTestHelper.criarAluno(nome, xp));
    }

    @Dado("existe um aluno cadastrado chamado {string} com {int} XP")
    public void existeUmAlunoCadastradoChamadoComXp(String nome, int xp) {
        apiTestHelper.criarAluno(nome, xp);
    }

    @Dado("existe o curso {string} disponivel na plataforma")
    public void existeOCursoDisponivelNaPlataforma(String titulo) {
        contexto.getCursoIdPorTitulo().put(titulo, apiTestHelper.obterOuCriarCurso(titulo, 100));
    }
}
