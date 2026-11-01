package br.edu.gamificacao.usuario;

import br.edu.gamificacao.usuario.internal.UsuarioRepository;
import br.edu.gamificacao.shared.RecursoNaoEncontradoException;
import br.edu.gamificacao.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API publica do modulo Usuario: cadastro e consulta de alunos.
 * As regras de XP, nivel e badges pertencem ao modulo Conquista, que
 * apenas registra aqui o progresso ja calculado.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario criar(UsuarioCreateDTO dto) {
        usuarioRepository.findByEmail(dto.email()).ifPresent(u -> {
            throw new RegraDeNegocioException("ja existe um usuario cadastrado com este email");
        });

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .build();

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuario nao encontrado: " + id));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorXpDecrescente() {
        return usuarioRepository.findAllByOrderByXpDesc();
    }

    @Transactional
    public Usuario registrarProgresso(Long usuarioId, int xp, int nivel) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setXp(xp);
        usuario.setNivel(nivel);
        return usuarioRepository.save(usuario);
    }
}
