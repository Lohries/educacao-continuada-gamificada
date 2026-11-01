package br.edu.gamificacao.recomendacao.internal;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Cliente HTTP do ia-service (FastAPI). Timeout curto e circuit breaker:
 * a IA pode cair ou ficar lenta sem segurar threads do Core (ADR-003).
 * Quem chama decide o comportamento degradado.
 */
@Component
public class IaServiceClient {

    public record CursoIa(Long id, String titulo, String descricao, Integer xpRecompensa) {
    }

    public record PedidoRecomendacao(Long alunoId, List<Long> cursosConcluidos, List<CursoIa> catalogo, int limite) {
    }

    public record ItemIa(Long cursoId, String titulo, double pontuacao, String motivo) {
    }

    public record RespostaRecomendacao(String modelo, List<ItemIa> itens) {
    }

    public record DocumentoIa(String id, String titulo, String texto) {
    }

    public record PedidoPergunta(String pergunta, List<DocumentoIa> documentos) {
    }

    public record FonteIa(String id, String titulo, double pontuacao) {
    }

    public record RespostaPergunta(String resposta, List<FonteIa> fontes, String gerador) {
    }

    private final RestClient http;

    public IaServiceClient(RestClient.Builder builder,
                           @Value("${ia.url}") String url,
                           @Value("${ia.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(timeoutMs);
        fabrica.setReadTimeout(timeoutMs);
        this.http = builder.baseUrl(url).requestFactory(fabrica).build();
    }

    @CircuitBreaker(name = "ia-service")
    public RespostaRecomendacao recomendar(PedidoRecomendacao pedido) {
        return http.post().uri("/recomendacoes").body(pedido).retrieve().body(RespostaRecomendacao.class);
    }

    @CircuitBreaker(name = "ia-service")
    public RespostaPergunta perguntar(PedidoPergunta pedido) {
        return http.post().uri("/assistente/perguntas").body(pedido).retrieve().body(RespostaPergunta.class);
    }
}
