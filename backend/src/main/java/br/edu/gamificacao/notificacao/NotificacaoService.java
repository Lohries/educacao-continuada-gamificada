package br.edu.gamificacao.notificacao;

import br.edu.gamificacao.notificacao.internal.NotificacaoRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** API publica do modulo Notificacao: consulta do que ja foi enviado. */
@Service
public class NotificacaoService {

    public record NotificacaoDTO(Long matriculaId, String mensagem, Instant enviadaEm) {
    }

    private final NotificacaoRepository notificacoes;
    private final JdbcTemplate jdbc;

    public NotificacaoService(NotificacaoRepository notificacoes, JdbcTemplate jdbc, MeterRegistry metricas) {
        this.notificacoes = notificacoes;
        this.jdbc = jdbc;
        Gauge.builder("eventos.pendentes", this, NotificacaoService::eventosPendentes)
                .description("Publicacoes de evento cujo consumidor ainda nao concluiu (ADR-004)")
                .register(metricas);
    }

    public List<NotificacaoDTO> listarPorUsuario(Long usuarioId) {
        return notificacoes.findByUsuarioIdOrderByEnviadaEmAsc(usuarioId).stream()
                .map(n -> new NotificacaoDTO(n.getMatriculaId(), n.getMensagem(), n.getEnviadaEm()))
                .toList();
    }

    /** Eventos publicados cujo consumidor ainda nao concluiu (tabela do Spring Modulith). */
    public long eventosPendentes() {
        Long total = jdbc.queryForObject("select count(*) from event_publication where completion_date is null", Long.class);
        return total == null ? 0 : total;
    }
}
