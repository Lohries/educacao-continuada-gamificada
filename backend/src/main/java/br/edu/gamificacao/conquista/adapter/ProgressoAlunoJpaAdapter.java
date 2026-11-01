package br.edu.gamificacao.conquista.adapter;

import br.edu.gamificacao.conquista.ProgressoGamificado;
import br.edu.gamificacao.conquista.porta.ProgressoAlunoPort;
import br.edu.gamificacao.shared.RecursoNaoEncontradoException;
import br.edu.gamificacao.usuario.Usuario;
import br.edu.gamificacao.usuario.UsuarioService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter de persistencia real: o XP continua na tabela {@code usuarios} da
 * AC1 (gravada via JPA pelo modulo Usuario), e aqui e traduzido de/para o
 * dominio {@link ProgressoGamificado}.
 */
@Component
public class ProgressoAlunoJpaAdapter implements ProgressoAlunoPort {

    private final UsuarioService usuarioService;

    public ProgressoAlunoJpaAdapter(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public Optional<ProgressoGamificado> buscar(Long alunoId) {
        try {
            Usuario usuario = usuarioService.buscarPorId(alunoId);
            return Optional.of(ProgressoGamificado.restaurar(usuario.getNome(), usuario.getXp()));
        } catch (RecursoNaoEncontradoException e) {
            return Optional.empty();
        }
    }

    @Override
    public void salvar(Long alunoId, ProgressoGamificado progresso) {
        usuarioService.registrarProgresso(alunoId, progresso.getXp(), progresso.getNivel());
    }
}
