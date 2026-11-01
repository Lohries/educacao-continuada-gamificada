package br.edu.gamificacao.notificacao.web;

import br.edu.gamificacao.notificacao.NotificacaoService;
import br.edu.gamificacao.notificacao.NotificacaoService.NotificacaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Notificacoes", description = "Notificacoes enviadas apos eventos de aprendizagem")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping("/api/notificacoes")
    @Operation(summary = "Lista as notificacoes enviadas a um aluno")
    public List<NotificacaoDTO> listar(@RequestParam Long usuarioId) {
        return notificacaoService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/api/notificacoes/eventos-pendentes")
    @Operation(summary = "Quantidade de eventos aguardando reprocessamento")
    public Map<String, Long> pendentes() {
        return Map.of("pendentes", notificacaoService.eventosPendentes());
    }
}
