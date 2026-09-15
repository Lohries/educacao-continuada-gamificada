package br.edu.gamificacao.domain;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Classe de dominio (fase RED do TDD): assinaturas definidas a partir dos
 * testes, sem implementacao de negocio ainda.
 */
public class ProgressoGamificado implements Comparable<ProgressoGamificado> {

    public ProgressoGamificado(String nomeAluno) {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public int getXp() {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public int getNivel() {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public Set<String> getBadges() {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public void adicionarXp(int pontos) {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public boolean possuiBadge(String nomeBadge) {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public void reiniciarTrilha() {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    public static int calcularNivel(int xp) {
        throw new UnsupportedOperationException("ainda nao implementado");
    }

    @Override
    public int compareTo(ProgressoGamificado outro) {
        throw new UnsupportedOperationException("ainda nao implementado");
    }
}
