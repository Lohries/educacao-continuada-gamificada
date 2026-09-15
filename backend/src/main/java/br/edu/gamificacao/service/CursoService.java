package br.edu.gamificacao.service;

import br.edu.gamificacao.dto.CursoCreateDTO;
import br.edu.gamificacao.entity.Curso;
import br.edu.gamificacao.repository.CursoRepository;
import br.edu.gamificacao.service.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    public Curso criar(CursoCreateDTO dto) {
        Curso curso = Curso.builder()
                .titulo(dto.titulo())
                .descricao(dto.descricao())
                .cargaHoraria(dto.cargaHoraria())
                .xpRecompensa(dto.xpRecompensa())
                .build();

        return cursoRepository.save(curso);
    }

    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("curso nao encontrado: " + id));
    }

    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }
}
