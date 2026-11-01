package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;
import br.edu.gamificacao.shared.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Caso de uso (porta de entrada) do modulo Conquista. As regras de XP,
 * nivel e badges ficam no dominio {@link ProgressoGamificado}; a
 * persistencia fica atras de {@link ProgressoAlunoPort} (ADR-002).
 */
@Service
public class ConquistaService {

    private final ProgressoAlunoPort progressos;

    public ConquistaService(ProgressoAlunoPort progressos) {
        this.progressos = progressos;
    }

    @Transactional
    public ProgressoGamificado concederXp(Long alunoId, int pontos) {
        ProgressoGamificado progresso = buscar(alunoId);
        progresso.adicionarXp(pontos);
        progressos.salvar(alunoId, progresso);
        return progresso;
    }

    public Set<String> badgesConquistadas(Long alunoId) {
        return buscar(alunoId).getBadges();
    }

    private ProgressoGamificado buscar(Long alunoId) {
        return progressos.buscar(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuario nao encontrado: " + alunoId));
    }
}
