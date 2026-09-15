package br.edu.gamificacao.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da classe de dominio ProgressoGamificado (exercicio TDD, nos moldes
 * do exercicio da calculadora): RED -> GREEN -> BLUE.
 *
 * Autoria dos casos de teste:
 * - Mathues de Luzia Souza (github.com/mathzk): matricula / inicio de trilha
 * - Emanuel Ronaldo Gomes de Souza (github.com/ManuJoestar): XP e niveis
 * - Luis Henrique Telo Ladeira Mota (github.com/Lohries): badges e ranking
 */
class ProgressoGamificadoTest {

    private ProgressoGamificado progresso;

    @BeforeEach
    void setUp() {
        progresso = new ProgressoGamificado("Aluno Teste");
    }

    // ---- Mathues de Luzia Souza ----

    @Test
    @DisplayName("Um progresso recem-criado comeca com XP zero e nivel 1")
    void deveIniciarComXpZeroENivelUm() {
        assertEquals(0, progresso.getXp());
        assertEquals(1, progresso.getNivel());
    }

    @Test
    @DisplayName("O nome do aluno informado na criacao deve ser recuperavel")
    void deveExporNomeDoAluno() {
        assertEquals("Aluno Teste", progresso.getNomeAluno());
    }

    @Test
    @DisplayName("Um progresso recem-criado nao possui badges")
    void deveIniciarSemBadges() {
        assertTrue(progresso.getBadges().isEmpty());
    }

    @Test
    @DisplayName("Nao deve permitir criar progresso com nome em branco")
    void naoDevePermitirNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new ProgressoGamificado("  "));
    }

    @Test
    @DisplayName("Nao deve permitir criar progresso com nome nulo")
    void naoDevePermitirNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ProgressoGamificado(null));
    }

    @Test
    @DisplayName("Reiniciar a trilha deve zerar XP, nivel e badges")
    void deveReiniciarTrilha() {
        progresso.adicionarXp(150);
        progresso.reiniciarTrilha();

        assertEquals(0, progresso.getXp());
        assertEquals(1, progresso.getNivel());
        assertTrue(progresso.getBadges().isEmpty());
    }

    // ---- Emanuel Ronaldo Gomes de Souza ----

    @Test
    @DisplayName("Adicionar XP positivo deve aumentar o total acumulado")
    void deveAdicionarXp() {
        progresso.adicionarXp(50);
        assertEquals(50, progresso.getXp());
    }

    @Test
    @DisplayName("Nao deve permitir adicionar XP zero ou negativo")
    void naoDevePermitirXpInvalido() {
        assertThrows(IllegalArgumentException.class, () -> progresso.adicionarXp(0));
        assertThrows(IllegalArgumentException.class, () -> progresso.adicionarXp(-10));
    }

    @Test
    @DisplayName("A cada 100 XP acumulados o aluno deve subir um nivel")
    void deveSubirDeNivelACada100Xp() {
        progresso.adicionarXp(99);
        assertEquals(1, progresso.getNivel());

        progresso.adicionarXp(1);
        assertEquals(2, progresso.getNivel());

        progresso.adicionarXp(200);
        assertEquals(4, progresso.getNivel());
    }

    @Test
    @DisplayName("calcularNivel deve ser uma funcao pura baseada no XP informado")
    void deveCalcularNivelDeFormaPura() {
        assertEquals(1, ProgressoGamificado.calcularNivel(0));
        assertEquals(1, ProgressoGamificado.calcularNivel(99));
        assertEquals(2, ProgressoGamificado.calcularNivel(100));
        assertEquals(11, ProgressoGamificado.calcularNivel(1000));
    }

    // ---- Luis Henrique Telo Ladeira Mota ----

    @Test
    @DisplayName("Deve desbloquear a badge Iniciante ao atingir 100 XP")
    void deveDesbloquearBadgeIniciante() {
        progresso.adicionarXp(100);
        assertTrue(progresso.possuiBadge("Iniciante"));
    }

    @Test
    @DisplayName("Deve desbloquear a badge Intermediario ao atingir 500 XP")
    void deveDesbloquearBadgeIntermediario() {
        progresso.adicionarXp(500);
        assertTrue(progresso.possuiBadge("Iniciante"));
        assertTrue(progresso.possuiBadge("Intermediario"));
    }

    @Test
    @DisplayName("Deve desbloquear a badge Avancado ao atingir 1000 XP")
    void deveDesbloquearBadgeAvancado() {
        progresso.adicionarXp(1000);
        assertTrue(progresso.possuiBadge("Avancado"));
    }

    @Test
    @DisplayName("Nao deve possuir badges cujo limiar ainda nao foi atingido")
    void naoDeveDesbloquearBadgeAntesDoLimiar() {
        progresso.adicionarXp(40);
        assertFalse(progresso.possuiBadge("Iniciante"));
    }

    @Test
    @DisplayName("Ranking deve ordenar do maior para o menor XP (compareTo)")
    void deveCompararPorXpParaRanking() {
        ProgressoGamificado outro = new ProgressoGamificado("Outro Aluno");
        progresso.adicionarXp(100);
        outro.adicionarXp(50);

        assertTrue(progresso.compareTo(outro) < 0);
        assertTrue(outro.compareTo(progresso) > 0);
    }

    @Test
    @DisplayName("Dois progressos com o mesmo XP devem ser considerados equivalentes no ranking")
    void deveConsiderarEmpateNoRanking() {
        ProgressoGamificado outro = new ProgressoGamificado("Outro Aluno");
        progresso.adicionarXp(80);
        outro.adicionarXp(80);

        assertEquals(0, progresso.compareTo(outro));
    }
}
