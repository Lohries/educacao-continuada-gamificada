package br.edu.gamificacao.service;

import br.edu.gamificacao.dto.MatriculaCreateDTO;
import br.edu.gamificacao.entity.Curso;
import br.edu.gamificacao.entity.Matricula;
import br.edu.gamificacao.entity.Usuario;
import br.edu.gamificacao.repository.MatriculaRepository;
import br.edu.gamificacao.service.exception.RecursoNaoEncontradoException;
import br.edu.gamificacao.service.exception.RegraDeNegocioException;
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

    public MatriculaService(MatriculaRepository matriculaRepository,
                             UsuarioService usuarioService,
                             CursoService cursoService) {
        this.matriculaRepository = matriculaRepository;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
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

        usuarioService.adicionarXp(matricula.getUsuario().getId(), matricula.getCurso().getXpRecompensa());

        return matricula;
    }

    public List<Matricula> listarPorUsuario(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId);
    }
}
