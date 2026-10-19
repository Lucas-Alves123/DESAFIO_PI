package com.mesadj.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstrumentoTest {

    // H1 - adicionar instrumento

    @Test
    void nomeEhGuardadoEmMinusculoESemEspacos() {
        Instrumento i = new Instrumento("  Bateria ", "bateria.wav");
        assertThat(i.getNome()).isEqualTo("bateria");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "baixo eletrico", "guitarra!"})
    void nomeInvalidoNaoEhAceito(String nome) {
        assertThatThrownBy(() -> new Instrumento(nome, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void nomeNuloNaoEhAceito() {
        assertThatThrownBy(() -> new Instrumento(null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void instrumentoNovoComecaPausadoCom120BpmEVolume80() {
        Instrumento i = new Instrumento("baixo", null);
        assertThat(i.isTocando()).isFalse();
        assertThat(i.getBpm()).isEqualTo(120);
        assertThat(i.getVolume()).isEqualTo(80);
    }

    // H2 - controlar o ritmo (BPM)

    @ParameterizedTest
    @ValueSource(ints = {40, 120, 300})
    void aceitaBpmDentroDaFaixa(int bpm) {
        Instrumento i = new Instrumento("bateria", null);
        i.alterarBpm(bpm);
        assertThat(i.getBpm()).isEqualTo(bpm);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, 39, 301, 1000})
    void recusaBpmForaDaFaixaEMantemOValorAnterior(int bpm) {
        Instrumento i = new Instrumento("bateria", null);
        assertThatThrownBy(() -> i.alterarBpm(bpm))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("40")
                .hasMessageContaining("300");
        assertThat(i.getBpm()).isEqualTo(120);
    }

    @Test
    @DisplayName("intervalo entre batidas = 60000 / BPM (era assim na versão de threads)")
    void calculaIntervaloEntreBatidas() {
        Instrumento i = new Instrumento("bateria", null);
        i.alterarBpm(150);
        assertThat(i.intervaloBatidaMs()).isEqualTo(400);
    }

    // H3 - tocar, pausar e volume

    @Test
    void tocarEPausar() {
        Instrumento i = new Instrumento("baixo", null);
        i.tocar();
        assertThat(i.isTocando()).isTrue();
        i.pausar();
        assertThat(i.isTocando()).isFalse();
    }

    @Test
    void volumeAcimaDe100FicaEm100() {
        Instrumento i = new Instrumento("baixo", null);
        i.ajustarVolume(150);
        assertThat(i.getVolume()).isEqualTo(100);
    }

    @Test
    void volumeNegativoFicaEmZero() {
        Instrumento i = new Instrumento("baixo", null);
        i.ajustarVolume(-5);
        assertThat(i.getVolume()).isZero();
    }

    @Test
    void volumeNormalEhAplicado() {
        Instrumento i = new Instrumento("baixo", null);
        i.ajustarVolume(35);
        assertThat(i.getVolume()).isEqualTo(35);
    }
}
