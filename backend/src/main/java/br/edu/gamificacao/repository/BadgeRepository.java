package br.edu.gamificacao.repository;

import br.edu.gamificacao.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByXpMinimoLessThanEqualOrderByXpMinimoAsc(Integer xp);
}
