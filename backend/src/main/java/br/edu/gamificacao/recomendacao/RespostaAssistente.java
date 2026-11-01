package br.edu.gamificacao.recomendacao;

import java.util.List;

public record RespostaAssistente(String origem, boolean degradado, String resposta, List<String> fontes) {
}
