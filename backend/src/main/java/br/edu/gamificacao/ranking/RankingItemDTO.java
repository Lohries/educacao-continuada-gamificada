package br.edu.gamificacao.ranking;

public record RankingItemDTO(
        int posicao,
        Long usuarioId,
        String nome,
        Integer xp,
        Integer nivel
) {
}
