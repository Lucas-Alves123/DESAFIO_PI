package com.mesadj.aceitacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesadj.PostgresTestConfig;
import com.mesadj.domain.Instrumento;
import com.mesadj.domain.InstrumentoRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestConfig.class)
public class MesaSteps {

    @Autowired
    TestRestTemplate http;

    @Autowired
    InstrumentoRepository repository;

    private final ObjectMapper json = new ObjectMapper();
    private ResponseEntity<String> ultimaResposta;

    @Dado("que a mesa começa só com {string} e {string}")
    public void mesaInicial(String a, String b) {
        repository.deleteAll();
        repository.save(new Instrumento(a, a + ".wav"));
        repository.save(new Instrumento(b, b + ".wav"));
    }

    @Dado("que a mesa já está com {int} instrumentos")
    public void mesaCom(int quantidade) {
        for (long i = repository.count(); i < quantidade; i++) {
            repository.save(new Instrumento("extra" + i, null));
        }
    }

    @Quando("eu adiciono o instrumento {string}")
    public void adiciono(String nome) {
        ultimaResposta = enviar(HttpMethod.POST, "/api/instrumentos", Map.of("nome", nome));
    }

    @Quando("eu mudo o BPM da {string} para {int}")
    public void mudoBpm(String nome, int bpm) {
        ultimaResposta = enviar(HttpMethod.PUT, "/api/instrumentos/" + nome + "/bpm", Map.of("valor", bpm));
    }

    @Quando("eu ajusto o volume da {string} para {int}")
    public void ajustoVolume(String nome, int volume) {
        ultimaResposta = enviar(HttpMethod.PUT, "/api/instrumentos/" + nome + "/volume", Map.of("valor", volume));
    }

    @Quando("eu mando tocar o {string}")
    public void tocar(String nome) {
        ultimaResposta = enviar(HttpMethod.POST, "/api/instrumentos/" + nome + "/tocar", null);
    }

    @Quando("eu mando pausar o {string}")
    public void pausar(String nome) {
        ultimaResposta = enviar(HttpMethod.POST, "/api/instrumentos/" + nome + "/pausar", null);
    }

    @Então("a resposta tem status {int}")
    public void status(int esperado) {
        assertThat(ultimaResposta.getStatusCode().value()).isEqualTo(esperado);
    }

    @E("a mesa passa a ter {int} instrumentos")
    public void totalNaMesa(int esperado) throws Exception {
        String corpo = http.getForObject("/api/instrumentos", String.class);
        assertThat(json.readTree(corpo).size()).isEqualTo(esperado);
    }

    @Então("o instrumento {string} está pausado")
    public void estaPausado(String nome) throws Exception {
        assertThat(consultar(nome).get("tocando").asBoolean()).isFalse();
    }

    @Então("o instrumento {string} está tocando")
    public void estaTocando(String nome) throws Exception {
        assertThat(consultar(nome).get("tocando").asBoolean()).isTrue();
    }

    @E("o BPM da {string} é {int}")
    public void bpmEh(String nome, int bpm) throws Exception {
        assertThat(consultar(nome).get("bpm").asInt()).isEqualTo(bpm);
    }

    @E("o volume da {string} é {int}")
    public void volumeEh(String nome, int volume) throws Exception {
        assertThat(consultar(nome).get("volume").asInt()).isEqualTo(volume);
    }

    @E("a mensagem de erro fala {string}")
    public void mensagemDeErro(String trecho) throws Exception {
        assertThat(json.readTree(ultimaResposta.getBody()).get("detail").asText()).contains(trecho);
    }

    private JsonNode consultar(String nome) throws Exception {
        return json.readTree(http.getForObject("/api/instrumentos/" + nome, String.class));
    }

    private ResponseEntity<String> enviar(HttpMethod metodo, String url, Object corpo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.exchange(url, metodo, new HttpEntity<>(corpo, headers), String.class);
    }
}
