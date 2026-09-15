package br.edu.gamificacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCreateDTO(
        @NotBlank(message = "nome e obrigatorio") String nome,
        @NotBlank(message = "email e obrigatorio") @Email(message = "email invalido") String email
) {
}
