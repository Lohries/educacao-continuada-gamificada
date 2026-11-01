package br.edu.gamificacao.conquista;

import br.edu.gamificacao.shared.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Caso de uso de gamificacao testado so com dominio + adapter fake: nao sobe
 * Spring, nao toca banco (ADR-002).
 */
class ConquistaServiceTest {

    private ProgressoAlunoEmMemoria progressos;
    private ConquistaService conquistas;
    private Long alunoId;

    @BeforeEach
    void setUp() {
        progressos = new ProgressoAlunoEmMemoria();
        conquistas = new ConquistaService(progressos);
        alunoId = progressos.cadastrar("Carla Hexagonal");
    }

    @Test
    @DisplayName("Conceder XP soma ao progresso, recalcula o nivel e persiste pela porta")
    void concederXpPersistePelaPorta() {
        conquistas.concederXp(alunoId, 150);
        ProgressoGamificado depois = conquistas.concederXp(alunoId, 100);

        assertEquals(250, depois.getXp());
        assertEquals(3, depois.getNivel());
        assertEquals(2, progressos.gravacoes());
        assertEquals(250, progressos.buscar(alunoId).orElseThrow().getXp());
    }

    @Test
    @DisplayName("Badges conquistadas vem das regras do dominio")
    void badgesVemDoDominio() {
        conquistas.concederXp(alunoId, 500);

        assertTrue(conquistas.badgesConquistadas(alunoId).containsAll(java.util.Set.of("Iniciante", "Intermediario")));
    }

    @Test
    @DisplayName("Pontos invalidos sao recusados pelo dominio e nada e gravado")
    void pontosInvalidosNaoGravam() {
        assertThrows(IllegalArgumentException.class, () -> conquistas.concederXp(alunoId, 0));
        assertEquals(0, progressos.gravacoes());
    }

    @Test
    @DisplayName("Aluno inexistente gera RecursoNaoEncontradoException")
    void alunoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> conquistas.concederXp(42L, 10));
    }
}
