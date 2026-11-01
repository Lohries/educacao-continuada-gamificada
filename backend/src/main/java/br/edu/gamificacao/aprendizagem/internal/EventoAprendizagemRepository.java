package br.edu.gamificacao.aprendizagem.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAprendizagemRepository extends JpaRepository<EventoAprendizagem, Long> {

    boolean existsByEventId(String eventId);

    List<EventoAprendizagem> findByAlunoIdOrderByOcorridoEmAsc(Long alunoId);
}
