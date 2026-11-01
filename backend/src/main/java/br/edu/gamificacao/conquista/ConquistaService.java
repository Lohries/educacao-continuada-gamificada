package br.edu.gamificacao.conquista;

import br.edu.gamificacao.conquista.internal.Badge;
import br.edu.gamificacao.conquista.internal.BadgeRepository;
import br.edu.gamificacao.usuario.Usuario;
import br.edu.gamificacao.usuario.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API publica do modulo Conquista: concentra as regras de XP, nivel e
 * badges. A formula de nivel vem da classe de dominio
 * {@link ProgressoGamificado}, garantindo uma unica fonte de verdade.
 */
@Service
public class ConquistaService {

    private final UsuarioService usuarioService;
    private final BadgeRepository badgeRepository;

    public ConquistaService(UsuarioService usuarioService, BadgeRepository badgeRepository) {
        this.usuarioService = usuarioService;
        this.badgeRepository = badgeRepository;
    }

    @Transactional
    public Usuario concederXp(Long usuarioId, int pontos) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        int novoXp = usuario.getXp() + pontos;
        return usuarioService.registrarProgresso(usuarioId, novoXp, ProgressoGamificado.calcularNivel(novoXp));
    }

    public List<String> badgesConquistadas(Long usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        return badgeRepository.findByXpMinimoLessThanEqualOrderByXpMinimoAsc(usuario.getXp()).stream()
                .map(Badge::getNome)
                .toList();
    }
}
