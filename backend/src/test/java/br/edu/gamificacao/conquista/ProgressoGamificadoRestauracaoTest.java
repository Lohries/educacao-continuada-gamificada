package br.edu.gamificacao.conquista;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reconstrucao do dominio a partir do XP persistido (usada pelos adapters). */
class ProgressoGamificadoRestauracaoTest {

    @Test
    @DisplayName("Restaurar com XP zero equivale a um progresso novo")
    void restaurarZerado() {
        ProgressoGamificado p = ProgressoGamificado.restaurar("Davi", 0);

        assertEquals(0, p.getXp());
        assertEquals(1, p.getNivel());
        assertTrue(p.getBadges().isEmpty());
    }

    @Test
    @DisplayName("Restaurar recalcula nivel e badges pelas mesmas regras de adicionarXp")
    void restaurarRecalcula() {
        ProgressoGamificado p = ProgressoGamificado.restaurar("Eva", 1000);

        assertEquals(11, p.getNivel());
        assertTrue(p.possuiBadge("Avancado"));
    }

    @Test
    @DisplayName("XP persistido negativo e recusado")
    void restaurarNegativo() {
        assertThrows(IllegalArgumentException.class, () -> ProgressoGamificado.restaurar("Fabio", -1));
    }
}
