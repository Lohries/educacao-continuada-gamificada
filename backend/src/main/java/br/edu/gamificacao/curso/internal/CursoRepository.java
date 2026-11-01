package br.edu.gamificacao.curso.internal;

import br.edu.gamificacao.curso.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
