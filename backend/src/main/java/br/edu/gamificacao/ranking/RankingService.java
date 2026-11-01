package br.edu.gamificacao.ranking;

import br.edu.gamificacao.usuario.Usuario;
import br.edu.gamificacao.usuario.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * API publica do modulo Ranking. E uma leitura: depende apenas da API do
 * modulo Usuario, o que mantem a fronteira pronta para uma extracao futura
 * caso as metricas mostrem necessidade de escala propria (ADR-001).
 */
@Service
public class RankingService {

    private final UsuarioService usuarioService;

    public RankingService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public List<RankingItemDTO> ranking() {
        List<Usuario> ordenados = usuarioService.listarPorXpDecrescente();

        return IntStream.range(0, ordenados.size())
                .mapToObj(i -> {
                    Usuario u = ordenados.get(i);
                    return new RankingItemDTO(i + 1, u.getId(), u.getNome(), u.getXp(), u.getNivel());
                })
                .toList();
    }
}
