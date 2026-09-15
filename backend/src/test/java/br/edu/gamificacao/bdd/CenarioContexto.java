package br.edu.gamificacao.bdd;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Estado compartilhado entre as classes de step definitions de um mesmo
 * cenario (escopo de cucumber-spring), para permitir cenarios com Given/When
 * /Then organizados em classes diferentes por autor/US.
 *
 * Nao e anotada com @Component de proposito: e registrada explicitamente
 * como bean apenas dentro de {@link CucumberSpringConfiguration}, para nao
 * ser varrida pelo component scan da aplicacao principal (que cobriria
 * qualquer outro teste @SpringBootTest do projeto).
 */
@Getter
@Setter
public class CenarioContexto {

    private Long usuarioAtualId;

    private final Map<String, Long> cursoIdPorTitulo = new HashMap<>();
    private final Map<String, Long> matriculaIdPorCurso = new HashMap<>();
}
