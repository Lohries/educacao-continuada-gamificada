package br.edu.gamificacao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ADR-006: as metricas que sustentam as decisoes anteriores precisam existir
 * no endpoint lido pelo Prometheus; se alguem remover uma delas, o pipeline quebra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@ActiveProfiles("h2")
class MetricasTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void endpointPrometheusExpoeAsMetricasArquiteturais() throws Exception {
        mvc.perform(get("/api/ranking")).andExpect(status().isOk());

        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http_server_requests_seconds_bucket{")))
                .andExpect(content().string(containsString("uri=\"/api/ranking\"")))
                .andExpect(content().string(containsString("gamificacao_conclusoes_total")))
                .andExpect(content().string(containsString("eventos_pendentes")))
                .andExpect(content().string(containsString("resilience4j_circuitbreaker_state{")));
    }
}
