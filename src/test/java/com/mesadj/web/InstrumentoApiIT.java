package com.mesadj.web;

import com.mesadj.PostgresTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfig.class)
@Transactional
class InstrumentoApiIT {

    @Autowired
    MockMvc mvc;

    @Test
    void listaOsInstrumentosDaMesa() throws Exception {
        mvc.perform(get("/api/instrumentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("bateria"));
    }

    @Test
    void adicionaInstrumento() throws Exception {
        mvc.perform(post("/api/instrumentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Synth\",\"arquivoAudio\":\"synth.wav\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("synth"))
                .andExpect(jsonPath("$.tocando").value(false));
    }

    @Test
    void nomeRepetidoDa409() throws Exception {
        mvc.perform(post("/api/instrumentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"bateria\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void bpmForaDaFaixaDa422() throws Exception {
        mvc.perform(put("/api/instrumentos/bateria/bpm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void instrumentoQueNaoExisteDa404() throws Exception {
        mvc.perform(post("/api/instrumentos/piano/tocar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthcheckRespondeUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
