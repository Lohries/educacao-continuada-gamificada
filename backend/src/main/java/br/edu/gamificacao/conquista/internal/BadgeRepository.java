package br.edu.gamificacao.conquista.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByXpMinimoLessThanEqualOrderByXpMinimoAsc(Integer xp);
}
