package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato da porta {@link ProgressoAlunoPort}. Os mesmos testes rodam contra
 * cada adapter (memoria e JPA), o que prova que trocar o adapter nao muda o
 * comportamento visto pelo caso de uso (ADR-002).
 */
abstract class ProgressoAlunoPortContrato {

    protected abstract ProgressoAlunoPort porta();

    protected abstract Long cadastrarAluno(String nome);

    @Test
    @DisplayName("Aluno recem-cadastrado e lido com XP zero, nivel 1 e sem badges")
    void alunoNovoComecaZerado() {
        Long id = cadastrarAluno("Ana Contrato");

        ProgressoGamificado progresso = porta().buscar(id).orElseThrow();

        assertEquals("Ana Contrato", progresso.getNomeAluno());
        assertEquals(0, progresso.getXp());
        assertEquals(1, progresso.getNivel());
        assertTrue(progresso.getBadges().isEmpty());
    }

    @Test
    @DisplayName("O progresso salvo e lido de volta com XP, nivel e badges recalculados")
    void salvaELeDeVolta() {
        Long id = cadastrarAluno("Bruno Contrato");
        ProgressoGamificado progresso = porta().buscar(id).orElseThrow();
        progresso.adicionarXp(550);

        porta().salvar(id, progresso);
        ProgressoGamificado lido = porta().buscar(id).orElseThrow();

        assertEquals(550, lido.getXp());
        assertEquals(6, lido.getNivel());
        assertTrue(lido.possuiBadge("Iniciante"));
        assertTrue(lido.possuiBadge("Intermediario"));
    }

    @Test
    @DisplayName("Aluno inexistente resulta em Optional vazio")
    void alunoInexistente() {
        assertTrue(porta().buscar(999_999L).isEmpty());
    }
}
