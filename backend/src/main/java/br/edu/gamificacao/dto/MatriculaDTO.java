package br.edu.gamificacao.dto;

import br.edu.gamificacao.entity.Matricula;

import java.time.LocalDateTime;

public record MatriculaDTO(
        Long id,
        Long usuarioId,
        String nomeUsuario,
        Long cursoId,
        String tituloCurso,
        LocalDateTime dataMatricula,
        boolean concluida,
        LocalDateTime dataConclusao
) {
    public static MatriculaDTO from(Matricula matricula) {
        return new MatriculaDTO(
                matricula.getId(),
                matricula.getUsuario().getId(),
                matricula.getUsuario().getNome(),
                matricula.getCurso().getId(),
                matricula.getCurso().getTitulo(),
                matricula.getDataMatricula(),
                matricula.isConcluida(),
                matricula.getDataConclusao()
        );
    }
}
