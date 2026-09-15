package br.edu.gamificacao.controller;

import br.edu.gamificacao.dto.CursoCreateDTO;
import br.edu.gamificacao.dto.CursoDTO;
import br.edu.gamificacao.entity.Curso;
import br.edu.gamificacao.service.CursoService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "Catalogo de cursos/trilhas de educacao continuada")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo curso")
    public ResponseEntity<CursoDTO> criar(@Valid @RequestBody CursoCreateDTO dto) {
        Curso curso = cursoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(CursoDTO.from(curso));
    }

    @GetMapping
    @Operation(summary = "Lista todos os cursos disponiveis")
    public List<CursoDTO> listar() {
        return cursoService.listarTodos().stream().map(CursoDTO::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um curso pelo id")
    public CursoDTO buscarPorId(@PathVariable Long id) {
        return CursoDTO.from(cursoService.buscarPorId(id));
    }
}
