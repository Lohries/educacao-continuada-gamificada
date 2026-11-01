package br.edu.gamificacao.aprendizagem.web;

import br.edu.gamificacao.aprendizagem.EventoAprendizagemService;
import br.edu.gamificacao.aprendizagem.EventoAprendizagemService.EventoDTO;
import br.edu.gamificacao.aprendizagem.EventoAprendizagemService.NovoEvento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Eventos de aprendizagem", description = "Eventos dos laboratorios (IoT), recebidos via gateway MQTT")
public class EventoAprendizagemController {

    private final EventoAprendizagemService service;

    public EventoAprendizagemController(EventoAprendizagemService service) {
        this.service = service;
    }

    @PostMapping("/api/eventos-aprendizagem")
    @Operation(summary = "Registra um evento de laboratorio (201 novo, 200 reentrega ja registrada)")
    public ResponseEntity<EventoDTO> registrar(@Valid @RequestBody NovoEvento evento) {
        EventoAprendizagemService.Registro registro = service.registrar(evento);
        return ResponseEntity.status(registro.novo() ? HttpStatus.CREATED : HttpStatus.OK).body(registro.evento());
    }

    @GetMapping("/api/eventos-aprendizagem")
    @Operation(summary = "Lista os eventos de laboratorio de um aluno")
    public List<EventoDTO> listar(@RequestParam Long alunoId) {
        return service.listarPorAluno(alunoId);
    }
}
