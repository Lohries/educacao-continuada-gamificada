package br.edu.gamificacao.recomendacao;

import br.edu.gamificacao.curso.CursoService;
import br.edu.gamificacao.matricula.MatriculaCreateDTO;
import br.edu.gamificacao.matricula.MatriculaService;
import br.edu.gamificacao.usuario.UsuarioCreateDTO;
import br.edu.gamificacao.usuario.UsuarioService;
import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Evidencia do ADR-003: o Core consome o ia-service por HTTP e, quando ele
 * responde com erro, demora alem do timeout ou esta com o circuito aberto,
 * devolve o comportamento degradado sem bloquear o fluxo essencial.
 * O ia-service e substituido por um stub HTTP controlado pelo teste.
 */
@SpringBootTest
@ActiveProfiles("h2")
class RecomendacaoDegradadaTest {

    enum Modo { OK, LENTO, FORA }

    private static final AtomicInteger chamadas = new AtomicInteger();
    private static volatile Modo modo = Modo.OK;
    private static final HttpServer stub = iniciarStub();

    private static HttpServer iniciarStub() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext("/recomendacoes", troca -> {
                chamadas.incrementAndGet();
                if (modo == Modo.LENTO) {
                    dormir(1500);
                }
                byte[] corpo = (modo == Modo.FORA ? "{\"erro\":\"indisponivel\"}"
                        : "{\"modelo\":\"stub-v1\",\"itens\":[{\"cursoId\":2,\"titulo\":\"TDD na Pratica\",\"pontuacao\":0.9,\"motivo\":\"stub\"}]}")
                        .getBytes(StandardCharsets.UTF_8);
                troca.getResponseHeaders().add("Content-Type", "application/json");
                troca.sendResponseHeaders(modo == Modo.FORA ? 503 : 200, corpo.length);
                troca.getResponseBody().write(corpo);
                troca.close();
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @DynamicPropertySource
    static void apontarParaStub(DynamicPropertyRegistry registro) {
        registro.add("ia.url", () -> "http://127.0.0.1:" + stub.getAddress().getPort());
        registro.add("ia.timeout-ms", () -> "300");
    }

    @AfterAll
    static void pararStub() {
        stub.stop(0);
    }

    @Autowired
    private RecomendacaoService recomendacaoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private CircuitBreakerRegistry circuitos;

    private Long alunoId;

    @BeforeEach
    void setUp() {
        circuitos.circuitBreaker("ia-service").reset();
        chamadas.set(0);
        modo = Modo.OK;
        alunoId = usuarioService.criar(new UsuarioCreateDTO("Aluno IA", "ia." + UUID.randomUUID() + "@teste.com")).getId();
    }

    @Test
    @DisplayName("Com a IA no ar, a recomendacao vem do ia-service")
    void iaNoAr() {
        Recomendacoes r = recomendacaoService.recomendar(alunoId);

        assertEquals("ia", r.origem());
        assertFalse(r.degradado());
        assertEquals("stub-v1", r.modelo());
    }

    @Test
    @DisplayName("IA respondendo erro: fallback local, sem excecao para o chamador")
    void iaComErro() {
        modo = Modo.FORA;

        Recomendacoes r = recomendacaoService.recomendar(alunoId);

        assertEquals("fallback", r.origem());
        assertTrue(r.degradado());
        assertEquals(3, r.itens().size());
        String menorXp = cursoService.listarTodos().stream()
                .min(java.util.Comparator.comparing(br.edu.gamificacao.curso.Curso::getXpRecompensa)
                        .thenComparing(br.edu.gamificacao.curso.Curso::getId))
                .orElseThrow().getTitulo();
        assertEquals(menorXp, r.itens().get(0).titulo());
    }

    @Test
    @DisplayName("IA lenta: o timeout corta a espera e o fallback responde rapido")
    void iaLenta() {
        modo = Modo.LENTO;

        long inicio = System.nanoTime();
        Recomendacoes r = recomendacaoService.recomendar(alunoId);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        assertEquals("fallback", r.origem());
        assertTrue(ms < 1500, "esperou " + ms + " ms; o timeout e de 300 ms");
    }

    @Test
    @DisplayName("Falhas seguidas abrem o circuito e o Core para de chamar a IA")
    void circuitoAbre() {
        modo = Modo.FORA;

        for (int i = 0; i < 6; i++) {
            recomendacaoService.recomendar(alunoId);
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitos.circuitBreaker("ia-service").getState());
        assertEquals(3, chamadas.get(), "depois de aberto o circuito, as chamadas nao chegam ao ia-service");
    }

    @Test
    @DisplayName("Com a IA fora, o fluxo essencial (matricula e conclusao com XP) continua funcionando")
    void fluxoEssencialSemIa() {
        modo = Modo.FORA;
        Long cursoId = cursoService.listarTodos().get(0).getId();

        Long matriculaId = matriculaService.matricular(new MatriculaCreateDTO(alunoId, cursoId)).getId();
        matriculaService.concluir(matriculaId);

        assertTrue(usuarioService.buscarPorId(alunoId).getXp() > 0);
        assertEquals("fallback", recomendacaoService.recomendar(alunoId).origem());
    }
}
