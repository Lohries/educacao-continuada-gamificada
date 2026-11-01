package br.edu.gamificacao.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Listeners de eventos assincronos e reprocessamento agendado (ADR-004). */
@Configuration
@EnableAsync
@EnableScheduling
public class AssincronoConfig {
}
