package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter fake da porta {@link ProgressoAlunoPort}: guarda so nome e XP,
 * como o banco faria, para que cada leitura reconstrua o dominio do zero.
 */
class ProgressoAlunoEmMemoria implements ProgressoAlunoPort {

    private record Registro(String nome, int xp) {
    }

    private final Map<Long, Registro> registros = new HashMap<>();
    private long proximoId = 1;
    private int gravacoes = 0;

    Long cadastrar(String nome) {
        Long id = proximoId++;
        registros.put(id, new Registro(nome, 0));
        return id;
    }

    int gravacoes() {
        return gravacoes;
    }

    @Override
    public Optional<ProgressoGamificado> buscar(Long alunoId) {
        return Optional.ofNullable(registros.get(alunoId))
                .map(r -> ProgressoGamificado.restaurar(r.nome(), r.xp()));
    }

    @Override
    public void salvar(Long alunoId, ProgressoGamificado progresso) {
        registros.put(alunoId, new Registro(progresso.getNomeAluno(), progresso.getXp()));
        gravacoes++;
    }
}
