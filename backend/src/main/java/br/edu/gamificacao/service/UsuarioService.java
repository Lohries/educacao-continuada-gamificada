package br.edu.gamificacao.service;

import br.edu.gamificacao.domain.ProgressoGamificado;
import br.edu.gamificacao.dto.RankingItemDTO;
import br.edu.gamificacao.dto.UsuarioCreateDTO;
import br.edu.gamificacao.entity.Badge;
import br.edu.gamificacao.entity.Usuario;
import br.edu.gamificacao.repository.BadgeRepository;
import br.edu.gamificacao.repository.UsuarioRepository;
import br.edu.gamificacao.service.exception.RecursoNaoEncontradoException;
import br.edu.gamificacao.service.exception.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Regras de negocio do aluno/usuario, incluindo o ganho de XP.
 * A formula de nivel e a mesma usada pela classe de dominio
 * {@link ProgressoGamificado}, garantindo uma unica fonte de verdade.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BadgeRepository badgeRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, BadgeRepository badgeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.badgeRepository = badgeRepository;
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

    @Transactional
    public Usuario adicionarXp(Long usuarioId, int pontos) {
        Usuario usuario = buscarPorId(usuarioId);

        int novoXp = usuario.getXp() + pontos;
        usuario.setXp(novoXp);
        usuario.setNivel(ProgressoGamificado.calcularNivel(novoXp));

        return usuarioRepository.save(usuario);
    }

    public List<Badge> badgesConquistadas(Usuario usuario) {
        return badgeRepository.findByXpMinimoLessThanEqualOrderByXpMinimoAsc(usuario.getXp());
    }

    public List<RankingItemDTO> ranking() {
        List<Usuario> ordenados = usuarioRepository.findAllByOrderByXpDesc();

        return IntStream.range(0, ordenados.size())
                .mapToObj(i -> {
                    Usuario u = ordenados.get(i);
                    return new RankingItemDTO(i + 1, u.getId(), u.getNome(), u.getXp(), u.getNivel());
                })
                .toList();
    }
}
