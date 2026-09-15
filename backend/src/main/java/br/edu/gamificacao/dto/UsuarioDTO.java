package br.edu.gamificacao.dto;

import br.edu.gamificacao.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioDTO(
        Long id,
        String nome,
        String email,
        Integer xp,
        Integer nivel,
        LocalDateTime criadoEm
) {
    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getXp(),
                usuario.getNivel(),
                usuario.getCriadoEm()
        );
    }
}
