package br.edu.gamificacao.bdd;

import br.edu.gamificacao.dto.RankingItemDTO;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions da US de Luis Henrique Telo Ladeira Mota (github.com/Lohries):
 * ranking de alunos por pontuacao (XP).
 */
public class RankingSteps {

    @Autowired
    private ApiTestHelper apiTestHelper;

    private List<RankingItemDTO> ranking;

    @Quando("eu consulto o ranking de alunos")
    public void euConsultoORankingDeAlunos() {
        ranking = List.of(apiTestHelper.buscarRanking().getBody());
    }

    @Entao("{string} deve aparecer no ranking em posicao melhor que {string}")
    public void deveAparecerNoRankingEmPosicaoMelhorQue(String nomeMelhorColocado, String nomePiorColocado) {
        int posicaoMelhor = posicaoDoAluno(nomeMelhorColocado);
        int posicaoPior = posicaoDoAluno(nomePiorColocado);

        assertTrue(posicaoMelhor < posicaoPior,
                nomeMelhorColocado + " (posicao " + posicaoMelhor + ") deveria estar em posicao melhor que "
                        + nomePiorColocado + " (posicao " + posicaoPior + ")");
    }

    private int posicaoDoAluno(String nome) {
        return ranking.stream()
                .filter(item -> item.nome().equals(nome))
                .findFirst()
                .map(RankingItemDTO::posicao)
                .orElseThrow(() -> new IllegalStateException("aluno nao encontrado no ranking: " + nome));
    }
}
