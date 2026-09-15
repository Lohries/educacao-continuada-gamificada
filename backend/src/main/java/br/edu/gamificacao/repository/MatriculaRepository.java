package br.edu.gamificacao.repository;

import br.edu.gamificacao.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByUsuarioId(Long usuarioId);

    Optional<Matricula> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);
}
