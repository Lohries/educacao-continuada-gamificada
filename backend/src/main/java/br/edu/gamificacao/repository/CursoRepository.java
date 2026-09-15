package br.edu.gamificacao.repository;

import br.edu.gamificacao.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
