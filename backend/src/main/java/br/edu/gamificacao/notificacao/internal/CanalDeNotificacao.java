package br.edu.gamificacao.notificacao.internal;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simula o provedor externo de notificacao (e-mail/push/SMS). Pode ser
 * colocado fora do ar para o mini-lab do Desafio 4.
 */
@Component
public class CanalDeNotificacao {

    private static final Logger log = LoggerFactory.getLogger(CanalDeNotificacao.class);

    private final AtomicBoolean indisponivel = new AtomicBoolean(false);
    private final MeterRegistry metricas;

    public CanalDeNotificacao(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    public void enviar(Long usuarioId, String mensagem) {
        if (indisponivel.get()) {
            metricas.counter("notificacao.envios", "resultado", "falha").increment();
            throw new IllegalStateException("provedor de notificacao indisponivel");
        }
        metricas.counter("notificacao.envios", "resultado", "sucesso").increment();
        log.info("notificacao enviada ao usuario {}: {}", usuarioId, mensagem);
    }

    public void definirIndisponivel(boolean valor) {
        indisponivel.set(valor);
    }

    public boolean isIndisponivel() {
        return indisponivel.get();
    }
}
