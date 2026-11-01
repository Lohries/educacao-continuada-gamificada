package br.edu.gamificacao.notificacao.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    boolean existsByMatriculaId(Long matriculaId);

    List<Notificacao> findByUsuarioIdOrderByEnviadaEmAsc(Long usuarioId);
}
