package br.edu.gamificacao.bdd;

import br.edu.gamificacao.dto.CursoCreateDTO;
import br.edu.gamificacao.dto.CursoDTO;
import br.edu.gamificacao.dto.MatriculaCreateDTO;
import br.edu.gamificacao.dto.RankingItemDTO;
import br.edu.gamificacao.dto.UsuarioCreateDTO;
import br.edu.gamificacao.dto.UsuarioDTO;
import br.edu.gamificacao.entity.Curso;
import br.edu.gamificacao.service.CursoService;
import br.edu.gamificacao.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Cliente HTTP de apoio aos testes de aceitacao (Cucumber), usado pelas
 * classes de step definitions para conversar com a API REST real da
 * aplicacao (subida em porta aleatoria pelo @SpringBootTest). O
 * TestRestTemplate injetado pelo Spring Boot ja resolve caminhos relativos
 * para o host/porta corretos, entao nao ha necessidade de @LocalServerPort
 * aqui (isso evitaria a resolucao do placeholder antes do servidor subir).
 *
 * Nao e anotada com @Component de proposito: e registrada explicitamente
 * como bean apenas dentro de {@link CucumberSpringConfiguration}, para nao
 * ser varrida pelo component scan da aplicacao principal (que exigiria um
 * TestRestTemplate mesmo em testes @SpringBootTest sem servidor web, como
 * o EducacaoContinuadaGamificadaApplicationTests padrao).
 */
public class ApiTestHelper {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CursoService cursoService;


    public Long criarAluno(String nome, int xpInicial) {
        String emailUnico = nome.toLowerCase().replace(" ", ".") + "." + UUID.randomUUID() + "@teste.com";

        ResponseEntity<UsuarioDTO> resposta = restTemplate.postForEntity(
                "/api/usuarios", new UsuarioCreateDTO(nome, emailUnico), UsuarioDTO.class);

        if (!resposta.getStatusCode().is2xxSuccessful() || resposta.getBody() == null) {
            throw new IllegalStateException("falha ao cadastrar aluno de teste: " + resposta.getStatusCode());
        }

        Long usuarioId = resposta.getBody().id();
        if (xpInicial > 0) {
            usuarioService.adicionarXp(usuarioId, xpInicial);
        }

        return usuarioId;
    }

    public Long obterOuCriarCurso(String titulo, int xpRecompensaSeNovo) {
        Curso existente = cursoService.listarTodos().stream()
                .filter(c -> c.getTitulo().equals(titulo))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            return existente.getId();
        }

        ResponseEntity<CursoDTO> resposta = restTemplate.postForEntity(
                "/api/cursos",
                new CursoCreateDTO(titulo, "curso de teste", 10, xpRecompensaSeNovo),
                CursoDTO.class);

        if (!resposta.getStatusCode().is2xxSuccessful() || resposta.getBody() == null) {
            throw new IllegalStateException("falha ao cadastrar curso de teste: " + resposta.getStatusCode());
        }

        return resposta.getBody().id();
    }

    /**
     * Retorna a resposta crua (String) propositalmente: em cenarios de
     * recusa de negocio (ex.: matricula duplicada) o corpo de erro tem um
     * formato diferente do MatriculaDTO, e nao queremos que a falha de
     * desserializacao mascare a asercao real do teste.
     */
    public ResponseEntity<String> matricular(Long usuarioId, Long cursoId) {
        return restTemplate.postForEntity(
                "/api/matriculas", new MatriculaCreateDTO(usuarioId, cursoId), String.class);
    }

    public ResponseEntity<String> concluirMatricula(Long matriculaId) {
        return restTemplate.postForEntity("/api/matriculas/" + matriculaId + "/concluir", null, String.class);
    }

    public ResponseEntity<UsuarioDTO> buscarUsuario(Long usuarioId) {
        return restTemplate.getForEntity("/api/usuarios/" + usuarioId, UsuarioDTO.class);
    }

    public ResponseEntity<RankingItemDTO[]> buscarRanking() {
        return restTemplate.getForEntity("/api/usuarios/ranking", RankingItemDTO[].class);
    }
}
