package br.edu.gamificacao.curso.internal;

import br.edu.gamificacao.curso.Curso;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CursoSeeder implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    public CursoSeeder(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    public void run(String... args) {
        if (cursoRepository.count() == 0) {
            cursoRepository.save(Curso.builder()
                    .titulo("Fundamentos de Spring Boot")
                    .descricao("Introducao a criacao de APIs REST com Spring Boot")
                    .cargaHoraria(20)
                    .xpRecompensa(100)
                    .build());

            cursoRepository.save(Curso.builder()
                    .titulo("TDD na Pratica")
                    .descricao("Test Driven Development com JUnit e Mockito")
                    .cargaHoraria(15)
                    .xpRecompensa(150)
                    .build());

            cursoRepository.save(Curso.builder()
                    .titulo("BDD e ATDD com Cucumber")
                    .descricao("Escrevendo criterios de aceite executaveis a partir de User Stories")
                    .cargaHoraria(10)
                    .xpRecompensa(200)
                    .build());
        }
    }
}
