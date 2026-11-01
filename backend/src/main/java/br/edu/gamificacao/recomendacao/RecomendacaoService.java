package br.edu.gamificacao.recomendacao;

import br.edu.gamificacao.curso.Curso;
import br.edu.gamificacao.curso.CursoService;
import br.edu.gamificacao.matricula.Matricula;
import br.edu.gamificacao.matricula.MatriculaService;
import br.edu.gamificacao.recomendacao.internal.IaServiceClient;
import br.edu.gamificacao.recomendacao.internal.IaServiceClient.CursoIa;
import br.edu.gamificacao.recomendacao.internal.IaServiceClient.DocumentoIa;
import br.edu.gamificacao.usuario.UsuarioService;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Recomendacao e assistente consumidos do ia-service (ADR-003). Se a IA
 * falhar, ficar lenta ou estiver com o circuito aberto, responde com o
 * comportamento degradado em vez de propagar o erro: nenhuma operacao
 * essencial do Core depende deste modulo.
 */
@Service
public class RecomendacaoService {

    static final int LIMITE = 3;

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);

    private final IaServiceClient ia;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final MatriculaService matriculaService;
    private final MeterRegistry metricas;

    public RecomendacaoService(IaServiceClient ia, UsuarioService usuarioService,
                               CursoService cursoService, MatriculaService matriculaService,
                               MeterRegistry metricas) {
        this.ia = ia;
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.matriculaService = matriculaService;
        this.metricas = metricas;
    }

    /** Metrica ia_respostas_total{recurso, origem}: evidencia do ADR-003 no Grafana (ADR-006). */
    private void contar(String recurso, String origem) {
        metricas.counter("ia.respostas", "recurso", recurso, "origem", origem).increment();
    }

    public Recomendacoes recomendar(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        List<Curso> catalogo = cursoService.listarTodos();
        Set<Long> concluidos = matriculaService.listarPorUsuario(usuarioId).stream()
                .filter(Matricula::isConcluida)
                .map(m -> m.getCurso().getId())
                .collect(Collectors.toSet());

        try {
            var resposta = ia.recomendar(new IaServiceClient.PedidoRecomendacao(
                    usuarioId, List.copyOf(concluidos),
                    catalogo.stream().map(c -> new CursoIa(c.getId(), c.getTitulo(), c.getDescricao(), c.getXpRecompensa())).toList(),
                    LIMITE));
            contar("recomendacao", "ia");
            return new Recomendacoes("ia", false, resposta.modelo(), resposta.itens().stream()
                    .map(i -> new Recomendacoes.CursoRecomendado(i.cursoId(), i.titulo(), i.pontuacao(), i.motivo()))
                    .toList());
        } catch (RuntimeException e) {
            log.warn("ia-service indisponivel para recomendacao, usando fallback: {}", e.toString());
            contar("recomendacao", "fallback");
            return recomendacaoPadrao(catalogo, concluidos);
        }
    }

    public RespostaAssistente perguntar(String pergunta) {
        List<DocumentoIa> documentos = cursoService.listarTodos().stream()
                .map(c -> new DocumentoIa("curso-" + c.getId(), c.getTitulo(), c.getDescricao() == null ? "" : c.getDescricao()))
                .toList();
        try {
            var resposta = ia.perguntar(new IaServiceClient.PedidoPergunta(pergunta, documentos));
            contar("assistente", "ia");
            return new RespostaAssistente("ia", false, resposta.resposta(),
                    resposta.fontes().stream().map(IaServiceClient.FonteIa::titulo).toList());
        } catch (RuntimeException e) {
            log.warn("ia-service indisponivel para o assistente: {}", e.toString());
            contar("assistente", "fallback");
            return new RespostaAssistente("fallback", true,
                    "O assistente esta temporariamente indisponivel. Matriculas, conclusoes e ranking continuam funcionando normalmente.",
                    List.of());
        }
    }

    /** Fallback local: cursos ainda nao concluidos, do menor para o maior XP. */
    private Recomendacoes recomendacaoPadrao(List<Curso> catalogo, Set<Long> concluidos) {
        List<Recomendacoes.CursoRecomendado> itens = catalogo.stream()
                .filter(c -> !concluidos.contains(c.getId()))
                .sorted(Comparator.comparing(Curso::getXpRecompensa).thenComparing(Curso::getId))
                .limit(LIMITE)
                .map(c -> new Recomendacoes.CursoRecomendado(c.getId(), c.getTitulo(), 0.0,
                        "recomendacao padrao (IA indisponivel)"))
                .toList();
        return new Recomendacoes("fallback", true, "regra-local-menor-xp", itens);
    }
}
