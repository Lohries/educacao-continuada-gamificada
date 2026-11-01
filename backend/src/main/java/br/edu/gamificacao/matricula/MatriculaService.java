package br.edu.gamificacao.matricula;

import br.edu.gamificacao.conquista.ConquistaService;
import br.edu.gamificacao.curso.Curso;
import br.edu.gamificacao.curso.CursoService;
import br.edu.gamificacao.usuario.UsuarioService;
import br.edu.gamificacao.usuario.Usuario;
import br.edu.gamificacao.matricula.internal.MatriculaRepository;
import br.edu.gamificacao.shared.RecursoNaoEncontradoException;
import br.edu.gamificacao.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Matricula de um aluno em um curso e conclusao da trilha, que concede XP
 * (US de Mathues de Luzia Souza - matricula / inicio de trilha; e de
 * Emanuel Ronaldo Gomes de Souza - ganho de XP ao concluir).
 */
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final ConquistaService conquistaService;

    public MatriculaService(MatriculaRepository matriculaRepository,
                             UsuarioService usuarioService,
                             CursoService cursoService,
                             ConquistaService conquistaService) {
        this.matriculaRepository = matriculaRepository;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.conquistaService = conquistaService;
    }

    @Transactional
    public Matricula matricular(MatriculaCreateDTO dto) {
        Usuario usuario = usuarioService.buscarPorId(dto.usuarioId());
        Curso curso = cursoService.buscarPorId(dto.cursoId());

        matriculaRepository.findByUsuarioIdAndCursoId(usuario.getId(), curso.getId())
                .ifPresent(m -> {
                    throw new RegraDeNegocioException("usuario ja matriculado neste curso");
                });

        Matricula matricula = Matricula.builder()
                .usuario(usuario)
                .curso(curso)
                .build();

        return matriculaRepository.save(matricula);
    }

    @Transactional
    public Matricula concluir(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("matricula nao encontrada: " + matriculaId));

        if (matricula.isConcluida()) {
            throw new RegraDeNegocioException("matricula ja concluida");
        }

        matricula.setConcluida(true);
        matricula.setDataConclusao(LocalDateTime.now());
        matriculaRepository.save(matricula);

        conquistaService.concederXp(matricula.getUsuario().getId(), matricula.getCurso().getXpRecompensa());

        return matricula;
    }

    public List<Matricula> listarPorUsuario(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId);
    }
}
