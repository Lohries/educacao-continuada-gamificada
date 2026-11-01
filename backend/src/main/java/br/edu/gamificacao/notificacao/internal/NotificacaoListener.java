package br.edu.gamificacao.notificacao.internal;

import br.edu.gamificacao.matricula.CursoConcluido;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumidor assincrono de {@link CursoConcluido}: roda depois do commit da
 * conclusao, em outra thread e outra transacao. Se lancar excecao, a
 * publicacao fica incompleta no registro e e reprocessada (ADR-004).
 */
@Component
public class NotificacaoListener {

    private final CanalDeNotificacao canal;
    private final NotificacaoRepository notificacoes;

    public NotificacaoListener(CanalDeNotificacao canal, NotificacaoRepository notificacoes) {
        this.canal = canal;
        this.notificacoes = notificacoes;
    }

    @ApplicationModuleListener
    public void aoConcluirCurso(CursoConcluido evento) {
        if (notificacoes.existsByMatriculaId(evento.matriculaId())) {
            return;
        }
        String mensagem = "Parabens! Voce concluiu \"" + evento.tituloCurso() + "\" e ganhou " + evento.xpGanho() + " XP.";
        canal.enviar(evento.usuarioId(), mensagem);
        notificacoes.save(Notificacao.builder()
                .usuarioId(evento.usuarioId())
                .matriculaId(evento.matriculaId())
                .mensagem(mensagem)
                .enviadaEm(Instant.now())
                .build());
    }
}
