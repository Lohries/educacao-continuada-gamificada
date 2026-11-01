package br.edu.gamificacao.aprendizagem;

import br.edu.gamificacao.usuario.UsuarioCreateDTO;
import br.edu.gamificacao.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Entrada dos eventos IoT no Core (ADR-005): contrato e idempotencia por eventId. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class EventoAprendizagemTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoAprendizagemService service;

    private Long alunoId;

    @BeforeEach
    void setUp() {
        alunoId = usuarioService.criar(new UsuarioCreateDTO("Aluno IoT", "iot." + UUID.randomUUID() + "@teste.com")).getId();
    }

    private String evento(String eventId, String tipo) {
        return """
                {"eventId":"%s","dispositivoId":"lab-redes-sensor1","laboratorio":"lab-redes",
                 "tipo":"%s","alunoId":%d,"ocorridoEm":"2026-11-01T12:00:00Z"}""".formatted(eventId, tipo, alunoId);
    }

    @Test
    @DisplayName("Evento novo responde 201; a reentrega do mesmo eventId responde 200 e nao duplica")
    void idempotentePorEventId() throws Exception {
        String id = UUID.randomUUID().toString();

        mvc.perform(post("/api/eventos-aprendizagem").contentType(MediaType.APPLICATION_JSON).content(evento(id, "PRESENCA")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/eventos-aprendizagem").contentType(MediaType.APPLICATION_JSON).content(evento(id, "PRESENCA")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/eventos-aprendizagem").param("alunoId", alunoId.toString()))
                .andExpect(jsonPath("$.length()").value(1));
        assertEquals(1, service.listarPorAluno(alunoId).size());
    }

    @Test
    @DisplayName("Tipo fora do contrato e recusado com 400")
    void tipoInvalido() throws Exception {
        mvc.perform(post("/api/eventos-aprendizagem").contentType(MediaType.APPLICATION_JSON)
                        .content(evento(UUID.randomUUID().toString(), "DESCONHECIDO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Aluno inexistente e recusado com 404")
    void alunoInexistente() throws Exception {
        String corpo = evento(UUID.randomUUID().toString(), "CONCLUSAO_ATIVIDADE").replace(":" + alunoId + ",", ":999999,");
        mvc.perform(post("/api/eventos-aprendizagem").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNotFound());
    }
}
