package br.edu.gamificacao.matricula;

import java.time.Instant;

/**
 * Evento de dominio publicado quando um aluno conclui um curso. Os
 * consumidores (notificacao, analytics...) dependem deste contrato, nunca o
 * contrario (ADR-004).
 */
public record CursoConcluido(Long matriculaId, Long usuarioId, Long cursoId, String tituloCurso,
                             int xpGanho, Instant concluidoEm) {
}
