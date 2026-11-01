package br.edu.gamificacao.recomendacao.web;

import br.edu.gamificacao.recomendacao.Recomendacoes;
import br.edu.gamificacao.recomendacao.RecomendacaoService;
import br.edu.gamificacao.recomendacao.RespostaAssistente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "IA", description = "Recomendacao de cursos e assistente educacional (via ia-service)")
public class RecomendacaoController {

    public record PerguntaDTO(@NotBlank(message = "pergunta e obrigatoria") String pergunta) {
    }

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/api/recomendacoes")
    @Operation(summary = "Recomenda cursos para o aluno (degrada para regra local se a IA cair)")
    public Recomendacoes recomendar(@RequestParam Long usuarioId) {
        return recomendacaoService.recomendar(usuarioId);
    }

    @PostMapping("/api/assistente/perguntas")
    @Operation(summary = "Pergunta ao assistente educacional (RAG)")
    public RespostaAssistente perguntar(@Valid @RequestBody PerguntaDTO dto) {
        return recomendacaoService.perguntar(dto.pergunta());
    }
}
