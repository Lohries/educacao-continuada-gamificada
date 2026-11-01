package br.edu.gamificacao.notificacao.web;

import br.edu.gamificacao.notificacao.internal.CanalDeNotificacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controle do mini-lab: derruba/religa o provedor de notificacao simulado.
 * So existe com {@code lab.habilitado=true}; nunca em producao.
 */
@RestController
@ConditionalOnProperty(name = "lab.habilitado", havingValue = "true")
@Tag(name = "Laboratorio", description = "Simulacao de falhas (somente com lab.habilitado=true)")
public class LabNotificacaoController {

    private final CanalDeNotificacao canal;

    public LabNotificacaoController(CanalDeNotificacao canal) {
        this.canal = canal;
    }

    @PostMapping("/api/lab/notificacao")
    @Operation(summary = "Coloca o provedor de notificacao fora do ar (indisponivel=true) ou de volta")
    public Map<String, Boolean> definir(@RequestParam boolean indisponivel) {
        canal.definirIndisponivel(indisponivel);
        return Map.of("indisponivel", canal.isIndisponivel());
    }
}
