package br.edu.gamificacao.aprendizagem.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "eventos_aprendizagem")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoAprendizagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Gerado no dispositivo; unico para descartar reentregas (ADR-005). */
    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "dispositivo_id", nullable = false, length = 64)
    private String dispositivoId;

    @Column(nullable = false, length = 64)
    private String laboratorio;

    @Column(nullable = false, length = 32)
    private String tipo;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;
}
