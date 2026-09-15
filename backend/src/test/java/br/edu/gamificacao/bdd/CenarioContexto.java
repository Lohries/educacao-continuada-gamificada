package br.edu.gamificacao.bdd;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Estado compartilhado entre as classes de step definitions de um mesmo
 * cenario (escopo de cucumber-spring), para permitir cenarios com Given/When
 * /Then organizados em classes diferentes por autor/US.
 */
@Component
@ScenarioScope
@Getter
@Setter
public class CenarioContexto {

    private Long usuarioAtualId;

    private final Map<String, Long> cursoIdPorTitulo = new HashMap<>();
    private final Map<String, Long> matriculaIdPorCurso = new HashMap<>();
}
