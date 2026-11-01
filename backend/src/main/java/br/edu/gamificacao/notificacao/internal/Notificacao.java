package br.edu.gamificacao.notificacao.internal;

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
@Table(name = "notificacoes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Unico: um evento reprocessado nao gera notificacao duplicada. */
    @Column(name = "matricula_id", nullable = false, unique = true)
    private Long matriculaId;

    @Column(nullable = false, length = 255)
    private String mensagem;

    @Column(name = "enviada_em", nullable = false)
    private Instant enviadaEm;
}
