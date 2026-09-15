package br.edu.gamificacao.bdd;

import br.edu.gamificacao.dto.UsuarioDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Step definitions da US de Emanuel Ronaldo Gomes de Souza (github.com/ManuJoestar):
 * ganho de XP e evolucao de nivel ao concluir um curso.
 */
public class XpNivelSteps {

    @Autowired
    private ApiTestHelper apiTestHelper;

    @Autowired
    private CenarioContexto contexto;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Dado("o aluno esta matriculado no curso {string} que vale {int} XP")
    public void oAlunoEstaMatriculadoNoCursoQueValeXp(String tituloCurso, int xpRecompensa) {
        Long cursoId = apiTestHelper.obterOuCriarCurso(tituloCurso, xpRecompensa);
        contexto.getCursoIdPorTitulo().put(tituloCurso, cursoId);

        ResponseEntity<String> resposta = apiTestHelper.matricular(contexto.getUsuarioAtualId(), cursoId);
        assertEquals(201, resposta.getStatusCode().value(), "falha ao matricular aluno de teste");

        Long matriculaId = corpoComoJson(resposta).get("id").asLong();
        contexto.getMatriculaIdPorCurso().put(tituloCurso, matriculaId);
    }

    @Quando("o aluno conclui a matricula no curso {string}")
    public void oAlunoConcluiAMatriculaNoCurso(String tituloCurso) {
        Long matriculaId = contexto.getMatriculaIdPorCurso().get(tituloCurso);
        ResponseEntity<String> resposta = apiTestHelper.concluirMatricula(matriculaId);
        assertEquals(200, resposta.getStatusCode().value(), "falha ao concluir matricula de teste");
    }

    @Entao("o aluno deve possuir {int} XP")
    public void oAlunoDevePossuirXp(int xpEsperado) {
        UsuarioDTO usuario = apiTestHelper.buscarUsuario(contexto.getUsuarioAtualId()).getBody();
        assertEquals(xpEsperado, usuario.xp());
    }

    @Entao("o nivel do aluno deve ser {int}")
    public void oNivelDoAlunoDeveSer(int nivelEsperado) {
        UsuarioDTO usuario = apiTestHelper.buscarUsuario(contexto.getUsuarioAtualId()).getBody();
        assertEquals(nivelEsperado, usuario.nivel());
    }

    private JsonNode corpoComoJson(ResponseEntity<String> resposta) {
        try {
            return objectMapper.readTree(resposta.getBody());
        } catch (IOException e) {
            fail("resposta da API nao e um JSON valido: " + resposta.getBody());
            return null;
        }
    }
}
