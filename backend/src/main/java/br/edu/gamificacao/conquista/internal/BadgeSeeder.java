package br.edu.gamificacao.conquista.internal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BadgeSeeder implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    public BadgeSeeder(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(String... args) {
        if (badgeRepository.count() == 0) {
            badgeRepository.save(Badge.builder().nome("Iniciante").descricao("Atingiu 100 XP").xpMinimo(100).build());
            badgeRepository.save(Badge.builder().nome("Intermediario").descricao("Atingiu 500 XP").xpMinimo(500).build());
            badgeRepository.save(Badge.builder().nome("Avancado").descricao("Atingiu 1000 XP").xpMinimo(1000).build());
        }
    }
}
