package br.edu.gamificacao.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Step definitions da US de Mathues de Luzia Souza (github.com/mathzk):
 * matricula do aluno em um curso.
 */
public class MatriculaSteps {

    @Autowired
    private ApiTestHelper apiTestHelper;

    @Autowired
    private CenarioContexto contexto;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> ultimaResposta;

    @Dado("o aluno ja esta matriculado no curso {string}")
    public void oAlunoJaEstaMatriculadoNoCurso(String tituloCurso) {
        Long cursoId = contexto.getCursoIdPorTitulo().get(tituloCurso);
        apiTestHelper.matricular(contexto.getUsuarioAtualId(), cursoId);
    }

    @Quando("o aluno se matricula no curso {string}")
    public void oAlunoSeMatriculaNoCurso(String tituloCurso) {
        Long cursoId = contexto.getCursoIdPorTitulo().get(tituloCurso);
        ultimaResposta = apiTestHelper.matricular(contexto.getUsuarioAtualId(), cursoId);
    }

    @Quando("o aluno tenta se matricular novamente no curso {string}")
    public void oAlunoTentaSeMatricularNovamenteNoCurso(String tituloCurso) {
        oAlunoSeMatriculaNoCurso(tituloCurso);
    }

    @Entao("a matricula deve ser criada com sucesso")
    public void aMatriculaDeveSerCriadaComSucesso() {
        assertEquals(201, ultimaResposta.getStatusCode().value());
        assertNotNull(ultimaResposta.getBody());
    }

    @Entao("a matricula deve estar com o status {string}")
    public void aMatriculaDeveEstarComOStatus(String statusEsperado) {
        boolean concluida = corpoComoJson().get("concluida").asBoolean();
        if ("nao concluida".equals(statusEsperado)) {
            assertFalse(concluida);
        } else {
            assertEquals("concluida", statusEsperado, "status esperado desconhecido: " + statusEsperado);
        }
    }

    @Entao("a plataforma deve recusar a nova matricula")
    public void aPlataformaDeveRecusarANovaMatricula() {
        assertEquals(422, ultimaResposta.getStatusCode().value());
    }

    private JsonNode corpoComoJson() {
        try {
            return objectMapper.readTree(ultimaResposta.getBody());
        } catch (IOException e) {
            fail("resposta da API nao e um JSON valido: " + ultimaResposta.getBody());
            return null;
        }
    }
}
