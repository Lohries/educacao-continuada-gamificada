package br.edu.gamificacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CursoCreateDTO(
        @NotBlank(message = "titulo e obrigatorio") String titulo,
        String descricao,
        @Positive(message = "carga horaria deve ser positiva") Integer cargaHoraria,
        @Positive(message = "xp de recompensa deve ser positivo") Integer xpRecompensa
) {
}
