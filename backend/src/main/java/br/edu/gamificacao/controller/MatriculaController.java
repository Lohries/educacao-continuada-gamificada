package br.edu.gamificacao.controller;

import br.edu.gamificacao.dto.MatriculaCreateDTO;
import br.edu.gamificacao.dto.MatriculaDTO;
import br.edu.gamificacao.entity.Matricula;
import br.edu.gamificacao.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
@Tag(name = "Matriculas", description = "Matricula de alunos em cursos e conclusao de trilhas (concede XP)")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @Operation(summary = "Matricula um aluno em um curso")
    public ResponseEntity<MatriculaDTO> matricular(@Valid @RequestBody MatriculaCreateDTO dto) {
        Matricula matricula = matriculaService.matricular(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(MatriculaDTO.from(matricula));
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Conclui uma matricula e concede XP ao aluno")
    public MatriculaDTO concluir(@PathVariable Long id) {
        return MatriculaDTO.from(matriculaService.concluir(id));
    }

    @GetMapping
    @Operation(summary = "Lista as matriculas de um aluno")
    public List<MatriculaDTO> listarPorUsuario(@RequestParam Long usuarioId) {
        return matriculaService.listarPorUsuario(usuarioId).stream().map(MatriculaDTO::from).toList();
    }
}
