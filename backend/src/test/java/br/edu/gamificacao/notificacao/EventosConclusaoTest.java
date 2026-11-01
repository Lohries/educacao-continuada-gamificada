package br.edu.gamificacao.notificacao;

import br.edu.gamificacao.curso.CursoService;
import br.edu.gamificacao.matricula.MatriculaCreateDTO;
import br.edu.gamificacao.matricula.MatriculaService;
import br.edu.gamificacao.notificacao.internal.CanalDeNotificacao;
import br.edu.gamificacao.usuario.UsuarioCreateDTO;
import br.edu.gamificacao.usuario.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Evidencia do ADR-004: conclusao + XP sao sincronos; a notificacao e
 * assincrona via evento persistido. Consumidor indisponivel nao impede a
 * conclusao, e o evento pendente e reprocessado quando ele volta.
 */
@SpringBootTest(properties = "notificacao.reprocessar-a-cada-ms=3600000")
@ActiveProfiles("h2")
class EventosConclusaoTest {

    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private CanalDeNotificacao canal;

    @Autowired
    private IncompleteEventPublications pendentes;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transacao;

    private Long alunoId;
    private Long matriculaId;

    @BeforeEach
    void setUp() {
        alunoId = usuarioService.criar(new UsuarioCreateDTO("Aluno Eventos", "eventos." + UUID.randomUUID() + "@teste.com")).getId();
        Long cursoId = cursoService.listarTodos().get(0).getId();
        matriculaId = matriculaService.matricular(new MatriculaCreateDTO(alunoId, cursoId)).getId();
    }

    @AfterEach
    void religarCanal() {
        canal.definirIndisponivel(false);
    }

    @Test
    @DisplayName("Consumidor no ar: XP imediato na resposta e notificacao entregue de forma assincrona")
    void consumidorDisponivel() {
        matriculaService.concluir(matriculaId);

        assertTrue(usuarioService.buscarPorId(alunoId).getXp() > 0, "XP e sincrono: ja esta gravado no retorno");
        esperarAte(() -> notificacaoService.listarPorUsuario(alunoId).size() == 1);
        esperarAte(() -> pendentesDaMatricula() == 0);
    }

    @Test
    @DisplayName("Consumidor fora: a conclusao conclui, o evento fica pendente e e reprocessado quando ele volta")
    void consumidorIndisponivelEReprocessamento() throws InterruptedException {
        canal.definirIndisponivel(true);

        matriculaService.concluir(matriculaId);

        assertTrue(usuarioService.buscarPorId(alunoId).getXp() > 0, "conclusao e XP nao dependem do consumidor");
        esperarAte(() -> pendentesDaMatricula() == 1);
        Thread.sleep(300);
        assertTrue(notificacaoService.listarPorUsuario(alunoId).isEmpty());

        canal.definirIndisponivel(false);
        pendentes.resubmitIncompletePublications(p -> true);

        esperarAte(() -> notificacaoService.listarPorUsuario(alunoId).size() == 1);
        esperarAte(() -> pendentesDaMatricula() == 0);
    }

    @Test
    @DisplayName("Contraste: se a notificacao fosse chamada de forma sincrona, a falha desfaria a conclusao")
    void notificacaoSincronaDesfariaAConclusao() {
        canal.definirIndisponivel(true);

        assertThrows(IllegalStateException.class, () -> transacao.executeWithoutResult(status -> {
            matriculaService.concluir(matriculaId);
            canal.enviar(alunoId, "chamada sincrona dentro da conclusao");
        }));

        assertFalse(matriculaService.listarPorUsuario(alunoId).get(0).isConcluida(), "rollback: matricula nao concluida");
        assertEquals(0, usuarioService.buscarPorId(alunoId).getXp(), "rollback: XP perdido");
        assertEquals(0, pendentesDaMatricula(), "rollback: nem o evento foi registrado");
    }

    private long pendentesDaMatricula() {
        Long total = jdbc.queryForObject(
                "select count(*) from event_publication where completion_date is null and serialized_event like ?",
                Long.class, "%\"matriculaId\":" + matriculaId + ",%");
        return total == null ? 0 : total;
    }

    private static void esperarAte(BooleanSupplier condicao) {
        long limite = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < limite) {
            if (condicao.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        fail("condicao nao atingida em 10 s");
    }
}
