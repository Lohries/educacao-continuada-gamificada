package br.edu.gamificacao.seed;

import br.edu.gamificacao.entity.Badge;
import br.edu.gamificacao.entity.Curso;
import br.edu.gamificacao.repository.BadgeRepository;
import br.edu.gamificacao.repository.CursoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CursoRepository cursoRepository;
    private final BadgeRepository badgeRepository;

    public DataSeeder(CursoRepository cursoRepository, BadgeRepository badgeRepository) {
        this.cursoRepository = cursoRepository;
        this.badgeRepository = badgeRepository;
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

        if (badgeRepository.count() == 0) {
            badgeRepository.save(Badge.builder().nome("Iniciante").descricao("Atingiu 100 XP").xpMinimo(100).build());
            badgeRepository.save(Badge.builder().nome("Intermediario").descricao("Atingiu 500 XP").xpMinimo(500).build());
            badgeRepository.save(Badge.builder().nome("Avancado").descricao("Atingiu 1000 XP").xpMinimo(1000).build());
        }
    }
}
