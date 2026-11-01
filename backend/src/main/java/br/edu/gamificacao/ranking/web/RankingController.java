package br.edu.gamificacao.ranking.web;

import br.edu.gamificacao.ranking.RankingItemDTO;
import br.edu.gamificacao.ranking.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Ranking", description = "Ranking de alunos por XP")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * {@code /api/usuarios/ranking} e o caminho da AC1, mantido para nao
     * quebrar o front-end; {@code /api/ranking} e o caminho do modulo.
     */
    @GetMapping({"/api/ranking", "/api/usuarios/ranking"})
    @Operation(summary = "Lista o ranking de alunos ordenado por XP (maior para o menor)")
    public List<RankingItemDTO> ranking() {
        return rankingService.ranking();
    }
}
