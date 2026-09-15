package br.edu.gamificacao.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Progresso de gamificacao de um aluno na plataforma de Educacao Continuada.
 * Concentra as regras de negocio de XP, nivel e badges, independente de
 * qualquer detalhe de persistencia ou framework.
 */
public class ProgressoGamificado implements Comparable<ProgressoGamificado> {

    private static final int XP_POR_NIVEL = 100;

    private static final TreeMap<Integer, String> BADGES_POR_LIMIAR = new TreeMap<>();
    static {
        BADGES_POR_LIMIAR.put(100, "Iniciante");
        BADGES_POR_LIMIAR.put(500, "Intermediario");
        BADGES_POR_LIMIAR.put(1000, "Avancado");
    }

    private final String nomeAluno;
    private int xp;
    private int nivel;
    private final Set<String> badges = new LinkedHashSet<>();

    public ProgressoGamificado(String nomeAluno) {
        if (nomeAluno == null || nomeAluno.isBlank()) {
            throw new IllegalArgumentException("nome do aluno nao pode ser vazio");
        }
        this.nomeAluno = nomeAluno;
        this.xp = 0;
        this.nivel = 1;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public int getXp() {
        return xp;
    }

    public int getNivel() {
        return nivel;
    }

    public Set<String> getBadges() {
        return Set.copyOf(badges);
    }

    public void adicionarXp(int pontos) {
        if (pontos <= 0) {
            throw new IllegalArgumentException("pontos de XP devem ser maiores que zero");
        }
        this.xp += pontos;
        this.nivel = calcularNivel(this.xp);
        atualizarBadges();
    }

    public boolean possuiBadge(String nomeBadge) {
        return badges.contains(nomeBadge);
    }

    public void reiniciarTrilha() {
        this.xp = 0;
        this.nivel = 1;
        this.badges.clear();
    }

    public static int calcularNivel(int xp) {
        return 1 + (xp / XP_POR_NIVEL);
    }

    private void atualizarBadges() {
        for (var entrada : BADGES_POR_LIMIAR.entrySet()) {
            if (xp >= entrada.getKey()) {
                badges.add(entrada.getValue());
            }
        }
    }

    @Override
    public int compareTo(ProgressoGamificado outro) {
        return Integer.compare(outro.xp, this.xp);
    }
}
