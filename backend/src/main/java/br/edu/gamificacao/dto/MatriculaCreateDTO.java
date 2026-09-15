package br.edu.gamificacao.dto;

import jakarta.validation.constraints.NotNull;

public record MatriculaCreateDTO(
        @NotNull(message = "usuarioId e obrigatorio") Long usuarioId,
        @NotNull(message = "cursoId e obrigatorio") Long cursoId
) {
}
