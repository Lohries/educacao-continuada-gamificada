package br.edu.gamificacao.conquista.porta;

import br.edu.gamificacao.conquista.ProgressoGamificado;

import java.util.Optional;

/**
 * Porta de saida do modulo Conquista (ADR-002): o caso de uso de
 * gamificacao so conhece esta interface, nunca o banco, o JPA ou o modulo
 * que guarda os dados do aluno.
 */
public interface ProgressoAlunoPort {

    Optional<ProgressoGamificado> buscar(Long alunoId);

    void salvar(Long alunoId, ProgressoGamificado progresso);
}
