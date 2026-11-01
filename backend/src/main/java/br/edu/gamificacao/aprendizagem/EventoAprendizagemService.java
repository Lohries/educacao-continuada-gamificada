package br.edu.gamificacao.aprendizagem;

import br.edu.gamificacao.aprendizagem.internal.EventoAprendizagem;
import br.edu.gamificacao.aprendizagem.internal.EventoAprendizagemRepository;
import br.edu.gamificacao.usuario.UsuarioService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Entrada dos eventos de aprendizagem vindos dos laboratorios (IoT). O Core
 * nao conhece MQTT nem dispositivos: recebe o evento ja traduzido pelo
 * gateway e o registra de forma idempotente pelo {@code eventId} (ADR-005).
 */
@Service
public class EventoAprendizagemService {

    public record NovoEvento(
            @NotBlank String eventId,
            @NotBlank String dispositivoId,
            @NotBlank String laboratorio,
            @NotBlank @Pattern(regexp = "PRESENCA|INICIO_ATIVIDADE|CONCLUSAO_ATIVIDADE") String tipo,
            @NotNull Long alunoId,
            @NotNull Instant ocorridoEm) {
    }

    public record EventoDTO(String eventId, String dispositivoId, String laboratorio, String tipo,
                            Long alunoId, Instant ocorridoEm) {
    }

    /** {@code novo=false} indica reentrega de um evento ja registrado. */
    public record Registro(boolean novo, EventoDTO evento) {
    }

    private final EventoAprendizagemRepository eventos;
    private final UsuarioService usuarioService;

    public EventoAprendizagemService(EventoAprendizagemRepository eventos, UsuarioService usuarioService) {
        this.eventos = eventos;
        this.usuarioService = usuarioService;
    }

    /**
     * Sem transacao externa de proposito: o save roda na transacao do
     * repositorio, e a violacao da unique (entrega concorrente) pode ser
     * tratada aqui como reentrega.
     */
    public Registro registrar(NovoEvento novo) {
        EventoDTO dto = new EventoDTO(novo.eventId(), novo.dispositivoId(), novo.laboratorio(), novo.tipo(),
                novo.alunoId(), novo.ocorridoEm());
        if (eventos.existsByEventId(novo.eventId())) {
            return new Registro(false, dto);
        }
        usuarioService.buscarPorId(novo.alunoId());
        try {
            eventos.saveAndFlush(EventoAprendizagem.builder()
                    .eventId(novo.eventId())
                    .dispositivoId(novo.dispositivoId())
                    .laboratorio(novo.laboratorio())
                    .tipo(novo.tipo())
                    .alunoId(novo.alunoId())
                    .ocorridoEm(novo.ocorridoEm())
                    .recebidoEm(Instant.now())
                    .build());
            return new Registro(true, dto);
        } catch (DataIntegrityViolationException corrida) {
            // duas entregas simultaneas do mesmo evento: a unique constraint decide
            return new Registro(false, dto);
        }
    }

    public List<EventoDTO> listarPorAluno(Long alunoId) {
        return eventos.findByAlunoIdOrderByOcorridoEmAsc(alunoId).stream()
                .map(e -> new EventoDTO(e.getEventId(), e.getDispositivoId(), e.getLaboratorio(), e.getTipo(),
                        e.getAlunoId(), e.getOcorridoEm()))
                .toList();
    }
}
