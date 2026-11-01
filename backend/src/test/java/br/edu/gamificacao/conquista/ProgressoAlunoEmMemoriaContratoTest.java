package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;

/** Contrato da porta executado contra o adapter fake, sem Spring nem banco. */
class ProgressoAlunoEmMemoriaContratoTest extends ProgressoAlunoPortContrato {

    private final ProgressoAlunoEmMemoria adapter = new ProgressoAlunoEmMemoria();

    @Override
    protected ProgressoAlunoPort porta() {
        return adapter;
    }

    @Override
    protected Long cadastrarAluno(String nome) {
        return adapter.cadastrar(nome);
    }
}
