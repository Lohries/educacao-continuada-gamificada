package br.edu.gamificacao.dto;

import br.edu.gamificacao.entity.Curso;

public record CursoDTO(
        Long id,
        String titulo,
        String descricao,
        Integer cargaHoraria,
        Integer xpRecompensa
) {
    public static CursoDTO from(Curso curso) {
        return new CursoDTO(
                curso.getId(),
                curso.getTitulo(),
                curso.getDescricao(),
                curso.getCargaHoraria(),
                curso.getXpRecompensa()
        );
    }
}
