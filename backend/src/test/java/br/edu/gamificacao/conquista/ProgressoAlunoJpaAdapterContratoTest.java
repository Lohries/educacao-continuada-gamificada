package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.adapter.ProgressoAlunoJpaAdapter;
import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;
import br.edu.gamificacao.usuario.UsuarioCreateDTO;
import br.edu.gamificacao.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Contrato da porta executado contra o adapter real (JPA, banco H2). */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ProgressoAlunoJpaAdapterContratoTest extends ProgressoAlunoPortContrato {

    @Autowired
    private ProgressoAlunoJpaAdapter adapter;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected ProgressoAlunoPort porta() {
        return adapter;
    }

    @Override
    protected Long cadastrarAluno(String nome) {
        String email = "contrato." + UUID.randomUUID() + "@teste.com";
        return usuarioService.criar(new UsuarioCreateDTO(nome, email)).getId();
    }
}
