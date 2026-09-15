package br.edu.gamificacao.controller;

import br.edu.gamificacao.dto.RankingItemDTO;
import br.edu.gamificacao.dto.UsuarioCreateDTO;
import br.edu.gamificacao.dto.UsuarioDTO;
import br.edu.gamificacao.entity.Usuario;
import br.edu.gamificacao.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Cadastro de alunos e ranking de gamificacao")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo aluno")
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
        Usuario usuario = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.from(usuario));
    }

    @GetMapping
    @Operation(summary = "Lista todos os alunos cadastrados")
    public List<UsuarioDTO> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioDTO::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um aluno pelo id")
    public UsuarioDTO buscarPorId(@PathVariable Long id) {
        return UsuarioDTO.from(usuarioService.buscarPorId(id));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Lista o ranking de alunos ordenado por XP (maior para o menor)")
    public List<RankingItemDTO> ranking() {
        return usuarioService.ranking();
    }
}
