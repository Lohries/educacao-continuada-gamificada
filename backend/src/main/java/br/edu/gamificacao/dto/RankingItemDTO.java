package br.edu.gamificacao.dto;

public record RankingItemDTO(
        int posicao,
        Long usuarioId,
        String nome,
        Integer xp,
        Integer nivel
) {
}
