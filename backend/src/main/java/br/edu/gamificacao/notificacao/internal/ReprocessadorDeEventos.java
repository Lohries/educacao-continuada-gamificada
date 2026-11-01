package br.edu.gamificacao.notificacao.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Recuperabilidade: reentrega periodicamente os eventos cujo consumidor
 * falhou (publicacoes incompletas no Event Publication Registry).
 */
@Component
public class ReprocessadorDeEventos {

    private final IncompleteEventPublications pendentes;
    private final Duration idadeMinima;

    public ReprocessadorDeEventos(IncompleteEventPublications pendentes,
                                  @Value("${notificacao.reprocessar-apos-ms}") long idadeMinimaMs) {
        this.pendentes = pendentes;
        this.idadeMinima = Duration.ofMillis(idadeMinimaMs);
    }

    @Scheduled(fixedDelayString = "${notificacao.reprocessar-a-cada-ms}",
            initialDelayString = "${notificacao.reprocessar-a-cada-ms}")
    public void reprocessar() {
        pendentes.resubmitIncompletePublicationsOlderThan(idadeMinima);
    }
}
