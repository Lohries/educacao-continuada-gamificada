package br.edu.gamificacao.recomendacao;

import java.util.List;

/**
 * Resposta de recomendacao. {@code origem} e {@code degradado} deixam claro
 * para o front-end quando a lista veio do fallback local (IA indisponivel).
 */
public record Recomendacoes(String origem, boolean degradado, String modelo, List<CursoRecomendado> itens) {

    public record CursoRecomendado(Long cursoId, String titulo, double pontuacao, String motivo) {
    }
}
